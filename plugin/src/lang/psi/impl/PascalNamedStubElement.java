package com.siberika.idea.pascal.lang.psi.impl;

import com.intellij.extapi.psi.StubBasedPsiElementBase;
import com.intellij.lang.ASTNode;
import com.intellij.navigation.ItemPresentation;
import com.intellij.psi.PsiElement;
import com.intellij.psi.stubs.IStubElementType;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.IncorrectOperationException;
import com.siberika.idea.pascal.lang.parser.PascalParserUtil;
import com.siberika.idea.pascal.lang.psi.PasNamespaceIdent;
import com.siberika.idea.pascal.lang.psi.PasTypes;
import com.siberika.idea.pascal.lang.psi.PascalNamedElement;
import com.siberika.idea.pascal.lang.psi.PascalQualifiedIdent;
import com.siberika.idea.pascal.lang.psi.PascalStubElement;
import com.siberika.idea.pascal.lang.stub.PasNamedStub;
import com.siberika.idea.pascal.util.SyncUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.locks.ReentrantLock;

public abstract class PascalNamedStubElement<B extends PasNamedStub> extends StubBasedPsiElementBase<B> implements PascalStubElement<B>, PascalNamedElement {

    public PascalNamedStubElement(ASTNode node) {
        super(node);
    }

    public PascalNamedStubElement(B stub, IStubElementType nodeType) {
        super(stub, nodeType);
    }

    private String myCachedUniqueName;
    private String myCachedName;
    private ReentrantLock nameLock = new ReentrantLock();

    @Override
    public ItemPresentation getPresentation() {
        return PascalParserUtil.getPresentation(this);
    }

    @Override
    public void subtreeChanged() {
        super.subtreeChanged();
        if (SyncUtil.lockOrCancel(nameLock)) {
            myCachedName = null;
            myCachedUniqueName = null;
            nameLock.unlock();
        }
    }

    @NotNull
    @Override
    public String getName() {
        B stub = retrieveStub();
        if (stub != null) {
            return stub.getName();
        }
        if (SyncUtil.lockOrCancel(nameLock)) {
            try {
                if ((myCachedName == null) || (myCachedName.length() == 0)) {
                    myCachedName = PascalNamedElementImpl.calcName(getNameElement());
                }
            } finally {
                nameLock.unlock();
            }
        }
        return myCachedName;
    }

    @Override
    public String getNamespace() {
        String name = getName();
        int pos = name.lastIndexOf(".");
        return pos >= 0 ? name.substring(0, pos) : null;
    }

    @Override
    public String getNamePart() {
        String name = getName();
        int pos = name.lastIndexOf(".");
        return pos >= 0 ? name.substring(pos + 1) : name;
    }

    // Name qualified with container names
    public String getUniqueName() {
        B stub = retrieveStub();
        if (stub != null) {
            return stub.getUniqueName();
        }
        if (SyncUtil.lockOrCancel(nameLock)) {
            try {
                if ((myCachedUniqueName == null) || (myCachedUniqueName.length() == 0)) {
                    myCachedUniqueName = calcUniqueName();
                }
            } finally {
                nameLock.unlock();
            }
        }
        return myCachedUniqueName;
    }

    protected abstract String calcUniqueName();

    @Nullable
    @Override
    public PsiElement getNameIdentifier() {
        return getNameElement();
    }

    @Override
    public PsiElement setName(@NotNull String name) throws IncorrectOperationException {
        PsiElement element = getNameElement();
        if (element != null) {
            element.replace(PasElementFactory.createLeafFromText(getProject(), name));
        }
        return this;
    }

    @Nullable
    protected PsiElement getNameElement() {
        if ((this instanceof PasNamespaceIdent) || (this instanceof PascalQualifiedIdent)) {
            return this;
        }
        PsiElement result = findChildByType(PasTypes.NAMESPACE_IDENT);
        if (null == result) {
            PascalNamedElement namedChild = PsiTreeUtil.getChildOfType(this, PascalNamedElement.class);
            result = namedChild != null ? namedChild.getNameIdentifier() : null;
        }
        if (null == result) {
            result = findChildByType(NAME_TYPE_SET);
        }
        return result;
    }

}
