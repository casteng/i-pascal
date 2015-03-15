package com.siberika.idea.pascal.lang.psi;

import com.siberika.idea.pascal.lang.psi.impl.PasField;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.List;

/**
 * Author: George Bakhtadze
 * Date: 15/09/2013
 */
public interface PasEntityScope extends PascalNamedElement {
    @NotNull
    String getName();
    @Nullable
    PasField getField(final String name) throws PasInvalidScopeException;
    @NotNull
    Collection<PasField> getAllFields() throws PasInvalidScopeException;
    @NotNull
    List<PasEntityScope> getParentScope() throws PasInvalidScopeException;
    @Nullable
    PasEntityScope getOwnerScope() throws PasInvalidScopeException;

    void invalidateCache();
}
