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

public class PasFunctionDirectiveImpl extends PascalPsiElementImpl implements PasFunctionDirective {

  public PasFunctionDirectiveImpl(ASTNode node) {
    super(node);
  }

  public void accept(@NotNull PasVisitor visitor) {
    visitor.visitFunctionDirective(this);
  }

  @Override
  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof PasVisitor) accept((PasVisitor)visitor);
    else super.accept(visitor);
  }

  @Override
  @Nullable
  public PasExpression getExpression() {
    return PsiTreeUtil.getChildOfType(this, PasExpression.class);
  }

  @Override
  @Nullable
  public PasStringFactor getStringFactor() {
    return PsiTreeUtil.getChildOfType(this, PasStringFactor.class);
  }

  @Override
  @Nullable
  public PsiElement getAbstract() {
    return findChildByType(ABSTRACT);
  }

  @Override
  @Nullable
  public PsiElement getAssembler() {
    return findChildByType(ASSEMBLER);
  }

  @Override
  @Nullable
  public PsiElement getCdecl() {
    return findChildByType(CDECL);
  }

  @Override
  @Nullable
  public PsiElement getDeprecated() {
    return findChildByType(DEPRECATED);
  }

  @Override
  @Nullable
  public PsiElement getDispid() {
    return findChildByType(DISPID);
  }

  @Override
  @Nullable
  public PsiElement getDynamic() {
    return findChildByType(DYNAMIC);
  }

  @Override
  @Nullable
  public PsiElement getExperimental() {
    return findChildByType(EXPERIMENTAL);
  }

  @Override
  @Nullable
  public PsiElement getExport() {
    return findChildByType(EXPORT);
  }

  @Override
  @Nullable
  public PsiElement getFinal() {
    return findChildByType(FINAL);
  }

  @Override
  @Nullable
  public PsiElement getInline() {
    return findChildByType(INLINE);
  }

  @Override
  @Nullable
  public PsiElement getLibrary() {
    return findChildByType(LIBRARY);
  }

  @Override
  @Nullable
  public PsiElement getMessage() {
    return findChildByType(MESSAGE);
  }

  @Override
  @Nullable
  public PsiElement getOverload() {
    return findChildByType(OVERLOAD);
  }

  @Override
  @Nullable
  public PsiElement getOverride() {
    return findChildByType(OVERRIDE);
  }

  @Override
  @Nullable
  public PsiElement getPascal() {
    return findChildByType(PASCAL);
  }

  @Override
  @Nullable
  public PsiElement getPlatform() {
    return findChildByType(PLATFORM);
  }

  @Override
  @Nullable
  public PsiElement getRegister() {
    return findChildByType(REGISTER);
  }

  @Override
  @Nullable
  public PsiElement getReintroduce() {
    return findChildByType(REINTRODUCE);
  }

  @Override
  @Nullable
  public PsiElement getSafecall() {
    return findChildByType(SAFECALL);
  }

  @Override
  @Nullable
  public PsiElement getStatic() {
    return findChildByType(STATIC);
  }

  @Override
  @Nullable
  public PsiElement getStdcall() {
    return findChildByType(STDCALL);
  }

  @Override
  @Nullable
  public PsiElement getVirtual() {
    return findChildByType(VIRTUAL);
  }

}
