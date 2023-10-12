// This is a generated file. Not intended for manual editing.
package com.siberika.idea.pascal.lang.psi.impl;

import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.stubs.IStubElementType;
import com.intellij.psi.util.PsiTreeUtil;
import com.siberika.idea.pascal.lang.psi.PasEscapedIdent;
import com.siberika.idea.pascal.lang.psi.PasKeywordIdent;
import com.siberika.idea.pascal.lang.psi.PasNamedIdentDecl;
import com.siberika.idea.pascal.lang.psi.PasVisitor;
import com.siberika.idea.pascal.lang.stub.PasIdentStub;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class PasNamedIdentDeclImpl extends PascalIdentDeclImpl implements PasNamedIdentDecl {

  public PasNamedIdentDeclImpl(ASTNode node) {
    super(node);
  }

  public PasNamedIdentDeclImpl(PasIdentStub stub, IStubElementType type) {
    super(stub, type);
  }

  public void accept(@NotNull PasVisitor visitor) {
    visitor.visitNamedIdentDecl(this);
  }

  @Override
  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof PasVisitor) accept((PasVisitor)visitor);
    else super.accept(visitor);
  }

  @Override
  @Nullable
  public PasEscapedIdent getEscapedIdent() {
    return PsiTreeUtil.getChildOfType(this, PasEscapedIdent.class);
  }

  @Override
  @Nullable
  public PasKeywordIdent getKeywordIdent() {
    return PsiTreeUtil.getChildOfType(this, PasKeywordIdent.class);
  }

  @Override
  @Nullable
  public PsiElement getPasName() {
    return getNameIdentifier();
  }

}
