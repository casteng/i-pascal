package com.siberika.idea.pascal.lang.stub;

import com.intellij.psi.stubs.StubBase;
import com.intellij.psi.stubs.StubElement;
import com.siberika.idea.pascal.lang.psi.PascalIdentDecl;
import com.siberika.idea.pascal.lang.psi.impl.PasField;
import org.jetbrains.annotations.Nullable;

public class PasIdentStubImpl extends StubBase<PascalIdentDecl> implements PasIdentStub {

    private String name;
    private PasField.FieldType kind;
    private String typeString;
    private PasField.Kind typeKind;

    public PasIdentStubImpl(StubElement parent, String name, PasField.FieldType kind, String typeString, PasField.Kind typeKind) {
        super(parent, PasIdentStubElementType.INSTANCE);
        this.name = name;
        this.kind = kind;
        this.typeString = typeString;
        this.typeKind = typeKind;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public PasField.FieldType getType() {
        return kind;
    }

    @Nullable
    @Override
    public String getTypeString() {
        return typeString;
    }

    @Nullable
    @Override
    public PasField.Kind getTypeKind() {
        return typeKind;
    }
}
