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

public class PasClassTypeTypeDeclImpl extends PascalPsiElementImpl implements PasClassTypeTypeDecl {

  public PasClassTypeTypeDeclImpl(ASTNode node) {
    super(node);
  }

  public void accept(@NotNull PasVisitor visitor) {
    visitor.visitClassTypeTypeDecl(this);
  }

  @Override
  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof PasVisitor) accept((PasVisitor)visitor);
    else super.accept(visitor);
  }

  @Override
  @NotNull
  public PasTypeID getTypeID() {
    return notNullChild(PsiTreeUtil.getChildOfType(this, PasTypeID.class));
  }

  @Override
  @NotNull
  public PsiElement getPasClass() {
    return notNullChild(findChildByType(CLASS));
  }

  @Override
  @NotNull
  public PsiElement getOf() {
    return notNullChild(findChildByType(OF));
  }

}
