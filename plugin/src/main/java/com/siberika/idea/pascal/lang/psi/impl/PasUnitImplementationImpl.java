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

public class PasUnitImplementationImpl extends PascalPsiElementImpl implements PasUnitImplementation {

  public PasUnitImplementationImpl(ASTNode node) {
    super(node);
  }

  public void accept(@NotNull PasVisitor visitor) {
    visitor.visitUnitImplementation(this);
  }

  @Override
  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof PasVisitor) accept((PasVisitor)visitor);
    else super.accept(visitor);
  }

  @Override
  @Nullable
  public PasCompoundStatement getCompoundStatement() {
    return PsiTreeUtil.getChildOfType(this, PasCompoundStatement.class);
  }

  @Override
  @Nullable
  public PasImplDeclSection getImplDeclSection() {
    return PsiTreeUtil.getChildOfType(this, PasImplDeclSection.class);
  }

  @Override
  @Nullable
  public PasUnitFinalization getUnitFinalization() {
    return PsiTreeUtil.getChildOfType(this, PasUnitFinalization.class);
  }

  @Override
  @Nullable
  public PasUnitInitialization getUnitInitialization() {
    return PsiTreeUtil.getChildOfType(this, PasUnitInitialization.class);
  }

  @Override
  @Nullable
  public PasUsesClause getUsesClause() {
    return PsiTreeUtil.getChildOfType(this, PasUsesClause.class);
  }

  @Override
  @Nullable
  public PsiElement getEnd() {
    return findChildByType(END);
  }

  @Override
  @NotNull
  public PsiElement getImplementation() {
    return notNullChild(findChildByType(IMPLEMENTATION));
  }

}
