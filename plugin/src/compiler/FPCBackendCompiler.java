package com.siberika.idea.pascal.compiler;

import com.intellij.compiler.OutputParser;
import com.intellij.compiler.impl.javaCompiler.ExternalCompiler;
import com.intellij.compiler.impl.javaCompiler.ModuleChunk;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.compiler.CompileContext;
import com.intellij.openapi.compiler.CompileScope;
import com.intellij.openapi.compiler.CompilerMessageCategory;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.roots.OrderRootType;
import com.intellij.openapi.roots.ProjectFileIndex;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.vfs.VirtualFile;
import com.siberika.idea.pascal.PascalBundle;
import com.siberika.idea.pascal.PascalFileType;
import com.siberika.idea.pascal.module.PascalModuleType;
import com.siberika.idea.pascal.sdk.BasePascalSdkType;
import com.siberika.idea.pascal.sdk.FPCSdkType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.PropertyKey;

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

    public static final String COMPILER_SETTING_OPATH = "-FE";
    private static final String COMPILER_SETTING_COMMON = "-viewnb";
    private static final String COMPILER_SETTING_SRCPATH = "-Fu";
    private static final String COMPILER_SETTING_INCPATH = "-Fi";
    private static final String COMPILER_SETTING_BUILDALL = "-B";


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
    public String[] createStartupCommand(final ModuleChunk chunk, final CompileContext context, final String outputPath) throws IOException, IllegalArgumentException {
        System.out.println("compiling project with SDK: " + chunk.getJdk().getSdkType().getName());
        for (Module module : chunk.getModules()) {
            System.out.println("compiling module " + module.getName() + " with SDK: " + ModuleRootManager.getInstance(module).getSdk().getSdkType().getName());
        }
        final ArrayList<String> commandLine = new ArrayList<String>();
        final Exception[] ex = new Exception[]{null};
        ApplicationManager.getApplication().runReadAction(new Runnable() {
            public void run() {
                try {
                    for (Module module : chunk.getNodes()) {
                        createStartupCommandImpl(module, context, commandLine, outputPath);
                    }
                    if (commandLine.size() == 0) {
                        throw new IllegalArgumentException(getMessage(null, "compile.errorCall"));
                    }
                } catch (IllegalArgumentException e) {
                    ex[0] = e;
                } catch (IOException e) {
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

    private void createStartupCommandImpl(Module module, CompileContext context, ArrayList<String> commandLine, String outputPath) throws IOException {
        final ModuleRootManager moduleRootManager = ModuleRootManager.getInstance(module);

        final Sdk sdk = moduleRootManager.getSdk();
        if ((null == sdk) || !(sdk.getSdkType() instanceof FPCSdkType)) {
            context.addMessage(CompilerMessageCategory.ERROR, getMessage(module, "compile.wrongSDK"), null, -1, -1);
            return;
        }

        File executable = getCompilerExe(sdk, module, context);
        if (null == executable) return;

        commandLine.add(executable.getPath());

        String compilerOptions = BasePascalSdkType.getAdditionalData(sdk).getValue(BasePascalSdkType.DATA_KEY_COMPILER_OPTIONS);
        if (compilerOptions != null) {
            commandLine.add(compilerOptions);
        }
        commandLine.add(COMPILER_SETTING_COMMON);

        if (context.isRebuild()) {
            commandLine.add(COMPILER_SETTING_BUILDALL);
        }

        VirtualFile outDir = context.getModuleOutputDirectory(module);
        if (outDir == null) {
            context.addMessage(CompilerMessageCategory.ERROR, getMessage(module, "compile.noOutputDir"), null, -1, -1);
            return;
        }
        commandLine.add(COMPILER_SETTING_OPATH + outDir.getPath());

        for (VirtualFile sourceRoot : moduleRootManager.getSourceRoots()) {
            addSourceRootToCmdLine(commandLine, sourceRoot);
        }
        for (VirtualFile sourceRoot : sdk.getRootProvider().getFiles(OrderRootType.SOURCES)) {
            addSourceRootToCmdLine(commandLine, sourceRoot);
        }

        VirtualFile mainFile;

        VirtualFile[] files = context.getCompileScope().getFiles(PascalFileType.INSTANCE, true);

        if (files.length == 1) {
            mainFile = files[0];
        } else {
            mainFile = PascalModuleType.getMainFile(module);
        }
        if (mainFile != null) {
            commandLine.add(mainFile.getPath());
        } else {
            context.addMessage(CompilerMessageCategory.ERROR, getMessage(module, "compile.noSource"), null, -1, -1);
            return;
        }

        StringBuilder sb = new StringBuilder("");
        for (String cmd : commandLine) {
            sb.append(" ").append(cmd);
        }
        context.addMessage(CompilerMessageCategory.INFORMATION, getMessage(module, "compile.commandLine", sb.toString()), null, -1, -1);
    }

    private void addSourceRootToCmdLine(ArrayList<String> commandLine, VirtualFile sourceRoot) {
        String path = sourceRoot.getPath();
        if (path != null) {
            commandLine.add(COMPILER_SETTING_SRCPATH + path);
            commandLine.add(COMPILER_SETTING_INCPATH + path);
        }
    }

    private File getCompilerExe(Sdk sdk, Module module, CompileContext context) {
        File result = null;
        String sdkHomePath = sdk.getHomePath();
        if (sdkHomePath != null) {
            result = FPCSdkType.getCompilerExecutable(sdkHomePath);
            if (!result.canExecute()) {
                context.addMessage(CompilerMessageCategory.ERROR, getMessage(module, "compile.noCompiler", result.getPath()), null, -1, -1);
                result = null;
            }
        } else {
            context.addMessage(CompilerMessageCategory.ERROR, getMessage(module, "compile.noSdkHomePath"), null, -1, -1);
        }
        return result;
    }

    private String getMessage(Module module, @PropertyKey(resourceBundle = PascalBundle.BUNDLE)String msgId, String...args) {
        return PascalBundle.message(msgId, args) + (module != null ? " (" + PascalBundle.message("general.module", module.getName()) + ")" : "");
    }

    public void compileFinished() {

    }

    @Override
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

