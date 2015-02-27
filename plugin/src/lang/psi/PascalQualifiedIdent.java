package com.siberika.idea.pascal.lang.psi;

import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Author: George Bakhtadze
 * Date: 16/08/2013
 */
public interface PascalQualifiedIdent extends PascalNamedElement {
    @NotNull
    List<PasSubIdent> getSubIdentList();
}
