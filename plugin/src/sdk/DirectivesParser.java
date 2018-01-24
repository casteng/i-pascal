package com.siberika.idea.pascal.sdk;

import com.intellij.util.SmartList;
import org.jetbrains.annotations.NotNull;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
* Author: George Bakhtadze
* Date: 31/08/2013
*/
class DirectivesParser {

    static Map<String, Map<String, Directive>> parse(@NotNull InputStream stream) {
        try {
            SAXParserFactory factory = SAXParserFactory.newInstance();
            SAXParser saxParser = factory.newSAXParser();
            final Map<String, Map<String, Directive>> directives = new TreeMap<String, Map<String, Directive>>();

            DefaultHandler handler = new DefaultHandler() {
                private String version = null;
                private StringBuilder sb = new StringBuilder();
                private Directive directive = new Directive();
                private List<String> ids = new SmartList<String>();

                public void startElement(String uri, String localName,String qName, Attributes attributes) throws SAXException {
                    if (qName.equalsIgnoreCase("compiler")) {
                        version = attributes.getValue("version");
                    }
                    sb = new StringBuilder();
                }

                public void endElement(String uri, String localName, String qName) throws SAXException {
                    if (qName.equalsIgnoreCase("directive")) {
                        addDirective(directives, version, ids, directive);
                        directive = new Directive();
                        ids = new SmartList<String>();
                    } else if (qName.equalsIgnoreCase("id")) {
                        ids.add(sb.toString());
                    } else if (qName.equalsIgnoreCase("desc")) {
                        directive.desc = sb.toString();
                    } else if (qName.equalsIgnoreCase("value")) {
                        directive.addValue(sb.toString());
                    }
                }

                public void characters(char ch[], int start, int length) throws SAXException {
                    sb.append(ch, start, length);
                }

            };

            saxParser.parse(stream, handler);
            return directives;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private static void addDirective(Map<String, Map<String, Directive>> directives, String version, List<String> ids, Directive directive) {
        Map<String, Directive> dirs = directives.get(version);
        if (null == dirs) {
            dirs = new HashMap<String, Directive>();
        }
        for (String id : ids) {
            dirs.put(id.toUpperCase(), directive);
        }
        directives.put(version, dirs);
    }

}
