/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ir.ac.ut.common;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.iis.plagiarismdetector.core.lucene.MyAnalyzer;

import cue.lang.WordIterator;

/**
 *
 * @author Hamed Zamani (http://hamedz.ir/ | hamedzamani at acm.org)
 */
public class Sentence {
	List<String> words;

	public enum Type {
		Declarative, Exclamatory, Interrogative
	};

	Type type;
	String sentence;
	// static Tokenizer tokenizerEnglish;
	String lang = "";

	// InputStream is = new FileInputStream("en-token.bin");
	// TokenizerModel model = new TokenizerModel(is);
	// Tokenizer tokenizer = new TokenizerME(model);
	// String tokens[] = tokenizer.tokenize("Hi. How are you? This is Mike.");
	// for (String a : tokens)
	// System.out.println(a);
	// is.close();

	static {
		try {
			// InputStream is = new FileInputStream(
			// "src/main/resources/en-token.bin");
			// TokenizerModel model = new TokenizerModel(is);
			// tokenizerEnglish = new TokenizerME(model);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	public Sentence(String sentence, String lang) throws FileNotFoundException,
			IOException, Exception {
		this.sentence = sentence;
		this.lang = lang;
		this.words = new ArrayList<String>();
		if (lang.equals("EN") || lang.equals("EE"))
			words.addAll(luceneTokenizer(sentence, false, false));
		else if (lang.equals("GR") || lang.equals("SP")) {
			for (String word : new WordIterator(sentence)) {
				words.add(word);
			}
		} else
			throw new Exception("WTF LANG!");
	}

	public List<String> getWords() {
		return words;
	}

	public Type getType() {
		return type;
	}

	public String getSentence() {
		return sentence;
	}

	public void setWords(List<String> words) {
		this.words = words;
	}

	public void setType(Type type) {
		this.type = type;
	}

	public static List<String> luceneTokenizer(String text, Boolean ifstem,
			Boolean ifremovestopwords) throws IOException {
		List<String> tokensList = new ArrayList<String>();

		Analyzer analyzer = ifremovestopwords ? (new MyAnalyzer(ifstem))
				.getAnalyzer("EN") : (new MyAnalyzer(ifstem)).getAnalyzer("EN");
		try {
			TokenStream stream = analyzer.tokenStream("TEXT", new StringReader(
					text));
			stream.reset();
			while (stream.incrementToken()) {
				/*
				 * System.out.println(stream.getAttribute(CharTermAttribute.class
				 * ) .toString());
				 * System.out.println(stream.getAttribute(OffsetAttribute.class)
				 * .startOffset() + " " +
				 * stream.getAttribute(OffsetAttribute.class) .endOffset());
				 */
				tokensList.add(stream.getAttribute(CharTermAttribute.class)
						.toString());

			}
		} catch (IOException e) {
			// not thrown b/c we're using a string reader...
			throw new RuntimeException(e);
		}

		return tokensList;
	}
}
