package com.siberika.idea.pascal.lang.stub;

import com.intellij.psi.stubs.StubElement;
import com.siberika.idea.pascal.lang.psi.PascalModule;
import org.jetbrains.annotations.NotNull;

/**
 * Author: George Bakhtadze
 * Date: 13/10/2015
 */
public interface PasModuleStub extends StubElement<PascalModule> {
    String getName();

    @NotNull
    PascalModule.ModuleType getModuleType();
}
