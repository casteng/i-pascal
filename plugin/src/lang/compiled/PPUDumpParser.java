package com.siberika.idea.pascal.lang.compiled;

import com.intellij.openapi.diagnostic.Logger;
import com.siberika.idea.pascal.PascalBundle;
import org.apache.commons.lang.StringUtils;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

/**
 * Author: George Bakhtadze
 * Date: 20/11/2013
 */
public class PPUDumpParser {

    private static final Logger LOG = Logger.getInstance(PPUDumpParser.class);

    public static String parse(InputStream inputStream) throws ParseException, ParserConfigurationException, SAXException, IOException {
        SAXParserFactory factory = SAXParserFactory.newInstance();
        SAXParser parser = factory.newSAXParser();
        XMLHandler handler = new XMLHandler();
        parser.parse(inputStream, handler);
        return handler.result.toString();
    }

    public static String parse(String xml) throws ParseException, ParserConfigurationException, SAXException, IOException {
        return parse(new ByteArrayInputStream(xml.getBytes("utf-8")));
    }

    private static class XMLHandler extends DefaultHandler {
        public static final Set<String> TYPES = new HashSet<String>(Arrays.asList("/ord", "/ptr", "/string", "/float", "/type", "/file", "/variant"));
        public static final Set<String> DIRECTIVES = new HashSet<String>(Arrays.asList(
                "overload", "virtual", "dynamic", "static", "override",
                "abstract", "final",
                "inline", "assembler,",
                "cdecl", "pascal", "register", "safecall", "stdcall", "export"));
        public static final Set<String> DATA = new HashSet<String>(Arrays.asList("ptr/ptr", "eltype", "rangetype", "vartype", "rettype"));
        public static final Set<String> COMMENTS = new HashSet<String>(Arrays.asList("version", "targetcpu", "targetos", "crc", "interfacecrc"));
        public static final Set<String> IGNORED = new HashSet<String>(Arrays.asList("1"));

        public static final Map<String, String> NAME_SUB = new HashMap<String, String>();

        public static final Map<String, Section> SECTIONS = new LinkedHashMap<String, Section>();

        static {
            addSection("/unit/uses/unit", "  ", "", "", ",\n", 0);
            addSection("/unit/files/file", "", "", "", ", ", 0);

            addSection("/enum", "type ", " = (", "", ");\n\n", 0);
            addSection("/elements", "", "", "", "", 2);
            addSection("/elements/const", "", " = ", "", ", ", 0);

            addSection("/unit", "unit ", ";", "\ninterface\n", "", 0);
            addSection("/units", "", "", "", "", 0).ignore = true;
            addSection("/uses", "uses \n", "", "", ";\n", 2);
            addSection("/files", "{" + PascalBundle.message("decompiled.unit.files") + " ", "", "", "}\n", 2);
            addSection("/interface", "", "", "", "implementation\n" + PascalBundle.message("decompiled.unit.footer") + "\nend.", 0);

            addSection("/fields/rec", "record\n", "", "", "end", 0);
            addSection("/fields/proctype", "procedure", "", "", "", 0);

            addSection("/rec", "type ", " = record\n", "", "end;\n\n", 0);
            addSection("/fields", "", "", "", "", 0);
            addSection("/field", "  ", ": ", "", ";\n", 0);

            addSection("/options", "", "", "", "", 0).ignore = true;
            addSection("/undefined", "", "", "", "", 0).ignore = true;
            addSection("/formal", "", "", "", "", 0).ignore = true;

            addSection("/proc", "", "", "", ";\n\n", 0);
            addSection("/proctype", "", null, "", ";\n\n", 0);
            addSection("prop/params", "[", "", "", "]", 2);
            addSection("/params", "(", "", "", ")", 2);
            addSection("/param", "", "", "", "; ", 0);

            addSection("/const", "const ", " = ", "", ";\n\n", 0);
            addSection("/var", "var ", ": ", "", ";\n\n", 0);

            addSection("/array", "type ", " = array", "", ";\n\n", 0);

            addSection("/obj", "", null, "", "end;\n\n", 0);

            addSection("/classref", "type ", " = class of ", "", ";\n\n", 0);

            addSection("/prop", "property ", "", "", ";\n\n", 0);

            for (String name : TYPES) {
                addSection(name, "type ", " = ", "", ";\n\n", 0);
            }

            NAME_SUB.put("$assign",   ":= ");
            NAME_SUB.put("$or",       "or ");
            NAME_SUB.put("$and",      "and ");
            NAME_SUB.put("$xor",      "xor ");
            NAME_SUB.put("$not",      "not ");
            NAME_SUB.put("$shl",      "shl ");
            NAME_SUB.put("$shr",      "shr ");
            NAME_SUB.put("$plus",     "+ ");
            NAME_SUB.put("$minus",    "- ");
            NAME_SUB.put("$star",     "* ");
            NAME_SUB.put("$slash",    "/ ");
            NAME_SUB.put("$starstar", "** ");
            NAME_SUB.put("$div",      "div ");
            NAME_SUB.put("$mod",      "mod ");
            NAME_SUB.put("$equal",    "= ");
            NAME_SUB.put("$lower",    "< ");
            NAME_SUB.put("$greater",  "> ");
            NAME_SUB.put("$greater_or_equal", ">= ");
            NAME_SUB.put("$lower_or_equal",   "<= ");
        }

        private static Section addSection(String id, String textBegin, String afterName, String beforeSubsec, String textEnd, int removeChars) {
            Section res = new Section(id, textBegin, afterName, beforeSubsec, textEnd, removeChars);
            SECTIONS.put(id, res);
            return res;
        }

        public final Map<String, String> idNameMap = new HashMap<String, String>();
        public final Map<String, String> symidNameMap = new HashMap<String, String>();
        public final List<String> units = new ArrayList<String>();

        private Deque<Section> stack = new ArrayDeque<Section>();
        private StringBuilder chars = new StringBuilder();
        private StringBuilder result = new StringBuilder(PascalBundle.message("decompiled.unit.header"));
        private String path = "";

        @Override
        public void characters(char[] ch, int start, int length) throws SAXException {
            chars.append(ch, start, length);
        }

        private Section getSection(String qName) {
            if (isIgnored(path)) {
                return null;
            }
            for (String dpath : SECTIONS.keySet()) {
                if (path.endsWith(dpath)) {
                    return SECTIONS.get(dpath);
                }
            }
            return null;
        }

        private boolean isIgnored(String path) {
            for (String str : IGNORED) {
                if (str.startsWith("/")) {
                    if (str.equalsIgnoreCase(path)) {
                        return true;
                    }
                } else {
                    if (path.endsWith(str)) {
                        return true;
                    }
                }
            }
            return IGNORED.contains(path);
        }

        private boolean sectionBegin(Section sec) {
            if (null == sec) {
                return false;
            }
            sec = sec.copy();

            if (!stack.isEmpty()) {
                stack.getFirst().appendBeforeSubsec();
            }

            sec.reset();
            stack.push(sec);
            sec.appendBegin();
            return true;
        }

        private void sectionEnd(Section sec) {
            if (null == sec) {
                return;
            }
            assert sec.type.equals(stack.getFirst().type);
            sec = stack.pop();

            sec.doRemoveChars();

            handleSectionEnd(sec);

            sec.sb.append(sec.textEnd);

            if (!sec.ignore) {
                if (!StringUtils.isBlank(sec.name) ||
                        (StringUtils.isBlank(sec.getDataStr("id")) && StringUtils.isBlank(sec.getDataStr("symid")))) {
                    if (stack.isEmpty()) {
                        result.append(sec.sb);
                    } else {
                        stack.getFirst().sb.append(sec.sb);
                    }
                } else {
                    if (!StringUtils.isBlank(sec.getDataStr("id"))) {
                        idNameMap.put(sec.getDataStr("id"), sec.sb.toString());
                    }
                    if (!StringUtils.isBlank(sec.getDataStr("symid"))) {
                        symidNameMap.put(sec.getDataStr("symid"), sec.sb.toString());
                    }
                }
            } else if (LOG.isDebugEnabled()) {
                LOG.debug("Section ignored: " + sec);
                System.out.println("Section ignored: " + sec); //===***
            }
        }

        private void handleSectionEnd(Section sec) {
            if (TYPES.contains(sec.type)) {
                if ("/ord".equalsIgnoreCase(sec.type)) {
                    if (sec.getDataStr("ordtype").endsWith("ool")) {
                        sec.sb.append("false..true");
                    } else if (sec.getDataStr("ordtype").endsWith("int")) {
                        sec.sb.append(sec.getDataStr("low")).append("..").append(sec.getDataStr("high"));
                    } else {
                        sec.sb.append(sec.name);
                    }
                } else if ("/ptr".equalsIgnoreCase(sec.type)) {
                    sec.sb.append("^");
                    sec.sb.append(retrieveReference(sec.data.get("ptr/unit"), sec.data.get("ptr/id"), sec.data.get("ptr/symid"), sec.name));
                } else {
                    sec.sb.append(sec.name);
                }
            } else if ("/array".equalsIgnoreCase(sec.type)) {
                if (!hasOption(sec, "dynamic")) {
                    if (allDataPresent(sec, "low", "high")) {
                        sec.sb.append("[").append(sec.getDataStr("low")).append("..").append(sec.getDataStr("high")).append("]");
                    }
                }
                sec.sb.append(" of ");
                sec.sb.append(retrieveReference(sec.data.get("eltype/unit"), sec.data.get("eltype/id"), sec.data.get("eltype/symid"), sec.name));
            } else if ("/options".equalsIgnoreCase(sec.type)) {
                if (!stack.isEmpty()) {
                    stack.getFirst().data.putAll(sec.data);
                }
            } else if ("/param".equalsIgnoreCase(sec.type)) {
                sec.sb.insert(0, sec.getDataStr("spez") + " ");
                String typeStr = retrieveReference(sec.data.get("vartype/unit"), sec.data.get("vartype/id"), sec.data.get("vartype/symid"), "");
                if (!StringUtils.isBlank(typeStr)) {
                    sec.sb.append(": " + typeStr);
                }
            } else if ("/field".equalsIgnoreCase(sec.type)) {
                sec.insertVisibility();
                sec.sb.append(retrieveReference(sec.data.get("vartype/unit"), sec.data.get("vartype/id"), sec.data.get("vartype/symid"), "__unknown__"));
            } else if ("/var".equalsIgnoreCase(sec.type)) {
                sec.sb.append(retrieveReference(sec.data.get("vartype/unit"), sec.data.get("vartype/id"), sec.data.get("vartype/symid"), "__unknown__"));
            } else if ("/proc".equalsIgnoreCase(sec.type)) {
                if (hasOption(sec, "procedure")) {
                    sec.sb.insert(0, "procedure ");
                } else if (hasOption(sec, "constructor")) {
                    sec.sb.insert(0, "constructor ");
                } else if (hasOption(sec, "destructor")) {
                    sec.sb.insert(0, "destructor ");
                } else if (hasOption(sec, "operator")) {
                    sec.sb.insert(0, "operator ");
                    sec.sb.append(" dest: ").append(retrieveReference(sec.data.get("rettype/unit"), sec.data.get("rettype/id"), sec.data.get("rettype/symid"), "__Unknown__"));
                } else if (hasOption(sec, "function")) {
                    sec.sb.insert(0, "function ");
                    sec.sb.append(": ").append(retrieveReference(sec.data.get("rettype/unit"), sec.data.get("rettype/id"), sec.data.get("rettype/symid"), "__Unknown__"));
                }
                if (hasOption(sec, "classmethod")) {
                    sec.sb.insert(0, "class ");
                }
                for (String directive : DIRECTIVES) {
                    if (hasOption(sec, directive)) {
                        sec.sb.append("; " + directive);
                    }
                }
                sec.insertVisibility();
            } else if ("/obj".equalsIgnoreCase(sec.type)) {
                StringBuilder psb = new StringBuilder("type ");
                psb.append(sec.name).append(" = ").append(sec.getDataStr("objtype"));
                String ancStr = retrieveReference(sec.data.get("ancestor/unit"), sec.data.get("ancestor/id"), sec.data.get("ancestor/symid"), sec.name);
                if (!StringUtils.isBlank(ancStr)) {
                    psb.append("(").append(ancStr).append(")");
                }
                psb.append("\n");
                if (!StringUtils.isBlank(sec.getDataStr("iid"))) {
                    psb.append("['").append(sec.getDataStr("iid")).append("']").append("\n");
                }
                sec.sb.insert(0, psb);
            } else if ("/classref".equalsIgnoreCase(sec.type)) {
                sec.sb.append(retrieveReference(sec.data.get("ref/unit"), sec.data.get("ref/id"), sec.data.get("ref/symid"), "__TUnknown__"));
            } else if ("/proctype".equalsIgnoreCase(sec.type)) {
                String retType = retrieveReference(sec.data.get("rettype/unit"), sec.data.get("rettype/id"), sec.data.get("rettype/symid"), "");
                if (StringUtils.isBlank(retType)) {
                    sec.sb.insert(0, "procedure ");
                } else {
                    sec.sb.insert(0, "function ");
                    sec.sb.append(": ").append(retType);
                }
                if (sec.name != null) {
                    sec.sb.insert(0, "type " + sec.name + " = ");
                }
                if ("1".equals(sec.getDataStr("methodptr"))) {
                    sec.sb.append(" of object");
                }
            } else if ("/prop".equalsIgnoreCase(sec.type)) {
                sec.sb.append(": ").append(retrieveReference(sec.data.get("proptype/unit"), sec.data.get("proptype/id"), sec.data.get("proptype/symid"), "__TUnknown__"));
                appendIfAllNotBlank(sec.sb, " read ", retrieveReference(null, sec.data.get("getter/id"), sec.data.get("getter/symid"), ""));
                appendIfAllNotBlank(sec.sb, " write ", retrieveReference(null, sec.data.get("setter/id"), sec.data.get("setter/symid"), ""));
                sec.insertVisibility();
            }
        }

        private void appendIfAllNotBlank(StringBuilder sb, String...args) {
            for (String arg : args) {
                if (StringUtils.isBlank(arg)) {
                    return;
                }
            }
            for (String arg : args) {
                sb.append(arg);
            }
        }

        private boolean hasOption(Section sec, String option) {
            return sec.data.containsKey("/options/" + option);
        }

        private boolean allDataPresent(Section sec, String...keys) {
            for (String key : keys) {
                if (null == sec.getDataStr(key)) {
                    return false;
                }
            }
            return true;
        }

        private String retrieveReference(Object unit, Object id, Object symid, String defalut) {
            String res = "";
            if (unit != null) {
                int ind = Integer.parseInt((String) unit);
                res = units.get(ind) + ".";
            }
            Map<String, String> nameMap = idNameMap;
            if (null == id) {
                id = symid;
                nameMap = symidNameMap;
            }
            if (id != null) {
                if (unit != null) {
                    res = res + "__" + id;
                } else {
                    String ref = nameMap.get(id);
                    if (StringUtils.isBlank(ref) || (ref.startsWith("$"))) {
                        res = defalut;
                    } else {
                        res = res + ref;
                    }

                }
            } else {
                LOG.warn("retrieveReference: id is null");
            }
            return res;
        }

        private boolean handleDataTag(String qname, boolean start) {
            if (stack.isEmpty()) { return false; }
            Section sec = stack.getFirst();
            if (sec.dataPrefix != null) {
                if (start) {
                    sec.appendDataPrefix(qname);
                } else {
                    sec.removeDataPrefix(qname);
                }
                return true;
            }
            for (String dpath : DATA) {
                if (path.endsWith(dpath)) {
                    if (start) {
                        sec.dataPrefix = dpath.substring(dpath.indexOf("/") + 1) + "/";
                    } else {
                        sec.dataPrefix = null;
                    }
                    return true;
                }
            }
            return false;
        }

        @Override
        public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
            path = path + "/" + qName;
            chars = new StringBuilder();
            if (!handleDataTag(qName, true)) {
                if (!sectionBegin(getSection(qName)) && !stack.isEmpty()) {
                    stack.getFirst().appendDataPrefix(qName);
                }
            }
        }

        @Override
        public void endElement(String uri, String localName, String qName) throws SAXException {
            if (!handleDataTag(qName, false)) {
                sectionEnd(getSection(qName));
            }
            String txt = chars.toString().trim();
            if ((txt.length() > 0) && (!isIgnored(path)) && !stack.isEmpty()) {
                Section sec = stack.getFirst();
                if (COMMENTS.contains(qName)) {
                    sec.sb.append(" {").append(qName).append(":").append(txt).append("}");
                } else if ("name".equalsIgnoreCase(qName)) {
                    sec.name = NAME_SUB.get(txt.toString());
                    sec.name = sec.name != null ? sec.name : txt.toString();
                    if (!path.endsWith(sec.type + "/name")) {
                        LOG.warn("! name for section: " + sec.type + ", path: " + path);
                        System.out.println("! name for section: " + sec.type + ", path: " + path);//===***
                    }
                    if (sec.name.startsWith("$")) {
                        sec.ignore = true;
                    }
                    if (sec.textAfterName != null) {
                        sec.sb.append(sec.name);
                        sec.sb.append(sec.textAfterName);
                    }
                } else if ("id".equalsIgnoreCase(qName) && (sec.name != null)) {
                    if (null == sec.dataPrefix) {
                        idNameMap.put(txt.toString(), sec.name);
                    }
                } else if ("symid".equalsIgnoreCase(qName) && (sec.name != null)) {
                    if (null == sec.dataPrefix) {
                        symidNameMap.put(txt.toString(), sec.name);
                    }
                } else if ("value".equalsIgnoreCase(qName)) {
                    if ("/units".equalsIgnoreCase(sec.type)) {
                        units.add(txt);
                    } else if ("/options".equalsIgnoreCase(sec.type)) {
                        sec.putData(sec.type + "/" + txt, "");
                    }
                    if (null == sec.dataPrefix) {
                        sec.sb.append(txt);
                    }
                }
                if (txt.length() > 0) {
                    sec.putData(qName, txt);
                }
            }
            chars = new StringBuilder();
            path = path.substring(0, path.lastIndexOf("/"));
        }

    }

    private static class Section {
        final String type;
        final String textBegin;
        final String textAfterName;                   // if null name is not printed
        final String beforeSubsec;
        final String textEnd;
        final int removeChars;

        Map<String, Object> data = new TreeMap<String, Object>();
        StringBuilder sb = new StringBuilder();

        String name;
        private boolean ignore = false;
        private String dataPrefix = null;

        private boolean beforeSubsecAdded = false;

        private Section(String type, String textBegin, String textAfterName, String beforeSubsec, String textEnd, int removeChars) {
            this.type = type;
            this.textBegin = textBegin;
            this.textAfterName = textAfterName;
            this.beforeSubsec = beforeSubsec;
            this.textEnd = textEnd;
            this.removeChars = removeChars;
        }

        void reset() {
            data = new TreeMap<String, Object>();
            sb = new StringBuilder();
        }

        public void appendBeforeSubsec() {
            if (beforeSubsecAdded) { return; }
            sb.append(beforeSubsec);
            beforeSubsecAdded = true;
        }

        public void doRemoveChars() {
            sb.delete(sb.length() - removeChars, sb.length());
        }

        Section copy() {
            Section res = new Section(type, textBegin, textAfterName, beforeSubsec, textEnd, removeChars);
            res.ignore = ignore;
            res.dataPrefix = dataPrefix;
            return res;
        }

        public void appendBegin() {
            sb.append(textBegin);
            for (int i = 0; i < removeChars; i++) {
                sb.append(" ");
            }
        }

        public void putData(String name, Object obj) {
            data.put((dataPrefix != null ? dataPrefix : "") + name, obj);
        }

        public String getDataStr(String name) {
            Object res = data.get(name);
            return res != null ? (String) res : "";
        }

        public void appendDataPrefix(String qName) {
            if (dataPrefix != null) {
                dataPrefix = dataPrefix + qName + "/";
            } else {
                dataPrefix = qName + "/";
            }
        }

        public void removeDataPrefix(String qname) {
            assert (dataPrefix.endsWith(qname + "/"));
            dataPrefix = dataPrefix.substring(0, dataPrefix.length() - (qname.length() + 1));
            if (dataPrefix.length() == 0) {
                dataPrefix = null;
            }
        }

        @Override
        public String toString() {
            return "Section{" +
                    "type='" + type + '\'' +
                    "name='" + name + '\'' +
                    ", ignore=" + ignore +
                    ", dataPrefix='" + dataPrefix + '\'' +
                    ", sb=" + sb +
                    '}';
        }

        public void insertVisibility() {
            sb.insert(0, StringUtils.isBlank(getDataStr("visibility")) ? "public " : getDataStr("visibility") + " ");
        }
    }

    /*private static class PascalBundle {
        public static String message(String s) {
            return s + "\n";
        }
    }*/
}
