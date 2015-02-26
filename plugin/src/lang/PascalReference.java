package com.siberika.idea.pascal.lang;

import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementResolveResult;
import com.intellij.psi.PsiPolyVariantReferenceBase;
import com.intellij.psi.ResolveResult;
import com.intellij.psi.impl.source.resolve.ResolveCache;
import com.siberika.idea.pascal.lang.parser.NamespaceRec;
import com.siberika.idea.pascal.lang.psi.PascalNamedElement;
import com.siberika.idea.pascal.lang.psi.impl.PasField;
import com.siberika.idea.pascal.lang.references.PasReferenceUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

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
        Project project = myElement.getProject();
        List<LookupElement> variants = new ArrayList<LookupElement>();

        /*List<PascalNamedElement> properties = PascalParserUtil.findTypes(project);
        for (final PascalNamedElement property : properties) {
            if (property.getName().length() > 0) {
                variants.add(LookupElementBuilder.create(property).
                        withIcon(PascalIcons.GENERAL).
                        withTypeText(property.getContainingFile().getName())
                );
            }
        }*/
        return variants.toArray();
    }

    private static class Resolver implements ResolveCache.PolyVariantResolver<PascalReference> {
        public static final Resolver INSTANCE = new Resolver();

        @NotNull
        @Override
        public ResolveResult[] resolve(@NotNull PascalReference pascalReference, boolean incompleteCode) {
            //final Collection<PascalNamedElement> references = PascalParserUtil.findAllReferences(pascalReference.getElement(), pascalReference.key);
            final Collection<PsiElement> references = PasReferenceUtil.resolve(NamespaceRec.fromElement(pascalReference.getElement()), PasField.TYPES_ALL);
            // return only first reference
            for (PsiElement el : references) {
                return PsiElementResolveResult.createResults(Arrays.asList(el));
            }
            return PsiElementResolveResult.createResults(references);
        }
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
