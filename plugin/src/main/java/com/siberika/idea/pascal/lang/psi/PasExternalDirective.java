// This is a generated file. Not intended for manual editing.
package com.siberika.idea.pascal.lang.psi;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElement;

public interface PasExternalDirective extends PascalPsiElement {

  @Nullable
  PasConstExpression getConstExpression();

  @NotNull
  List<PasExpr> getExprList();

  @NotNull
  List<PasExpression> getExpressionList();

  @NotNull
  PsiElement getExternal();

  @Nullable
  PsiElement getIndex();

}
