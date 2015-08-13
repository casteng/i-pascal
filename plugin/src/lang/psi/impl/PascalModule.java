package com.siberika.idea.pascal.lang.psi.impl;

import com.siberika.idea.pascal.lang.psi.PasEntityScope;
import com.siberika.idea.pascal.lang.psi.PasInvalidScopeException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.List;

/**
 * Author: George Bakhtadze
 * Date: 13/08/2015
 */
public interface PascalModule extends PasEntityScope {

    enum ModuleType {
        UNIT, PROGRAM, LIBRARY, PACKAGE
    }

    ModuleType getModuleType();

    @Nullable
    PasField getPublicField(final String name) throws PasInvalidScopeException;

    @Nullable
    PasField getPrivateField(final String name) throws PasInvalidScopeException;

    @NotNull
    Collection<PasField> getPrivateFields() throws PasInvalidScopeException;

    @NotNull
    Collection<PasField> getPubicFields() throws PasInvalidScopeException;

    List<PasEntityScope> getPrivateUnits() throws PasInvalidScopeException;

    List<PasEntityScope> getPublicUnits() throws PasInvalidScopeException;

}
