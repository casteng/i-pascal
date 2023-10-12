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

public class PasRoutineImplDeclNested1Impl extends PasRoutineImplDeclImpl implements PasRoutineImplDeclNested1 {

  public PasRoutineImplDeclNested1Impl(ASTNode node) {
    super(node);
  }

  @Override
  public void accept(@NotNull PasVisitor visitor) {
    visitor.visitRoutineImplDeclNested1(this);
  }

  @Override
  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof PasVisitor) accept((PasVisitor)visitor);
    else super.accept(visitor);
  }

  @Override
  @Nullable
  public PasBlockLocalNested1 getBlockLocalNested1() {
    return PsiTreeUtil.getChildOfType(this, PasBlockLocalNested1.class);
  }

  @Override
  @Nullable
  public PasProcForwardDecl getProcForwardDecl() {
    return PsiTreeUtil.getChildOfType(this, PasProcForwardDecl.class);
  }

}
