package com.siberika.idea.pascal.lang.references;

import com.intellij.openapi.application.QueryExecutorBase;
import com.intellij.openapi.util.Pair;
import com.intellij.psi.PsiComment;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import com.intellij.psi.search.RequestResultProcessor;
import com.intellij.psi.search.UsageSearchContext;
import com.intellij.psi.search.searches.ReferencesSearch;
import com.intellij.util.Processor;
import com.siberika.idea.pascal.lang.psi.PasTypes;
import com.siberika.idea.pascal.util.StrUtil;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Author: George Bakhtadze
 * Date: 06/02/2015
 */
public class PascalReferencesSearcher extends QueryExecutorBase<PascalCommentReference, ReferencesSearch.SearchParameters> {
    public PascalReferencesSearcher() {
        super(true);
    }

    @Override
    public void processQuery(@NotNull ReferencesSearch.SearchParameters p, @NotNull Processor<? super PascalCommentReference> consumer) {
        final PsiElement element = p.getElementToSearch();
        if (element instanceof PsiComment) {
            List<Pair<Integer, String>> directives = StrUtil.parseDirectives(element.getText());
            for (Pair<Integer, String> directive : directives) {
                p.getOptimizer().searchWord(directive.getSecond(), p.getEffectiveSearchScope(), UsageSearchContext.IN_COMMENTS, false, element,
                        new RequestResultProcessor() {
                            @Override
                            public boolean processTextOccurrence(@NotNull PsiElement el, int offsetInElement, @NotNull Processor<? super PsiReference> c) {
                                if (el.getNode().getElementType() == PasTypes.CT_DEFINE) {
                                    PsiReference ref = el.getReference();
                                    ref = ref != null ? ref : new PascalCommentReference(el);
                                    return c.process(ref);
                                }
                                return true;
                            }
                        });
            }
        }
    }
}
