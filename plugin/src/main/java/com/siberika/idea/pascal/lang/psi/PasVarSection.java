// This is a generated file. Not intended for manual editing.
package com.siberika.idea.pascal.lang.psi;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElement;

public interface PasVarSection extends PasDeclSection {

  @NotNull
  List<PasVarDeclaration> getVarDeclarationList();

  @Nullable
  PsiElement getThreadvar();

  @Nullable
  PsiElement getVar();

}
