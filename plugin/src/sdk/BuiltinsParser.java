package com.siberika.idea.pascal.sdk;

import com.siberika.idea.pascal.lang.psi.impl.PasField;
import org.jetbrains.annotations.NotNull;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.InputStream;
import java.util.Collection;
import java.util.HashSet;

/**
* Author: George Bakhtadze
* Date: 03/10/2013
*/
public class BuiltinsParser {

    private static Collection<PasField> builtins = new HashSet<PasField>();

    static void parse(@NotNull InputStream resource) {
        try {
            SAXParserFactory factory = SAXParserFactory.newInstance();
            SAXParser saxParser = factory.newSAXParser();


            DefaultHandler handler = new DefaultHandler() {
                private StringBuilder sb = new StringBuilder();
                private PasField.ValueType valueType = null;

                public void startElement(String uri, String localName,String qName, Attributes attributes) throws SAXException {
                    sb = new StringBuilder();
                    valueType = getValueType(attributes.getValue("type"));
                }

                public void endElement(String uri, String localName, String qName) throws SAXException {
                    if (qName.equalsIgnoreCase("type")) {
                        builtins.add(new PasField(null, null, sb.toString(), PasField.FieldType.TYPE, PasField.Visibility.PUBLIC, valueType));
                    } else if (qName.equalsIgnoreCase("var")) {
                        builtins.add(new PasField(null, null, sb.toString(), PasField.FieldType.VARIABLE, PasField.Visibility.PUBLIC, valueType));
                    } else if (qName.equalsIgnoreCase("routine")) {
                        builtins.add(new PasField(null, null, sb.toString(), PasField.FieldType.ROUTINE, PasField.Visibility.PUBLIC, valueType));
                    }
                }

                public void characters(char ch[], int start, int length) throws SAXException {
                    sb.append(ch, start, length);
                }

            };

            saxParser.parse(resource, handler);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static PasField.ValueType getValueType(String name) {
        PasField.Kind kind = getKindByName(name);
        if (null == kind) {
            return null;
        }
        switch (kind) {
            case BOOLEAN:
                return PasField.BOOLEAN;
            case INTEGER:
                return PasField.INTEGER;
            case FLOAT:
                return PasField.FLOAT;
            case POINTER:
                return PasField.POINTER;
            case STRING:
                return PasField.STRING;
            default:
                return null;
        }
    }

    private static PasField.Kind getKindByName(String value) {
        if (null == value) {
            return null;
        }
        for (PasField.Kind kind : PasField.Kind.values()) {
            if (value.equalsIgnoreCase(kind.name())) {
                return kind;
            }
        }
        return null;
    }

    public static Collection<PasField> getBuiltins() {
        return builtins;
    }
}
