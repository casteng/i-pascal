package com.siberika.idea.pascal.lang.compiled;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.util.text.StringUtil;
import com.siberika.idea.pascal.PascalBundle;
import org.jetbrains.annotations.NotNull;
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
import java.util.Iterator;
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
    static final String UNRESOLVED_INTERNAL = "__INTERNAL__";
    static final String INDENT = "  ";
    private static final String LF = "\n@";

    public static Section parse(InputStream inputStream, PPUDecompilerCache cache) throws ParseException, ParserConfigurationException, SAXException, IOException {
        SAXParserFactory factory = SAXParserFactory.newInstance();
        SAXParser parser = factory.newSAXParser();
        XMLHandler handler = new XMLHandler(cache);
        parser.parse(inputStream, handler);
        return handler.result;
    }

    public static Section parse(@NotNull String xml, PPUDecompilerCache cache) throws ParseException, ParserConfigurationException, SAXException, IOException {
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
            addSection("/unit/uses/unit", LF, "", "", ",", 0);
            addSection("/unit/files/file", "", "", "", ", ", 0);

            addSection("/enum", "", null, "", "", 0);
            addSection("/elements", "", "", "", "", 2);
            addSection("/elements/const", "", " = ", "", ", ", 0);

            addSection("/unit", "unit ", ";", "\ninterface\n", "implementation\n" + PascalBundle.message("decompiled.unit.footer") + "\nend.", 0);
            addSection("/units", "", "", "", "", 0).ignore = true;
            addSection("/uses", LF + "uses", "", "", ";\n", 1);
            addSection("/files", "\n{" + PascalBundle.message("decompiled.unit.files") + " ", "", "", "}\n", 2);
            addSection("/interface", "", "", "", "", 0);

//            addSection("/fields/rec", "record\n", "", "", "end", 0);
//            addSection("/fields/proctype", "procedure", "", "", "", 0);

            addSection("/rec", "", null, "", "", 0);
            addSection("/fields", "", "", "", "", 0);
            addSection("/field", LF, ": ", "", ";", 0);

            addSection("/options", "", "", "", "", 0).ignore = true;
            addSection("/undefined", "", "", "", "", 0).ignore = true;
            addSection("/formal", "", "", "", "", 0).ignore = true;

            addSection("/proc", "", "", "", ";\n", 0);
            addSection("/proctype", "", null, "", "", 0);
            addSection("prop/params", "[", "", "", "]", 2);
            addSection("/params", "(", "", "", ")", 2);
            addSection("/param", "", "", "", "; ", 0);

            addSection("/const", LF + "const ", " = ", "", ";\n", 0);
            addSection("/var", LF + "var ", ": ", "", ";\n", 0);

            addSection("/array", "", null, "", "", 0);

            addSection("/obj", "", null, "", LF + "end;\n", 0);

            addSection("/classref", "", null, "", "", 0);

            addSection("/prop", LF + "property ", "", "", ";\n", 0);

            for (String name : TYPES) {
                addSection(name, "", null, "", "", 0);
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

            NAME_SUB.put("True",  "__True");
            NAME_SUB.put("False", "__False");
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

        final Map<String, String> idNameMap = new HashMap<String, String>();
        final Map<String, String> symidNameMap = new HashMap<String, String>();
        final List<String> units = new ArrayList<String>();

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
            for (Map.Entry<String, Section> entry : SECTIONS.entrySet()) {
                if (path.endsWith(entry.getKey())) {
                    return entry.getValue();
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
            sec.setIndent(stack.size());
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

                if (!sec.isAnonimous()) {
                    if (stack.isEmpty()) {
                        result = sec;
                        fixupUndefined(sec);
                        result.idNameMap = idNameMap;
                        result.symidNameMap = symidNameMap;
                    }
                } else {
                    if (!StringUtil.isEmpty(sec.getDataStr("id"))) {
                        idNameMap.put(sec.getDataStr("id"), sec.sb.toString());
                    }
                    if (!StringUtil.isEmpty(sec.getDataStr("symid"))) {
                        symidNameMap.put(sec.getDataStr("symid"), sec.sb.toString());
                    }
                }
                if (!stack.isEmpty()) {
                    stack.getFirst().merge(sec);
                }
            } else if (LOG.isDebugEnabled()) {
                LOG.debug("Section ignored: " + sec);
            }
        }

        private void handleSectionEnd(Section sec) {
            if (TYPES.contains(sec.type)) {
                insertTypeDeclName(sec);
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
                    appendReference(sec, sec.sb.length(), "ptr", "", "", UNRESOLVED_INTERNAL);
                } else if ("/type".equalsIgnoreCase(sec.type)) {
                    appendReference(sec, sec.sb.length(), "ref", "", "", UNRESOLVED_INTERNAL);
                } else if ("/set".equalsIgnoreCase(sec.type)) {
                    sec.sb.append("set");
                    appendReference(sec, sec.sb.length(), "eltype", " of ", "", UNRESOLVED_INTERNAL);
                } else {
                    sec.sb.append(sec.name);
                }
                appendLineEnd(sec);
            } else if ("/array".equalsIgnoreCase(sec.type)) {
                insertTypeDeclName(sec);
                sec.sb.append(" array");
                if (!hasOption(sec, "dynamic") && allDataPresent(sec, "low", "high") && !"-1".equals(sec.getDataStr("high"))) {
                    sec.sb.append("[").append(sec.getDataStr("low")).append("..").append(sec.getDataStr("high")).append("]");
                }
                sec.sb.append(" of ");
                appendReference(sec, sec.sb.length(), "eltype", "", "", UNRESOLVED_INTERNAL);
                appendLineEnd(sec);
            } else if ("/options".equalsIgnoreCase(sec.type)) {
                if (!stack.isEmpty()) {
                    stack.getFirst().data.putAll(sec.data);
                }
            } else if ("/param".equalsIgnoreCase(sec.type)) {
                sec.insertText(0, sec.getDataStr("spez") + " ");
                appendReference(sec, sec.sb.length(), "vartype", ": ", "", UNRESOLVED_INTERNAL);
            } else if ("/field".equalsIgnoreCase(sec.type)) {
                sec.insertVisibility(LF.length(), getDefaultVisibility());
                appendReference(sec, sec.sb.length(), "vartype", "", "", UNRESOLVED_INTERNAL);
            } else if ("/var".equalsIgnoreCase(sec.type)) {
                appendReference(sec, sec.sb.length(), "vartype", "", "", UNRESOLVED_INTERNAL);
            } else if ("/proc".equalsIgnoreCase(sec.type)) {
                String comment = "";
                sec.insertText(0, LF);
                if (hasOption(sec, "procedure")) {
                    sec.insertText(LF.length(), "procedure ");
                } else if (hasOption(sec, "constructor")) {
                    sec.insertText(LF.length(), "constructor ");
                } else if (hasOption(sec, "destructor")) {
                    sec.insertText(LF.length(), "destructor ");
                } else if (hasOption(sec, "operator")) {
                    sec.insertText(LF.length(), "function __");
                    sec.sb.append(": ");
                    appendReference(sec, sec.sb.length(), "rettype", "", "", UNRESOLVED_INTERNAL);
                    comment = "; // operator " + NAME_SUB.get("$" + sec.name);
                } else if (hasOption(sec, "function")) {
                    sec.insertText(LF.length(), "function ");
                    sec.sb.append(": ");
                    appendReference(sec, sec.sb.length(), "rettype", "", "", UNRESOLVED_INTERNAL);
                }
                if (hasOption(sec, "classmethod")) {
                    sec.insertText(LF.length(), "class ");
                }
                for (String directive : DIRECTIVES) {
                    if (hasOption(sec, directive)) {
                        sec.sb.append("; ").append(directive);
                    }
                }
                sec.sb.append(comment);
                sec.insertVisibility(LF.length(), getDefaultVisibility());
            } else if ("/obj".equalsIgnoreCase(sec.type)) {
                StringBuilder psb = new StringBuilder(LF + "type ");
                psb.append(sec.name).append(" = ").append(sec.getDataStr("objtype"));
                int pos = psb.length();
                if (!StringUtil.isEmpty(sec.getDataStr("iid"))) {
                    psb.append(LF).append(INDENT).append("['").append(sec.getDataStr("iid")).append("']");
                }
                sec.insertText(0, psb.toString());
                appendReference(sec, pos, "ancestor", "(", ")", UNRESOLVED_INTERNAL);
            } else if ("/rec".equalsIgnoreCase(sec.type)) {
                StringBuilder psb = new StringBuilder("");
                if (!StringUtil.isEmpty(sec.name)) {
                    psb.append(LF).append("type ").append(sec.name).append(" = record");
                } else {
                    psb.append("record ");
                }
                sec.insertText(0, psb.toString());
                sec.sb.append(LF + "end");
                appendLineEnd(sec);
            } else if ("/classref".equalsIgnoreCase(sec.type)) {
                insertTypeDeclName(sec);
                sec.sb.append("class of ");
                appendReference(sec, sec.sb.length(), "ref", "", "", UNRESOLVED_INTERNAL);
                appendLineEnd(sec);
            } else if ("/enum".equalsIgnoreCase(sec.type)) {
                int pos = 0;
                if (sec.name != null) {
                    pos = sec.insertText(0, LF + "type " + sec.name + " = ");
                }
                sec.insertText(pos, "(");
                sec.sb.append(")");
                appendLineEnd(sec);
            } else if ("/proctype".equalsIgnoreCase(sec.type)) {
                String returnTypeId = sec.getDataStr("rettype/id");
                if (StringUtil.isEmpty(returnTypeId) || "$void".equalsIgnoreCase(idNameMap.get(returnTypeId))) {
                    sec.insertText(0, "procedure ");
                } else {
                    sec.insertText(0, "function ");
                    sec.sb.append(": ");
                    appendReference(sec, sec.sb.length(), "rettype", "", "", UNRESOLVED_INTERNAL);
                }
                if (sec.name != null) {
                    sec.insertText(0, LF + "type " + sec.name + " = ");
                }
                if ("1".equals(sec.getDataStr("methodptr"))) {
                    sec.sb.append(" of object");
                }
                appendLineEnd(sec);
            } else if ("/prop".equalsIgnoreCase(sec.type)) {
                sec.sb.append(": ");
                appendReference(sec, sec.sb.length(), "proptype", "", "", UNRESOLVED_INTERNAL);
                appendIfAllNotBlank(sec.sb, " read ", retrieveReference(null, sec.data.get("getter/id"), sec.data.get("getter/symid"), ""));
                appendIfAllNotBlank(sec.sb, " write ", retrieveReference(null, sec.data.get("setter/id"), sec.data.get("setter/symid"), ""));
                sec.insertVisibility(LF.length(), getDefaultVisibility());
            }
        }

        private String getDefaultVisibility() {
            Iterator<Section> it = stack.iterator();
            it.next();
            if (it.hasNext()) {
                Section sec = it.next();
                if (sec.isStructuredType()) {
                    return "public ";
                }
            }
            return "";
        }

        private void appendLineEnd(Section sec) {
            if (!StringUtil.isEmpty(sec.name)) {
                sec.sb.append(";\n");
            }
        }

        private void insertTypeDeclName(Section sec) {
            if (!StringUtil.isEmpty(sec.name)) {
                sec.sb.append(LF).append("type ").append(sec.name).append(" = ");
            }
        }

        private void appendIfAllNotBlank(StringBuilder sb, String...args) {
            for (String arg : args) {
                if (StringUtil.isEmpty(arg)) {
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
         * prefix/postfix and default are mutually exclusive
         */
        private void appendReference(Section sec, int pos, String refName, String prefix, String postfix, String def) {
            Object unit = sec.data.get(refName + "/unit");
            if (unit != null) {
                pos = sec.insertText(pos, prefix);
                pos = resolveUsed(sec, pos, sec.data.get(refName + "/id"), sec.data.get(refName + "/symid"), Integer.parseInt((String) unit));
                pos = sec.insertText(pos, postfix);
            } else {
                appendLocalReference(sec, pos, sec.data.get(refName + "/id"), sec.data.get(refName + "/symid"), prefix, postfix, def,
                        idNameMap, symidNameMap);
            }
        }

        @SuppressWarnings("UnusedAssignment")
        private int appendLocalReference(Section sec, int pos, Object id, Object symid, String prefix, String postfix, String def,
                                         Map<String, String> idNameMap, Map<String, String> symidNameMap) {
            Map<String, String> nameMap = idNameMap;
            if (null == id) {
                id = symid;
                nameMap = symidNameMap;
            }
            if (id != null) {
                @SuppressWarnings("SuspiciousMethodCalls")
                String ref = nameMap != null ? nameMap.get(id) : null;
                if (StringUtil.isEmpty(ref)) {
                    pos = sec.insertText(pos, prefix + postfix);
                    if (idNameMap != null) {
                        if (sec.undefined != null) {
                            sec.undefined.put(pos - postfix.length(), (nameMap == idNameMap ? "i" : "s") + id);
                        }
                    } else {
                        pos = sec.insertText(pos, prefix + def + postfix);
                    }
                } else if (!("$formal".equalsIgnoreCase(ref) || "$void".equalsIgnoreCase(ref))) {
                    pos = sec.insertText(pos, prefix + ref + postfix);
                }
            }
            return pos;
        }

        @SuppressWarnings("UnusedAssignment")
        private int resolveUsed(Section sec, int pos, Object id, Object symid, int unitIndex) {
            String unitName = getUnit(unitIndex);
            pos = sec.insertText(pos, unitName + ".");
            String def = "__unresolved_" + id;
            Section section = cache != null ? cache.getContents(unitName, null) : null;
            if (section != null) {
                return appendLocalReference(sec, pos, id, symid, "", "", def, section.idNameMap, section.symidNameMap);
            } else {
                return sec.insertText(pos, def);
            }
        }

        private String getUnit(int unitIndex) {
            if ((unitIndex >= 0) && (unitIndex < units.size())) {
                return units.get(unitIndex);
            }
            return "";
        }

        private String retrieveReference(Object unit, Object id, Object symid, String defalut) {
            String res = "";
            if (unit != null) {
                int ind = Integer.parseInt((String) unit);
                res = getUnit(ind) + ".";
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
                    if (StringUtil.isEmpty(ref) || (ref.startsWith("$"))) {
                        res = defalut;
                    } else {
                        res = res + ref;
                    }

                }
            } else {
                LOG.info("ERROR: retrieveReference: id is null");
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
                    if (NAME_SUB.get(txt) != null) {
                        if (txt.startsWith("$")) {
                            sec.name = txt.substring(1);
                        } else {
                            sec.name = NAME_SUB.get(txt);
                        }
                    } else {
                        int pos = txt.indexOf('$');
                        sec.name = pos < 1 ? txt : txt.substring(0, pos);
                    }
                    if (!path.endsWith(sec.type + "/name")) {
                        LOG.info("ERROR: ! name for section: " + sec.type + ", path: " + path);
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
                        idNameMap.put(txt, sec.name);
                    }
                } else if ("symid".equalsIgnoreCase(qName) && (sec.name != null)) {
                    if (null == sec.dataPrefix) {
                        symidNameMap.put(txt, sec.name);
                    }
                } else if ("value".equalsIgnoreCase(qName)) {
                    if ("/units".equalsIgnoreCase(sec.type)) {
                        units.add(txt);
                    } else if ("/options".equalsIgnoreCase(sec.type)) {
                        sec.putData(sec.type + "/" + txt, "");
                    }
                    if (null == sec.dataPrefix) {
                        if (sec.data.get(sec.type + "/isString") != null) {
                            sec.sb.append('\'').append(handleStringData(txt)).append('\'');
                        } else {
                            sec.sb.append(txt);
                        }
                    }
                } else if ("valtype".equalsIgnoreCase(qName)) {
                    if ("string".equalsIgnoreCase(txt)) {
                        sec.putData(sec.type + "/isString", Boolean.TRUE);
                    }
                }
                if (txt.length() > 0) {
                    sec.putData(qName, txt);
                }
            }
            chars = new StringBuilder();
            path = path.substring(0, path.lastIndexOf("/"));
        }

        private String handleStringData(String txt) {
            return txt.replaceAll("'", "''").replaceAll("\n", "'#10'").replaceAll("\r", "'#13'");
        }

        public void fixupUndefined(Section sec) {
            for (Integer pos : sec.undefined.keySet()) {
                String key = sec.undefined.get(pos);
                Map<String, String> nameMap = idNameMap;
                if ('s' == key.charAt(0)) {
                    nameMap = symidNameMap;
                }
                String res = nameMap.get(key.substring(1));
                res = res != null ? res : "__unresolved__";
                result.insertText(pos, res);
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
        private String indent = "";

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

        public boolean isError() {
            return (null == type) && (null == textBegin) && (null == textAfterName) && (null == beforeSubsec) && (null == textEnd);
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

        public void insertVisibility(int pos, String defaultVisibility) {
            insertText(pos, StringUtil.isEmpty(getDataStr("visibility")) ? defaultVisibility : getDataStr("visibility") + " ");
        }

        public void merge(Section sec) {
            // add indents
            int lastPos = 0;
            int pos = sec.sb.indexOf(LF, lastPos);
            while ((pos >= 0) && (pos <= sec.sb.length() - LF.length())) {
                lastPos = sec.replaceText(pos, LF.length(), "\n" + sec.indent);
                pos = sec.sb.indexOf(LF, lastPos);
            }
            // fix undefined references
            for (Integer key : sec.undefined.keySet()) {
                undefined.put(key + sb.length(), sec.undefined.get(key));
            }
            // merge buffers
            if (!sec.isAnonimous()) {
                sb.append(sec.sb);
            }
        }

        private int replaceText(int pos, int len, String text) {
            sb.delete(pos, pos + len);
            sb.insert(pos, text);
            Map<Integer, String> newUndef = newUndef();
            for (Map.Entry<Integer, String> entry : undefined.entrySet()) {
                newUndef.put(entry.getKey() < pos ? entry.getKey() : entry.getKey() + text.length() - len, entry.getValue());
            }
            undefined = newUndef;
            return pos + text.length() - len;
        }

        public int insertText(int pos, String text) {
            if (!StringUtil.isEmpty(text)) {
                return doInsertText(pos, text);
            } else {
                return pos;
            }
        }

        private int doInsertText(int pos, String text) {
            sb.insert(pos, text);
            Map<Integer, String> newUndef = newUndef();
            for (Map.Entry<Integer, String> entry : undefined.entrySet()) {
                newUndef.put(entry.getKey() < pos ? entry.getKey() : entry.getKey() + text.length(), entry.getValue());
            }
            undefined = newUndef;
            return pos + text.length();
        }

        public String getResult() {
            return sb.toString();
        }

        public boolean isAnonimous() {
            return StringUtil.isEmpty(name) &&
                    (!StringUtil.isEmpty(getDataStr("id")) || !StringUtil.isEmpty(getDataStr("symid")));
        }

        public void setIndent(int size) {
            StringBuilder isb = new StringBuilder();
            for (int i = 0; i < size; i++) {
                isb.append(INDENT);
            }
            indent = isb.toString();
        }

        public boolean isStructuredType() {
            return "/obj".equals(type);
        }
    }

/*    static class PPUDecompilerCache {
        public Section getContents(String unitName) {
            try {
                return DumpParser.parse("<unit></unit>", this);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }
    }

    private static class PascalBundle {
        public static String message(String s) {
            return s + "\n";
        }
    }

    private static class Logger {
        private static Logger log = new Logger();

        public static Logger getInstance(Class<DumpParser> ppuDumpParserClass) {
            return log;
        }

        public void warn(String s) {
            System.out.println("W: " + s);
        }
    }*/
}
