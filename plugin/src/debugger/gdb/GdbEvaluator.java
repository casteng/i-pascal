package com.siberika.idea.pascal.debugger.gdb;

import com.intellij.debugger.impl.DebuggerUtilsEx;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.project.IndexNotReadyException;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiErrorElement;
import com.intellij.psi.PsiWhiteSpace;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.xdebugger.XSourcePosition;
import com.intellij.xdebugger.evaluation.XDebuggerEvaluator;
import com.siberika.idea.pascal.debugger.VariableManager;
import com.siberika.idea.pascal.lang.psi.PasFullyQualifiedIdent;
import com.siberika.idea.pascal.lang.psi.PasGenericTypeIdent;
import com.siberika.idea.pascal.lang.psi.PasNamedIdent;
import com.siberika.idea.pascal.lang.psi.PasNamedIdentDecl;
import com.siberika.idea.pascal.lang.psi.PasRefNamedIdent;
import com.siberika.idea.pascal.lang.psi.PasSubIdent;
import com.siberika.idea.pascal.lang.psi.PasTypes;
import com.siberika.idea.pascal.lang.psi.impl.PascalExpression;
import com.siberika.idea.pascal.util.PsiUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Author: George Bakhtadze
 * Date: 04/04/2017
 */
public class GdbEvaluator extends XDebuggerEvaluator {
    private final GdbStackFrame frame;
    private final VariableManager variableManager;

    GdbEvaluator(GdbStackFrame frame, VariableManager variableManager) {
        this.frame = frame;
        this.variableManager = variableManager;
    }

    @Override
    public void evaluate(@NotNull String expression, @NotNull XEvaluationCallback callback, @Nullable XSourcePosition expressionPosition) {
        variableManager.evaluate(frame, expression, callback, expressionPosition);
    }

    @Nullable
    @Override
    public TextRange getExpressionRangeAtOffset(Project project, Document document, int offset, boolean sideEffectsAllowed) {
        return PsiDocumentManager.getInstance(project).commitAndRunReadAction(() -> {
            try {
                PsiElement element = DebuggerUtilsEx.findElementAt(PsiDocumentManager.getInstance(project).getPsiFile(document), offset);
                if (!PsiUtil.isElementUsable(element)) {
                    return null;
                }
                if ((element.getNode().getElementType() == PasTypes.NAME) || PsiUtil.isEntityName(element)) {
                    PsiElement expr = PsiTreeUtil.skipParentsOfType(element,
                            PasFullyQualifiedIdent.class, PasSubIdent.class, PasRefNamedIdent.class, PasNamedIdent.class, PasNamedIdentDecl.class, PasGenericTypeIdent.class,
                            PsiWhiteSpace.class, PsiErrorElement.class);
                    return expr instanceof PascalExpression ? TextRange.create(expr.getTextRange().getStartOffset(), Math.min(expr.getTextRange().getEndOffset(), element.getTextRange().getEndOffset())) : null;
                }
            } catch (IndexNotReadyException ignored) {}
            return null;
        });
    }
}
