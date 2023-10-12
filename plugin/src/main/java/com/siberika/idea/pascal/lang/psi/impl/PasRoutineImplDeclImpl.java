// This is a generated file. Not intended for manual editing.
package com.siberika.idea.pascal.lang.psi.impl;

import static com.siberika.idea.pascal.lang.psi.PasTypes.CLASS;
import static com.siberika.idea.pascal.lang.psi.PasTypes.CONSTRUCTOR;
import static com.siberika.idea.pascal.lang.psi.PasTypes.DESTRUCTOR;
import static com.siberika.idea.pascal.lang.psi.PasTypes.FUNCTION;
import static com.siberika.idea.pascal.lang.psi.PasTypes.GENERIC;
import static com.siberika.idea.pascal.lang.psi.PasTypes.IN;
import static com.siberika.idea.pascal.lang.psi.PasTypes.NAMED_IDENT;
import static com.siberika.idea.pascal.lang.psi.PasTypes.OPERATOR;
import static com.siberika.idea.pascal.lang.psi.PasTypes.PROCEDURE;

import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.util.PsiTreeUtil;
import com.siberika.idea.pascal.lang.psi.PasClassQualifiedIdent;
import com.siberika.idea.pascal.lang.psi.PasConstrainedTypeParam;
import com.siberika.idea.pascal.lang.psi.PasCustomAttributeDecl;
import com.siberika.idea.pascal.lang.psi.PasFormalParameterSection;
import com.siberika.idea.pascal.lang.psi.PasFunctionDirective;
import com.siberika.idea.pascal.lang.psi.PasNamedIdent;
import com.siberika.idea.pascal.lang.psi.PasProcBodyBlock;
import com.siberika.idea.pascal.lang.psi.PasRoutineImplDecl;
import com.siberika.idea.pascal.lang.psi.PasTypeDecl;
import com.siberika.idea.pascal.lang.psi.PasVisitor;
import java.util.List;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class PasRoutineImplDeclImpl extends PascalRoutineImpl implements PasRoutineImplDecl {

  public PasRoutineImplDeclImpl(ASTNode node) {
    super(node);
  }

  public void accept(@NotNull PasVisitor visitor) {
    visitor.visitRoutineImplDecl(this);
  }

  @Override
  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof PasVisitor) accept((PasVisitor)visitor);
    else super.accept(visitor);
  }

  @Override
  @Nullable
  public PasClassQualifiedIdent getClassQualifiedIdent() {
    return PsiTreeUtil.getChildOfType(this, PasClassQualifiedIdent.class);
  }

  @Override
  @NotNull
  public List<PasConstrainedTypeParam> getConstrainedTypeParamList() {
    return PsiTreeUtil.getChildrenOfTypeAsList(this, PasConstrainedTypeParam.class);
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
  @NotNull
  public List<PasFunctionDirective> getFunctionDirectiveList() {
    return PsiTreeUtil.getChildrenOfTypeAsList(this, PasFunctionDirective.class);
  }

  @Override
  @Nullable
  public PasNamedIdent getNamedIdent() {
    return PsiTreeUtil.getChildOfType(this, PasNamedIdent.class);
  }

  @Override
  @Nullable
  public PasProcBodyBlock getProcBodyBlock() {
    return PsiTreeUtil.getChildOfType(this, PasProcBodyBlock.class);
  }

  @Override
  @Nullable
  public PasTypeDecl getTypeDecl() {
    return PsiTreeUtil.getChildOfType(this, PasTypeDecl.class);
  }

  @Override
  @Nullable
  public PsiElement getPasClass() {
    return findChildByType(CLASS);
  }

  @Override
  @Nullable
  public PsiElement getConstructor() {
    return findChildByType(CONSTRUCTOR);
  }

  @Override
  @Nullable
  public PsiElement getDestructor() {
    return findChildByType(DESTRUCTOR);
  }

  @Override
  @Nullable
  public PsiElement getFunction() {
    return findChildByType(FUNCTION);
  }

  @Override
  @Nullable
  public PsiElement getGeneric() {
    return findChildByType(GENERIC);
  }

  @Override
  @Nullable
  public PsiElement getIn() {
    return findChildByType(IN);
  }

  @Override
  @Nullable
  public PsiElement getOperator() {
    return findChildByType(OPERATOR);
  }

  @Override
  @Nullable
  public PsiElement getProcedure() {
    return findChildByType(PROCEDURE);
  }

  @Override
  public @Nullable PsiElement getPasName() {
    return findChildByType(NAMED_IDENT);
  }
}
