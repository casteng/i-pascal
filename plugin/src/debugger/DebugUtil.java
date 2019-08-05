package com.siberika.idea.pascal.debugger;

import com.intellij.execution.configurations.RunProfile;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.roots.ProjectRootManager;
import com.siberika.idea.pascal.debugger.gdb.parser.GdbMiLine;
import com.siberika.idea.pascal.jps.sdk.PascalSdkData;
import com.siberika.idea.pascal.run.PascalRunConfiguration;
import com.siberika.idea.pascal.sdk.BasePascalSdkType;

public class DebugUtil {

    private static final Logger LOG = Logger.getInstance(DebugUtil.class);

    static boolean isLldb(Sdk sdk) {
        PascalSdkData data = sdk != null ? BasePascalSdkType.getAdditionalData(sdk) : PascalSdkData.EMPTY;
        return data.isLldbBackend();
    }

    static Sdk retrieveSdk(ExecutionEnvironment environment) {
        Sdk sdk = null;
        RunProfile conf = environment.getRunProfile();
        if (conf instanceof PascalRunConfiguration) {
            Module module = ((PascalRunConfiguration) conf).getConfigurationModule().getModule();
            sdk = module != null ? ModuleRootManager.getInstance(module).getSdk() : null;
        } else {
            LOG.warn("Invalid run configuration class: " + (conf != null ? conf.getClass().getName() : "<null>"));
        }
        return sdk != null ? sdk : ProjectRootManager.getInstance(environment.getProject()).getProjectSdk();
    }

    public static PascalSdkData getData(Sdk sdk) {
        return sdk != null ? BasePascalSdkType.getAdditionalData(sdk) : PascalSdkData.EMPTY;
    }

    public static long parseHex(String s) {
        int len = s.length();
        long data = 0;
        for (int i = 0; i < len; i += 2) {
            data = data * 256 + ((Character.digit(s.charAt(len - 2 - i), 16) << 4)
                    + Character.digit(s.charAt(len - 1 - i), 16));
        }
        return data;
    }

    public static String retrieveResultValue(GdbMiLine res) {
        if (res.getType() == GdbMiLine.Type.RESULT_RECORD && "done".equals(res.getRecClass())) {
            return res.getResults().getString("value");
        }
        return null;
    }

    public static Integer retrieveResultValueInt(GdbMiLine res) {
        if (res.getType() == GdbMiLine.Type.RESULT_RECORD && "done".equals(res.getRecClass())) {
            return res.getResults().getInteger("value");
        }
        return null;
    }

}
