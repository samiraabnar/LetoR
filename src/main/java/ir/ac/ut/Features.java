package ir.ac.ut;

import static ir.ac.ut.config.Config.configFile;
import static ir.ac.ut.engine.Retrieval.ireader;
import ir.ac.ut.engine.Retrieval;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.store.SimpleFSDirectory;

public class Features {
	/*
	 * a = numOfWords b = numOfCommonWordsSusp; c = numOfCommonWordsSrc d =
	 * numOfSentence e = numOfThe f = numOfBigramsSusp g = numOfBigramsSrc h =
	 * numOfLines k = numOfChars l = numOfKeyWordSusp m = numOfKeyWordSusp1 n =
	 * numOfKeyWordSrc o = numOfKeyWordSrc1 p = mutualKeyWord q =
	 * numOfSpecialWordSusp r = numOfSpecialWordSrc s = score
	 */

	public static IndexReader suspIreader = null;
	static {
		try {
			suspIreader = IndexReader.open(new SimpleFSDirectory(new File(
					configFile.getProperty("suspIndexPath"))));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	public static double a, b, c, d, e, f, g, h, k, l, m, n, o, p, q, r, s;
	public static int numLines, srcWords, suspWords;
	public static String keyWordSusp, keyWordSrc;

	public static void main(String[] args) throws IOException {
		Map<String, Integer> docIds = new HashMap<String, Integer>();
		DecimalFormat df = new DecimalFormat("#.##");
		File pairsDir = new File(configFile.getProperty("pairsPath"));
		String pairs = readFromFile(pairsDir);

		Retrieval retrieval = new Retrieval();
		for (int i = 0; i < suspIreader.numDocs(); i++) {
			Document susp = suspIreader.document(i);
			try {
				ScoreDoc[] hits = Retrieval.search(susp.get("TEXT"),
						susp.get("DOCID"));
				for (int j = 0; j < hits.length; j++) {
					Document src = ireader.document(hits[j].doc);
					String srcText = (src.get("TEXT"));
					/*
					 * numOfCommonWords(susp, srcText); d = numOfSentence(susp,
					 * srcText); e = numOfThe(susp, srcText); h =
					 * numOfLines(susp, srcText); k = numOfChars(susp, srcText);
					 * numOfKeyWordSusp(susp, srcText); numOfKeyWordSrc(susp,
					 * srcText); p = mutualKeyWord(susp, srcText); q =
					 * numOfSpecialWordSusp(susp, srcText); r =
					 * numOfSpecialWordSrc(susp, srcText);
					 */
					// s = hits[j].score;
					// r = numOfSpecialWordSrc(susp, srcText);
					// out.write(/*findPairs(pairs, filesSusp[i].getName(),
					// hitDoc.get("DOCID"))+*/"qid:"+(filesSusp[i].getName().substring(19,24))+" 1:"+a+" 2:"+b+" 3:"+c+" 4:"+d+" 5:"+e+" 6:"+f+" 7:"+g+" 8:"+h+" 9:"+k+" 10:"+l+" 11:"+m+" 12:"+n+" 13:"+o+" 14:"+p+" 15:"+q+" 16:"+r+" # "+(hitDoc.get("DOCID"))+"\n");
					// out.write(r+"\n");
					// String line = in.readLine();
					// String newLine = line.substring(0, line.indexOf("#")) +
					// "17:" + s + " # " + line.substring(line.indexOf("#")+1)
					// +"\n";
					// out.write(newLine);
					// System.out.println(findPairs(pairs,
					// filesSusp[i].getName(),
					// hitDoc.get("DOCID"))+" qid:"+(filesSusp[i].getName().substring(19,24))+" 1:"+df.format(a)+" 2:"+df.format(b)+" 3:"+df.format(c)+" 4:"+df.format(d)+" 5:"+df.format(e)+" 6:"+df.format(f)+" 7:"+df.format(g)+" 8:"+df.format(h)+" 9:"+df.format(k)+" 10:"+df.format(l)+" 11:"+df.format(m)+" 12:"+df.format(n)+" 13:"+df.format(o)+" 14:"+df.format(p)+" 15:"+df.format(q)+" 16:"+df.format(r)+" # "+(hitDoc.get("DOCID"))+"\n");
				}
			} catch (Exception e) {
				e.printStackTrace();
			}

		}
	}

	public static String readFromFile(File suspfile) throws IOException {
		BufferedReader suspf = new BufferedReader(new InputStreamReader(
				new FileInputStream(suspfile)));

		String susp = "";
		String temp = "";
		susp = suspf.readLine();
		numLines = 0;
		while ((temp = suspf.readLine()) != null) {
			susp = susp + "\n" + temp;
			numLines++;
		}
		suspf.close();
		return susp;
	}

	public static void numOfCommonWords(String susp, String src)
			throws IOException {
		String[] wordsSusp = susp.split(" ");
		String[] wordsSrc = src.split(" ");

		srcWords = wordsSrc.length;
		suspWords = wordsSusp.length;

		a = (double) suspWords / (double) srcWords;

		int count = 0;
		int countBi = 0;
		for (int i = 0; i < wordsSusp.length; i++) {
			if (src.contains(" " + wordsSusp[i] + " "))
				count++;
			if (i > 0
					&& src.contains(" " + wordsSusp[i - 1] + " " + wordsSusp[i]
							+ " "))
				countBi++;
		}

		b = (double) count / (double) wordsSusp.length;
		c = (double) count / (double) wordsSrc.length;
		f = (double) countBi / (double) (wordsSusp.length - 1);
		g = (double) countBi / (double) (wordsSrc.length - 1);
	}

	public static double numOfSentence(String susp, String src)
			throws IOException {
		String[] sentSusp = susp.split(". ");
		String[] sentSrc = src.split(". ");
		return ((double) sentSusp.length / (double) sentSrc.length);
	}

	public static double numOfThe(String susp, String src) throws IOException {
		String[] sentSusp = susp.split(" the ");
		String[] sentSrc = src.split(" the ");
		int countSusp = sentSusp.length - 1;
		int countSrc = sentSrc.length - 1;
		sentSusp = susp.split("The ");
		sentSrc = src.split("The ");
		countSusp += sentSusp.length - 1;
		countSrc += sentSrc.length - 1;
		if (countSrc == 0)
			return 0;
		return ((double) countSusp / (double) countSrc);
	}

	public static double numOfChars(String susp, String src) throws IOException {
		return ((double) susp.length() / (double) src.length());
	}

	public static double numOfLines(String susp, String src) throws IOException {
		String[] srcLines = src.split("\n");
		return ((double) numLines / (double) srcLines.length);
	}

	public static void numOfKeyWordSusp(String susp, String src)
			throws IOException {
		String[] stopWords = { "", ",", "\"", "is", "was", "were", "are",
				"his", "her", "my", "our", "your", "their", "for", "the",
				"The", "that", "of", "on", "in", "a", "an", "to", "and", "with" };
		HashMap<String, Integer> num = new HashMap<String, Integer>();
		String[] words = susp.split(" ");
		for (int i = 0; i < words.length; i++) {
			if (num.containsKey(words[i]))
				num.put(words[i], num.get(words[i]) + 1);
			else
				num.put(words[i], 1);
		}
		int max = 0;
		String MOW = "";
		Object[] keys = num.keySet().toArray();
		for (int i = 0; i < keys.length; i++) {
			boolean noProb = true;
			for (int j = 0; j < stopWords.length; j++)
				if (keys[i].equals(stopWords[j]))
					noProb = false;
			if (num.get(keys[i]) > max && noProb) {
				max = num.get(keys[i]);
				MOW = (String) keys[i];
			}
		}
		keyWordSusp = MOW;
		int countMOW = src.split(" " + MOW + " ").length - 1;
		if (countMOW == 0)
			l = 0;
		else
			l = (double) max / (double) countMOW;
		m = (double) countMOW / (double) srcWords;
	}

	public static void numOfKeyWordSrc(String susp, String src)
			throws IOException {
		String[] stopWords = { "", ",", "\"", "is", "was", "were", "are",
				"his", "her", "my", "our", "your", "their", "for", "the",
				"The", "that", "of", "on", "in", "a", "an", "to", "and", "with" };
		HashMap<String, Integer> num = new HashMap<String, Integer>();
		String[] words = src.split(" ");
		for (int i = 0; i < words.length; i++) {
			if (num.containsKey(words[i]))
				num.put(words[i], num.get(words[i]) + 1);
			else
				num.put(words[i], 1);
		}
		int max = 0;
		String MOW = "";
		Object[] keys = num.keySet().toArray();
		for (int i = 0; i < keys.length; i++) {
			boolean noProb = true;
			for (int j = 0; j < stopWords.length; j++)
				if (keys[i].equals(stopWords[j]))
					noProb = false;
			if (num.get(keys[i]) > max && noProb) {
				max = num.get(keys[i]);
				MOW = (String) keys[i];
			}
		}
		keyWordSrc = MOW;
		int countMOW = susp.split(" " + MOW + " ").length;
		if (countMOW == 0)
			n = 0;
		else
			n = (double) max / (double) countMOW;
		o = (double) countMOW / (double) suspWords;
	}

	public static double mutualKeyWord(String susp, String src)
			throws IOException {
		if (keyWordSrc.equals(keyWordSusp))
			return 1;
		return 0;
	}

	public static double numOfSpecialWordSusp(String susp, String src)
			throws IOException {
		String[] words = susp.split(" ");
		HashSet<String> special = new HashSet<String>();
		for (int i = 1; i < words.length; i++) {
			if (!words[i].contains("\n")) {
				if ((!words[i].toLowerCase().equals(words[i]))) {
					if (words[i].length() > 1 && !words[i - 1].contains(".")) {
						special.add(remove(words[i]));
					}
				}
			}
		}
		Object[] spwords = special.toArray();
		int count = 0;
		for (int i = 0; i < spwords.length; i++)
			if (src.contains((String) spwords[i]))
				count++;
		return (double) count / (double) special.size();
	}

	public static double numOfSpecialWordSrc(String susp, String src)
			throws IOException {
		String[] words = src.split(" ");
		HashSet<String> special = new HashSet<String>();
		for (int i = 1; i < words.length; i++) {
			if (!words[i].contains("\n")) {
				if ((!words[i].toLowerCase().equals(words[i]))) {
					if (words[i].length() > 1 && !words[i - 1].contains(".")) {
						special.add(remove(words[i]));
					}
				}
			}
		}
		Object[] spwords = special.toArray();
		int count = 0;
		for (int i = 0; i < spwords.length; i++)
			if (susp.contains((String) spwords[i]))
				count++;
		return (double) count / (double) special.size();
	}

	public static String remove(String str) {
		// System.out.print(str+"**");
		String out = str;
		int size = 0;
		while (out.length() != size) {
			size = out.length();
			if (out.substring(out.length() - 1, out.length()).equals(","))
				out = out.substring(0, out.length() - 1);
			if (out.substring(out.length() - 1, out.length()).equals("."))
				out = out.substring(0, out.length() - 1);
			if (out.substring(out.length() - 1, out.length()).equals(":"))
				out = out.substring(0, out.length() - 1);
			if (out.substring(out.length() - 1, out.length()).equals("("))
				out = out.substring(0, out.length() - 1);
			if (out.substring(out.length() - 1, out.length()).equals(")"))
				out = out.substring(0, out.length() - 1);
			if (out.substring(0, 1).equals("("))
				out = out.substring(1);
			if (out.substring(0, 1).equals(")"))
				out = out.substring(1);
			if (out.substring(0, 1).equals("."))
				out = out.substring(1);
			if (out.substring(0, 1).equals(","))
				out = out.substring(1);
			if (out.substring(0, 1).equals(":"))
				out = out.substring(1);
		}
		// System.out.println(out.toLowerCase());
		return out;
	}

	public static int findPairs(String pairs, String susp, String src)
			throws IOException {
		String srcName = src + ".txt";
		String suspSrc = susp + " " + srcName;
		if (pairs.contains(suspSrc))
			return 1;
		return 0;
	}
}
