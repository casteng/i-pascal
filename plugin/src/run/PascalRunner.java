package com.siberika.idea.pascal.run;

import com.intellij.execution.configurations.RunProfile;
import com.intellij.execution.executors.DefaultRunExecutor;
import com.intellij.execution.runners.DefaultProgramRunner;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.roots.CompilerModuleExtension;
import com.intellij.openapi.vfs.VirtualFile;
import com.siberika.idea.pascal.jps.util.FileUtil;
import com.siberika.idea.pascal.module.PascalModuleType;
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
                (executorId.equals(DefaultRunExecutor.EXECUTOR_ID));
    }

    @Nullable
    public static String getExecutable(@NotNull Module module) {
        VirtualFile mainFile = PascalModuleType.getMainFile(module);
        CompilerModuleExtension compilerModuleExtension = CompilerModuleExtension.getInstance(module);
        if ((mainFile != null) && (compilerModuleExtension != null) && (compilerModuleExtension.getCompilerOutputPath() != null)) {
            File outputPath = new File(compilerModuleExtension.getCompilerOutputPath().getPath());
            return FileUtil.getExecutable(outputPath, mainFile.getNameWithoutExtension()).getPath();
        } else {
            return null;
        }
    }
}
