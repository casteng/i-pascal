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

public class PasProcBodyBlockImpl extends PascalPsiElementImpl implements PasProcBodyBlock {

  public PasProcBodyBlockImpl(ASTNode node) {
    super(node);
  }

  public void accept(@NotNull PasVisitor visitor) {
    visitor.visitProcBodyBlock(this);
  }

  @Override
  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof PasVisitor) accept((PasVisitor)visitor);
    else super.accept(visitor);
  }

  @Override
  @Nullable
  public PasBlockLocal getBlockLocal() {
    return PsiTreeUtil.getChildOfType(this, PasBlockLocal.class);
  }

  @Override
  @Nullable
  public PasExternalDirective getExternalDirective() {
    return PsiTreeUtil.getChildOfType(this, PasExternalDirective.class);
  }

  @Override
  @Nullable
  public PasProcForwardDecl getProcForwardDecl() {
    return PsiTreeUtil.getChildOfType(this, PasProcForwardDecl.class);
  }

}
