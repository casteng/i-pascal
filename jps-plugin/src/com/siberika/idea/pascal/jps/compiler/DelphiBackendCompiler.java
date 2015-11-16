package com.siberika.idea.pascal.jps.compiler;

import com.intellij.execution.process.ProcessAdapter;
import com.siberika.idea.pascal.jps.builder.DelphiCompilerProcessAdapter;
import com.siberika.idea.pascal.jps.sdk.PascalSdkData;
import com.siberika.idea.pascal.jps.sdk.PascalSdkUtil;
import com.siberika.idea.pascal.jps.util.FileUtil;
import com.siberika.idea.pascal.jps.util.ParamMap;
import org.apache.commons.lang.StringUtils;
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

    private static final String COMPILER_SETTING_OPATH_EXE = "-E";
    private static final String COMPILER_SETTING_OPATH_UNIT = "-N";
    private static final String COMPILER_SETTING_OPATH_UNIT_D2007 = "-N0";
    private static final String COMPILER_SETTING_COMMON = "";
    private static final String COMPILER_SETTING_SRCPATH = "-U";
    private static final String COMPILER_SETTING_INCPATH = "-I";
    private static final String COMPILER_SETTING_BUILDALL = "-B";
    private static final String COMPILER_SETTING_BUILDMODIFIED = "-M";
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

    @Override
    protected void createStartupCommandImpl(String sdkHomePath, String moduleName, String outputDirExe, String outputDirUnit,
                                          List<File> sdkFiles, List<File> moduleLibFiles, boolean isRebuild,
                                          @Nullable ParamMap pascalSdkData, ArrayList<String> commandLine) {
        File executable = checkCompilerExe(sdkHomePath, moduleName, compilerMessager, PascalSdkUtil.getDCC32Executable(sdkHomePath));
        if (null == executable) return;
        commandLine.add(executable.getPath());

        if (pascalSdkData != null) {
            String[] compilerOptions = pascalSdkData.get(PascalSdkData.DATA_KEY_COMPILER_OPTIONS).split("\\s+");
            Collections.addAll(commandLine, compilerOptions);
        }
        commandLine.add(COMPILER_SETTING_COMMON);

        if (isRebuild) {
            commandLine.add(COMPILER_SETTING_BUILDALL);
        } else {
            commandLine.add(COMPILER_SETTING_BUILDMODIFIED);
        }

        commandLine.add(COMPILER_SETTING_OPATH_UNIT + outputDirUnit);
        commandLine.add(COMPILER_SETTING_OPATH_UNIT_D2007 + outputDirUnit);
        if (StringUtils.isEmpty(outputDirExe)) {
            commandLine.add(COMPILER_SETTING_OPATH_EXE + outputDirUnit);
        } else {
            commandLine.add(COMPILER_SETTING_OPATH_EXE + outputDirExe);
        }

        for (File sourceRoot : FileUtil.retrievePaths(moduleLibFiles)) {
            addLibPathToCmdLine(commandLine, sourceRoot, COMPILER_SETTING_SRCPATH, COMPILER_SETTING_INCPATH);
        }

        for (File sdkPath : FileUtil.retrievePaths(sdkFiles)) {
            addLibPathToCmdLine(commandLine, sdkPath, COMPILER_SETTING_SRCPATH, COMPILER_SETTING_INCPATH);
        }
    }

}

