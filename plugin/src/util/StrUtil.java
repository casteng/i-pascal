package com.siberika.idea.pascal.util;

/**
 * Author: George Bakhtadze
 * Date: 09/04/2015
 */
public class StrUtil {
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
}
