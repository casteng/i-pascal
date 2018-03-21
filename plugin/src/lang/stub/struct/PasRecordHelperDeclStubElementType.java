package com.siberika.idea.pascal.lang.stub.struct;

import com.intellij.lang.LighterAST;
import com.intellij.lang.LighterASTNode;
import com.intellij.psi.stubs.StubElement;
import com.siberika.idea.pascal.lang.psi.PascalRecordHelperDecl;
import com.siberika.idea.pascal.lang.psi.impl.PasRecordHelperDeclImpl;
import com.siberika.idea.pascal.lang.references.ResolveUtil;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;

public class PasRecordHelperDeclStubElementType extends PasStructDeclStubElementType<PasRecordHelperDeclStub, PascalRecordHelperDecl> {

    private static PasRecordHelperDeclStubElementType INSTANCE;

    public PasRecordHelperDeclStubElementType(String debugName) {
        super(debugName);
        INSTANCE = this;
    }

    @Override
    public PasRecordHelperDeclStub createStub(LighterAST tree, LighterASTNode node, StubElement parentStub) {
        return new PasRecordHelperDeclStubImpl(parentStub, "-", Collections.emptyList(), INSTANCE);
    }

    @Override
    public PascalRecordHelperDecl createPsi(@NotNull PasRecordHelperDeclStub stub) {
        return new PasRecordHelperDeclImpl(stub, this);
    }

    @NotNull
    @Override
    public PasRecordHelperDeclStub createStub(@NotNull PascalRecordHelperDecl psi, StubElement parentStub) {
        return new PasRecordHelperDeclStubImpl(parentStub, psi.getName() + ResolveUtil.STRUCT_SUFFIX, psi.getParentNames(), INSTANCE);
    }

    @Override
    protected PasRecordHelperDeclStub createStub(StubElement parentStub, String name, List<String> parentNames) {
        return new PasRecordHelperDeclStubImpl(parentStub, name, parentNames, INSTANCE);
    }

    @NotNull
    @Override
    public String getExternalId() {
        return "pas.stub.recordhelper";
    }

}
