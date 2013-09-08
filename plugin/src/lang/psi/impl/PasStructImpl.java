package com.siberika.idea.pascal.lang.psi.impl;

import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import com.siberika.idea.pascal.lang.psi.PasTypeDeclaration;
import com.siberika.idea.pascal.util.PsiUtil;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Author: George Bakhtadze
 * Date: 07/09/2013
 */
public class PasStructImpl extends PascalNamedElementImpl {

    enum Visibility {STRICT_PRIVATE, PRIVATE, PROTECTED, PUBLIC, PUBLISHED}

    private Map<String, PasField> privateMembers = new LinkedHashMap<String, PasField>();
    private Map<String, PasField> protectedMembers = new LinkedHashMap<String, PasField>();
    private Map<String, PasField> publicMembers = new LinkedHashMap<String, PasField>();

    public PasStructImpl(ASTNode node) {
        super(node);
    }

    @Nullable
    @SuppressWarnings("unchecked")
    public static PasStructImpl findOwner(PascalRoutineImpl element) {
        return PsiTreeUtil.getParentOfType(element,
                PasClassHelperDeclImpl.class, PasClassTypeDeclImpl.class, PasInterfaceTypeDeclImpl.class, PasObjectDeclImpl.class, PasRecordHelperDeclImpl.class, PasRecordDeclImpl.class);
    }

    @Override
    @Nullable
    @SuppressWarnings("unchecked")
    protected PsiElement getNameElement() {
        PasTypeDeclaration typeDecl = PsiTreeUtil.getParentOfType(this, PasTypeDeclaration.class);
        return PsiUtil.findImmChildOfAnyType(typeDecl, PasGenericTypeIdentImpl.class);
    }

    private class PasField {
        public String name;
        public PasStructImpl owner;
        public Visibility visibility;
        public PsiElement element;
    }
}
