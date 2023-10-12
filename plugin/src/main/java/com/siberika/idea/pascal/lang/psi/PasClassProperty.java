// This is a generated file. Not intended for manual editing.
package com.siberika.idea.pascal.lang.psi;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElement;

public interface PasClassProperty extends PascalPsiElement {

  @Nullable
  PasClassPropertyArray getClassPropertyArray();

  @Nullable
  PasClassPropertyIndex getClassPropertyIndex();

  @NotNull
  List<PasClassPropertySpecifier> getClassPropertySpecifierList();

  @NotNull
  List<PasCustomAttributeDecl> getCustomAttributeDeclList();

  @Nullable
  PasNamedIdentDecl getNamedIdentDecl();

  @NotNull
  List<PasStringFactor> getStringFactorList();

  @Nullable
  PasTypeID getTypeID();

  @Nullable
  PsiElement getPasClass();

  @Nullable
  PsiElement getDefault();

  @NotNull
  PsiElement getProperty();

}
