// This is a generated file. Not intended for manual editing.
package com.siberika.idea.pascal.lang.psi;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElement;

public interface PasCaseStatement extends PasStatement {

  @Nullable
  PasCaseElse getCaseElse();

  @NotNull
  List<PasCaseItem> getCaseItemList();

  @Nullable
  PasExpression getExpression();

  @NotNull
  PsiElement getCase();

  @NotNull
  PsiElement getEnd();

  @Nullable
  PsiElement getOf();

}
