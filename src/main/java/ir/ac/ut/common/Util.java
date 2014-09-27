package ir.ac.ut.common;

import static ir.ac.ut.config.Config.getSrcMapPath;
import static ir.ac.ut.config.Config.getSuspMapPath;
import ir.ac.ut.config.Config;
import static ir.ac.ut.config.Config.getSrcIndexPath;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import l2r.sam.IndexedDocument;

import org.apache.lucene.index.IndexReader;
import org.apache.lucene.store.SimpleFSDirectory;
import org.iis.plagiarismdetector.core.lucene.IndexInfo;

/**
 * Created by Mahsa on 6/27/14.
 */
public class Util {
	public static IndexReader suspIreader = null;
	static {
		try {
			suspIreader = IndexReader.open(new SimpleFSDirectory(new File(
					Config.getSuspIndexPath())));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	public static IndexReader srcIreader = null;
	static {
		try {
			srcIreader = IndexReader.open(new SimpleFSDirectory(new File(Config
					.getSrcIndexPath())));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static Map<String, Integer> loadDocMap(String path)
			throws IOException {
		Map<String, Integer> map = new TreeMap<String,Integer>();
		BufferedReader breader = new BufferedReader(new FileReader(path));
		String line = breader.readLine();
		while (line != null) {
			String[] split = line.split(",");
			map.put(split[0], Integer.valueOf(split[1]));
			line = breader.readLine();
		}
		breader.close();
		return map;
	}

	public static Map<Integer, Set<Integer>> loadCandidatesMap(String path)
			throws IOException {
		Map<Integer, Set<Integer>> map = new TreeMap<Integer, Set<Integer>>();
		BufferedReader breader = new BufferedReader(new FileReader(path));
		String line = breader.readLine();
		String key = line.split(",")[0];
		String[] split = line.split(",");
		while (line != null) {
			Set<Integer> set = new HashSet<Integer>();
			while (key.equals(split[0]) && line != null) {
				set.add(Integer.valueOf(split[1]));
				if ((line = breader.readLine()) == null)
					break;
				split = line.split(",");
			}
			map.put(Integer.valueOf(key), set);
			key = split[0];
		}
		breader.close();
		return map;
	}

	public static Map<Integer, Set<Integer>> loadCandidatesMapTrecFormat(String path)
			throws IOException {
		
		Map<String, Integer> suspDocMap = null;
		Map<String, Integer> srcDocMap = null;
		suspDocMap = Util.loadDocMap(getSuspMapPath());
		srcDocMap = Util.loadDocMap(getSrcMapPath());

		
		Map<Integer, Set<Integer>> map = new TreeMap<Integer, Set<Integer>>();
		BufferedReader breader = new BufferedReader(new FileReader(path));
		String line = breader.readLine();
		String key = line.split(" ")[0];
		String[] split = line.split(" ");

		while (line != null) {
			Set<Integer> set = new HashSet<Integer>();
			while (key.equals(split[0]) && line != null) {
				set.add(srcDocMap.get(split[2].trim()));
				if ((line = breader.readLine()) == null)
					break;
				split = line.split(" ");
			}
			map.put(suspDocMap.get(key), set);
			key = split[0];
		}
		breader.close();
		return map;
	}

	public static void removeFeatures(List<Integer> toRemove)
			throws IOException {
		BufferedReader breader = new BufferedReader(new FileReader(
				Config.getFeaturesPath()));
		BufferedWriter out = new BufferedWriter(new OutputStreamWriter(
				new FileOutputStream(Config.getFeaturesPath() + "s")));
		String line = breader.readLine();
		while (line != null) {
			String[] split = line.split(" ");
			for (int i = 0; i < split.length; i++) {
				if (toRemove.contains(i - 1))
					continue;
				out.write(split[i]);
				if (i == split.length - 1)
					continue;
				out.write(" ");
			}
			out.write("\n");
			line = breader.readLine();
		}
		breader.close();
		out.close();
	}

	public static Map<Integer, String> loadInverseDocMap(String path)
			throws IOException {
		Map<Integer, String> map = new TreeMap<Integer, String>();
		BufferedReader breader = new BufferedReader(new FileReader(path));
		String line = breader.readLine();
		while (line != null) {
			String[] split = line.split(",");
			map.put(Integer.valueOf(split[1]), split[0]);
			line = breader.readLine();
		}
		breader.close();
		return map;
	}

    public static Map<String, Integer> loadDocMapFromIndex(String indexPath) throws IOException {
        IndexInfo iinfo = new IndexInfo(IndexReader.open(new SimpleFSDirectory(new File(
					indexPath))));
        
        Map<String,Integer> idMap = new HashMap<String, Integer>();
        for(int i=0; i < iinfo.getIndexReader().numDocs(); i++)
        {
            idMap.put(iinfo.getIndexReader().document(i).get(IndexedDocument.FIELD_REAL_ID),i);
        }
        
        return idMap;
    }

}
