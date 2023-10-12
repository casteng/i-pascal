// This is a generated file. Not intended for manual editing.
package com.siberika.idea.pascal.lang.psi.impl;

import static com.siberika.idea.pascal.lang.psi.PasTypes.END;
import static com.siberika.idea.pascal.lang.psi.PasTypes.FOR;
import static com.siberika.idea.pascal.lang.psi.PasTypes.HELPER;
import static com.siberika.idea.pascal.lang.psi.PasTypes.RECORD;

import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.stubs.IStubElementType;
import com.intellij.psi.util.PsiTreeUtil;
import com.siberika.idea.pascal.lang.psi.PasClassField;
import com.siberika.idea.pascal.lang.psi.PasClassMethodResolution;
import com.siberika.idea.pascal.lang.psi.PasClassParent;
import com.siberika.idea.pascal.lang.psi.PasClassProperty;
import com.siberika.idea.pascal.lang.psi.PasConstSection;
import com.siberika.idea.pascal.lang.psi.PasExportedRoutine;
import com.siberika.idea.pascal.lang.psi.PasRecordHelperDecl;
import com.siberika.idea.pascal.lang.psi.PasTypeID;
import com.siberika.idea.pascal.lang.psi.PasTypeSection;
import com.siberika.idea.pascal.lang.psi.PasVarSection;
import com.siberika.idea.pascal.lang.psi.PasVisibility;
import com.siberika.idea.pascal.lang.psi.PasVisitor;
import com.siberika.idea.pascal.lang.stub.struct.PasHelperDeclStub;
import java.util.List;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class PasRecordHelperDeclImpl extends PascalRecordHelperDeclImpl implements PasRecordHelperDecl {

  public PasRecordHelperDeclImpl(ASTNode node) {
    super(node);
  }

  public PasRecordHelperDeclImpl(PasHelperDeclStub stub, IStubElementType type) {
    super(stub, type);
  }

  public void accept(@NotNull PasVisitor visitor) {
    visitor.visitRecordHelperDecl(this);
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
  public PasTypeID getTypeID() {
    return PsiTreeUtil.getChildOfType(this, PasTypeID.class);
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
  public PsiElement getEnd() {
    return findChildByType(END);
  }

  @Override
  @Nullable
  public PsiElement getFor() {
    return findChildByType(FOR);
  }

  @Override
  @NotNull
  public PsiElement getHelper() {
    return notNullChild(findChildByType(HELPER));
  }

  @Override
  @NotNull
  public PsiElement getRecord() {
    return notNullChild(findChildByType(RECORD));
  }

  @Override
  @Nullable
  public PsiElement getPasName() {
    return getNameIdentifier();
  }
}
