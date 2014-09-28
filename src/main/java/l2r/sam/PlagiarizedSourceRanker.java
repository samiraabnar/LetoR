package l2r.sam;

import static ir.ac.ut.common.Util.srcIreader;
import static ir.ac.ut.common.Util.suspIreader;
import static ir.ac.ut.config.Config.getLanguage;
import static ir.ac.ut.config.Config.getSrcMapPath;
import static ir.ac.ut.config.Config.getSuspMapPath;
import ir.ac.ut.Analyzer;
import ir.ac.ut.common.Document;
import ir.ac.ut.common.Pair;
import ir.ac.ut.common.Util;
import ir.ac.ut.config.Config;
import ir.ac.ut.engine.Engine;
import ir.ac.ut.engine.FeaturedRetriever;
import ir.ac.ut.featureext.Collector;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

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
import org.apache.lucene.search.similarities.BM25Similarity;
import org.apache.lucene.search.similarities.DefaultSimilarity;
import org.apache.lucene.search.similarities.LMDirichletSimilarity;
import org.apache.lucene.search.similarities.LMJelinekMercerSimilarity;
import org.apache.lucene.search.similarities.Similarity;
import org.apache.lucene.store.SimpleFSDirectory;
import org.apache.lucene.util.Version;
import org.iis.plagiarismdetector.core.lucene.IndexInfo;

public class PlagiarizedSourceRanker {

    private static final Boolean IFSTEM = false;
    public static final double log2 = Math.log(2);
    public static int N = 2;
    private static final Boolean REMOVE_STOPWORDS = true;
    public static List<String> stopWords;
    private static final int TOPK = 1000;
    IndexInfo suspFeaturedIndex;
    IndexInfo srcFeaturedIndex;
   
    final private Integer CLASSIFIER_FEATURES_PAIRED_ALLTERMS = 1;
    final private Integer CLASSIFIER_FEATURES_PAIRED_BIGRAMS = 2;
    final private Integer CLASSIFIER_FEATURES_PAIRED_CONTENTWORDS = 3;
    final private Integer CLASSIFIER_FEATURES_PAIRED_LESSFREQUENTWORDS = 4;
    final private Integer CLASSIFIER_FEATURES_PAIRED_MOSTFREQUENTWORDS = 5;
    final private Integer CLASSIFIER_FEATURES_PAIRED_POSTAGKRAMS = 6;
    final private Integer CLASSIFIER_FEATURES_PAIRED_POSTAGS = 7;
    final private Integer CLASSIFIER_FEATURES_PAIRED_PUNCTUATIONS = 8;
    final private Integer CLASSIFIER_FEATURES_PAIRED_SENTENCESLENGTH = 9;
    final private Integer CLASSIFIER_FEATURES_PAIRED_STOPWORDSKGRAM = 10;
    final private Integer CLASSIFIER_FEATURES_PAIRED_SYNSETS = 11;
    final private Integer CLASSIFIER_FEATURES_PAIRED_TRIGRAMS = 12;
    final private Integer CLASSIFIER_FEATURES_PAIRED_UNIQUETERMS = 13;
    final private Integer CLASSIFIER_FEATURES_QUERYINDEPENDENT_SRCLENGTH = 14;
    final private Integer CLASSIFIER_FEATURES_QUERYINDEPENDENT_UNIQUESRCLENGTH = 15;
    final private Integer CLASSIFIER_FEATURES_QUERYINDEPENDENT_SRCUNIQUETERMSCOUNT = 16;
   final private Integer CLASSIFIER_FEATURES_PAIRED_NAMEDENTITIES = 17;
       final private Integer CLASSIFIER_FEATURES_PAIRED_STOPWORDS = 18;


    private static final int DOCFEATURE_CONTENTWORDS = 0;
    private static final int DOCFEATURE_LESSFREQUENTWORDS = 1;
    private static final int DOCFEATURE_MOSTFREQUENTWORDS = 2;
    private static final int DOCFEATURE_NAMEDENTITIES = 3;
    private static final int DOCFEATURE_POSTAGKGRAMS = 5;
    private static final int DOCFEATURE_POSTAGS = 6;
    private static final int DOCFEATURE_PUNCTUATIONS = 7;
    private static final int DOCFEATURE_SENTENCESLENGTH = 9;
    private static final int DOCFEATURE_SORTEDBIGRAMS = 10;
    private static final int DOCFEATURE_SORTEDTRIGRAMS = 11;
    private static final int DOCFEATURE_STOPWORDSMGRAM = 12;
    private static final int DOCFEATURE_SYNSETS = 13;
    private static final int DOCFEATURE_TERMS = 14;
    
    private IndexInfo indexInfo;
    Map<Integer, Document> srcDocuments;
    Map<Integer, Document> suspDocuments;
    private IndexInfo suspIndexInfo;



    public static void loadStopwords() throws FileNotFoundException, IOException {
            BufferedReader in = new BufferedReader(new FileReader(
                    Config.getStopWordsPath()));
            stopWords = new ArrayList<String>();
            String line = in.readLine();
            while (line != null) {
                stopWords.add(line);
                line = in.readLine();
            }
            in.close();
       
    }

    public static void main(String[] args) throws ParseException {
        PlagiarizedSourceRanker psr;
		try {
			psr = new PlagiarizedSourceRanker();
		
//        psr.makeFeaturesReady(Util.loadCandidatesMap(Config
//                    .getCandidatesMapPath()));
	psr.makeFeaturesReady(Util.loadCandidatesMapTrecFormat(Config.getCandidatesMapPath()));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public PlagiarizedSourceRanker() throws IOException
    {
    	indexInfo = new IndexInfo(IndexReader.open(new SimpleFSDirectory(new File(
    			Config.getSrcIndexPath()))));
    	suspIndexInfo =  new IndexInfo(IndexReader.open(new SimpleFSDirectory(new File(
    			Config.getSuspIndexPath()))));
    	suspFeaturedIndex = new IndexInfo(IndexReader.open(new SimpleFSDirectory(new File(
    			Config.getSuspFeaturedIndexPath()))));
        srcFeaturedIndex = new IndexInfo(IndexReader.open(new SimpleFSDirectory(new File(
    			Config.getSrcFeaturedIndexPath()))));
    //	Collector.getStopWords();
    }

    
    public TreeMap<Integer, List<Pair<TreeMap<Integer, Float>, Integer>>> extractLanguageModelBasedFeatures( Map<Integer, Set<Integer>> candidates) throws IOException,ParseException
    {
        TreeMap<Integer, List<Pair<TreeMap<Integer, Float>, Integer>>> features = new TreeMap<Integer, List<Pair<TreeMap<Integer, Float>, Integer>>>();
     
        BufferedWriter bwriter = new BufferedWriter(new FileWriter(
                Config.getFeaturesPath()));
        
        
        for(Integer suspDocId : candidates.keySet())
        {
            List<Pair<TreeMap<Integer, Float>, Integer>> suspfeatures = getSuspLanguageModelBasedFeatures(suspDocId,candidates);
            features.put(suspDocId, suspfeatures);
            String featureStr = new String();
               for (Pair<TreeMap<Integer, Float>, Integer> pair : suspfeatures) {
                    String featureline = "qid:" + suspDocId + " ";
                    for (Integer fkey : pair.getFirst().keySet()) {
                        featureline += fkey + ":" + pair.getFirst().get(fkey) + " ";
                    }
                    featureline += "# " + pair.getSecond();
                    featureStr += featureline.trim() + "\n";
                }
            

            featureStr.trim();
           

            bwriter.write(featureStr);
        
        
        }
        
        bwriter.close();       
        return features;
    }
    
    public void retrieveScoresPerField(Integer suspDocId, Map<Integer,Set<Integer>> candidates, Map<Integer,Map<Integer,Float>> documentScores,String field, Integer featureId) 
            throws IOException, ParseException
    {
    	if(suspFeaturedIndex.getIndexReader().document(suspDocId).get(field).length() > 0)
    	{
         ScoreDoc[] scores = FeaturedRetriever.search(suspFeaturedIndex.getIndexReader().document(suspDocId).get(field),
                    suspFeaturedIndex.getIndexReader().document(suspDocId).get(IndexedDocument.FIELD_REAL_ID) ,field);
           
            
            for (int i = 0; i < scores.length; i++) {
			float Score = scores[i].score;
                        int retrievedDocId = scores[i].doc;
                        if(candidates.get(suspDocId).contains(retrievedDocId))
                            documentScores.get(retrievedDocId).put(featureId,Score);
            }
    	}
    	else
    	{
    		 for (int retrievedDocId: candidates.get(suspDocId)) {
    			 documentScores.get(retrievedDocId).put(featureId,0F);
    		 }
    	}
    }
    public TreeMap<Integer, Float> fillFeatureVectorForaPair(int srcDocId, Map<Integer,Map<Integer,Float>> documentScores)
    {
        TreeMap<Integer, Float> featureVector = new TreeMap<Integer, Float>();

                featureVector.put(CLASSIFIER_FEATURES_PAIRED_CONTENTWORDS, 
                        documentScores.get(srcDocId).get(CLASSIFIER_FEATURES_PAIRED_ALLTERMS) == null ? 0F : documentScores.get(srcDocId).get(CLASSIFIER_FEATURES_PAIRED_ALLTERMS));
                featureVector.put(CLASSIFIER_FEATURES_PAIRED_BIGRAMS, 
                        documentScores.get(srcDocId).get(CLASSIFIER_FEATURES_PAIRED_BIGRAMS) == null ? 0F :documentScores.get(srcDocId).get(CLASSIFIER_FEATURES_PAIRED_BIGRAMS));
                featureVector.put(CLASSIFIER_FEATURES_PAIRED_TRIGRAMS, 
                        documentScores.get(srcDocId).get(CLASSIFIER_FEATURES_PAIRED_TRIGRAMS) == null ? 0F :documentScores.get(srcDocId).get(CLASSIFIER_FEATURES_PAIRED_TRIGRAMS));
                featureVector.put(CLASSIFIER_FEATURES_PAIRED_ALLTERMS, 
                        documentScores.get(srcDocId).get(CLASSIFIER_FEATURES_PAIRED_ALLTERMS) == null ? 0F :documentScores.get(srcDocId).get(CLASSIFIER_FEATURES_PAIRED_ALLTERMS));
                featureVector.put(CLASSIFIER_FEATURES_PAIRED_SYNSETS, 
                        documentScores.get(srcDocId).get(CLASSIFIER_FEATURES_PAIRED_ALLTERMS) == null ? 0F :documentScores.get(srcDocId).get(CLASSIFIER_FEATURES_PAIRED_ALLTERMS));
                featureVector.put(CLASSIFIER_FEATURES_PAIRED_POSTAGS, 
                        documentScores.get(srcDocId).get(CLASSIFIER_FEATURES_PAIRED_POSTAGS) == null ? 0F :documentScores.get(srcDocId).get(CLASSIFIER_FEATURES_PAIRED_POSTAGS));
                featureVector.put(CLASSIFIER_FEATURES_PAIRED_POSTAGKRAMS, 
                        documentScores.get(srcDocId).get(CLASSIFIER_FEATURES_PAIRED_POSTAGKRAMS) == null ? 0F :documentScores.get(srcDocId).get(CLASSIFIER_FEATURES_PAIRED_POSTAGKRAMS));
                featureVector.put(CLASSIFIER_FEATURES_PAIRED_NAMEDENTITIES, 
                        documentScores.get(srcDocId).get(CLASSIFIER_FEATURES_PAIRED_NAMEDENTITIES) == null ? 0F :documentScores.get(srcDocId).get(CLASSIFIER_FEATURES_PAIRED_NAMEDENTITIES));
                featureVector.put(CLASSIFIER_FEATURES_PAIRED_STOPWORDSKGRAM, 
                        documentScores.get(srcDocId).get(CLASSIFIER_FEATURES_PAIRED_STOPWORDSKGRAM) == null ? 0F :documentScores.get(srcDocId).get(CLASSIFIER_FEATURES_PAIRED_STOPWORDSKGRAM));
                featureVector.put(CLASSIFIER_FEATURES_PAIRED_MOSTFREQUENTWORDS, 
                        documentScores.get(srcDocId).get(CLASSIFIER_FEATURES_PAIRED_MOSTFREQUENTWORDS) == null ? 0F :documentScores.get(srcDocId).get(CLASSIFIER_FEATURES_PAIRED_MOSTFREQUENTWORDS));
                featureVector.put(CLASSIFIER_FEATURES_PAIRED_LESSFREQUENTWORDS, 
                        documentScores.get(srcDocId).get(CLASSIFIER_FEATURES_PAIRED_LESSFREQUENTWORDS) == null ? 0F :documentScores.get(srcDocId).get(CLASSIFIER_FEATURES_PAIRED_LESSFREQUENTWORDS));
                featureVector.put(CLASSIFIER_FEATURES_PAIRED_SENTENCESLENGTH, 
                        documentScores.get(srcDocId).get(CLASSIFIER_FEATURES_PAIRED_SENTENCESLENGTH) == null ? 0F :documentScores.get(srcDocId).get(CLASSIFIER_FEATURES_PAIRED_SENTENCESLENGTH));
                featureVector.put(CLASSIFIER_FEATURES_PAIRED_PUNCTUATIONS, 
                        documentScores.get(srcDocId).get(CLASSIFIER_FEATURES_PAIRED_PUNCTUATIONS) == null ? 0F :documentScores.get(srcDocId).get(CLASSIFIER_FEATURES_PAIRED_PUNCTUATIONS));
                featureVector.put(CLASSIFIER_FEATURES_PAIRED_UNIQUETERMS, 
                        documentScores.get(srcDocId).get(CLASSIFIER_FEATURES_PAIRED_UNIQUETERMS) == null ? 0F :documentScores.get(srcDocId).get(CLASSIFIER_FEATURES_PAIRED_UNIQUETERMS));
                
                featureVector.put(CLASSIFIER_FEATURES_QUERYINDEPENDENT_SRCLENGTH,
                        (srcFeaturedIndex.getDocumentLength(srcDocId, IndexedDocument.FIELD_ALLTERMS)).floatValue());
                featureVector.put(CLASSIFIER_FEATURES_QUERYINDEPENDENT_UNIQUESRCLENGTH, srcFeaturedIndex.getNumberofUniqTermsInDocument(srcDocId, IndexedDocument.FIELD_ALLTERMS).floatValue());
                featureVector.put(CLASSIFIER_FEATURES_QUERYINDEPENDENT_SRCUNIQUETERMSCOUNT, srcFeaturedIndex.getNumberofUniqTermsInDocument(srcDocId, IndexedDocument.FIELD_UNIQUE_WORDS).floatValue());
                return featureVector;
    }
    
    public TreeMap<Integer, Float> extractFeatures(Document suspDocument,
            Document srcDocument) throws IOException {
        System.out.println("writing phase");
        // are values are in terms of relative frequency.


        Map<Integer, Map<String, Float>> srcFeaturesMap = srcDocument
                .getFeaturesMap();
        Map<Integer, Map<String, Float>> suspFeaturesMap = suspDocument
                .getFeaturesMap();

        float contentWordsCosinSim = Collector.computeCosinSimilarity(suspFeaturesMap.get(DOCFEATURE_CONTENTWORDS),
                srcFeaturesMap.get(DOCFEATURE_CONTENTWORDS));
        float bigramCosinSim = Collector.computeCosinSimilarity(suspFeaturesMap.get(DOCFEATURE_SORTEDBIGRAMS),
                srcFeaturesMap.get(DOCFEATURE_SORTEDBIGRAMS));
        float trigramCosinSim = Collector.computeCosinSimilarity(suspFeaturesMap.get(DOCFEATURE_SORTEDTRIGRAMS),
                srcFeaturesMap.get(DOCFEATURE_SORTEDTRIGRAMS));
        float allTermsCosinSim = Collector.computeCosinSimilarity(suspFeaturesMap.get(DOCFEATURE_TERMS),
                srcFeaturesMap.get(DOCFEATURE_TERMS));
        
        float synsetsCosinSim = Collector.computeCosinSimilarity(suspFeaturesMap.get(DOCFEATURE_SYNSETS),
                srcFeaturesMap.get(DOCFEATURE_SYNSETS));
        float posTagsCosinSim = Collector.computeCosinSimilarity(suspFeaturesMap.get(DOCFEATURE_POSTAGS),
                srcFeaturesMap.get(DOCFEATURE_POSTAGS));
        float posTagsKGramCosinSim = Collector.computeCosinSimilarity(suspFeaturesMap.get(DOCFEATURE_POSTAGKGRAMS),
                srcFeaturesMap.get(DOCFEATURE_POSTAGKGRAMS));
        float namedEntietiesCosinSim = Collector.computeCosinSimilarity(suspFeaturesMap.get(DOCFEATURE_NAMEDENTITIES),
                srcFeaturesMap.get(DOCFEATURE_NAMEDENTITIES));
      //  float entietiesCosinSim = Collector.computeCosinSimilarity(suspFeaturesMap.get(DOCFEATURE_ENTITIES),
        //        srcFeaturesMap.get(DOCFEATURE_ENTITIES));
        float stopwordsMGramCosinSim = Collector.computeCosinSimilarity(suspFeaturesMap.get(DOCFEATURE_STOPWORDSMGRAM),
                srcFeaturesMap.get(DOCFEATURE_STOPWORDSMGRAM));
        float mostFrequentWordsCosinSim = Collector.computeCosinSimilarity(suspFeaturesMap.get(DOCFEATURE_MOSTFREQUENTWORDS),
                srcFeaturesMap.get(DOCFEATURE_MOSTFREQUENTWORDS));
        float lessFrequentWordsCosinSim = Collector.computeCosinSimilarity(suspFeaturesMap.get(DOCFEATURE_LESSFREQUENTWORDS),
                srcFeaturesMap.get(DOCFEATURE_LESSFREQUENTWORDS));
        float sentenceLengthsCosinSim = Collector.computeCosinSimilarity(suspFeaturesMap.get(DOCFEATURE_SENTENCESLENGTH),
                srcFeaturesMap.get(DOCFEATURE_SENTENCESLENGTH));
      
        float punctuationsCosinSim = Collector.computeCosinSimilarity(suspFeaturesMap.get(DOCFEATURE_PUNCTUATIONS),
                srcFeaturesMap.get(DOCFEATURE_PUNCTUATIONS));
        
        TreeMap<Integer, Float> featureVector = new TreeMap<Integer, Float>();

        featureVector.put(CLASSIFIER_FEATURES_PAIRED_CONTENTWORDS, contentWordsCosinSim);
        featureVector.put(CLASSIFIER_FEATURES_PAIRED_BIGRAMS, bigramCosinSim);
        featureVector.put(CLASSIFIER_FEATURES_PAIRED_TRIGRAMS, trigramCosinSim);
        featureVector.put(CLASSIFIER_FEATURES_PAIRED_ALLTERMS, allTermsCosinSim);
        featureVector.put(CLASSIFIER_FEATURES_PAIRED_SYNSETS, synsetsCosinSim);
        featureVector.put(CLASSIFIER_FEATURES_PAIRED_POSTAGS, posTagsCosinSim);
        featureVector.put(CLASSIFIER_FEATURES_PAIRED_POSTAGKRAMS, posTagsKGramCosinSim);
        featureVector.put(CLASSIFIER_FEATURES_PAIRED_NAMEDENTITIES, namedEntietiesCosinSim);
      //  featureVector.put(CLASSIFIER_FEATURES_PAIRED_ENTITIES, entietiesCosinSim);

        featureVector.put(CLASSIFIER_FEATURES_PAIRED_STOPWORDSKGRAM, stopwordsMGramCosinSim);
        featureVector.put(CLASSIFIER_FEATURES_PAIRED_MOSTFREQUENTWORDS, mostFrequentWordsCosinSim);
        featureVector.put(CLASSIFIER_FEATURES_PAIRED_LESSFREQUENTWORDS, lessFrequentWordsCosinSim);
        featureVector.put(CLASSIFIER_FEATURES_PAIRED_SENTENCESLENGTH, sentenceLengthsCosinSim);
        featureVector.put(CLASSIFIER_FEATURES_PAIRED_PUNCTUATIONS, punctuationsCosinSim);
        
        return featureVector;
    }

    public TreeMap<Integer, List<Pair<TreeMap<Integer, Float>, Integer>>> extractFeatures(
            Map<Integer, Set<Integer>> candidates) throws IOException {
        TreeMap<Integer, List<Pair<TreeMap<Integer, Float>, Integer>>> features = new TreeMap<Integer, List<Pair<TreeMap<Integer, Float>, Integer>>>();

        for (Integer suspIndxId : suspDocuments.keySet()) {

            setopDocumentFeatures(suspDocuments.get(suspIndxId),suspIndexInfo.getIndexReader());
            features.put(
                    suspIndxId,
                    getASuspCandidzFeatures(candidates.get(suspIndxId),
                            suspIndxId));

        }
        return features;
    }
    

    public void extractFeatures_StepsInFiles(Map<Integer, Set<Integer>> candidates ) throws IOException {

        Map<String, Integer> suspDocMap = null;
        Map<String, Integer> srcDocMap = null;
        try {
            suspDocMap = Util.loadDocMap(getSuspMapPath());
            srcDocMap = Util.loadDocMap(getSrcMapPath());
        } catch (IOException e1) {
            e1.printStackTrace();
        }

        suspDocuments = new TreeMap<Integer, Document>();
        srcDocuments = new TreeMap<Integer, Document>();

        for (int susp : candidates.keySet()) {
            org.apache.lucene.document.Document doc;
            try {
                doc = suspIreader.document(susp);
                 suspDocuments.put(susp,
                        new Document(doc.get("TEXT"), Config.getLanguage(),
                                susp));
            } catch (IOException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }
            

            for (int src : candidates.get(susp)) {
                try {
                	
                	if(!srcDocuments.containsKey(src))
                	{
                        org.apache.lucene.document.Document srcdoc;
                	srcdoc = srcIreader.document(src);
                    srcDocuments
                            .put(src,
                                    new Document(srcdoc.get("TEXT"), Config
                                            .getLanguage(), src));
                	}
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        }


        setupSrcDocs();
        TreeMap<Integer, List<Pair<TreeMap<Integer, Float>, Integer>>> features = extractFeatures(candidates);

        writeFeatures(features);

    }

    public List<Pair<TreeMap<Integer, Float>, Integer>> getASuspCandidzFeatures(
            Set<Integer> candidates, Integer suspIndxId) throws IOException {
        Document susp = suspDocuments.get(suspIndxId);

        Map<Integer, Integer> rankedCandidzByJM = getCandidatesRanksForGivenSusp(
                suspIndxId, candidates, new LMJelinekMercerSimilarity(0.1F));
        Map<Integer, Integer> rankedCandidzByBM25 = getCandidatesRanksForGivenSusp(
                suspIndxId, candidates, new BM25Similarity());
        Map<Integer, Integer> rankedCandidzByDirichlet = getCandidatesRanksForGivenSusp(
                suspIndxId, candidates, new LMDirichletSimilarity());
        Map<Integer, Integer> rankedCandidzByTFIDF = getCandidatesRanksForGivenSusp(
                suspIndxId, candidates, new DefaultSimilarity());
        List<Pair<TreeMap<Integer, Float>, Integer>> features = new ArrayList<Pair<TreeMap<Integer, Float>, Integer>>();
        for (Integer srcIndxId : candidates) {
            Document src = srcDocuments.get(srcIndxId);
            TreeMap<Integer, Float> pairFeatureVecotor = extractFeatures(susp, src);
          
        
            /* pairFeatureVecotor.put(PAIRFEATUREID_JMRANK,
                    rankedCandidzByJM.get(srcIndxId).floatValue());
            pairFeatureVecotor.put(PAIRFEATUREID_BM25,
                    rankedCandidzByBM25.get(srcIndxId).floatValue());
            pairFeatureVecotor.put(PAIRFEATUREID_DIRICHLET,
                    rankedCandidzByDirichlet.get(srcIndxId).floatValue());
            pairFeatureVecotor.put(PAIRFEATUREID_TFIDF, rankedCandidzByTFIDF
                    .get(srcIndxId).floatValue());
            pairFeatureVecotor.put(PAIRFEATUREID_NAMEDENTITYRATIO,
                    ratioOfCommonNamedEntities(suspIndxId, srcIndxId));*/

            features.add(new Pair<TreeMap<Integer, Float>, Integer>(
                    pairFeatureVecotor, srcIndxId));
        }

        return features;
    }

    public Map<Integer, Integer> getCandidatesRanksForGivenSusp(Integer suspId,
            Set<Integer> candidates, Similarity simFunction) throws IOException {
        QueryParser qParser;
        Map<Integer, Integer> candidatesRank = new HashMap<Integer, Integer>();

        for (Integer srcIndx : candidates) {
            candidatesRank.put(srcIndx, 0);
        }
        org.apache.lucene.document.Document document = suspIreader
                .document(suspId);
        String query = document.get("TEXT");
        BooleanQuery.setMaxClauseCount(query.split("\\s+").length);
        if (getLanguage().equals("EN")) {
            qParser = new QueryParser(Version.LUCENE_47, "TEXT",
                    Engine.MyEnglishAnalyzer(IFSTEM, REMOVE_STOPWORDS));
        } else if (getLanguage().equals("FA")) {
            qParser = new QueryParser(Version.LUCENE_47, "TEXT",
                    Engine.MyPersianAnalyzer(IFSTEM, REMOVE_STOPWORDS));
        } else {
            qParser = new QueryParser(Version.LUCENE_47, "TEXT",
                    Engine.MyEnglishAnalyzer(IFSTEM, REMOVE_STOPWORDS));
        }
        Query q = null;
        try {
            q = qParser.parse(QueryParser.escape(query));
        } catch (ParseException e) {
            e.printStackTrace();
        }

        IndexSearcher isearcher = new IndexSearcher(srcIreader);
        isearcher.setSimilarity(simFunction);
        TopFieldCollector tfc = TopFieldCollector.create(Sort.RELEVANCE, TOPK,
                true, true, true, false);
        isearcher.search(q, tfc);
        TopDocs results = tfc.topDocs();
        ScoreDoc[] hits = results.scoreDocs;

        for (int i = 0; i < hits.length; i++) {
            Double Score = (double) hits[i].score;
            org.apache.lucene.document.Document hitDoc = srcIreader
                    .document(hits[i].doc);
            Integer srcIndexedId = hits[i].doc;
            candidatesRank.put(srcIndexedId, hits.length - i);
        }

        return candidatesRank;

    }

    public void makeFeaturesReady(Map<Integer, Set<Integer>> candidates) throws IOException, ParseException{
    
    	 
    	TreeMap<Integer, List<Pair<TreeMap<Integer, Float>, Integer>>> features =	extractLanguageModelBasedFeatures(candidates);
        //writeFeatures(features);
    	Analyzer.makeFeaturesReady();
    }

    public List<String> rerankResults(List<String> candidSrces,
            String suspFileName) throws IOException {
        List<String> rerankedResults = new ArrayList<String>();

        Set<Integer> candidates = new HashSet<Integer>();

        Map<String, Integer> suspDocMap = null;
        Map<String, Integer> srcDocMap = null;
        suspDocMap = Util.loadDocMap(getSuspMapPath());
        srcDocMap = Util.loadDocMap(getSrcMapPath());

        Integer suspIndxId = suspDocMap.get(suspFileName);

        for (String srcId : candidSrces) {
            candidates.add(srcDocMap.get(srcId));
        }

        List<Pair<TreeMap<Integer, Float>, Integer>> features = getASuspCandidzFeatures(
                candidates, suspIndxId);

        return rerankedResults;
    }

    public void setopDocumentFeatures(Document document,IndexReader ireader) throws IOException
    {
            Map<String, Float> contentWordsDist = Collector.sortedNGram(document, 1);
            Map<String, Float> bigramsDist = Collector.sortedNGram(document, 2);
            Map<String, Float> trigramsDist = Collector.sortedNGram(document, 3);
            Map<String, Float> termDist = Collector.getAllTermsDist(document);

            Map<String, Float> synsetDist = Collector.getSynsetsDist(contentWordsDist);

            List<String> orderedPOS = Collector.getOrderedPOS(document,ireader);
            int k = 3;
            Map<String, Float> posTagDist = Collector.getPOSkGram(
                    orderedPOS, 1);
            Map<String, Float> posTagKGramDist = Collector.getPOSkGram(
                    orderedPOS, k);

            Integer m = 3;
            Map<String, Float> stopwordMGramsDist = Collector.getSortedStopWordsmGram(
                    document, m);
            Map<String, Float> mostFrequentWordsDist = Collector
                    .getRelativeFrequencyOfMostFrequentWords(document, indexInfo.getTopTerms_DF("TEXT", 100));
            Map<String, Float> lessFrequentWordsDist = Collector
                    .getRelativeFrequencyOfMostFrequentWords(document, indexInfo.getDownTerms_DF("TEXT", 100));
 
            Map<String, Float> namedEntitiesDist = Collector.getNamedEntitiesDist(document,ireader);
            //Map<String, Float> entitiesDist = Collector.getEntitiesDist(document,ireader);

            HashMap<String, Float> sentenceLengthDist = Collector
                    .numOfWordsInSentence(document);
          
            Map<String, Float> punctuationsDist = Collector
                    .getPunctuationDist(document,ireader);
            document.addFeature(DOCFEATURE_CONTENTWORDS,contentWordsDist);
            document.addFeature(DOCFEATURE_SORTEDBIGRAMS, bigramsDist);
            document.addFeature(DOCFEATURE_SORTEDTRIGRAMS, trigramsDist);
            document.addFeature(DOCFEATURE_TERMS, termDist);
            document.addFeature(DOCFEATURE_SYNSETS, synsetDist);
            document.addFeature(DOCFEATURE_POSTAGS, posTagDist);
            document.addFeature(DOCFEATURE_POSTAGKGRAMS, posTagKGramDist);
            document.addFeature(DOCFEATURE_STOPWORDSMGRAM, stopwordMGramsDist);
            document.addFeature(DOCFEATURE_MOSTFREQUENTWORDS, mostFrequentWordsDist);
            document.addFeature(DOCFEATURE_LESSFREQUENTWORDS, lessFrequentWordsDist);
            document.addFeature(DOCFEATURE_NAMEDENTITIES, namedEntitiesDist);
           // document.addFeature(DOCFEATURE_ENTITIES, entitiesDist);
            document.addFeature(DOCFEATURE_SENTENCESLENGTH, sentenceLengthDist);
            document.addFeature(DOCFEATURE_PUNCTUATIONS, punctuationsDist);
        
    }

    public void setupSrcDocs() throws IOException {
        for (Document srcDoc : srcDocuments.values()) {
            setopDocumentFeatures(srcDoc,indexInfo.getIndexReader());
        }
    }
    
    public List<Pair<Integer, Float>> svm_classify(
            List<Pair<Map<Integer, Float>, Integer>> features, Integer suspId) {
        List<Pair<Integer, Float>> rankedList = new ArrayList<Pair<Integer, Float>>();
        try {
            String tmpFileName = "tmpFeatures";
            write_features(features, tmpFileName, suspId);

            Runtime rt = Runtime.getRuntime();
            String[] params = {"/bin/sh", "-c",
                "cd " + Config.getSVMRankFolder()};
            Process proc1 = rt.exec(params);// > HunTagger_Output.txt");
            int exitVal = proc1.waitFor();
            // System.out.println("Exited with error code "+exitVal);
            String[] paramsArray = {
                "/bin/sh",
                "-c",
                "./svm_rank_classify  " + tmpFileName + " "
                + "trainModel.txt" + " " + "predz.txt"};
            Process proc = rt.exec(paramsArray);// > HunTagger_Output.txt");
            // BufferedReader input = new BufferedReader(new
            // InputStreamReader(proc.getInputStream()));
            BufferedReader input = new BufferedReader(new InputStreamReader(
                    proc.getErrorStream()));

            String lline = null;

            while ((lline = input.readLine()) != null) {
                System.out.println(lline);
            }

            exitVal = proc.waitFor();
            // System.out.println("Exited with error code "+exitVal);
            BufferedReader br = new BufferedReader(new InputStreamReader(
                    new FileInputStream("predz.txt")));

            String scoreLine = null;

            for (int i = 0; i < features.size(); i++) {
                scoreLine = br.readLine();
                rankedList.add(new Pair<Integer, Float>(features.get(i)
                        .getSecond(), Float.parseFloat(scoreLine)));
            }
            br.close();
            File file = new File("tmpFeatures");
            if (file.exists()) {
                file.delete();
            }
            file = new File("predz.txt");
            if (file.exists()) {
                file.delete();
            }

            Collections.sort(rankedList,
                    new Comparator<Pair<Integer, Float>>() {
                        public int compare(Pair<Integer, Float> o1,
                                Pair<Integer, Float> o2) {
                            return o1.getSecond().compareTo(o2.getSecond());
                        }
                    });

            return rankedList;

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        return null;
    }

    public void trainSourceCandidRanker_FinalInFiles() {

    }

 

    private void write_features(
            List<Pair<Map<Integer, Float>, Integer>> features,
            String tmpFileName, Integer suspId) throws IOException {
        String featureStr = new String();
        for (Pair<Map<Integer, Float>, Integer> pair : features) {
            String featureline = "qid:" + suspId + " ";
            for (Integer fkey : pair.getFirst().keySet()) {
                featureline += fkey + ":" + pair.getFirst().get(fkey) + " ";
            }
            featureline += "# " + pair.getSecond();
            featureStr += featureline.trim() + "\n";

        }

        featureStr.trim();
        BufferedWriter bwriter = new BufferedWriter(new FileWriter(tmpFileName));

        bwriter.write(featureStr);
        bwriter.close();

    }

    private void writeFeatures(
            TreeMap<Integer, List<Pair<TreeMap<Integer, Float>, Integer>>> features)
            throws IOException {
        String featureStr = new String();
        for (Integer suspId : features.keySet()) {
            for (Pair<TreeMap<Integer, Float>, Integer> pair : features
                    .get(suspId)) {
                String featureline = "qid:" + suspId + " ";
                for (Integer fkey : pair.getFirst().keySet()) {
                    featureline += fkey + ":" + pair.getFirst().get(fkey) + " ";
                }
                featureline += "# " + pair.getSecond();
                featureStr += featureline.trim() + "\n";
            }
        }

        featureStr.trim();
        BufferedWriter bwriter = new BufferedWriter(new FileWriter(
                Config.getFeaturesPath()));

        bwriter.write(featureStr);
        bwriter.close();
    }

    private List<Pair<TreeMap<Integer, Float>, Integer>> getSuspLanguageModelBasedFeatures(Integer suspDocId, Map<Integer, Set<Integer>> candidates) 
            throws IOException, ParseException {
            List<Pair<TreeMap<Integer, Float>, Integer>> suspfeatures = new ArrayList<Pair<TreeMap<Integer, Float>, Integer>>();

            Map<Integer,Map<Integer,Float>> documentScores = new HashMap<Integer, Map<Integer,Float>>();
            for(Integer srcDocId: candidates.get(suspDocId))
            {
                documentScores.put(srcDocId,new HashMap<Integer,Float>());
            }
            retrieveScoresPerField(suspDocId,candidates,documentScores,IndexedDocument.FIELD_ALLTERMS,CLASSIFIER_FEATURES_PAIRED_ALLTERMS);
            retrieveScoresPerField(suspDocId,candidates,documentScores,IndexedDocument.FIELD_CONTENTWORDS,CLASSIFIER_FEATURES_PAIRED_CONTENTWORDS);
            retrieveScoresPerField(suspDocId,candidates,documentScores,IndexedDocument.FIELD_SORTED_BIGRAMS, CLASSIFIER_FEATURES_PAIRED_BIGRAMS);
            retrieveScoresPerField(suspDocId,candidates,documentScores,IndexedDocument.FIELD_SORTED_TRIGRAMS,CLASSIFIER_FEATURES_PAIRED_TRIGRAMS);
            retrieveScoresPerField(suspDocId,candidates,documentScores,IndexedDocument.FIELD_LESS_FREQUENT_WORDS,CLASSIFIER_FEATURES_PAIRED_LESSFREQUENTWORDS);
            retrieveScoresPerField(suspDocId,candidates,documentScores,IndexedDocument.FIELD_MOST_FREQUENT_WORDS,CLASSIFIER_FEATURES_PAIRED_MOSTFREQUENTWORDS);
            retrieveScoresPerField(suspDocId,candidates,documentScores,IndexedDocument.FIELD_NAMED_ENTITIES,CLASSIFIER_FEATURES_PAIRED_NAMEDENTITIES);
            retrieveScoresPerField(suspDocId,candidates,documentScores,IndexedDocument.FIELD_POS3GRAM,CLASSIFIER_FEATURES_PAIRED_POSTAGS);
            retrieveScoresPerField(suspDocId,candidates,documentScores,IndexedDocument.FIELD_POS3GRAM,CLASSIFIER_FEATURES_PAIRED_POSTAGKRAMS);
            retrieveScoresPerField(suspDocId,candidates,documentScores,IndexedDocument.FIELD_PUNCTUATIONS,CLASSIFIER_FEATURES_PAIRED_PUNCTUATIONS);
            retrieveScoresPerField(suspDocId,candidates,documentScores,IndexedDocument.FIELD_STOPWORDS, CLASSIFIER_FEATURES_PAIRED_STOPWORDS);
            retrieveScoresPerField(suspDocId,candidates,documentScores,IndexedDocument.FIELD_STOPWORDS3Gram,CLASSIFIER_FEATURES_PAIRED_STOPWORDSKGRAM);
            retrieveScoresPerField(suspDocId,candidates,documentScores,IndexedDocument.FIELD_SENTENCES_LENGTH,CLASSIFIER_FEATURES_PAIRED_SENTENCESLENGTH);
            retrieveScoresPerField(suspDocId,candidates,documentScores,IndexedDocument.FIELD_UNIQUE_WORDS,CLASSIFIER_FEATURES_PAIRED_UNIQUETERMS);
            retrieveScoresPerField(suspDocId,candidates,documentScores,IndexedDocument.FIELD_SYSNSETS,CLASSIFIER_FEATURES_PAIRED_SYNSETS);
                    
            for(Integer srcDocId: candidates.get(suspDocId))
            {
             TreeMap<Integer, Float> featureVector = fillFeatureVectorForaPair(srcDocId,documentScores);
             suspfeatures.add(new Pair<TreeMap<Integer, Float>, Integer>(featureVector, srcDocId));
            }
            
            return suspfeatures;
    }

}
