// This is a generated file. Not intended for manual editing.
package com.siberika.idea.pascal.lang.psi;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElement;

public interface PasClassMethodResolution extends PasDeclSection {

  @Nullable
  PasEscapedIdent getEscapedIdent();

  @NotNull
  PasGenericTypeIdent getGenericTypeIdent();

  @Nullable
  PasKeywordIdent getKeywordIdent();

  @NotNull
  PasRefNamedIdent getRefNamedIdent();

  @Nullable
  PsiElement getPasClass();

  @Nullable
  PsiElement getFunction();

  @Nullable
  PsiElement getPasName();

  @Nullable
  PsiElement getProcedure();

}
