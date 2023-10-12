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

public class PasRelationalExprImpl extends PasExprImpl implements PasRelationalExpr {

  public PasRelationalExprImpl(ASTNode node) {
    super(node);
  }

  @Override
  public void accept(@NotNull PasVisitor visitor) {
    visitor.visitRelationalExpr(this);
  }

  @Override
  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof PasVisitor) accept((PasVisitor)visitor);
    else super.accept(visitor);
  }

  @Override
  @NotNull
  public List<PasExpr> getExprList() {
    return PsiTreeUtil.getChildrenOfTypeAsList(this, PasExpr.class);
  }

  @Override
  @NotNull
  public PasRelOp getRelOp() {
    return notNullChild(PsiTreeUtil.getChildOfType(this, PasRelOp.class));
  }

}
