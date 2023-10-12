package com.siberika.idea.pascal.debugger.gdb.parser;

import com.intellij.codeInspection.SmartHashMap;
import com.siberika.idea.pascal.util.StrUtil;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Author: George Bakhtadze
 * Date: 29/03/2017
 */
public class GdbMiResults {
    private Map<String, Object> data;

    public void setValue(String name, Object value) {
        ensureData();
        data.put(name, value);
    }

    public Object getValue(String name) {
        ensureData();
        return data.get(name);
    }

    private void ensureData() {
        if (null == data) {
            data = new SmartHashMap<>();
        }
    }

    public List<Object> getList(String name) {
        return (List<Object>) getValue(name);
    }

    public String getString(String name) {
        return (String) getValue(name);
    }

    public GdbMiResults getTuple(String name) {
        return (GdbMiResults) getValue(name);
    }

    public Integer getInteger(String name) {
        return StrUtil.strToIntDef(getString(name), null);
    }

    @Override
    public String toString() {
        ensureData();
        return "[" + new HashMap<>(data).toString() + "]";
    }
}
