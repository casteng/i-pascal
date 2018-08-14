package com.siberika.idea.pascal.lang.stub.struct;

import com.intellij.psi.stubs.StubElement;
import com.siberika.idea.pascal.lang.psi.PascalStructType;
import com.siberika.idea.pascal.lang.psi.impl.PasField;
import com.siberika.idea.pascal.lang.stub.PasNamedStubBase;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Set;

public class PasStructStubImpl<T extends PascalStructType> extends PasNamedStubBase<T> implements PasStructStub<T> {

    boolean local;
    private List<String> parentNames;
    private List<String> aliases;
    private Set<String> typeParameters;

    public PasStructStubImpl(StubElement parent, String name, String containingUnitName, boolean local, List<String> parentNames,
                             List<String> aliases, PasStructDeclStubElementType elementType) {
        super(parent, elementType, name, containingUnitName);
        this.local = local;
        this.parentNames = parentNames;
        this.aliases = aliases;
    }

    @Override
    public PasField.FieldType getType() {
        return PasField.FieldType.TYPE;
    }

    @Override
    public boolean isLocal() {
        return local;
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
    public Set<String> getTypeParameters() {
        return typeParameters;
    }

}
