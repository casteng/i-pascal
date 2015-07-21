package com.siberika.idea.pascal.ide.actions;

import com.intellij.lang.LanguageCodeInsightActionHandler;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.util.PsiTreeUtil;
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
import java.util.List;

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
        Collection<PsiElement> targets = new LinkedHashSet<PsiElement>();
        // cases el is: struct type, method decl, method impl
        PascalRoutineImpl routine = PsiTreeUtil.getParentOfType(el, PascalRoutineImpl.class);
        if (routine != null) {
            getRoutineTarget(targets, routine);
        } else {
            getStructTarget(targets, PsiUtil.getStructByElement(el));
        }
        if (!targets.isEmpty()) {
            EditorUtil.navigateTo(editor, targets);
        }
    }

    private void getStructTarget(Collection<PsiElement> targets, PasEntityScope struct) {
        if (struct instanceof PascalStructType) {
            for (PasEntityScope parent : struct.getParentScope()) {
                addTarget(targets, parent);
                getStructTarget(targets, parent);
            }
        }
    }

    private void addTarget(Collection<PsiElement> targets, PsiElement target) {
        if (target != null) {
            targets.add(target);
        }
    }

    private void getRoutineTarget(Collection<PsiElement> targets, PascalRoutineImpl routine) {
        if (null == routine) {
            return;
        }
        PasEntityScope scope = routine.getContainingScope();
        if (scope instanceof PascalStructType) {
            checkParents(targets, scope, routine);
        }
    }

    private void checkParents(Collection<PsiElement> targets, PasEntityScope scope, PascalRoutineImpl routine) {
        List<PasEntityScope> parents = scope.getParentScope();
        for (PasEntityScope parent : parents) {
            if (parent instanceof PascalStructType) {
                PasField field = parent.getField(StrUtil.getFieldName(PsiUtil.getFieldName(routine)));
                if ((field != null) && (field.fieldType == PasField.FieldType.ROUTINE)) {
                    addTarget(targets, field.element);
                }
                checkParents(targets, parent, routine);
            }
        }
    }

    @Override
    public boolean startInWriteAction() {
        return false;
    }
}
