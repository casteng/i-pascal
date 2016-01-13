package com.siberika.idea.pascal.sdk;

import org.jetbrains.annotations.NotNull;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.InputStream;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

/**
* Author: George Bakhtadze
* Date: 31/08/2013
*/
public class DefinesParser {

    private static Map<String, Map<String, Set<String>>> defaultDefines = new TreeMap<String, Map<String, Set<String>>>();

    static void parse(@NotNull InputStream stream) {
        try {
            SAXParserFactory factory = SAXParserFactory.newInstance();
            SAXParser saxParser = factory.newSAXParser();


            DefaultHandler handler = new DefaultHandler() {
                private String compiler = null;
                private String version = null;
                private StringBuilder sb = new StringBuilder();

                public void startElement(String uri, String localName,String qName, Attributes attributes) throws SAXException {
                    if (qName.equalsIgnoreCase("compiler")) {
                        compiler = attributes.getValue("name");
                        version = attributes.getValue("version");
                    }
                    sb = new StringBuilder();
                }

                public void endElement(String uri, String localName, String qName) throws SAXException {
                    if (qName.equalsIgnoreCase("define")) {
                        addDefine(compiler, version, sb.toString());
                    }
                }

                public void characters(char ch[], int start, int length) throws SAXException {
                    sb.append(ch, start, length);
                }

            };

            saxParser.parse(stream, handler);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void addDefine(String compiler, String version, String name) {
        Map<String, Set<String>> compilerDefines = defaultDefines.get(compiler);
        if (null == compilerDefines) {
            compilerDefines = new TreeMap<String, Set<String>>();
            defaultDefines.put(compiler, compilerDefines);
        }
        Set<String> defines = compilerDefines.get(version);
        if (null == defines) {
            defines = new HashSet<String>();
        }
        defines.add(name);
        compilerDefines.put(version, defines);
    }

    public static Set<String> getDefaultDefines(String compiler, String version) {
        Map<String, Set<String>> compilerDefines = defaultDefines.get(compiler);
        if (null == compilerDefines) {
            return Collections.emptySet();
        }
        Set<String> result = new HashSet<String>();
        for (Map.Entry<String, Set<String>> entry : compilerDefines.entrySet()) {
            if (isVersionLessOrEqual(entry.getKey(), version)) {
                result.addAll(entry.getValue());
            }
        }
        return result;
    }

    private static boolean isVersionLessOrEqual(String version1, String version2) {
        return version1.compareTo(version2) <= 0;
    }

}
