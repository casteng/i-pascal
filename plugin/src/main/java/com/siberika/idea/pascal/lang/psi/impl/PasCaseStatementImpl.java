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

public class PasCaseStatementImpl extends PasStatementImpl implements PasCaseStatement {

  public PasCaseStatementImpl(ASTNode node) {
    super(node);
  }

  @Override
  public void accept(@NotNull PasVisitor visitor) {
    visitor.visitCaseStatement(this);
  }

  @Override
  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof PasVisitor) accept((PasVisitor)visitor);
    else super.accept(visitor);
  }

  @Override
  @Nullable
  public PasCaseElse getCaseElse() {
    return PsiTreeUtil.getChildOfType(this, PasCaseElse.class);
  }

  @Override
  @NotNull
  public List<PasCaseItem> getCaseItemList() {
    return PsiTreeUtil.getChildrenOfTypeAsList(this, PasCaseItem.class);
  }

  @Override
  @Nullable
  public PasExpression getExpression() {
    return PsiTreeUtil.getChildOfType(this, PasExpression.class);
  }

  @Override
  @NotNull
  public PsiElement getCase() {
    return notNullChild(findChildByType(CASE));
  }

  @Override
  @NotNull
  public PsiElement getEnd() {
    return notNullChild(findChildByType(END));
  }

  @Override
  @Nullable
  public PsiElement getOf() {
    return findChildByType(OF);
  }

}
