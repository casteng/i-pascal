package com.siberika.idea.pascal.ide.actions;

import com.intellij.openapi.application.QueryExecutorBase;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import com.intellij.psi.SmartPointerManager;
import com.intellij.psi.SmartPsiElementPointer;
import com.intellij.psi.search.searches.DefinitionsScopedSearch;
import com.intellij.psi.search.searches.ReferencesSearch;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.Processor;
import com.siberika.idea.pascal.lang.psi.PasEntityScope;
import com.siberika.idea.pascal.lang.psi.PascalStructType;
import com.siberika.idea.pascal.lang.psi.impl.PascalRoutineImpl;
import com.siberika.idea.pascal.util.PsiUtil;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;

/**
 * Author: George Bakhtadze
 * Date: 02/07/2015
 */
public class PascalDefinitionsSearch extends QueryExecutorBase<PsiElement, DefinitionsScopedSearch.SearchParameters> {

    private static final Logger LOG = Logger.getInstance(IntfImplNavAction.class.getName());

    private static final int MAX_RECURSION = 10;

    public PascalDefinitionsSearch() {
        super(true);
    }

    @Override
    public void processQuery(@NotNull DefinitionsScopedSearch.SearchParameters queryParameters, @NotNull Processor<PsiElement> consumer) {
        Collection<PasEntityScope> targets = findImplementations(queryParameters.getElement(), 0);
        for (PsiElement target : targets) {
            consumer.process(target);
        }
    }

    public static Collection<PasEntityScope> findImplementations(PsiElement element, int rCnt) {
        Collection<PasEntityScope> targets = new LinkedHashSet<PasEntityScope>();
        PascalRoutineImpl routine = PsiTreeUtil.getParentOfType(element, PascalRoutineImpl.class);
        if (routine != null) {
            findImplementingMethods(targets, routine, 0);
        } else {
            findDescendingStructs(targets, PsiUtil.getStructByElement(element), 0);
        }
        return targets;
    }

    public static void findImplementingMethods(Collection<PasEntityScope> targets, PascalRoutineImpl routine, int rCnt) {
        Collection<PasEntityScope> scopes = new LinkedHashSet<PasEntityScope>();
        findDescendingStructs(scopes, PsiUtil.getStructByElement(routine), rCnt);
        Collection<SmartPsiElementPointer<PasEntityScope>> scopePtrs = new ArrayList<SmartPsiElementPointer<PasEntityScope>>(scopes.size());
        for (PasEntityScope scope : scopes) {
            scopePtrs.add(SmartPointerManager.getInstance(scope.getProject()).createSmartPsiElementPointer(scope));
        }
        GotoSuper.extractMethodsByName(targets, scopePtrs, routine, false);
    }

    public static void findDescendingStructs(Collection<PasEntityScope> targets, PascalStructType struct, int rCnt) {
        if (rCnt > MAX_RECURSION) {
            LOG.error("Max recursion reached");
            return;
        }
        if ((null == struct) || (null == struct.getNameIdentifier())) {
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
