// This is a generated file. Not intended for manual editing.
package com.siberika.idea.pascal.lang.psi;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElement;

public interface PasSumExpr extends PasExpr {

  @NotNull
  List<PasExpr> getExprList();

  @NotNull
  PasAddOp getAddOp();

}
