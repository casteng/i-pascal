package com.siberika.idea.pascal.lang.stub.struct;

import com.intellij.lang.LighterAST;
import com.intellij.lang.LighterASTNode;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.stubs.IndexSink;
import com.intellij.psi.stubs.StubElement;
import com.intellij.psi.stubs.StubInputStream;
import com.intellij.psi.stubs.StubOutputStream;
import com.intellij.util.SmartList;
import com.siberika.idea.pascal.lang.psi.PascalRecordHelperDecl;
import com.siberika.idea.pascal.lang.psi.impl.PasRecordHelperDeclImpl;
import com.siberika.idea.pascal.lang.stub.PascalHelperIndex;
import com.siberika.idea.pascal.lang.stub.PascalSymbolIndex;
import com.siberika.idea.pascal.lang.stub.StubUtil;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
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
        return new PasRecordHelperDeclStubImpl(parentStub, "-", ".", "", Collections.emptyList(), null, INSTANCE, null);
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
        return new PasRecordHelperDeclStubImpl(parentStub, stubName, psi.getContainingUnitName(), psi.getTarget(), psi.getParentNames(), aliases, INSTANCE, psi.getTypeParameters());
    }

    @Override
    protected PasRecordHelperDeclStub createStub(StubElement parentStub, String name, String containingUnitName,
                                                 List<String> parentNames, List<String> aliases, List<String> typeParameters) {
        throw new IllegalStateException("createStub should not be called for helpers");
    }

    @Override
    protected PasRecordHelperDeclStub createHelperStub(StubElement parentStub, String name, String containingUnitName, List<String> parentNames, String target, List<String> typeParameters) {
        return new PasRecordHelperDeclStubImpl(parentStub, name, containingUnitName, target, parentNames, Collections.emptyList(), INSTANCE, typeParameters);
    }

    @NotNull
    @Override
    public String getExternalId() {
        return "pas.stub.recordhelper";
    }

    @Override
    public void serialize(@NotNull PasRecordHelperDeclStub stub, @NotNull StubOutputStream dataStream) throws IOException {
        StubUtil.serializeHelper(stub, dataStream);
    }

    @NotNull
    @Override
    public PasRecordHelperDeclStub deserialize(@NotNull StubInputStream dataStream, StubElement parentStub) throws IOException {
        return deserializeHelper(dataStream, parentStub);
    }

    @Override
    public void indexStub(@NotNull PasRecordHelperDeclStub stub, @NotNull IndexSink sink) {
        sink.occurrence(PascalSymbolIndex.KEY, stub.getName());
        if (StringUtil.isNotEmpty(stub.getTarget())) {
            sink.occurrence(PascalHelperIndex.KEY, stub.getTarget().toUpperCase());
        }
    }

}
