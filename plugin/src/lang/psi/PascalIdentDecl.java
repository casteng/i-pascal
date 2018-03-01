package com.siberika.idea.pascal.lang.psi;

import com.intellij.psi.StubBasedPsiElement;
import com.siberika.idea.pascal.lang.psi.impl.PasField;
import com.siberika.idea.pascal.lang.stub.PasIdentStub;
import org.jetbrains.annotations.Nullable;

public interface PascalIdentDecl extends StubBasedPsiElement<PasIdentStub>, PasNamedIdent {

    @Nullable
    String getTypeString();

    @Nullable
    PasField.Kind getTypeKind();

}
