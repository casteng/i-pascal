package com.siberika.idea.pascal.lang.psi;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public interface HasTypeParameters {
    @NotNull
    List<String> getTypeParameters();

    @Nullable
    String getCanonicalTypeName();
}
