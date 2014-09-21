package ir.ac.ut.engine;

import static ir.ac.ut.config.Config.configFile;
import static ir.ac.ut.config.Config.getCandidatesMapPath;
import static ir.ac.ut.config.Config.getLanguage;
import static ir.ac.ut.config.Config.getSrcIndexPath;
import static ir.ac.ut.config.Config.getSrcMapPath;
import static ir.ac.ut.config.Config.getSuspMapPath;
import ir.ac.ut.common.Util;
import ir.ac.ut.config.Config;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.io.Serializable;
import java.sql.SQLException;
import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import javax.xml.parsers.ParserConfigurationException;

import oracle.jrockit.jfr.parser.ParseException;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.Sort;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.TopFieldCollector;
import org.apache.lucene.search.similarities.LMDirichletSimilarity;
import org.apache.lucene.search.similarities.Similarity;
import org.apache.lucene.store.SimpleFSDirectory;
import org.apache.lucene.util.Version;
import org.xml.sax.SAXException;

public class Retrieval extends Engine implements Serializable {

	public static IndexReader ireader = null;
	static {
		try {
			ireader = IndexReader.open(new SimpleFSDirectory(new File(
					getSrcIndexPath())));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	public static int SUSPCOUNT = 398;
	public static int SRCCOUNT = 489;

	public static ScoreDoc[] search(String query, String qId)
			throws IOException, ParseException,
			org.apache.lucene.queryparser.classic.ParseException {
		float mu = (float) 1000;
		QueryParser qParser;
		BooleanQuery.setMaxClauseCount(query.split("\\s+").length);
		if (getLanguage().equals("EN"))
			qParser = new QueryParser(Version.LUCENE_47, "TEXT",
					MyEnglishAnalyzer(false, true));
		else if (getLanguage().equals("FA"))
			qParser = new QueryParser(Version.LUCENE_47, "TEXT",
					MyPersianAnalyzer(false, true));
		else
			qParser = new QueryParser(Version.LUCENE_47, "TEXT",
					MyEnglishAnalyzer(false, true));
		Query q = qParser.parse(QueryParser.escape(query));
		Similarity simFunction = new LMDirichletSimilarity(mu);
		// Similarity simFunction = new BM25Similarity();
		IndexSearcher isearcher = new IndexSearcher(ireader);
		isearcher.setSimilarity(simFunction);
		TopFieldCollector tfc = TopFieldCollector.create(Sort.RELEVANCE,
				Integer.parseInt(configFile.getProperty("topK")), true, true,
				true, false);
		isearcher.search(q, tfc);
		TopDocs results = tfc.topDocs();
		ScoreDoc[] hits = results.scoreDocs;
		reportInTREC(hits, qId);
		return hits;
	}

	// public static void main(String[] args) throws ParseException,
	// org.apache.lucene.queryparser.classic.ParseException, IOException {
	// System.out.println("mahsa "+ireader.document(15).get("DOCID"));
	// }
	/*
	 * public Doc[] randomSearch() { Doc[] documents = new Doc[100]; Random
	 * random = new Random(); for(int i=0) try { documents[i] =
	 * ireader.document(random.nextInt()); } catch (IOException e) {
	 * System.out.println("invalid docid!"); } return documents; }
	 */

	public static void reportInTREC(ScoreDoc[] hits, String qID)
			throws IOException {
		RandomAccessFile res = new RandomAccessFile("alaki.txt", "rw");
		res.seek(res.length());
		for (int i = 0; i < hits.length; i++) {
			float Score = hits[i].score;
			Document hitDoc = ireader.document(hits[i].doc);
			String docID = hitDoc.get("DOCID");
			String line = qID.replaceAll("suspicious-document", "").replaceAll(
					".txt", "")
					+ " Q0 "
					+ docID.replaceAll("source-document", "")
					+ " "
					+ (i + 1) + " " + Score + " RUN\n";
			res.writeBytes(line);
		}
		res.close();
	}

	public static void generateCandidates() throws IOException {
		Random random = new Random();
		int k = 0;
		Map<String, Integer> suspDocMap = null;
		Map<String, Integer> srcDocMap = null;
		Map<Integer, Set<Integer>> candidates = new TreeMap<Integer, Set<Integer>>();
		suspDocMap = Util.loadDocMap(getSuspMapPath());
		srcDocMap = Util.loadDocMap(getSrcMapPath());
		AbstractList<Integer> suspDocValues = new ArrayList<Integer>(
				suspDocMap.values());
		ArrayList<Integer> srcDocValues = new ArrayList<Integer>(srcDocMap.values());
		BufferedWriter bwriter = null;
		BufferedReader breader = new BufferedReader(new FileReader(
				Config.getJudgePath()));
		String line = breader.readLine();
		while (line != null) {
			String[] split = line.split(" ");
			int suspNum = suspDocMap.get(split[0].trim());
			Set<Integer> set;
			if (candidates.containsKey(suspNum))
				set = candidates.get(suspNum);
			else
				set = new TreeSet<Integer>();

			set.add(srcDocMap.get(split[2].trim()));
			candidates.put(suspNum, set);
			line = breader.readLine();
		}
		for (int value : suspDocValues) {
			Set<Integer> set;
			if (candidates.containsKey(value))
				set = candidates.get(value);
			else
				set = new TreeSet<Integer>();
			while (set.size() != 30)
				set.add(srcDocValues.get(random.nextInt(srcDocValues.size() - 1)));
			candidates.put(value, set);
		}
		System.out.println("PHASE #1");
		try {
			bwriter = new BufferedWriter(new FileWriter(getCandidatesMapPath()));
			for (Map.Entry<Integer, Set<Integer>> entry : candidates.entrySet()) {
				for (Integer i : entry.getValue()) {
					bwriter.write(entry.getKey() + "," + i);
					bwriter.write("\n");
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		try {
			breader.close();
			bwriter.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void generateRankedCandidates() throws IOException,
			ParseException,
			org.apache.lucene.queryparser.classic.ParseException {
		Random random = new Random();
		int k = 0;
		Map<String, Integer> suspDocMap = null;
		Map<String, Integer> srcDocMap = null;
		Map<Integer, Set<Integer>> candidates = new TreeMap<Integer, Set<Integer>>();
		suspDocMap = Util.loadDocMap(getSuspMapPath());
		srcDocMap = Util.loadDocMap(getSrcMapPath());
		AbstractList<Integer> suspDocValues = new ArrayList<Integer>(
				suspDocMap.values());
		ArrayList<Integer> srcDocValues = new ArrayList<Integer>(srcDocMap.values());
		BufferedWriter bwriter = null;
		BufferedReader breader = new BufferedReader(new FileReader(
				Config.getJudgePath()));
		String line = breader.readLine();
		while (line != null) {
			String[] split = line.split(" ");
			int suspNum = suspDocMap
					.get(split[0].replaceAll(".txt", "").trim());
			Set<Integer> set;
			if (candidates.containsKey(suspNum))
				set = candidates.get(suspNum);
			else
				set = new TreeSet<Integer>();

			set.add(srcDocMap.get(split[2].replaceAll(".txt", "").trim()));
			candidates.put(suspNum, set);
			line = breader.readLine();
		}
		breader.close();
		for (int value : suspDocValues) {
			Set<Integer> set;
			if (candidates.containsKey(value))
				set = candidates.get(value);
			else
				set = new TreeSet<Integer>();
			Document suspDoc = Util.suspIreader.document(value);
			ScoreDoc[] hits = search(suspDoc.get("TEXT"), suspDoc.get("DOCID"));
			for (ScoreDoc hit : hits) {
				String idd = Util.srcIreader.document(hit.doc).get("DOCID");
				idd = idd.substring(idd.indexOf("-") + 1);
				set.add(srcDocMap.get(idd));
				System.out.println(idd);
			}
			candidates.put(value, set);
		}
		System.out.println("PHASE #1");
		try {
			bwriter = new BufferedWriter(new FileWriter(getCandidatesMapPath()));
			for (Map.Entry<Integer, Set<Integer>> entry : candidates.entrySet()) {
				for (Integer i : entry.getValue()) {
					bwriter.write(entry.getKey() + "," + i);
					bwriter.write("\n");
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		try {
			bwriter.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) throws ParserConfigurationException,
			SAXException, SQLException, IOException, ParseException,
			org.apache.lucene.queryparser.classic.ParseException {
		generateRankedCandidates();
	}
}