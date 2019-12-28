package com.siberika.idea.pascal.debugger;

import com.intellij.openapi.projectRoots.Sdk;
import com.siberika.idea.pascal.debugger.gdb.GdbVariableObject;
import com.siberika.idea.pascal.debugger.settings.PascalDebuggerViewSettings;
import com.siberika.idea.pascal.debugger.settings.PascalTypeRenderers;
import com.siberika.idea.pascal.debugger.settings.TypeRenderer;
import com.siberika.idea.pascal.jps.sdk.PascalSdkData;
import com.siberika.idea.pascal.jps.util.FileUtil;

public abstract class DebugBackend {
    protected final PascalXDebugProcess process;
    protected final Sdk sdk;
    public final DebugBackend.Options options;

    public abstract void init();
    public abstract void applySettings();
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

    public abstract void queryArrayValue(GdbVariableObject var, int start, long end, String arrayType);

    public abstract void addLineBreakpoint(String filename, int line, int ignoreCount, boolean temporary ,CommandSender.FinishCallback callback);

    public abstract void threadSelect(String id);

    public void evaluate(String expression, CommandSender.FinishCallback finishCallback) {
        process.sendCommand("-data-evaluate-expression \"" + expression + "\"", finishCallback);
    }

    protected String getFileName(String fullPath) {
        return options.useFullnameForBreakpoints ? fullPath : FileUtil.getFilename(fullPath);
    }

    public static final class Options {
        private final Sdk sdk;
        public boolean supportsBulkDelete;

        public boolean useFullnameForBreakpoints;
        public PascalDebuggerViewSettings view;
        public final PascalTypeRenderers typeRenderers;

        public int pointerSize;
        public int maxFrames = 1000;

        public Options(Sdk sdk) {
            this.sdk = sdk;
            this.view = PascalDebuggerViewSettings.getInstance();
            this.typeRenderers = PascalTypeRenderers.getInstance();
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

        public String getTypeRenderer(String type) {
            for (TypeRenderer typeRenderer : typeRenderers.typeRenderers) {
                if (typeRenderer.type.equalsIgnoreCase(type)) {
                    return typeRenderer.value;
                }
            }
            return null;
        }
    }

    protected static boolean isPointer(GdbVariableObject var) {
        return var.getType().startsWith("P") || var.getType().contains("(*)");     // TODO: implement more correct check
    }

}
