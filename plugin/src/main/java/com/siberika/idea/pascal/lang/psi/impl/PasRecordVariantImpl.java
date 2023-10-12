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

public class PasRecordVariantImpl extends PascalPsiElementImpl implements PasRecordVariant {

  public PasRecordVariantImpl(ASTNode node) {
    super(node);
  }

  public void accept(@NotNull PasVisitor visitor) {
    visitor.visitRecordVariant(this);
  }

  @Override
  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof PasVisitor) accept((PasVisitor)visitor);
    else super.accept(visitor);
  }

  @Override
  @NotNull
  public List<PasClassField> getClassFieldList() {
    return PsiTreeUtil.getChildrenOfTypeAsList(this, PasClassField.class);
  }

  @Override
  @NotNull
  public List<PasConstExpressionOrd> getConstExpressionOrdList() {
    return PsiTreeUtil.getChildrenOfTypeAsList(this, PasConstExpressionOrd.class);
  }

  @Override
  @Nullable
  public PasNamedIdentDecl getNamedIdentDecl() {
    return PsiTreeUtil.getChildOfType(this, PasNamedIdentDecl.class);
  }

  @Override
  @NotNull
  public List<PasRecordVariant> getRecordVariantList() {
    return PsiTreeUtil.getChildrenOfTypeAsList(this, PasRecordVariant.class);
  }

  @Override
  @Nullable
  public PasTypeDecl getTypeDecl() {
    return PsiTreeUtil.getChildOfType(this, PasTypeDecl.class);
  }

  @Override
  @Nullable
  public PsiElement getCase() {
    return findChildByType(CASE);
  }

  @Override
  @Nullable
  public PsiElement getOf() {
    return findChildByType(OF);
  }

}
