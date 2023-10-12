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

public class PasBlockLocalNested1Impl extends PascalPsiElementImpl implements PasBlockLocalNested1 {

  public PasBlockLocalNested1Impl(ASTNode node) {
    super(node);
  }

  public void accept(@NotNull PasVisitor visitor) {
    visitor.visitBlockLocalNested1(this);
  }

  @Override
  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof PasVisitor) accept((PasVisitor)visitor);
    else super.accept(visitor);
  }

  @Override
  @Nullable
  public PasBlockBody getBlockBody() {
    return PsiTreeUtil.getChildOfType(this, PasBlockBody.class);
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
  public List<PasLabelDeclSection> getLabelDeclSectionList() {
    return PsiTreeUtil.getChildrenOfTypeAsList(this, PasLabelDeclSection.class);
  }

  @Override
  @NotNull
  public List<PasRoutineImplDeclWoNested> getRoutineImplDeclWoNestedList() {
    return PsiTreeUtil.getChildrenOfTypeAsList(this, PasRoutineImplDeclWoNested.class);
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

}
