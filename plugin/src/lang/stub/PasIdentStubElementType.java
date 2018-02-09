package com.siberika.idea.pascal.lang.stub;

import com.intellij.lang.LighterAST;
import com.intellij.lang.LighterASTNode;
import com.intellij.psi.stubs.ILightStubElementType;
import com.intellij.psi.stubs.IndexSink;
import com.intellij.psi.stubs.StubElement;
import com.intellij.psi.stubs.StubInputStream;
import com.intellij.psi.stubs.StubOutputStream;
import com.siberika.idea.pascal.PascalLanguage;
import com.siberika.idea.pascal.lang.psi.PascalIdentDecl;
import com.siberika.idea.pascal.lang.psi.impl.PascalIdentDeclImpl;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

public class PasIdentStubElementType extends ILightStubElementType<PasIdentStub, PascalIdentDecl> {

    public static PasIdentStubElementType INSTANCE;

    public PasIdentStubElementType(String debugName) {
        super(debugName, PascalLanguage.INSTANCE);
        INSTANCE = this;
    }

    @Override
    public PasIdentStub createStub(LighterAST tree, LighterASTNode node, StubElement parentStub) {
        return new PasIdentStubImpl(parentStub, "-");
    }

    @Override
    public PascalIdentDecl createPsi(@NotNull PasIdentStub stub) {
        return new PascalIdentDeclImpl(stub, this);
    }

    @NotNull
    @Override
    public PasIdentStub createStub(@NotNull PascalIdentDecl psi, StubElement parentStub) {
        return new PasIdentStubImpl(parentStub, psi.getName());
    }

    @NotNull
    @Override
    public String getExternalId() {
        return "pas.stub.ident";
    }

    @Override
    public void serialize(@NotNull PasIdentStub stub, @NotNull StubOutputStream dataStream) throws IOException {
        StubUtil.printStub("PasIdentStub.serialize", stub);

        dataStream.writeName(stub.getName());
    }

    @NotNull
    @Override
    public PasIdentStub deserialize(@NotNull StubInputStream dataStream, StubElement parentStub) throws IOException {
        System.out.println("PasIdentStubElementType.deserialize");
        String name = StubUtil.readName(dataStream);
        return new PasIdentStubImpl(parentStub, name);
    }

    @Override
    public void indexStub(@NotNull PasIdentStub stub, @NotNull IndexSink sink) {
        //System.out.println("PasIdentStubElementType.indexStub");
    }
}
