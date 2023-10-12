// This is a generated file. Not intended for manual editing.
package com.siberika.idea.pascal.lang.psi;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElement;

public interface PasLiteralExpr extends PasExpr {

  @Nullable
  PasStringFactor getStringFactor();

  @Nullable
  PsiElement getFalse();

  @Nullable
  PsiElement getNil();

  @Nullable
  PsiElement getNumberBin();

  @Nullable
  PsiElement getNumberHex();

  @Nullable
  PsiElement getNumberInt();

  @Nullable
  PsiElement getNumberOct();

  @Nullable
  PsiElement getNumberReal();

  @Nullable
  PsiElement getTrue();

}
