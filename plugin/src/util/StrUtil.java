package com.siberika.idea.pascal.util;

import com.intellij.codeInspection.SmartHashMap;
import com.intellij.openapi.util.Pair;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Author: George Bakhtadze
 * Date: 09/04/2015
 */
public class StrUtil {
    public static final Pattern PATTERN_FIELD = Pattern.compile("[fF][A-Z]\\w*");

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
}
