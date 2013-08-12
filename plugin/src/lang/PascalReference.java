package com.siberika.idea.pascal.lang;

import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementResolveResult;
import com.intellij.psi.PsiReferenceBase;
import com.intellij.psi.ResolveResult;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.tree.TokenSet;
import com.siberika.idea.pascal.PascalIcons;
import com.siberika.idea.pascal.lang.lexer.PascalLexer;
import com.siberika.idea.pascal.lang.parser.PascalParserUtil;
import com.siberika.idea.pascal.lang.psi.PasTypes;
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
    private final String key;

    public PascalReference(@NotNull PsiElement element, TextRange textRange) {
        super((PascalNamedElement) element, textRange);
        key = element.getText().substring(textRange.getStartOffset(), textRange.getEndOffset());
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
        List<LookupElement> variants = new ArrayList<LookupElement>();

        appendCommonVariants(variants);

        List<PascalNamedElement> properties = PascalParserUtil.findTypes(project);
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

    private void appendCommonVariants(List<LookupElement> variants) {
        appendTokenSet(variants, PascalLexer.OPERATORS);
        appendTokenSet(variants, PascalLexer.STATEMENTS);
        appendTokenSet(variants, PascalLexer.VALUES);
        appendTokenSet(variants, PascalLexer.TOP_LEVEL_DECLARATIONS);
        appendTokenSet(variants, PascalLexer.DECLARATIONS);
        appendTokenSet(variants, PascalLexer.DIRECTIVE);
        appendTokenSet(variants, PascalLexer.TYPE_DECLARATIONS);
    }

    private void appendTokenSet(List<LookupElement> variants, TokenSet tokenSet) {
        for (IElementType op : tokenSet.getTypes()) {
            variants.add(LookupElementBuilder.create(op.toString()).
                    withIcon(PascalIcons.GENERAL).withStrikeoutness(op.equals(PasTypes.GOTO))
            );
        }
    }
}
