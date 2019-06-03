package com.siberika.idea.pascal.ide.intention;

import com.intellij.openapi.editor.Editor;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiErrorElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiWhiteSpace;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.psi.util.PsiUtilCore;
import com.siberika.idea.pascal.PascalBundle;
import com.siberika.idea.pascal.lang.psi.PasEntityScope;
import com.siberika.idea.pascal.lang.psi.PasGenericTypeIdent;
import com.siberika.idea.pascal.lang.psi.PascalNamedElement;
import com.siberika.idea.pascal.lang.psi.PascalRoutine;
import com.siberika.idea.pascal.lang.psi.PascalStructType;
import com.siberika.idea.pascal.lang.search.DescendingEntities;
import com.siberika.idea.pascal.util.EditorUtil;
import com.siberika.idea.pascal.util.PsiUtil;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

class GotoImplementationAction extends IntentionActionBase {

    @NotNull
    @Override
    public String getText() {
        return PascalBundle.message("action.fix.struct.goto.descending");
    }

    @Override
    protected boolean isAvailable(PsiFile file, PsiElement element) {
        return getScopeElement(element) != null;
    }

    @Override
    protected void invoke(PsiFile file, PsiElement element, Editor editor) {
        PasEntityScope scope = getScopeElement(element);
        Collection<PasEntityScope> targets = DescendingEntities.getQuery(scope, GlobalSearchScope.allScope(PsiUtilCore.getProjectInReadAction(element))).findAll();
        EditorUtil.navigateTo(editor, getText(), targets);
    }

    private static PasEntityScope getScopeElement(PsiElement element) {
        element = PsiTreeUtil.skipParentsOfType(element, PsiWhiteSpace.class, PsiErrorElement.class);
        if ((element instanceof PascalRoutine) || (element instanceof PascalStructType)) {
            return (PasEntityScope) element;
        }
        return ((element instanceof PascalNamedElement) && (element.getParent() instanceof PasGenericTypeIdent)) ? PsiUtil.getStructByElement(element) : null;
    }

}
