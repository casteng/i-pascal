package com.siberika.idea.pascal.ide.actions;

import com.intellij.lang.LanguageCodeInsightActionHandler;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.util.PsiTreeUtil;
import com.siberika.idea.pascal.PascalBundle;
import com.siberika.idea.pascal.lang.psi.PasEntityScope;
import com.siberika.idea.pascal.lang.psi.PascalStructType;
import com.siberika.idea.pascal.lang.psi.impl.PasField;
import com.siberika.idea.pascal.lang.psi.impl.PascalRoutineImpl;
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
        PascalRoutineImpl routine = PsiTreeUtil.getParentOfType(el, PascalRoutineImpl.class);
        if (routine != null) {
            getRoutineTarget(targets, routine);
        } else {
            getStructTarget(targets, PsiUtil.getStructByElement(el));
        }
        return targets;
    }

    private static void getStructTarget(Collection<PasEntityScope> targets, PasEntityScope struct) {
        if (struct instanceof PascalStructType) {
            for (PasEntityScope parent : struct.getParentScope()) {
                addTarget(targets, parent);
                getStructTarget(targets, parent);
            }
        }
    }

    private static void addTarget(Collection<PasEntityScope> targets, PasEntityScope target) {
        if (target != null) {
            targets.add(target);
        }
    }

    private static void addTarget(Collection<PasEntityScope> targets, PasField target) {
        if ((target != null) && (target.element instanceof PasEntityScope)) {
            targets.add((PasEntityScope) target.element);
            //target.element.putUserData(KEY_ELEMENT_FIELD, target);
        }
    }

    private static void getRoutineTarget(Collection<PasEntityScope> targets, PascalRoutineImpl routine) {
        if (null == routine) {
            return;
        }
        PasEntityScope scope = routine.getContainingScope();
        if (scope instanceof PascalStructType) {
            extractMethodsByName(targets, scope.getParentScope(), routine, true);
        }
    }

    /**
     * Extracts methods with same name as routine from the given scopes and places them into targets collection
     * @param targets    target collection
     * @param scopes     scopes where to search methods
     * @param routine    routine which name to search
     */
    static void extractMethodsByName(Collection<PasEntityScope> targets, Collection<PasEntityScope> scopes, PascalRoutineImpl routine, boolean handleParents) {
        for (PasEntityScope scope : scopes) {
            if (scope instanceof PascalStructType) {
                PasField field = scope.getField(StrUtil.getFieldName(PsiUtil.getFieldName(routine)));
                if ((field != null) && (field.fieldType == PasField.FieldType.ROUTINE)) {
                    addTarget(targets, field);
                }
                if (handleParents) {
                    extractMethodsByName(targets, scope.getParentScope(), routine, true);
                }
            }
        }
    }

    @Override
    public boolean startInWriteAction() {
        return false;
    }

}
