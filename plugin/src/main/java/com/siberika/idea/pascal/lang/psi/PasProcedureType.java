// This is a generated file. Not intended for manual editing.
package com.siberika.idea.pascal.lang.psi;

import com.intellij.psi.PsiReference;
import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElement;

public interface PasProcedureType extends PascalRoutineEntity {

  @NotNull
  List<PasCustomAttributeDecl> getCustomAttributeDeclList();

  @Nullable
  PasFormalParameterSection getFormalParameterSection();

  @Nullable
  PasTypeDecl getTypeDecl();

  @Nullable
  PsiElement getFunction();

  @Nullable
  PsiElement getObject();

  @Nullable
  PsiElement getOf();

  @Nullable
  PsiElement getProcedure();

  @Nullable
  PsiReference getReference();

  @Nullable
  PsiElement getTo();

}
