package com.siberika.idea.pascal.lang.psi.impl;

import com.intellij.extapi.psi.ASTWrapperPsiElement;
import com.intellij.lang.ASTNode;
import com.intellij.navigation.ItemPresentation;
import com.intellij.openapi.roots.FileIndexFacade;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import com.intellij.psi.search.LocalSearchScope;
import com.intellij.psi.search.ProjectScopeImpl;
import com.intellij.psi.search.SearchScope;
import com.intellij.util.IncorrectOperationException;
import com.siberika.idea.pascal.ide.actions.SectionToggle;
import com.siberika.idea.pascal.lang.PascalReference;
import com.siberika.idea.pascal.lang.parser.PascalParserUtil;
import com.siberika.idea.pascal.lang.psi.PasFormalParameter;
import com.siberika.idea.pascal.lang.psi.PasGenericTypeIdent;
import com.siberika.idea.pascal.lang.psi.PasNamedIdent;
import com.siberika.idea.pascal.lang.psi.PasNamespaceIdent;
import com.siberika.idea.pascal.lang.psi.PasRefNamedIdent;
import com.siberika.idea.pascal.lang.psi.PasRoutineImplDecl;
import com.siberika.idea.pascal.lang.psi.PasSubIdent;
import com.siberika.idea.pascal.lang.psi.PasUsesClause;
import com.siberika.idea.pascal.lang.psi.PascalInlineDeclaration;
import com.siberika.idea.pascal.lang.psi.PascalNamedElement;
import com.siberika.idea.pascal.lang.psi.PascalRoutine;
import com.siberika.idea.pascal.util.StrUtil;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Author: George Bakhtadze
 * Date: 1/4/13
 */
public abstract class PascalNamedElementImpl extends ASTWrapperPsiElement implements PascalNamedElement {

    protected PascalHelperNamed helper = createHelper();

    public PascalNamedElementImpl(ASTNode node) {
        super(node);
    }

    protected PascalHelperNamed createHelper() {
        return new PascalHelperNamed(this);
    }

    @Override
    public void subtreeChanged() {
        super.subtreeChanged();
        helper.invalidateCache(true);
    }

    @Override
    public ItemPresentation getPresentation() {
        return PascalParserUtil.getPresentation(this);
    }

    @NotNull
    @Override
    public String getName() {
        return helper.getName();
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
        return helper.calcType();
    }

    @Override
    public boolean isExported() {
        if (this instanceof PasGenericTypeIdent) {
            return ((PasGenericTypeIdent) this).getNamedIdentDecl().isExported();
        }
        PsiElement parent = getParent();
        if (parent instanceof PascalNamedElement) {
            return ((PascalNamedElement) parent).isLocal();
        } else {
            return false;
        }
    }

    @Override
    public boolean isLocal() {
        helper.ensureCacheActual();
        if (!helper.isLocalInit()) {
            boolean tempLocal = false;
            if (this instanceof PascalRoutineImpl) {
                if (this instanceof PasRoutineImplDecl) {
                    PsiElement decl = SectionToggle.retrieveDeclaration((PascalRoutine) this, true);
                    if (decl instanceof PascalRoutine) {
                        tempLocal = ((PascalRoutine) decl).isLocal();
                    } else {
                        tempLocal = true;
                    }
                } else {
                    tempLocal = true;
                }
            } else {
                PsiElement parent = getParent();
                if ((parent instanceof PasFormalParameter) || (parent instanceof PascalInlineDeclaration)) {
                    tempLocal = true;
                } else if (parent instanceof PascalNamedElement) {
                    tempLocal = ((PascalNamedElement) parent).isLocal();
                }
            }
            helper.setLocal(tempLocal);
        }
        return helper.isLocal();
    }

    @Override
    public int getTextOffset() {
        PsiElement element = getNameIdentifier();
        return (element != null) && (element != this) ? element.getTextOffset() : getNode().getStartOffset();
    }

    @NotNull
    @Override
    public SearchScope getUseScope() {
        if (!isExported()) {
            return new LocalSearchScope(this.getContainingFile());
        } else {
            return new ProjectScopeImpl(getProject(), FileIndexFacade.getInstance(getProject()));
        }
    }

    @Override
    public PsiElement getNameIdentifier() {
        return helper.calcNameElement();
    }

    @Override
    public PsiReference getReference() {
        PsiReference[] refs = getReferences();
        return refs.length > 0 ? refs[0] : null;
    }

    @Override
    @NotNull
    public PsiReference[] getReferences() {
        if (this instanceof PasSubIdent) {
            PsiElement parent = getParent();
            if (parent instanceof PasNamespaceIdent && parent.getParent() instanceof PasUsesClause) {
                return new PsiReference[]{
                        new PascalReference(parent, new TextRange(0, ((PasNamespaceIdent) parent).getName().length()))
                };
            }
        }
        if ((this instanceof PasSubIdent) || (this instanceof PasRefNamedIdent)) {
            PsiElement nameEl = getNameIdentifier();
            if ((nameEl != null) && getTextRange().intersects(nameEl.getTextRange())) {
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

    @Override
    public PsiElement setName(@NonNls @NotNull String s) throws IncorrectOperationException {
        PsiElement element = getNameIdentifier();
        if (element != null) {
            PsiElement el = PasElementFactory.createReplacementElement(element, s);
            if (el != null) {
                element.replace(el);
            }
        }
        return this;
    }

    @Override
    public @Nullable PsiElement getPasName() {
        return getNameIdentifier();
    }
}
