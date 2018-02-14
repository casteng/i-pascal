package com.siberika.idea.pascal.lang.stub;

import com.siberika.idea.pascal.lang.psi.PascalIdentDecl;
import org.jetbrains.annotations.Nullable;

public interface PasIdentStub extends PasNamedStub<PascalIdentDecl> {

    @Nullable
    String getTypeString();

}
