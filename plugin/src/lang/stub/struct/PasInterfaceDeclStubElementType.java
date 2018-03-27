package com.siberika.idea.pascal.lang.stub.struct;

import com.intellij.lang.LighterAST;
import com.intellij.lang.LighterASTNode;
import com.intellij.psi.stubs.StubElement;
import com.siberika.idea.pascal.lang.psi.PascalInterfaceDecl;
import com.siberika.idea.pascal.lang.psi.impl.PasInterfaceTypeDeclImpl;
import kotlin.reflect.jvm.internal.impl.utils.SmartList;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;

public class PasInterfaceDeclStubElementType extends PasStructDeclStubElementType<PasInterfaceDeclStub, PascalInterfaceDecl> {

    private static PasInterfaceDeclStubElementType INSTANCE;

    public PasInterfaceDeclStubElementType(String debugName) {
        super(debugName);
        INSTANCE = this;
    }

    @Override
    public PasInterfaceDeclStub createStub(LighterAST tree, LighterASTNode node, StubElement parentStub) {
        return new PasInterfaceDeclStubImpl(parentStub, "-", Collections.emptyList(), null, INSTANCE);
    }

    @Override
    public PascalInterfaceDecl createPsi(@NotNull PasInterfaceDeclStub stub) {
        return new PasInterfaceTypeDeclImpl(stub, this);
    }

    @NotNull
    @Override
    public PasInterfaceDeclStub createStub(@NotNull PascalInterfaceDecl psi, StubElement parentStub) {
        List<String> aliases = new SmartList<>();
        String stubName = calcStubName(psi, aliases);
        return new PasInterfaceDeclStubImpl(parentStub, stubName, psi.getParentNames(), aliases, INSTANCE);
    }

    @Override
    protected PasInterfaceDeclStub createStub(StubElement parentStub, String name, List<String> parentNames, List<String> aliases) {
        return new PasInterfaceDeclStubImpl(parentStub, name, parentNames, aliases, INSTANCE);
    }

    @NotNull
    @Override
    public String getExternalId() {
        return "pas.stub.interface";
    }

}
