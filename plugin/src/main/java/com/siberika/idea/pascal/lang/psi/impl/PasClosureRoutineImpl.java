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

public class PasClosureRoutineImpl extends PascalRoutineImpl implements PasClosureRoutine {

  public PasClosureRoutineImpl(ASTNode node) {
    super(node);
  }

  public void accept(@NotNull PasVisitor visitor) {
    visitor.visitClosureRoutine(this);
  }

  @Override
  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof PasVisitor) accept((PasVisitor)visitor);
    else super.accept(visitor);
  }

  @Override
  @NotNull
  public PasBlockLocal getBlockLocal() {
    return notNullChild(PsiTreeUtil.getChildOfType(this, PasBlockLocal.class));
  }

  @Override
  @NotNull
  public List<PasCustomAttributeDecl> getCustomAttributeDeclList() {
    return PsiTreeUtil.getChildrenOfTypeAsList(this, PasCustomAttributeDecl.class);
  }

  @Override
  @Nullable
  public PasFormalParameterSection getFormalParameterSection() {
    return PsiTreeUtil.getChildOfType(this, PasFormalParameterSection.class);
  }

  @Override
  @Nullable
  public PasTypeDecl getTypeDecl() {
    return PsiTreeUtil.getChildOfType(this, PasTypeDecl.class);
  }

  @Override
  @Nullable
  public PsiElement getFunction() {
    return findChildByType(FUNCTION);
  }

  @Override
  @Nullable
  public PsiElement getProcedure() {
    return findChildByType(PROCEDURE);
  }

}