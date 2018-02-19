package com.siberika.idea.pascal.lang.psi.impl;

import com.intellij.lang.ASTNode;
import com.intellij.psi.stubs.IStubElementType;
import com.intellij.psi.stubs.StubElement;
import com.siberika.idea.pascal.lang.psi.PascalModule;
import com.siberika.idea.pascal.lang.stub.PasModuleStub;
import com.siberika.idea.pascal.lang.stub.PasNamedStub;
import kotlin.reflect.jvm.internal.impl.utils.SmartList;

import java.util.Collection;

// TODO: merge with PascalModuleImpl
public abstract class PascalModuleImplStub extends PasStubScopeImpl<PasModuleStub> implements PascalModule {
    public PascalModuleImplStub(ASTNode node) {
        super(node);
    }

    public PascalModuleImplStub(PasModuleStub stub, IStubElementType nodeType) {
        super(stub, nodeType);
    }

    PasField getFieldStub(String name) {
        for (StubElement stubElement : getStub().getChildrenStubs()) {
            if (name.equalsIgnoreCase(((PasNamedStub) stubElement).getName())) {
                return new PasField((PasNamedStub) stubElement);
            }
        }
        return null;
    }

    Collection<PasField> getAllFieldsStub() {
        Collection<PasField> res = new SmartList<PasField>();
        for (StubElement stubElement : getStub().getChildrenStubs()) {
            res.add(new PasField((PasNamedStub) stubElement));
        }
        return res;
    }

}
