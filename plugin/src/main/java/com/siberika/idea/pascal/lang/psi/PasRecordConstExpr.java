// This is a generated file. Not intended for manual editing.
package com.siberika.idea.pascal.lang.psi;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElement;

public interface PasRecordConstExpr extends PasExpr {

  @NotNull
  List<PasEscapedIdent> getEscapedIdentList();

  @NotNull
  List<PasExpr> getExprList();

  @NotNull
  List<PasExpression> getExpressionList();

  @NotNull
  List<PasKeywordIdent> getKeywordIdentList();

}
