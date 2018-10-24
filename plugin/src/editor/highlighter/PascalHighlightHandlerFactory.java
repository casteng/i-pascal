package com.siberika.idea.pascal.editor.highlighter;

import com.intellij.codeInsight.highlighting.HighlightUsagesHandlerBase;
import com.intellij.codeInsight.highlighting.HighlightUsagesHandlerFactoryBase;
import com.intellij.openapi.editor.Editor;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.siberika.idea.pascal.lang.context.ContextUtil;
import com.siberika.idea.pascal.lang.psi.PasNamespaceIdent;
import com.siberika.idea.pascal.lang.psi.PasSubIdent;
import com.siberika.idea.pascal.lang.psi.PasUsesClause;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class PascalHighlightHandlerFactory extends HighlightUsagesHandlerFactoryBase {

    /*
     * when unit in uses is highlighted: all identifiers from the unit
     * when on argument of a WITH statement: all identifiers from the argument's scope within WITH statement scope
     */

    @Nullable
    @Override
    public HighlightUsagesHandlerBase createHighlightUsagesHandler(@NotNull Editor editor, @NotNull PsiFile file, @NotNull PsiElement target) {
        if ("EXIT".equalsIgnoreCase(target.getText()) || "RAISE".equalsIgnoreCase(target.getText()) || isResultReference(target)) {
            return new PasHighlightExitPointsHandler(editor, file, target);
        }
        if ("CONTINUE".equalsIgnoreCase(target.getText()) || "BREAK".equalsIgnoreCase(target.getText())) {
            return new PasHighlightBreakOutsHandler(editor, file, target);
        }
        if (getUnitReference(target) != null) {
            return new PasHighlightUnitIdentsHandler(editor, file, target);
        }
        return null;
    }

    static boolean isResultReference(PsiElement target) {
        if (target.getParent() instanceof PasSubIdent) {
            PasSubIdent ident = (PasSubIdent) target.getParent();
            return "RESULT".equalsIgnoreCase(ident.getName()) && ContextUtil.isAssignLeftPart((ident));
        } else {
            return false;
        }
    }

    static PasNamespaceIdent getUnitReference(PsiElement target) {
        PsiElement parent = target.getParent();
        if (parent instanceof PasSubIdent) {
            parent = parent.getParent();
            return (parent instanceof PasNamespaceIdent) && (parent.getParent() instanceof PasUsesClause) ? (PasNamespaceIdent) parent : null;
        } else {
            return null;
        }
    }
}
