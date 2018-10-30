package com.siberika.idea.pascal.jps.builder;

import com.intellij.execution.process.BaseOSProcessHandler;
import com.intellij.execution.process.ProcessAdapter;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.util.SmartList;
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
import org.jetbrains.jps.builders.BuildTarget;
import org.jetbrains.jps.builders.BuildTargetIndex;
import org.jetbrains.jps.builders.BuildTargetType;
import org.jetbrains.jps.builders.DirtyFilesHolder;
import org.jetbrains.jps.builders.FileProcessor;
import org.jetbrains.jps.builders.java.JavaBuilderUtil;
import org.jetbrains.jps.cmdline.ProjectDescriptor;
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
import org.jetbrains.jps.model.module.JpsDependencyElement;
import org.jetbrains.jps.model.module.JpsModule;
import org.jetbrains.jps.model.module.JpsModuleDependency;
import org.jetbrains.jps.model.module.JpsModuleSourceRoot;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Author: George Bakhtadze
 * Date: 19/05/2015
 */
public class PascalTargetBuilder extends TargetBuilder<PascalSourceRootDescriptor, PascalTarget> {
    private static final Logger LOG = Logger.getInstance(PascalTargetBuilder.class);
    private static final String NAME = "Pascal builder";

    public static final Key<String> RUN_CONFIGURATION_KEY = Key.create("RUN_CONFIGURATION");

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
        LOG.info(String.format("Build() for target %s", target.getId()));
        if (isDependencyTarget(target, context)) {
            return;
        }
        JpsModule module = target.getModule();
        File mainFile = PascalBackendCompiler.getMainFile(ParamMap.getJpsParams(module.getProperties()));

        // Force main file to compile. TODO: force only for context-based (line marker?) run configurations
//        if (!holder.hasDirtyFiles() && !holder.hasRemovedFiles()) return;
        final Map<PascalTarget, List<File>> files = new HashMap<PascalTarget, List<File>>();
        if (mainFile != null) {
            files.put(target, new SmartList<File>(mainFile));
        }
        collectChangedFiles(files, holder);
        boolean isRebuild = JavaBuilderUtil.isForcedRecompilationAllJavaModules(context) || (!JavaBuilderUtil.isCompileJavaIncrementally(context));
        if (files.isEmpty() && !isRebuild) {
            context.processMessage(new CompilerMessage(getPresentableName(), BuildMessage.Kind.INFO, "No changes detected"));
            return;
        }

        CompilerMessager messager = new PascalCompilerMessager(getPresentableName(), context);

        boolean isDebug = isDebugBuild(context);

        JpsSdk<?> sdk = module.getSdk(JpsPascalSdkType.INSTANCE);
        if (sdk != null) {
            PascalBackendCompiler compiler = getCompiler(sdk, messager);
            if (compiler != null) {
                messager.info("Compiler family:" + compiler.getId(), "", -1L, -1);
                List<File> sdkFiles = sdk.getParent().getFiles(JpsOrderRootType.COMPILED);
                sdkFiles.addAll(sdk.getParent().getFiles(JpsOrderRootType.SOURCES));
                File outputDir = getBuildOutputDirectory(module, target.isTests(), context);

                for (File file : files.get(target)) {
                    File compiled = new File(outputDir, FileUtil.getNameWithoutExtension(file) + compiler.getCompiledUnitExt());
                    //messager.info(String.format("Map: %s => %s ", file.getCanonicalPath(), compiled.getCanonicalPath()), null, -1L, -1L);
                    outputConsumer.registerOutputFile(compiled, Collections.singleton(file.getCanonicalPath()));
                }

                List<File> sourcePaths = new ArrayList<File>();
                getFiles(new HashSet<JpsModule>(), sourcePaths, module);
                String[] cmdLine = compiler.createStartupCommand(sdk.getHomePath(), module.getName(), outputDir.getAbsolutePath(),
                        sdkFiles, sourcePaths,
                        files.get(target), ParamMap.getJpsParams(module.getProperties()),
                        isRebuild, isDebug,
                        ParamMap.getJpsParams(sdk.getSdkProperties()));
                if (cmdLine != null) {
                    // For Delphi workingDirectory should be null otherwise file paths in compiler messages will be relative
                    File workingDirectory = PascalCompilerFamily.DELPHI.equals(getCompilerFamily(sdk)) ? null : new File(FileUtil.expandUserHome("~/"));
                    int exitCode = launchCompiler(compiler, messager, cmdLine, workingDirectory);
                    if (exitCode != 0) {
                        messager.warning("Error. Compiler exit code: " + exitCode, null, -1L, -1L);
                    }
                } else {
                    messager.warning("Error. Can't launch compiler", null, -1L, -1L);
                }
            } else {
                messager.error("Can't determine compiler family", "", -1L, -1L);
            }
        } else {
            log(context, "Pascal SDK is not defined for module " + module.getName());
        }
    }

    private boolean isDebugBuild(CompileContext context) {
        String runConfigName = context.getBuilderParameter(RUN_CONFIGURATION_KEY.toString());
        return (runConfigName != null) && runConfigName.startsWith("[debug]");
    }

    private boolean isDependencyTarget(PascalTarget target, CompileContext context) {
        final ProjectDescriptor pd = context.getProjectDescriptor();
        final BuildTargetIndex targetIndex = pd.getBuildTargetIndex();
        targetIndex.getSortedTargetChunks(context);
        List<PascalTarget> all = targetIndex.getAllTargets(PascalTargetType.PRODUCTION);
        for (PascalTarget pascalTarget : all) {
            Collection<BuildTarget<?>> deps = pascalTarget.computeDependencies();
            if (context.getScope().isAffected(pascalTarget)) {
                for (BuildTarget<?> dep : deps) {
                    if (target.equals(dep)) {
                        log(context, String.format("Skipping build of module \"%s\" because it's a dependency of module \"%s\"", target.getId(), pascalTarget.getId()));
                        return true;
                    }
                }
            }
        }
        return false;
    }

    @Nullable
    private PascalBackendCompiler getCompiler(@NotNull JpsSdk<?> sdk, CompilerMessager messager) {
        PascalCompilerFamily family = getCompilerFamily(sdk);
        if (PascalCompilerFamily.FPC.equals(family)) {
            return new FPCBackendCompiler(messager);
        } else if (PascalCompilerFamily.DELPHI.equals(family)) {
            return new DelphiBackendCompiler(messager);
        }
        return null;
    }

    private PascalCompilerFamily getCompilerFamily(JpsSdk<?> sdk) {
        ParamMap params = ParamMap.getJpsParams(sdk.getSdkProperties());
        String family = params != null ? params.get(PascalSdkData.Keys.COMPILER_FAMILY.getKey()) : null;
        for (PascalCompilerFamily compilerFamily : PascalCompilerFamily.values()) {
            if (compilerFamily.name().equals(family)) {
                return compilerFamily;
            }
        }
        return null;
    }

    private int launchCompiler(PascalBackendCompiler compiler, CompilerMessager messager, String[] cmdLine, File workingDir) throws IOException {
        Process process = Runtime.getRuntime().exec(cmdLine, null, workingDir);
        BaseOSProcessHandler handler = new BaseOSProcessHandler(process, cmdLine[0], Charset.defaultCharset());
        ProcessAdapter adapter = compiler.getCompilerProcessAdapter(messager);
        handler.addProcessListener(adapter);
        handler.startNotify();
        handler.waitFor();
        return process.exitValue();
    }

    private static void log(CompileContext context, String text) {
        context.processMessage(new CompilerMessage(NAME, BuildMessage.Kind.INFO, text));
    }

    private void getFiles(Set<JpsModule> visited, List<File> result, JpsModule module) {
        if ((null == module) || visited.contains(module)) {
            return;
        }
        visited.add(module);
        LOG.info("Collecting files for module: " + module.getName());
        for (JpsModuleSourceRoot root : module.getSourceRoots()) {
            result.add(root.getFile());
        }
        for (JpsDependencyElement element : module.getDependenciesList().getDependencies()) {
            if (element instanceof JpsModuleDependency) {
                getFiles(visited, result, ((JpsModuleDependency) element).getModule());
            }
        }
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

    private static void collectChangedFiles(final Map<PascalTarget, List<File>> result, DirtyFilesHolder<PascalSourceRootDescriptor, PascalTarget> dirtyFilesHolder) throws IOException {
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
