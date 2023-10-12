package com.siberika.idea.pascal.lang.psi.impl;

import com.intellij.psi.SmartPsiElementPointer;
import com.siberika.idea.pascal.lang.psi.PasEntityScope;
import com.siberika.idea.pascal.lang.psi.PasGenericTypeIdent;
import com.siberika.idea.pascal.lang.psi.PasWithStatement;
import com.siberika.idea.pascal.lang.psi.PascalIdentDecl;
import com.siberika.idea.pascal.lang.psi.PascalNamedElement;
import com.siberika.idea.pascal.lang.psi.PascalRoutine;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

public final class PasEnumTypeScope extends PasScopeImpl {

    private final PasEntityScope owner;
    private final PascalIdentDecl element;

    public PasEnumTypeScope(@NotNull PasEntityScope owner, @NotNull PascalIdentDecl element) {
        super(element.getNode());
        this.owner = owner;
        this.element = element;
    }

    public static PasEnumTypeScope fromNamedElement(PasEntityScope owner, PascalNamedElement element) {
        if (null == owner) {
            return null;
        }
        PascalNamedElement el;
        if (element instanceof PasGenericTypeIdent) {
            el = ((PasGenericTypeIdent) element).getNamedIdentDecl();
        } else {
            el = element;
        }
        return el instanceof PascalIdentDecl ? new PasEnumTypeScope(owner, (PascalIdentDecl) el) : null;
    }

    @Nullable
    @Override
    public PasField getField(String name) {
        for (String subMember : element.getSubMembers()) {
            if (name.equalsIgnoreCase(subMember)) {
                return owner.getField(name);
            }
        }
        return null;
    }

    @Nullable
    @Override
    public PascalRoutine getRoutine(String reducedName) {
        return null;
    }

    @NotNull
    @Override
    public Collection<PasField> getAllFields() {
        return Collections.emptyList();  // TODO: implement via processor
    }

    @NotNull
    @Override
    public List<SmartPsiElementPointer<PasEntityScope>> getParentScope() {
        return Collections.emptyList();
    }

    @Nullable
    @Override
    public PasEntityScope getContainingScope() {
        return owner;
    }

    @NotNull
    @Override
    public Collection<PasWithStatement> getWithStatements() {
        return Collections.emptyList();
    }

    @Override
    public boolean isPhysical() {
        return false;
    }

}
