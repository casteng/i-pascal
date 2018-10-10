package com.siberika.idea.pascal.lang.stub.struct;

import com.intellij.lang.LighterAST;
import com.intellij.lang.LighterASTNode;
import com.intellij.psi.stubs.StubElement;
import com.intellij.util.SmartList;
import com.siberika.idea.pascal.lang.psi.PascalObjectDecl;
import com.siberika.idea.pascal.lang.psi.impl.PasObjectDeclImpl;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;

public class PasObjectDeclStubElementType extends PasStructDeclStubElementType<PasObjectDeclStub, PascalObjectDecl> {

    private static PasObjectDeclStubElementType INSTANCE;

    public PasObjectDeclStubElementType(String debugName) {
        super(debugName);
        INSTANCE = this;
    }

    @Override
    public PasObjectDeclStub createStub(LighterAST tree, LighterASTNode node, StubElement parentStub) {
        return new PasObjectDeclStubImpl(parentStub, "-", ".", Collections.emptyList(), null, INSTANCE, null);
    }

    @Override
    public PascalObjectDecl createPsi(@NotNull PasObjectDeclStub stub) {
        return new PasObjectDeclImpl(stub, this);
    }

    @NotNull
    @Override
    public PasObjectDeclStub createStub(@NotNull PascalObjectDecl psi, StubElement parentStub) {
        List<String> aliases = new SmartList<>();
        String stubName = calcStubName(psi, aliases);
        return new PasObjectDeclStubImpl(parentStub, stubName, psi.getContainingUnitName(), psi.getParentNames(), aliases, INSTANCE, psi.getTypeParameters());
    }

    @Override
    protected PasObjectDeclStub createStub(StubElement parentStub, String name, String containingUnitName,
                                           List<String> parentNames, List<String> aliases, List<String> typeParameters) {
        return new PasObjectDeclStubImpl(parentStub, name, containingUnitName, parentNames, aliases, INSTANCE, typeParameters);
    }

    @NotNull
    @Override
    public String getExternalId() {
        return "pas.stub.object";
    }

}
