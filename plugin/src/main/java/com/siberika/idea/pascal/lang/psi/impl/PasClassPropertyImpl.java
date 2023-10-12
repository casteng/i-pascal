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

public class PasClassPropertyImpl extends PascalPsiElementImpl implements PasClassProperty {

  public PasClassPropertyImpl(ASTNode node) {
    super(node);
  }

  public void accept(@NotNull PasVisitor visitor) {
    visitor.visitClassProperty(this);
  }

  @Override
  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof PasVisitor) accept((PasVisitor)visitor);
    else super.accept(visitor);
  }

  @Override
  @Nullable
  public PasClassPropertyArray getClassPropertyArray() {
    return PsiTreeUtil.getChildOfType(this, PasClassPropertyArray.class);
  }

  @Override
  @Nullable
  public PasClassPropertyIndex getClassPropertyIndex() {
    return PsiTreeUtil.getChildOfType(this, PasClassPropertyIndex.class);
  }

  @Override
  @NotNull
  public List<PasClassPropertySpecifier> getClassPropertySpecifierList() {
    return PsiTreeUtil.getChildrenOfTypeAsList(this, PasClassPropertySpecifier.class);
  }

  @Override
  @NotNull
  public List<PasCustomAttributeDecl> getCustomAttributeDeclList() {
    return PsiTreeUtil.getChildrenOfTypeAsList(this, PasCustomAttributeDecl.class);
  }

  @Override
  @Nullable
  public PasNamedIdentDecl getNamedIdentDecl() {
    return PsiTreeUtil.getChildOfType(this, PasNamedIdentDecl.class);
  }

  @Override
  @NotNull
  public List<PasStringFactor> getStringFactorList() {
    return PsiTreeUtil.getChildrenOfTypeAsList(this, PasStringFactor.class);
  }

  @Override
  @Nullable
  public PasTypeID getTypeID() {
    return PsiTreeUtil.getChildOfType(this, PasTypeID.class);
  }

  @Override
  @Nullable
  public PsiElement getPasClass() {
    return findChildByType(CLASS);
  }

  @Override
  @Nullable
  public PsiElement getDefault() {
    return findChildByType(DEFAULT);
  }

  @Override
  @NotNull
  public PsiElement getProperty() {
    return notNullChild(findChildByType(PROPERTY));
  }

}
