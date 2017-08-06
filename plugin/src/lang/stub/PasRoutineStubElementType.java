package com.siberika.idea.pascal.lang.stub;

import com.intellij.lang.LighterAST;
import com.intellij.lang.LighterASTNode;
import com.intellij.psi.stubs.*;
import com.siberika.idea.pascal.PascalLanguage;
import com.siberika.idea.pascal.lang.psi.impl.PascalRoutineImpl;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

/**
 * Author: George Bakhtadze
 * Date: 13/10/2015
 */
public class PasRoutineStubElementType extends ILightStubElementType<PasRoutineStub, PascalRoutineImpl> {

    public PasRoutineStubElementType(String debugName) {
        super(debugName, PascalLanguage.INSTANCE);
    }

    @Override
    public PasRoutineStub createStub(LighterAST tree, LighterASTNode node, StubElement parentStub) {
        return new PasRoutineStubImpl(parentStub, this);
    }

    @Override
    public PascalRoutineImpl createPsi(@NotNull PasRoutineStub stub) {
        return new PascalRoutineImpl(stub, this);
    }

    @NotNull
    @Override
    public PasRoutineStub createStub(@NotNull PascalRoutineImpl psi, StubElement parentStub) {
        return new PasRoutineStubImpl(parentStub, this);
    }

    @NotNull
    @Override
    public String getExternalId() {
        return "pas.stub.module";
    }

    @Override
    public void serialize(@NotNull PasRoutineStub stub, @NotNull StubOutputStream dataStream) throws IOException {
        System.out.println("PasModuleStubElementType.serialize");
        //dataStream.writeName(stub.getName());
    }

    @NotNull
    @Override
    public PasRoutineStub deserialize(@NotNull StubInputStream dataStream, StubElement parentStub) throws IOException {
        System.out.println("PasModuleStubElementType.deserialize");
        return new PasRoutineStubImpl(parentStub, this);
    }

    @Override
    public void indexStub(@NotNull PasRoutineStub stub, @NotNull IndexSink sink) {
        System.out.println("PasModuleStubElementType.indexStub");
    }
}
