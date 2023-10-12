// This is a generated file. Not intended for manual editing.
package com.siberika.idea.pascal.lang.psi.impl;

import static com.siberika.idea.pascal.lang.psi.PasTypes.DISPINTERFACE;
import static com.siberika.idea.pascal.lang.psi.PasTypes.END;
import static com.siberika.idea.pascal.lang.psi.PasTypes.EXTERNAL;
import static com.siberika.idea.pascal.lang.psi.PasTypes.INDEX;
import static com.siberika.idea.pascal.lang.psi.PasTypes.INTERFACE;
import static com.siberika.idea.pascal.lang.psi.PasTypes.NAME_;
import static com.siberika.idea.pascal.lang.psi.PasTypes.STRING_LITERAL;

import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.stubs.IStubElementType;
import com.intellij.psi.util.PsiTreeUtil;
import com.siberika.idea.pascal.lang.psi.PasClassField;
import com.siberika.idea.pascal.lang.psi.PasClassMethodResolution;
import com.siberika.idea.pascal.lang.psi.PasClassParent;
import com.siberika.idea.pascal.lang.psi.PasClassProperty;
import com.siberika.idea.pascal.lang.psi.PasConstExpression;
import com.siberika.idea.pascal.lang.psi.PasConstSection;
import com.siberika.idea.pascal.lang.psi.PasExportedRoutine;
import com.siberika.idea.pascal.lang.psi.PasInterfaceTypeDecl;
import com.siberika.idea.pascal.lang.psi.PasTypeSection;
import com.siberika.idea.pascal.lang.psi.PasVarSection;
import com.siberika.idea.pascal.lang.psi.PasVisibility;
import com.siberika.idea.pascal.lang.psi.PasVisitor;
import com.siberika.idea.pascal.lang.stub.struct.PasInterfaceDeclStub;
import java.util.List;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class PasInterfaceTypeDeclImpl extends PascalInterfaceDeclImpl implements PasInterfaceTypeDecl {

  public PasInterfaceTypeDeclImpl(ASTNode node) {
    super(node);
  }

  public PasInterfaceTypeDeclImpl(PasInterfaceDeclStub stub, IStubElementType type) {
    super(stub, type);
  }

  public void accept(@NotNull PasVisitor visitor) {
    visitor.visitInterfaceTypeDecl(this);
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
  @Nullable
  public PasConstExpression getConstExpression() {
    return PsiTreeUtil.getChildOfType(this, PasConstExpression.class);
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
  public PsiElement getDispinterface() {
    return findChildByType(DISPINTERFACE);
  }

  @Override
  @Nullable
  public PsiElement getEnd() {
    return findChildByType(END);
  }

  @Override
  @Nullable
  public PsiElement getExternal() {
    return findChildByType(EXTERNAL);
  }

  @Override
  @Nullable
  public PsiElement getIndex() {
    return findChildByType(INDEX);
  }

  @Override
  @Nullable
  public PsiElement getInterface() {
    return findChildByType(INTERFACE);
  }

  @Override
  @Nullable
  public PsiElement getStringLiteral() {
    return findChildByType(STRING_LITERAL);
  }

  @Override
  public @Nullable PsiElement getPasName() {
    return findChildByType(NAME_);
  }

}
