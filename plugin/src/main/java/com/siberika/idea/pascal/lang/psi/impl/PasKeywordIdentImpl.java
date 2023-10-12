// This is a generated file. Not intended for manual editing.
package com.siberika.idea.pascal.lang.psi.impl;

import static com.siberika.idea.pascal.lang.psi.PasTypes.ABSTRACT;
import static com.siberika.idea.pascal.lang.psi.PasTypes.ASSEMBLER;
import static com.siberika.idea.pascal.lang.psi.PasTypes.AUTOMATED;
import static com.siberika.idea.pascal.lang.psi.PasTypes.BREAK;
import static com.siberika.idea.pascal.lang.psi.PasTypes.CDECL;
import static com.siberika.idea.pascal.lang.psi.PasTypes.CONTAINS;
import static com.siberika.idea.pascal.lang.psi.PasTypes.CONTINUE;
import static com.siberika.idea.pascal.lang.psi.PasTypes.DEFAULT;
import static com.siberika.idea.pascal.lang.psi.PasTypes.DEPRECATED;
import static com.siberika.idea.pascal.lang.psi.PasTypes.DISPID;
import static com.siberika.idea.pascal.lang.psi.PasTypes.DYNAMIC;
import static com.siberika.idea.pascal.lang.psi.PasTypes.EXIT;
import static com.siberika.idea.pascal.lang.psi.PasTypes.EXPERIMENTAL;
import static com.siberika.idea.pascal.lang.psi.PasTypes.EXPORT;
import static com.siberika.idea.pascal.lang.psi.PasTypes.EXTERNAL;
import static com.siberika.idea.pascal.lang.psi.PasTypes.FINAL;
import static com.siberika.idea.pascal.lang.psi.PasTypes.FORWARD;
import static com.siberika.idea.pascal.lang.psi.PasTypes.HELPER;
import static com.siberika.idea.pascal.lang.psi.PasTypes.IMPLEMENTS;
import static com.siberika.idea.pascal.lang.psi.PasTypes.INDEX;
import static com.siberika.idea.pascal.lang.psi.PasTypes.MESSAGE;
import static com.siberika.idea.pascal.lang.psi.PasTypes.NAME_;
import static com.siberika.idea.pascal.lang.psi.PasTypes.OPERATOR;
import static com.siberika.idea.pascal.lang.psi.PasTypes.OUT;
import static com.siberika.idea.pascal.lang.psi.PasTypes.OVERLOAD;
import static com.siberika.idea.pascal.lang.psi.PasTypes.OVERRIDE;
import static com.siberika.idea.pascal.lang.psi.PasTypes.PACKAGE;
import static com.siberika.idea.pascal.lang.psi.PasTypes.PASCAL;
import static com.siberika.idea.pascal.lang.psi.PasTypes.PLATFORM;
import static com.siberika.idea.pascal.lang.psi.PasTypes.READ;
import static com.siberika.idea.pascal.lang.psi.PasTypes.REFERENCE;
import static com.siberika.idea.pascal.lang.psi.PasTypes.REGISTER;
import static com.siberika.idea.pascal.lang.psi.PasTypes.REINTRODUCE;
import static com.siberika.idea.pascal.lang.psi.PasTypes.REQUIRES;
import static com.siberika.idea.pascal.lang.psi.PasTypes.SAFECALL;
import static com.siberika.idea.pascal.lang.psi.PasTypes.SEALED;
import static com.siberika.idea.pascal.lang.psi.PasTypes.SELF;
import static com.siberika.idea.pascal.lang.psi.PasTypes.STATIC;
import static com.siberika.idea.pascal.lang.psi.PasTypes.STDCALL;
import static com.siberika.idea.pascal.lang.psi.PasTypes.VIRTUAL;
import static com.siberika.idea.pascal.lang.psi.PasTypes.WRITE;

import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.PsiReference;
import com.siberika.idea.pascal.lang.psi.PasKeywordIdent;
import com.siberika.idea.pascal.lang.psi.PasVisitor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class PasKeywordIdentImpl extends PascalNamedElementImpl implements PasKeywordIdent {

  public PasKeywordIdentImpl(ASTNode node) {
    super(node);
  }

  public void accept(@NotNull PasVisitor visitor) {
    visitor.visitKeywordIdent(this);
  }

  @Override
  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof PasVisitor) accept((PasVisitor)visitor);
    else super.accept(visitor);
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
  public PsiElement getAutomated() {
    return findChildByType(AUTOMATED);
  }

  @Override
  @Nullable
  public PsiElement getBreak() {
    return findChildByType(BREAK);
  }

  @Override
  @Nullable
  public PsiElement getCdecl() {
    return findChildByType(CDECL);
  }

  @Override
  @Nullable
  public PsiElement getContains() {
    return findChildByType(CONTAINS);
  }

  @Override
  @Nullable
  public PsiElement getContinue() {
    return findChildByType(CONTINUE);
  }

  @Override
  @Nullable
  public PsiElement getDefault() {
    return findChildByType(DEFAULT);
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
  public PsiElement getExit() {
    return findChildByType(EXIT);
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
  public PsiElement getExternal() {
    return findChildByType(EXTERNAL);
  }

  @Override
  @Nullable
  public PsiElement getFinal() {
    return findChildByType(FINAL);
  }

  @Override
  @Nullable
  public PsiElement getForward() {
    return findChildByType(FORWARD);
  }

  @Override
  @Nullable
  public PsiElement getHelper() {
    return findChildByType(HELPER);
  }

  @Override
  @Nullable
  public PsiElement getImplements() {
    return findChildByType(IMPLEMENTS);
  }

  @Override
  @Nullable
  public PsiElement getIndex() {
    return findChildByType(INDEX);
  }

  @Override
  @Nullable
  public PsiElement getMessage() {
    return findChildByType(MESSAGE);
  }

  @Override
  @Nullable
  public PsiElement getOperator() {
    return findChildByType(OPERATOR);
  }

  @Override
  @Nullable
  public PsiElement getOut() {
    return findChildByType(OUT);
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
  public PsiElement getPackage() {
    return findChildByType(PACKAGE);
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
  public PsiElement getRead() {
    return findChildByType(READ);
  }

  @Override
  @Nullable
  public PsiReference getReference() {
    return findChildByType(REFERENCE);
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
  public PsiElement getRequires() {
    return findChildByType(REQUIRES);
  }

  @Override
  @Nullable
  public PsiElement getSafecall() {
    return findChildByType(SAFECALL);
  }

  @Override
  @Nullable
  public PsiElement getSealed() {
    return findChildByType(SEALED);
  }

  @Override
  @Nullable
  public PsiElement getSelf() {
    return findChildByType(SELF);
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

  @Override
  @Nullable
  public PsiElement getWrite() {
    return findChildByType(WRITE);
  }

  @Override
  public @Nullable PsiElement getPasName() {
    return findChildByType(NAME_);
  }

}
