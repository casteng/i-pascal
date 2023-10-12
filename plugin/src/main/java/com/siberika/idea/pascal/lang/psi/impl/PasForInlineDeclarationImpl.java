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

public class PasForInlineDeclarationImpl extends PascalPsiElementImpl implements PasForInlineDeclaration {

  public PasForInlineDeclarationImpl(ASTNode node) {
    super(node);
  }

  public void accept(@NotNull PasVisitor visitor) {
    visitor.visitForInlineDeclaration(this);
  }

  @Override
  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof PasVisitor) accept((PasVisitor)visitor);
    else super.accept(visitor);
  }

  @Override
  @Nullable
  public PasNamedIdent getNamedIdent() {
    return PsiTreeUtil.getChildOfType(this, PasNamedIdent.class);
  }

  @Override
  @Nullable
  public PasTypeDecl getTypeDecl() {
    return PsiTreeUtil.getChildOfType(this, PasTypeDecl.class);
  }

  @Override
  @NotNull
  public PsiElement getVar() {
    return notNullChild(findChildByType(VAR));
  }

  @Override
  public @NotNull List<PasCustomAttributeDecl> getCustomAttributeDeclList() {
    return PascalPsiImplUtil.getCustomAttributeDeclList(this);
  }

  @Override
  public @NotNull List<? extends PascalNamedElement> getNamedIdentDeclList() {
    return PascalPsiImplUtil.getNamedIdentDeclList(this);
  }

}
