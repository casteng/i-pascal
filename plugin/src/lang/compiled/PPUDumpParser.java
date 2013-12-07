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
import java.util.Comparator;
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

    public static Section parse(InputStream inputStream, PPUDecompilerCache cache) throws ParseException, ParserConfigurationException, SAXException, IOException {
        SAXParserFactory factory = SAXParserFactory.newInstance();
        SAXParser parser = factory.newSAXParser();
        XMLHandler handler = new XMLHandler(cache);
        parser.parse(inputStream, handler);
        return handler.result;
    }

    public static Section parse(String xml, PPUDecompilerCache cache) throws ParseException, ParserConfigurationException, SAXException, IOException {
        return parse(new ByteArrayInputStream(xml.getBytes("utf-8")), cache);
    }

    private static class XMLHandler extends DefaultHandler {
        public static final Set<String> TYPES = new HashSet<String>(Arrays.asList("/ord", "/ptr", "/string", "/float", "/type", "/file", "/variant", "/set"));
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

        private final PPUDecompilerCache cache;

        private XMLHandler(PPUDecompilerCache cache) {
            this.cache = cache;
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
        private Section result = null;
        private String path = "";

        @Override
        public void characters(char[] ch, int start, int length) throws SAXException {
            chars.append(ch, start, length);
        }

        private Section getSection() {
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
                        result = sec;
                        fixupUndefined(sec);
                        result.idNameMap = idNameMap;
                        result.symidNameMap = symidNameMap;
                    } else {
                        stack.getFirst().merge(sec);
                    }
                } else {
                    if (!StringUtils.isBlank(sec.getDataStr("id"))) {
                        idNameMap.put(sec.getDataStr("id"), sec.sb.toString());
                    }
                    if (!StringUtils.isBlank(sec.getDataStr("symid"))) {
                        symidNameMap.put(sec.getDataStr("symid"), sec.sb.toString());
                    }
                }
            } else {//if (LOG.isDebugEnabled()) {
                //LOG.debug("Section ignored: " + sec);
                //System.out.println("Section ignored: " + sec); //===***
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
                    appendReference(sec, "ptr", "", "", sec.name);
                } else if ("/set".equalsIgnoreCase(sec.type)) {
                    sec.sb.append("set");
                    appendReference(sec, "eltype", " of ", "", sec.name);
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
                appendReference(sec, "eltype", "", "", "__INTERNAL__");
            } else if ("/options".equalsIgnoreCase(sec.type)) {
                if (!stack.isEmpty()) {
                    stack.getFirst().data.putAll(sec.data);
                }
            } else if ("/param".equalsIgnoreCase(sec.type)) {
                sec.insertText(0, sec.getDataStr("spez") + " ");
                appendReference(sec, "vartype", ": ", "", "__INTERNAL__");
            } else if ("/field".equalsIgnoreCase(sec.type)) {
                sec.insertVisibility();
                appendReference(sec, "vartype", "", "", "__INTERNAL__");
            } else if ("/var".equalsIgnoreCase(sec.type)) {
                appendReference(sec, "vartype", "", "", "__INTERNAL__");
            } else if ("/proc".equalsIgnoreCase(sec.type)) {
                if (hasOption(sec, "procedure")) {
                    sec.insertText(0, "procedure ");
                } else if (hasOption(sec, "constructor")) {
                    sec.insertText(0, "constructor ");
                } else if (hasOption(sec, "destructor")) {
                    sec.insertText(0, "destructor ");
                } else if (hasOption(sec, "operator")) {
                    sec.insertText(0, "operator ");
                    appendReference(sec, "rettype", "", "", "__INTERNAL__");
                } else if (hasOption(sec, "function")) {
                    sec.insertText(0, "function ");
                    sec.sb.append(": ");
                    appendReference(sec, "rettype", "", "", "__INTERNAL__");
                }
                if (hasOption(sec, "classmethod")) {
                    sec.insertText(0, "class ");
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
                appendReference(sec, "ancestor", "(", ")", "__INTERNAL__");
                psb.append("\n");
                if (!StringUtils.isBlank(sec.getDataStr("iid"))) {
                    psb.append("['").append(sec.getDataStr("iid")).append("']").append("\n");
                }
                sec.insertText(0, psb.toString());
            } else if ("/classref".equalsIgnoreCase(sec.type)) {
                appendReference(sec, "ref", "", "", "__INTERNAL__");
            } else if ("/proctype".equalsIgnoreCase(sec.type)) {
                if (StringUtils.isBlank(sec.getDataStr("rettype/id"))) {
                    sec.insertText(0, "procedure ");
                } else {
                    sec.insertText(0, "function ");
                    sec.sb.append(": ");
                    appendReference(sec, "rettype", "", "", "");
                }
                if (sec.name != null) {
                    sec.insertText(0, "type " + sec.name + " = ");
                }
                if ("1".equals(sec.getDataStr("methodptr"))) {
                    sec.sb.append(" of object");
                }
            } else if ("/prop".equalsIgnoreCase(sec.type)) {
                sec.sb.append(": ");
                appendReference(sec, "proptype", "", "", "__INTERNAL__");
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

        /**
         * prefix and default are mutually exclusive
         */
        private void appendReference(Section sec, String refName, String prefix, String postfix, String def) {
            Object unit = sec.data.get(refName + "/unit");
            if (unit != null) {
                sec.sb.append(prefix);
                resolveUsed(sec.sb, sec.data.get(refName + "/id"), sec.data.get(refName + "/symid"), Integer.parseInt((String) unit));
                sec.sb.append(postfix);
            } else {
                appendLocalReference(sec.sb, sec.data.get(refName + "/id"), sec.data.get(refName + "/symid"), prefix, postfix, def,
                        idNameMap, symidNameMap, sec.undefined);
            }
        }

        private void appendLocalReference(StringBuilder sb, Object id, Object symid, String prefix, String postfix, String def,
                                          Map<String, String> idNameMap, Map<String, String> symidNameMap, Map<Integer, String> undefined) {
            Map<String, String> nameMap = idNameMap;
            if (null == id) {
                id = symid;
                nameMap = symidNameMap;
            }
            if (id != null) {
                @SuppressWarnings("SuspiciousMethodCalls")
                String ref = nameMap.get(id);
                if (StringUtils.isBlank(ref)) {
                    sb.append(prefix);
                    if (undefined != null) {
                        undefined.put(sb.length(), (nameMap == idNameMap ? "i" : "s") + id);
                    }
                    sb.append(postfix);
                } else if (ref.startsWith("$")) {
                    sb.append(def);
                } else {
                    sb.append(prefix).append(ref).append(postfix);
                }
            }
        }

        private void resolveUsed(StringBuilder sb, Object id, Object symid, int unitIndex) {
            String unitName = units.get(unitIndex);
            sb.append(unitName).append(".");
            String def = "__unresolved_" + id;
            Section sec = cache != null ? cache.getContents(unitName) : null;
            if (sec != null) {
                appendLocalReference(sb, id, symid, "", "", def, sec.idNameMap, sec.symidNameMap, null);
            } else {
                sb.append(def);
            }
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
                    @SuppressWarnings("SuspiciousMethodCalls")
                    String ref = nameMap.get(id);
                    if (StringUtils.isBlank(ref) || (ref.startsWith("$"))) {
                        res = defalut;
                    } else {
                        res = res + ref;
                    }

                }
            } else {
                LOG.warn("retrieveReference: id is null");
                System.out.println("retrieveReference: id is null");//===***
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
                if (!sectionBegin(getSection()) && !stack.isEmpty()) {
                    stack.getFirst().appendDataPrefix(qName);
                }
            }
        }

        @Override
        public void endElement(String uri, String localName, String qName) throws SAXException {
            if (!handleDataTag(qName, false)) {
                sectionEnd(getSection());
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

        public void fixupUndefined(Section sec) {
            for (Integer pos : sec.undefined.keySet()) {
                String key = sec.undefined.get(pos);
                Map<String, String> nameMap = idNameMap;
                if ('s' == key.charAt(0)) {
                    nameMap = symidNameMap;
                }
                //result.insert(pos, "[<" + key.substring(1) + ">]");// + nameMap.get(key.substring(1)));
                result.insertText(pos, nameMap.get(key.substring(1)));
            }
            result.insertText(0, PascalBundle.message("decompiled.unit.header"));
        }
    }

    static class Section {
        final String type;
        final String textBegin;
        final String textAfterName;                   // if null name is not printed
        final String beforeSubsec;
        final String textEnd;
        final int removeChars;

        Map<String, Object> data = new TreeMap<String, Object>();
        StringBuilder sb = new StringBuilder();
        public Map<String, String> idNameMap;
        public Map<String, String> symidNameMap;
        Map<Integer, String> undefined = newUndef();

        private static Map<Integer, String> newUndef() {
            return new TreeMap<Integer, String>(new Comparator<Integer>() {
                public int compare(Integer o1, Integer o2) {
                    return o2-o1; // reverse order
                }
            });
        }

        String name;
        private boolean ignore = false;
        private String dataPrefix = null;

        private boolean beforeSubsecAdded = false;

        public Section(String error) {
            this(null, null, null, null, null, 0);
            sb = new StringBuilder(error);
        }

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
            insertText(0, StringUtils.isBlank(getDataStr("visibility")) ? "" : getDataStr("visibility") + " ");
        }

        public void merge(Section sec) {
            for (Integer key : sec.undefined.keySet()) {
                undefined.put(key + sb.length(), sec.undefined.get(key));
            }
            sb.append(sec.sb);
        }

        public void insertText(int pos, String text) {
            if (StringUtils.isBlank(text)) { return; }
            sb.insert(pos, text);
            Map<Integer, String> newUndef = newUndef();
            for (Integer offs : undefined.keySet()) {
                newUndef.put(offs < pos ? offs : offs + text.length(), undefined.get(offs));
            }
            undefined = newUndef;
        }

        public String getResult() {
            return sb.toString();
        }
    }

/*    private static class PascalBundle {
        public static String message(String s) {
            return s + "\n";
        }
    }*/
}
