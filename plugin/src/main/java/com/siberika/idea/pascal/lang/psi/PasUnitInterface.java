// This is a generated file. Not intended for manual editing.
package com.siberika.idea.pascal.lang.psi;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElement;

public interface PasUnitInterface extends PascalPsiElement {

  @NotNull
  List<PasClassMethodResolution> getClassMethodResolutionList();

  @NotNull
  List<PasClassProperty> getClassPropertyList();

  @NotNull
  List<PasConstSection> getConstSectionList();

  @NotNull
  List<PasCustomAttributeDecl> getCustomAttributeDeclList();

  @NotNull
  List<PasExportedRoutine> getExportedRoutineList();

  @NotNull
  List<PasExportsSection> getExportsSectionList();

  @NotNull
  List<PasTypeSection> getTypeSectionList();

  @Nullable
  PasUsesClause getUsesClause();

  @NotNull
  List<PasVarSection> getVarSectionList();

  @NotNull
  PsiElement getInterface();

}
