package com.siberika.idea.pascal.lang.stub;

import com.siberika.idea.pascal.lang.psi.PascalIdentDecl;
import com.siberika.idea.pascal.lang.psi.impl.PasField;
import org.jetbrains.annotations.Nullable;

public interface PasIdentStub extends PasNamedStub<PascalIdentDecl> {

    @Nullable
    String getTypeString();

    @Nullable
    PasField.Kind getTypeKind();

}
