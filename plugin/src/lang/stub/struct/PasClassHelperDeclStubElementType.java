package com.siberika.idea.pascal.lang.stub.struct;

import com.intellij.lang.LighterAST;
import com.intellij.lang.LighterASTNode;
import com.intellij.psi.stubs.StubElement;
import com.siberika.idea.pascal.lang.psi.PascalClassHelperDecl;
import com.siberika.idea.pascal.lang.psi.impl.PasClassHelperDeclImpl;
import com.siberika.idea.pascal.lang.references.ResolveUtil;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;

public class PasClassHelperDeclStubElementType extends PasStructDeclStubElementType<PasClassHelperDeclStub, PascalClassHelperDecl> {

    private static PasClassHelperDeclStubElementType INSTANCE;

    public PasClassHelperDeclStubElementType(String debugName) {
        super(debugName);
        INSTANCE = this;
    }

    @Override
    public PasClassHelperDeclStub createStub(LighterAST tree, LighterASTNode node, StubElement parentStub) {
        return new PasClassHelperDeclStubImpl(parentStub, "-", Collections.emptyList(), INSTANCE);
    }

    @Override
    public PascalClassHelperDecl createPsi(@NotNull PasClassHelperDeclStub stub) {
        return new PasClassHelperDeclImpl(stub, this);
    }

    @NotNull
    @Override
    public PasClassHelperDeclStub createStub(@NotNull PascalClassHelperDecl psi, StubElement parentStub) {
        return new PasClassHelperDeclStubImpl(parentStub, psi.getName() + ResolveUtil.STRUCT_SUFFIX, psi.getParentNames(), INSTANCE);
    }

    @Override
    protected PasClassHelperDeclStub createStub(StubElement parentStub, String name, List<String> parentNames) {
        return new PasClassHelperDeclStubImpl(parentStub, name, parentNames, INSTANCE);
    }

    @NotNull
    @Override
    public String getExternalId() {
        return "pas.stub.classhelper";
    }

}
