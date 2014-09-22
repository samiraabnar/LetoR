package ir.ac.ut.config;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Created by Mahsa on 5/22/14.
 */
public class Config {
	public static Properties configFile = new Properties();

	static {
			  try {  	                        
	                        File cFile = new File("config.properties");
	                        System.out.println("Config File Path: " + cFile.getAbsolutePath());

	                        InputStream stream = new FileInputStream(cFile);
	                        configFile.load(stream);

		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	

	public static String getJudgePath() {
		if (configFile.getProperty("phase").equals("test"))
			return configFile.getProperty("testJudgePath");
		else
			return configFile.getProperty("judgePath");
	}

	public static String getSCAMResultsPath() {
		return configFile.getProperty("SCAMResultsPath");
	}

	public static String getIndexPath() {
		if (configFile.getProperty("phase").equals("test")) {
			if (configFile.getProperty("selector").equals("src"))
				return configFile.getProperty("testSrcIndexPath");
			else if (configFile.getProperty("selector").equals("susp"))
				return configFile.getProperty("testSuspIndexPath");
			else
				return configFile.getProperty("suspIndexPath");
		} else {
			if (configFile.getProperty("selector").equals("src"))
				return configFile.getProperty("srcIndexPath");
			else if (configFile.getProperty("selector").equals("susp"))
				return configFile.getProperty("suspIndexPath");
			else
				return configFile.getProperty("suspIndexPath");
		}
	}

	public static String getSrcIndexPath() {
		if (configFile.getProperty("phase").equals("test"))
			return configFile.getProperty("testSrcIndexPath");
		else
			return configFile.getProperty("srcIndexPath");
	}

	public static String getStopWordsPath() {
		if (getLanguage().equals("FA"))
			return configFile.getProperty("FA_stopword");
		else
			return configFile.getProperty("EN_stopword");
	}

	public static String getSuspIndexPath() {
		if (configFile.getProperty("phase").equals("test"))
			return configFile.getProperty("testSuspIndexPath");
		else
			return configFile.getProperty("suspIndexPath");
	}

	public static String getFeaturesPath() {
		if (configFile.getProperty("phase").equals("test"))
			return configFile.getProperty("testFeaturesPath");
		else
			return configFile.getProperty("featuresPath");
	}

	public static String getCorpusPath() {
		if (configFile.getProperty("phase").equals("test")) {
			if (configFile.getProperty("selector").equals("src"))
				return configFile.getProperty("testSrcCorpusPath");
			else if (configFile.getProperty("selector").equals("susp"))
				return configFile.getProperty("testSuspCorpusPath");
			else
				return configFile.getProperty("testSuspCorpusPath");
		} else {
			if (configFile.getProperty("selector").equals("src"))
				return configFile.getProperty("srcCorpusPath");
			else if (configFile.getProperty("selector").equals("susp"))
				return configFile.getProperty("suspCorpusPath");
			else
				return configFile.getProperty("suspCorpusPath");
		}
	}

	public static String getLanguage() {
		return configFile.getProperty("lang");
	}

	public static String getSuspMapPath() {
		if (configFile.getProperty("phase").equals("test"))
			return configFile.getProperty("testSuspDocMapPath");
		else
			return configFile.getProperty("suspDocMapPath");
	}

	public static String getSrcMapPath() {
		if (configFile.getProperty("phase").equals("test"))
			return configFile.getProperty("testSrcDocMapPath");
		else
			return configFile.getProperty("srcDocMapPath");
	}

	public static String getMapPath() {
		if (configFile.getProperty("phase").equals("test")) {
			if (configFile.getProperty("selector").equals("src"))
				return configFile.getProperty("testSrcDocMapPath");
			else if (configFile.getProperty("selector").equals("susp"))
				return configFile.getProperty("testSuspDocMapPath");
			else
				return configFile.getProperty("testSuspDocMapPath");
		} else {
			if (configFile.getProperty("selector").equals("src"))
				return configFile.getProperty("srcDocMapPath");
			else if (configFile.getProperty("selector").equals("susp"))
				return configFile.getProperty("suspDocMapPath");
			else
				return configFile.getProperty("suspDocMapPath");
		}
	}

	public static String getCandidatesMapPath() {
		if (configFile.getProperty("phase").equals("test"))
			return configFile.getProperty("testCandidatesMapPath");
		else
			return configFile.getProperty("candidatesMapPath");
	}

	public static String getPairsPath() {
		if (configFile.getProperty("phase").equals("test"))
			return configFile.getProperty("testPairsPath");
		else
			return configFile.getProperty("pairsPath");
	}

	public static String getSortedResultsPath() {
		return configFile.getProperty("sortedResultsPath");
	}

	public static String getClassifierResultsPath() {
		return configFile.getProperty("classifierResultsPath");
	}

	public static String getSVMRankFolder() {
		return configFile.getProperty("svmrankFolderPath");
	}
	
	public static String getFeaturedIndexPath()
	{
		if (configFile.getProperty("phase").equals("test")) {
			if (configFile.getProperty("selector").equals("src"))
				return configFile.getProperty("testFeaturedSrcIndexPath");
			else if (configFile.getProperty("selector").equals("susp"))
				return configFile.getProperty("testFeaturedSuspIndexPath");
			else
				return configFile.getProperty("testFeaturedSuspIndexPath");
		} else {
			if (configFile.getProperty("selector").equals("src"))
				return configFile.getProperty("featuredSrcIndexPath");
			else if (configFile.getProperty("selector").equals("susp"))
				return configFile.getProperty("featuredSuspIndexPath");
			else
				return configFile.getProperty("featuredSuspIndexPath");
		}
	}

	public static String getPuncMapPath() {
		// TODO Auto-generated method stub
		return configFile.getProperty("puncsMap");
	}
}