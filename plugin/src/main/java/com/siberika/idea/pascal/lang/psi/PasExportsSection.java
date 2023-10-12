// This is a generated file. Not intended for manual editing.
package com.siberika.idea.pascal.lang.psi;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElement;

public interface PasExportsSection extends PasDeclSection {

  @NotNull
  List<PasExpression> getExpressionList();

  @NotNull
  List<PasFormalParameterSection> getFormalParameterSectionList();

  @NotNull
  List<PasRefNamedIdent> getRefNamedIdentList();

  @NotNull
  PsiElement getExports();

}
