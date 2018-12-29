package com.siberika.idea.pascal.lang.stub.struct;

import com.intellij.lang.LighterAST;
import com.intellij.lang.LighterASTNode;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.stubs.IndexSink;
import com.intellij.psi.stubs.StubElement;
import com.intellij.psi.stubs.StubInputStream;
import com.intellij.psi.stubs.StubOutputStream;
import com.intellij.util.SmartList;
import com.siberika.idea.pascal.lang.psi.PascalClassHelperDecl;
import com.siberika.idea.pascal.lang.psi.impl.PasClassHelperDeclImpl;
import com.siberika.idea.pascal.lang.stub.PascalHelperIndex;
import com.siberika.idea.pascal.lang.stub.PascalSymbolIndex;
import com.siberika.idea.pascal.lang.stub.StubUtil;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
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
        return new PasClassHelperDeclStubImpl(parentStub, "-", ".", "", Collections.emptyList(), null, INSTANCE, null);
    }

    @Override
    public PascalClassHelperDecl createPsi(@NotNull PasClassHelperDeclStub stub) {
        return new PasClassHelperDeclImpl(stub, this);
    }

    @NotNull
    @Override
    public PasClassHelperDeclStub createStub(@NotNull PascalClassHelperDecl psi, StubElement parentStub) {
        List<String> aliases = new SmartList<>();
        String stubName = calcStubName(psi, aliases);
        return new PasClassHelperDeclStubImpl(parentStub, stubName, psi.getContainingUnitName(), psi.getTarget(), psi.getParentNames(), aliases, INSTANCE, psi.getTypeParameters());
    }

    @Override
    protected PasClassHelperDeclStub createStub(StubElement parentStub, String name, String containingUnitName,
                                                 List<String> parentNames, List<String> aliases, List<String> typeParameters) {
        throw new IllegalStateException("createStub should not be called for helpers");
    }

    @Override
    protected PasClassHelperDeclStub createHelperStub(StubElement parentStub, String name, String containingUnitName, List<String> parentNames, String target, List<String> typeParameters) {
        return new PasClassHelperDeclStubImpl(parentStub, name, containingUnitName, target, parentNames, Collections.emptyList(), INSTANCE, typeParameters);
    }

    @NotNull
    @Override
    public String getExternalId() {
        return "pas.stub.classhelper";
    }

    @Override
    public void serialize(@NotNull PasClassHelperDeclStub stub, @NotNull StubOutputStream dataStream) throws IOException {
        StubUtil.serializeHelper(stub, dataStream);
    }

    @NotNull
    @Override
    public PasClassHelperDeclStub deserialize(@NotNull StubInputStream dataStream, StubElement parentStub) throws IOException {
        return deserializeHelper(dataStream, parentStub);
    }

    @Override
    public void indexStub(@NotNull PasClassHelperDeclStub stub, @NotNull IndexSink sink) {
        sink.occurrence(PascalSymbolIndex.KEY, stub.getName());
        if (StringUtil.isNotEmpty(stub.getTarget())) {
            sink.occurrence(PascalHelperIndex.KEY, stub.getTarget());
        }
    }

}
