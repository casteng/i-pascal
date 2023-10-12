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

public class PasForStatementImpl extends PasStatementImpl implements PasForStatement {

  public PasForStatementImpl(ASTNode node) {
    super(node);
  }

  @Override
  public void accept(@NotNull PasVisitor visitor) {
    visitor.visitForStatement(this);
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
  public PasForInlineDeclaration getForInlineDeclaration() {
    return PsiTreeUtil.getChildOfType(this, PasForInlineDeclaration.class);
  }

  @Override
  @Nullable
  public PasFromExpression getFromExpression() {
    return PsiTreeUtil.getChildOfType(this, PasFromExpression.class);
  }

  @Override
  @Nullable
  public PasFullyQualifiedIdent getFullyQualifiedIdent() {
    return PsiTreeUtil.getChildOfType(this, PasFullyQualifiedIdent.class);
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
  @Nullable
  public PsiElement getDownto() {
    return findChildByType(DOWNTO);
  }

  @Override
  @NotNull
  public PsiElement getFor() {
    return notNullChild(findChildByType(FOR));
  }

  @Override
  @Nullable
  public PsiElement getIn() {
    return findChildByType(IN);
  }

  @Override
  @Nullable
  public PsiElement getTo() {
    return findChildByType(TO);
  }

}
