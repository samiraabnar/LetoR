/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package ir.ac.ut.engine;

//import ir.ac.ut.UTPersianNormalizer.PersianNormalizerScheme;

import ir.ac.ut.config.Config;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.SQLException;
import java.util.Map;
import java.util.TreeMap;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.core.SimpleAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.SimpleFSDirectory;
import org.apache.lucene.util.Version;
import org.xml.sax.SAXException;

/**
 *
 * @author Mostafa Dehghani
 *
 **/
public class Indexer extends Engine {
	private static IndexWriter writer;
	private static String eol = System.getProperty("line.separator");
	private int i = 0;
	private static Map<String, Integer> docMap;

	public Indexer() throws IOException, ParserConfigurationException,
			SAXException, SQLException {

		Analyzer analyzer;
		if (Config.getLanguage().equals("EN"))
			analyzer = MyEnglishAnalyzer(false, false);
		else if (Config.getLanguage().equals("FA"))
			analyzer = MyPersianAnalyzer(false, false);
		else {
			analyzer = new SimpleAnalyzer(Version.LUCENE_47);
		}

		IndexWriterConfig irc = new IndexWriterConfig(Version.LUCENE_47,
				analyzer);
		writer = new IndexWriter(new SimpleFSDirectory(new File(
				Config.getIndexPath())), irc);
		readCorpus_plainText();
		writer.commit();
		writer.close();
		analyzer.close();
	}

	public void readCorpus_plainText() throws IOException {
		File curpusPath = new File(Config.getCorpusPath());
		for (File f : curpusPath.listFiles()) {
			String text = getMatn(f);
			String docId = f.getName();
			if (f.getName().contains("/"))
				docId = f.getName().substring(f.getName().lastIndexOf("/"));
			docId = docId.replaceAll(".txt", "");
			docMap.put(docId, i);
			docId = i + "-" + docId;
			i++;
			indexDocument(docId.trim(), text.trim());

		}
	}

	public static String getMatn(File f) {
		String matn = "";
		try {
			FileInputStream fstream = new FileInputStream(f);
			DataInputStream in = new DataInputStream(fstream);
			BufferedReader br = new BufferedReader(new InputStreamReader(in,
					"UTF8"));

			String line;
			while ((line = br.readLine()) != null) {
				matn = matn + line + eol;
			}

			br.close();
			in.close();
			fstream.close();
		} catch (Exception e) {
			System.err.println(e);
		}
		return matn;
	}

	public void indexDocument(String docIDBuffer, String textBuffer)
			throws IOException {
		Document doc = new Document();
		doc.add(new Field("DOCID", docIDBuffer, Field.Store.YES, Field.Index.NO));
		if (Config.getLanguage().equals("FA"))
		{
			doc.add(new Field("TEXT", Persian_Normalizer
					.persianNormalizer(textBuffer), Field.Store.YES,
					Field.Index.ANALYZED, Field.TermVector.WITH_POSITIONS));
		}
		else
		{
			doc.add(new Field("TEXT", textBuffer, Field.Store.YES,
					Field.Index.ANALYZED, Field.TermVector.WITH_POSITIONS));
		}
		writer.addDocument(doc);
	}

	public static void main(String[] args) throws ParserConfigurationException,
			SAXException, SQLException, IOException {
		docMap = new TreeMap<String, Integer>();
		try {
			new Indexer();
		} catch (IOException e) {
			e.printStackTrace();
		}
		try {
			BufferedWriter bwriter = new BufferedWriter(new FileWriter(
					Config.getMapPath()));
			for (Map.Entry<String, Integer> entry : docMap.entrySet()) {
				bwriter.write(entry.getKey() + "," + entry.getValue());
				bwriter.write("\n");
			}
			bwriter.close();
		} catch (IOException e) {
			System.out.println("doc map path doesn't exist!");
		}
	}
}
