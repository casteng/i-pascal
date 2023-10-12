// This is a generated file. Not intended for manual editing.
package com.siberika.idea.pascal.lang.psi;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElement;

public interface PasClassPropertySpecifier extends PascalPsiElement {

  @Nullable
  PasExpression getExpression();

  @Nullable
  PasRefNamedIdent getRefNamedIdent();

  @Nullable
  PasTypeID getTypeID();

  @Nullable
  PsiElement getDefault();

  @Nullable
  PsiElement getDispid();

  @Nullable
  PsiElement getImplements();

  @Nullable
  PsiElement getRead();

  @Nullable
  PsiElement getWrite();

}
