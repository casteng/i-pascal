package com.siberika.idea.pascal.lang.stub.struct;

import com.intellij.lang.LighterAST;
import com.intellij.lang.LighterASTNode;
import com.intellij.psi.stubs.ILightStubElementType;
import com.intellij.psi.stubs.IndexSink;
import com.intellij.psi.stubs.StubElement;
import com.intellij.psi.stubs.StubInputStream;
import com.intellij.psi.stubs.StubOutputStream;
import com.siberika.idea.pascal.PascalLanguage;
import com.siberika.idea.pascal.lang.psi.PascalClassDecl;
import com.siberika.idea.pascal.lang.psi.impl.PascalClassDeclImpl;
import com.siberika.idea.pascal.lang.stub.StubUtil;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

public class PasClassDeclStubElementType extends ILightStubElementType<PasClassDeclStub, PascalClassDecl> {

    public static PasClassDeclStubElementType INSTANCE;

    public PasClassDeclStubElementType(String debugName) {
        super(debugName, PascalLanguage.INSTANCE);
        INSTANCE = this;
    }

    @Override
    public PasClassDeclStub createStub(LighterAST tree, LighterASTNode node, StubElement parentStub) {
        return new PasClassDeclStubImpl(parentStub, "-");
    }

    @Override
    public PascalClassDecl createPsi(@NotNull PasClassDeclStub stub) {
        return new PascalClassDeclImpl(stub, this);
    }

    @NotNull
    @Override
    public PasClassDeclStub createStub(@NotNull PascalClassDecl psi, StubElement parentStub) {
        return new PasClassDeclStubImpl(parentStub, psi.getName());
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
    }

    @NotNull
    @Override
    public PasClassDeclStub deserialize(@NotNull StubInputStream dataStream, StubElement parentStub) throws IOException {
        System.out.println("PasClassDeclStubElementType.deserialize");
        String name = StubUtil.readName(dataStream);
        return new PasClassDeclStubImpl(parentStub, name);
    }

    @Override
    public void indexStub(@NotNull PasClassDeclStub stub, @NotNull IndexSink sink) {
        //System.out.println("PasClassDeclStubElementType.indexStub");
    }
}
