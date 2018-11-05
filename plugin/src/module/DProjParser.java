package com.siberika.idea.pascal.module;

import com.intellij.openapi.diagnostic.Logger;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.File;

class DProjParser {

    private static final Logger LOG = Logger.getInstance(DProjParser.class);

    static ProjectData parse(File file) {
        ProjectData result = new ProjectData();
        try {
            SAXParserFactory factory = SAXParserFactory.newInstance();
            SAXParser saxParser = factory.newSAXParser();

            DefaultHandler handler = new DefaultHandler() {

                private StringBuilder sb = new StringBuilder();

                public void startElement(String uri, String localName,String qName, Attributes attributes) throws SAXException {
                    sb = new StringBuilder();
                }

                public void endElement(String uri, String localName, String qName) throws SAXException {
                    if (qName.equalsIgnoreCase("MainSource")) {
                        result.setMainFile(PascalProjectStructureDetector.getMainFile(file.getParentFile(), sb.toString()));
                    } else if (qName.equalsIgnoreCase("SanitizedProjectName")) {
                        result.setName(sb.toString());
                    } else if (qName.equalsIgnoreCase("DCC_UnitSearchPath")) {
                        for (String path : sb.toString().split(";")) {
                            if (!path.startsWith("$")) {
                                result.addUnitPath(path);
                            }
                        }
                    }
                }

                public void characters(char ch[], int start, int length) throws SAXException {
                    sb.append(ch, start, length);
                }

            };

            saxParser.parse(file, handler);
            return result;
        } catch (Exception e) {
            LOG.error("Error parsing file: " + file.getPath(), e);
            return null;
        }
    }

}
