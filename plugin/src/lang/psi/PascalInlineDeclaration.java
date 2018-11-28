package com.siberika.idea.pascal.lang.psi;

import org.jetbrains.annotations.Nullable;

public interface PascalInlineDeclaration extends PascalPsiElement {

    @Nullable
    PasTypeDecl getTypeDecl();

}
