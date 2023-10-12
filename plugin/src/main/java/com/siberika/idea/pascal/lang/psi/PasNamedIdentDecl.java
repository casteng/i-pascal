// This is a generated file. Not intended for manual editing.
package com.siberika.idea.pascal.lang.psi;

import com.intellij.psi.PsiElement;
import com.intellij.psi.StubBasedPsiElement;
import com.siberika.idea.pascal.lang.stub.PasIdentStub;
import org.jetbrains.annotations.Nullable;

public interface PasNamedIdentDecl extends PascalIdentDecl, StubBasedPsiElement<PasIdentStub> {

  @Nullable
  PasEscapedIdent getEscapedIdent();

  @Nullable
  PasKeywordIdent getKeywordIdent();

  @Nullable
  PsiElement getPasName();

}
