package com.siberika.idea.pascal.jps.sdk;

import com.intellij.openapi.projectRoots.SdkAdditionalData;

import java.util.HashMap;
import java.util.Map;

/**
 * Author: George Bakhtadze
 * Date: 18/01/2013
 */
public class PascalSdkData implements SdkAdditionalData {

    public static final String DATA_KEY_COMPILER_FAMILY = "compilerFamily";
    public static final String DATA_KEY_COMPILER_OPTIONS = "compilerOptions";
    public static final String DATA_KEY_DECOMPILER_CACHE = "decompilerCache";
    public static final String DATA_KEY_DECOMPILER_COMMAND = "decompilerCommand";
    public static final String DATA_KEY_DEBUGGER_COMMAND = "debuggerCommand";

    private final Map<String, Object> data;

    public PascalSdkData() {
        this.data = new HashMap<String, Object>();
    }

    public PascalSdkData(Map<String, Object> data) {
        this.data = data;
    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        return new PascalSdkData(data);
    };

    public Object getValue(final String key) {
        return data.get(key);
    }

    public void setValue(final String key, final Object value) {
        data.put(key, value);
    }
}
