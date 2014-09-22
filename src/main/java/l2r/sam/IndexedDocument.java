package l2r.sam;

import ir.ac.ut.common.Sentence;
import ir.ac.ut.common.Util;
import ir.ac.ut.config.Config;
import ir.ac.ut.engine.FeaturedIndexer;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.core.SimpleAnalyzer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.util.Version;
import org.iis.plagiarismdetector.core.TextProcessor;
import org.iis.plagiarismdetector.core.lucene.IndexInfo;
import org.iis.plagiarismdetector.core.wordnet.WordNetUtil;
import org.iis.ut.stanford_ner.StanfordNamedEntityRecognizer;

import edu.stanford.nlp.ling.HasWord;
import edu.stanford.nlp.ling.TaggedWord;
import edu.stanford.nlp.process.DocumentPreprocessor;

public class IndexedDocument {

	public final static String FIELD_REAL_ID = "RealID";
	public final static String FIELD_INDEXED_ID = "IndexedID";

	public final static String FIELD_CONTENTWORDS = "ContentWords";
	public final static String FIELD_SORTED_BIGRAMS = "SortedBigrams";
	public final static String FIELD_SORTED_TRIGRAMS = "SortedTrigrams";
	public final static String FIELD_ALLTERMS = "AllTerms";
	public final static String FIELD_POSTAGS = "POSTags";
	public final static String FIELD_POS3GRAM = "POS3GRAM";
	public final static String FIELD_SYSNSETS = "Synsets";
	public final static String FIELD_NAMED_ENTITIES = "NamedEntities";
	public final static String FIELD_MOST_FREQUENT_WORDS = "MostFrequentWords";
	public final static String FIELD_LESS_FREQUENT_WORDS = "LessFrequentWords";
	public final static String FIELD_STOPWORDS = "Stopwords";
	public final static String FIELD_STOPWORDS3Gram = "Stopwords3Gram";
	public final static String FIELD_SENTENCES_LENGTH = "SentencesLength";
	public final static String FIELD_PUNCTUATIONS = "Punctuation";
	public final static String FIELD_UNIQUE_WORDS = "UniqueWords";
	public final static String FIELD_DOCUMENT_LENGTH = "DocumentLength";
	public final static String FIELD_DOCUMENT_LENGTH_UNIQUE = "DocumentLengthUnique";
	private static final String SPLITTER = " ";

	Map<String, String> features = new HashMap<String,String>();

	public String get(String key) {
		return features.get(key);
	}

	public IndexedDocument(String indexedId, String realId) throws IOException {
		features.put(FIELD_INDEXED_ID, indexedId);
		features.put(FIELD_REAL_ID, realId);
		
	}

	private List<String> documentOrderedTokens(String text) throws IOException
	{
	
		Analyzer analyzer = new SimpleAnalyzer(Version.LUCENE_CURRENT);
		TokenStream stream = analyzer.tokenStream("TEXT", new StringReader(
				text));
		stream.reset();
		List<String> tokensList = new ArrayList<String>();
		while (stream.incrementToken()) {
			
			tokensList.add(stream.getAttribute(CharTermAttribute.class
			 ) .toString());
		}
		
		return tokensList;
	}

	private static String getWordsInList(List<String> documentTerms,
			ArrayList<String> listedWords) {
		String result = new String();

		for (String word : documentTerms) {
			if (listedWords.contains(word)) {
				result += SPLITTER + word;
			}
		}

		return result;
	}

	private static String sortedNGram(List<String> documentNotStopwordTerms,
			int n) {
		String result = new String();

		for (int i = 0; i < (documentNotStopwordTerms.size() - n); i++) {
			String ngram = "";
			List<String> sortedWordMGram = new ArrayList<String>(
					documentNotStopwordTerms.subList(i, i + n));
			Collections.sort(sortedWordMGram);
			for (int j = 0; j < n; j++) {
				ngram += "0" + sortedWordMGram.get(j);
			}
			ngram = ngram.trim();

			result += SPLITTER + ngram;
		}

		return result;
	}

	private List<String> getNonStopwordTerms(List<String> documentTerms,
			Set<String> stopWords) {

		List<String> words = new ArrayList<String>();
		for (int i = 0; i < documentTerms.size(); i++) {
			if (!stopWords.contains(documentTerms.get(i))) {
				words.add(documentTerms.get(i));
			}
		}

		return words;
	}

	static WordNetUtil wordnetUtil = new WordNetUtil();

	private static String getSynsets(List<String> documentTerms) {

		String synsets = new String();

		for (String word : documentTerms) {
			Set<Long> wordSynsets = wordnetUtil.getAllSynsets(word);
			for (Long synsetId : wordSynsets) {

				synsets += SPLITTER + synsetId.toString();
			}
		}

		return synsets;
	}

	private static String getNamedEntities(String text) throws IOException {
		String namedEntitiesString = new String();

		List<String> namedEntities = StanfordNamedEntityRecognizer.NER(text);
		for (int i = 0; i < (namedEntities.size()); i++) {
			namedEntitiesString += SPLITTER + namedEntities.get(i).replaceAll("\\s+", "-");
		}
		return namedEntitiesString;
	}

	 private List<String> getOrderedPOS(String text) throws IOException {

		List<String> posList = new ArrayList<String>();
		List<List<TaggedWord>> taggedWords = TextProcessor.tagText(text);
		for (List<TaggedWord> taggedSent : taggedWords) {
			for (TaggedWord taggedword : taggedSent) {

				posList.add(taggedword.tag());
			}
		}

		return posList;
	}

	 private String getPOSkGram(List<String> posList, int k)
			throws IOException {

		String poskGrams = new String();
		for (int i = 0; i < (posList.size() - k); i++) {

			String ngram = "";
			List<String> poskGram = new ArrayList<String>(posList.subList(i, i
					+ k));
			for (int j = 0; j < k; j++) {
				ngram += "0" + poskGram.get(j);
			}
			ngram = ngram.trim();

			poskGrams += SPLITTER + ngram;
		}

		return poskGrams;
	}

	 static List <String> puncsMap = new ArrayList<String>();
	 
	public static void loadPuncMap() throws IOException
	{
		File puncFile = new File(Config.getPuncMapPath());
		if(!puncFile.exists())
		{
			puncFile.createNewFile();
		}
		BufferedReader breader = new BufferedReader(new FileReader(puncFile));
		String line = breader.readLine();
		while (line != null) {
			String[] split = line.split(" ");
			puncsMap.add(split[0]);
			line = breader.readLine();
		}
		breader.close();
	}
	
	public static void savePuncMap() throws IOException
	{
		BufferedWriter bw = new BufferedWriter(new FileWriter(Config.getPuncMapPath()));
		for(int i=0; i < puncsMap.size(); i++)
		{
			bw.write(puncsMap.get(i)+' '+i+"\n");
		}
		bw.close();
	}
	
	private String getPunctuations(String text) throws IOException {
		// TODO Auto-generated method stub
		String result = new String();

		Pattern p = Pattern.compile("\\p{Punct}+");
		Matcher m = p.matcher(text);

		while (m.find()) {
			String punc = m.group();
			if(!puncsMap.contains(punc))
				puncsMap.add(punc);
				
			result += SPLITTER + (puncsMap.indexOf(punc));
		}
		return result;
	}

	public static void fetchFeaturedDocuments(FeaturedIndexer indexer) throws Exception {

		Map<Integer, String> docIdMaps = null;
		IndexInfo indexInfo = null;
		if (Config.configFile.getProperty("selector").equals("src")) {
			docIdMaps = Util.loadInverseDocMap(Config.getSrcMapPath());
			indexInfo = new IndexInfo(Util.srcIreader);
		} else {
			docIdMaps = Util.loadInverseDocMap(Config.getSuspMapPath());

			indexInfo = new IndexInfo(Util.suspIreader);
		}

		for (Integer indexedId : docIdMaps.keySet()) {

			IndexedDocument indexedDocument = new IndexedDocument(
					indexedId.toString(), docIdMaps.get(indexedId));
			indexedDocument.computeFeatures(indexInfo, indexedId);
			indexer.indexDocument(indexedDocument);
		}

	}

	private void computeFeatures(IndexInfo indexInfo, Integer indexedId) throws Exception {
		String documentText = indexInfo.getIndexReader().document(indexedId).get("TEXT");
		List<String> documentAllOrderedTerms = documentOrderedTokens(documentText);
		List<String> documentNonStopwordTerms = getNonStopwordTerms(documentAllOrderedTerms, TextProcessor.getStopWords("EN"));
		List<String> documentStopwords = getSopwords(documentAllOrderedTerms, TextProcessor.getStopWords("EN"));
		List<Sentence> documentSentences = getDocumentSentences(documentText);
		List<String> posList = getOrderedPOS(documentText);
		List<String> uniqueTerms = getUniqueTerms(documentNonStopwordTerms);
	
		features.put(FIELD_ALLTERMS, sortedNGram(documentAllOrderedTerms, 1));
		features.put(FIELD_CONTENTWORDS,sortedNGram(documentNonStopwordTerms, 1));
		features.put(FIELD_DOCUMENT_LENGTH, indexInfo.getDocumentLength(indexedId, "TEXT").toString());
		features.put(FIELD_DOCUMENT_LENGTH_UNIQUE,  indexInfo.getNumberofUniqTermsInDocument(indexedId, "TEXT").toString());
		features.put(FIELD_LESS_FREQUENT_WORDS, getWordsInList(documentAllOrderedTerms, indexInfo.getDownTerms_DF("TEXT", 100)) );
		features.put(FIELD_MOST_FREQUENT_WORDS, getWordsInList(documentAllOrderedTerms, indexInfo.getTopTerms_DF("TEXT", 100)));
		features.put(FIELD_NAMED_ENTITIES, getNamedEntities(documentText));
		features.put(FIELD_POSTAGS, getPOSkGram(posList, 1) );
		features.put(FIELD_POS3GRAM, getPOSkGram(posList, 3));
		features.put(FIELD_PUNCTUATIONS, getPunctuations(documentText));
		features.put(FIELD_SENTENCES_LENGTH, getSentencesLength(documentSentences));
		features.put(FIELD_SORTED_BIGRAMS, sortedNGram(documentNonStopwordTerms, 2));
		features.put(FIELD_SORTED_TRIGRAMS, sortedNGram(documentNonStopwordTerms, 3));
		features.put(FIELD_STOPWORDS, sortedNGram(documentStopwords, 1));
		features.put(FIELD_STOPWORDS3Gram, sortedNGram(documentStopwords, 3));
		features.put(FIELD_SYSNSETS, getSynsets(documentNonStopwordTerms));
		features.put(FIELD_UNIQUE_WORDS, getListString(uniqueTerms));
	}

	private List<Sentence> getDocumentSentences(String documentText) throws FileNotFoundException, IOException, Exception {
		
	DocumentPreprocessor dp = new DocumentPreprocessor(new InputStreamReader( new ByteArrayInputStream(documentText.getBytes(StandardCharsets.UTF_8)))); 
    List<Sentence> sentences = new ArrayList<Sentence>() ;
	for (List<HasWord> sentence : dp) {
   	 String sentenceString = "";
  	  for(int i = 0; i< sentence.size(); i++)
  		  sentenceString += " "+sentence.get(i);
  	  
  	
  		  sentences .add(new Sentence(sentenceString.toLowerCase(), "EN"));
  	  }
    return sentences;
	}
	

	private List<String> getUniqueTerms(List<String> documentNonStopwordTerms) {
		List<String> uniqueTerms = new ArrayList<String>();
		
		
		for(String term : documentNonStopwordTerms)
		{
			if(uniqueTerms.contains(term))
					uniqueTerms.remove(term);
			else
				uniqueTerms.add(term);
		}
		return uniqueTerms;
	}
	
	String getListString(List<String> list)
	{
		String result = "";
		for(int i=0; i < list.size(); i++)
			result += SPLITTER + list.get(i);
	
		return result;
	}

	private List<String> getSopwords(List<String> documentAllOrderedTerms,
			Set<String> stopWords) {
		List<String> words = new ArrayList<String>();
		for (int i = 0; i < documentAllOrderedTerms.size(); i++) {
			if (stopWords.contains(documentAllOrderedTerms.get(i))) {
				words.add(documentAllOrderedTerms.get(i));
			}
		}

		return words;
	}

	public String getSentencesLength(List<Sentence> sentences) {
		String numOfWords ="";

		
		for (Sentence sentence : sentences) {
			int numberOfWords = sentence.getWords().size();
			numOfWords  += SPLITTER + numberOfWords;
		}
			return numOfWords;
	}
}
