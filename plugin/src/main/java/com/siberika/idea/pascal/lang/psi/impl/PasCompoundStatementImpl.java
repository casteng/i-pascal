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

public class PasCompoundStatementImpl extends PasStatementImpl implements PasCompoundStatement {

  public PasCompoundStatementImpl(ASTNode node) {
    super(node);
  }

  @Override
  public void accept(@NotNull PasVisitor visitor) {
    visitor.visitCompoundStatement(this);
  }

  @Override
  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof PasVisitor) accept((PasVisitor)visitor);
    else super.accept(visitor);
  }

  @Override
  @NotNull
  public List<PasStatement> getStatementList() {
    return PsiTreeUtil.getChildrenOfTypeAsList(this, PasStatement.class);
  }

  @Override
  @NotNull
  public PsiElement getBegin() {
    return notNullChild(findChildByType(BEGIN));
  }

  @Override
  @Nullable
  public PsiElement getEnd() {
    return findChildByType(END);
  }

}
