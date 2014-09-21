/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ir.ac.ut.common;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 ** ------- هو اللطیف ------- **
 * 
 * @author Mostafa Dehghani
 *
 **/
public class L2RFeatureNormalizer {
	public static void normalizer(String featureFilePath, String outputPath) {
		try {
			ArrayList<String> docID = new ArrayList<String>();
			String QueryID = "";
			ArrayList<String> Rank = new ArrayList<String>();
			String InputFile = featureFilePath;
			File f = new File(outputPath);
			if (f.exists())
				f.delete();
			Scanner fileScanner = new Scanner(new File(InputFile));
			String line = fileScanner.nextLine();
			String[] ComentSeperator = line.split("#");
			docID.add(ComentSeperator[1]);
			String[] scoresPart = ComentSeperator[0].trim().split("\\s+");
			ArrayList<Double>[] Scores = new ArrayList[scoresPart.length - 2];
			Rank.add(scoresPart[0]);
			QueryID = scoresPart[1];
			for (int k = 2; k < scoresPart.length; k++) {
				String S = scoresPart[k].split(":")[1].trim();
				Scores[k - 2] = new ArrayList<Double>();
				Scores[k - 2].add(Double.parseDouble(S));
			}
			while (fileScanner.hasNextLine()) {
				line = fileScanner.nextLine();
				ComentSeperator = line.split("#");
				line = ComentSeperator[0];
				scoresPart = line.split("\\s+");
				if (QueryID.equals(scoresPart[1])) {
					Rank.add(scoresPart[0]);
					docID.add(ComentSeperator[1]);
					for (int k = 2; k < scoresPart.length; k++) {
						String S = scoresPart[k].substring(scoresPart[k]
								.indexOf(":") + 1);
						Scores[k - 2].add(Double.parseDouble(S));
					}
				} else {
					BufferedWriter out = new BufferedWriter(new FileWriter(
							outputPath, true));
					ArrayList<String> tmp = LineMaker(Normalizer(Scores),
							docID, QueryID, Rank);
					for (String L : tmp)
						out.write(L + "\r\n");
					out.close();
					for (int k = 0; k < Scores.length; k++) {
						Scores[k].clear();
					}
					for (int k = 2; k < scoresPart.length; k++) {
						String S = scoresPart[k].substring(scoresPart[k]
								.indexOf(":") + 1);
						Scores[k - 2].add(Double.parseDouble(S));
					}
					Rank.clear();
					docID.clear();
					Rank.add(scoresPart[0]);
					docID.add(ComentSeperator[1]);
					QueryID = scoresPart[1];
				}
			}
			fileScanner.close();
			BufferedWriter out = new BufferedWriter(new FileWriter(outputPath,
					true));
			ArrayList<String> tmp = LineMaker(Normalizer(Scores), docID,
					QueryID, Rank);
			for (String L : tmp)
				out.write(L + "\r\n");
			out.close();
			for (int k = 0; k < Scores.length; k++) {
				Scores[k].clear();
			}
		} catch (FileNotFoundException ex) {
			Logger.getLogger(L2RFeatureNormalizer.class.getName()).log(
					Level.SEVERE, null, ex);
		} catch (IOException ex) {
			Logger.getLogger(L2RFeatureNormalizer.class.getName()).log(
					Level.SEVERE, null, ex);
		}
	}

	public static ArrayList<Double>[] Normalizer(ArrayList<Double>[] Scores) {
		ArrayList<Double>[] NormalScores = new ArrayList[Scores.length];
		for (int j = 0; j < Scores.length; j++) {
			NormalScores[j] = new ArrayList<Double>();
			double Min = Min_Max(Scores[j])[0];
			double Max = Min_Max(Scores[j])[1];
			for (int k = 0; k < Scores[j].size(); k++) {
				NormalScores[j].add(FeatureNormalizer(Scores[j].get(k), Min,
						Max));
			}
		}
		return NormalScores;
	}

	public static ArrayList<String> LineMaker(ArrayList<Double>[] Scores,
			ArrayList<String> docID, String queryID, ArrayList<String> Rank) {
		String line = "";
		ArrayList<String> Lines = new ArrayList<String>();
		for (int k = 0; k < Rank.size(); k++) {
			line = Rank.get(k) + " " + queryID + " ";
			for (int j = 0; j < Scores.length; j++) {
				line += j + 1 + ":" + Scores[j].get(k) + " ";
			}
			line += "# " + docID.get(k);
			Lines.add(line);
		}
		return Lines;
	}

	public static double[] Min_Max(ArrayList<Double> Feature) {
		ArrayList<Double> F = new ArrayList<Double>();
		F.addAll(Feature);
		double[] MinMax = new double[2];
		Collections.sort(F);
		MinMax[0] = F.get(0);
		MinMax[1] = F.get(F.size() - 1);
		return MinMax;
	}

	public static double FeatureNormalizer(double Score, double Min, double Max) {
		if (Max != Min)
			return (Score - Min) / (Max - Min);
		else if (Max != 0)
			return 1;
		else
			return 0;
	}
}
