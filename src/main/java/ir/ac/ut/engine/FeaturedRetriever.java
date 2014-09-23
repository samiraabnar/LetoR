///*
// * To change this license header, choose License Headers in Project Properties.
// * To change this template file, choose Tools | Templates
// * and open the template in the editor.
// */
//package ir.ac.ut.engine;
//
//import com.sun.tools.example.debug.expr.ParseException;
//import static ir.ac.ut.config.Config.configFile;
//import static ir.ac.ut.config.Config.getLanguage;
//import static ir.ac.ut.config.Config.getSrcIndexPath;
//import static ir.ac.ut.engine.Engine.MyEnglishAnalyzer;
//import static ir.ac.ut.engine.Engine.MyPersianAnalyzer;
//import static ir.ac.ut.engine.Retrieval.ireader;
//import static ir.ac.ut.engine.Retrieval.reportInTREC;
//import java.io.File;
//import java.io.IOException;
//import l2r.sam.IndexedDocument;
//import org.apache.lucene.analysis.core.SimpleAnalyzer;
//import org.apache.lucene.index.IndexReader;
//import org.apache.lucene.queryparser.classic.QueryParser;
//import org.apache.lucene.search.BooleanQuery;
//import org.apache.lucene.search.IndexSearcher;
//import org.apache.lucene.search.Query;
//import org.apache.lucene.search.ScoreDoc;
//import org.apache.lucene.search.Sort;
//import org.apache.lucene.search.TopDocs;
//import org.apache.lucene.search.TopFieldCollector;
//import org.apache.lucene.search.similarities.LMDirichletSimilarity;
//import org.apache.lucene.search.similarities.Similarity;
//import org.apache.lucene.store.SimpleFSDirectory;
//import org.apache.lucene.util.Version;
//import org.iis.plagiarismdetector.core.lucene.MyAnalyzer;
//
///**
// *
// * @author Sam
// */
//public class FeaturedRetriever {
//    
//    public static IndexReader ireader = null;
//	static {
//		try {
//			ireader = IndexReader.open(new SimpleFSDirectory(new File(
//					getSrcFeaturedIndexPath())));
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
//	}
//        
//        
//        
//        public static ScoreDoc[] search(String query, String qId,String field)
//			throws IOException, ParseException,
//			org.apache.lucene.queryparser.classic.ParseException {
//		float mu = (float) 1000;
//                BooleanQuery.setMaxClauseCount(query.split("\\s+").length);
//
//		QueryParser qParser;
//		Analyzer analyzer;
//                if((IndexedDocument.FIELD_REAL_ID, new SimpleAnalyzer(
//				Version.LUCENE_CURRENT));
//		analyzerMap.put(IndexedDocument.FIELD_NAMED_ENTITIES, (new MyAnalyzer(
//				false)).MyNgramAnalyzer());
//		analyzerMap.put(IndexedDocument.FIELD_SORTED_BIGRAMS, (new MyAnalyzer(
//				false)).MyNgramAnalyzer());
//		analyzerMap.put(IndexedDocument.FIELD_SORTED_TRIGRAMS, (new MyAnalyzer(
//				false)).MyNgramAnalyzer());
//		analyzerMap.put(IndexedDocument.FIELD_STOPWORDS3Gram, (new MyAnalyzer(
//				false)).MyNgramAnalyzer());
//		analyzerMap.put(IndexedDocument.FIELD_POS3GRAM,
//				(new MyAnalyzer(false)).MyNgramAnalyzer());
//                
//		Query q = qParser.parse(QueryParser.escape(query));
//		Similarity simFunction = new LMDirichletSimilarity(mu);
//		// Similarity simFunction = new BM25Similarity();
//		IndexSearcher isearcher = new IndexSearcher(ireader);
//		isearcher.setSimilarity(simFunction);
//		TopFieldCollector tfc = TopFieldCollector.create(Sort.RELEVANCE,
//				Integer.parseInt(configFile.getProperty("topK")), true, true,
//				true, false);
//		isearcher.search(q, tfc);
//		TopDocs results = tfc.topDocs();
//		ScoreDoc[] hits = results.scoreDocs;
//		reportInTREC(hits, qId);
//		return hits;
//	}
//}
