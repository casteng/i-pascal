package com.siberika.idea.pascal.lang.psi;

import com.siberika.idea.pascal.lang.psi.impl.PasField;
import com.siberika.idea.pascal.lang.stub.PasIdentStub;
import org.jetbrains.annotations.Nullable;

public interface PascalIdentDecl extends PascalStubElement<PasIdentStub>, PasNamedIdent {

    @Nullable
    String getTypeString();

    @Nullable
    PasField.Kind getTypeKind();

}
