package com.siberika.idea.pascal.lang.psi;

import com.siberika.idea.pascal.lang.psi.impl.PasField;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;

/**
 * Author: George Bakhtadze
 * Date: 15/09/2013
 */
public interface PasEntityScope extends PascalNamedElement {
    @NotNull
    String getName();
    @Nullable
    PasField getField(final String name);
    @NotNull
    Collection<PasField> getAllFields();
}
