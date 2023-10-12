// This is a generated file. Not intended for manual editing.
package com.siberika.idea.pascal.lang.psi.impl;

import static com.siberika.idea.pascal.lang.psi.PasTypes.NAME;

import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.util.PsiTreeUtil;
import com.siberika.idea.pascal.lang.psi.PasEscapedIdent;
import com.siberika.idea.pascal.lang.psi.PasKeywordIdent;
import com.siberika.idea.pascal.lang.psi.PasNamedIdent;
import com.siberika.idea.pascal.lang.psi.PasVisitor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class PasNamedIdentImpl extends PascalNamedElementImpl implements PasNamedIdent {

  public PasNamedIdentImpl(ASTNode node) {
    super(node);
  }

  public void accept(@NotNull PasVisitor visitor) {
    visitor.visitNamedIdent(this);
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
  public String getName() {
    return findChildByType(NAME).getText();
  }

}
