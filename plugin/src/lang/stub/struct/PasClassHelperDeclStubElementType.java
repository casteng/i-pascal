package com.siberika.idea.pascal.lang.stub.struct;

import com.intellij.lang.LighterAST;
import com.intellij.lang.LighterASTNode;
import com.intellij.psi.stubs.StubElement;
import com.intellij.util.SmartList;
import com.siberika.idea.pascal.lang.psi.PascalClassHelperDecl;
import com.siberika.idea.pascal.lang.psi.impl.PasClassHelperDeclImpl;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;

public class PasClassHelperDeclStubElementType extends PasStructDeclStubElementType<PasClassHelperDeclStub, PascalClassHelperDecl> {

    private static PasClassHelperDeclStubElementType INSTANCE;

    public PasClassHelperDeclStubElementType(String debugName) {
        super(debugName);
        INSTANCE = this;
    }

    @Override
    public PasClassHelperDeclStub createStub(LighterAST tree, LighterASTNode node, StubElement parentStub) {
        return new PasClassHelperDeclStubImpl(parentStub, "-", ".", Collections.emptyList(), null, INSTANCE, null);
    }

    @Override
    public PascalClassHelperDecl createPsi(@NotNull PasClassHelperDeclStub stub) {
        return new PasClassHelperDeclImpl(stub, this);
    }

    @NotNull
    @Override
    public PasClassHelperDeclStub createStub(@NotNull PascalClassHelperDecl psi, StubElement parentStub) {
        List<String> aliases = new SmartList<>();
        String stubName = calcStubName(psi, aliases);
        return new PasClassHelperDeclStubImpl(parentStub, stubName, psi.getContainingUnitName(), psi.getParentNames(), aliases, INSTANCE, psi.getTypeParameters());
    }

    @Override
    protected PasClassHelperDeclStub createStub(StubElement parentStub, String name, String containingUnitName,
                                                List<String> parentNames, List<String> aliases, List<String> typeParameters) {
        return new PasClassHelperDeclStubImpl(parentStub, name, containingUnitName, parentNames, aliases, INSTANCE, typeParameters);
    }

    @NotNull
    @Override
    public String getExternalId() {
        return "pas.stub.classhelper";
    }

}
