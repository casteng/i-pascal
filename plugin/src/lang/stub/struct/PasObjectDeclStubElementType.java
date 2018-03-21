package com.siberika.idea.pascal.lang.stub.struct;

import com.intellij.lang.LighterAST;
import com.intellij.lang.LighterASTNode;
import com.intellij.psi.stubs.StubElement;
import com.siberika.idea.pascal.lang.psi.PascalObjectDecl;
import com.siberika.idea.pascal.lang.psi.impl.PasObjectDeclImpl;
import com.siberika.idea.pascal.lang.references.ResolveUtil;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;

public class PasObjectDeclStubElementType extends PasStructDeclStubElementType<PasObjectDeclStub, PascalObjectDecl> {

    private static PasObjectDeclStubElementType INSTANCE;

    public PasObjectDeclStubElementType(String debugName) {
        super(debugName);
        INSTANCE = this;
    }

    @Override
    public PasObjectDeclStub createStub(LighterAST tree, LighterASTNode node, StubElement parentStub) {
        return new PasObjectDeclStubImpl(parentStub, "-", Collections.emptyList(), INSTANCE);
    }

    @Override
    public PascalObjectDecl createPsi(@NotNull PasObjectDeclStub stub) {
        return new PasObjectDeclImpl(stub, this);
    }

    @NotNull
    @Override
    public PasObjectDeclStub createStub(@NotNull PascalObjectDecl psi, StubElement parentStub) {
        return new PasObjectDeclStubImpl(parentStub, psi.getName() + ResolveUtil.STRUCT_SUFFIX, psi.getParentNames(), INSTANCE);
    }

    @Override
    protected PasObjectDeclStub createStub(StubElement parentStub, String name, List<String> parentNames) {
        return new PasObjectDeclStubImpl(parentStub, name, parentNames, INSTANCE);
    }

    @NotNull
    @Override
    public String getExternalId() {
        return "pas.stub.object";
    }

}
