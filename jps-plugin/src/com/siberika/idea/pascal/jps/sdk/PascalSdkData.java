package com.siberika.idea.pascal.jps.sdk;

import com.intellij.openapi.projectRoots.SdkAdditionalData;

import java.util.HashMap;
import java.util.Map;

/**
 * Author: George Bakhtadze
 * Date: 18/01/2013
 */
public class PascalSdkData implements SdkAdditionalData {

    public enum keys {
        COMPILER_COMMAND("compilerCommand"),
        COMPILER_FAMILY("compilerFamily"),
        COMPILER_OPTIONS("compilerOptions"),
        DECOMPILER_CACHE("decompilerCache"),
        DECOMPILER_COMMAND("decompilerCommand"),

        DEBUGGER_COMMAND("debuggerCommand"),
        DEBUGGER_OPTIONS("debuggerOptions"),
        DEBUGGER_REDIRECT_CONSOLE("debuggerRedirectConsole"),
        DEBUGGER_RETRIEVE_CHILDS("debuggerRetrieveChilds"),
        DEBUGGER_USE_GDBINIT("debuggerUseGdbinit"),
        DEBUGGER_RESOLVE_NAMES("debuggerResolveNames"),
        DEBUGGER_CALL_GETTERS("debuggerCallGetters"),
        DEBUGGER_ASM_FORMAT("debuggerAsmFormat"),
        ;
        private final String key;

        keys(String key) {
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
        return data.get(key);
    }

    public void setValue(final String key, final Object value) {
        data.put(key, value);
    }
}
