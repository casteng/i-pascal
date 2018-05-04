package com.siberika.idea.pascal.lang.stub;

import com.intellij.psi.PsiElement;
import com.intellij.psi.stubs.IStubElementType;
import com.intellij.psi.stubs.StubBase;
import com.intellij.psi.stubs.StubElement;

public class PasNamedStubBase<T extends PsiElement> extends StubBase<T> {

    protected String name;
    protected String uniqueName;
    protected String containingUnitName;

    protected PasNamedStubBase(StubElement parent, IStubElementType elementType, String name, String containingUnitName) {
        super(parent, elementType);
        this.name = name;
        this.uniqueName = (parent instanceof PasNamedStub ? ((PasNamedStub) parent).getUniqueName() + "." : "") + name;
        this.containingUnitName = containingUnitName;
    }

    public String getName() {
        return name;
    }

    public String getUniqueName() {
        return uniqueName;
    }

    public String getContainingUnitName() {
        return containingUnitName;
    }
}
