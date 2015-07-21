package com.siberika.idea.pascal.ide.actions;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import com.intellij.psi.search.searches.ReferencesSearch;
import com.intellij.util.Processor;
import com.intellij.util.QueryExecutor;
import com.siberika.idea.pascal.lang.psi.PascalStructType;
import com.siberika.idea.pascal.util.PsiUtil;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.LinkedHashSet;

/**
 * Author: George Bakhtadze
 * Date: 02/07/2015
 */
public class PascalDefinitionsSearch implements QueryExecutor<PsiElement, PsiElement> {

    private static final Logger LOG = Logger.getInstance(IntfImplNavAction.class.getName());

    private static final int MAX_RECURSION = 10;

    @Override
    public boolean execute(@NotNull PsiElement queryParameters, @NotNull Processor<PsiElement> consumer) {
        Collection<PsiElement> targets = new LinkedHashSet<PsiElement>();
        findDescendingStructs(targets, PsiUtil.getStructByElement(queryParameters), 0);
        return !targets.isEmpty();
    }

    private void findDescendingStructs(Collection<PsiElement> targets, PascalStructType struct, int rCnt) {
        if (rCnt > MAX_RECURSION) {
            LOG.error("Max recursion reached");
            return;
        }
        if (null == struct.getNameIdentifier()) {
            return;
        }
        for (PsiReference psiReference : ReferencesSearch.search(struct.getNameIdentifier())) {
            if (PsiUtil.isClassParent(psiReference.getElement())) {
                struct = PsiUtil.getStructByElement(psiReference.getElement());
                if (PsiUtil.isElementUsable(struct)) {
                    targets.add(struct);
                    findDescendingStructs(targets, struct, rCnt + 1);
                }
            }
        }
    }

}
