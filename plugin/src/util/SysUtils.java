package com.siberika.idea.pascal.util;

import com.intellij.execution.ExecutionException;
import com.intellij.execution.configurations.GeneralCommandLine;
import com.intellij.execution.process.CapturingProcessHandler;
import com.intellij.execution.process.ProcessOutput;
import com.intellij.openapi.diagnostic.Logger;
import com.siberika.idea.pascal.PascalBundle;
import com.siberika.idea.pascal.PascalException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;

/**
 * Author: George Bakhtadze
 * Date: 10/01/2013
 */
public class SysUtils {
    public static final Logger LOG = Logger.getInstance(SysUtils.class.getName());

    public static final int LONG_TIMEOUT = 5 * 60 * 1000;
    public static final int SHORT_TIMEOUT = 5 * 1000;

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
        return execute(cmd, LONG_TIMEOUT);
    }

    @NotNull
    public static ProcessOutput execute(@NotNull final GeneralCommandLine cmd,
                                        final int timeout) throws ExecutionException {
        LOG.info("Executing: " + cmd.getCommandLineString());
        final CapturingProcessHandler processHandler = new CapturingProcessHandler(cmd);
        return timeout < 0 ? processHandler.runProcess() : processHandler.runProcess(timeout);
    }

    @Nullable
    public static String runAndGetStdOut(String workDir, String exePath, int timeoutMs, String...params) throws PascalException {
        final ProcessOutput processOutput;
        try {
            processOutput = getProcessOutput(timeoutMs, workDir, exePath, params);
        } catch (final ExecutionException e) {
            return null;
        }
        int exitCode = processOutput.getExitCode();
        final String stdout = processOutput.getStdout().trim();
        final String stderr = processOutput.getStderr().trim();
        if ((exitCode != 0) && (stdout.isEmpty())) {
            LOG.info(String.format("WARNING: Error running %s. Code: %d", exePath, exitCode));
            LOG.info(String.format("Output: %s", stdout));
            LOG.info(String.format("Error: %s", stderr));
            throw new PascalException(PascalBundle.message("error.exit.code", exePath, exitCode, stderr));
        }
        if (stdout.isEmpty()) return null;
        return stdout;
    }

    public static void close(Closeable closeable) {
        try {
            if (closeable != null) {
                closeable.close();
            }
        } catch (IOException e) {
            LOG.warn("Error closing resource", e);
        }
    }
    
}
