// This is a generated file. Not intended for manual editing.
package com.siberika.idea.pascal.lang.psi;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElement;

public interface PasStatement extends PascalPsiElement {

  @Nullable
  PasAssemblerStatement getAssemblerStatement();

  @Nullable
  PasAssignPart getAssignPart();

  @Nullable
  PasBreakStatement getBreakStatement();

  @Nullable
  PasCaseStatement getCaseStatement();

  @Nullable
  PasCompoundStatement getCompoundStatement();

  @Nullable
  PasContinueStatement getContinueStatement();

  @Nullable
  PasExitStatement getExitStatement();

  @Nullable
  PasExpression getExpression();

  @Nullable
  PasForStatement getForStatement();

  @Nullable
  PasGotoStatement getGotoStatement();

  @Nullable
  PasIfStatement getIfStatement();

  @Nullable
  PasInlineConstDeclaration getInlineConstDeclaration();

  @Nullable
  PasInlineVarDeclaration getInlineVarDeclaration();

  @Nullable
  PasLabelId getLabelId();

  @Nullable
  PasRaiseStatement getRaiseStatement();

  @Nullable
  PasRepeatStatement getRepeatStatement();

  @Nullable
  PasTryStatement getTryStatement();

  @Nullable
  PasWhileStatement getWhileStatement();

  @Nullable
  PasWithStatement getWithStatement();

  @Nullable
  PasStmtEmpty getStmtEmpty();

}
