package com.siberika.idea.pascal.jps.compiler;

import com.intellij.execution.process.ProcessAdapter;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.util.Processor;
import com.siberika.idea.pascal.jps.JpsPascalBundle;
import com.siberika.idea.pascal.jps.model.JpsPascalModuleType;
import com.siberika.idea.pascal.jps.sdk.PascalSdkData;
import com.siberika.idea.pascal.jps.sdk.PascalSdkUtil;
import com.siberika.idea.pascal.jps.util.ParamMap;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.PropertyKey;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Author: George Bakhtadze
 * Date: 1/6/13
 */
public class DelphiBackendCompiler implements PascalBackendCompiler {

    public static final String COMPILER_SETTING_OPATH = "-FE";
    private static final String COMPILER_SETTING_COMMON = "-viewnb";
    private static final String COMPILER_SETTING_SRCPATH = "-Fu";
    private static final String COMPILER_SETTING_INCPATH = "-Fi";
    private static final String COMPILER_SETTING_BUILDALL = "-B";
    private static final String NAME = "Delphi";

    private final CompilerMessager compilerMessager;

    public DelphiBackendCompiler(CompilerMessager compilerMessager) {
        this.compilerMessager = compilerMessager;
    }

    @NotNull
    public String getId() {
        return NAME;
    }

    @Override
    public ProcessAdapter getCompilerProcessAdapter(CompilerMessager messager) {
        return null;
    }

    @Override
    public String getCompiledUnitExt() {
        return ".dcu";
    }

    @NotNull
    public String[] createStartupCommand(final String sdkHomePath, final String moduleName, final String outputDir,
                                                final List<File> sdkLibFiles, final List<File> moduleLibFiles,
                                                final List<File> files, @Nullable final ParamMap moduleData,
                                                final boolean isRebuild,
                                                @Nullable final ParamMap pascalSdkData) throws IOException, IllegalArgumentException {

        final ArrayList<String> commandLine = new ArrayList<String>();
        createStartupCommandImpl(sdkHomePath, moduleName, outputDir,
                sdkLibFiles, moduleLibFiles,
                files, getMainFile(moduleData),
                isRebuild,
                pascalSdkData, commandLine);
        if (commandLine.size() == 0) {
            throw new IllegalArgumentException(getMessage(null, "compile.errorCall"));
        }
        return commandLine.toArray(new String[commandLine.size()]);
    }

    private File getMainFile(ParamMap moduleData) {
        String fileName = moduleData != null ? moduleData.get(JpsPascalModuleType.USERDATA_KEY_MAIN_FILE.toString()) : null;
        return fileName != null ? new File(fileName) : null;
    }

    private void createStartupCommandImpl(String sdkHomePath, String moduleName, String outputDir,
                                          List<File> sdkFiles, List<File> moduleLibFiles,
                                          List<File> files, @Nullable File mainFile,
                                          boolean isRebuild,
                                          @Nullable ParamMap pascalSdkData, ArrayList<String> commandLine) {
        File executable = getCompilerExe(sdkHomePath, moduleName, compilerMessager);
        if (null == executable) return;
        commandLine.add(executable.getPath());

        if (pascalSdkData != null) {
            String[] compilerOptions = pascalSdkData.get(PascalSdkData.DATA_KEY_COMPILER_OPTIONS).split("\\s+");
            Collections.addAll(commandLine, compilerOptions);
        }
        commandLine.add(COMPILER_SETTING_COMMON);

        if (isRebuild) {
            commandLine.add(COMPILER_SETTING_BUILDALL);
        }

        if (outputDir == null) {
            compilerMessager.error(getMessage(moduleName, "compile.noOutputDir"), null, -1, -1);
            return;
        }
        commandLine.add(COMPILER_SETTING_OPATH + outputDir);

        for (File sourceRoot : retrievePaths(moduleLibFiles)) {
            addLibPathToCmdLine(commandLine, sourceRoot);
        }

        for (File sdkPath : retrievePaths(sdkFiles)) {
            addLibPathToCmdLine(commandLine, sdkPath);
        }

        if (files.size() == 1) {
            mainFile = files.get(0);
        }
        if (mainFile != null) {
            commandLine.add(mainFile.getAbsolutePath());
        } else {
            compilerMessager.error(getMessage(moduleName, "compile.noSource"), null, -1, -1);
            return;
        }

        StringBuilder sb = new StringBuilder();
        for (String cmd : commandLine) {
            sb.append(" ").append(cmd);
        }
        compilerMessager.info(getMessage(moduleName, "compile.commandLine", sb.toString()), null, -1, -1);
    }

    private static Set<File> retrievePaths(List<File> files) {
        Set<File> result = new HashSet<File>();
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    result.add(file);
                } else {
                    result.add(file.getParentFile());
                }
            }
        }
        return result;
    }

    private static void addLibPathToCmdLine(final ArrayList<String> commandLine, File sourceRoot) {
        FileUtil.processFilesRecursively(sourceRoot, new Processor<File>() {
            @Override
            public boolean process(File file) {
                if (file.isDirectory()) {
                    commandLine.add(COMPILER_SETTING_SRCPATH + file.getAbsolutePath());
                    commandLine.add(COMPILER_SETTING_INCPATH + file.getAbsolutePath());
                }
                return true;
            }
        });
    }

    private static File getCompilerExe(String sdkHomePath, String moduleName, CompilerMessager compilerMessager) {
        File result = null;
        if (sdkHomePath != null) {
            result = PascalSdkUtil.getFPCExecutable(sdkHomePath);
            if (!result.canExecute()) {
                compilerMessager.error(getMessage(moduleName, "compile.noCompiler", result.getPath()), null, -1l, -1l);
                result = null;
            }
        } else {
            compilerMessager.error(getMessage(moduleName, "compile.noSdkHomePath"), null, -1l, -1l);
        }
        return result;
    }

    private static String getMessage(String moduleName, @PropertyKey(resourceBundle = JpsPascalBundle.JPSBUNDLE)String msgId, String...args) {
        return JpsPascalBundle.message(msgId, args) + (moduleName != null ? " (" + JpsPascalBundle.message("general.module", moduleName) + ")" : "");
    }

}

