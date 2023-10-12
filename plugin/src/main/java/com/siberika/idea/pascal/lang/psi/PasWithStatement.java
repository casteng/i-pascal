// This is a generated file. Not intended for manual editing.
package com.siberika.idea.pascal.lang.psi;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElement;

public interface PasWithStatement extends PasStatement {

  @NotNull
  List<PasExpression> getExpressionList();

  @Nullable
  PasStatement getStatement();

  @Nullable
  PsiElement getDo();

  @NotNull
  PsiElement getWith();

}
