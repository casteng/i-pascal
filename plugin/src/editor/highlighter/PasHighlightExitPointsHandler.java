package com.siberika.idea.pascal.editor.highlighter;

import com.intellij.codeInsight.highlighting.HighlightUsagesHandlerBase;
import com.intellij.featureStatistics.ProductivityFeatureNames;
import com.intellij.openapi.editor.Editor;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.Consumer;
import com.siberika.idea.pascal.lang.context.ContextUtil;
import com.siberika.idea.pascal.lang.psi.PasEntityScope;
import com.siberika.idea.pascal.lang.psi.PasExitStatement;
import com.siberika.idea.pascal.lang.psi.PasFullyQualifiedIdent;
import com.siberika.idea.pascal.lang.psi.PasRaiseStatement;
import com.siberika.idea.pascal.lang.psi.PascalNamedElement;
import com.siberika.idea.pascal.lang.psi.PascalPsiElement;
import com.siberika.idea.pascal.lang.psi.PascalRoutine;
import com.siberika.idea.pascal.util.PsiUtil;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * Highlight all EXIT and RAISE keywords or Result references (for functions) within nearest scope
 */
public class PasHighlightExitPointsHandler extends HighlightUsagesHandlerBase<PsiElement> {

    private static final Class[] CLASSES = {PasExitStatement.class, PasRaiseStatement.class};
    private static final Class[] CLASSES_FOR_FUNCTION = {PasExitStatement.class, PasRaiseStatement.class, PasFullyQualifiedIdent.class};

    private final PsiElement target;

    PasHighlightExitPointsHandler(Editor editor, PsiFile file, PsiElement target) {
        super(editor, file);
        this.target = target;
    }

    @Override
    public List<PsiElement> getTargets() {
        return Collections.singletonList(target);
    }

    @Override
    protected void selectTargets(List<PsiElement> targets, Consumer<List<PsiElement>> selectionConsumer) {
        selectionConsumer.consume(targets);
    }

    @Override
    public void computeUsages(List<PsiElement> targets) {
        PsiElement parent = target.getParent();
        if (!(parent instanceof PasExitStatement) && !(parent instanceof PasRaiseStatement) && !(PascalHighlightHandlerFactory.isResultReference(target))) {
            return;
        }

        PasEntityScope scope = PsiUtil.getNearestAffectingScope(target);
        if (null == scope) {
            return;
        }

        @SuppressWarnings("unchecked")
        Collection<PascalPsiElement> sts = PsiTreeUtil.findChildrenOfAnyType(scope, isFunction(scope) ? CLASSES_FOR_FUNCTION : CLASSES);
        for (PascalPsiElement st : sts) {
            if (PsiUtil.getNearestAffectingScope(st) == scope) {
                if (st instanceof PasFullyQualifiedIdent) {
                    PascalNamedElement ident = (PascalNamedElement) st;
                    if (ContextUtil.isAssignLeftPart(ident) && "RESULT".equalsIgnoreCase(ident.getName())) {
                        addOccurrence(st.getFirstChild());
                    }
                } else {
                    addOccurrence(st.getFirstChild());
                }
            }
        }
    }

    private boolean isFunction(PasEntityScope scope) {
        return scope instanceof PascalRoutine && ((PascalRoutine) scope).isFunction();
    }

    @Nullable
    @Override
    public String getFeatureId() {
        return ProductivityFeatureNames.CODEASSISTS_HIGHLIGHT_RETURN;
    }

}
