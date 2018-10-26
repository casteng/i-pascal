package com.siberika.idea.pascal.editor.highlighter;

import com.intellij.codeInsight.highlighting.HighlightUsagesHandlerBase;
import com.intellij.featureStatistics.ProductivityFeatureNames;
import com.intellij.lang.injection.InjectedLanguageManager;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.Consumer;
import com.siberika.idea.pascal.lang.psi.PasEntityScope;
import com.siberika.idea.pascal.lang.psi.PasExitStatement;
import com.siberika.idea.pascal.lang.psi.PasFullyQualifiedIdent;
import com.siberika.idea.pascal.lang.psi.PasRaiseStatement;
import com.siberika.idea.pascal.lang.psi.PasSubIdent;
import com.siberika.idea.pascal.lang.psi.PascalPsiElement;
import com.siberika.idea.pascal.util.PsiUtil;
import org.jetbrains.annotations.NotNull;
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
        Collection<PascalPsiElement> sts = PsiTreeUtil.findChildrenOfAnyType(scope, PascalHighlightHandlerFactory.isFunction(scope) ? CLASSES_FOR_FUNCTION : CLASSES);
        for (PascalPsiElement st : sts) {
            if (PsiUtil.getNearestAffectingScope(st) == scope) {
                if (st instanceof PasFullyQualifiedIdent) {
                    List<PasSubIdent> subidents = ((PasFullyQualifiedIdent) st).getSubIdentList();
                    if (!subidents.isEmpty() && "RESULT".equalsIgnoreCase(subidents.get(0).getName())) {
                        if (PascalReadWriteAccessDetector.isWriteAccess(st)) {
                            addWriteOccurrence(st.getFirstChild());
                        } else {
                            addOccurrence(st.getFirstChild());
                        }
                    }
                } else {
                    addOccurrence(st.getFirstChild());
                }
            }
        }
    }

    private void addWriteOccurrence(@NotNull PsiElement element) {
        TextRange range = element.getTextRange();
        if (range != null) {
            range = InjectedLanguageManager.getInstance(element.getProject()).injectedToHost(element, range);
            myWriteUsages.add(range);
        }
    }

    @Nullable
    @Override
    public String getFeatureId() {
        return ProductivityFeatureNames.CODEASSISTS_HIGHLIGHT_RETURN;
    }

}
