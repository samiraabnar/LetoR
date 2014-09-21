package ir.ac.ut.analyzer;

import org.xml.sax.helpers.DefaultHandler;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

public class Parser {

    private DefaultHandler handler;
    private SAXParser saxParser;


    public Parser(DefaultHandler handler) {
        this.handler = handler;
        create();
    }


    private void create() {
        try {
            // Obtain a new instance of a SAXParserFactory.
            SAXParserFactory factory = SAXParserFactory.newInstance();
            // Specifies that the parser produced by this code will provide support for XML namespaces.
            factory.setNamespaceAware(true);
            // Specifies that the parser produced by this code will validate documents as they are parsed.
            factory.setValidating(true);
            // Creates a new instance of a SAXParser using the currently configured factory parameters.
            saxParser = factory.newSAXParser();
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }


    public void parse(File file){
        try{
            saxParser.parse(file,handler);
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    public void parse(String uri) throws IOException{
        try{
            saxParser.parse(uri,handler);
        } catch (Throwable t) {
//        System.err.println(uri);
            t.printStackTrace();
        }
    }
    public void parse(InputStream stream){
        try{
            saxParser.parse(stream,handler);
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }
}