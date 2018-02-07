package com.siberika.idea.pascal.lang.stub;

import com.intellij.lang.LighterAST;
import com.intellij.lang.LighterASTNode;
import com.intellij.psi.stubs.ILightStubElementType;
import com.intellij.psi.stubs.IndexSink;
import com.intellij.psi.stubs.StubElement;
import com.intellij.psi.stubs.StubInputStream;
import com.intellij.psi.stubs.StubOutputStream;
import com.siberika.idea.pascal.PascalLanguage;
import com.siberika.idea.pascal.lang.psi.PascalExportedRoutine;
import com.siberika.idea.pascal.lang.psi.impl.PasExportedRoutineImpl;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

/**
 * Author: George Bakhtadze
 * Date: 13/10/2015
 */
public class PasExportedRoutineStubElementType extends ILightStubElementType<PasExportedRoutineStub, PascalExportedRoutine> {

    public PasExportedRoutineStubElementType(String debugName) {
        super(debugName, PascalLanguage.INSTANCE);
    }

    @Override
    public PasExportedRoutineStub createStub(LighterAST tree, LighterASTNode node, StubElement parentStub) {
        return new PasExportedRoutineStubImpl(parentStub, this);
    }

    @Override
    public PascalExportedRoutine createPsi(@NotNull PasExportedRoutineStub stub) {
        return new PasExportedRoutineImpl(stub, this);
    }

    @NotNull
    @Override
    public PasExportedRoutineStub createStub(@NotNull PascalExportedRoutine psi, StubElement parentStub) {
        return new PasExportedRoutineStubImpl(parentStub, this);
    }

    @NotNull
    @Override
    public String getExternalId() {
        return "pas.stub.routine";
    }

    @Override
    public void serialize(@NotNull PasExportedRoutineStub stub, @NotNull StubOutputStream dataStream) throws IOException {
        System.out.println("PasModuleStubElementType.serialize");
        //dataStream.writeName(stub.getName());
    }

    @NotNull
    @Override
    public PasExportedRoutineStub deserialize(@NotNull StubInputStream dataStream, StubElement parentStub) throws IOException {
        System.out.println("PasModuleStubElementType.deserialize");
        return new PasExportedRoutineStubImpl(parentStub, this);
    }

    @Override
    public void indexStub(@NotNull PasExportedRoutineStub stub, @NotNull IndexSink sink) {
        System.out.println("PasModuleStubElementType.indexStub");
    }
}
