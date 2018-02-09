package com.siberika.idea.pascal.lang.psi;

import com.intellij.psi.StubBasedPsiElement;
import com.siberika.idea.pascal.lang.stub.PasIdentStub;
import org.jetbrains.annotations.NotNull;

public interface PascalIdentDecl extends StubBasedPsiElement<PasIdentStub>, PasNamedIdent {

    @NotNull
    String getName();

}
