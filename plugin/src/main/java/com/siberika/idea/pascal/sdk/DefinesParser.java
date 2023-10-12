package com.siberika.idea.pascal.sdk;

import org.jetbrains.annotations.NotNull;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

/**
* Author: George Bakhtadze
* Date: 31/08/2013
*/
public class DefinesParser {

    static Map<String, Map<String, Define>> parse(@NotNull InputStream stream) {
        try {
            SAXParserFactory factory = SAXParserFactory.newInstance();
            SAXParser saxParser = factory.newSAXParser();

            final Map<String, Map<String, Define>> defines = new TreeMap<String, Map<String, Define>>();

            DefaultHandler handler = new DefaultHandler() {
                private String version = null;
                private StringBuilder sb = new StringBuilder();

                public void startElement(String uri, String localName,String qName, Attributes attributes) throws SAXException {
                    if (qName.equalsIgnoreCase("compiler")) {
                        version = attributes.getValue("version");
                    }
                    sb = new StringBuilder();
                }

                public void endElement(String uri, String localName, String qName) throws SAXException {
                    if (qName.equalsIgnoreCase("define")) {
                        addDefine(defines, version, sb.toString());
                    }
                }

                public void characters(char ch[], int start, int length) throws SAXException {
                    sb.append(ch, start, length);
                }

            };

            saxParser.parse(stream, handler);
            return defines;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private static void addDefine(Map<String, Map<String, Define>> compilerDefines, String version, String name) {
        Map<String, Define> defines = compilerDefines.get(version);
        if (null == defines) {
            defines = new HashMap<String, Define>();
        }
        defines.put(name.toUpperCase(), new Define(name, null, -1));
        compilerDefines.put(version, defines);
    }

}
