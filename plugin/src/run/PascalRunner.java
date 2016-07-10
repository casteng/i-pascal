package com.siberika.idea.pascal.run;

import com.intellij.execution.configurations.RunProfile;
import com.intellij.execution.executors.DefaultDebugExecutor;
import com.intellij.execution.executors.DefaultRunExecutor;
import com.intellij.execution.runners.DefaultProgramRunner;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.roots.CompilerModuleExtension;
import com.intellij.openapi.util.text.StringUtil;
import com.siberika.idea.pascal.jps.model.JpsPascalModuleType;
import com.siberika.idea.pascal.jps.util.FileUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;

/**
 * Author: George Bakhtadze
 * Date: 05/12/2012
 */
public class PascalRunner extends DefaultProgramRunner {
    @NotNull
    public String getRunnerId() {
        return "com.siberika.idea.pascal.run.PascalRunner";
    }

    public boolean canRun(@NotNull String executorId, @NotNull RunProfile profile) {
        return (profile instanceof PascalRunConfiguration) &&
                (executorId.equals(DefaultRunExecutor.EXECUTOR_ID) || executorId.equals(DefaultDebugExecutor.EXECUTOR_ID));
    }

    @Nullable
    public static String getExecutable(@NotNull Module module, String programFileName) {
        if (null == programFileName) {
            return null;
        }
        String path = module.getOptionValue(JpsPascalModuleType.USERDATA_KEY_EXE_OUTPUT_PATH.toString());
        if (StringUtil.isEmpty(path)) {
            CompilerModuleExtension compilerModuleExtension = CompilerModuleExtension.getInstance(module);
            if ((compilerModuleExtension != null) && (compilerModuleExtension.getCompilerOutputPath() != null)) {
                path = compilerModuleExtension.getCompilerOutputPath().getPath();
            }
        }
        return FileUtil.getExecutable(new File(path != null ? path : ""), programFileName).getPath();
    }
}
