package com.siberika.idea.pascal.util;

import com.intellij.codeInspection.SmartHashMap;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.psi.codeStyle.NameUtil;
import com.intellij.util.SmartList;
import com.siberika.idea.pascal.lang.lexer.PascalFlexLexer;
import com.siberika.idea.pascal.lang.psi.PascalNamedElement;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Author: George Bakhtadze
 * Date: 09/04/2015
 */
public class StrUtil {

    public static final Pattern PATTERN_FIELD = Pattern.compile("[fF][A-Z]\\w*");

    private static final int MAX_SHORT_TEXT_LENGTH = 32;

    public enum ElementType {VAR, CONST, TYPE, FIELD, PROPERTY, ACTUAL_PARAMETER}

    public static boolean hasLowerCaseChar(String s) {
        for (char c : s.toCharArray()) {
            if (Character.isLowerCase(c)) {
                return true;
            }
        }
        return false;
    }

    public static String getFieldName(String name) {
        int ind = Math.min(getPos(name, '('), getPos(name, ':'));
        ind = name.substring(0, ind).lastIndexOf('.');
        if (ind > 0) {
            return name.substring(ind + 1);
        } else {
            return name;
        }
    }

    private static int getPos(String name, char c) {
        int ind = name.indexOf(c);
        return ind >= 0 ? ind : name.length();
    }

    public static <K, V> Map<K, V> getParams(List<Pair<K, V>> entries) {
        Map<K, V> res = entries.size() <= 1 ? new SmartHashMap<K, V>() : new HashMap < K, V>(entries.size());
        for (Pair<K, V> entry : entries) {
            res.put(entry.first, entry.second);
        }
        return res;
    }

    public static String limit(String xml, int max) {
        if ((xml != null) && (xml.length() > max)) {
            return String.format("%s <more %d symbols>", xml.substring(0, max), xml.length() - max);
        } else {
            return xml;
        }
    }

    public static TextRange getIncludeNameRange(String text) {
        if ((null == text) || !text.startsWith("{$") || !text.endsWith("}")) {
            return null;
        }
        int end = text.length() - 1;
        String str = text.substring(2, end).toUpperCase();
        int start = end;

        if (str.startsWith("I ")) {
            start = 4;
        }
        if (str.startsWith("INCLUDE ")) {
            start = 10;
        }
        while ((start < end) && (text.charAt(start) <= ' ')) {
            start++;
        }
        while ((start < end) && (text.charAt(end - 1) <= ' ')) {
            end--;
        }
        if (text.charAt(start) == '\'') {
            start++;
            end--;
        }
        return start < end ? TextRange.create(start, end) : null;
    }

    public static String getIncludeName(String text) {
        TextRange r = getIncludeNameRange(text);
        return r != null ? r.substring(text) : null;
    }

    public static boolean isVersionLessOrEqual(String version1, String version2) {
        return version1.compareTo(version2) <= 0;
    }

    public static Integer strToIntDef(String value, Integer def) {
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return def;
        }
    }

    private static final Pattern PATTERN_DEF_DECL = Pattern.compile("(?i)(defined|declared)\\s*\\(\\s*(\\w+)\\s*\\)");
    public static List<Pair<Integer, String>> parseDirectives(String text) {
        Matcher m = PascalFlexLexer.PATTERN_DEFINE.matcher(text);
        if (m.matches()) {
            return Collections.singletonList(Pair.create(m.start(1), m.group(1)));
        } else {
            m = PascalFlexLexer.PATTERN_CONDITION.matcher(text);
            if (m.matches()) {
                List<Pair<Integer, String>> res = new SmartList<>();
                m = PATTERN_DEF_DECL.matcher(text);
                while (m.find()) {
                    res.add(Pair.create(m.start(2), m.group(2)));
                }
                return res;
            } else {
                return Collections.emptyList();
            }
        }
    }

    public static String toDebugString(PsiElement element) {
        if (element instanceof PascalNamedElement) {
            try {
                return "[" + element.getClass().getSimpleName() + "]\"" + ((PascalNamedElement) element).getName()
                        + "\" ^" + toDebugString(element.getParent()) + "..." + getShortText(element.getParent());
            } catch (NullPointerException e) {
                return "<NPE>";
            }
        } else {
            return element != null ? element.toString() : "";
        }
    }

    private static String getShortText(PsiElement parent) {
        if (null == parent) {
            return "";
        }
        int lfPos = parent.getText().indexOf("\n");
        if (lfPos > 0) {
            return parent.getText().substring(0, lfPos);
        } else {
            return parent.getText().substring(0, Math.min(parent.getText().length(), MAX_SHORT_TEXT_LENGTH));
        }
    }

    public static String removePrefixes(@NotNull String name, String[] prefixes) {
        if ((name.length() < 2) || !Character.isUpperCase(name.charAt(1))) {
            return name;
        }
        String nameUpper = name.toUpperCase();
        for (String prefix : prefixes) {
            if (nameUpper.startsWith(prefix)) {
                return name.substring(prefix.length());
            }
        }
        return name;
    }

    public static String[] extractWords(String s, ElementType type) {
        String[] splitNameIntoWords = NameUtil.splitNameIntoWords(s);
        String[] result = new String[splitNameIntoWords.length];
        String lastWord = "";
        for (int i = splitNameIntoWords.length - 1; i >= 0; i--) {
            String curWord = splitNameIntoWords[i];
            if (ElementType.CONST == type) {
                curWord = curWord.toUpperCase() + (lastWord.length() == 0 ? "" : "_");
            }
            lastWord = curWord + lastWord;
            result[i] = lastWord;
        }
        return result;
    }

    public static String getNamePart(String fqn) {
        int pos = fqn.lastIndexOf(".");
        return pos >= 0 ? fqn.substring(pos + 1) : fqn;
    }

    public static String getNamespace(String fqn) {
        int pos = fqn.lastIndexOf(".");
        return pos >= 0 ? fqn.substring(0, pos) : "";
    }

}
