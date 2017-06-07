package com.siberika.idea.pascal.lang.psi.impl;

import com.intellij.openapi.util.Pair;
import com.intellij.psi.SmartPsiElementPointer;
import com.intellij.psi.StubBasedPsiElement;
import com.siberika.idea.pascal.lang.psi.PasEntityScope;
import com.siberika.idea.pascal.lang.psi.PascalNamedElement;
import com.siberika.idea.pascal.lang.stub.PasModuleStub;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.List;

/**
 * Author: George Bakhtadze
 * Date: 13/08/2015
 */
public interface PascalModule extends PasEntityScope, StubBasedPsiElement<PasModuleStub> {

    enum ModuleType {
        UNIT, PROGRAM, LIBRARY, PACKAGE
    }

    ModuleType getModuleType();

    @Nullable
    PasField getPublicField(final String name);

    @Nullable
    PasField getPrivateField(final String name);

    @NotNull
    Collection<PasField> getPrivateFields();

    @NotNull
    Collection<PasField> getPubicFields();

    List<SmartPsiElementPointer<PasEntityScope>> getPrivateUnits();

    List<SmartPsiElementPointer<PasEntityScope>> getPublicUnits();

    // Used in interface and implementation identifiers list
    Pair<List<PascalNamedElement>, List<PascalNamedElement>> getIdentsFrom(@NotNull String module);
}
