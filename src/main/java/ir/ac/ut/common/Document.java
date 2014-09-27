package ir.ac.ut.common;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import opennlp.tools.sentdetect.SentenceDetectorME;
import opennlp.tools.sentdetect.SentenceModel;

/**
 * Created by Mahsa on 6/28/14.
 */
public class Document {

    static SentenceDetectorME sdetectorEnglish;
    static SentenceDetectorME sdetectorDutch;
    static int static_id = 0;

    static {
        try {
            InputStream is = new FileInputStream(
                    "src/main/resources/en-sent.bin");
            SentenceModel model = new SentenceModel(is);
            sdetectorEnglish = new SentenceDetectorME(model);

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
    List<Sentence> sentences;
    List<String> orderedTerms;

    String name;
    List<Integer> paragraphLength;
    Map<Integer, Map<String, Float>> featuresMap;
    int docLength = 0;
    String lang = "";
    int id;
    int docId;

   /* public Document(File file, String lang) throws FileNotFoundException, IOException, Exception {
        featuresMap = new HashMap<Integer, Map<String, Double>>();
        int id = static_id;
        static_id++;
        this.lang = lang;
        sentences = new ArrayList<Sentence>();
        paragraphLength = new ArrayList<Integer>();
        Scanner in = new Scanner(file);
        while (in.hasNextLine()) {
            String[] sen = null;
            if (lang.equals("EN") || lang.equals("EE")) {
                sen = sdetectorEnglish.sentDetect(in.nextLine());
            } else if (lang.equals("DE") || lang.equals("DR")) {
                sen = sdetectorDutch.sentDetect(in.nextLine());
            } else if (lang.equals("GR") || lang.equals("SP")) {
                for (String str : new SentenceIterator(in.nextLine())) {
                    Sentence s = new Sentence(str, lang);
                    sentences.add(s);
                    docLength += s.getWords().size();
                }
            }
            if (sen != null) {
                paragraphLength.add(sen.length);
                for (String str : sen) {
                    Sentence s = new Sentence(str, lang);
                    sentences.add(s);
                    docLength += s.getWords().size();
                }
            }
        }
        in.close();
        name = file.getName();
        fillOrderedTermsField();
    }
*/
    /*public Document(List<Sentence> s, String name, String lang) {
        int id = static_id;
        static_id++;
        this.lang = lang;
        sentences = new ArrayList<Sentence>();
        sentences.addAll(s);
        for (Sentence temp : s) {
            docLength += temp.getWords().size();
        }
        this.name = name;
        fillOrderedTermsField();
    }*/

    public Document(String content, String lang, int docId) throws Exception {
        int id = static_id;
        featuresMap = new HashMap<Integer, Map<String, Float>>();
        static_id++;
        this.lang = lang;
        this.docId = docId;
        sentences = new ArrayList<Sentence>();
        paragraphLength = new ArrayList<Integer>();
            // Scanner in = new Scanner(file);
        // while(in.hasNextLine()){
        String[] sen = null;
        // if (lang.equals("EN") || lang.equals("EE"))
        sen = sdetectorEnglish.sentDetect(content);
            // else if (lang.equals("DE") || lang.equals("DR"))
        // sen = sdetectorDutch.sentDetect(in.nextLine());
        // else if (lang.equals("GR") || lang.equals("SP")){
        // for (String str : new SentenceIterator(in.nextLine())){
        // Sentence s = new Sentence (str, lang);
        // sentences.add(s);
        // docLength += s.getWords().size();
        // }
        // }
        if (sen != null) {
            paragraphLength.add(sen.length);
            for (String str : sen) {
                Sentence s = new Sentence(str, lang);
                sentences.add(s);
                docLength += s.getWords().size();
            }
        }
		// }
        // in.close();
        name = "name";
        fillOrderedTermsField();
    }

    public List<String> getOrderedTerms() {
        return orderedTerms;
    }

    public void setOrderedTerms(List<String> orderedTerms) {
        this.orderedTerms = orderedTerms;
    }

    public void addFeature(int key, Map<String, Float> value) {
        featuresMap.put(key, value);
    }

    public Map<Integer, Map<String, Float>> getFeaturesMap() {
        return featuresMap;
    }

    public int getDocId() {
        return docId;
    }

    public void setDocId(int docId) {
        this.docId = docId;
    }

    public void setFeaturesMap(Map<Integer, Map<String, Float>> featuresMap) {
        this.featuresMap = featuresMap;
    }

    public List<Sentence> getSentences() {
        return sentences;
    }

    public String getName() {
        return name;
    }

    public List<Integer> getParagraphLength() {
        return paragraphLength;
    }
    public void setSentences(List<Sentence> sentences) {
        this.sentences = sentences;
    }

    public void setName(String name) {
        this.name = name;
    }

    // / n-gram suffix and prifix
    public int getDocLength() {
        return docLength;
    }

    /*public List<Document> partitioner(int size) {
        int numOfPartitions = sentences.size() / size;
        int max = 2;
        if (numOfPartitions > max) {
            size = sentences.size() / max;
            numOfPartitions = sentences.size() / size;
        }
        List<Document> result = new ArrayList<Document>();
        for (int i = 0; i < numOfPartitions; i++) {
            if (i < numOfPartitions - 1) {
                result.add(new Document(sentences.subList(i * size, i * size
                        + size), "known-fake" + i, lang));
            } else {
                result.add(new Document(sentences.subList(i * size,
                        sentences.size()), "known-fake" + i, lang));
            }
        }
        if (numOfPartitions == 0) {
            result.add(this);
        }
        return result;
    }
*/
    public String getLang() {
        return lang;
    }

    public int getId() {
        return id;
    }

    public void fillOrderedTermsField() {
        orderedTerms = new ArrayList<String>();
        for (Sentence sen : getSentences()) {
            for (int i = 0; i < sen.getWords().size(); i++) {
                    orderedTerms.add(sen.getWords().get(i));
                
            }
        }
    }

}
