package com.siberika.idea.pascal.lang.stub;

import com.intellij.lang.LighterAST;
import com.intellij.lang.LighterASTNode;
import com.intellij.psi.stubs.ILightStubElementType;
import com.intellij.psi.stubs.IndexSink;
import com.intellij.psi.stubs.StubElement;
import com.intellij.psi.stubs.StubInputStream;
import com.intellij.psi.stubs.StubOutputStream;
import com.intellij.util.SmartList;
import com.intellij.util.containers.SmartHashSet;
import com.siberika.idea.pascal.PascalLanguage;
import com.siberika.idea.pascal.lang.psi.PascalIdentDecl;
import com.siberika.idea.pascal.lang.psi.impl.PasField;
import com.siberika.idea.pascal.lang.psi.impl.PascalIdentDeclImpl;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Set;

public class PasIdentStubElementType extends ILightStubElementType<PasIdentStub, PascalIdentDecl> {

    public static PasIdentStubElementType INSTANCE;

    public PasIdentStubElementType(String debugName) {
        super(debugName, PascalLanguage.INSTANCE);
        INSTANCE = this;
    }

    @Override
    public PasIdentStub createStub(LighterAST tree, LighterASTNode node, StubElement parentStub) {
        return new PasIdentStubImpl(parentStub, "-", ".", PasField.FieldType.VARIABLE, null, null,
                PasField.Access.READWRITE, null, Collections.emptyList(), Collections.emptySet());
    }

    @Override
    public PascalIdentDecl createPsi(@NotNull PasIdentStub stub) {
        return PascalIdentDeclImpl.create(stub, this);
    }

    @NotNull
    @Override
    public PasIdentStub createStub(@NotNull PascalIdentDecl psi, StubElement parentStub) {
        return new PasIdentStubImpl(parentStub, psi.getName(), psi.getContainingUnitName(), psi.getType(), psi.getTypeString(), psi.getTypeKind(),
                psi.getAccess(), psi.getValue(), psi.getSubMembers(), psi.getTypeParameters());
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
        dataStream.writeName(stub.getContainingUnitName());
        dataStream.writeName(stub.getType().name());
        dataStream.writeName(stub.getTypeString());
        dataStream.writeName(stub.getTypeKind() != null ? stub.getTypeKind().name() : StubUtil.ENUM_NULL);
        dataStream.writeName(stub.getAccess().name());
        dataStream.writeName(stub.getValue());
        StubUtil.writeStringCollection(dataStream, stub.getSubMembers());
        StubUtil.writeStringCollection(dataStream, stub.getTypeParameters());
    }

    @NotNull
    @Override
    public PasIdentStub deserialize(@NotNull StubInputStream dataStream, StubElement parentStub) throws IOException {
        String name = StubUtil.readName(dataStream);
        String containingUnitName = StubUtil.readName(dataStream);
        PasField.FieldType type = StubUtil.readEnum(dataStream, PasField.FieldType.class);
        String typeString = StubUtil.readName(dataStream);
        PasField.Kind kind = StubUtil.readEnum(dataStream, PasField.Kind.class);
        PasField.Access access = StubUtil.readEnum(dataStream, PasField.Access.class);
        String value = StubUtil.readName(dataStream);
        List<String> subMembers = new SmartList<>();
        StubUtil.readStringCollection(dataStream, subMembers);
        Set<String> typeParameters = new SmartHashSet<>();
        StubUtil.readStringCollection(dataStream, typeParameters);
        return new PasIdentStubImpl(parentStub, name, containingUnitName, type, typeString, kind, access, value, subMembers, typeParameters);
    }

    @Override
    public void indexStub(@NotNull PasIdentStub stub, @NotNull IndexSink sink) {
        sink.occurrence(PascalSymbolIndex.KEY, stub.getName());
        sink.occurrence(PascalUnitSymbolIndex.KEY, stub.getName().toUpperCase());
    }
}
