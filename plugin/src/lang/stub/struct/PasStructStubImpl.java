package com.siberika.idea.pascal.lang.stub.struct;

import com.intellij.psi.stubs.StubBase;
import com.intellij.psi.stubs.StubElement;
import com.siberika.idea.pascal.lang.psi.PascalStructType;
import com.siberika.idea.pascal.lang.psi.impl.PasField;
import com.siberika.idea.pascal.lang.stub.PasNamedStub;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class PasStructStubImpl<T extends PascalStructType> extends StubBase<T> implements PasStructStub<T> {

    private String name;
    private String uniqueName;
    private List<String> parentNames;
    private List<String> aliases;

    public PasStructStubImpl(StubElement parent, String name, List<String> parentNames, List<String> aliases, PasStructDeclStubElementType elementType) {
        super(parent, elementType);
        this.name = name;
        this.uniqueName = (parent instanceof PasNamedStub ? ((PasNamedStub) parent).getUniqueName() + "." : "") + name;
        this.parentNames = parentNames;
        this.aliases = aliases;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public PasField.FieldType getType() {
        return PasField.FieldType.TYPE;
    }

    @Override
    public String getUniqueName() {
        return uniqueName;
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
}
