package com.siberika.idea.pascal.jps.builder;

import com.siberika.idea.pascal.jps.model.JpsPascalModuleType;
import com.siberika.idea.pascal.jps.util.ParamMap;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.jps.builders.BuildTargetLoader;
import org.jetbrains.jps.builders.ModuleBasedBuildTargetType;
import org.jetbrains.jps.model.JpsModel;
import org.jetbrains.jps.model.JpsSimpleElement;
import org.jetbrains.jps.model.module.JpsTypedModule;

import java.util.ArrayList;
import java.util.List;

public class PascalTargetType extends ModuleBasedBuildTargetType<PascalTarget> {
    public static final PascalTargetType PRODUCTION = new PascalTargetType("pascal-production", false);
    public static final PascalTargetType TESTS = new PascalTargetType("pascal-tests", true);
    private final boolean myTests;

    private PascalTargetType(String pascal, boolean tests) {
        super(pascal);
        myTests = tests;
    }

    @NotNull
    @Override
    public List<PascalTarget> computeAllTargets(@NotNull JpsModel model) {
        List<PascalTarget> targets = new ArrayList<PascalTarget>();
        for (JpsTypedModule<JpsSimpleElement<ParamMap>> module : model.getProject().getModules(JpsPascalModuleType.INSTANCE)) {
            targets.add(new PascalTarget(module, this));
        }
        return targets;
    }

    @NotNull
    @Override
    public BuildTargetLoader<PascalTarget> createLoader(@NotNull final JpsModel model) {
        return new BuildTargetLoader<PascalTarget>() {
            @Nullable
            @Override
            public PascalTarget createTarget(@NotNull String targetId) {
                for (JpsTypedModule<JpsSimpleElement<ParamMap>> module : model.getProject().getModules(JpsPascalModuleType.INSTANCE)) {
                    if (module.getName().equals(targetId)) {
                        return new PascalTarget(module, PascalTargetType.this);
                    }
                }
                return null;
            }
        };
    }

    public boolean isTests() {
        return myTests;
    }
}