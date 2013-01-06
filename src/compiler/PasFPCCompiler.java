package com.siberika.idea.pascal.compiler;

import com.intellij.compiler.CompilerException;
import com.intellij.compiler.impl.javaCompiler.BackendCompiler;
import com.intellij.compiler.impl.javaCompiler.BackendCompilerWrapper;
import com.intellij.compiler.make.CacheCorruptedException;
import com.intellij.openapi.compiler.CompileContext;
import com.intellij.openapi.compiler.CompileScope;
import com.intellij.openapi.compiler.CompilerMessageCategory;
import com.intellij.openapi.compiler.TranslatingCompiler;
import com.intellij.openapi.compiler.ex.CompileContextEx;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.fileTypes.FileTypeManager;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.Chunk;
import com.siberika.idea.pascal.PascalFileType;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;

/**
 * Author: George Bakhtadze
 * Date: 1/5/13
 */
public class PasFPCCompiler implements TranslatingCompiler {
    private static final Logger LOG = Logger.getInstance(TranslatingCompiler.class.getName());

    private final Project project;

    public PasFPCCompiler(Project project) {
        this.project = project;
    }

    @Override
    public boolean isCompilableFile(VirtualFile file, CompileContext context) {
        return PascalFileType.INSTANCE.equals(FileTypeManager.getInstance().getFileTypeByFile(file));
    }

    @Override
    public void compile(CompileContext context, Chunk<Module> moduleChunk, VirtualFile[] files, OutputSink sink) {
        System.out.println("===*** compile");

        final BackendCompiler backEndCompiler = getBackEndCompiler();

        final BackendCompilerWrapper wrapper = new BackendCompilerWrapper(moduleChunk, project, Arrays.asList(files),
                (CompileContextEx) context, backEndCompiler, sink);
        try {
            wrapper.compile();
        }
        catch (CompilerException e) {
            context.addMessage(CompilerMessageCategory.ERROR, e.getMessage(), null, -1, -1);
        }
        catch (CacheCorruptedException e) {
            LOG.info(e);
            context.requestRebuildNextTime(e.getMessage());
        }

        System.out.println("===*** compilation finished ");
    }

    @NotNull
    @Override
    public String getDescription() {
        return "FreePascal based Object Pascal compiler for IDEA";
    }

    @Override
    public boolean validateConfiguration(CompileScope scope) {
        return true;
    }

    private BackendCompiler getBackEndCompiler() {
        return new FPCBackendCompiler(project);
    }
}
