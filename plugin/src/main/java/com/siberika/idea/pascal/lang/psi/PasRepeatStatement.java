// This is a generated file. Not intended for manual editing.
package com.siberika.idea.pascal.lang.psi;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElement;

public interface PasRepeatStatement extends PasStatement {

  @Nullable
  PasExpression getExpression();

  @NotNull
  List<PasStatement> getStatementList();

  @NotNull
  PsiElement getRepeat();

  @Nullable
  PsiElement getUntil();

}
