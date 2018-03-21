package com.siberika.idea.pascal.lang.stub.struct;

import com.intellij.lang.LighterAST;
import com.intellij.lang.LighterASTNode;
import com.intellij.psi.stubs.StubElement;
import com.siberika.idea.pascal.lang.psi.PascalRecordDecl;
import com.siberika.idea.pascal.lang.psi.impl.PasRecordDeclImpl;
import com.siberika.idea.pascal.lang.references.ResolveUtil;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;

public class PasRecordDeclStubElementType extends PasStructDeclStubElementType<PasRecordDeclStub, PascalRecordDecl> {

    private static PasRecordDeclStubElementType INSTANCE;

    public PasRecordDeclStubElementType(String debugName) {
        super(debugName);
        INSTANCE = this;
    }

    @Override
    public PasRecordDeclStub createStub(LighterAST tree, LighterASTNode node, StubElement parentStub) {
        return new PasRecordDeclStubImpl(parentStub, "-", Collections.emptyList(), INSTANCE);
    }

    @Override
    public PascalRecordDecl createPsi(@NotNull PasRecordDeclStub stub) {
        return new PasRecordDeclImpl(stub, this);
    }

    @NotNull
    @Override
    public PasRecordDeclStub createStub(@NotNull PascalRecordDecl psi, StubElement parentStub) {
        return new PasRecordDeclStubImpl(parentStub, psi.getName() + ResolveUtil.STRUCT_SUFFIX, psi.getParentNames(), INSTANCE);
    }

    @Override
    protected PasRecordDeclStub createStub(StubElement parentStub, String name, List<String> parentNames) {
        return new PasRecordDeclStubImpl(parentStub, name, parentNames, INSTANCE);
    }

    @NotNull
    @Override
    public String getExternalId() {
        return "pas.stub.record";
    }

}
