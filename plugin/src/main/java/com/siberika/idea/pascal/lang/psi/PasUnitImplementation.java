// This is a generated file. Not intended for manual editing.
package com.siberika.idea.pascal.lang.psi;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElement;

public interface PasUnitImplementation extends PascalPsiElement {

  @Nullable
  PasCompoundStatement getCompoundStatement();

  @Nullable
  PasImplDeclSection getImplDeclSection();

  @Nullable
  PasUnitFinalization getUnitFinalization();

  @Nullable
  PasUnitInitialization getUnitInitialization();

  @Nullable
  PasUsesClause getUsesClause();

  @Nullable
  PsiElement getEnd();

  @NotNull
  PsiElement getImplementation();

}
