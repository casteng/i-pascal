package com.siberika.idea.pascal.module;

import com.intellij.openapi.diagnostic.Logger;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.File;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class LpiParser {

    private static final Logger LOG = Logger.getInstance(LpiParser.class);

    private static final Pattern PATTERN_UNITS = Pattern.compile("(?i)Unit(\\d+)");

    static ProjectData parse(File file) {
        ProjectData result = new ProjectData();
        try {
            SAXParserFactory factory = SAXParserFactory.newInstance();
            SAXParser saxParser = factory.newSAXParser();

            DefaultHandler handler = new DefaultHandler() {

                private String pathDelim;
                private String mainUnitIndex;
                private String filename;

                public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
                    if (qName.equalsIgnoreCase("Title")) {
                        result.setName(attributes.getValue("Value"));
                    } else if (qName.equalsIgnoreCase("PathDelim")) {
                        pathDelim = attributes.getValue("Value");
                    } else if (qName.equalsIgnoreCase("MainUnit")) {
                        mainUnitIndex = attributes.getValue("Value");
                    } else if (qName.equalsIgnoreCase("Filename")) {
                        filename = attributes.getValue("Value").replace(pathDelim, File.separator);
                    } else if (qName.equalsIgnoreCase("IncludeFiles")) {
                        result.setIncludeFilesPath(attributes.getValue("Value"));
                    } else if (qName.equalsIgnoreCase("OtherUnitFiles")) {
                        result.setOtherUnitFilesPath(attributes.getValue("Value"));
                    } else if (qName.equalsIgnoreCase("UnitOutputDirectory")) {
                        result.setUnitOutputDirectory(attributes.getValue("Value"));
                    }
                }

                public void endElement(String uri, String localName, String qName) throws SAXException {
                    Matcher m = PATTERN_UNITS.matcher(qName);
                    if (m.matches()) {
                        if (m.group(1).equals(mainUnitIndex)) {
                            result.setMainFile(PascalProjectStructureDetector.getMainFile(file.getParentFile(), filename));
                        }
                        if (filename != null) {
                            result.addUnitPath(filename);
                            filename = null;
                        }
                    }
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
