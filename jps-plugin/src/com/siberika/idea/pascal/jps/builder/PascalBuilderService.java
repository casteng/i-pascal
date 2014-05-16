package com.siberika.idea.pascal.jps.builder;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.jps.builders.BuildTargetType;
import org.jetbrains.jps.incremental.BuilderService;
import org.jetbrains.jps.incremental.ModuleLevelBuilder;

import java.util.Arrays;
import java.util.List;

/**
 * Author: George Bakhtadze
 * Date: 08/02/2014
 */
public class PascalBuilderService extends BuilderService {
    @NotNull
    @Override
    public List<? extends BuildTargetType<?>> getTargetTypes() {
        return Arrays.asList(PascalTargetType.PRODUCTION, PascalTargetType.TESTS);
    }

    @NotNull
    @Override
    public List<? extends ModuleLevelBuilder> createModuleLevelBuilders() {
        return Arrays.asList(new PascalModuleLevelBuilder());
    }
}
