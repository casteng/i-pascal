package com.siberika.idea.pascal.lang.references;

import com.intellij.psi.PsiElement;
import com.siberika.idea.pascal.lang.psi.PasEntityScope;
import com.siberika.idea.pascal.lang.psi.impl.PasField;

import java.util.List;
import java.util.Set;

public class ResolveContext {
    PasEntityScope scope;
    Set<PasField.FieldType> fieldTypes;
    final boolean includeLibrary;
    final List<PsiElement> resultScope;
    public boolean disableParentNamespaces;
    boolean stubsOnly = false;
    public List<String> unitNamespaces;

    public ResolveContext(PasEntityScope scope, Set<PasField.FieldType> fieldTypes, boolean includeLibrary, List<PsiElement> resultScope, List<String> unitPrefixes) {
        this.scope = scope;
        this.fieldTypes = fieldTypes;
        this.includeLibrary = includeLibrary;
        this.resultScope = resultScope;
        this.unitNamespaces = unitPrefixes;
    }

    public ResolveContext(Set<PasField.FieldType> fieldTypes, boolean includeLibrary) {
        this(null, fieldTypes, includeLibrary, null, null);
    }
}
