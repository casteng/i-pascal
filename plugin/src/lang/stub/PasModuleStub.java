package com.siberika.idea.pascal.lang.stub;

import com.siberika.idea.pascal.lang.psi.PascalModule;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Author: George Bakhtadze
 * Date: 13/10/2015
 */
public interface PasModuleStub extends PasNamedStub<PascalModule> {
    @NotNull
    PascalModule.ModuleType getModuleType();

    @NotNull
    List<String> getUsedUnitsPublic();

    @NotNull
    List<String> getUsedUnitsPrivate();
}
