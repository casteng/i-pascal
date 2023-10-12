// This is a generated file. Not intended for manual editing.
package com.siberika.idea.pascal.lang.psi;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElement;

public interface PasForInlineDeclaration extends PascalVariableDeclaration, PascalInlineDeclaration {

  @Nullable
  PasNamedIdent getNamedIdent();

  @Nullable
  PasTypeDecl getTypeDecl();

  @NotNull
  PsiElement getVar();

  @NotNull List<PasCustomAttributeDecl> getCustomAttributeDeclList();

  @NotNull List<? extends PascalNamedElement> getNamedIdentDeclList();

}
