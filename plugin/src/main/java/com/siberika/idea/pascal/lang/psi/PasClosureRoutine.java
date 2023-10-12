// This is a generated file. Not intended for manual editing.
package com.siberika.idea.pascal.lang.psi;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElement;

public interface PasClosureRoutine extends PasDeclSection, PascalRoutine {

  @NotNull
  PasBlockLocal getBlockLocal();

  @NotNull
  List<PasCustomAttributeDecl> getCustomAttributeDeclList();

  @Nullable
  PasFormalParameterSection getFormalParameterSection();

  @Nullable
  PasTypeDecl getTypeDecl();

  @Nullable
  PsiElement getFunction();

  @Nullable
  PsiElement getProcedure();

}
