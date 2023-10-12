// This is a generated file. Not intended for manual editing.
package com.siberika.idea.pascal.lang.psi;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElement;

public interface PasTryStatement extends PasStatement {

  @NotNull
  List<PasHandler> getHandlerList();

  @NotNull
  List<PasStatement> getStatementList();

  @Nullable
  PsiElement getElse();

  @Nullable
  PsiElement getEnd();

  @Nullable
  PsiElement getExcept();

  @Nullable
  PsiElement getFinally();

  @NotNull
  PsiElement getTry();

}
