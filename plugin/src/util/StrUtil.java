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

}
