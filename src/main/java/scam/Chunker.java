package scam;

import static ir.ac.ut.common.Util.srcIreader;
import static ir.ac.ut.common.Util.suspIreader;
import static ir.ac.ut.config.Config.getSrcMapPath;
import static ir.ac.ut.config.Config.getSuspMapPath;
import ir.ac.ut.common.Pair;
import ir.ac.ut.common.Util;
import ir.ac.ut.config.Config;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Map;
import java.util.TreeMap;

import org.apache.lucene.index.Fields;
import org.apache.lucene.index.MultiFields;
import org.apache.lucene.index.Terms;
import org.apache.lucene.index.TermsEnum;
import org.apache.lucene.util.BytesRef;

/**
 * Created by Mah Sa on 7/19/14.
 */
public class Chunker {
	private static TreeMap<String, ArrayList<Pair<Integer, Integer>>> vocabRepository;
	public static double EPS = 2.5;
	public static int ALPHA = 1;
	private static TreeMap<Integer, ArrayList<Pair<Integer, Double>>> sortedResults;

	public static void main(String[] args) throws Exception {
		sortedResults = new TreeMap<Integer, ArrayList<Pair<Integer, Double>>>();
		makeRepository();
		rank();
		write();
	}

	public static void write() throws IOException {
		System.out.println("here");
		BufferedWriter bwriter = new BufferedWriter(new FileWriter(
				Config.getSCAMResultsPath()), 10 * 1024);
		for (Map.Entry<Integer, ArrayList<Pair<Integer, Double>>> entry : sortedResults
				.entrySet()) {
			ArrayList<Pair<Integer, Double>> list = entry.getValue();
			Collections.sort(list, new Comparator<Pair<Integer, Double>>() {
				public int compare(Pair<Integer, Double> o1,
						Pair<Integer, Double> o2) {
					return (o2.getSecond().compareTo(o1.getSecond()));
				}
			});
			int i = 0;
			for (Pair<Integer, Double> pair : list)
				bwriter.write(entry.getKey() + " 0 " + pair.getFirst() + " "
						+ (i++) + " " + pair.getSecond() + " RUN0\n");
		}
		bwriter.close();
	}

	public static void rank() throws Exception {
		Map<String, Integer> suspDocMap = Util.loadDocMap(getSuspMapPath());
		// for(int i = 0; i<30; i++)
		for (int susp : suspDocMap.values()) {
			computeScore(susp);
		}
	}

	public static void computeScore(int susp) throws Exception {
		System.out.println("S: " + susp);
		Map<Integer, ArrayList<Pair<Integer, Integer>>> closenessSet; // srcID /
																		// fr,fs
		closenessSet = new TreeMap<Integer, ArrayList<Pair<Integer, Integer>>>();
		TreeMap<String, Integer> chunks;
		org.apache.lucene.document.Document doc = suspIreader.document(susp);
		chunks = getChunks(susp);
		Pair<Integer, Double> minPair = new Pair<Integer, Double>(0,
				Double.MIN_VALUE);
		ArrayList<Pair<Integer, Double>> sortedList = new ArrayList<Pair<Integer, Double>>();
		for (String chunk : chunks.keySet()) {
			if (vocabRepository.containsKey(chunk)) {
				ArrayList<Pair<Integer, Integer>> list = vocabRepository
						.get(chunk);
				double fr = 1;
				int src;
				int fs = 1;
				for (Pair<Integer, Integer> pair : list)
					if (pair.getFirst() == susp)
						fs = pair.getSecond();
				for (Pair<Integer, Integer> pair : list) {
					if (pair.getFirst() != susp) {
						src = pair.getFirst();
						fr = (double) pair.getSecond();
						double diff = (double) EPS
								- ((fr / (double) fs) + ((double) fs / fr));
						if (diff <= 0)
							continue;
						ArrayList<Pair<Integer, Integer>> list1;
						if (closenessSet.containsKey(src)) {
							list1 = closenessSet.get(src);
							list1.add(new Pair<Integer, Integer>(fs, pair
									.getSecond()));
							closenessSet.remove(src);
							closenessSet.put(src, list1);
						} else {
							list1 = new ArrayList<Pair<Integer, Integer>>();
							list1.add(new Pair<Integer, Integer>(fs, pair
									.getSecond()));
							closenessSet.put(src, list1);
						}
					}
				}
			}
		}
		for (Map.Entry<Integer, ArrayList<Pair<Integer, Integer>>> entry : closenessSet
				.entrySet()) {
			int fsrc = 0;
			int fsusp = 0;
			double sim1 = 0.0;
			double sim2 = 0.0;
			double sim3 = 0.0;
			double sim = 0.0;
			for (Pair<Integer, Integer> pair : entry.getValue()) {
				fsrc = pair.getFirst();
				fsusp = pair.getSecond();
				sim1 += (double) ((double) fsrc * (double) fsusp);
				sim2 += (double) ((double) fsrc * (double) fsrc);
				sim3 += (double) ((double) fsusp * (double) fsusp);
				sim += (sim2 > sim3) ? sim1 / (sim3 * sim3) : sim1
						/ (sim2 * sim2);
			}
			if (sortedList.size() >= 99) {
				if (sim > minPair.getSecond()) {
					Pair<Integer, Double> thisPair = new Pair<Integer, Double>(entry.getKey(),
							sim);
					sortedList.add(thisPair);
					sortedList.remove(minPair);
					minPair = thisPair;
				}
			} else {
				Pair<Integer, Double> thisPair = new Pair<Integer,Double>(entry.getKey(), sim);
				sortedList.add(thisPair);
				if (sim < minPair.getSecond())
					minPair = thisPair;
			}
			// bwriter.write(susp+" 0 "+srcId+" "+sim+ "RUN0\n");
		}
		sortedResults.put(susp, sortedList);
		// bwriter.close();
	}

	public static void makeRepository() throws Exception {
		vocabRepository = new TreeMap<String, ArrayList<Pair<Integer, Integer>>>();
		Map<String, Integer> srcDocMap = Util.loadDocMap(getSrcMapPath());
		for (int src : srcDocMap.values()) {
			TreeMap<String, Integer> chunks;
			org.apache.lucene.document.Document doc = srcIreader.document(src);
			chunks = getChunks(src);
			for (String chunk : chunks.keySet()) {
				ArrayList<Pair<Integer, Integer>> list;
				if (vocabRepository.containsKey(chunk))
					list = vocabRepository.get(chunk);
				else {
					list = new ArrayList<Pair<Integer, Integer>>();
					vocabRepository.remove(chunk);
				}
				list.add(new Pair<Integer, Integer>(src, chunks.get(chunk)));
				vocabRepository.put(chunk, list);
			}
		}
		System.out.println("i'm writing");
		BufferedWriter bwriter = new BufferedWriter(new FileWriter(
				Config.getFeaturesPath() + "khar"), 10 * 1024);
		for (String vocab : vocabRepository.keySet()) {
			bwriter.write(vocab + ":");
			for (Pair<Integer, Integer> pair : vocabRepository.get(vocab))
				bwriter.write(pair.getFirst() + "," + pair.getSecond() + "-");
			bwriter.write("\n");
		}
		bwriter.close();
	}

	public static TreeMap<String, Integer> getChunks(int src)
			throws IOException {
		TreeMap<String, Integer> numOfwords = new TreeMap<String,Integer>();
		Fields fields = MultiFields.getFields(srcIreader);
		Terms terms = srcIreader.getTermVector(src, "TEXT");
		TermsEnum iterator = terms.iterator(null);
		BytesRef byteRef = null;
		while ((byteRef = iterator.next()) != null) {
			String term = new String(byteRef.bytes, byteRef.offset,
					byteRef.length);
			numOfwords.put(term, (int) iterator.totalTermFreq());
		}
		return numOfwords;

	}
}
