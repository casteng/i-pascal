// This is a generated file. Not intended for manual editing.
package com.siberika.idea.pascal.lang.psi;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElement;
import com.intellij.psi.StubBasedPsiElement;
import com.siberika.idea.pascal.lang.stub.PasModuleStub;

public interface PasModule extends PascalModule, StubBasedPsiElement<PasModuleStub> {

  @Nullable
  PasBlockGlobal getBlockGlobal();

  @NotNull
  List<PasClassMethodResolution> getClassMethodResolutionList();

  @NotNull
  List<PasClassProperty> getClassPropertyList();

  @Nullable
  PasCompoundStatement getCompoundStatement();

  @NotNull
  List<PasConstSection> getConstSectionList();

  @Nullable
  PasContainsClause getContainsClause();

  @NotNull
  List<PasCustomAttributeDecl> getCustomAttributeDeclList();

  @NotNull
  List<PasExportedRoutine> getExportedRoutineList();

  @NotNull
  List<PasExportsSection> getExportsSectionList();

  @Nullable
  PasImplDeclSection getImplDeclSection();

  @NotNull
  List<PasLabelDeclSection> getLabelDeclSectionList();

  @Nullable
  PasLibraryModuleHead getLibraryModuleHead();

  @Nullable
  PasPackageModuleHead getPackageModuleHead();

  @Nullable
  PasProgramModuleHead getProgramModuleHead();

  @Nullable
  PasRequiresClause getRequiresClause();

  @NotNull
  List<PasRoutineImplDecl> getRoutineImplDeclList();

  @NotNull
  List<PasTypeSection> getTypeSectionList();

  @Nullable
  PasUnitFinalization getUnitFinalization();

  @Nullable
  PasUnitImplementation getUnitImplementation();

  @Nullable
  PasUnitInitialization getUnitInitialization();

  @Nullable
  PasUnitInterface getUnitInterface();

  @Nullable
  PasUnitModuleHead getUnitModuleHead();

  @Nullable
  PasUsesClause getUsesClause();

  @NotNull
  List<PasVarSection> getVarSectionList();

}
