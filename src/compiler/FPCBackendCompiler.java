package com.siberika.idea.pascal.compiler;

import com.intellij.compiler.OutputParser;
import com.intellij.compiler.impl.javaCompiler.ExternalCompiler;
import com.intellij.compiler.impl.javaCompiler.ModuleChunk;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.compiler.CompileContext;
import com.intellij.openapi.compiler.CompileScope;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ProjectFileIndex;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.vfs.VirtualFile;
import com.siberika.idea.pascal.PascalBundle;
import com.siberika.idea.pascal.PascalFileType;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Author: George Bakhtadze
 * Date: 1/6/13
 */
public class FPCBackendCompiler extends ExternalCompiler {
    private static final Logger LOG = Logger.getInstance(FPCBackendCompiler.class.getName());

    public static final String FPC_EXECUTABLE = "/usr/lib/codetyphon/fpc/bin/i386-linux/ppc386";
    public static final String FPC_OPTIONS = "-viewnb";

    private final Project myProject;
    private final List<File> myTempFiles = new ArrayList<File>();

    private final static HashSet<FileType> COMPILABLE_FILE_TYPES = new HashSet<FileType>(Arrays.asList(PascalFileType.INSTANCE));

    public FPCBackendCompiler(Project project) {
        myProject = project;
    }

    public boolean checkCompiler(CompileScope scope) {
        VirtualFile[] pascalFiles = scope.getFiles(PascalFileType.INSTANCE, true);
        if (pascalFiles.length == 0) return true;

        final ProjectFileIndex index = ProjectRootManager.getInstance(myProject).getFileIndex();
        Set<Module> modules = new HashSet<Module>();
        for (VirtualFile file : pascalFiles) {
            Module module = index.getModuleForFile(file);
            if (module != null) {
                modules.add(module);
            }
        }

        return true;
    }

    @NotNull
    public String getId() {
        return "FPC";
    }

    @NotNull
    public String getPresentableName() {
        return PascalBundle.message("pascal.compiler.name");
    }

    @NotNull
    public Configurable createConfigurable() {
        return null;
    }

    public OutputParser createOutputParser(@NotNull String outputDir) {
        return new FPCOutputParser();
    }

    @NotNull
    public String[] createStartupCommand(final ModuleChunk chunk, CompileContext context, final String outputPath) throws IOException, IllegalArgumentException {
        final ArrayList<String> commandLine = new ArrayList<String>();
        final Exception[] ex = new Exception[]{null};
        ApplicationManager.getApplication().runReadAction(new Runnable() {
            public void run() {
                try {
                    createStartupCommandImpl(chunk, commandLine, outputPath);
                }
                catch (IllegalArgumentException e) {
                    ex[0] = e;
                }
                catch (IOException e) {
                    ex[0] = e;
                }
            }
        });
        if (ex[0] != null) {
            if (ex[0] instanceof IOException) {
                throw (IOException) ex[0];
            } else if (ex[0] instanceof IllegalArgumentException) {
                throw (IllegalArgumentException) ex[0];
            } else {
                LOG.error(ex[0]);
            }
        }
        return commandLine.toArray(new String[commandLine.size()]);
    }

    @NotNull
    @Override
    public Set<FileType> getCompilableFileTypes() {
        return COMPILABLE_FILE_TYPES;
    }

    private void createStartupCommandImpl(ModuleChunk chunk, ArrayList<String> commandLine, String outputPath) throws IOException {
        commandLine.add(FPC_EXECUTABLE);
        commandLine.add(FPC_OPTIONS);
        commandLine.add("-FE" + outputPath);
        System.out.println("===*** output: " + outputPath);
        for (VirtualFile file : chunk.getFilesToCompile()) {
            if (file.getFileType() == PascalFileType.INSTANCE) {
                commandLine.add(file.getPath());
            }
        }
    }

    public void compileFinished() {

    }

    public OutputParser createErrorParser(@NotNull String outputDir, final Process process) {
        return new FPCOutputParser() {
            AtomicBoolean myDumperStarted = new AtomicBoolean(false);

            @Override
            public boolean processMessageLine(final Callback callback) {
                if (!myDumperStarted.getAndSet(true)) {
                    ApplicationManager.getApplication().executeOnPooledThread(new Runnable() {
                        public void run() {
                            try {
                                process.waitFor();
                            }
                            catch (InterruptedException ignored) {
                            }
                        }
                    });
                }
                return super.processMessageLine(callback);
            }
        };
    }
}

