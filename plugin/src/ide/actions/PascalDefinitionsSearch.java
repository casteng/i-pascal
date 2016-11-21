package com.siberika.idea.pascal.ide.actions;

import com.intellij.openapi.application.QueryExecutorBase;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import com.intellij.psi.search.searches.DefinitionsScopedSearch;
import com.intellij.psi.search.searches.ReferencesSearch;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.Processor;
import com.siberika.idea.pascal.lang.psi.PasEntityScope;
import com.siberika.idea.pascal.lang.psi.PasExportedRoutine;
import com.siberika.idea.pascal.lang.psi.PasRoutineImplDecl;
import com.siberika.idea.pascal.lang.psi.PascalStructType;
import com.siberika.idea.pascal.lang.psi.impl.PascalRoutineImpl;
import com.siberika.idea.pascal.util.PsiUtil;
import org.jetbrains.annotations.NotNull;

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
        Collection<PasEntityScope> targets = findImplementations(queryParameters.getElement(), GotoSuper.LIMIT_NONE, 0);
        for (PsiElement target : targets) {
            consumer.process(target);
        }
    }

    public static Collection<PasEntityScope> findImplementations(PsiElement element, Integer limit, int rCnt) {
        Collection<PasEntityScope> targets = new LinkedHashSet<PasEntityScope>();
        PascalRoutineImpl routine = PsiTreeUtil.getParentOfType(element, PascalRoutineImpl.class);
        if (routine != null) {
            findImplementingMethods(targets, routine, limit, 0);
        } else {
            findDescendingStructs(targets, PsiUtil.getStructByElement(element), limit, 0);
        }
        return targets;
    }

    public static void findImplementingMethods(Collection<PasEntityScope> targets, PascalRoutineImpl routine, Integer limit, int rCnt) {
        Collection<PasEntityScope> found = new LinkedHashSet<PasEntityScope>();
        Collection<PasEntityScope> scopes = new LinkedHashSet<PasEntityScope>();
        if (routine instanceof PasRoutineImplDecl) {
            PsiElement el = SectionToggle.retrieveDeclaration(routine);
            if (el instanceof PasExportedRoutine) {
                routine = (PascalRoutineImpl) el;
            } else {
                return;
            }
        }
        PascalStructType struct = PsiUtil.getStructByElement(routine);
        findDescendingStructs(scopes, struct, limit != null ? GotoSuper.LIMIT_FIRST_ATTEMPT : GotoSuper.LIMIT_NONE, rCnt);
        GotoSuper.extractMethodsByName(found, scopes, routine, false, GotoSuper.calcRemainingLimit(targets, limit), 0);
        if ((limit != null) && (scopes.size() == GotoSuper.LIMIT_FIRST_ATTEMPT) && (found.size() < limit)) {        // second attempt if the first one not found all results
            scopes = new LinkedHashSet<PasEntityScope>();
            findDescendingStructs(scopes, PsiUtil.getStructByElement(routine), GotoSuper.LIMIT_NONE, rCnt);
            GotoSuper.extractMethodsByName(targets, scopes, routine, false, GotoSuper.calcRemainingLimit(targets, limit), 0);
        } else {
            targets.addAll(found);
        }
    }

    public static void findDescendingStructs(Collection<PasEntityScope> targets, PascalStructType struct, Integer limit, int rCnt) {
        if ((limit != null) && (limit <= 0)) {
            return;
        }
        if (rCnt > MAX_RECURSION) {
            LOG.error("Max recursion reached");
            return;
        }
        if ((null == struct) || (null == struct.getNameIdentifier())) {
            return;
        }
        for (PsiReference psiReference : ReferencesSearch.search(struct.getNameIdentifier())) {
            if ((limit != null) && (limit <= targets.size())) {
                return;
            }
            if (PsiUtil.isClassParent(psiReference.getElement())) {
                struct = PsiUtil.getStructByElement(psiReference.getElement());
                if (PsiUtil.isElementUsable(struct)) {
                    targets.add(struct);
                    findDescendingStructs(targets, struct, GotoSuper.calcRemainingLimit(targets, limit), rCnt + 1);
                }
            }
        }
    }

}
