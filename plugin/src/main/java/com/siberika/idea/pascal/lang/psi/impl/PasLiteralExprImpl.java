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

public class PasLiteralExprImpl extends PasExprImpl implements PasLiteralExpr {

  public PasLiteralExprImpl(ASTNode node) {
    super(node);
  }

  @Override
  public void accept(@NotNull PasVisitor visitor) {
    visitor.visitLiteralExpr(this);
  }

  @Override
  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof PasVisitor) accept((PasVisitor)visitor);
    else super.accept(visitor);
  }

  @Override
  @Nullable
  public PasStringFactor getStringFactor() {
    return PsiTreeUtil.getChildOfType(this, PasStringFactor.class);
  }

  @Override
  @Nullable
  public PsiElement getFalse() {
    return findChildByType(FALSE);
  }

  @Override
  @Nullable
  public PsiElement getNil() {
    return findChildByType(NIL);
  }

  @Override
  @Nullable
  public PsiElement getNumberBin() {
    return findChildByType(NUMBER_BIN);
  }

  @Override
  @Nullable
  public PsiElement getNumberHex() {
    return findChildByType(NUMBER_HEX);
  }

  @Override
  @Nullable
  public PsiElement getNumberInt() {
    return findChildByType(NUMBER_INT);
  }

  @Override
  @Nullable
  public PsiElement getNumberOct() {
    return findChildByType(NUMBER_OCT);
  }

  @Override
  @Nullable
  public PsiElement getNumberReal() {
    return findChildByType(NUMBER_REAL);
  }

  @Override
  @Nullable
  public PsiElement getTrue() {
    return findChildByType(TRUE);
  }

}
