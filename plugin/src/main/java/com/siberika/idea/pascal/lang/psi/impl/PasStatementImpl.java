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

public class PasStatementImpl extends PascalPsiElementImpl implements PasStatement {

  public PasStatementImpl(ASTNode node) {
    super(node);
  }

  public void accept(@NotNull PasVisitor visitor) {
    visitor.visitStatement(this);
  }

  @Override
  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof PasVisitor) accept((PasVisitor)visitor);
    else super.accept(visitor);
  }

  @Override
  @Nullable
  public PasAssemblerStatement getAssemblerStatement() {
    return PsiTreeUtil.getChildOfType(this, PasAssemblerStatement.class);
  }

  @Override
  @Nullable
  public PasAssignPart getAssignPart() {
    return PsiTreeUtil.getChildOfType(this, PasAssignPart.class);
  }

  @Override
  @Nullable
  public PasBreakStatement getBreakStatement() {
    return PsiTreeUtil.getChildOfType(this, PasBreakStatement.class);
  }

  @Override
  @Nullable
  public PasCaseStatement getCaseStatement() {
    return PsiTreeUtil.getChildOfType(this, PasCaseStatement.class);
  }

  @Override
  @Nullable
  public PasCompoundStatement getCompoundStatement() {
    return PsiTreeUtil.getChildOfType(this, PasCompoundStatement.class);
  }

  @Override
  @Nullable
  public PasContinueStatement getContinueStatement() {
    return PsiTreeUtil.getChildOfType(this, PasContinueStatement.class);
  }

  @Override
  @Nullable
  public PasExitStatement getExitStatement() {
    return PsiTreeUtil.getChildOfType(this, PasExitStatement.class);
  }

  @Override
  @Nullable
  public PasExpression getExpression() {
    return PsiTreeUtil.getChildOfType(this, PasExpression.class);
  }

  @Override
  @Nullable
  public PasForStatement getForStatement() {
    return PsiTreeUtil.getChildOfType(this, PasForStatement.class);
  }

  @Override
  @Nullable
  public PasGotoStatement getGotoStatement() {
    return PsiTreeUtil.getChildOfType(this, PasGotoStatement.class);
  }

  @Override
  @Nullable
  public PasIfStatement getIfStatement() {
    return PsiTreeUtil.getChildOfType(this, PasIfStatement.class);
  }

  @Override
  @Nullable
  public PasInlineConstDeclaration getInlineConstDeclaration() {
    return PsiTreeUtil.getChildOfType(this, PasInlineConstDeclaration.class);
  }

  @Override
  @Nullable
  public PasInlineVarDeclaration getInlineVarDeclaration() {
    return PsiTreeUtil.getChildOfType(this, PasInlineVarDeclaration.class);
  }

  @Override
  @Nullable
  public PasLabelId getLabelId() {
    return PsiTreeUtil.getChildOfType(this, PasLabelId.class);
  }

  @Override
  @Nullable
  public PasRaiseStatement getRaiseStatement() {
    return PsiTreeUtil.getChildOfType(this, PasRaiseStatement.class);
  }

  @Override
  @Nullable
  public PasRepeatStatement getRepeatStatement() {
    return PsiTreeUtil.getChildOfType(this, PasRepeatStatement.class);
  }

  @Override
  @Nullable
  public PasTryStatement getTryStatement() {
    return PsiTreeUtil.getChildOfType(this, PasTryStatement.class);
  }

  @Override
  @Nullable
  public PasWhileStatement getWhileStatement() {
    return PsiTreeUtil.getChildOfType(this, PasWhileStatement.class);
  }

  @Override
  @Nullable
  public PasWithStatement getWithStatement() {
    return PsiTreeUtil.getChildOfType(this, PasWithStatement.class);
  }

  @Override
  @Nullable
  public PasStmtEmpty getStmtEmpty() {
    return PsiTreeUtil.getChildOfType(this, PasStmtEmpty.class);
  }

}
