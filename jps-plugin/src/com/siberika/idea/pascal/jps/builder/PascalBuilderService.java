package com.siberika.idea.pascal.jps.builder;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.jps.builders.BuildTargetType;
import org.jetbrains.jps.incremental.BuilderService;
import org.jetbrains.jps.incremental.TargetBuilder;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Author: George Bakhtadze
 * Date: 08/02/2014
 */
public class PascalBuilderService extends BuilderService {
    static final List<String> COMPILABLE_EXTENSIONS = Arrays.asList("pas", "inc", "dpr", "pp", "lpr");

    @NotNull
    @Override
    public List<? extends BuildTargetType<?>> getTargetTypes() {
        return Arrays.asList(PascalTargetType.PRODUCTION, PascalTargetType.TESTS);
    }

    @NotNull
    @Override
    public List<? extends TargetBuilder<?, ?>> createBuilders() {
        return Collections.singletonList(new FPCTargetBuilder(Arrays.asList(PascalTargetType.PRODUCTION, PascalTargetType.TESTS)));
    }

    /*@NotNull
    @Override
    public List<? extends ModuleLevelBuilder> createModuleLevelBuilders() {
        return Collections.singletonList(new PascalModuleLevelBuilder());
    }*/
}
