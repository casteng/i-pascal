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

public class PasVarDeclarationImpl extends PascalPsiElementImpl implements PasVarDeclaration {

  public PasVarDeclarationImpl(ASTNode node) {
    super(node);
  }

  public void accept(@NotNull PasVisitor visitor) {
    visitor.visitVarDeclaration(this);
  }

  @Override
  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof PasVisitor) accept((PasVisitor)visitor);
    else super.accept(visitor);
  }

  @Override
  @NotNull
  public List<PasCustomAttributeDecl> getCustomAttributeDeclList() {
    return PsiTreeUtil.getChildrenOfTypeAsList(this, PasCustomAttributeDecl.class);
  }

  @Override
  @NotNull
  public List<PasNamedIdentDecl> getNamedIdentDeclList() {
    return PsiTreeUtil.getChildrenOfTypeAsList(this, PasNamedIdentDecl.class);
  }

  @Override
  @NotNull
  public List<PasStringFactor> getStringFactorList() {
    return PsiTreeUtil.getChildrenOfTypeAsList(this, PasStringFactor.class);
  }

  @Override
  @Nullable
  public PasTypeDecl getTypeDecl() {
    return PsiTreeUtil.getChildOfType(this, PasTypeDecl.class);
  }

  @Override
  @Nullable
  public PasVarValueSpec getVarValueSpec() {
    return PsiTreeUtil.getChildOfType(this, PasVarValueSpec.class);
  }

}
