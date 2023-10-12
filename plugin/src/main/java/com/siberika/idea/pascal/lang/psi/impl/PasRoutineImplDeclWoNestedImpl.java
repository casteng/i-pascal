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

public class PasRoutineImplDeclWoNestedImpl extends PasRoutineImplDeclImpl implements PasRoutineImplDeclWoNested {

  public PasRoutineImplDeclWoNestedImpl(ASTNode node) {
    super(node);
  }

  @Override
  public void accept(@NotNull PasVisitor visitor) {
    visitor.visitRoutineImplDeclWoNested(this);
  }

  @Override
  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof PasVisitor) accept((PasVisitor)visitor);
    else super.accept(visitor);
  }

  @Override
  @Nullable
  public PasBlockLocalWONested getBlockLocalWONested() {
    return PsiTreeUtil.getChildOfType(this, PasBlockLocalWONested.class);
  }

  @Override
  @Nullable
  public PasProcForwardDecl getProcForwardDecl() {
    return PsiTreeUtil.getChildOfType(this, PasProcForwardDecl.class);
  }

}
