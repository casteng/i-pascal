package com.siberika.idea.pascal.sdk;

import com.intellij.util.SmartList;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 * Author: George Bakhtadze
 * Date: 04/09/2016
 */
public class Directive {
    private static final Collection<String> DEFINES = Arrays.asList("$IFDEF", "$IFNDEF");
    // description
    public String desc;
    // possible values
    public List<String> values;

    public void addValue(String value) {
        if (values != null) {
            values.add(value);
        } else {
            values = new SmartList<String>(value);
        }
    }

    public boolean hasParameters(String id) {
        return (values != null) || isDefine(id);
    }

    public static boolean isDefine(String id) {
        return DEFINES.contains(id.toUpperCase());
    }
}
