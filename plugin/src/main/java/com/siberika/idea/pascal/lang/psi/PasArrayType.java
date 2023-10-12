// This is a generated file. Not intended for manual editing.
package com.siberika.idea.pascal.lang.psi;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElement;

public interface PasArrayType extends PascalPsiElement {

  @NotNull
  List<PasArrayIndex> getArrayIndexList();

  @Nullable
  PasTypeDecl getTypeDecl();

  @NotNull
  PsiElement getArray();

  @Nullable
  PsiElement getConst();

  @Nullable
  PsiElement getOf();

  @Nullable
  PsiElement getPacked();

}
