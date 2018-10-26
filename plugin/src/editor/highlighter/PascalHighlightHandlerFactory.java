package com.siberika.idea.pascal.editor.highlighter;

import com.intellij.codeInsight.highlighting.HighlightUsagesHandlerBase;
import com.intellij.codeInsight.highlighting.HighlightUsagesHandlerFactoryBase;
import com.intellij.openapi.editor.Editor;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.siberika.idea.pascal.lang.psi.PasEntityScope;
import com.siberika.idea.pascal.lang.psi.PasNamespaceIdent;
import com.siberika.idea.pascal.lang.psi.PasSubIdent;
import com.siberika.idea.pascal.lang.psi.PasUsesClause;
import com.siberika.idea.pascal.lang.psi.PasWithStatement;
import com.siberika.idea.pascal.lang.psi.PascalRoutine;
import com.siberika.idea.pascal.util.PsiUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class PascalHighlightHandlerFactory extends HighlightUsagesHandlerFactoryBase {

    @Nullable
    @Override
    public HighlightUsagesHandlerBase createHighlightUsagesHandler(@NotNull Editor editor, @NotNull PsiFile file, @NotNull PsiElement target) {
        if ("EXIT".equalsIgnoreCase(target.getText()) || "RAISE".equalsIgnoreCase(target.getText()) || isResultReference(target)) {
            return new PasHighlightExitPointsHandler(editor, file, target);
        }
        if ("CONTINUE".equalsIgnoreCase(target.getText()) || "BREAK".equalsIgnoreCase(target.getText())) {
            return new PasHighlightBreakOutsHandler(editor, file, target);
        }
        if ("USES".equalsIgnoreCase(target.getText()) || getUnitReference(target) != null) {
            return new PasHighlightUnitIdentsHandler(editor, file, target);
        }
        if ("WITH".equalsIgnoreCase(target.getText()) || getWithStatement(target) != null) {
            return new PasHighlightWithIdentsHandler(editor, file, target);
        }
        return null;
    }

    static boolean isFunction(PasEntityScope scope) {
        return scope instanceof PascalRoutine && ((PascalRoutine) scope).isFunction();
    }

    static boolean isResultReference(PsiElement target) {
        if (target.getParent() instanceof PasSubIdent) {
            PasSubIdent ident = (PasSubIdent) target.getParent();
            if ("RESULT".equalsIgnoreCase(ident.getName())) {
                PasEntityScope scope = PsiUtil.getNearestAffectingScope(target);
                return isFunction(scope);
            }
        }
        return false;
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

    static PasWithStatement getWithStatement(PsiElement target) {
        PsiElement parent = PsiUtil.skipToExpressionParent(target.getParent());
        return (parent instanceof PasWithStatement) ? (PasWithStatement) parent : null;
    }
}
