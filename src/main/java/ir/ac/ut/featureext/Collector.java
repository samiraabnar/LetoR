package ir.ac.ut.featureext;

import static ir.ac.ut.common.Util.srcIreader;
import static ir.ac.ut.common.Util.suspIreader;
import static ir.ac.ut.config.Config.getLanguage;
import static ir.ac.ut.config.Config.getSrcMapPath;
import static ir.ac.ut.config.Config.getSuspMapPath;
import ir.ac.ut.common.Document;
import ir.ac.ut.common.Pair;
import ir.ac.ut.common.Sentence;
import ir.ac.ut.common.Util;
import ir.ac.ut.config.Config;
import ir.ac.ut.engine.Engine;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryparser.classic.ParseException;
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
import org.apache.lucene.util.Version;
import org.iis.plagiarismdetector.core.TextProcessor;
import org.iis.plagiarismdetector.core.wordnet.WordNetUtil;
import org.iis.ut.stanford_ner.StanfordNamedEntityRecognizer;

import edu.stanford.nlp.ling.TaggedWord;

//7: BM25
//8: LMD
//9: JM
/**
 * Created by Mah Sa on 6/27/14.
 */
public class Collector {

	public static int BIGRAM = 1;
	public static int TERMFREQ = 2;
	public static int SENTENCESINPARAGRAPH = 3;
	public static int WORDSINSENTENCE = 4;
	public static int STOPWORDSFREQ = 5;
	public static final double log2 = Math.log(2);
	public static int N = 2;
	private static Map<Integer, Set<Integer>> candidates;
	private static Map<Integer, Document> suspDocuments;
	private static Map<Integer, Document> srcDocuments;
	public static List<String> stopWords;

	public static String removeSurrendingPuncz(String str) {
		// System.out.print(str+"**");
		String out = str;
		int size = 0;
		while (out.length() != size) {
			size = out.length();
			if (out.substring(out.length() - 1, out.length()).equals(",")) {
				out = out.substring(0, out.length() - 1);
			}
			if (out.substring(out.length() - 1, out.length()).equals(".")) {
				out = out.substring(0, out.length() - 1);
			}
			if (out.substring(out.length() - 1, out.length()).equals(":")) {
				out = out.substring(0, out.length() - 1);
			}
			if (out.substring(out.length() - 1, out.length()).equals("(")) {
				out = out.substring(0, out.length() - 1);
			}
			if (out.substring(out.length() - 1, out.length()).equals(")")) {
				out = out.substring(0, out.length() - 1);
			}
			if (out.substring(0, 1).equals("(")) {
				out = out.substring(1);
			}
			if (out.substring(0, 1).equals(")")) {
				out = out.substring(1);
			}
			if (out.substring(0, 1).equals(".")) {
				out = out.substring(1);
			}
			if (out.substring(0, 1).equals(",")) {
				out = out.substring(1);
			}
			if (out.substring(0, 1).equals(":")) {
				out = out.substring(1);
			}
		}
		// System.out.println(out.toLowerCase());
		return out;
	}

	public static void namedEntity() throws Exception {
		// addDocuments();
		BufferedReader breader = new BufferedReader(new FileReader(
				Config.getFeaturesPath()));
		BufferedWriter bwriter = new BufferedWriter(new FileWriter(
				Config.getFeaturesPath() + "s"), 10 * 1024);
		// for(Map.Entry<Integer, Set<Integer>> entity: candidates.entrySet()) {
		// Iterator<Integer> iterator = entity.getValue().iterator();
		String line = breader.readLine();
		while (line != null) {
			int suspId = Integer.parseInt(line.split(" ")[1].substring(line
					.split(" ")[1].indexOf(":") + 1));
			int srcId = Integer.parseInt(line.split(" ")[12]);
			String[] words = suspIreader.document(suspId).get("TEXT")
					.split(" ");
			HashSet<String> special = new HashSet<String>();
			for (int i = 1; i < words.length; i++) {
				if (!words[i].contains("\n")) {
					if ((!words[i].toLowerCase().equals(words[i]))) {
						if (words[i].length() > 1
								&& !words[i - 1].contains(".")) {
							special.add(removeSurrendingPuncz(words[i]));
						}
					}
				}
			}
			String src = srcIreader.document(srcId).get("TEXT");
			Object[] spwords = special.toArray();
			double count = 0;
			for (int i = 0; i < spwords.length; i++) {
				if (src.contains((String) spwords[i])) {
					count++;
				}
			}
			double num = (double) (count / ((double) special.size()));
			System.out.println("c: " + num);
			bwriter.write(line.split("#")[0] + "10:" + num + " #"
					+ line.split("#")[1] + "\n");
			line = breader.readLine();
		}
		bwriter.close();
		breader.close();
	}

	public static void secondTypeFeatures() throws IOException {
		computeScore();
	}

	public static void getStopWords() {
		try {
			BufferedReader in = new BufferedReader(new FileReader(
					Config.getStopWordsPath()));
			stopWords = new ArrayList<String>();
			String line = in.readLine();
			while (line != null) {
				stopWords.add(line);
				line = in.readLine();
			}
			in.close();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	public static Float computeKL(Map<String, Float> susp,
			Map<String, Float> src) {
		HashSet<String> intersect = new HashSet<String>(susp.keySet());
		intersect.retainAll(src.keySet());
		Double klDiv = 0D;
		double[] p1 = new double[intersect.size()];
		double[] p2 = new double[intersect.size()];
		Iterator<String> it = intersect.iterator();
		int k = 0;
		while (it.hasNext()) {
			String next = (String) it.next();
			p1[k] = susp.get(next);
			p2[k++] = src.get(next);
		}
		for (int u = 0; u < p1.length; ++u) {
			if (p1[u] == 0) {
				continue;
			}
			if (p2[u] == 0.0) {
				continue;
			}
			klDiv += p1[u] * (Math.log(p1[u] / p2[u]) / log2);
		}
		return klDiv.floatValue();
	}

	public static Float computeDiceSimilarity(Set<String> susp, Set<String> src) {
		HashSet<String> intersect = new HashSet<String>(susp);
		intersect.retainAll(src);
		float intersectSize = intersect.size();
		float unionSize = src.size() + susp.size() - intersectSize;
		return (intersectSize / unionSize);
	}

	public static Float computeCosinSimilarity(Map<String, Float> susp,
			Map<String, Float> src) {
		HashSet<String> intersect = new HashSet<String>(susp.keySet());
		intersect.retainAll(src.keySet());

		Float simScore = 0F;
		for (String term : intersect) {
			simScore += susp.get(term) * src.get(term);
		}
		return (simScore);
	}

	public static void addDocuments() throws Exception {
		Map<String, Integer> suspDocMap = Util.loadDocMap(getSuspMapPath());
		Map<String, Integer> srcDocMap = Util.loadDocMap(getSrcMapPath());

		for (int susp : suspDocMap.values()) {
			org.apache.lucene.document.Document doc = suspIreader
					.document(susp);
			suspDocuments.put(susp,
					new Document(doc.get("TEXT"), Config.getLanguage(), susp));
		}

		System.out.println("Src+second");
		for (int src : srcDocMap.values()) {
			org.apache.lucene.document.Document doc = srcIreader.document(src);
			srcDocuments.put(src,
					new Document(doc.get("TEXT"), Config.getLanguage(), src));
		}
	}

	public static Map<String, Float> getStopWordsDist(Document doc) {
		Map<String, Float> result = new HashMap<String, Float>();
		for (String sw : stopWords) {
			result.put(sw, 0.0F);
		}
		for (String word : doc.getOrderedTerms()) {
			if (stopWords.contains(word)) {
				result.put(word, result.get(word) + 1.0F);
			}
		}

		for (String key : result.keySet()) {
			result.put(key, result.get(key) / doc.getDocLength());
		}

		return result;
	}

	public static Map<String, Float> numOfSentencesInPargraph(Document document)
			throws IOException {
		Map<String, Float> map = new HashMap<String, Float>();
		for (int i = 1; i <= 50; i++) {
			map.put(String.valueOf(i), 0.0F);
		}
		for (int d : document.getParagraphLength()) {
			int temp = d;
			if (temp > 50) {
				temp = 50;
			}
			if (temp == 0) {
				continue;
			}
			map.put(String.valueOf(temp), map.get(String.valueOf(temp)) + 1.0F
					);
		}
		
		for(String key: map.keySet())
		{
			map.put(key, map.get(key)/ document.getSentences().size());
		}
		return map;
	}

	public static HashMap<String, Float> getAllTermsDist(Document document) {
		HashMap<String, Float> numOfwords = new HashMap<String, Float>();
		for (String word : document.getOrderedTerms()) {
			if (numOfwords.keySet().contains(word)) {
				numOfwords.put(word, numOfwords.get(word) + 1);
			} else {
				numOfwords.put(word, 1.0F);
			}
		}

		HashMap<String, Float> frequencyOfWordsLength = new HashMap<String, Float>();
		for (String keyValue : numOfwords.keySet()) {
			float numOfword = numOfwords.get(keyValue);
			float frequency = numOfword / document.getOrderedTerms().size();
			frequencyOfWordsLength.put(keyValue, frequency);
		}
		return frequencyOfWordsLength;
	}

	public static HashMap<String, Float> numOfWordsInSentence(Document document) {
		HashMap<String, Integer> numOfWords = new HashMap<String, Integer>();
		HashMap<String, Float> frequencyOfWordNumber = new HashMap<String, Float>();
		float totalNumOfSentences = document.getSentences().size();

		for (int i = 0; i < 33; i++) {
			numOfWords.put(String.valueOf(i), 0);
		}
		for (Sentence sentence : document.getSentences()) {
			int numberOfWords = sentence.getWords().size();
			String numberOfWordOfSentence = String.valueOf(numberOfWords);
			// System.out.println(numberOfWordOfSentence+"     "+sentence.getSentence());

			// if(numOfWords.keySet().contains(numberOfWordOfSentence))
			if (numberOfWords > 30) {
				numberOfWordOfSentence = String.valueOf("31");
			}

			numOfWords.put(numberOfWordOfSentence,
					numOfWords.get(numberOfWordOfSentence) + 1);

			// else
			// numOfWords.put(numberOfWordOfSentence, 1);
		}
		for (String keyValue : numOfWords.keySet()) {
			float numOfWord = numOfWords.get(keyValue);
			float frequency = numOfWord / totalNumOfSentences;
			frequencyOfWordNumber.put(keyValue, frequency);
		}
		// frequencyOfWordNumber.put("32",sum/totalNumOfSentences);

		return frequencyOfWordNumber;
	}

	public static void computeScore() throws IOException {
		QueryParser qParser;
		Map<String, Integer> suspDocMap = Util.loadDocMap(getSuspMapPath());
		Map<Integer, Set<Pair<Integer, Float>>> scoreMap = new HashMap<Integer, Set<Pair<Integer, Float>>>();
		for (int sid : suspDocMap.values()) {
			org.apache.lucene.document.Document document = suspIreader
					.document(sid);
			String query = document.get("TEXT");
			BooleanQuery.setMaxClauseCount(query.split("\\s+").length);
			if (getLanguage().equals("EN")) {
				qParser = new QueryParser(Version.LUCENE_47, "TEXT",
						Engine.MyEnglishAnalyzer(false, true));
			} else if (getLanguage().equals("FA")) {
				qParser = new QueryParser(Version.LUCENE_47, "TEXT",
						Engine.MyPersianAnalyzer(false, true));
			} else {
				qParser = new QueryParser(Version.LUCENE_47, "TEXT",
						Engine.MyEnglishAnalyzer(false, true));
			}
			Query q = null;
			try {
				q = qParser.parse(QueryParser.escape(query));
			} catch (ParseException e) {
				e.printStackTrace();
			}
			Similarity simFunction = new LMDirichletSimilarity(0.1F);
			IndexSearcher isearcher = new IndexSearcher(srcIreader);
			isearcher.setSimilarity(simFunction);
			TopFieldCollector tfc = TopFieldCollector.create(Sort.RELEVANCE,
					3500, true, true, true, false);
			isearcher.search(q, tfc);
			TopDocs results = tfc.topDocs();
			ScoreDoc[] hits = results.scoreDocs;
			// writeFeat4(hits);
			System.out.println("sid" + sid);
			try {
				Iterator iterator = candidates.get(sid).iterator();
				int next = 0;
				while (iterator.hasNext()) {
					try {
						next = (Integer) iterator.next();
						if (scoreMap.containsKey(sid)) {
							Set<Pair<Integer, Float>> set = scoreMap.get(sid);
							set.add(new Pair<Integer, Float>(next,
									hits[next].score));
							scoreMap.put(sid, set);
						} else {
							Set<Pair<Integer, Float>> set = new HashSet<Pair<Integer, Float>>();
							set.add(new Pair<Integer, Float>(next,
									hits[next].score));
							scoreMap.put(sid, set);
						}
					} catch (IndexOutOfBoundsException e) {
						if (scoreMap.containsKey(sid)) {
							scoreMap.get(sid).add(
									new Pair<Integer, Float>(next, Float
											.valueOf(0)));
						} else {
							Set<Pair<Integer, Float>> set = new HashSet<Pair<Integer, Float>>();
							set.add(new Pair<Integer, Float>(next, Float
									.valueOf(0)));
							scoreMap.put(sid, set);
						}
					}
				}
			} catch (NullPointerException e) {

			}
		}
		writeFeat4(scoreMap);
	}

	public static void writeFeat4(
			Map<Integer, Set<Pair<Integer, Float>>> scoreMap) {
		// for(Map.Entry<Pair<Integer, Integer>, Float> s:scoreMap.entrySet())
		// System.out.println("ddd"+s.getKey().getFirst()+" "+s.getKey().getSecond()+" "+s.getValue());

		try {
			BufferedWriter bwriter = new BufferedWriter(new FileWriter(
					(Config.getFeaturesPath() + "s")));
			BufferedReader breader = new BufferedReader(new FileReader(
					Config.getFeaturesPath()));
			String line = breader.readLine();
			while (line != null) {
				String[] split = line.split(" ");
				int i = Integer.parseInt(split[0].substring(
						split[0].indexOf(":") + 1, (split[0].length())));
				int j = Integer.parseInt(split[10]);
				Set<Pair<Integer, Float>> pairs = scoreMap.get(i);
				Iterator<Pair<Integer, Float>> iterator = pairs.iterator();
				float s = 0;
				while (iterator.hasNext()) {
					Pair<Integer, Float> pair = (Pair<Integer, Float>) iterator
							.next();
					if (pair.getFirst() == j) {
						s = pair.getSecond();
						break;
					}
				}
				System.out.println(s);
				bwriter.write(split[0] + " " + split[1] + " " + split[2] + " "
						+ split[3] + " " + split[4] + " " + split[5] + " "
						+ split[6] + " " + split[7] + " " + split[8] + " 9:"
						+ s + " " + split[9] + " " + split[10] + "\n");
				line = breader.readLine();
			}
			bwriter.close();
			breader.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static Map<String, Float> nGramOnAllTerms(Document document, int n) {
		Map<String, Float> result = new HashMap<String, Float>();
		for (int i = 0; i < document.getOrderedTerms().size(); i++) {
			String ngram = "";
			List<String> ngramParts = new ArrayList(document.getOrderedTerms()
					.subList(i, i + n));
			for (int j = 0; j < n; j++) {
				ngram += ngramParts.get(j);
			}
			if (!result.containsKey(ngram)) {
				result.put(ngram, 0.0F);
			}
			result.put(ngram, result.get(ngram) + 1.0F);
		}

		for (String key : result.keySet()) {
			result.put(key, result.get(key) / document.getOrderedTerms().size());
		}
		return result;
	}

	public static Map<String, Float> getSortedStopWordsmGram(
			Document document, Integer m) {
		Map<String, Float> result = new HashMap<String, Float>();
		List<String> stopwords = new ArrayList<String>();
		for (int i = 0; i < document.getOrderedTerms().size(); i++) {
			if (stopWords.contains(document.getOrderedTerms().get(i).toLowerCase())) {
				stopwords.add(document.getOrderedTerms().get(i));
			}
		}

		for (int i = 0; i < (stopwords.size() - m); i++) {
			String ngram = "";
			List<String> sortedStopwordmgram = new ArrayList(stopwords.subList(
					i, i + m));
			Collections.sort(sortedStopwordmgram);
			for (int j = 0; j < m; j++) {
				ngram += " "+sortedStopwordmgram.get(j);
			}
			ngram = ngram.trim();

			if (!result.containsKey(ngram)) {
				result.put(ngram, 0.0F);
			}
			result.put(ngram, result.get(ngram) + 1.0F);
		}

		for (String stopwordmgram : result.keySet()) {
			result.put(stopwordmgram, result.get(stopwordmgram)
					/ document.getOrderedTerms().size());
		}
		return result;
	}

	public static Map<String, Float> getRelativeFrequencyOfMostFrequentWords(
			Document doc, ArrayList<String> frequentwords) {
		Map<String, Float> result = new HashMap<String, Float>();
		for (String sw : frequentwords) {
			result.put(sw, 0.0F);
		}
		for (String word : doc.getOrderedTerms()) {
			if (frequentwords.contains(word)) {
				result.put(word, result.get(word) + 1.0F);
			}
		}

		for (String key : result.keySet()) {
			result.put(key, result.get(key) / doc.getOrderedTerms().size());
		}

		return result;
	}

	public static Map<String, Float> sortedNGram(Document document, int n) {
		Map<String, Float> result = new HashMap<String, Float>();
		List<String> words = new ArrayList<String>();

		for (int i = 0; i < document.getOrderedTerms().size(); i++) {
			if (!stopWords.contains(document.getOrderedTerms().get(i))) {
				words.add(document.getOrderedTerms().get(i));
			}
		}

		for (int i = 0; i < (words.size() - n); i++) {
			String ngram = "";
			List<String> sortedWordMGram = new ArrayList(
					words.subList(i, i + n));
			Collections.sort(sortedWordMGram);
			for (int j = 0; j < n; j++) {
				ngram += " "+sortedWordMGram.get(j);
			}
			ngram = ngram.trim();
			if (!result.containsKey(ngram)) {
				result.put(ngram, 0.0F);
			}
			result.put(ngram, result.get(ngram) + 1.0F);
		}

		for (String ngram : result.keySet()) {
			result.put(ngram, result.get(ngram) / words.size());
		}

		return result;
	}

	static WordNetUtil wordnetUtil = new WordNetUtil();

	public static Map<String, Float> getSynsetsDist(
			Map<String, Float> documentTermDist) {

		Map<String, Float> synsetDist = new HashMap<String, Float>();

		for (String word : documentTermDist.keySet()) {
			Set<Long> wordSynsets = wordnetUtil.getAllSynsets(word);
			for (Long synsetId : wordSynsets) {
				if (!synsetDist.containsKey(synsetId.toString())) {
					synsetDist.put(synsetId.toString(), 0F);
				}

				synsetDist
						.put(synsetId.toString(),
								synsetDist.get(synsetId.toString())
										+ (documentTermDist.get(word)
												.floatValue() / wordSynsets
												.size()));
			}
		}

		return synsetDist;
	}

//	private static Annotator ann = new Annotator(0.0);

	public static Map<String, Float> getNamedEntitiesDist(Document doc,
			IndexReader ireader) throws IOException {
		Map<String, Float> namedEntityDist = new HashMap<String, Float>();

		List<String> namedEntities = StanfordNamedEntityRecognizer.NER(ireader.document(
				doc.getDocId()).get("TEXT"));
		for (int i = 0; i < (namedEntities.size()); i++) {

			if (!namedEntityDist.containsKey(namedEntities.get(i))) {
				namedEntityDist.put(namedEntities.get(i), 0.0F);
			}
			namedEntityDist.put(namedEntities.get(i),
					namedEntityDist.get(namedEntities.get(i)) + 1.0F);
		}

		for (String ngram : namedEntityDist.keySet()) {
			namedEntityDist.put(ngram, namedEntityDist.get(ngram)
					/ namedEntities.size());
		}
		return namedEntityDist;
	}

//	public static Map<String, Float> getEntitiesDist(Document doc,
//			IndexReader ireader) throws IOException {
//		Map<String, Float> entityDist = new HashMap<String, Float>();
//
//		List<String> entities  = ann.getNamedEntities(ireader.document(
//				doc.getDocId()).get("TEXT"));
//		
//		for (int i = 0; i < (entities.size()); i++) {
//
//			if (!entityDist.containsKey(entities.get(i))) {
//				entityDist.put(entities.get(i), 0.0);
//			}
//			entityDist.put(entities.get(i),
//					entityDist.get(entities.get(i)) + 1.0);
//		}
//
//		for (String ngram : entityDist.keySet()) {
//			entityDist.put(ngram, entityDist.get(ngram)
//					/ entityDist.size());
//		}
//		return entityDist;
//	}
	public static List<String> getOrderedPOS(Document document,
			IndexReader iReader) throws IOException {

		List<String> posList = new ArrayList<String>();
		String docText = iReader.document(document.getDocId()).get("TEXT");
		List<List<TaggedWord>> taggedWords = TextProcessor.tagText(docText);
		for (List<TaggedWord> taggedSent : taggedWords) {
			for (TaggedWord taggedword : taggedSent) {

				posList.add(taggedword.tag());
			}
		}

		return posList;
	}

	public static Map<String, Float> getPOSkGram(List<String> posList, int k)
			throws IOException {

		Map<String, Float> poskGramDist = new HashMap<String, Float>();
		for (int i = 0; i < (posList.size() - k); i++) {
			String ngram = "";
			List<String> poskGram = new ArrayList(posList.subList(i, i + k));
			for (int j = 0; j < k; j++) {
				ngram += " "+ poskGram.get(j);
			}
			ngram = ngram.trim();

			if (!poskGramDist.containsKey(ngram)) {
				poskGramDist.put(ngram, 0.0F);
			}
			poskGramDist.put(ngram, poskGramDist.get(ngram) + 1.0F);
		}

		for (String ngram : poskGramDist.keySet()) {
			poskGramDist.put(ngram, poskGramDist.get(ngram) / posList.size());
		}

		return poskGramDist;
	}

	public static Map<String, Float> getPunctuationDist(Document document,IndexReader ireader) throws IOException {
		// TODO Auto-generated method stub
		Map<String, Float> result = new HashMap<String, Float>();
		List<String> puncs = new ArrayList<String>();
		String text = ireader.document(document.getDocId()).get("TEXT");
		
		 Pattern p = Pattern.compile("\\p{Punct}+");
		 Matcher m = p.matcher(text);
		 
		while (m.find()) {
				puncs.add(m.group());
		}

		for (int i = 0; i < puncs.size() ; i++) {
			
			if (!result.containsKey(puncs.get(i))) {
				result.put(puncs.get(i), 0.0F);
			}
			result.put(puncs.get(i), result.get(puncs.get(i)) + 1.0F);
		}

		for (String punc : result.keySet()) {
			result.put(punc, result.get(punc)
					/ document.getOrderedTerms().size());
		}
		return result;	}

}
