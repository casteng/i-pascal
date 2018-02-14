package com.siberika.idea.pascal.lang.stub;

import com.siberika.idea.pascal.lang.psi.PascalModule;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

/**
 * Author: George Bakhtadze
 * Date: 13/10/2015
 */
public interface PasModuleStub extends PasNamedStub<PascalModule> {
    @NotNull
    PascalModule.ModuleType getModuleType();

    @NotNull
    Set<String> getUsedUnitsPublic();

    @NotNull
    Set<String> getUsedUnitsPrivate();
}
