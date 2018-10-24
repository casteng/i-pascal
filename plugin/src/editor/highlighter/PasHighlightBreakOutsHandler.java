package com.siberika.idea.pascal.editor.highlighter;

import com.intellij.codeInsight.highlighting.HighlightUsagesHandlerBase;
import com.intellij.featureStatistics.ProductivityFeatureNames;
import com.intellij.openapi.editor.Editor;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.Consumer;
import com.siberika.idea.pascal.lang.psi.PasBreakStatement;
import com.siberika.idea.pascal.lang.psi.PasContinueStatement;
import com.siberika.idea.pascal.lang.psi.PasEntityScope;
import com.siberika.idea.pascal.lang.psi.PasExitStatement;
import com.siberika.idea.pascal.lang.psi.PasForStatement;
import com.siberika.idea.pascal.lang.psi.PasRaiseStatement;
import com.siberika.idea.pascal.lang.psi.PasRepeatStatement;
import com.siberika.idea.pascal.lang.psi.PasWhileStatement;
import com.siberika.idea.pascal.lang.psi.PascalPsiElement;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * Highlight all BREAK and CONTINUE keywords within a loop as well as the loop keyword
 */
public class PasHighlightBreakOutsHandler extends HighlightUsagesHandlerBase<PsiElement> {
    private final PsiElement target;

    PasHighlightBreakOutsHandler(Editor editor, PsiFile file, PsiElement target) {
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
        if (!(parent instanceof PasBreakStatement) && !(parent instanceof PasContinueStatement)) {
            return;
        }

        PsiElement loop = getLoop(target);
        if (null == loop) {
            return;
        }
        addOccurrence(loop.getFirstChild());
        Collection<PascalPsiElement> sts = PsiTreeUtil.findChildrenOfAnyType(loop, PasExitStatement.class, PasRaiseStatement.class, PasBreakStatement.class, PasContinueStatement.class);
        for (PascalPsiElement st : sts) {
            if (getLoop(st) == loop) {
                addOccurrence(st.getFirstChild());
            }
        }
    }

    private PsiElement getLoop(PsiElement target) {
        PsiElement result = PsiTreeUtil.getNonStrictParentOfType(target, PasEntityScope.class, PasForStatement.class, PasWhileStatement.class, PasRepeatStatement.class);
        if (result instanceof PasForStatement || result instanceof PasWhileStatement || result instanceof PasRepeatStatement) {
            return result;
        } else {
            return null;
        }
    }

    @Nullable
    @Override
    public String getFeatureId() {
        return ProductivityFeatureNames.CODEASSISTS_HIGHLIGHT_RETURN;
    }

}
