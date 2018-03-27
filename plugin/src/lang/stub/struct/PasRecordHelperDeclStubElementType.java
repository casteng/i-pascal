package com.siberika.idea.pascal.lang.stub.struct;

import com.intellij.lang.LighterAST;
import com.intellij.lang.LighterASTNode;
import com.intellij.psi.stubs.StubElement;
import com.siberika.idea.pascal.lang.psi.PascalRecordHelperDecl;
import com.siberika.idea.pascal.lang.psi.impl.PasRecordHelperDeclImpl;
import kotlin.reflect.jvm.internal.impl.utils.SmartList;
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
        return new PasRecordHelperDeclStubImpl(parentStub, "-", Collections.emptyList(), null, INSTANCE);
    }

    @Override
    public PascalRecordHelperDecl createPsi(@NotNull PasRecordHelperDeclStub stub) {
        return new PasRecordHelperDeclImpl(stub, this);
    }

    @NotNull
    @Override
    public PasRecordHelperDeclStub createStub(@NotNull PascalRecordHelperDecl psi, StubElement parentStub) {
        List<String> aliases = new SmartList<>();
        String stubName = calcStubName(psi, aliases);
        return new PasRecordHelperDeclStubImpl(parentStub, stubName, psi.getParentNames(), aliases, INSTANCE);
    }

    @Override
    protected PasRecordHelperDeclStub createStub(StubElement parentStub, String name, List<String> parentNames, List<String> aliases) {
        return new PasRecordHelperDeclStubImpl(parentStub, name, parentNames, aliases, INSTANCE);
    }

    @NotNull
    @Override
    public String getExternalId() {
        return "pas.stub.recordhelper";
    }

}
