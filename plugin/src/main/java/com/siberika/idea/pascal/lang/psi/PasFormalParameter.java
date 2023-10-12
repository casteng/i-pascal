// This is a generated file. Not intended for manual editing.
package com.siberika.idea.pascal.lang.psi;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElement;

public interface PasFormalParameter extends PascalVariableDeclaration {

  @Nullable
  PasConstExpression getConstExpression();

  @NotNull
  List<PasCustomAttributeDecl> getCustomAttributeDeclList();

  @NotNull
  List<PasNamedIdent> getNamedIdentList();

  @Nullable
  PasParamType getParamType();

  @Nullable
  PasTypeDecl getTypeDecl();

}
