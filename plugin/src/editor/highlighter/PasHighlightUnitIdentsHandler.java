package com.siberika.idea.pascal.editor.highlighter;

import com.intellij.codeInsight.highlighting.HighlightUsagesHandlerBase;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtilCore;
import com.intellij.openapi.util.Pair;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.util.Consumer;
import com.siberika.idea.pascal.lang.context.ContextUtil;
import com.siberika.idea.pascal.lang.psi.PasModule;
import com.siberika.idea.pascal.lang.psi.PasNamespaceIdent;
import com.siberika.idea.pascal.lang.psi.PasUsesClause;
import com.siberika.idea.pascal.lang.psi.PascalNamedElement;
import com.siberika.idea.pascal.util.ModuleUtil;
import com.siberika.idea.pascal.util.PsiUtil;

import java.util.Collections;
import java.util.List;

/**
 * Highlight all identifiers which come from the unit under cursor or all external identifiers when cursor is on USES keyword
 */
public class PasHighlightUnitIdentsHandler extends HighlightUsagesHandlerBase<PsiElement> {
    private final PsiElement target;

    PasHighlightUnitIdentsHandler(Editor editor, PsiFile file, PsiElement target) {
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
        PasModule pasModule = PsiUtil.getElementPasModule(target);
        if (null == pasModule) {
            return;
        }

        PasNamespaceIdent unitName = null;
        if (!(target.getParent() instanceof PasUsesClause)) {
            unitName = PascalHighlightHandlerFactory.getUnitReference(target);
            if (null == unitName) {
                return;
            }
        }

        Module module = ModuleUtilCore.findModuleForPsiElement(target);
        addOccurrence(target);
        Pair<List<PascalNamedElement>, List<PascalNamedElement>> idents = pasModule.getIdentsFrom(unitName != null ? unitName.getName() : null,
                ContextUtil.belongsToInterface(target), ModuleUtil.retrieveUnitNamespaces(module, target.getProject()));
        for (PascalNamedElement ident : idents.first) {
            addOccurrence(ident);
        }
        for (PascalNamedElement ident : idents.second) {
            addOccurrence(ident);
        }
    }

}
