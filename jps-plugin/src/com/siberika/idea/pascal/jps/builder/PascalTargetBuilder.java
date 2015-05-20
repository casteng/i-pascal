package com.siberika.idea.pascal.jps.builder;

import com.intellij.execution.process.BaseOSProcessHandler;
import com.intellij.execution.process.ProcessAdapter;
import com.intellij.openapi.util.io.FileUtil;
import com.siberika.idea.pascal.jps.compiler.CompilerMessager;
import com.siberika.idea.pascal.jps.compiler.DelphiBackendCompiler;
import com.siberika.idea.pascal.jps.compiler.FPCBackendCompiler;
import com.siberika.idea.pascal.jps.compiler.PascalBackendCompiler;
import com.siberika.idea.pascal.jps.model.JpsPascalModuleType;
import com.siberika.idea.pascal.jps.model.JpsPascalSdkType;
import com.siberika.idea.pascal.jps.sdk.PascalCompilerFamily;
import com.siberika.idea.pascal.jps.sdk.PascalSdkData;
import com.siberika.idea.pascal.jps.util.ParamMap;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.jps.builders.BuildOutputConsumer;
import org.jetbrains.jps.builders.BuildTargetType;
import org.jetbrains.jps.builders.DirtyFilesHolder;
import org.jetbrains.jps.builders.FileProcessor;
import org.jetbrains.jps.builders.java.JavaBuilderUtil;
import org.jetbrains.jps.incremental.CompileContext;
import org.jetbrains.jps.incremental.ProjectBuildException;
import org.jetbrains.jps.incremental.TargetBuilder;
import org.jetbrains.jps.incremental.messages.BuildMessage;
import org.jetbrains.jps.incremental.messages.CompilerMessage;
import org.jetbrains.jps.incremental.resources.ResourcesBuilder;
import org.jetbrains.jps.incremental.resources.StandardResourceBuilderEnabler;
import org.jetbrains.jps.model.java.JpsJavaExtensionService;
import org.jetbrains.jps.model.library.JpsOrderRootType;
import org.jetbrains.jps.model.library.sdk.JpsSdk;
import org.jetbrains.jps.model.module.JpsModule;
import org.jetbrains.jps.model.module.JpsModuleSourceRoot;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Author: George Bakhtadze
 * Date: 19/05/2015
 */
public class PascalTargetBuilder extends TargetBuilder<PascalSourceRootDescriptor, PascalTarget> {
    private static final String NAME = "Pascal builder";

    protected PascalTargetBuilder(Collection<? extends BuildTargetType<? extends PascalTarget>> buildTargetTypes) {
        super(buildTargetTypes);

        //disables java resource builder for pascal modules
        ResourcesBuilder.registerEnabler(new StandardResourceBuilderEnabler() {
            @Override
            public boolean isResourceProcessingEnabled(@NotNull JpsModule module) {
                return !(module.getModuleType() instanceof JpsPascalModuleType);
            }
        });
    }

    @NotNull
    @Override
    public String getPresentableName() {
        return NAME;
    }

    @Override
    public void build(@NotNull PascalTarget target, @NotNull DirtyFilesHolder<PascalSourceRootDescriptor, PascalTarget> holder,
                      @NotNull BuildOutputConsumer outputConsumer, @NotNull CompileContext context) throws ProjectBuildException, IOException {
        if (!holder.hasDirtyFiles() && !holder.hasRemovedFiles()) return;
        final Map<PascalTarget, List<File>> files = collectChangedFiles(holder);
        if (files.isEmpty() && !JavaBuilderUtil.isForcedRecompilationAllJavaModules(context)) {
            context.processMessage(new CompilerMessage(getPresentableName(), BuildMessage.Kind.INFO, "No changes detected"));
            return;
        }

        CompilerMessager messager = new PascalCompilerMessager(getPresentableName(), context);

        JpsModule module = target.getModule();
        JpsSdk<?> sdk = module.getSdk(JpsPascalSdkType.INSTANCE);
        if (sdk != null) {
            PascalBackendCompiler compiler = getCompiler(sdk, messager);
            if (compiler != null) {
                messager.info("Compiler family:" + compiler.getId(), "", -1l, -1);
                List<File> sdkFiles = sdk.getParent().getFiles(JpsOrderRootType.COMPILED);
                sdkFiles.addAll(sdk.getParent().getFiles(JpsOrderRootType.SOURCES));
                File outputDir = getBuildOutputDirectory(module, target.isTests(), context);

                for (File file : files.get(target)) {
                    File compiled = new File(outputDir, FileUtil.getNameWithoutExtension(file) + compiler.getCompiledUnitExt());
                    messager.info(String.format("Map: %s => %s ", file.getCanonicalPath(), compiled.getCanonicalPath()), null, -1l, -1l);
                    outputConsumer.registerOutputFile(compiled, Collections.singleton(file.getCanonicalPath()));
                }

                String[] cmdLine = compiler.createStartupCommand(sdk.getHomePath(), module.getName(), outputDir.getAbsolutePath(),
                        sdkFiles, getFiles(module.getSourceRoots()),
                        files.get(target), ParamMap.getJpsParams(module.getProperties()),
                        JavaBuilderUtil.isForcedRecompilationAllJavaModules(context),
                        ParamMap.getJpsParams(sdk.getSdkProperties()));
                int exitCode = launchCompiler(compiler, messager, cmdLine);
                if (exitCode != 0) {
                    messager.error("Error. Compiler exit code: " + exitCode, null, -1l, -1l);
                }
            } else {
                messager.error("Can't determine compiler family", "", -1l, -1l);
            }
        } else {
            log(context, "Pascal SDK is not defined for module " + module.getName());
        }
    }

    @Nullable
    private PascalBackendCompiler getCompiler(@NotNull JpsSdk<?> sdk, CompilerMessager messager) {
        ParamMap params = ParamMap.getJpsParams(sdk.getSdkProperties());
        String family = params != null ? params.get(PascalSdkData.DATA_KEY_COMPILER_FAMILY) : null;
        if (PascalCompilerFamily.FPC.toString().equals(family)) {
            return new FPCBackendCompiler(messager);
        } else if (PascalCompilerFamily.DELPHI.toString().equals(family)) {
            return new DelphiBackendCompiler(messager);
        }
        return null;
    }

    private int launchCompiler(PascalBackendCompiler compiler, CompilerMessager messager, String[] cmdLine) throws IOException {
        messager.info("Command line: ", null, -1l, -1l);
        for (String s : cmdLine) {
            messager.info(s, null, -1l, -1l);
        }
        Process process = Runtime.getRuntime().exec(cmdLine);
        BaseOSProcessHandler handler = new BaseOSProcessHandler(process, "", Charset.defaultCharset());
        ProcessAdapter adapter = compiler.getCompilerProcessAdapter(messager);
        handler.addProcessListener(adapter);
        handler.startNotify();
        handler.waitFor();
        return process.exitValue();
    }

    private static void log(CompileContext context, String text) {
        context.processMessage(new CompilerMessage(NAME, BuildMessage.Kind.INFO, text));
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

    private static Map<PascalTarget, List<File>> collectChangedFiles(DirtyFilesHolder<PascalSourceRootDescriptor, PascalTarget> dirtyFilesHolder) throws IOException {
        final Map<PascalTarget, List<File>> result = new HashMap<PascalTarget, List<File>>();
        dirtyFilesHolder.processDirtyFiles(new FileProcessor<PascalSourceRootDescriptor, PascalTarget>() {
            public boolean apply(PascalTarget target, File file, PascalSourceRootDescriptor sourceRoot) throws IOException {
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
        for (String ext : PascalBuilderService.COMPILABLE_EXTENSIONS) {
            if (path.endsWith("." + ext)) {
                return true;
            }
        }
        return false;
    }

}
