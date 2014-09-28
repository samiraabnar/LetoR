///*
// * To change this license header, choose License Headers in Project Properties.
// * To change this template file, choose Tools | Templates
// * and open the template in the editor.
// */
package ir.ac.ut.engine;

import ir.ac.ut.config.Config;
import static ir.ac.ut.engine.Retrieval.reportInTREC;
import java.io.File;
import java.io.IOException;
import l2r.sam.IndexedDocument;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.core.SimpleAnalyzer;
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
import org.iis.plagiarismdetector.core.lucene.MyAnalyzer;

/**
 *
 * @author Sam
 */
public class FeaturedRetriever {

    public static IndexReader ireader = null;

    static {
        try {
            ireader = IndexReader.open(new SimpleFSDirectory(new File(
                    Config.getSrcFeaturedIndexPath())));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static ScoreDoc[] search(String query, String qId, String field)
            throws IOException {
        float mu = (float) 1000;
        query = query.toLowerCase();
        BooleanQuery.setMaxClauseCount(query.length());

        Analyzer analyzer;
        if (field.equals(IndexedDocument.FIELD_REAL_ID)) {
            analyzer = new SimpleAnalyzer(
                    Version.LUCENE_CURRENT);
        } else if (field.equals(IndexedDocument.FIELD_NAMED_ENTITIES)) {
            analyzer = (new MyAnalyzer(
                    false)).MyNgramAnalyzer();
        } else if (field.equals(IndexedDocument.FIELD_SORTED_BIGRAMS)) {
            analyzer = (new MyAnalyzer(
                    false)).MyNgramAnalyzer();
        } else if (field.equals(IndexedDocument.FIELD_SORTED_TRIGRAMS)) {
            analyzer = (new MyAnalyzer(
                    false)).MyNgramAnalyzer();
        } else if (field.equals(IndexedDocument.FIELD_STOPWORDS3Gram)) {
            analyzer = (new MyAnalyzer(
                    false)).MyNgramAnalyzer();
        }
        else if (field.equals(IndexedDocument.FIELD_POS3GRAM)) {
            analyzer = (new MyAnalyzer(false)).MyNgramAnalyzer();
        } else {
            analyzer = (new MyAnalyzer(false)).MyDefaultAnalyzer();
        }

        QueryParser qParser = new QueryParser(Version.LUCENE_47, field,
                analyzer);
        Query q = null;
        try {
            q = qParser.parse(QueryParser.escape(query));
        } catch (org.apache.lucene.queryparser.classic.ParseException e) {
            e.printStackTrace();
            System.out.println("Exceptional Query:" + qId);
            return new ScoreDoc[0];
        }

        Similarity simFunction = new LMDirichletSimilarity(mu);
        // Similarity simFunction = new BM25Similarity();
        IndexSearcher isearcher = new IndexSearcher(ireader);
        isearcher.setSimilarity(simFunction);
        TopFieldCollector tfc = TopFieldCollector.create(Sort.RELEVANCE,
                ireader.numDocs(), true, true,
                true, false);
        isearcher.search(q, tfc);

        TopDocs results = tfc.topDocs();
        ScoreDoc[] hits = results.scoreDocs;
        reportInTREC(hits, qId);
        return hits;
    }
}
