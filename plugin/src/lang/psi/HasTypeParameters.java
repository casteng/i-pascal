package com.siberika.idea.pascal.lang.psi;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public interface HasTypeParameters {
    @NotNull
    List<String> getTypeParameters();

}
