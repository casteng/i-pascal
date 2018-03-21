package com.siberika.idea.pascal.lang.stub.struct;

import com.intellij.psi.stubs.ILightStubElementType;
import com.intellij.psi.stubs.IndexSink;
import com.intellij.psi.stubs.StubElement;
import com.intellij.psi.stubs.StubInputStream;
import com.intellij.psi.stubs.StubOutputStream;
import com.intellij.util.SmartList;
import com.siberika.idea.pascal.PascalLanguage;
import com.siberika.idea.pascal.lang.psi.PascalStructType;
import com.siberika.idea.pascal.lang.references.ResolveUtil;
import com.siberika.idea.pascal.lang.stub.PascalStructIndex;
import com.siberika.idea.pascal.lang.stub.PascalSymbolIndex;
import com.siberika.idea.pascal.lang.stub.StubUtil;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.List;

public abstract class PasStructDeclStubElementType<StubT extends PasStructStub, PsiT extends PascalStructType> extends ILightStubElementType<StubT, PsiT> {

    PasStructDeclStubElementType(String debugName) {
        super(debugName, PascalLanguage.INSTANCE);
    }

    protected abstract StubT createStub(StubElement parentStub, String name, List<String> parentNames);

    @Override
    public void serialize(@NotNull StubT stub, @NotNull StubOutputStream dataStream) throws IOException {
        StubUtil.printStub("PasStructDeclStub.serialize", stub);
        dataStream.writeName(stub.getName());
        StubUtil.writeStringCollection(dataStream, stub.getParentNames());
    }

    @NotNull
    @Override
    public StubT deserialize(@NotNull StubInputStream dataStream, StubElement parentStub) throws IOException {
        String name = StubUtil.readName(dataStream);
        List<String> parentNames = new SmartList<>();
        StubUtil.readStringCollection(dataStream, parentNames);
        return createStub(parentStub, name, parentNames);
    }

    @Override
    public void indexStub(@NotNull StubT stub, @NotNull IndexSink sink) {
        sink.occurrence(PascalStructIndex.KEY, stub.getUniqueName() + ResolveUtil.STRUCT_SUFFIX);
        sink.occurrence(PascalSymbolIndex.KEY, stub.getName());
    }
}
