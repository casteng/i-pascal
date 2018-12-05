package com.siberika.idea.pascal.lang.psi.impl;

import com.intellij.extapi.psi.ASTWrapperPsiElement;
import com.intellij.lang.ASTNode;
import com.intellij.navigation.ItemPresentation;
import com.intellij.openapi.roots.FileIndexFacade;
import com.intellij.openapi.util.TextRange;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import com.intellij.psi.search.LocalSearchScope;
import com.intellij.psi.search.ProjectScopeImpl;
import com.intellij.psi.search.SearchScope;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.IncorrectOperationException;
import com.siberika.idea.pascal.ide.actions.SectionToggle;
import com.siberika.idea.pascal.lang.PascalReference;
import com.siberika.idea.pascal.lang.parser.PascalParserUtil;
import com.siberika.idea.pascal.lang.psi.PasFormalParameter;
import com.siberika.idea.pascal.lang.psi.PasNamedIdent;
import com.siberika.idea.pascal.lang.psi.PasNamespaceIdent;
import com.siberika.idea.pascal.lang.psi.PasOperatorSubIdent;
import com.siberika.idea.pascal.lang.psi.PasRefNamedIdent;
import com.siberika.idea.pascal.lang.psi.PasRoutineImplDecl;
import com.siberika.idea.pascal.lang.psi.PasSubIdent;
import com.siberika.idea.pascal.lang.psi.PasTypes;
import com.siberika.idea.pascal.lang.psi.PascalInlineDeclaration;
import com.siberika.idea.pascal.lang.psi.PascalNamedElement;
import com.siberika.idea.pascal.lang.psi.PascalQualifiedIdent;
import com.siberika.idea.pascal.lang.psi.PascalRoutine;
import com.siberika.idea.pascal.util.PsiUtil;
import com.siberika.idea.pascal.util.StrUtil;
import com.siberika.idea.pascal.util.SyncUtil;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Iterator;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Author: George Bakhtadze
 * Date: 1/4/13
 */
public abstract class PascalNamedElementImpl extends ASTWrapperPsiElement implements PascalNamedElement {
    private volatile String myCachedName;
    private ReentrantLock nameLock = new ReentrantLock();
    private volatile PasField.FieldType myCachedType;
    private ReentrantLock typeLock = new ReentrantLock();
    private volatile PsiElement myCachedNameEl;
    private Boolean local;
    private long modificationStamp;
    private ReentrantLock localityLock = new ReentrantLock();

    public PascalNamedElementImpl(ASTNode node) {
        super(node);
    }

    @Override
    public void subtreeChanged() {
        super.subtreeChanged();
        if (SyncUtil.lockOrCancel(nameLock)) {
            myCachedName = null;
            myCachedNameEl = null;
            nameLock.unlock();
        }
        if (SyncUtil.lockOrCancel(typeLock)) {
            myCachedType = null;
            typeLock.unlock();
        }
        if (SyncUtil.lockOrCancel(localityLock)) {
            local = null;
            localityLock.unlock();
        }
    }

    @NotNull
    @Override
    public String getName() {
        if (this instanceof PasOperatorSubIdent) {
            myCachedName = getText();
        } else {
            SyncUtil.doWithLock(nameLock, () -> {
                if (StringUtil.isEmpty(myCachedName)) {
                    myCachedName = calcName(getNameElement());
                }
            });
        }
        return myCachedName;
    }

    static String calcName(PsiElement nameElement) {
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

    @Override
    public String getNamespace() {
        return StrUtil.getNamespace(getName());
    }

    @Override
    public String getNamePart() {
        return StrUtil.getNamePart(getName());
    }

    @NotNull
    @Override
    public PasField.FieldType getType() {
        SyncUtil.doWithLock(typeLock, () -> {
            if (null == myCachedType) {
                myCachedType = PsiUtil.getFieldType(this);
            }
        });
        return myCachedType;
    }

    @Override
    public boolean isExported() {
        return false;
    }

    @Override
    public boolean isLocal() {
        if (SyncUtil.lockOrCancel(localityLock)) {
            try {
                long modStamp = PsiUtil.getModificationStamp(this);
                if ((null == local) || (modificationStamp != modStamp)) {
                    modificationStamp = modStamp;
                    local = false;
                    if (this instanceof PascalRoutineImpl) {
                        if (this instanceof PasRoutineImplDecl) {
                            PsiElement decl = SectionToggle.retrieveDeclaration((PascalRoutine) this, true);
                            if (decl instanceof PascalRoutine) {
                                local = ((PascalRoutine) decl).isLocal();
                            } else {
                                local = true;
                            }
                        } else {
                            local = true;
                        }
                    } else {
                        PsiElement parent = getParent();
                        if ((parent instanceof PasFormalParameter) || (parent instanceof PascalInlineDeclaration)) {
                            local = true;
                        } else if (parent instanceof PascalNamedElement) {
                            local = ((PascalNamedElement) parent).isLocal();
                        }
                    }
                }
            } finally {
                localityLock.unlock();
            }
        }
        return local;
    }

    @Nullable
    private PsiElement getNameElement() {
        if (this instanceof PasOperatorSubIdent) {
            myCachedNameEl = this;
        } else {
            SyncUtil.doWithLock(nameLock, () -> {
                if (!PsiUtil.isElementUsable(myCachedNameEl)) {
                    calcNameElement();
                }
            });
        }
        return myCachedNameEl;
    }

    private void calcNameElement() {
        if ((this instanceof PasNamespaceIdent) || (this instanceof PascalQualifiedIdent)) {
            myCachedNameEl = this;
            return;
        }
        PsiElement result = findChildByType(PasTypes.NAMESPACE_IDENT);
        if (null == result) {
            PascalNamedElement namedChild = PsiTreeUtil.getChildOfType(this, PascalNamedElement.class);
            result = namedChild != null ? namedChild.getNameIdentifier() : null;
        }
        if (null == result) {
            result = findChildByType(NAME_TYPE_SET);
        }
        myCachedNameEl = result;
    }

    @Override
    public PsiElement setName(@NonNls @NotNull String s) throws IncorrectOperationException {
        PsiElement element = getNameElement();
        if (element != null) {
            PsiElement el = PasElementFactory.createReplacementElement(element, s);
            if (el != null) {
                element.replace(el);
            }
        }
        return this;
    }

    @Override
    public int getTextOffset() {
        PsiElement element = getNameElement();
        return (element != null) && (element != this) ? element.getTextOffset() : getNode().getStartOffset();
    }

    @NotNull
    @Override
    public SearchScope getUseScope() {
        if (isLocal()) {
            return new LocalSearchScope(this.getContainingFile());
        } else {
            return new ProjectScopeImpl(getProject(), FileIndexFacade.getInstance(getProject()));
        }
    }

    @Override
    public PsiElement getNameIdentifier() {
        return getNameElement();
    }

    @Override
    public ItemPresentation getPresentation() {
        return PascalParserUtil.getPresentation(this);
    }

    @Override
    public PsiReference getReference() {
        PsiReference[] refs = getReferences();
        return refs.length > 0 ? refs[0] : null;
    }

    @Override
    @NotNull
    public PsiReference[] getReferences() {
        if ((this instanceof PasSubIdent) || (this instanceof PasRefNamedIdent)) {
            if ((getNameElement() != null) && getTextRange().intersects(getNameElement().getTextRange())) {
                return new PsiReference[]{
                        new PascalReference(this, new TextRange(0, getName().length()))
                };
            }
        } else if (this instanceof PasNamedIdent && this.getParent() instanceof PasRoutineImplDecl) {
            return new PsiReference[]{
                    new PascalReference(this, new TextRange(0, getName().length()))
            };
        }
        return PsiReference.EMPTY_ARRAY;
    }

}
