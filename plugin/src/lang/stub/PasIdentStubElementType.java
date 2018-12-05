package com.siberika.idea.pascal.lang.stub;

import com.intellij.lang.LighterAST;
import com.intellij.lang.LighterASTNode;
import com.intellij.psi.stubs.ILightStubElementType;
import com.intellij.psi.stubs.IndexSink;
import com.intellij.psi.stubs.StubElement;
import com.intellij.psi.stubs.StubInputStream;
import com.intellij.psi.stubs.StubOutputStream;
import com.intellij.util.SmartList;
import com.siberika.idea.pascal.PascalLanguage;
import com.siberika.idea.pascal.lang.psi.PascalIdentDecl;
import com.siberika.idea.pascal.lang.psi.impl.PasField;
import com.siberika.idea.pascal.lang.psi.impl.PascalIdentDeclImpl;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

public class PasIdentStubElementType extends ILightStubElementType<PasIdentStub, PascalIdentDecl> {

    public static PasIdentStubElementType INSTANCE;

    public PasIdentStubElementType(String debugName) {
        super(debugName, PascalLanguage.INSTANCE);
        INSTANCE = this;
    }

    @Override
    public PasIdentStub createStub(LighterAST tree, LighterASTNode node, StubElement parentStub) {
        return new PasIdentStubImpl(parentStub, "-", false, ".", PasField.FieldType.VARIABLE, null, null,
                PasField.Access.READWRITE, null, Collections.emptyList());
    }

    @Override
    public PascalIdentDecl createPsi(@NotNull PasIdentStub stub) {
        return PascalIdentDeclImpl.create(stub, this);
    }

    @NotNull
    @Override
    public PasIdentStub createStub(@NotNull PascalIdentDecl psi, StubElement parentStub) {
        return new PasIdentStubImpl(parentStub, psi.getName(), psi.isExported(), psi.getContainingUnitName(),
                psi.getType(), psi.getTypeString(), psi.getTypeKind(), psi.getAccess(), psi.getValue(), psi.getSubMembers());
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
        dataStream.writeBoolean(stub.isExported());
        dataStream.writeName(stub.getContainingUnitName());
        dataStream.writeName(stub.getType().name());
        dataStream.writeName(stub.getTypeString());
        dataStream.writeName(stub.getTypeKind() != null ? stub.getTypeKind().name() : StubUtil.ENUM_NULL);
        dataStream.writeName(stub.getAccess().name());
        dataStream.writeName(stub.getValue());
        StubUtil.writeStringCollection(dataStream, stub.getSubMembers());
    }

    @NotNull
    @Override
    public PasIdentStub deserialize(@NotNull StubInputStream dataStream, StubElement parentStub) throws IOException {
        String name = StubUtil.readName(dataStream);
        boolean exported = dataStream.readBoolean();
        String containingUnitName = StubUtil.readName(dataStream);
        PasField.FieldType type = StubUtil.readEnum(dataStream, PasField.FieldType.class);
        String typeString = StubUtil.readName(dataStream);
        PasField.Kind kind = StubUtil.readEnum(dataStream, PasField.Kind.class);
        PasField.Access access = StubUtil.readEnum(dataStream, PasField.Access.class);
        String value = StubUtil.readName(dataStream);
        List<String> subMembers = new SmartList<>();
        StubUtil.readStringCollection(dataStream, subMembers);
        return new PasIdentStubImpl(parentStub, name, exported, containingUnitName, type, typeString, kind, access, value, subMembers);
    }

    @Override
    public void indexStub(@NotNull PasIdentStub stub, @NotNull IndexSink sink) {
        sink.occurrence(PascalSymbolIndex.KEY, stub.getName());
        if (stub.isExported()) {
            sink.occurrence(PascalUnitSymbolIndex.KEY, stub.getName().toUpperCase());
        }
    }
}
