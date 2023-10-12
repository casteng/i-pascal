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

public class PasWithStatementImpl extends PasStatementImpl implements PasWithStatement {

  public PasWithStatementImpl(ASTNode node) {
    super(node);
  }

  @Override
  public void accept(@NotNull PasVisitor visitor) {
    visitor.visitWithStatement(this);
  }

  @Override
  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof PasVisitor) accept((PasVisitor)visitor);
    else super.accept(visitor);
  }

  @Override
  @NotNull
  public List<PasExpression> getExpressionList() {
    return PsiTreeUtil.getChildrenOfTypeAsList(this, PasExpression.class);
  }

  @Override
  @Nullable
  public PasStatement getStatement() {
    return PsiTreeUtil.getChildOfType(this, PasStatement.class);
  }

  @Override
  @Nullable
  public PsiElement getDo() {
    return findChildByType(DO);
  }

  @Override
  @NotNull
  public PsiElement getWith() {
    return notNullChild(findChildByType(WITH));
  }

}
