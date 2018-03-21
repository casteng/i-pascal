package com.siberika.idea.pascal.lang.stub.struct;

import com.intellij.lang.LighterAST;
import com.intellij.lang.LighterASTNode;
import com.intellij.psi.stubs.StubElement;
import com.siberika.idea.pascal.lang.psi.PascalClassDecl;
import com.siberika.idea.pascal.lang.psi.impl.PasClassTypeDeclImpl;
import com.siberika.idea.pascal.lang.references.ResolveUtil;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;

public class PasClassDeclStubElementType extends PasStructDeclStubElementType<PasClassDeclStub, PascalClassDecl> {

    private static PasClassDeclStubElementType INSTANCE;

    public PasClassDeclStubElementType(String debugName) {
        super(debugName);
        INSTANCE = this;
    }

    @Override
    public PasClassDeclStub createStub(LighterAST tree, LighterASTNode node, StubElement parentStub) {
        return new PasClassDeclStubImpl(parentStub, "-", Collections.emptyList(), INSTANCE);
    }

    @Override
    public PascalClassDecl createPsi(@NotNull PasClassDeclStub stub) {
        return new PasClassTypeDeclImpl(stub, this);
    }

    @NotNull
    @Override
    public PasClassDeclStub createStub(@NotNull PascalClassDecl psi, StubElement parentStub) {
        return new PasClassDeclStubImpl(parentStub, psi.getName() + ResolveUtil.STRUCT_SUFFIX, psi.getParentNames(), INSTANCE);
    }

    @Override
    protected PasClassDeclStub createStub(StubElement parentStub, String name, List<String> parentNames) {
        return new PasClassDeclStubImpl(parentStub, name, parentNames, INSTANCE);
    }

    @NotNull
    @Override
    public String getExternalId() {
        return "pas.stub.class";
    }

}
