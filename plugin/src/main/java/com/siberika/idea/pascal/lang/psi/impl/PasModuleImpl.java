// This is a generated file. Not intended for manual editing.
package com.siberika.idea.pascal.lang.psi.impl;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.util.PsiTreeUtil;
import static com.siberika.idea.pascal.lang.psi.PasTypes.*;
import com.siberika.idea.pascal.lang.psi.*;
import com.siberika.idea.pascal.lang.stub.PasModuleStub;
import com.intellij.psi.stubs.IStubElementType;

public class PasModuleImpl extends PascalModuleImpl implements PasModule {

  public PasModuleImpl(ASTNode node) {
    super(node);
  }

  public PasModuleImpl(@NotNull PasModuleStub stub, IStubElementType type) {
    super(stub, type);
  }

  public void accept(@NotNull PasVisitor visitor) {
    visitor.visitModule(this);
  }

  @Override
  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof PasVisitor) accept((PasVisitor)visitor);
    else super.accept(visitor);
  }

  @Override
  @Nullable
  public PasBlockGlobal getBlockGlobal() {
    return PsiTreeUtil.getChildOfType(this, PasBlockGlobal.class);
  }

  @Override
  @NotNull
  public List<PasClassMethodResolution> getClassMethodResolutionList() {
    return PsiTreeUtil.getChildrenOfTypeAsList(this, PasClassMethodResolution.class);
  }

  @Override
  @NotNull
  public List<PasClassProperty> getClassPropertyList() {
    return PsiTreeUtil.getChildrenOfTypeAsList(this, PasClassProperty.class);
  }

  @Override
  @Nullable
  public PasCompoundStatement getCompoundStatement() {
    return PsiTreeUtil.getChildOfType(this, PasCompoundStatement.class);
  }

  @Override
  @NotNull
  public List<PasConstSection> getConstSectionList() {
    return PsiTreeUtil.getChildrenOfTypeAsList(this, PasConstSection.class);
  }

  @Override
  @Nullable
  public PasContainsClause getContainsClause() {
    return PsiTreeUtil.getChildOfType(this, PasContainsClause.class);
  }

  @Override
  @NotNull
  public List<PasCustomAttributeDecl> getCustomAttributeDeclList() {
    return PsiTreeUtil.getChildrenOfTypeAsList(this, PasCustomAttributeDecl.class);
  }

  @Override
  @NotNull
  public List<PasExportedRoutine> getExportedRoutineList() {
    return PsiTreeUtil.getStubChildrenOfTypeAsList(this, PasExportedRoutine.class);
  }

  @Override
  @NotNull
  public List<PasExportsSection> getExportsSectionList() {
    return PsiTreeUtil.getChildrenOfTypeAsList(this, PasExportsSection.class);
  }

  @Override
  @Nullable
  public PasImplDeclSection getImplDeclSection() {
    return PsiTreeUtil.getChildOfType(this, PasImplDeclSection.class);
  }

  @Override
  @NotNull
  public List<PasLabelDeclSection> getLabelDeclSectionList() {
    return PsiTreeUtil.getChildrenOfTypeAsList(this, PasLabelDeclSection.class);
  }

  @Override
  @Nullable
  public PasLibraryModuleHead getLibraryModuleHead() {
    return PsiTreeUtil.getChildOfType(this, PasLibraryModuleHead.class);
  }

  @Override
  @Nullable
  public PasPackageModuleHead getPackageModuleHead() {
    return PsiTreeUtil.getChildOfType(this, PasPackageModuleHead.class);
  }

  @Override
  @Nullable
  public PasProgramModuleHead getProgramModuleHead() {
    return PsiTreeUtil.getChildOfType(this, PasProgramModuleHead.class);
  }

  @Override
  @Nullable
  public PasRequiresClause getRequiresClause() {
    return PsiTreeUtil.getChildOfType(this, PasRequiresClause.class);
  }

  @Override
  @NotNull
  public List<PasRoutineImplDecl> getRoutineImplDeclList() {
    return PsiTreeUtil.getChildrenOfTypeAsList(this, PasRoutineImplDecl.class);
  }

  @Override
  @NotNull
  public List<PasTypeSection> getTypeSectionList() {
    return PsiTreeUtil.getChildrenOfTypeAsList(this, PasTypeSection.class);
  }

  @Override
  @Nullable
  public PasUnitFinalization getUnitFinalization() {
    return PsiTreeUtil.getChildOfType(this, PasUnitFinalization.class);
  }

  @Override
  @Nullable
  public PasUnitImplementation getUnitImplementation() {
    return PsiTreeUtil.getChildOfType(this, PasUnitImplementation.class);
  }

  @Override
  @Nullable
  public PasUnitInitialization getUnitInitialization() {
    return PsiTreeUtil.getChildOfType(this, PasUnitInitialization.class);
  }

  @Override
  @Nullable
  public PasUnitInterface getUnitInterface() {
    return PsiTreeUtil.getChildOfType(this, PasUnitInterface.class);
  }

  @Override
  @Nullable
  public PasUnitModuleHead getUnitModuleHead() {
    return PsiTreeUtil.getChildOfType(this, PasUnitModuleHead.class);
  }

  @Override
  @Nullable
  public PasUsesClause getUsesClause() {
    return PsiTreeUtil.getChildOfType(this, PasUsesClause.class);
  }

  @Override
  @NotNull
  public List<PasVarSection> getVarSectionList() {
    return PsiTreeUtil.getChildrenOfTypeAsList(this, PasVarSection.class);
  }

}
