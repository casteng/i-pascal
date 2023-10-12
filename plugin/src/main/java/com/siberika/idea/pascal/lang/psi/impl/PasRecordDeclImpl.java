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
import com.siberika.idea.pascal.lang.stub.struct.PasRecordDeclStub;
import com.intellij.psi.stubs.IStubElementType;

public class PasRecordDeclImpl extends PascalRecordDeclImpl implements PasRecordDecl {

  public PasRecordDeclImpl(ASTNode node) {
    super(node);
  }

  public PasRecordDeclImpl(PasRecordDeclStub stub, IStubElementType type) {
    super(stub, type);
  }

  public void accept(@NotNull PasVisitor visitor) {
    visitor.visitRecordDecl(this);
  }

  @Override
  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof PasVisitor) accept((PasVisitor)visitor);
    else super.accept(visitor);
  }

  @Override
  @NotNull
  public List<PasClassField> getClassFieldList() {
    return PsiTreeUtil.getChildrenOfTypeAsList(this, PasClassField.class);
  }

  @Override
  @NotNull
  public List<PasClassMethodResolution> getClassMethodResolutionList() {
    return PsiTreeUtil.getChildrenOfTypeAsList(this, PasClassMethodResolution.class);
  }

  @Override
  @Nullable
  public PasClassParent getClassParent() {
    return PsiTreeUtil.getChildOfType(this, PasClassParent.class);
  }

  @Override
  @NotNull
  public List<PasClassProperty> getClassPropertyList() {
    return PsiTreeUtil.getChildrenOfTypeAsList(this, PasClassProperty.class);
  }

  @Override
  @NotNull
  public List<PasConstSection> getConstSectionList() {
    return PsiTreeUtil.getChildrenOfTypeAsList(this, PasConstSection.class);
  }

  @Override
  @NotNull
  public List<PasExportedRoutine> getExportedRoutineList() {
    return PsiTreeUtil.getStubChildrenOfTypeAsList(this, PasExportedRoutine.class);
  }

  @Override
  @Nullable
  public PasNamedIdentDecl getNamedIdentDecl() {
    return PsiTreeUtil.getStubChildOfType(this, PasNamedIdentDecl.class);
  }

  @Override
  @NotNull
  public List<PasRecordVariant> getRecordVariantList() {
    return PsiTreeUtil.getChildrenOfTypeAsList(this, PasRecordVariant.class);
  }

  @Override
  @Nullable
  public PasTypeDecl getTypeDecl() {
    return PsiTreeUtil.getChildOfType(this, PasTypeDecl.class);
  }

  @Override
  @NotNull
  public List<PasTypeSection> getTypeSectionList() {
    return PsiTreeUtil.getChildrenOfTypeAsList(this, PasTypeSection.class);
  }

  @Override
  @NotNull
  public List<PasVarSection> getVarSectionList() {
    return PsiTreeUtil.getChildrenOfTypeAsList(this, PasVarSection.class);
  }

  @Override
  @NotNull
  public List<PasVisibility> getVisibilityList() {
    return PsiTreeUtil.getChildrenOfTypeAsList(this, PasVisibility.class);
  }

  @Override
  @Nullable
  public PsiElement getCase() {
    return findChildByType(CASE);
  }

  @Override
  @Nullable
  public PsiElement getEnd() {
    return findChildByType(END);
  }

  @Override
  @Nullable
  public PsiElement getOf() {
    return findChildByType(OF);
  }

  @Override
  @Nullable
  public PsiElement getPacked() {
    return findChildByType(PACKED);
  }

  @Override
  @NotNull
  public PsiElement getRecord() {
    return notNullChild(findChildByType(RECORD));
  }

}
