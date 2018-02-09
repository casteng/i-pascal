package com.siberika.idea.pascal.lang.psi;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Author: George Bakhtadze
 * Date: 12/07/2016
 */
public interface PascalVariableDeclaration extends PascalPsiElement {
    @NotNull
    List<PasCustomAttributeDecl> getCustomAttributeDeclList();

    @NotNull
    List<? extends PascalNamedElement> getNamedIdentDeclList();

    @Nullable
    PasTypeDecl getTypeDecl();
}
