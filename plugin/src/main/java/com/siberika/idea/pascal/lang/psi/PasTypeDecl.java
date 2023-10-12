// This is a generated file. Not intended for manual editing.
package com.siberika.idea.pascal.lang.psi;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElement;

public interface PasTypeDecl extends PascalPsiElement {

  @Nullable
  PasArrayType getArrayType();

  @Nullable
  PasClassHelperDecl getClassHelperDecl();

  @Nullable
  PasClassTypeDecl getClassTypeDecl();

  @Nullable
  PasClassTypeTypeDecl getClassTypeTypeDecl();

  @Nullable
  PasEnumType getEnumType();

  @Nullable
  PasFileType getFileType();

  @Nullable
  PasInterfaceTypeDecl getInterfaceTypeDecl();

  @Nullable
  PasObjectDecl getObjectDecl();

  @Nullable
  PasPointerType getPointerType();

  @Nullable
  PasProcedureType getProcedureType();

  @Nullable
  PasRecordDecl getRecordDecl();

  @Nullable
  PasRecordHelperDecl getRecordHelperDecl();

  @Nullable
  PasSetType getSetType();

  @Nullable
  PasStringType getStringType();

  @Nullable
  PasSubRangeType getSubRangeType();

  @Nullable
  PasTypeID getTypeID();

  @Nullable
  PsiElement getType();

}
