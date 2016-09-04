package com.siberika.idea.pascal.sdk;

import com.intellij.util.SmartList;

import java.util.List;

/**
 * Author: George Bakhtadze
 * Date: 04/09/2016
 */
public class Directive {
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
}
