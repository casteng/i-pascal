package com.siberika.idea.pascal.jps.compiler;

import com.intellij.execution.process.ProcessAdapter;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.siberika.idea.pascal.jps.JpsPascalBundle;
import com.siberika.idea.pascal.jps.builder.DelphiCompilerProcessAdapter;
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
public class DelphiBackendCompiler extends PascalBackendCompiler {

    public static final String DELPHI_STARTER_RESPONSE = "This version of the product does not support command line compiling";
    private static final String COMPILER_SETTING_OPATH_EXE = "-E";
    private static final String COMPILER_SETTING_OPATH_UNIT = "-N";
    private static final String COMPILER_SETTING_OPATH_UNIT_D2007 = "-N0";
    private static final String COMPILER_SETTING_COMMON = "-H";
    private static final String COMPILER_SETTING_SRCPATH = "-U";
    private static final String COMPILER_SETTING_INCPATH = "-I";
    private static final String COMPILER_SETTING_BUILDALL = "-B";
    private static final String COMPILER_SETTING_BUILDMODIFIED = "-M";
    private static final String COMPILER_SETTING_NAMESPACE = "-NS";
    private static final String NAME = "Delphi";

    public DelphiBackendCompiler(CompilerMessager compilerMessager) {
        super(compilerMessager);
    }

    @NotNull
    public String getId() {
        return NAME;
    }

    @Override
    public ProcessAdapter getCompilerProcessAdapter(CompilerMessager messager) {
        return new DelphiCompilerProcessAdapter(messager);
    }

    @Override
    public String getCompiledUnitExt() {
        return ".dcu";
    }

    private boolean addCompilerCommand(String sdkHomePath, String moduleName, String compilerCommand, ArrayList<String> commandLine) {
        File executable = checkCompilerExe(sdkHomePath, moduleName, compilerMessager, PascalSdkUtil.getDCC32Executable(sdkHomePath), compilerCommand);
        if (null == executable) {
            return false;
        }
        commandLine.add(executable.getPath());
        return true;
    }

    @Override
    protected boolean createStartupCommandImpl(String sdkHomePath, String moduleName, String outputDirExe, String outputDirUnit,
                                          List<File> sdkFiles, List<File> moduleLibFiles, boolean isRebuild, boolean isDebug,
                                          @Nullable ParamMap pascalSdkData, ArrayList<String> commandLine) {
        if (!addCompilerCommand(sdkHomePath, moduleName, pascalSdkData != null ? pascalSdkData.get(PascalSdkData.Keys.COMPILER_COMMAND.getKey()) : null, commandLine)) {
            return false;
        }

        if (pascalSdkData != null) {
            String[] compilerOptions = pascalSdkData.get(PascalSdkData.Keys.COMPILER_OPTIONS.getKey()).split("\\s+");
            Collections.addAll(commandLine, compilerOptions);
            addNamespaces(pascalSdkData.get(PascalSdkData.Keys.COMPILER_NAMESPACES.getKey()), commandLine);
        }
        commandLine.add(COMPILER_SETTING_COMMON);

        if (isRebuild) {
            commandLine.add(COMPILER_SETTING_BUILDALL);
        } else {
            commandLine.add(COMPILER_SETTING_BUILDMODIFIED);
        }

        commandLine.add(COMPILER_SETTING_OPATH_UNIT + outputDirUnit);
        commandLine.add(COMPILER_SETTING_OPATH_UNIT_D2007 + outputDirUnit);
        if (StringUtil.isEmpty(outputDirExe)) {
            commandLine.add(COMPILER_SETTING_OPATH_EXE + outputDirUnit);
        } else {
            commandLine.add(COMPILER_SETTING_OPATH_EXE + outputDirExe);
        }

        for (File sourceRoot : FileUtil.retrievePaths(moduleLibFiles)) {
            addLibPathToCmdLine(commandLine, sourceRoot, COMPILER_SETTING_SRCPATH, COMPILER_SETTING_INCPATH);
        }

        for (File sdkPath : FileUtil.retrievePaths(sdkFiles)) {
            if (isFromRTL(sdkHomePath, sdkPath)) {
                compilerMessager.info(null, JpsPascalBundle.message("compile.skipRTL") + " " + sdkPath.getPath(), null, -1, -1);
            } else {
                addLibPathToCmdLine(commandLine, sdkPath, COMPILER_SETTING_SRCPATH, COMPILER_SETTING_INCPATH);
            }
        }
        return true;
    }

    @Override
    public boolean createSyntaxCheckCommandImpl(String sdkHomePath, String modulePath, PascalSdkData pascalSdkData,
                                                VirtualFile[] sourcePaths, ArrayList<String> commandLine, String tempDir) {
        if (!addCompilerCommand(sdkHomePath, modulePath, pascalSdkData != null ? pascalSdkData.getString(PascalSdkData.Keys.COMPILER_COMMAND) : null, commandLine)) {
            return false;
        }
        addNamespaces(pascalSdkData != null ? pascalSdkData.getString(PascalSdkData.Keys.COMPILER_NAMESPACES) : null, commandLine);

        commandLine.add(COMPILER_SETTING_COMMON);
        commandLine.add("-W");
        commandLine.add("--no-config");

        commandLine.add(COMPILER_SETTING_OPATH_EXE + tempDir);
        commandLine.add(COMPILER_SETTING_OPATH_UNIT + tempDir);

        for (File sdkPath : FileUtil.retrievePaths(sourcePaths)) {
            if (isFromRTL(sdkHomePath, sdkPath)) {
                compilerMessager.info(null, JpsPascalBundle.message("compile.skipRTL") + " " + sdkPath.getPath(), null, -1, -1);
            } else {
                addLibPathToCmdLine(commandLine, sdkPath, COMPILER_SETTING_SRCPATH, COMPILER_SETTING_INCPATH);
            }
        }

        commandLine.add(modulePath);

        return true;
    }

    private void addNamespaces(String namespaces, ArrayList<String> commandLine) {
        if (StringUtil.isNotEmpty(namespaces)) {
            for (String namespace : namespaces.split("[;,]")) {
                commandLine.add(COMPILER_SETTING_NAMESPACE + namespace);
            }
        }
    }

    private boolean isFromRTL(String sdkHomePath, File file) {
        String path = file.getPath();
        return path.toUpperCase().startsWith(sdkHomePath + PascalSdkUtil.DELPHI_RTL_PATH_CONST);
    }

}

