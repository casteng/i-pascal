// This is a generated file. Not intended for manual editing.
package com.siberika.idea.pascal.lang.psi;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElement;

public interface PasInlineVarDeclaration extends PascalVariableDeclaration, PascalInlineDeclaration {

  @NotNull
  List<PasCustomAttributeDecl> getCustomAttributeDeclList();

  @NotNull
  List<PasNamedIdent> getNamedIdentList();

  @Nullable
  PasTypeDecl getTypeDecl();

  @NotNull
  PsiElement getVar();

  @NotNull List<? extends PascalNamedElement> getNamedIdentDeclList();

}
