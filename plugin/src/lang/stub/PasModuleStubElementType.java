package com.siberika.idea.pascal.lang.stub;

import com.intellij.lang.LighterAST;
import com.intellij.lang.LighterASTNode;
import com.intellij.psi.stubs.ILightStubElementType;
import com.intellij.psi.stubs.IndexSink;
import com.intellij.psi.stubs.StubElement;
import com.intellij.psi.stubs.StubInputStream;
import com.intellij.psi.stubs.StubOutputStream;
import com.intellij.util.containers.SmartHashSet;
import com.siberika.idea.pascal.PascalLanguage;
import com.siberika.idea.pascal.lang.psi.PascalModule;
import com.siberika.idea.pascal.lang.psi.impl.PasModuleImpl;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.Collections;
import java.util.Set;

/**
 * Author: George Bakhtadze
 * Date: 13/10/2015
 */
public class PasModuleStubElementType extends ILightStubElementType<PasModuleStub, PascalModule> {

    public static PasModuleStubElementType INSTANCE;

    public PasModuleStubElementType(String debugName) {
        super(debugName, PascalLanguage.INSTANCE);
        INSTANCE = this;
    }

    @Override
    public PasModuleStub createStub(LighterAST tree, LighterASTNode node, StubElement parentStub) {
        return new PasModuleStubImpl(parentStub, "-", null, Collections.emptySet(), Collections.emptySet());
    }

    @Override
    public PascalModule createPsi(@NotNull PasModuleStub stub) {
        return new PasModuleImpl(stub, this);
    }

    @NotNull
    @Override
    public PasModuleStub createStub(@NotNull PascalModule psi, StubElement parentStub) {
        return new PasModuleStubImpl(parentStub, psi.getName(), psi.getModuleType(), psi.getUsedUnitsPublic(), psi.getUsedUnitsPrivate());
    }

    @NotNull
    @Override
    public String getExternalId() {
        return "pas.stub.module";
    }

    @Override
    public void serialize(@NotNull PasModuleStub stub, @NotNull StubOutputStream dataStream) throws IOException {
        StubUtil.printStub("PasModuleStub.serialize", stub);

        dataStream.writeName(stub.getName());
        dataStream.writeName(stub.getModuleType().name());
        StubUtil.writeStringCollection(dataStream, stub.getUsedUnitsPublic());
        StubUtil.writeStringCollection(dataStream, stub.getUsedUnitsPrivate());
    }

    @NotNull
    @Override
    public PasModuleStub deserialize(@NotNull StubInputStream dataStream, StubElement parentStub) throws IOException {
        String name = StubUtil.readName(dataStream);
        PascalModule.ModuleType type = StubUtil.readEnum(dataStream, PascalModule.ModuleType.class);
        Set<String> usedUnitsPublic = new SmartHashSet<>();
        StubUtil.readStringCollection(dataStream, usedUnitsPublic);
        Set<String> usedUnitsPrivate = new SmartHashSet<>();
        StubUtil.readStringCollection(dataStream, usedUnitsPrivate);
        return new PasModuleStubImpl(parentStub, name, type, usedUnitsPublic, usedUnitsPrivate);
    }

    @Override
    public void indexStub(@NotNull PasModuleStub stub, @NotNull IndexSink sink) {
        sink.occurrence(PascalModuleIndex.KEY, stub.getName().toUpperCase());
        sink.occurrence(PascalSymbolIndex.KEY, stub.getName());
        sink.occurrence(PascalUnitSymbolIndex.KEY, stub.getName().toUpperCase());
    }
}
