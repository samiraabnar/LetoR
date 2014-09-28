package ir.ac.ut;

import static ir.ac.ut.config.Config.getSrcMapPath;
import static ir.ac.ut.config.Config.getSuspMapPath;
import ir.ac.ut.common.L2RFeatureNormalizer;
import ir.ac.ut.common.Util;
import ir.ac.ut.config.Config;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

/**
 * Created by Mahsa on 6/2/14.
 */
public class Analyzer {

	public static void main(String[] args) throws IOException {
		//resultMaker(16);
		//resMaker();
		// judgeMaker();
		// tagger();
		// removeFeatures(new ArrayList<>(Arrays.asList(2,3)));
		// L2RFeatureNormalizer.normalizer(Config.getFeaturesPath(),
		// Config.getFeaturesPath() + "-normalized");
	}

	public static void makeFeaturesReady() throws IOException {
		tagger();
		L2RFeatureNormalizer.normalizer(Config.getFeaturesPath() + "s",
				Config.getFeaturesPath() + "s-normalized");
	}

	public static void judgeMaker() throws IOException {
		Map<String, Integer> suspDocMap = null;
		Map<String, Integer> srcDocMap = null;
		BufferedWriter bwriter = null;
		BufferedReader breader = null;
		suspDocMap = Util.loadDocMap(getSuspMapPath());
		srcDocMap = Util.loadDocMap(getSrcMapPath());
		BufferedReader in = new BufferedReader(new InputStreamReader(
				new FileInputStream(Config.getPairsPath())));
		BufferedWriter out = new BufferedWriter(new OutputStreamWriter(
				new FileOutputStream(Config.getJudgePath())));
		String line = in.readLine();
		while (line != null) {
			String[] split = line.split(" ");
			String l1 = split[0];
			String l2 = split[1];
			String newLine = l1 + " 0 " + l2 + " 1\n";
			String t1 = split[0].replaceAll(".txt", "");
			String t2 = split[1].replaceAll(".txt", "");
			System.out.println(t1 + " " + t2);
			out.write(newLine);
			line = in.readLine();
		}
		in.close();
		out.close();
	}

	public static void tagger() throws IOException {

		Map<String, Integer> suspDocMap = null;
		Map<String, Integer> srcDocMap = null;
		
		suspDocMap = Util.loadDocMapFromIndex(Config.getSuspFeaturedIndexPath());
		srcDocMap = Util.loadDocMapFromIndex(Config.getSrcFeaturedIndexPath());

		BufferedReader in = new BufferedReader(new InputStreamReader(
				new FileInputStream(Config.getJudgePath())));
		Map<Integer, Set<Integer>> pairs = new HashMap<Integer, Set<Integer>>();
		String line = in.readLine();
		while (line != null) {
			String[] split = line.replaceAll(".txt", "").split(" ");
			if (!pairs.containsKey(suspDocMap.get(split[0])))
				pairs.put(suspDocMap.get(split[0]), new HashSet<Integer>());
			pairs.get(suspDocMap.get(split[0])).add(srcDocMap.get(split[2]));
			line = in.readLine();
		}
		in.close();
		BufferedReader bin = new BufferedReader(new InputStreamReader(
				new FileInputStream(Config.getFeaturesPath())));
		BufferedWriter bout = new BufferedWriter(new OutputStreamWriter(
				new FileOutputStream(Config.getFeaturesPath() + "s")));
		String bline = bin.readLine();
		while (bline != null) {
			String[] split = bline.split(" ");
			int i = Integer.parseInt(split[0].substring(
					split[0].indexOf(":") + 1, (split[0].length())));
			int j = Integer.parseInt(split[split.length - 1]);
			String tag = "0";
			if (pairs.containsKey(i) && pairs.get(i).contains(j)) {
				tag = "1";
			}
			bout.write(tag + " " + bline + "\n");
			bline = bin.readLine();
		}
		bout.close();
		bin.close();
	}

	public static void resultMaker(int srcColNum) throws IOException {

		String line;
		String newLine;
		String scoreLine;
		BufferedWriter out = new BufferedWriter(new OutputStreamWriter(
				new FileOutputStream("temp.txt")));
		BufferedReader in = new BufferedReader(new InputStreamReader(
				new FileInputStream(Config.getFeaturesPath())));
		BufferedReader scoreIn = new BufferedReader(new InputStreamReader(
				new FileInputStream(Config.getClassifierResultsPath())));
		line = in.readLine();
		while (line != null) {
			scoreLine = scoreIn.readLine();
			String[] split = line.split(" ");
			newLine = split[0].substring(split[0].indexOf("qid:") + 4) + " 0 "
					+ split[srcColNum] + " " + scoreLine + " RUN0\n";
			out.write(newLine);
			line = in.readLine();
		}
		scoreIn.close();
		in.close();
		out.close();
	}

	public static void resMaker() throws IOException {

		Map<Integer, String> suspDocMap = null;
		Map<Integer, String> srcDocMap = null;
		suspDocMap = Util.loadInverseDocMap(getSuspMapPath());
		srcDocMap = Util.loadInverseDocMap(getSrcMapPath());

		String line;
		BufferedReader in = new BufferedReader(new InputStreamReader(
				new FileInputStream("temp.txt")));
		line = in.readLine();
		String docid = line.split(" ")[0];
		Map<Integer, TreeMap<Double, String>> map = new TreeMap<Integer, TreeMap<Double, String>>();
		TreeMap<Double, String> t = new TreeMap<Double, String>();
		map.put(Integer.parseInt(docid), t);
		List<List<Double>> list = new ArrayList<List<Double>>();
		list.add(new ArrayList<Double>());
		int i = 0;
		while (line != null) {
			i++;
			String[] split = line.split(" ");
			if (split[0].equals(docid)) {
				TreeMap child = map.get(Integer.parseInt(docid));
				child.put((Double.parseDouble(split[3])), line);
				map.put(Integer.parseInt(docid), child);
			} else {
				docid = split[0];
				TreeMap child = new TreeMap();
				child.put((Double.parseDouble(split[3])), line);
				map.put(Integer.parseInt(docid), child);
			}
			line = in.readLine();
		}
		in.close();
		BufferedWriter out = new BufferedWriter(new OutputStreamWriter(
				new FileOutputStream(Config.getSortedResultsPath())));
		for (Map.Entry<Integer, TreeMap<Double, String>> entry : map.entrySet()) {
			int count = 0;
			for (Map.Entry<Double, String> sec : entry.getValue()
					.descendingMap().entrySet()) {
				String[] split = sec.getValue().split(" ");
				out.write(suspDocMap.get(Integer.valueOf(split[0])) + " "
						+ split[1] + " "
						+ srcDocMap.get(Integer.valueOf(split[2])) + " "
						+ count++ + " " + split[3] + " " + split[4] + "\n");
			}
		}
		out.close();
	}
}