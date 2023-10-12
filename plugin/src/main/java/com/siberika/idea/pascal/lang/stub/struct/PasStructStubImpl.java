package com.siberika.idea.pascal.lang.stub.struct;

import com.intellij.psi.stubs.StubElement;
import com.siberika.idea.pascal.lang.psi.PascalStructType;
import com.siberika.idea.pascal.lang.psi.impl.PasField;
import com.siberika.idea.pascal.lang.stub.PasNamedStubBase;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class PasStructStubImpl<T extends PascalStructType> extends PasNamedStubBase<T> implements PasStructStub<T> {

    private List<String> parentNames;
    private List<String> aliases;
    private List<String> typeParameters;

    public PasStructStubImpl(StubElement parent, String name, String containingUnitName, List<String> parentNames,
                             List<String> aliases, PasStructDeclStubElementType elementType, List<String> typeParameters) {
        super(parent, elementType, name, containingUnitName);
        this.parentNames = parentNames;
        this.aliases = aliases;
        this.typeParameters = typeParameters;
    }

    @Override
    public PasField.FieldType getType() {
        return PasField.FieldType.TYPE;
    }

    @Override
    public boolean isExported() {
        return false;
    }

    @NotNull
    @Override
    public List<String> getParentNames() {
        return parentNames;
    }

    public void setAliases(List<String> aliases) {
        this.aliases = aliases;
    }

    @Override
    public List<String> getAliases() {
        return aliases;
    }

    @NotNull
    @Override
    public List<String> getTypeParameters() {
        return typeParameters;
    }

}
