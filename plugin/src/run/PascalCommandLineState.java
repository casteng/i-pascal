package com.siberika.idea.pascal.run;

import com.intellij.execution.ExecutionException;
import com.intellij.execution.configurations.CommandLineState;
import com.intellij.execution.configurations.GeneralCommandLine;
import com.intellij.execution.filters.TextConsoleBuilderFactory;
import com.intellij.execution.process.CapturingProcessHandler;
import com.intellij.execution.process.ProcessHandler;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.util.SystemInfo;
import com.intellij.openapi.vfs.VirtualFile;
import com.siberika.idea.pascal.PascalBundle;
import com.siberika.idea.pascal.debugger.PascalDebugFactory;
import com.siberika.idea.pascal.jps.util.FileUtil;
import com.siberika.idea.pascal.module.PascalModuleType;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class PascalCommandLineState extends CommandLineState {

    private static final Logger LOG = Logger.getInstance(PascalCommandLineState.class);

    private final List<String> params;
    private final PascalRunConfiguration runConfiguration;
    private final boolean debug;
    private final String workDirectory;
    private final boolean fixIOBuffering;

    public PascalCommandLineState(PascalRunConfiguration runConfiguration, ExecutionEnvironment env, boolean debug,
                                  String workDirectory, String parameters, boolean fixIOBuffering) {
        super(env);
        this.runConfiguration = runConfiguration;
        this.debug = debug;
        this.workDirectory = workDirectory;
        this.fixIOBuffering = fixIOBuffering;
        params = new ArrayList<String>();
        if ((parameters != null) && (parameters.length() > 0)) {
            params.addAll(Arrays.asList(parameters.split("\\s+"))); //TODO: use exec*utils to correctly split params
        }
    }

    @NotNull
    @Override
    protected ProcessHandler startProcess() throws ExecutionException {
        Module module = runConfiguration.findModule(getEnvironment());
        GeneralCommandLine commandLine = new GeneralCommandLine();

        String fileName;
        if (runConfiguration.getProgramFileName() != null) {
            fileName = FileUtil.getFilename(runConfiguration.getProgramFileName());
        } else {
            VirtualFile mainFile = PascalModuleType.getMainFile(module);
            fileName = mainFile != null ? mainFile.getNameWithoutExtension() : null;
        }
        String executable = PascalRunner.getExecutable(module, fileName);
        if (debug) {
            Sdk sdk = runConfiguration.getSdk();
            PascalDebugFactory.adjustCommand(sdk, commandLine, executable);
        } else {
            if (executable != null) {
                if (fixIOBuffering && SystemInfo.isLinux) {
                    commandLine.setExePath("script");
                    commandLine.addParameters("-q", "-c", executable, "/dev/null");
                } else if (fixIOBuffering && (SystemInfo.isUnix || SystemInfo.isMac)) {
                    commandLine.setExePath("script");
                    commandLine.addParameters("-q", "/dev/null", executable);
                } else {
                    commandLine.setExePath(executable);
                }
            } else {
                throw new ExecutionException(PascalBundle.message("execution.noExecutable"));
            }
        }
        commandLine.addParameters(params);
        commandLine.setWorkDirectory(workDirectory);
        ProcessHandler handler = new CapturingProcessHandler(commandLine.createProcess(), commandLine.getCharset(), commandLine.getCommandLineString());
        setConsoleBuilder(TextConsoleBuilderFactory.getInstance().createBuilder(runConfiguration.getProject()));
        return handler;
    }

}
