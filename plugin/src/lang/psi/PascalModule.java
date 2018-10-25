package com.siberika.idea.pascal.lang.psi;

import com.intellij.openapi.util.Pair;
import com.intellij.psi.SmartPsiElementPointer;
import com.siberika.idea.pascal.lang.psi.impl.PasField;
import com.siberika.idea.pascal.lang.stub.PasModuleStub;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.List;
import java.util.Set;

/**
 * Author: George Bakhtadze
 * Date: 13/08/2015
 */
public interface PascalModule extends PasEntityScope, PascalStubElement<PasModuleStub> {

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
    Pair<List<PascalNamedElement>, List<PascalNamedElement>> getIdentsFrom(@Nullable String module, boolean includeInterface, List<String> unitPrefixes);

    @NotNull
    Set<String> getUsedUnitsPublic();

    @NotNull
    Set<String> getUsedUnitsPrivate();
}
