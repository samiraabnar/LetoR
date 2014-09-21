package ir.ac.ut.analyzer;

import org.xml.sax.helpers.DefaultHandler;

public class DocumentHandler extends DefaultHandler {
	/*
	 * private final String TEXT = "TEXT"; private final String DOCID = "DOCID";
	 * private final String DATE = "DATE"; private final String CAT = "CAT";
	 * 
	 * boolean docId = false; boolean text = false; boolean date = false;
	 * boolean cat = false;
	 * 
	 * private String textBuffer = ""; private String docIDBuffer = ""; private
	 * String dateBuffer = ""; private String catBuffer = "";
	 * 
	 * 
	 * public void startElement(String namespaceURI, String localName, String
	 * qName, Attributes atts) throws SAXException {
	 * 
	 * if (DOCID.equalsIgnoreCase(localName)) { docId = true; } else if
	 * (TEXT.equalsIgnoreCase(localName)) { text = true; } else if
	 * (DATE.equalsIgnoreCase(localName)) { date = true; } else if
	 * (CAT.equalsIgnoreCase(localName) &&
	 * atts.getValue("xml:lang").equalsIgnoreCase("fa")) { cat = true; } }
	 * 
	 * public void characters(char[] ch, int start, int length) throws
	 * SAXException {
	 * 
	 * if(docId){ docIDBuffer += new String(ch, start, length); } else if(text){
	 * textBuffer += new String(ch, start, length); } else if(date){ dateBuffer
	 * += new String(ch, start, length); } else if(cat){ catBuffer += new
	 * String(ch, start, length); } }
	 * 
	 * public void endElement(String namespaceURI, String localName, String
	 * qName) throws SAXException { if (TEXT.equalsIgnoreCase(localName)) { try
	 * { // Indexer.indexDocument(docIDBuffer, textBuffer, dateBuffer,
	 * catBuffer); textBuffer = ""; docIDBuffer = ""; dateBuffer = ""; catBuffer
	 * = "";
	 * 
	 * } catch (IOException e) { e.printStackTrace(); //To change body of catch
	 * statement use File | Settings | File Templates. } text = false; } else if
	 * (DOCID.equalsIgnoreCase(localName)) docId = false; else if
	 * (CAT.equalsIgnoreCase(localName)) cat = false; else if
	 * (DATE.equalsIgnoreCase(localName)) date = false; }
	 */
}
