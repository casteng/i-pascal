package com.siberika.idea.pascal.jps.sdk;

import com.google.common.collect.ImmutableMap;
import com.intellij.openapi.projectRoots.SdkAdditionalData;
import com.intellij.openapi.util.SystemInfo;

import java.util.HashMap;
import java.util.Map;

/**
 * Author: George Bakhtadze
 * Date: 18/01/2013
 */
public class PascalSdkData implements SdkAdditionalData {

    public static final PascalSdkData EMPTY = new PascalSdkData();
    public static final String[] DEBUGGER_BACKENDS = {"GDB", "LLDB"};
    public static final String SDK_DATA_TRUE = "1";

    private static final Map<String, Object> DEFAULTS_MAP = new ImmutableMap.Builder<String, Object>()
            .put(Keys.DEBUGGER_BACKEND.getKey(), DEBUGGER_BACKENDS[SystemInfo.isMac ? 1 : 0])
            .put(Keys.DEBUGGER_REDIRECT_CONSOLE.getKey(), "1")
            .put(Keys.DEBUGGER_BREAK_FULL_NAME.getKey(), "1")
            .put(Keys.DEBUGGER_RETRIEVE_CHILDS.getKey(), "1")
            .put(Keys.DEBUGGER_USE_GDBINIT.getKey(), "0")
            .put(Keys.DEBUGGER_RESOLVE_NAMES.getKey(), "1")
            .build();
    private static final String LLDB_MI_DEFAULT_PATH = "/Applications/Xcode.app/Contents/Developer/usr/bin/lldb-mi";

    public static String getDefaultLLDBCommand() {
        return SystemInfo.isMac ? LLDB_MI_DEFAULT_PATH : "lldb-mi";
    }

    public enum Keys {
        COMPILER_COMMAND("compilerCommand"),
        COMPILER_FAMILY("compilerFamily"),
        COMPILER_NAMESPACES("compilerNamespaces"),
        COMPILER_OPTIONS("compilerOptions"),
        COMPILER_OPTIONS_DEBUG("compilerOptionsDebug"),
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
        } else if (Keys.DEBUGGER_COMMAND.getKey().equals(key)) {
            return isLldbBackend() ? getDefaultLLDBCommand() : "gdb";
        } else {
            return DEFAULTS_MAP.get(key);
        }
    }

    public boolean isLldbBackend() {
        return DEBUGGER_BACKENDS[1].equals(getString(Keys.DEBUGGER_BACKEND));
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
