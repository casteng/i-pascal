// This is a generated file. Not intended for manual editing.
package com.siberika.idea.pascal.lang.psi;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElement;

public interface PasIfStatement extends PasStatement {

  @Nullable
  PasExpression getExpression();

  @Nullable
  PasIfElseStatement getIfElseStatement();

  @Nullable
  PasIfThenStatement getIfThenStatement();

  @Nullable
  PsiElement getElse();

  @NotNull
  PsiElement getIf();

  @Nullable
  PsiElement getThen();

}
