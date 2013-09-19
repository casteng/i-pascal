package com.siberika.idea.pascal.lang.psi;

import com.siberika.idea.pascal.lang.psi.impl.PasField;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Author: George Bakhtadze
 * Date: 15/09/2013
 */
public interface PasStruct extends PascalNamedElement {
    @NotNull
    String getName();
    @Nullable
    PasField getField(final String name);
}
