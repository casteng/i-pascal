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

public class PasGenericConstraintImpl extends PascalPsiElementImpl implements PasGenericConstraint {

  public PasGenericConstraintImpl(ASTNode node) {
    super(node);
  }

  public void accept(@NotNull PasVisitor visitor) {
    visitor.visitGenericConstraint(this);
  }

  @Override
  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof PasVisitor) accept((PasVisitor)visitor);
    else super.accept(visitor);
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
  public PsiElement getConstructor() {
    return findChildByType(CONSTRUCTOR);
  }

  @Override
  @Nullable
  public PsiElement getInterface() {
    return findChildByType(INTERFACE);
  }

  @Override
  @Nullable
  public PsiElement getObject() {
    return findChildByType(OBJECT);
  }

  @Override
  @Nullable
  public PsiElement getRecord() {
    return findChildByType(RECORD);
  }

}
