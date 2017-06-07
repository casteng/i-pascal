package com.siberika.idea.pascal.lang.stub;

import com.intellij.lang.LighterAST;
import com.intellij.lang.LighterASTNode;
import com.intellij.psi.stubs.ILightStubElementType;
import com.intellij.psi.stubs.IndexSink;
import com.intellij.psi.stubs.StubElement;
import com.intellij.psi.stubs.StubInputStream;
import com.intellij.psi.stubs.StubOutputStream;
import com.siberika.idea.pascal.PascalLanguage;
import com.siberika.idea.pascal.lang.psi.impl.PascalModule;
import com.siberika.idea.pascal.lang.psi.impl.PascalModuleImpl;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

/**
 * Author: George Bakhtadze
 * Date: 13/10/2015
 */
public class PasModuleStubElementType extends ILightStubElementType<PasModuleStub, PascalModule> {

    public PasModuleStubElementType(String debugName) {
        super(debugName, PascalLanguage.INSTANCE);
    }

    @Override
    public PasModuleStub createStub(LighterAST tree, LighterASTNode node, StubElement parentStub) {
        return new PasModuleStubImpl(parentStub, this);
    }

    @Override
    public PascalModule createPsi(@NotNull PasModuleStub stub) {
        return new PascalModuleImpl(stub, this);
    }

    @NotNull
    @Override
    public PasModuleStub createStub(@NotNull PascalModule psi, StubElement parentStub) {
        return new PasModuleStubImpl(parentStub, this);
    }

    @NotNull
    @Override
    public String getExternalId() {
        return "pas.stub.module";
    }

    @Override
    public void serialize(@NotNull PasModuleStub stub, @NotNull StubOutputStream dataStream) throws IOException {
        System.out.println("PasModuleStubElementType.serialize");
        //dataStream.writeName(stub.getName());
    }

    @NotNull
    @Override
    public PasModuleStub deserialize(@NotNull StubInputStream dataStream, StubElement parentStub) throws IOException {
        System.out.println("PasModuleStubElementType.deserialize");
        return new PasModuleStubImpl(parentStub, this);
    }

    @Override
    public void indexStub(@NotNull PasModuleStub stub, @NotNull IndexSink sink) {
        System.out.println("PasModuleStubElementType.indexStub");
    }
}
