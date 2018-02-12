package com.siberika.idea.pascal.lang.psi.impl;

import com.intellij.lang.ASTNode;
import com.intellij.psi.stubs.IStubElementType;
import com.siberika.idea.pascal.lang.psi.PasClassField;
import com.siberika.idea.pascal.lang.psi.PasClassProperty;
import com.siberika.idea.pascal.lang.psi.PasConstSection;
import com.siberika.idea.pascal.lang.psi.PasExportedRoutine;
import com.siberika.idea.pascal.lang.psi.PasTypeSection;
import com.siberika.idea.pascal.lang.psi.PasVarSection;
import com.siberika.idea.pascal.lang.psi.PasVisibility;
import com.siberika.idea.pascal.lang.psi.PascalClassDecl;
import com.siberika.idea.pascal.lang.stub.struct.PasClassDeclStub;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;

public class PascalClassDeclImpl extends PasStubStructTypeImpl<PascalClassDecl, PasClassDeclStub> implements PascalClassDecl {

    public PascalClassDeclImpl(ASTNode node) {
        super(node);
    }

    public PascalClassDeclImpl(PasClassDeclStub stub, IStubElementType nodeType) {
        super(stub, nodeType);
    }

    @NotNull
    @Override
    public List<PasVisibility> getVisibilityList() {
        return Collections.emptyList();
    }

    @NotNull
    @Override
    public List<PasClassField> getClassFieldList() {
        return Collections.emptyList();
    }

    @NotNull
    @Override
    public List<PasClassProperty> getClassPropertyList() {
        return Collections.emptyList();
    }

    @NotNull
    @Override
    public List<PasVarSection> getVarSectionList() {
        return Collections.emptyList();
    }

    @NotNull
    @Override
    public List<PasConstSection> getConstSectionList() {
        return Collections.emptyList();
    }

    @NotNull
    @Override
    public List<PasTypeSection> getTypeSectionList() {
        return Collections.emptyList();
    }

    @NotNull
    @Override
    public List<PasExportedRoutine> getExportedRoutineList() {
        return Collections.emptyList();
    }
}
