package com.siberika.idea.pascal.lang.stub;

import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.stubs.StubElement;
import com.intellij.psi.stubs.StubIndex;
import com.intellij.psi.stubs.StubIndexKey;
import com.intellij.psi.stubs.StubInputStream;
import com.intellij.psi.stubs.StubOutputStream;
import com.intellij.util.io.StringRef;
import com.siberika.idea.pascal.lang.psi.PasEntityScope;
import com.siberika.idea.pascal.lang.psi.PascalStubElement;
import com.siberika.idea.pascal.lang.stub.struct.PasClassDeclStub;
import com.siberika.idea.pascal.lang.stub.struct.PascalHelperDeclStub;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.Collection;
import java.util.List;

public class StubUtil {
    public static final String ENUM_NULL = "-";

    public static <T extends Enum<T>> T readEnum(StubInputStream dataStream, Class<T> clazz) throws IOException {
        String name = readName(dataStream);
        return (name != null) && !name.equals(ENUM_NULL) ? T.valueOf(clazz, name) : null;
    }

    public static String readName(StubInputStream dataStream) throws IOException {
        StringRef ref = dataStream.readName();
        return ref != null ? ref.getString() : null;
    }

    public static void printStub(String msg, StubElement stub) {
        String stubStr = "?";
        if (stub instanceof PasModuleStub) {
            stubStr = "[M]" + ((PasModuleStub) stub).getName();
        } else if (stub instanceof PasExportedRoutineStub) {
            stubStr = "[R]" + ((PasExportedRoutineStub) stub).getName();
        } else if (stub instanceof PasIdentStub) {
            stubStr = "[I]" + ((PasIdentStub) stub).getName();
        } else if (stub instanceof PasClassDeclStub) {
            stubStr = "[C]" + ((PasClassDeclStub) stub).getName();
        }
        StubElement parent = stub.getParentStub();
        String parentStr = " - ";
        if (parent instanceof PasModuleStub) {
            parentStr = "[M]" + ((PasModuleStub) parent).getName();
        } else if (parent instanceof PasExportedRoutineStub) {
            parentStr = "[R]" + ((PasExportedRoutineStub) parent).getName();
        } else if (parent instanceof PasClassDeclStub) {
            parentStr = "[C]" + ((PasClassDeclStub) parent).getName();
        }
        //System.out.println(String.format(msg + ": %s ^ %s", stubStr, parentStr));
    }

    public static void writeStringCollection(StubOutputStream dataStream, Collection<String> collection) throws IOException {
        dataStream.writeInt(collection.size());
        for (String entry : collection) {
            dataStream.writeName(entry);
        }
    }

    public static void readStringCollection(StubInputStream dataStream, Collection<String> result) throws IOException {
        int size = dataStream.readInt();
        for (int i = 0; i < size; i++) {
            StringRef ref = dataStream.readName();
            if (ref != null) {
                result.add(ref.getString());
            }
        }
    }

    public static void writeEnumCollection(StubOutputStream dataStream, List<? extends Enum> collection) throws IOException {
        dataStream.writeInt(collection.size());
        for (Enum entry : collection) {
            dataStream.writeInt(entry.ordinal());
        }
    }

    public static <T extends Enum<T>> void readEnumCollection(StubInputStream dataStream, List<T> result, T[] values) throws IOException {
        int size = dataStream.readInt();
        for (int i = 0; i < size; i++) {
            result.add(values[dataStream.readInt()]);
        }
    }

    public static PasEntityScope retrieveScope(PascalStubElement el) {
        if (el instanceof PasEntityScope) {
            return (PasEntityScope) el;
        } else {
            // Parent stub is always present for non-scope stub based elements (IdentDecl) and it's PSI is always a scope
            StubElement stub = el.retrieveStub();
            return stub != null ? (PasEntityScope) stub.getParentStub().getPsi() : null;
        }
    }

    public static void serializeHelper(PascalHelperDeclStub stub, StubOutputStream dataStream) throws IOException {
        StubUtil.printStub("serializeHelper", stub);
        dataStream.writeName(stub.getName());
        dataStream.writeName(stub.getContainingUnitName());
        StubUtil.writeStringCollection(dataStream, stub.getParentNames());
        dataStream.writeName(stub.getTarget());
        StubUtil.writeStringCollection(dataStream, stub.getTypeParameters());
    }

    public static <Key, Psi extends PsiElement> boolean keyExists(@NotNull StubIndexKey<Key, Psi> indexKey, @NotNull Key key, Project project, GlobalSearchScope scope, Class<Psi> psiClass) {
        return !StubIndex.getElements(indexKey, key, project, scope, psiClass).isEmpty();
    }
}
