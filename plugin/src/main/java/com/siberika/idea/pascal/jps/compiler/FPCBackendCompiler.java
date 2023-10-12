package com.siberika.idea.pascal.jps.compiler;

import com.intellij.execution.process.ProcessAdapter;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.siberika.idea.pascal.jps.builder.FPCCompilerProcessAdapter;
import com.siberika.idea.pascal.jps.sdk.PascalSdkData;
import com.siberika.idea.pascal.jps.sdk.PascalSdkUtil;
import com.siberika.idea.pascal.jps.util.FileUtil;
import com.siberika.idea.pascal.jps.util.ParamMap;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Author: George Bakhtadze
 * Date: 1/6/13
 */
public class FPCBackendCompiler extends PascalBackendCompiler {

    private static final String COMPILER_SETTING_OPATH_EXE = "-FE";
    private static final String COMPILER_SETTING_OPATH_UNIT = "-FU";
    private static final String COMPILER_SETTING_COMMON = "-viewhnbq";
    private static final String COMPILER_SETTING_SRCPATH = "-Fu";
    private static final String COMPILER_SETTING_INCPATH = "-Fi";
    private static final String COMPILER_SETTING_BUILDALL = "-B";
    private static final String COMPILER_SETTING_IMPLICIT_UNITS = "-Fa";
    private static final String NAME = "FPC";

    public FPCBackendCompiler(CompilerMessager compilerMessager) {
        super(compilerMessager);
    }

    @NotNull
    public String getId() {
        return NAME;
    }

    @Override
    public ProcessAdapter getCompilerProcessAdapter(CompilerMessager messager) {
        return new FPCCompilerProcessAdapter(messager);
    }

    @Override
    public String getCompiledUnitExt() {
        return ".ppu";
    }

    @Override
    protected boolean createStartupCommandImpl(String sdkHomePath, String moduleName, String outputDirExe, String outputDirUnit,
                                          List<File> sdkFiles, List<File> moduleLibFiles, boolean isRebuild, boolean isDebug,
                                          @Nullable ParamMap pascalSdkData, ArrayList<String> commandLine) {
        String compilerCommand = pascalSdkData != null ? pascalSdkData.get(PascalSdkData.Keys.COMPILER_COMMAND.getKey()) : null;
        File executable = checkCompilerExe(sdkHomePath, moduleName, compilerMessager, PascalSdkUtil.getFPCExecutable(sdkHomePath), compilerCommand);
        if (null == executable) return false;
        commandLine.add(executable.getPath());

        commandLine.add(COMPILER_SETTING_COMMON);

        if (isRebuild) {
            commandLine.add(COMPILER_SETTING_BUILDALL);
        }

        if (StringUtil.isEmpty(outputDirExe)) {
            commandLine.add(COMPILER_SETTING_OPATH_EXE + outputDirUnit);
        } else {
            commandLine.add(COMPILER_SETTING_OPATH_EXE + outputDirExe);
            commandLine.add(COMPILER_SETTING_OPATH_UNIT + outputDirUnit);
        }

        for (File sourceRoot : FileUtil.retrievePaths(moduleLibFiles)) {
            addLibPathToCmdLine(commandLine, sourceRoot, COMPILER_SETTING_SRCPATH, COMPILER_SETTING_INCPATH);
        }

        for (File sdkPath : FileUtil.retrievePaths(sdkFiles)) {
            addLibPathToCmdLine(commandLine, sdkPath, COMPILER_SETTING_SRCPATH, COMPILER_SETTING_INCPATH);
        }

        if (pascalSdkData != null) {
            if (isDebug) {
                String debugOpts = pascalSdkData.get(PascalSdkData.Keys.COMPILER_OPTIONS_DEBUG.getKey());
                if (StringUtil.isNotEmpty(debugOpts)) {
                    Collections.addAll(commandLine, debugOpts.split("\\s+"));
                }
                String implicitUnits = pascalSdkData.get(PascalSdkData.Keys.COMPILER_IMPLICIT_UNITS.getKey());
                String implicitUnitsDir = pascalSdkData.get(PascalSdkData.Keys.COMPILER_IMPLICIT_UNITS_DIR.getKey());
                if (StringUtil.isNotEmpty(implicitUnits) && StringUtil.isNotEmpty(implicitUnitsDir)) {
                    commandLine.add(COMPILER_SETTING_IMPLICIT_UNITS + implicitUnits);
                    addLibPathToCmdLine(commandLine, new File(implicitUnitsDir), COMPILER_SETTING_SRCPATH, null);
                }
            } else {
                String compilerOptions = pascalSdkData.get(PascalSdkData.Keys.COMPILER_OPTIONS.getKey());
                if (StringUtil.isNotEmpty(compilerOptions))
                    Collections.addAll(commandLine, compilerOptions.split("\\s+"));
            }
        }
        return true;
    }

    @Override
    public boolean createSyntaxCheckCommandImpl(String sdkHomePath, String modulePath, PascalSdkData pascalSdkData,
                                                VirtualFile[] sourcePaths, ArrayList<String> commandLine, String tempDir) {
        String compilerCommand = pascalSdkData != null ? pascalSdkData.getString(PascalSdkData.Keys.COMPILER_COMMAND) : null;
        File executable = checkCompilerExe(sdkHomePath, modulePath, compilerMessager, PascalSdkUtil.getFPCExecutable(sdkHomePath), compilerCommand);
        if (null == executable) return false;
        commandLine.add(executable.getPath());

        commandLine.add(COMPILER_SETTING_COMMON);
        commandLine.add("-Cn");
        commandLine.add("-n");
        commandLine.add("-Se100");

        commandLine.add(COMPILER_SETTING_OPATH_EXE + tempDir);
        commandLine.add(COMPILER_SETTING_OPATH_UNIT + tempDir);

        for (File sdkPath : FileUtil.retrievePaths(sourcePaths)) {
            addLibPathToCmdLine(commandLine, sdkPath, COMPILER_SETTING_SRCPATH, COMPILER_SETTING_INCPATH);
        }

        commandLine.add(modulePath);
        
        return true;
    }

}

