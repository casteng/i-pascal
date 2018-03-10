package com.siberika.idea.pascal.lang.stub.struct;

import com.intellij.lang.LighterAST;
import com.intellij.lang.LighterASTNode;
import com.intellij.psi.stubs.ILightStubElementType;
import com.intellij.psi.stubs.IndexSink;
import com.intellij.psi.stubs.StubElement;
import com.intellij.psi.stubs.StubInputStream;
import com.intellij.psi.stubs.StubOutputStream;
import com.intellij.util.SmartList;
import com.siberika.idea.pascal.PascalLanguage;
import com.siberika.idea.pascal.lang.psi.PascalClassDecl;
import com.siberika.idea.pascal.lang.psi.impl.PasClassTypeDeclImpl;
import com.siberika.idea.pascal.lang.stub.PascalStructIndex;
import com.siberika.idea.pascal.lang.stub.StubUtil;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

public class PasClassDeclStubElementType extends ILightStubElementType<PasClassDeclStub, PascalClassDecl> {

    public static PasClassDeclStubElementType INSTANCE;

    public PasClassDeclStubElementType(String debugName) {
        super(debugName, PascalLanguage.INSTANCE);
        INSTANCE = this;
    }

    @Override
    public PasClassDeclStub createStub(LighterAST tree, LighterASTNode node, StubElement parentStub) {
        return new PasClassDeclStubImpl(parentStub, "-", Collections.emptyList());
    }

    @Override
    public PascalClassDecl createPsi(@NotNull PasClassDeclStub stub) {
        return new PasClassTypeDeclImpl(stub, this);
    }

    @NotNull
    @Override
    public PasClassDeclStub createStub(@NotNull PascalClassDecl psi, StubElement parentStub) {
        return new PasClassDeclStubImpl(parentStub, "#" + psi.getName(), psi.getParentNames());
    }

    @NotNull
    @Override
    public String getExternalId() {
        return "pas.stub.class";
    }

    @Override
    public void serialize(@NotNull PasClassDeclStub stub, @NotNull StubOutputStream dataStream) throws IOException {
        StubUtil.printStub("PasClassDeclStub.serialize", stub);
        dataStream.writeName(stub.getName());
        StubUtil.writeStringCollection(dataStream, stub.getParentNames());
    }

    @NotNull
    @Override
    public PasClassDeclStub deserialize(@NotNull StubInputStream dataStream, StubElement parentStub) throws IOException {
        String name = StubUtil.readName(dataStream);
        List<String> parentNames = new SmartList<>();
        StubUtil.readStringCollection(dataStream, parentNames);
        return new PasClassDeclStubImpl(parentStub, name, parentNames);
    }

    @Override
    public void indexStub(@NotNull PasClassDeclStub stub, @NotNull IndexSink sink) {
        sink.occurrence(PascalStructIndex.KEY, "#" + stub.getName());
    }
}
