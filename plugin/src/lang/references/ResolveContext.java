package com.siberika.idea.pascal.lang.references;

import com.intellij.psi.PsiElement;
import com.siberika.idea.pascal.lang.psi.PasEntityScope;
import com.siberika.idea.pascal.lang.psi.impl.PasField;
import com.siberika.idea.pascal.lang.references.resolve.ResolveOptions;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;

public class ResolveContext {
    public PasEntityScope scope;
    public Set<PasField.FieldType> fieldTypes;
    public final boolean includeLibrary;
    final List<PsiElement> resultScope;
    public boolean disableParentNamespaces;
    boolean stubsOnly = false;
    public List<String> unitNamespaces;
    public Set<ResolveOptions> options = EnumSet.noneOf(ResolveOptions.class);

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

    public boolean isFirstPart() {
        return options.contains(ResolveOptions.FIRST_PART);
    }
}
