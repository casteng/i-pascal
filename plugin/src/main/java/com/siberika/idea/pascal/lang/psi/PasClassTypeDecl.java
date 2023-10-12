// This is a generated file. Not intended for manual editing.
package com.siberika.idea.pascal.lang.psi;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElement;
import com.intellij.psi.StubBasedPsiElement;
import com.siberika.idea.pascal.lang.stub.struct.PasClassDeclStub;

public interface PasClassTypeDecl extends PascalClassDecl, StubBasedPsiElement<PasClassDeclStub> {

  @NotNull
  List<PasClassField> getClassFieldList();

  @NotNull
  List<PasClassMethodResolution> getClassMethodResolutionList();

  @Nullable
  PasClassParent getClassParent();

  @NotNull
  List<PasClassProperty> getClassPropertyList();

  @Nullable
  PasClassState getClassState();

  @Nullable
  PasConstExpression getConstExpression();

  @NotNull
  List<PasConstSection> getConstSectionList();

  @NotNull
  List<PasExportedRoutine> getExportedRoutineList();

  @NotNull
  List<PasTypeSection> getTypeSectionList();

  @NotNull
  List<PasVarSection> getVarSectionList();

  @NotNull
  List<PasVisibility> getVisibilityList();

  @Nullable
  PsiElement getEnd();

  @Nullable
  PsiElement getExternal();

  @Nullable
  PsiElement getIndex();

  @Nullable
  PsiElement getObjcClass();

  @Nullable
  PsiElement getPacked();

}
