// This is a generated file. Not intended for manual editing.
package com.siberika.idea.pascal.lang.psi;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElement;
import com.intellij.psi.StubBasedPsiElement;
import com.siberika.idea.pascal.lang.stub.struct.PasInterfaceDeclStub;

public interface PasInterfaceTypeDecl extends PascalInterfaceDecl, StubBasedPsiElement<PasInterfaceDeclStub> {

  @NotNull
  List<PasClassField> getClassFieldList();

  @NotNull
  List<PasClassMethodResolution> getClassMethodResolutionList();

  @Nullable
  PasClassParent getClassParent();

  @NotNull
  List<PasClassProperty> getClassPropertyList();

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
  PsiElement getDispinterface();

  @Nullable
  PsiElement getEnd();

  @Nullable
  PsiElement getExternal();

  @Nullable
  PsiElement getIndex();

  @Nullable
  PsiElement getInterface();

  @Nullable
  PsiElement getStringLiteral();

}
