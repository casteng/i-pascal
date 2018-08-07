package com.siberika.idea.pascal.lang.stub;

import com.intellij.psi.stubs.StubElement;
import com.siberika.idea.pascal.lang.psi.PascalIdentDecl;
import com.siberika.idea.pascal.lang.psi.impl.PasField;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Set;

public class PasIdentStubImpl extends PasNamedStubBase<PascalIdentDecl> implements PasIdentStub {

    private PasField.FieldType kind;
    private String typeString;
    private PasField.Access access;
    private String value;
    private PasField.Kind typeKind;
    private List<String> subMembers;                            // members which can be qualified by this ident as well as accessed directly (enums)
    private Set<String> typeParameters;

    public PasIdentStubImpl(StubElement parent, String name, String containingUnitName, PasField.FieldType kind, String typeString, PasField.Kind typeKind,
                            PasField.Access access, String value, List<String> subMembers, Set<String> typeParameters) {
        super(parent, PasIdentStubElementType.INSTANCE, name, containingUnitName);
        this.kind = kind;
        this.typeString = typeString;
        this.typeKind = typeKind;
        this.subMembers = subMembers;
        this.access = access;
        this.value = value;
        this.typeParameters = typeParameters;
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

    @NotNull
    @Override
    public List<String> getSubMembers() {
        return subMembers;
    }

    @NotNull
    @Override
    public PasField.Access getAccess() {
        return access;
    }

    @Nullable
    @Override
    public String getValue() {
        return value;
    }

    @NotNull
    @Override
    public Set<String> getTypeParameters() {
        return typeParameters;
    }
}
