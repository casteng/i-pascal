package com.siberika.idea.pascal.util;

import com.intellij.execution.ExecutionException;
import com.intellij.execution.configurations.GeneralCommandLine;
import com.intellij.execution.process.CapturingProcessHandler;
import com.intellij.execution.process.ProcessOutput;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;

/**
 * Author: George Bakhtadze
 * Date: 10/01/2013
 */
public class SysUtils {
    public static final int STANDARD_TIMEOUT = 10 * 1000;
    @NotNull
    public static ProcessOutput getProcessOutput(@NotNull final String workDir, @NotNull final String exePath,
                                                 @NotNull final String... arguments) throws ExecutionException {
        return getProcessOutput(STANDARD_TIMEOUT, workDir, exePath, arguments);
    }

    @NotNull
    public static ProcessOutput getProcessOutput(final int timeout, @NotNull final String workDir,
                                                 @NotNull final String exePath,
                                                 @NotNull final String... arguments) throws ExecutionException {
        if (!new File(workDir).isDirectory() || !new File(exePath).canExecute()) {
            return new ProcessOutput();
        }

        final GeneralCommandLine cmd = new GeneralCommandLine();
        cmd.setWorkDirectory(workDir);
        cmd.setExePath(exePath);
        cmd.addParameters(arguments);

        return execute(cmd, timeout);
    }

    @NotNull
    public static ProcessOutput execute(@NotNull final GeneralCommandLine cmd) throws ExecutionException {
        return execute(cmd, STANDARD_TIMEOUT);
    }

    @NotNull
    public static ProcessOutput execute(@NotNull final GeneralCommandLine cmd,
                                        final int timeout) throws ExecutionException {
        final CapturingProcessHandler processHandler = new CapturingProcessHandler(cmd.createProcess());
        return timeout < 0 ? processHandler.runProcess() : processHandler.runProcess(timeout);
    }

    @Nullable
    public static String runAndGetStdOut(String workDir, String exePath, String...params) {
        final ProcessOutput processOutput;
        try {
            processOutput = getProcessOutput(workDir, exePath, params);
        } catch (final ExecutionException e) {
            return null;
        }
        if (processOutput.getExitCode() != 0) {
            return null;
        }
        final String stdout = processOutput.getStdout().trim();
        if (stdout.isEmpty()) return null;

        return stdout;
    }
}
