package com.siberika.idea.pascal.lang.psi.impl;

import com.intellij.psi.SmartPsiElementPointer;
import com.siberika.idea.pascal.lang.psi.PasEntityScope;
import com.siberika.idea.pascal.lang.psi.PasWithStatement;
import com.siberika.idea.pascal.lang.psi.PascalNamedElement;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class PasVariantScope extends PasScopeImpl {

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

    @Override
    public void invalidateCaches() {
    }

    @NotNull
    @Override
    public Collection<PasWithStatement> getWithStatements() {
        return Collections.emptyList();
    }

}
