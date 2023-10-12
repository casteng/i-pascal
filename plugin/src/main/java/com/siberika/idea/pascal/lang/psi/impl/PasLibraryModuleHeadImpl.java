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

public class PasLibraryModuleHeadImpl extends PascalNamedElementImpl implements PasLibraryModuleHead {

  public PasLibraryModuleHeadImpl(ASTNode node) {
    super(node);
  }

  public void accept(@NotNull PasVisitor visitor) {
    visitor.visitLibraryModuleHead(this);
  }

  @Override
  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof PasVisitor) accept((PasVisitor)visitor);
    else super.accept(visitor);
  }

  @Override
  @Nullable
  public PasNamespaceIdent getNamespaceIdent() {
    return PsiTreeUtil.getChildOfType(this, PasNamespaceIdent.class);
  }

  @Override
  @NotNull
  public List<PasStringFactor> getStringFactorList() {
    return PsiTreeUtil.getChildrenOfTypeAsList(this, PasStringFactor.class);
  }

}
