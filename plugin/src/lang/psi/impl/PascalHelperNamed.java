package com.siberika.idea.pascal.lang.psi.impl;

import com.intellij.lang.ASTNode;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiElement;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.tree.TokenSet;
import com.intellij.psi.util.PsiTreeUtil;
import com.siberika.idea.pascal.lang.psi.PasNamespaceIdent;
import com.siberika.idea.pascal.lang.psi.PasOperatorSubIdent;
import com.siberika.idea.pascal.lang.psi.PasSubIdent;
import com.siberika.idea.pascal.lang.psi.PasTypes;
import com.siberika.idea.pascal.lang.psi.PascalNamedElement;
import com.siberika.idea.pascal.lang.psi.PascalQualifiedIdent;
import com.siberika.idea.pascal.util.PsiUtil;
import org.jetbrains.annotations.Nullable;

import java.util.Iterator;

public class PascalHelperNamed {
    protected final PascalNamedElement self;
    volatile private long modified;
    volatile private String cachedName;
    volatile private PsiElement cachedNameEl;
    volatile private PasField.FieldType cachedType;
    volatile Boolean local;

    //TODO: remove
    volatile String cachedUniqueName;
    volatile String containingUnitName;

    public PascalHelperNamed(PascalNamedElement self) {
        this.self = self;
    }

    void invalidateCaches() {
        local = null;
        cachedUniqueName = null;
        containingUnitName = null;
        modified = self.getContainingFile().getModificationStamp();
        // TODO: don't invalidate on each file modification
        cachedName = null;
        cachedNameEl = null;
    }

    void ensureCacheActual() {
        if (modified != self.getContainingFile().getModificationStamp()) {
            invalidateCaches();
        }
    }

    boolean isCacheActual() {
        return (modified == self.getContainingFile().getModificationStamp());
    }

    public String getName() {
        if (self instanceof PasOperatorSubIdent) {
            cachedName = self.getText();
        } else {
            if (StringUtil.isEmpty(cachedName)) {
                cachedName = calcName(self.getNameIdentifier());
            }
        }
        return cachedName;
    }

    private static String calcName(PsiElement nameElement) {
        if ((nameElement instanceof PascalQualifiedIdent)) {
            Iterator<PasSubIdent> it = ((PascalQualifiedIdent) nameElement).getSubIdentList().iterator();
            StringBuilder sb = new StringBuilder(it.next().getName());
            while (it.hasNext()) {
                String name = it.next().getName();
                name = !name.startsWith("&") ? name : name.substring(1);
                sb.append(".").append(name);
            }
            return sb.toString();
        } else {
            if (nameElement != null) {
                String name = nameElement.getText();
                return !name.startsWith("&") ? name : name.substring(1);
            } else {
                return "";
            }
        }
    }

    protected PsiElement calcNameElement() {
        if ((self instanceof PasNamespaceIdent) || (self instanceof PascalQualifiedIdent) || self instanceof PasOperatorSubIdent) {
            cachedNameEl = self;
        } else {
            PsiElement result = findChildByType(PasTypes.NAMESPACE_IDENT);
            if (null == result) {
                PascalNamedElement namedChild = PsiTreeUtil.getChildOfType(self, PascalNamedElement.class);
                result = namedChild != null ? namedChild.getNameIdentifier() : null;
            }
            if (null == result) {
                result = findChildByType(PascalNamedElement.NAME_TYPE_SET);
            }
            cachedNameEl = result;
        }
        return cachedNameEl;
    }

    @Nullable
    private <T extends PsiElement> T findChildByType(IElementType type) {
        ASTNode node = self.getNode().findChildByType(type);
        return node == null ? null : (T) node.getPsi();
    }

    @Nullable
    private <T extends PsiElement> T findChildByType(TokenSet type) {
        ASTNode node = self.getNode().findChildByType(type);
        return node == null ? null : (T) node.getPsi();
    }

    PasField.FieldType calcType() {
        ensureCacheActual();
        if (null == cachedType) {
            cachedType = PsiUtil.getFieldType(self);
        }
        return cachedType;
    }
}
