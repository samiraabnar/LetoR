/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package ir.ac.ut.engine;


import org.apache.commons.lang3.StringUtils;

public class Persian_Normalizer  {

        //////////////////////////nubers
    	/** Persian 0*/
	public static final String FA_D0 = "\u06F0";
	/** Persian 1*/
	public static final String FA_D1 = "\u06F1";
	/** Persian 2*/
	public static final String FA_D2 = "\u06F2";
	/** Persian 3*/
	public static final String FA_D3 = "\u06F3";
	/**Persian 4*/
	public static final String FA_D4 = "\u06F4";
	/** Persian 5*/
	public static final String FA_D5 = "\u06F5";
	/** Persian 6*/
	public static final String FA_D6 = "\u06F6";
	/** Persian 7*/
	public static final String FA_D7 = "\u06F7";
	/**Persian 8*/
	public static final String FA_D8 = "\u06F8";
	/** Persian 9*/
	public static final String FA_D9 = "\u06F9";
		
        /** Arabic 0*/
	public static final String Ar_D0 = "\u0660";
        /** Arabic 1*/
	public static final String Ar_D1 = "\u0661";
        /** Arabic 2*/
	public static final String Ar_D2 = "\u0662";
        /** Arabic 3*/
	public static final String Ar_D3 = "\u0663";
        /** Arabic 4*/
	public static final String Ar_D4 = "\u0664";
        /** Arabic 5*/
	public static final String Ar_D5 = "\u0665";
        /** Arabic 6*/
	public static final String Ar_D6 = "\u0666";
        /** Arabic 7*/
	public static final String Ar_D7 = "\u0667";
        /** Arabic 8*/
	public static final String Ar_D8 = "\u0668";
        /** Arabic 9*/
	public static final String Ar_D9 = "\u0669";

        /** English 0*/
	public static final String EN_D0 = "\u0030";
	/** English 1*/
	public static final String EN_D1 = "\u0031";
	/** English 2*/
	public static final String EN_D2 = "\u0032";
	/** English 3*/
	public static final String EN_D3 = "\u0033";
	/** English 4*/
	public static final String EN_D4 = "\u0034";
	/** English 5*/
	public static final String EN_D5 = "\u0035";
	/** English 6*/
	public static final String EN_D6 = "\u0036";
	/** English 7*/
	public static final String EN_D7 = "\u0037";
	/** English 8*/
	public static final String EN_D8 = "\u0038";
	/**English 9*/
	public static final String EN_D9 = "\u0039";
        //////////////////////////////////////////////////////
	
	/** invalid space*/
	public static final char SPACE1 = (char) 160;
	public static final char SPACE2 = (char) 8206;
	public static final char SPACE3 = '\u200c';
        
	/** ReGex for invalid spaces  */
	public static final String SPACE_EXEPT_REGEX = "[" + SPACE1 + SPACE2 + SPACE3 +  " \\s]+";
        
    public static String persianNormalizer(String input) {
        StringBuilder s = new StringBuilder(input);
        StringPairArray tmp = new StringPairArray();
        
        tmp.add("أ", "ا");
        tmp.add("ؤ", "و");
        tmp.add("إ", "ا");
        tmp.add("ئ", "ی");
        tmp.add("ا", "ا");
        tmp.add("ڪ", "ک");
        tmp.add("ة", "ه");
        tmp.add("ۓ", "ی");
        tmp.add("ے", "ی");
        tmp.add("ە", "ه");
        tmp.add("ہ", "ه");
        tmp.add("ۀ", "ه");
        tmp.add("ك", "ک");
        tmp.add("ي", "ی");
        tmp.add("ٵ", "ا");
        tmp.add("ٷ", "و");
        tmp.add("ٸ", "ی");
        tmp.add("ﭕ", "ی");
        tmp.add("ﭙ", "پ");
        tmp.add("ﭽ", "چ");
        tmp.add("ﮋ", "ژ");
        tmp.add("ﮑ", "ک");
        tmp.add("ﮕ", "گ");
        tmp.add("ﮭ", "ه");
        tmp.add("ﮱ", "ی");
        tmp.add("ﯣ", "و");
        tmp.add("ﯧ", "ی");
        tmp.add("ﯿ", "ی");
        tmp.add("ﴽ", "ا");
        tmp.add("ﯿ", "ی");
        tmp.add("ﺂ", "ا");
        tmp.add("ﺄ", "ا");
        tmp.add("ﺆ", "و");
        tmp.add("ﺈ", "ا");
        tmp.add("ﺌ", "ی");
        tmp.add("ﺎ", "ا");
        tmp.add("ﺒ", "ب");
        tmp.add("ﺔ", "ه");
        tmp.add("ﺘ", "ت");
        tmp.add("ﺜ", "ث");
        tmp.add("ﺠ", "ج");
        tmp.add("ﺤ", "ح");
        tmp.add("ﺨ", "خ");
        tmp.add("ﺪ", "د");
        tmp.add("ﺬ", "ذ");
        tmp.add("ﺮ", "ر");
        tmp.add("ﺰ", "ز");
        tmp.add("ﺴ", "س");
        tmp.add("ﺸ", "ش");
        tmp.add("ﺼ", "ص");
        tmp.add("ﻀ", "ض");
        tmp.add("ﻄ", "ط");
        tmp.add("ﻈ", "ظ");
        tmp.add("ﻌ", "ع");
        tmp.add("ﻐ", "غ");
        tmp.add("ﻘ", "ق");
        tmp.add("ﻜ", "ک");
        tmp.add("ﻠ", "ل");
        tmp.add("ﻤ", "م");
        tmp.add("ﻨ", "ن");
//        tmp.add("آ", "ا");
        tmp.add("ﻮ", "و");
        tmp.add("ﻴ", "ی");
        tmp.add("ﻼ", "لا");
        tmp.add("ء","");
        tmp.add("ٔ","");
        //Digit: En->Fa
        tmp.add(EN_D0, FA_D0);
        tmp.add(EN_D1, FA_D1);
        tmp.add(EN_D2, FA_D2);
        tmp.add(EN_D3, FA_D3);
        tmp.add(EN_D4, FA_D4);
        tmp.add(EN_D5, FA_D5);
        tmp.add(EN_D6, FA_D6);
        tmp.add(EN_D7, FA_D7);
        tmp.add(EN_D8, FA_D8);
        tmp.add(EN_D9, FA_D9);
        //Digit: Ar->Fa        
        tmp.add(Ar_D0, FA_D0);
        tmp.add(Ar_D1, FA_D1);
        tmp.add(Ar_D2, FA_D2);
        tmp.add(Ar_D3, FA_D3);
        tmp.add(Ar_D4, FA_D4);
        tmp.add(Ar_D5, FA_D5);
        tmp.add(Ar_D6, FA_D6);
        tmp.add(Ar_D7, FA_D7);
        tmp.add(Ar_D8, FA_D8);
        tmp.add(Ar_D9, FA_D9);
        //Symboles
        tmp.add("?", "؟");
        tmp.add("%", "٪");
        tmp.add("–","-");
        tmp.add("˗","-");
        tmp.add("־","-");
        tmp.add("ـ","-");
        String[] searchList = tmp.getA();
        String[] replacementList = tmp.getB();
        String ret = StringUtils.replaceEach(input, searchList, replacementList);
        return ret;
    }

}

class StringPairArray {
    private String[] data2;
    private String[] data1;
    int len = 0;
    {
        data1 = new String[100];
        data2 = new String[100];
    }

    public void add(String a, String b) {
        data1[len] = a;
        data2[len] = b;
        len++;
    }

    public String[] getA() {
        return data1;
    }

    public String[] getB() {
        return data2;
    }
//    public static void main(String[] args) throws FileNotFoundException {
//        Scanner scanner = new Scanner(new File("Resources/stopwords.txt"));
//        PrintWriter pw = new PrintWriter(new File("Resources/stopwords1.txt"));
//        
//        while(scanner.hasNext()){
//            String Line = Persian_Normalizer.persianNormalizer(scanner.nextLine().trim());
//            pw.println(Line);
//        }
//        pw.close();
//    }
}