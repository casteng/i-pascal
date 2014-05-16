package com.siberika.idea.pascal.jps.builder;

import com.intellij.util.containers.ContainerUtil;
import com.siberika.idea.pascal.jps.model.JpsPascalModuleType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.jps.builders.BuildRootIndex;
import org.jetbrains.jps.builders.BuildTarget;
import org.jetbrains.jps.builders.BuildTargetRegistry;
import org.jetbrains.jps.builders.ModuleBasedTarget;
import org.jetbrains.jps.builders.TargetOutputIndex;
import org.jetbrains.jps.builders.storage.BuildDataPaths;
import org.jetbrains.jps.incremental.CompileContext;
import org.jetbrains.jps.indices.IgnoredFileIndex;
import org.jetbrains.jps.indices.ModuleExcludeIndex;
import org.jetbrains.jps.model.JpsModel;
import org.jetbrains.jps.model.java.JavaSourceRootProperties;
import org.jetbrains.jps.model.java.JavaSourceRootType;
import org.jetbrains.jps.model.java.JpsJavaClasspathKind;
import org.jetbrains.jps.model.java.JpsJavaExtensionService;
import org.jetbrains.jps.model.module.JpsModule;
import org.jetbrains.jps.model.module.JpsTypedModuleSourceRoot;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;

public class PascalTarget extends ModuleBasedTarget<PascalSourceRootDescriptor> {
    public PascalTarget(@NotNull JpsModule module, PascalTargetType targetType) {
        super(targetType, module);
    }

    @Override
    public String getId() {
        return myModule.getName();
    }

    @Override
    public Collection<BuildTarget<?>> computeDependencies(BuildTargetRegistry targetRegistry, TargetOutputIndex outputIndex) {
        return computeDependencies();
    }

    public Collection<BuildTarget<?>> computeDependencies() {
        List<BuildTarget<?>> dependencies = new ArrayList<BuildTarget<?>>();
        Set<JpsModule> modules = JpsJavaExtensionService.dependencies(myModule).includedIn(JpsJavaClasspathKind.compile(isTests())).getModules();
        for (JpsModule module : modules) {
            if (module.getModuleType().equals(JpsPascalModuleType.INSTANCE)) {
                dependencies.add(new PascalTarget(module, getPascalTargetType()));
            }
        }
        if (isTests()) {
            dependencies.add(new PascalTarget(myModule, PascalTargetType.PRODUCTION));
        }
        return dependencies;
    }

    @NotNull
    @Override
    public List<PascalSourceRootDescriptor> computeRootDescriptors(JpsModel model, ModuleExcludeIndex index, IgnoredFileIndex ignoredFileIndex, BuildDataPaths dataPaths) {
        List<PascalSourceRootDescriptor> result = new ArrayList<PascalSourceRootDescriptor>();
        JavaSourceRootType type = isTests() ? JavaSourceRootType.TEST_SOURCE : JavaSourceRootType.SOURCE;
        for (JpsTypedModuleSourceRoot<JavaSourceRootProperties> root : myModule.getSourceRoots(type)) {
            result.add(new PascalSourceRootDescriptor(root.getFile(), this));
        }
        return result;
    }

    @Nullable
    @Override
    public PascalSourceRootDescriptor findRootDescriptor(String rootId, BuildRootIndex rootIndex) {
        return ContainerUtil.getFirstItem(rootIndex.getRootDescriptors(new File(rootId), Collections.singletonList(getPascalTargetType()), null));
    }

    @NotNull
    @Override
    public String getPresentableName() {
        return "Pascal '" + myModule.getName() + "' " + (isTests() ? "tests" : "production");
    }

    @NotNull
    @Override
    public Collection<File> getOutputRoots(CompileContext context) {
        return ContainerUtil.createMaybeSingletonList(JpsJavaExtensionService.getInstance().getOutputDirectory(myModule, isTests()));
    }

    @Override
    public boolean isTests() {
        return getPascalTargetType().isTests();
    }

    public PascalTargetType getPascalTargetType() {
        return (PascalTargetType) getTargetType();
    }
}