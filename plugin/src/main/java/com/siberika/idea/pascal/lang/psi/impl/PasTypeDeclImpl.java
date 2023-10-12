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

public class PasTypeDeclImpl extends PascalPsiElementImpl implements PasTypeDecl {

  public PasTypeDeclImpl(ASTNode node) {
    super(node);
  }

  public void accept(@NotNull PasVisitor visitor) {
    visitor.visitTypeDecl(this);
  }

  @Override
  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof PasVisitor) accept((PasVisitor)visitor);
    else super.accept(visitor);
  }

  @Override
  @Nullable
  public PasArrayType getArrayType() {
    return PsiTreeUtil.getChildOfType(this, PasArrayType.class);
  }

  @Override
  @Nullable
  public PasClassHelperDecl getClassHelperDecl() {
    return PsiTreeUtil.getChildOfType(this, PasClassHelperDecl.class);
  }

  @Override
  @Nullable
  public PasClassTypeDecl getClassTypeDecl() {
    return PsiTreeUtil.getChildOfType(this, PasClassTypeDecl.class);
  }

  @Override
  @Nullable
  public PasClassTypeTypeDecl getClassTypeTypeDecl() {
    return PsiTreeUtil.getChildOfType(this, PasClassTypeTypeDecl.class);
  }

  @Override
  @Nullable
  public PasEnumType getEnumType() {
    return PsiTreeUtil.getChildOfType(this, PasEnumType.class);
  }

  @Override
  @Nullable
  public PasFileType getFileType() {
    return PsiTreeUtil.getChildOfType(this, PasFileType.class);
  }

  @Override
  @Nullable
  public PasInterfaceTypeDecl getInterfaceTypeDecl() {
    return PsiTreeUtil.getChildOfType(this, PasInterfaceTypeDecl.class);
  }

  @Override
  @Nullable
  public PasObjectDecl getObjectDecl() {
    return PsiTreeUtil.getChildOfType(this, PasObjectDecl.class);
  }

  @Override
  @Nullable
  public PasPointerType getPointerType() {
    return PsiTreeUtil.getChildOfType(this, PasPointerType.class);
  }

  @Override
  @Nullable
  public PasProcedureType getProcedureType() {
    return PsiTreeUtil.getChildOfType(this, PasProcedureType.class);
  }

  @Override
  @Nullable
  public PasRecordDecl getRecordDecl() {
    return PsiTreeUtil.getChildOfType(this, PasRecordDecl.class);
  }

  @Override
  @Nullable
  public PasRecordHelperDecl getRecordHelperDecl() {
    return PsiTreeUtil.getChildOfType(this, PasRecordHelperDecl.class);
  }

  @Override
  @Nullable
  public PasSetType getSetType() {
    return PsiTreeUtil.getChildOfType(this, PasSetType.class);
  }

  @Override
  @Nullable
  public PasStringType getStringType() {
    return PsiTreeUtil.getChildOfType(this, PasStringType.class);
  }

  @Override
  @Nullable
  public PasSubRangeType getSubRangeType() {
    return PsiTreeUtil.getChildOfType(this, PasSubRangeType.class);
  }

  @Override
  @Nullable
  public PasTypeID getTypeID() {
    return PsiTreeUtil.getChildOfType(this, PasTypeID.class);
  }

  @Override
  @Nullable
  public PsiElement getType() {
    return findChildByType(TYPE);
  }

}
