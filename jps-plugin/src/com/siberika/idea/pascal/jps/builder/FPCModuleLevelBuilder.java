package com.siberika.idea.pascal.jps.builder;

import com.intellij.execution.process.BaseOSProcessHandler;
import com.intellij.execution.process.ProcessAdapter;
import com.intellij.openapi.util.io.FileUtil;
import com.siberika.idea.pascal.jps.compiler.CompilerMessager;
import com.siberika.idea.pascal.jps.compiler.FPCBackendCompiler;
import com.siberika.idea.pascal.jps.model.JpsPascalSdkType;
import com.siberika.idea.pascal.jps.util.ParamMap;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.jps.ModuleChunk;
import org.jetbrains.jps.builders.DirtyFilesHolder;
import org.jetbrains.jps.builders.FileProcessor;
import org.jetbrains.jps.builders.java.JavaBuilderUtil;
import org.jetbrains.jps.builders.java.JavaSourceRootDescriptor;
import org.jetbrains.jps.incremental.BuilderCategory;
import org.jetbrains.jps.incremental.CompileContext;
import org.jetbrains.jps.incremental.ModuleBuildTarget;
import org.jetbrains.jps.incremental.ModuleLevelBuilder;
import org.jetbrains.jps.incremental.ProjectBuildException;
import org.jetbrains.jps.incremental.messages.BuildMessage;
import org.jetbrains.jps.incremental.messages.CompilerMessage;
import org.jetbrains.jps.model.java.JpsJavaExtensionService;
import org.jetbrains.jps.model.library.JpsOrderRootType;
import org.jetbrains.jps.model.library.sdk.JpsSdk;
import org.jetbrains.jps.model.module.JpsModule;
import org.jetbrains.jps.model.module.JpsModuleSourceRoot;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Author: George Bakhtadze
 * Date: 11/02/2014
 */
public class FPCModuleLevelBuilder extends ModuleLevelBuilder {

    private static final List<String> COMPILABLE_EXTENSIONS = Arrays.asList("pas", "inc", "dpr", "pp", "lpr");
    public static final String NAME = "FPC";
    private static final String FILE_EXT_PPU = ".ppu";

    public FPCModuleLevelBuilder() {
        super(BuilderCategory.OVERWRITING_TRANSLATOR);
    }

    @Override
    public ExitCode build(final CompileContext context, ModuleChunk chunk,
                          DirtyFilesHolder<JavaSourceRootDescriptor, ModuleBuildTarget> dirtyFilesHolder,
                          OutputConsumer outputConsumer) throws ProjectBuildException, IOException {
//        context.processMessage(new CompilerMessage(getPresentableName(), BuildMessage.Kind.ERROR, "Don't close messages"));
        final Map<ModuleBuildTarget, List<File>> files = collectChangedFiles(context, dirtyFilesHolder);
        if (files.isEmpty()) {
            context.processMessage(new CompilerMessage(getPresentableName(), BuildMessage.Kind.INFO, "No changes detected"));
            return ExitCode.NOTHING_DONE;
        }

        CompilerMessager messager = new CompilerMessager() {
            @Override
            public void info(String msg, String path, long line, long column) {
                context.processMessage(new CompilerMessage(getPresentableName(), BuildMessage.Kind.INFO, msg, path, -1l, -1l, -1l, line, column));
            }

            @Override
            public void warning(String msg, String path, long line, long column) {
                context.processMessage(new CompilerMessage(getPresentableName(), BuildMessage.Kind.WARNING, msg, path, -1l, -1l, -1l, line, column));
            }

            @Override
            public void error(String msg, String path, long line, long column) {
                context.processMessage(new CompilerMessage(getPresentableName(), BuildMessage.Kind.ERROR, msg, path, -1l, -1l, -1l, line, column));
            }
        };

        FPCBackendCompiler compiler = new FPCBackendCompiler(messager);
        for (ModuleBuildTarget target : chunk.getTargets()) {
            JpsModule module = target.getModule();
            JpsSdk<?> sdk = module.getSdk(JpsPascalSdkType.INSTANCE);
            if (sdk != null) {
                List<File> sdkFiles = sdk.getParent().getFiles(JpsOrderRootType.COMPILED);
                sdkFiles.addAll(sdk.getParent().getFiles(JpsOrderRootType.SOURCES));
                File outputDir = getBuildOutputDirectory(module, target.isTests(), context);

                for (File file : files.get(target)) {
                    File compiled = new File(outputDir, FileUtil.getNameWithoutExtension(file) + FILE_EXT_PPU);
                    messager.info(String.format("Map: %s => %s ", file.getCanonicalPath(), compiled.getCanonicalPath()), null, -1l, -1l);
                    outputConsumer.registerOutputFile(chunk.representativeTarget(), compiled, Collections.singleton(file.getCanonicalPath()));
                }

                String[] cmdLine = compiler.createStartupCommand(sdk.getHomePath(), module.getName(), outputDir.getAbsolutePath(),
                        sdkFiles, getFiles(module.getSourceRoots()),
                        files.get(target), ParamMap.getJpsParams(module.getProperties()),
                        JavaBuilderUtil.isForcedRecompilationAllJavaModules(context),
                        ParamMap.getJpsParams(sdk.getSdkProperties()));
                int exitCode = launchCompiler(messager, cmdLine);
                if (exitCode != 0) {
                    messager.error("Error. Compiler exit code: " + exitCode, null, -1l, -1l);
                    return ExitCode.ABORT;
                }
            } else {
                log(context, "Pascal SDK is not defined for module " + module.getName());
            }
            /*public String[] createStartupCommand(final String sdkHomePath, final String moduleName, final String outputDir,
            final VirtualFile[] sdkSourceRoots, final VirtualFile[] moduleSourceRoots,
            final VirtualFile[] files, final VirtualFile mainFile,
            final boolean isRebuild,
            final PascalSdkData pascalSdkData) throws IOException, IllegalArgumentException {*/
        }

        return ExitCode.OK;
    }

    private int launchCompiler(CompilerMessager messager, String[] cmdLine) throws IOException {
        messager.info("Command line: ", null, -1l, -1l);
        for (String s : cmdLine) {
            messager.info(s, null, -1l, -1l);
        }
        Process process = Runtime.getRuntime().exec(cmdLine);
        BaseOSProcessHandler handler = new BaseOSProcessHandler(process, "", Charset.defaultCharset());
        ProcessAdapter adapter = new FPCCompilerProcessAdapter(messager);
        handler.addProcessListener(adapter);
        handler.startNotify();
        handler.waitFor();
        return process.exitValue();
    }

    private List<File> getFiles(List<JpsModuleSourceRoot> sourceRoots) {
        List<File> result = new ArrayList<File>();
        for (JpsModuleSourceRoot root : sourceRoots) {
            result.add(root.getFile());
        }
        return result;
    }

    private static File getBuildOutputDirectory(@NotNull JpsModule module, boolean forTests,
                                                @NotNull CompileContext context) throws ProjectBuildException {
        JpsJavaExtensionService instance = JpsJavaExtensionService.getInstance();
        File outputDirectory = instance.getOutputDirectory(module, forTests);
        if (outputDirectory == null) {
            context.processMessage(new CompilerMessage(NAME, BuildMessage.Kind.ERROR, "No output dir for module " + module.getName()));
        } else {
            if (!outputDirectory.exists()) {
                FileUtil.createDirectory(outputDirectory);
            }
        }
        return outputDirectory;
    }

    private static void log(CompileContext context, String text) {
        context.processMessage(new CompilerMessage(NAME, BuildMessage.Kind.INFO, text));
    }

    @Override
    public List<String> getCompilableFileExtensions() {
        return COMPILABLE_EXTENSIONS;
    }

    private static Map<ModuleBuildTarget, List<File>> collectChangedFiles(CompileContext context, DirtyFilesHolder<JavaSourceRootDescriptor,
            ModuleBuildTarget> dirtyFilesHolder) throws IOException {
        final Map<ModuleBuildTarget, List<File>> result = new HashMap<ModuleBuildTarget, List<File>>();
        dirtyFilesHolder.processDirtyFiles(new FileProcessor<JavaSourceRootDescriptor, ModuleBuildTarget>() {
            public boolean apply(ModuleBuildTarget target, File file, JavaSourceRootDescriptor sourceRoot) throws IOException {
                final String path = file.getPath();
                if (isPascalFile(path)) { //todo file type check
                    List<File> toCompile = result.get(target);
                    if (null == toCompile) {
                        toCompile = new ArrayList<File>();
                        result.put(target, toCompile);
                    }
                    toCompile.add(file);
                }
                return true;
            }
        });
        return result;
    }

    private static boolean isPascalFile(String path) {
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
        return NAME;
    }


}
