package com.siberika.idea.pascal.lang.stub.struct;

import com.intellij.lang.LighterAST;
import com.intellij.lang.LighterASTNode;
import com.intellij.psi.stubs.StubElement;
import com.siberika.idea.pascal.lang.psi.PascalInterfaceDecl;
import com.siberika.idea.pascal.lang.psi.impl.PasInterfaceTypeDeclImpl;
import com.siberika.idea.pascal.lang.references.ResolveUtil;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;

public class PasInterfaceDeclStubElementType extends PasStructDeclStubElementType<PasInterfaceDeclStub, PascalInterfaceDecl> {

    private static PasInterfaceDeclStubElementType INSTANCE;

    public PasInterfaceDeclStubElementType(String debugName) {
        super(debugName);
        INSTANCE = this;
    }

    @Override
    public PasInterfaceDeclStub createStub(LighterAST tree, LighterASTNode node, StubElement parentStub) {
        return new PasInterfaceDeclStubImpl(parentStub, "-", Collections.emptyList(), INSTANCE);
    }

    @Override
    public PascalInterfaceDecl createPsi(@NotNull PasInterfaceDeclStub stub) {
        return new PasInterfaceTypeDeclImpl(stub, this);
    }

    @NotNull
    @Override
    public PasInterfaceDeclStub createStub(@NotNull PascalInterfaceDecl psi, StubElement parentStub) {
        return new PasInterfaceDeclStubImpl(parentStub, psi.getName() + ResolveUtil.STRUCT_SUFFIX, psi.getParentNames(), INSTANCE);
    }

    @Override
    protected PasInterfaceDeclStub createStub(StubElement parentStub, String name, List<String> parentNames) {
        return new PasInterfaceDeclStubImpl(parentStub, name, parentNames, INSTANCE);
    }

    @NotNull
    @Override
    public String getExternalId() {
        return "pas.stub.interface";
    }

}
