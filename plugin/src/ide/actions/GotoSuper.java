package com.siberika.idea.pascal.ide.actions;

import com.intellij.lang.LanguageCodeInsightActionHandler;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.SmartPsiElementPointer;
import com.intellij.psi.util.PsiTreeUtil;
import com.siberika.idea.pascal.PascalBundle;
import com.siberika.idea.pascal.PascalRTException;
import com.siberika.idea.pascal.lang.psi.PasEntityScope;
import com.siberika.idea.pascal.lang.psi.PascalInterfaceDecl;
import com.siberika.idea.pascal.lang.psi.PascalRoutine;
import com.siberika.idea.pascal.lang.psi.PascalStructType;
import com.siberika.idea.pascal.lang.psi.impl.PasField;
import com.siberika.idea.pascal.lang.references.PasReferenceUtil;
import com.siberika.idea.pascal.util.EditorUtil;
import com.siberika.idea.pascal.util.PsiUtil;
import com.siberika.idea.pascal.util.StrUtil;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.LinkedHashSet;

/**
 * Author: George Bakhtadze
 * Date: 02/07/2015
 */
public class GotoSuper implements LanguageCodeInsightActionHandler {

    private static final Logger LOG = Logger.getInstance(GotoSuper.class.getName());

    public static final Integer LIMIT_NONE = null;

    static final Integer LIMIT_FIRST_ATTEMPT = 5;        // for first attempts

    static Integer calcRemainingLimit(Collection<PasEntityScope> targets, Integer limit) {
        return limit != null ? limit - targets.size() : null;
    }

    @Override
    public boolean isValidFor(Editor editor, PsiFile file) {
        return false;
    }

    @Override
    public void invoke(@NotNull Project project, @NotNull Editor editor, @NotNull PsiFile file) {
        PsiElement el = file.findElementAt(editor.getCaretModel().getOffset());
        Collection<PasEntityScope> targets = retrieveGotoSuperTargets(el);
        if (!targets.isEmpty()) {
            EditorUtil.navigateTo(editor, PascalBundle.message("navigate.title.goto.super"), targets);
        }
    }

    public static Collection<PasEntityScope> retrieveGotoSuperTargets(PsiElement el) {
        LinkedHashSet<PasEntityScope> targets = new LinkedHashSet<PasEntityScope>();
        // cases el is: struct type, method decl, method impl
        PascalRoutine routine = PsiTreeUtil.getParentOfType(el, PascalRoutine.class);
        if (routine != null) {
            getRoutineTarget(targets, routine);
        } else {
            retrieveParentStructs(targets, PsiUtil.getStructByElement(el), 0);
        }
        return targets;
    }

    public static void retrieveParentStructs(Collection<PasEntityScope> targets, PasEntityScope struct, final int recursionCount) {
        if (recursionCount > PasReferenceUtil.MAX_RECURSION_COUNT) {
            throw new PascalRTException("Too much recursion during retrieving parents: " + struct.getUniqueName());
        }
        if (struct instanceof PascalStructType) {
            for (SmartPsiElementPointer<PasEntityScope> parent : struct.getParentScope()) {
                PasEntityScope el = parent.getElement();
                addTarget(targets, el);
                if (!struct.equals(el)) {
                    retrieveParentStructs(targets, el, recursionCount + 1);
                }
            }
        }
    }

    public static void retrieveParentInterfaces(Collection<PasEntityScope> targets, PasEntityScope struct, final int recursionCount) {
        if (recursionCount > PasReferenceUtil.MAX_RECURSION_COUNT) {
            throw new PascalRTException("Too much recursion during retrieving parents: " + struct.getUniqueName());
        }
        if (struct instanceof PascalStructType) {
            for (SmartPsiElementPointer<PasEntityScope> parent : struct.getParentScope()) {
                PasEntityScope el = parent.getElement();
                if (el instanceof PascalInterfaceDecl) {
                    targets.add(el);
                }
                if (!struct.equals(el)) {
                    retrieveParentInterfaces(targets, el, recursionCount + 1);
                }
            }
        }
    }

    private static void addTarget(Collection<PasEntityScope> targets, PasEntityScope target) {
        if (target != null) {
            targets.add(target);
        }
    }

    private static void addTarget(Collection<PasEntityScope> targets, PasField target) {
        if ((target != null) && (target.getElement() instanceof PasEntityScope)) {
            targets.add((PasEntityScope) target.getElement());
        }
    }

    private static void getRoutineTarget(Collection<PasEntityScope> targets, PascalRoutine routine) {
        if (null == routine) {
            return;
        }
        PasEntityScope scope = routine.getContainingScope();
        if (scope instanceof PascalStructType) {
            extractMethodsByName(targets, PsiUtil.extractSmartPointers(scope.getParentScope()), routine, true, LIMIT_NONE, 0);
        }
    }

    private static final int MAX_RECURSION_COUNT = 100;

    /**
     * Extracts methods with same name as routine from the given scopes and places them into targets collection
     * @param targets    target collection
     * @param scopes     scopes where to search methods
     * @param routine    routine which name to search
     */
    static void extractMethodsByName(Collection<PasEntityScope> targets, Collection<PasEntityScope> scopes, PascalRoutine routine, boolean handleParents, Integer limit, int recursionCount) {
        if (recursionCount > MAX_RECURSION_COUNT) {
            throw new IllegalStateException("Recursion limit reached");
        }
        for (PasEntityScope scope : scopes) {
            if ((limit != null) && (limit <= targets.size())) {
                return;
            }
            if (scope != null) {
                if (scope instanceof PascalStructType) {
                    PasField field = scope.getField(StrUtil.getFieldName(PsiUtil.getFieldName(routine)));
                    if ((field != null) && (field.fieldType == PasField.FieldType.ROUTINE)) {
                        addTarget(targets, field);
                    }
                    if (handleParents) {
                        extractMethodsByName(targets, PsiUtil.extractSmartPointers(scope.getParentScope()), routine, true, calcRemainingLimit(targets, limit), recursionCount++);
                    }
                }
            } else {
                LOG.info("Invalid scope pointer resolved while extracting methods for: " + routine);
            }
        }
    }

    @Override
    public boolean startInWriteAction() {
        return false;
    }

}
