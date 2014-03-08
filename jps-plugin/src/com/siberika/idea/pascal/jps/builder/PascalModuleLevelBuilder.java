package com.siberika.idea.pascal.jps.builder;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.jps.ModuleChunk;
import org.jetbrains.jps.builders.DirtyFilesHolder;
import org.jetbrains.jps.builders.java.JavaSourceRootDescriptor;
import org.jetbrains.jps.incremental.BuilderCategory;
import org.jetbrains.jps.incremental.CompileContext;
import org.jetbrains.jps.incremental.ModuleBuildTarget;
import org.jetbrains.jps.incremental.ModuleLevelBuilder;
import org.jetbrains.jps.incremental.ProjectBuildException;
import org.jetbrains.jps.incremental.messages.BuildMessage;
import org.jetbrains.jps.incremental.messages.CompilerMessage;
import org.jetbrains.jps.model.java.JpsJavaSdkType;
import org.jetbrains.jps.model.library.sdk.JpsSdk;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

/**
 * Author: George Bakhtadze
 * Date: 11/02/2014
 */
public class PascalModuleLevelBuilder extends ModuleLevelBuilder {

    private static final List<String> COMPILABLE_EXTENSIONS = Arrays.asList("pas", "inc", "dpr", "pp");

    public PascalModuleLevelBuilder() {
        super(BuilderCategory.TRANSLATOR);
    }

    @Override
    public ExitCode build(CompileContext context,
                          ModuleChunk chunk,
                          DirtyFilesHolder<JavaSourceRootDescriptor, ModuleBuildTarget> dirtyFilesHolder,
                          OutputConsumer outputConsumer) throws ProjectBuildException, IOException {

        context.processMessage(new CompilerMessage(getPresentableName(), BuildMessage.Kind.WARNING, "Starting compile..."));
        JpsSdk<?> sdk = chunk.getModules().iterator().next().getSdk(JpsJavaSdkType.INSTANCE);
        final List<File> toCompile = collectChangedFiles(context, dirtyFilesHolder);
        for (File file : toCompile) {
            context.processMessage(new CompilerMessage(getPresentableName(), BuildMessage.Kind.WARNING, "compiling: " + file.getAbsolutePath()));
        }
        context.processMessage(new CompilerMessage(getPresentableName(), BuildMessage.Kind.ERROR, "Error message 7"));
        return ExitCode.OK;
    }

    @Override
    public List<String> getCompilableFileExtensions() {
        return COMPILABLE_EXTENSIONS;
    }

    private static List<File> collectChangedFiles(CompileContext context,
                                                  DirtyFilesHolder<JavaSourceRootDescriptor, ModuleBuildTarget> dirtyFilesHolder) throws IOException {
        final ResourcePatterns patterns = ResourcePatterns.KEY.get(context);
        assert patterns != null;
        final List<File> toCompile = new ArrayList<File>();
        dirtyFilesHolder.processDirtyFiles(new FileProcessor<JavaSourceRootDescriptor, ModuleBuildTarget>() {
            public boolean apply(ModuleBuildTarget target, File file, JavaSourceRootDescriptor sourceRoot) throws IOException {
                final String path = file.getPath();
                if (isGroovyFile(path) && !patterns.isResourceFile(file, sourceRoot.root)) { //todo file type check
                    toCompile.add(file);
                }
                return true;
            }
        });
        return toCompile;
    }

    private static boolean isGroovyFile(String path) {
        for (String ext : COMPILABLE_EXTENSIONS) {
            if (path.endsWith("." + ext)) {
                return true;
            }
        }
        return false;
    }

    @NotNull
    @Override
    public String getPresentableName() {
        return "Free Pascal Compiler";
    }
}
