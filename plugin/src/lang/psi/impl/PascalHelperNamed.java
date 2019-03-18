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
import com.siberika.idea.pascal.lang.psi.field.Flag;
import com.siberika.idea.pascal.util.PsiUtil;
import org.jetbrains.annotations.Nullable;

import java.util.Iterator;

public class PascalHelperNamed {
    protected final PascalNamedElement self;
    volatile private long modified;
    volatile private String cachedName;
    volatile private PsiElement cachedNameEl;
    volatile private PasField.FieldType cachedType;
    volatile private long flags;

    public PascalHelperNamed(PascalNamedElement self) {
        this.self = self;
    }

    void invalidateCache(boolean subtreeChanged) {
        flags = 0;
        modified = self.getContainingFile().getModificationStamp();
        // TODO: don't invalidate on each file modification
        cachedName = null;
        cachedNameEl = null;
        self.invalidateCache(subtreeChanged);
    }

    void ensureCacheActual() {
        if (modified != self.getContainingFile().getModificationStamp()) {
            invalidateCache(false);
        }
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
                name = !name.startsWith("&") ? name : name.replaceFirst("$&+", "");
                sb.append(".").append(name);
            }
            return sb.toString();
        } else {
            if (nameElement != null) {
                String name = nameElement.getText();
                return !name.startsWith("&") ? name : name.replaceFirst("$&+", "");
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

    public long getFlags() {
        return flags;
    }

    public boolean isFlagInit(Flag flag) {
        return (flags & (0x100000000L << flag.ordinal())) != 0;
    }

    public boolean isFlagSet(Flag flag) {
        return (flags & (1L << flag.ordinal())) != 0;
    }

    public void setFlag(Flag flag, boolean value) {
        //TODO: make atomic
        long f = flags | (0x1_00000001L << flag.ordinal());         // set flag and its initialization flag
        flags = f & (~((value ? 0L : 1L) << flag.ordinal()));                     // clear flag if it should be false

    }

    public boolean isLocalInit() {
        return isFlagInit(Flag.LOCAL);
    }

    public boolean isLocal() {
        return isFlagSet(Flag.LOCAL);
    }

    public void setLocal(final boolean value) {
        setFlag(Flag.LOCAL, value);
    }

    public boolean isDefaultPropertyInit() {
        return isFlagInit(Flag.DEFAULT_PROPERTY);
    }

    public boolean isDefaultProperty() {
        return isFlagSet(Flag.DEFAULT_PROPERTY);
    }

    public void setDefaultProperty(final boolean value) {
        setFlag(Flag.DEFAULT_PROPERTY, value);
    }
}
