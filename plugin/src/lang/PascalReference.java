package com.siberika.idea.pascal.lang;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementResolveResult;
import com.intellij.psi.PsiNameIdentifierOwner;
import com.intellij.psi.PsiPolyVariantReferenceBase;
import com.intellij.psi.ResolveResult;
import com.intellij.psi.impl.source.resolve.ResolveCache;
import com.intellij.util.ArrayUtil;
import com.siberika.idea.pascal.ide.actions.SectionToggle;
import com.siberika.idea.pascal.lang.parser.NamespaceRec;
import com.siberika.idea.pascal.lang.psi.PascalNamedElement;
import com.siberika.idea.pascal.lang.psi.impl.PasField;
import com.siberika.idea.pascal.lang.psi.impl.PasRoutineImplDeclImpl;
import com.siberika.idea.pascal.lang.references.PasReferenceUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;

/**
 * Date: 3/13/13
 * Author: George Bakhtadze
 */
public class PascalReference extends PsiPolyVariantReferenceBase<PascalNamedElement> {
        //PsiReferenceBase<PascalNamedElement> {
    private static final Logger LOG = Logger.getInstance(PascalReference.class);
    private final String key;

    public PascalReference(@NotNull PsiElement element, TextRange textRange) {
        super((PascalNamedElement) element, textRange);
        key = element.getText().substring(textRange.getStartOffset(), textRange.getEndOffset());
    }

    @NotNull
    @Override
    public ResolveResult[] multiResolve(boolean incompleteCode) {
        final ResolveCache resolveCache = ResolveCache.getInstance(myElement.getProject());
        return resolveCache.resolveWithCaching(this, Resolver.INSTANCE, true, incompleteCode, myElement.getContainingFile());
//        return Resolver.INSTANCE.resolve(this, incompleteCode);
    }

    @Nullable
    @Override
    public PsiElement resolve() {
        ResolveResult[] resolveResults = multiResolve(false);
        return resolveResults.length > 0 ? resolveResults[0].getElement() : null;
    }

    @NotNull
    @Override
    public Object[] getVariants() {
        return ArrayUtil.EMPTY_OBJECT_ARRAY;
    }

    private static class Resolver implements ResolveCache.PolyVariantResolver<PascalReference> {
        public static final Resolver INSTANCE = new Resolver();

        @NotNull
        @Override
        public ResolveResult[] resolve(@NotNull PascalReference pascalReference, boolean incompleteCode) {
            return doResolve(pascalReference.getElement(), pascalReference.key, incompleteCode);
        }

        static ResolveResult[] doResolve(@NotNull PascalNamedElement named, @NotNull String key, boolean incompleteCode) {
            if (PasField.DUMMY_IDENTIFIER.equals(key)) {
                return ResolveResult.EMPTY_ARRAY;
            }

            if (named.getParent() instanceof PasRoutineImplDeclImpl) {
                return resolveRoutineImpl((PasRoutineImplDeclImpl) named.getParent());
            }

            final Collection<PasField> references = PasReferenceUtil.resolveExpr(null, NamespaceRec.fromElement(named), PasField.TYPES_ALL, true, 0);
            // return only first reference
            for (PasField el : references) {
                if (el != null) {
                    PsiElement element = el.target != null ? el.target : el.getElement();
                    if (element != null) {
                        return createResults(element);
                    }
                } else {
                    LOG.info("ERROR: null resolved for " + named);
                }
            }
            return ResolveResult.EMPTY_ARRAY;
        }

        private static ResolveResult[] resolveRoutineImpl(PasRoutineImplDeclImpl routine) {
            PsiElement decl = SectionToggle.retrieveDeclaration(routine, true);
            if (decl != null) {
                return createResults(decl);
            } else {
                decl = SectionToggle.getRoutineForwardDeclaration(routine);
                return decl != null ? createResults(decl) : ResolveResult.EMPTY_ARRAY;
            }
        }

        private static ResolveResult[] createResults(PsiElement element) {
            return PsiElementResolveResult.createResults(new PsiElement[]{element});
        }

    }

    @Override
    public boolean isReferenceTo(PsiElement element) {
        final ResolveResult[] results = multiResolve(false);
        for (ResolveResult result : results) {
            if (getElement().getManager().areElementsEquivalent(getNamedElement(result.getElement()), getNamedElement(element))) {
                return true;
            }
        }
        return false;
    }

    private PsiElement getNamedElement(PsiElement element) {
        if (element instanceof PsiNameIdentifierOwner) {
            return ((PsiNameIdentifierOwner) element).getNameIdentifier();
        }
        return element;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        PascalReference that = (PascalReference) o;

        if (!getElement().equals(that.getElement())) return false;
        if (!key.equals(that.key)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = key.hashCode();
        result = 31 * result + getElement().hashCode();
        return result;
    }
}
