package com.siberika.idea.pascal.lang.psi.impl;

import com.intellij.psi.SmartPsiElementPointer;
import com.siberika.idea.pascal.lang.psi.PasEntityScope;
import com.siberika.idea.pascal.lang.psi.PasWithStatement;
import com.siberika.idea.pascal.lang.psi.PascalNamedElement;
import com.siberika.idea.pascal.lang.psi.PascalRoutine;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

public final class PasVariantScope extends PasScopeImpl {

    private final PascalNamedElement element;

    public PasVariantScope(@NotNull PascalNamedElement element) {
        super(element.getNode());
        this.element = element;
    }

    @Nullable
    @Override
    public PasField getField(String name) {
        return new PasField(null, element, name, PasField.FieldType.VARIABLE, PasField.Visibility.PUBLIC, PasField.VARIANT);
    }

    @Nullable
    @Override
    public PascalRoutine getRoutine(String reducedName) {
        return null;
    }

    @NotNull
    @Override
    public Collection<PasField> getAllFields() {
        return Collections.emptyList();
    }

    @NotNull
    @Override
    public List<SmartPsiElementPointer<PasEntityScope>> getParentScope() {
        return Collections.emptyList();
    }

    @Nullable
    @Override
    public PasEntityScope getContainingScope() {
        return null;
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
