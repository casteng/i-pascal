package com.siberika.idea.pascal.lang.stub;

import com.siberika.idea.pascal.lang.psi.PascalIdentDecl;
import com.siberika.idea.pascal.lang.psi.impl.PasField;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public interface PasIdentStub extends PasNamedStub<PascalIdentDecl> {

    @Nullable
    String getTypeString();

    @Nullable
    PasField.Kind getTypeKind();

    @NotNull
    List<String> getSubMembers();

    @NotNull
    PasField.Access getAccess();

    @Nullable
    String getValue();

}
