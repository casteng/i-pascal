package com.siberika.idea.pascal.lang.stub.struct;

import com.intellij.lang.LighterAST;
import com.intellij.lang.LighterASTNode;
import com.intellij.psi.stubs.StubElement;
import com.intellij.util.SmartList;
import com.siberika.idea.pascal.lang.psi.PascalClassDecl;
import com.siberika.idea.pascal.lang.psi.impl.PasClassTypeDeclImpl;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;

public class PasClassDeclStubElementType extends PasStructDeclStubElementType<PasClassDeclStub, PascalClassDecl> {

    private static PasClassDeclStubElementType INSTANCE;

    public PasClassDeclStubElementType(String debugName) {
        super(debugName);
        INSTANCE = this;
    }

    @Override
    public PasClassDeclStub createStub(LighterAST tree, LighterASTNode node, StubElement parentStub) {
        return new PasClassDeclStubImpl(parentStub, "-", Collections.emptyList(), null, INSTANCE);
    }

    @Override
    public PascalClassDecl createPsi(@NotNull PasClassDeclStub stub) {
        return new PasClassTypeDeclImpl(stub, this);
    }

    @NotNull
    @Override
    public PasClassDeclStub createStub(@NotNull PascalClassDecl psi, StubElement parentStub) {
        List<String> aliases = new SmartList<>();
        String stubName = calcStubName(psi, aliases);
        return new PasClassDeclStubImpl(parentStub, stubName, psi.getParentNames(), aliases, INSTANCE);
    }

    @Override
    protected PasClassDeclStub createStub(StubElement parentStub, String name, List<String> parentNames, List<String> aliases) {
        return new PasClassDeclStubImpl(parentStub, name, parentNames, aliases, INSTANCE);
    }

    @NotNull
    @Override
    public String getExternalId() {
        return "pas.stub.class";
    }

}
