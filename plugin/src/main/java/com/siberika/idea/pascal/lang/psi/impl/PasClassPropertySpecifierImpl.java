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

public class PasClassPropertySpecifierImpl extends PascalPsiElementImpl implements PasClassPropertySpecifier {

  public PasClassPropertySpecifierImpl(ASTNode node) {
    super(node);
  }

  public void accept(@NotNull PasVisitor visitor) {
    visitor.visitClassPropertySpecifier(this);
  }

  @Override
  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof PasVisitor) accept((PasVisitor)visitor);
    else super.accept(visitor);
  }

  @Override
  @Nullable
  public PasExpression getExpression() {
    return PsiTreeUtil.getChildOfType(this, PasExpression.class);
  }

  @Override
  @Nullable
  public PasRefNamedIdent getRefNamedIdent() {
    return PsiTreeUtil.getChildOfType(this, PasRefNamedIdent.class);
  }

  @Override
  @Nullable
  public PasTypeID getTypeID() {
    return PsiTreeUtil.getChildOfType(this, PasTypeID.class);
  }

  @Override
  @Nullable
  public PsiElement getDefault() {
    return findChildByType(DEFAULT);
  }

  @Override
  @Nullable
  public PsiElement getDispid() {
    return findChildByType(DISPID);
  }

  @Override
  @Nullable
  public PsiElement getImplements() {
    return findChildByType(IMPLEMENTS);
  }

  @Override
  @Nullable
  public PsiElement getRead() {
    return findChildByType(READ);
  }

  @Override
  @Nullable
  public PsiElement getWrite() {
    return findChildByType(WRITE);
  }

}
