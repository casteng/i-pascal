package com.siberika.idea.pascal.lang.psi;

import com.siberika.idea.pascal.lang.psi.impl.PasField;
import com.siberika.idea.pascal.lang.stub.PasIdentStub;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public interface PascalIdentDecl extends PascalStubElement<PasIdentStub>, PasNamedIdent {

    @Nullable
    String getTypeString();

    @Nullable
    PasField.Kind getTypeKind();

    @NotNull
    PasField.Access getAccess();

    @Nullable
    String getValue();

    @NotNull
    List<String> getSubMembers();
}
