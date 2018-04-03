package com.siberika.idea.pascal.lang.stub.struct;

import com.intellij.lang.LighterAST;
import com.intellij.lang.LighterASTNode;
import com.intellij.psi.stubs.StubElement;
import com.intellij.util.SmartList;
import com.siberika.idea.pascal.lang.psi.PascalRecordDecl;
import com.siberika.idea.pascal.lang.psi.impl.PasRecordDeclImpl;
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
        return new PasRecordDeclStubImpl(parentStub, "-", Collections.emptyList(), null, INSTANCE);
    }

    @Override
    public PascalRecordDecl createPsi(@NotNull PasRecordDeclStub stub) {
        return new PasRecordDeclImpl(stub, this);
    }

    @NotNull
    @Override
    public PasRecordDeclStub createStub(@NotNull PascalRecordDecl psi, StubElement parentStub) {
        List<String> aliases = new SmartList<>();
        String stubName = calcStubName(psi, aliases);
        PasRecordDeclStubImpl result = new PasRecordDeclStubImpl(parentStub, stubName, psi.getParentNames(), aliases, INSTANCE);
        return result;
    }

    @Override
    protected PasRecordDeclStub createStub(StubElement parentStub, String name, List<String> parentNames, List<String> aliases) {
        return new PasRecordDeclStubImpl(parentStub, name, parentNames, aliases, INSTANCE);
    }

    @NotNull
    @Override
    public String getExternalId() {
        return "pas.stub.record";
    }

}
