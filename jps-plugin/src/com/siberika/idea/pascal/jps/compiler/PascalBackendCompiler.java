package com.siberika.idea.pascal.jps.compiler;

import com.intellij.execution.process.ProcessAdapter;
import com.intellij.openapi.util.text.StringUtil;
import com.siberika.idea.pascal.jps.JpsPascalBundle;
import com.siberika.idea.pascal.jps.model.JpsPascalModuleType;
import com.siberika.idea.pascal.jps.util.ParamMap;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.PropertyKey;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Author: George Bakhtadze
 * Date: 15/05/2015
 */
public abstract class PascalBackendCompiler {

    protected final CompilerMessager compilerMessager;

    public PascalBackendCompiler(CompilerMessager compilerMessager) {
        this.compilerMessager = compilerMessager;
    }

    abstract protected boolean createStartupCommandImpl(String sdkHomePath, String moduleName, String outputDirExe, String outputDirUnit,
                                          List<File> sdkFiles, List<File> moduleLibFiles, boolean isRebuild, boolean isDebug,
                                          @Nullable ParamMap pascalSdkData, ArrayList<String> commandLine);
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
                compilerMessager.error(getMessage(moduleName, "compile.noSource"), null, -1, -1);
            }

            StringBuilder sb = new StringBuilder();
            for (String cmd : commandLine) {
                sb.append(" ").append(cmd);
            }
            compilerMessager.info(getMessage(moduleName, "compile.commandLine", sb.toString()), null, -1, -1);
        } else {
            compilerMessager.error(getMessage(moduleName, "compile.noOutputDir"), null, -1, -1);
        }
        if (commandLine.size() == 0) {
            throw new IllegalArgumentException(getMessage(null, "compile.errorCall"));
        }
        return commandLine.toArray(new String[commandLine.size()]);
    }

    public static File getMainFile(ParamMap moduleData) {
        String fileName = moduleData != null ? moduleData.get(JpsPascalModuleType.USERDATA_KEY_MAIN_FILE.toString()) : null;
        return fileName != null ? new File(fileName) : null;
    }

    static String getExeOutputPath(ParamMap moduleData) {
        return moduleData != null ? moduleData.get(JpsPascalModuleType.USERDATA_KEY_EXE_OUTPUT_PATH.toString()) : null;
    }

    protected static void addLibPathToCmdLine(final ArrayList<String> commandLine, File sourceRoot,
                                              final String compilerSettingSrcpath, final String compilerSettingIncpath) {
        if (sourceRoot.isDirectory()) {
            commandLine.add(compilerSettingSrcpath + sourceRoot.getAbsolutePath());
            commandLine.add(compilerSettingIncpath + sourceRoot.getAbsolutePath());
        }
    }

    protected static String getMessage(String moduleName, @PropertyKey(resourceBundle = JpsPascalBundle.JPSBUNDLE)String msgId, Object...args) {
        return JpsPascalBundle.message(msgId, args) + (moduleName != null ? " (" + JpsPascalBundle.message("general.module", moduleName) + ")" : "");
    }

    protected static File checkCompilerExe(String sdkHomePath, String moduleName, CompilerMessager compilerMessager, File executable, String compilerCommand) {
        if (!StringUtil.isEmpty(compilerCommand)) {
            return checkExecutable(compilerMessager, moduleName, new File(compilerCommand));
        }
        if (sdkHomePath != null) {
            return checkExecutable(compilerMessager, moduleName, executable);
        } else {
            compilerMessager.error(getMessage(moduleName, "compile.noSdkHomePath"), null, -1, -1);
            return null;
        }
    }

    private static File checkExecutable(CompilerMessager compilerMessager, String moduleName, File executable) {
        if (executable.exists() && executable.canExecute()) {
            return executable;
        } else {
            compilerMessager.error(getMessage(moduleName, "compile.noCompiler", executable.getPath()), null, -1, -1);
            return null;
        }
    }

}
