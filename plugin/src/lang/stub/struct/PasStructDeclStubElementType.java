package com.siberika.idea.pascal.lang.stub.struct;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.psi.PsiElement;
import com.intellij.psi.stubs.ILightStubElementType;
import com.intellij.psi.stubs.IndexSink;
import com.intellij.psi.stubs.StubElement;
import com.intellij.psi.stubs.StubInputStream;
import com.intellij.psi.stubs.StubOutputStream;
import com.intellij.util.SmartList;
import com.siberika.idea.pascal.PascalLanguage;
import com.siberika.idea.pascal.lang.psi.PasArrayType;
import com.siberika.idea.pascal.lang.psi.PasConstDeclaration;
import com.siberika.idea.pascal.lang.psi.PasTypeDeclaration;
import com.siberika.idea.pascal.lang.psi.PascalNamedElement;
import com.siberika.idea.pascal.lang.psi.PascalStructType;
import com.siberika.idea.pascal.lang.psi.PascalVariableDeclaration;
import com.siberika.idea.pascal.lang.references.ResolveUtil;
import com.siberika.idea.pascal.lang.stub.PascalStructIndex;
import com.siberika.idea.pascal.lang.stub.PascalSymbolIndex;
import com.siberika.idea.pascal.lang.stub.StubUtil;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.List;

public abstract class PasStructDeclStubElementType<StubT extends PasStructStub, PsiT extends PascalStructType> extends ILightStubElementType<StubT, PsiT> {

    private static final Logger LOG = Logger.getInstance(PasStructDeclStubElementType.class);

    PasStructDeclStubElementType(String debugName) {
        super(debugName, PascalLanguage.INSTANCE);
    }

    protected abstract StubT createStub(StubElement parentStub, String name, String containingUnitName, List<String> parentNames, List<String> aliases, List<String> typeParameters);

    @Override
    public void serialize(@NotNull StubT stub, @NotNull StubOutputStream dataStream) throws IOException {
        StubUtil.printStub("PasStructDeclStub.serialize", stub);
        dataStream.writeName(stub.getName());
        dataStream.writeName(stub.getContainingUnitName());
        StubUtil.writeStringCollection(dataStream, stub.getParentNames());
        StubUtil.writeStringCollection(dataStream, stub.getAliases());
        StubUtil.writeStringCollection(dataStream, stub.getTypeParameters());
    }

    @NotNull
    @Override
    public StubT deserialize(@NotNull StubInputStream dataStream, StubElement parentStub) throws IOException {
        String name = StubUtil.readName(dataStream);
        String containingUnitName = StubUtil.readName(dataStream);
        List<String> parentNames = new SmartList<>();
        StubUtil.readStringCollection(dataStream, parentNames);
        List<String> aliases = new SmartList<>();
        StubUtil.readStringCollection(dataStream, aliases);
        List<String> typeParameters = new SmartList<>();
        StubUtil.readStringCollection(dataStream, typeParameters);
        return createStub(parentStub, name, containingUnitName, parentNames, aliases, typeParameters);
    }

    @Override
    public void indexStub(@NotNull StubT stub, @NotNull IndexSink sink) {
        sink.occurrence(PascalStructIndex.KEY, stub.getUniqueName());
        sink.occurrence(PascalSymbolIndex.KEY, stub.getName());
//        sink.occurrence(PascalUnitSymbolIndex.KEY, stub.getName().toUpperCase());
    }

    static String calcStubName(PascalStructType psi, List<String> aliases) {
        if (isAnonymous(psi)) {
            PsiElement decl = retrieveUperLevelDecl(psi);
            if (decl instanceof PasTypeDeclaration) {
                return ((PasTypeDeclaration) decl).getGenericTypeIdent().getName();
            } else if (decl instanceof PascalVariableDeclaration) {
                List<? extends PascalNamedElement> idents = ((PascalVariableDeclaration) decl).getNamedIdentDeclList();
                if (idents.size() > 1) {
                    for (int i = 1; i < idents.size(); i++) {
                        aliases.add(idents.get(i).getName() + ResolveUtil.STRUCT_SUFFIX);
                    }
                }
                return !idents.isEmpty() ? idents.get(0).getName() + ResolveUtil.STRUCT_SUFFIX : "";
            } else if (decl instanceof PasConstDeclaration) {
                return ((PasConstDeclaration) decl).getNamedIdentDecl().getName() + ResolveUtil.STRUCT_SUFFIX;
            } else {
                LOG.warn("ERROR: Unexpected anonymous PSI parent class: " + psi.getParent().getParent().getText());
                return "";
            }
        } else {
            return psi.getName() + ResolveUtil.STRUCT_SUFFIX;
        }
    }

    private static PsiElement retrieveUperLevelDecl(PsiElement psi) {
        PsiElement parent = psi.getParent();
        parent = parent != null ? parent.getParent() : null;
        if (parent instanceof PasArrayType) {
            parent = retrieveUperLevelDecl(parent);
        }
        return parent;
    }

    private static boolean isAnonymous(PascalStructType psi) {
        return psi.getName().length() == 0;
    }

}
