package com.siberika.idea.pascal.lang;

import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementResolveResult;
import com.intellij.psi.PsiReferenceBase;
import com.intellij.psi.ResolveResult;
import com.siberika.idea.pascal.PascalIcons;
import com.siberika.idea.pascal.lang.parser.PascalParserUtil;
import com.siberika.idea.pascal.lang.psi.PascalNamedElement;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * Date: 3/13/13
 * Author: George Bakhtadze
 */
public class PascalReference extends PsiReferenceBase<PascalNamedElement> {
    private String key;

    public PascalReference(@NotNull PsiElement element, TextRange textRange) {
        super((PascalNamedElement) element, textRange);
/*        if (element instanceof PascalNamedElement) {
            key = ((PascalNamedElement) element).getName();
        } else {*/
            key = element.getText().substring(textRange.getStartOffset(), textRange.getEndOffset());
//        }
    }

    @NotNull
    //@Override
    public ResolveResult[] multiResolve(boolean incompleteCode) {
        Project project = myElement.getProject();
        final List<PascalNamedElement> references = PascalParserUtil.findAllReferences(myElement, key);
        return PsiElementResolveResult.createResults(references);
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
        List<PascalNamedElement> properties = PascalParserUtil.findTypes(project);
        List<LookupElement> variants = new ArrayList<LookupElement>();
        for (final PascalNamedElement property : properties) {
            if (property.getName().length() > 0) {
                variants.add(LookupElementBuilder.create(property).
                        withIcon(PascalIcons.GENERAL).
                        withTypeText(property.getContainingFile().getName())
                );
            }
        }
        return variants.toArray();
    }
}
