package com.siberika.idea.pascal.jps.compiler;

import com.intellij.execution.process.BaseOSProcessHandler;
import com.intellij.execution.process.ProcessAdapter;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.siberika.idea.pascal.jps.JpsPascalBundle;
import com.siberika.idea.pascal.jps.model.JpsPascalModuleType;
import com.siberika.idea.pascal.jps.sdk.PascalCompilerFamily;
import com.siberika.idea.pascal.jps.sdk.PascalSdkData;
import com.siberika.idea.pascal.jps.util.ParamMap;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.PropertyKey;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

/**
 * Author: George Bakhtadze
 * Date: 15/05/2015
 */
public abstract class PascalBackendCompiler {

    public static final Logger LOG = Logger.getInstance(PascalBackendCompiler.class.getName());

    final CompilerMessager compilerMessager;

    PascalBackendCompiler(CompilerMessager compilerMessager) {
        this.compilerMessager = compilerMessager;
    }

    abstract protected boolean createStartupCommandImpl(String sdkHomePath, String moduleName, String outputDirExe, String outputDirUnit,
                                                        List<File> sdkFiles, List<File> moduleLibFiles, boolean isRebuild, boolean isDebug,
                                                        @Nullable ParamMap pascalSdkData, ArrayList<String> commandLine);

    abstract public boolean createSyntaxCheckCommandImpl(String sdkHomePath, String modulePath, PascalSdkData pascalSdkData, VirtualFile[] sourcePaths, ArrayList<String> commandLine, String tempDir);

    @Nullable
    public static PascalBackendCompiler getCompiler(PascalCompilerFamily family, CompilerMessager messager) {
        if (PascalCompilerFamily.FPC.equals(family)) {
            return new FPCBackendCompiler(messager);
        } else if (PascalCompilerFamily.DELPHI.equals(family)) {
            return new DelphiBackendCompiler(messager);
        }
        return null;
    }

    public void launch(CompilerMessager messager, String[] cmdLine, File workingDir) throws IOException {
        BaseOSProcessHandler handler = launchNoWait(messager, cmdLine, workingDir);
        handler.waitFor();
        int exitCode = handler.getProcess().exitValue();
        if (exitCode != 0) {
            messager.warning(null, JpsPascalBundle.message("compiler.exit.code", exitCode), null, -1L, -1L);
        }
    }

    public BaseOSProcessHandler launchNoWait(CompilerMessager messager, String[] cmdLine, File workingDir) throws IOException {
        Process process = Runtime.getRuntime().exec(cmdLine, null, workingDir);
        BaseOSProcessHandler handler = new BaseOSProcessHandler(process, cmdLine[0], Charset.defaultCharset());
        ProcessAdapter adapter = getCompilerProcessAdapter(messager);
        handler.addProcessListener(adapter);
        handler.startNotify();
        return handler;
    }

    @NotNull
    public abstract String getId();

    public abstract ProcessAdapter getCompilerProcessAdapter(CompilerMessager messager);

    public abstract String getCompiledUnitExt();

    public String[] createStartupCommand(final String sdkHomePath, final String moduleName, final String outputDir,
                                         final List<File> sdkLibFiles, final List<File> moduleLibFiles,
                                         final List<File> files, @Nullable final ParamMap moduleData,
                                         final boolean isRebuild, boolean isDebug,
                                         @Nullable final ParamMap pascalSdkData) throws IOException, IllegalArgumentException {
        final ArrayList<String> commandLine = new ArrayList<String>();
        if (outputDir != null) {
            if (!createStartupCommandImpl(sdkHomePath, moduleName, getExeOutputPath(moduleData), outputDir, sdkLibFiles, moduleLibFiles,
                    isRebuild, isDebug, pascalSdkData, commandLine)) {
                return null;
            }
            File mainFile = getMainFile(moduleData);
            if ((null == mainFile) && (files.size() > 0)) {
                mainFile = files.get(0);
            }
            if (mainFile != null) {
                commandLine.add(mainFile.getAbsolutePath());
            } else {
                compilerMessager.error(null, getMessage(moduleName, "compile.noSource"), null, -1, -1);
            }

            StringBuilder sb = new StringBuilder();
            for (String cmd : commandLine) {
                sb.append(" ").append(cmd);
            }
            compilerMessager.info(null, getMessage(moduleName, "compile.commandLine", sb.toString()), null, -1, -1);
        } else {
            compilerMessager.error(null, getMessage(moduleName, "compile.noOutputDir"), null, -1, -1);
        }
        if (commandLine.size() == 0) {
            throw new IllegalArgumentException(getMessage(null, "compile.errorCall"));
        }
        LOG.info("Final compiler command: " + commandLine.toString());
        return commandLine.toArray(new String[0]);
    }

    public static File getMainFile(ParamMap moduleData) {
        String fileName = moduleData != null ? moduleData.get(JpsPascalModuleType.USERDATA_KEY_MAIN_FILE.toString()) : null;
        return fileName != null ? new File(fileName) : null;
    }

    public static String getExeOutputPath(ParamMap moduleData) {
        return moduleData != null ? moduleData.get(JpsPascalModuleType.USERDATA_KEY_EXE_OUTPUT_PATH.toString()) : null;
    }

    protected static void addLibPathToCmdLine(final ArrayList<String> commandLine, File sourceRoot,
                                              final String compilerSettingSrcpath, final String compilerSettingIncpath) {
        if (sourceRoot.isDirectory()) {
            if (compilerSettingSrcpath != null) {
                commandLine.add(compilerSettingSrcpath + sourceRoot.getAbsolutePath());
            }
            if (compilerSettingIncpath != null) {
                commandLine.add(compilerSettingIncpath + sourceRoot.getAbsolutePath());
            }
        }
    }

    protected static String getMessage(String moduleName, @PropertyKey(resourceBundle = JpsPascalBundle.JPSBUNDLE) String msgId, Object... args) {
        return JpsPascalBundle.message(msgId, args) + (moduleName != null ? " (" + JpsPascalBundle.message("general.module", moduleName) + ")" : "");
    }

    protected static File checkCompilerExe(String sdkHomePath, String moduleName, CompilerMessager compilerMessager, File executable, String compilerCommand) {
        if (!StringUtil.isEmpty(compilerCommand)) {
            return checkExecutable(compilerMessager, moduleName, new File(compilerCommand));
        }
        if (sdkHomePath != null) {
            return checkExecutable(compilerMessager, moduleName, executable);
        } else {
            compilerMessager.error(null, getMessage(moduleName, "compile.noSdkHomePath"), null, -1, -1);
            return null;
        }
    }

    private static File checkExecutable(CompilerMessager compilerMessager, String moduleName, File executable) {
        if (executable.exists() && executable.canExecute()) {
            return executable;
        } else {
            compilerMessager.error(null, getMessage(moduleName, "compile.noCompiler", executable.getPath()), null, -1, -1);
            return null;
        }
    }

}
