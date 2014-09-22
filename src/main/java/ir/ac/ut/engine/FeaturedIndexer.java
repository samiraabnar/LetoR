package ir.ac.ut.engine;

import ir.ac.ut.config.Config;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import javax.xml.parsers.ParserConfigurationException;

import l2r.sam.IndexedDocument;

import org.apache.commons.io.FileUtils;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.core.SimpleAnalyzer;
import org.apache.lucene.analysis.miscellaneous.PerFieldAnalyzerWrapper;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.SimpleFSDirectory;
import org.apache.lucene.util.Version;
import org.iis.plagiarismdetector.core.MyDictionary;
import org.iis.plagiarismdetector.core.lucene.MyAnalyzer;
import org.xml.sax.SAXException;

/**
 *
 * @author Mostafa Dehghani
 */
public class FeaturedIndexer {

	static final org.apache.log4j.Logger log = org.apache.log4j.Logger
			.getLogger(FeaturedIndexer.class.getName());
	private IndexWriter writer;
	private Map<String, Analyzer> analyzerMap = new HashMap<String, Analyzer>();

	public FeaturedIndexer() throws Exception {

		log.info("-----------------------INDEXING--------------------------");

		preIndexerCleaning();
		analyzerMap.put(IndexedDocument.FIELD_REAL_ID, new SimpleAnalyzer(
				Version.LUCENE_CURRENT));
		analyzerMap.put(IndexedDocument.FIELD_NAMED_ENTITIES, (new MyAnalyzer(
				false)).MyNamedEntityAnalyzer());
		analyzerMap.put(IndexedDocument.FIELD_SORTED_BIGRAMS, (new MyAnalyzer(
				false)).MyNgramEntityAnalyzer());
		analyzerMap.put(IndexedDocument.FIELD_SORTED_TRIGRAMS, (new MyAnalyzer(
				false)).MyNgramEntityAnalyzer());
		analyzerMap.put(IndexedDocument.FIELD_STOPWORDS3Gram, (new MyAnalyzer(
				false)).MyNgramEntityAnalyzer());
		analyzerMap.put(IndexedDocument.FIELD_POS3GRAM,
				(new MyAnalyzer(false)).MyNgramEntityAnalyzer());

		Analyzer analyzer = ( new MyAnalyzer(false)).MyDefaultAnalyzer();
		PerFieldAnalyzerWrapper prfWrapper = new PerFieldAnalyzerWrapper(
				analyzer, analyzerMap);
		IndexWriterConfig irc = new IndexWriterConfig(Version.LUCENE_CURRENT,
				prfWrapper);
		this.writer = new IndexWriter(new SimpleFSDirectory(new File(
				Config.getFeaturedIndexPath())), irc);
		IndexedDocument.loadPuncMap();
		IndexedDocument.fetchFeaturedDocuments(this);
		// fileReader(new File(configFile.getProperty("CORPUS_CON_PATH")));
		IndexedDocument.savePuncMap();

		this.writer.commit();
		this.writer.close();
		analyzer.close();
		prfWrapper.close();
		log.info("-------------------------------------------------");
		log.info("Index is created successfully...");
		log.info("-------------------------------------------------");
	}

	public void indexDocument(IndexedDocument indexedDocument) {
		Document doc = new Document();
		doc.add(new Field(IndexedDocument.FIELD_REAL_ID, indexedDocument
				.get(IndexedDocument.FIELD_REAL_ID), Field.Store.YES,
				Field.Index.NO));
		doc.add(new Field(IndexedDocument.FIELD_ALLTERMS, indexedDocument
				.get(IndexedDocument.FIELD_ALLTERMS), Field.Store.YES,
				Field.Index.ANALYZED, Field.TermVector.WITH_POSITIONS));
		doc.add(new Field(IndexedDocument.FIELD_CONTENTWORDS, indexedDocument
				.get(IndexedDocument.FIELD_CONTENTWORDS), Field.Store.YES,
				Field.Index.ANALYZED, Field.TermVector.WITH_POSITIONS));
		doc.add(new Field(IndexedDocument.FIELD_SORTED_BIGRAMS, indexedDocument
				.get(IndexedDocument.FIELD_SORTED_BIGRAMS), Field.Store.YES,
				Field.Index.ANALYZED, Field.TermVector.WITH_POSITIONS));
		doc.add(new Field(IndexedDocument.FIELD_SORTED_TRIGRAMS,
				indexedDocument.get(IndexedDocument.FIELD_SORTED_TRIGRAMS),
				Field.Store.YES, Field.Index.ANALYZED,
				Field.TermVector.WITH_POSITIONS));
		doc.add(new Field(IndexedDocument.FIELD_STOPWORDS3Gram, indexedDocument
				.get(IndexedDocument.FIELD_STOPWORDS3Gram), Field.Store.YES,
				Field.Index.ANALYZED, Field.TermVector.WITH_POSITIONS));
		doc.add(new Field(IndexedDocument.FIELD_POS3GRAM, indexedDocument
				.get(IndexedDocument.FIELD_POS3GRAM), Field.Store.YES,
				Field.Index.ANALYZED, Field.TermVector.WITH_POSITIONS));
		doc.add(new Field(IndexedDocument.FIELD_POSTAGS, indexedDocument
				.get(IndexedDocument.FIELD_POSTAGS), Field.Store.YES,
				Field.Index.ANALYZED, Field.TermVector.WITH_POSITIONS));
		doc.add(new Field(IndexedDocument.FIELD_PUNCTUATIONS, indexedDocument
				.get(IndexedDocument.FIELD_PUNCTUATIONS), Field.Store.YES,
				Field.Index.ANALYZED, Field.TermVector.WITH_POSITIONS));
		doc.add(new Field(IndexedDocument.FIELD_SYSNSETS, indexedDocument
				.get(IndexedDocument.FIELD_SYSNSETS), Field.Store.YES,
				Field.Index.ANALYZED, Field.TermVector.WITH_POSITIONS));
		doc.add(new Field(IndexedDocument.FIELD_MOST_FREQUENT_WORDS,
				indexedDocument.get(IndexedDocument.FIELD_MOST_FREQUENT_WORDS),
				Field.Store.YES, Field.Index.ANALYZED,
				Field.TermVector.WITH_POSITIONS));
		doc.add(new Field(IndexedDocument.FIELD_LESS_FREQUENT_WORDS,
				indexedDocument.get(IndexedDocument.FIELD_LESS_FREQUENT_WORDS),
				Field.Store.YES, Field.Index.ANALYZED,
				Field.TermVector.WITH_POSITIONS));
		doc.add(new Field(IndexedDocument.FIELD_NAMED_ENTITIES, indexedDocument
				.get(IndexedDocument.FIELD_NAMED_ENTITIES), Field.Store.YES,
				Field.Index.ANALYZED, Field.TermVector.WITH_POSITIONS));
		doc.add(new Field(IndexedDocument.FIELD_STOPWORDS, indexedDocument
				.get(IndexedDocument.FIELD_STOPWORDS), Field.Store.YES,
				Field.Index.ANALYZED, Field.TermVector.WITH_POSITIONS));
		doc.add(new Field(IndexedDocument.FIELD_UNIQUE_WORDS, indexedDocument
				.get(IndexedDocument.FIELD_UNIQUE_WORDS), Field.Store.YES,
				Field.Index.ANALYZED, Field.TermVector.WITH_POSITIONS));
		doc.add(new Field(IndexedDocument.FIELD_SENTENCES_LENGTH,
				indexedDocument.get(IndexedDocument.FIELD_SENTENCES_LENGTH),
				Field.Store.YES, Field.Index.ANALYZED,
				Field.TermVector.WITH_POSITIONS));
		doc.add(new Field(IndexedDocument.FIELD_DOCUMENT_LENGTH,
				indexedDocument.get(IndexedDocument.FIELD_DOCUMENT_LENGTH),
				Field.Store.YES, Field.Index.NO));
		doc.add(new Field(IndexedDocument.FIELD_DOCUMENT_LENGTH_UNIQUE,
				indexedDocument
						.get(IndexedDocument.FIELD_DOCUMENT_LENGTH_UNIQUE),
				Field.Store.YES, Field.Index.NO));

		try {
			this.writer.addDocument(doc);
		} catch (IOException ex) {
			log.error(ex);
		}
		log.info("Document "
				+ indexedDocument.get(IndexedDocument.FIELD_REAL_ID)
				+ " has been indexed successfully...");
	}

	private void preIndexerCleaning() {
		try {
			File tmpIndex = new File(Config.getFeaturedIndexPath());
			if (tmpIndex.exists()) {
				FileUtils.deleteDirectory(tmpIndex);
				log.info("Deletting the existing index directory on: "
						+ Config.getFeaturedIndexPath());
				FileUtils.forceMkdir(new File(Config.getFeaturedIndexPath()));
				log.info("Making index directory on: "
						+ Config.getFeaturedIndexPath());
			}
		} catch (IOException ex) {
			log.error(ex);
		}
		log.info("\n\n -----------------------CLeaning Finished--------------------------\n");
	}

	public static void main(String[] args) throws ParserConfigurationException,
			SAXException, SQLException, IOException {
		try {
			new FeaturedIndexer();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
