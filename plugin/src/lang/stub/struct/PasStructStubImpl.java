package com.siberika.idea.pascal.lang.stub.struct;

import com.intellij.psi.stubs.StubBase;
import com.intellij.psi.stubs.StubElement;
import com.siberika.idea.pascal.lang.psi.PascalStructType;
import com.siberika.idea.pascal.lang.psi.impl.PasField;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class PasStructStubImpl<T extends PascalStructType> extends StubBase<T> implements PasStructStub<T> {

    private String name;
    private List<String> parentNames;

    public PasStructStubImpl(StubElement parent, String name, List<String> parentNames) {
        super(parent, PasClassDeclStubElementType.INSTANCE);
        this.name = name;
        this.parentNames = parentNames;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public PasField.FieldType getType() {
        return PasField.FieldType.TYPE;
    }

    @NotNull
    @Override
    public List<String> getParentNames() {
        return parentNames;
    }
}
