package com.siberika.idea.pascal.jps.sdk;

import com.google.common.collect.ImmutableMap;
import com.intellij.openapi.projectRoots.SdkAdditionalData;

import java.util.HashMap;
import java.util.Map;

/**
 * Author: George Bakhtadze
 * Date: 18/01/2013
 */
public class PascalSdkData implements SdkAdditionalData {

    public static final PascalSdkData EMPTY = new PascalSdkData();
    public static final String SDK_DATA_TRUE = "1";
    private static final Map<String, Object> DEFAULTS_MAP = new ImmutableMap.Builder<String, Object>()
            .put(Keys.DEBUGGER_REDIRECT_CONSOLE.getKey(), "1")
            .put(Keys.DEBUGGER_RETRIEVE_CHILDS.getKey(), "1")
            .put(Keys.DEBUGGER_USE_GDBINIT.getKey(), "0")
            .put(Keys.DEBUGGER_RESOLVE_NAMES.getKey(), "1")
            .put(Keys.DEBUGGER_BREAK_FULL_NAME.getKey(), "1")
            .build();

    public enum Keys {
        COMPILER_COMMAND("compilerCommand"),
        COMPILER_FAMILY("compilerFamily"),
        COMPILER_OPTIONS("compilerOptions"),
        DECOMPILER_CACHE("decompilerCache"),
        DECOMPILER_COMMAND("decompilerCommand"),

        DEBUGGER_BACKEND("debuggerBackend"),
        DEBUGGER_COMMAND("debuggerCommand"),
        DEBUGGER_OPTIONS("debuggerOptions"),
        DEBUGGER_REDIRECT_CONSOLE("debuggerRedirectConsole"),
        DEBUGGER_BREAK_FULL_NAME("debuggerBreakFullName"),
        DEBUGGER_RETRIEVE_CHILDS("debuggerRetrieveChilds"),
        DEBUGGER_USE_GDBINIT("debuggerUseGdbinit"),
        DEBUGGER_RESOLVE_NAMES("debuggerResolveNames"),
        DEBUGGER_CALL_GETTERS("debuggerCallGetters"),
        DEBUGGER_ASM_FORMAT("debuggerAsmFormat"),
        DELPHI_IS_STARTER("delphiIsStarter")
        ;
        private final String key;

        Keys(String key) {
            this.key = key;
        }

        public String getKey() {
            return key;
        }
    }

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
        Object res = data.get(key);
        if (res != null) {
            return res;
        } else {
            return DEFAULTS_MAP.get(key);
        }
    }

    public void setValue(final String key, final Object value) {
        data.put(key, value);
    }

    public boolean getBoolean(final Keys key) {
        return SDK_DATA_TRUE.equals(getValue(key.getKey()));
    }

    public String getString(final Keys key) {
        return (String) getValue(key.getKey());
    }

}
