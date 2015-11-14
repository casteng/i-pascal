package com.siberika.idea.pascal.lang.psi;

import com.siberika.idea.pascal.lang.psi.impl.PasField;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.List;

/**
 * Author: George Bakhtadze
 * Date: 29/01/2015
 */
public interface PascalStructType extends PasEntityScope {
    @Nullable
    PasField getField(String name);
    @NotNull
    Collection<PasField> getAllFields();
    @NotNull
    List<PasVisibility> getVisibilityList();
}
