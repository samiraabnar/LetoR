package ir.ac.ut.engine;

import static ir.ac.ut.config.Config.configFile;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import org.apache.lucene.analysis.Analyzer;
import org.iis.plagiarismdetector.core.lucene.MyAnalyzer;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by Mahsa on 6/16/14.
 */
public class Engine {
	public static Set<String> Stoplistloader(String filePath)
			throws FileNotFoundException {
		Set<String> stopCollection = new HashSet<String>();
		Scanner fileScanner = new Scanner(new File(filePath));
		while (fileScanner.hasNextLine()) {
			stopCollection.add(fileScanner.nextLine().trim());
		}
		fileScanner.close();
		return stopCollection;
	}

	public static Analyzer MyPersianAnalyzer(Boolean steming,
			Boolean stopwordRemooving) throws FileNotFoundException {
		Set<String> stopword = Stoplistloader(configFile
				.getProperty("FA_stopword"));
		if (steming && stopwordRemooving) {
			return (new MyAnalyzer(steming, stopword)).getAnalyzer("FA");
		} else if (!steming && stopwordRemooving) {
			return (new MyAnalyzer(steming, stopword)).getAnalyzer("FA");
		} else if (steming && !stopwordRemooving) {
			return (new MyAnalyzer(steming)).getAnalyzer("FA");
		}
		return (new MyAnalyzer(steming)).getAnalyzer("FA");
	}

	public static Analyzer MyEnglishAnalyzer(Boolean steming,
			Boolean stopwordRemooving) throws FileNotFoundException {
		if(stopwordRemooving)
		{
			Set<String> stopword = Stoplistloader(configFile
					.getProperty("EN_stopword"));
			MyAnalyzer mAnalyzer = new MyAnalyzer(steming, stopword);
			return mAnalyzer.getAnalyzer("EN");
		}
		else
		{
			MyAnalyzer mAnalyzer = new MyAnalyzer(steming);
			return mAnalyzer.getAnalyzer("EN");
		}
	}

}
