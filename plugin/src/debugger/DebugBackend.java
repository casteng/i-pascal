package com.siberika.idea.pascal.debugger;

import com.intellij.openapi.projectRoots.Sdk;
import com.siberika.idea.pascal.debugger.gdb.GdbVariableObject;
import com.siberika.idea.pascal.jps.sdk.PascalSdkData;
import com.siberika.idea.pascal.jps.util.FileUtil;

public abstract class DebugBackend {
    protected final PascalXDebugProcess process;
    protected final Sdk sdk;
    public final DebugBackend.Options options;

    public abstract void init();
    public abstract void onSessionInit();

    public DebugBackend(PascalXDebugProcess process) {
        this.process = process;
        this.sdk = DebugUtil.retrieveSdk(process.environment);
        this.options = new DebugBackend.Options(sdk);
    }

    public PascalSdkData getData() {
        return DebugUtil.getData(sdk);
    }

    public abstract void createVar(String key, String expression, CommandSender.FinishCallback finishCallback);

    public abstract void queryArrayValue(GdbVariableObject var, int start, long end);

    public abstract void addLineBreakpoint(String filename, int line, CommandSender.FinishCallback callback);

    protected String getFileName(String fullPath) {
        return options.useFullnameForBreakpoints ? fullPath : FileUtil.getFilename(fullPath);
    }

    public static final class Options {
        private final Sdk sdk;
        public boolean supportsBulkDelete;

        public boolean useFullnameForBreakpoints;
        public boolean showNonPrintable = true;
        public boolean refineStrings = true;
        public boolean refineDynamicArrays = true;
        public boolean refineOpenArrays = true;
        public boolean refineStructured = true;
        public int limitChars = 1000;
        public int limitElements = 1000;
        public int limitChilds = 100;
        public int limitValueSize = 2 * 1024 * 1024;

        public int pointerSize;

        public Options(Sdk sdk) {
            this.sdk = sdk;
        }

        public boolean resolveNames() {
            return DebugUtil.getData(sdk).getBoolean(PascalSdkData.Keys.DEBUGGER_RESOLVE_NAMES);
        }
        public boolean callGetters() {
            return false;
        }
        public String asmFormat() {
            return DebugUtil.getData(sdk).getString(PascalSdkData.Keys.DEBUGGER_ASM_FORMAT);
        }

    }
}
