package com.siberika.idea.pascal.jps.compiler;

import com.intellij.execution.process.ProcessAdapter;
import com.siberika.idea.pascal.jps.util.ParamMap;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * Author: George Bakhtadze
 * Date: 15/05/2015
 */
public interface PascalBackendCompiler {
    String[] createStartupCommand(final String sdkHomePath, final String moduleName, final String outputDir,
                                  final List<File> sdkLibFiles, final List<File> moduleLibFiles,
                                  final List<File> files, @Nullable final ParamMap moduleData,
                                  final boolean isRebuild,
                                  @Nullable final ParamMap pascalSdkData) throws IOException, IllegalArgumentException;

    @NotNull
    String getId();

    ProcessAdapter getCompilerProcessAdapter(CompilerMessager messager);
}
