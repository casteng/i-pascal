// This is a generated file. Not intended for manual editing.
package com.siberika.idea.pascal.lang.psi;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElement;

public interface PasRoutineImplDecl extends PascalRoutine {

  @Nullable
  PasClassQualifiedIdent getClassQualifiedIdent();

  @NotNull
  List<PasConstrainedTypeParam> getConstrainedTypeParamList();

  @NotNull
  List<PasCustomAttributeDecl> getCustomAttributeDeclList();

  @Nullable
  PasFormalParameterSection getFormalParameterSection();

  @NotNull
  List<PasFunctionDirective> getFunctionDirectiveList();

  @Nullable
  PasNamedIdent getNamedIdent();

  @Nullable
  PasProcBodyBlock getProcBodyBlock();

  @Nullable
  PasTypeDecl getTypeDecl();

  @Nullable
  PsiElement getPasClass();

  @Nullable
  PsiElement getConstructor();

  @Nullable
  PsiElement getDestructor();

  @Nullable
  PsiElement getFunction();

  @Nullable
  PsiElement getGeneric();

  @Nullable
  PsiElement getIn();

  @Nullable
  PsiElement getOperator();

  @Nullable
  PsiElement getProcedure();

}
