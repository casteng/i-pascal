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

public class PasClassMethodResolutionImpl extends PascalPsiElementImpl implements PasClassMethodResolution {

  public PasClassMethodResolutionImpl(ASTNode node) {
    super(node);
  }

  public void accept(@NotNull PasVisitor visitor) {
    visitor.visitClassMethodResolution(this);
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
  @NotNull
  public PasGenericTypeIdent getGenericTypeIdent() {
    return notNullChild(PsiTreeUtil.getChildOfType(this, PasGenericTypeIdent.class));
  }

  @Override
  @Nullable
  public PasKeywordIdent getKeywordIdent() {
    return PsiTreeUtil.getChildOfType(this, PasKeywordIdent.class);
  }

  @Override
  @NotNull
  public PasRefNamedIdent getRefNamedIdent() {
    return notNullChild(PsiTreeUtil.getChildOfType(this, PasRefNamedIdent.class));
  }

  @Override
  @Nullable
  public PsiElement getPasClass() {
    return findChildByType(CLASS);
  }

  @Override
  @Nullable
  public PsiElement getFunction() {
    return findChildByType(FUNCTION);
  }

  @Override
  @Nullable
  public String getName() {
    return findChildByType(NAME).getText();
  }
  @Override
  @Nullable
  public PsiElement getPasName() {
    return findChildByType(NAME);
  }


  @Override
  @Nullable
  public PsiElement getProcedure() {
    return findChildByType(PROCEDURE);
  }

}
