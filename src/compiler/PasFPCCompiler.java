package com.siberika.idea.pascal.compiler;

import com.intellij.compiler.CompilerException;
import com.intellij.compiler.OutputParser;
import com.intellij.compiler.impl.CompilerUtil;
import com.intellij.compiler.impl.javaCompiler.BackendCompiler;
import com.intellij.compiler.impl.javaCompiler.BackendCompilerWrapper;
import com.intellij.compiler.impl.javaCompiler.CompilerParsingThread;
import com.intellij.compiler.impl.javaCompiler.ModuleChunk;
import com.intellij.compiler.make.CacheCorruptedException;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.compiler.CompileContext;
import com.intellij.openapi.compiler.CompileScope;
import com.intellij.openapi.compiler.CompilerBundle;
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

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;

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
        compileNative(context, moduleChunk, files, sink);
    }

    public void compileJvm(CompileContext context, Chunk<Module> moduleChunk, VirtualFile[] files, OutputSink sink) {
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

    public void compileNative(CompileContext context, Chunk<Module> chunk, VirtualFile[] files, OutputSink outputSink) {
        context.getProgressIndicator().checkCanceled();
        context.getProgressIndicator().pushState();
        context.getProgressIndicator().setText("Upper text");
        context.getProgressIndicator().setText2("Bottom text");

        BackendCompiler myCompiler = getBackEndCompiler();

        Map<Module, List<VirtualFile>> moduleFileMap;
        if (chunk.getNodes().size() == 1) {
            moduleFileMap = Collections.singletonMap(chunk.getNodes().iterator().next(), Collections.unmodifiableList(Arrays.asList(files)));
        }
        moduleFileMap = CompilerUtil.buildModuleToFilesMap(context, files);

        ModuleChunk moduleChunk = new ModuleChunk((CompileContextEx)context, chunk, moduleFileMap);

        int exitValue = 0;
        try {
            final Process process = myCompiler.launchProcess(moduleChunk, "", context);
            final long compilationStart = System.currentTimeMillis();

            OutputParser errorParser = myCompiler.createErrorParser("", process);
            CompilerParsingThread errorParsingThread = errorParser == null
                    ? null
                    : new SynchedCompilerParsing(process, context, errorParser,
                    true, errorParser.isTrimLines());
            Future<?> errorParsingThreadFuture = null;
            if (errorParsingThread != null) {
                errorParsingThreadFuture = ApplicationManager.getApplication().executeOnPooledThread(errorParsingThread);
            }

            OutputParser outputParser = myCompiler.createOutputParser("");
            CompilerParsingThread outputParsingThread = outputParser == null
                    ? null
                    : new SynchedCompilerParsing(process, context, outputParser,
                    false, outputParser.isTrimLines());
            Future<?> outputParsingThreadFuture = null;
            if (outputParsingThread != null) {
                outputParsingThreadFuture = ApplicationManager.getApplication().executeOnPooledThread(outputParsingThread);
            }

            try {
                exitValue = process.waitFor();
            }
            catch (InterruptedException e) {
                process.destroy();
                exitValue = process.exitValue();
            }
            catch (Error e) {
                process.destroy();
                exitValue = process.exitValue();
                throw e;
            }
            finally {
                System.out.println("Compiler exit code is " + exitValue);
                if (errorParsingThread != null) {
                    errorParsingThread.setProcessTerminated(true);
                }
                if (outputParsingThread != null) {
                    outputParsingThread.setProcessTerminated(true);
                }
                joinThread(errorParsingThreadFuture);
                joinThread(outputParsingThreadFuture);

                registerParsingException(context, outputParsingThread);
                registerParsingException(context, errorParsingThread);
            }
        } catch (IOException e) {
            LOG.error(e);
        } catch (IllegalArgumentException e) {
            LOG.error(e);
        } finally {
            if (exitValue != 0 && !context.getProgressIndicator().isCanceled() && context.getMessageCount(CompilerMessageCategory.ERROR) == 0) {
                context.addMessage(CompilerMessageCategory.ERROR, CompilerBundle.message("error.compiler.internal.error", exitValue), null, -1, -1);
            }
        }
    }

    private static void joinThread(final Future<?> threadFuture) {
        if (threadFuture != null) {
            try {
                threadFuture.get();
            }
            catch (InterruptedException ignored) {
                LOG.info("Thread interrupted", ignored);
            }
            catch (java.util.concurrent.ExecutionException ignored) {
                LOG.info("Thread interrupted", ignored);
            }
        }
    }

    private void registerParsingException(CompileContext context, final CompilerParsingThread outputParsingThread) {
        Throwable error = outputParsingThread == null ? null : outputParsingThread.getError();
        if (error != null) {
            String message = error.getMessage();
            context.addMessage(CompilerMessageCategory.ERROR, message, null, -1, -1);
        }
    }

    private final Object lock = new Object();

    private class SynchedCompilerParsing extends CompilerParsingThread {
        private SynchedCompilerParsing(Process process,
                                       final CompileContext context,
                                       OutputParser outputParser,
                                       boolean readErrorStream,
                                       boolean trimLines) {
            super(process, outputParser, readErrorStream, trimLines,context);
        }

        public void setProgressText(String text) {
            synchronized (lock) {
                super.setProgressText(text);
            }
        }

        public void message(CompilerMessageCategory category, String message, String url, int lineNum, int columnNum) {
            synchronized (lock) {
                super.message(category, message, url, lineNum, columnNum);
            }
        }

    }
}
