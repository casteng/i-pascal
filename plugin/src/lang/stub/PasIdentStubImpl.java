package com.siberika.idea.pascal.lang.stub;

import com.intellij.psi.stubs.StubBase;
import com.intellij.psi.stubs.StubElement;
import com.siberika.idea.pascal.lang.psi.PascalIdentDecl;
import com.siberika.idea.pascal.lang.psi.impl.PasField;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class PasIdentStubImpl extends StubBase<PascalIdentDecl> implements PasIdentStub {

    private String name;
    private String uniqueName;
    private PasField.FieldType kind;
    private String typeString;
    private PasField.Kind typeKind;
    private List<String> subMembers;                            // members which can be qualified by this ident as well as accessed directly (enums)

    public PasIdentStubImpl(StubElement parent, String name, PasField.FieldType kind, String typeString, PasField.Kind typeKind, List<String> subMembers) {
        super(parent, PasIdentStubElementType.INSTANCE);
        this.name = name;
        this.uniqueName = (parent instanceof PasNamedStub ? ((PasNamedStub) parent).getUniqueName() + "." : "") + name;
        this.kind = kind;
        this.typeString = typeString;
        this.typeKind = typeKind;
        this.subMembers = subMembers;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public PasField.FieldType getType() {
        return kind;
    }

    @Override
    public String getUniqueName() {
        return uniqueName;
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

    @Override
    public List<String> getSubMembers() {
        return subMembers;
    }
}
