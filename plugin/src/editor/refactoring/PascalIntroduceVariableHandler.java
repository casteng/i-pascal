package com.siberika.idea.pascal.editor.refactoring;

import com.intellij.codeInsight.template.TextResult;
import com.intellij.codeInsight.template.impl.TemplateState;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Pass;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.refactoring.IntroduceTargetChooser;
import com.intellij.refactoring.RefactoringActionHandler;
import com.intellij.util.SmartList;
import com.siberika.idea.pascal.PascalBundle;
import com.siberika.idea.pascal.editor.PascalActionDeclare;
import com.siberika.idea.pascal.ide.actions.quickfix.IdentQuickFixes;
import com.siberika.idea.pascal.lang.parser.NamespaceRec;
import com.siberika.idea.pascal.lang.psi.PasCallExpr;
import com.siberika.idea.pascal.lang.psi.PasCompoundStatement;
import com.siberika.idea.pascal.lang.psi.PasEntityScope;
import com.siberika.idea.pascal.lang.psi.PasExpr;
import com.siberika.idea.pascal.lang.psi.PasExpression;
import com.siberika.idea.pascal.lang.psi.PasFullyQualifiedIdent;
import com.siberika.idea.pascal.lang.psi.PasParenExpr;
import com.siberika.idea.pascal.lang.psi.PasReferenceExpr;
import com.siberika.idea.pascal.lang.psi.PasStatement;
import com.siberika.idea.pascal.lang.psi.PascalNamedElement;
import com.siberika.idea.pascal.lang.psi.PascalRoutine;
import com.siberika.idea.pascal.lang.psi.impl.PasElementFactory;
import com.siberika.idea.pascal.lang.psi.impl.PasField;
import com.siberika.idea.pascal.lang.psi.impl.PascalExpression;
import com.siberika.idea.pascal.lang.references.ResolveContext;
import com.siberika.idea.pascal.lang.references.resolve.Resolve;
import com.siberika.idea.pascal.lang.references.resolve.ResolveProcessor;
import com.siberika.idea.pascal.util.PsiUtil;
import com.siberika.idea.pascal.util.StmtUtil;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public class PascalIntroduceVariableHandler implements RefactoringActionHandler {
    @Override
    public void invoke(@NotNull Project project, Editor editor, PsiFile file, DataContext dataContext) {
        doIntroduceVar(project, editor, file, file.findElementAt(editor.getCaretModel().getOffset()));
    }

    @Override
    public void invoke(@NotNull Project project, @NotNull PsiElement[] elements, DataContext dataContext) {
        // not supported
    }

    private void doIntroduceVar(Project project, Editor editor, PsiFile file, PsiElement element) {
        List<PsiElement> expressionList = findExpressions(element);
        PsiElement nearestStatement = findNearestStatement(element);
        PasEntityScope scope = PsiUtil.getNearestAffectingScope(nearestStatement);
        if (!expressionList.isEmpty()) {
            if (editor != null) {
                IntroduceTargetChooser.showChooser(editor, expressionList, new Pass<PsiElement>() {
                            @Override
                            public void pass(PsiElement expression) {
                                String type = PascalExpression.inferType(expression);
                                PascalActionDeclare.ActionCreateVar cva = new PascalActionDeclare.ActionCreateVar("", expression, null, scope, type) {
                                    public void afterExecution(Editor editor, PsiFile file, TemplateState state) {
                                        TextResult varName = state.getVariableValue(TPL_VAR_NAME);
                                        if (varName != null) {
                                            replaceExprAndAddAssignment(nearestStatement, varName.getText(), expression);
                                        }
                                    }
                                };
                                cva.invoke(project, editor, file);
                            }
                        },
                        new PsiElementTrimRenderer(100),
                        PascalBundle.message("popup.expressions.title")
                );
            }
        }
    }

    private void replaceExprAndAddAssignment(PsiElement nearestStatement, String name, PsiElement expression) {
        final String text = expression.getText();
        PsiElement varElement = PasElementFactory.createElementFromText(expression.getProject(), name);
        PsiElement stmt = PasElementFactory.createElementFromText(expression.getProject(),
                "begin " + name +  " := " + text + ";end.", PasCompoundStatement.class);
        PsiElement stmtFinal = PsiTreeUtil.getChildOfType(stmt, PasStatement.class);
        if (stmtFinal != null) {
            ApplicationManager.getApplication().runWriteAction(
                    () -> {
                        expression.replace(varElement);
                        PsiElement newStmt = nearestStatement.getParent().addBefore(stmtFinal, nearestStatement);
                        IdentQuickFixes.addElements(nearestStatement.getParent(), newStmt, true, ";");
                    }
            );
        }
    }

    private PsiElement findNearestStatement(PsiElement element) {
        while ((element != null) && !(element instanceof PasStatement)) {
            element = element.getParent();
        }
        if (element != null) {             // Check if the statement is not child of a single-statement structured operator
            PsiElement parent = element.getParent();
            while (StmtUtil.isStructuredOperatorStatement(parent)) {
                parent = parent.getParent();
                element = parent;
            }
        }
        return element;
    }

    private List<PsiElement> findExpressions(PsiElement element) {
        List<PsiElement> result = new SmartList<>();
        PsiElement parent = PsiUtil.skipToExpressionParent(element);
        collectExpressions(result, element, parent);
        return result;
    }

    private void collectExpressions(List<PsiElement> result, PsiElement elementUnderCaret, PsiElement parent) {
        if (null == parent) {
            return;
        }
        for (PsiElement child : parent.getChildren()) {
            if (child instanceof PasExpr) {
                addExpression(result, elementUnderCaret, child, true);
            }
            if (child instanceof PasExpression) {
                addExpression(result, elementUnderCaret, ((PasExpression) child).getExpr(), true);
            }
        }
        parent = parent.getParent();
        if (parent instanceof PasExpr) {
            collectExpressions(result, elementUnderCaret, PsiUtil.skipToExpressionParent(parent));
        }
    }

    private void addExpression(List<PsiElement> result, PsiElement elementUnderCaret, PsiElement element, boolean addElement) {
        if (addElement && isAllowed(elementUnderCaret, element)) {
            result.add(element);
        }
        for (PsiElement child : element.getChildren()) {
            if (child instanceof PasExpr) {
                addExpression(result, elementUnderCaret, child, !child.getTextRange().equals(element.getTextRange()));
            }
        }
    }

    private boolean isAllowed(PsiElement elementUnderCaret, PsiElement element) {
        if (!element.getTextRange().contains(elementUnderCaret.getTextRange().getStartOffset())) {
            return false;
        }
        if (element instanceof PasCallExpr) {
            AtomicBoolean result = new AtomicBoolean(false);
            PasExpr callExpr = ((PasCallExpr) element).getExpr();
            if (callExpr instanceof PasReferenceExpr) {        // Filter out procedure calls
                result.set(true);
                Resolve.resolveExpr(NamespaceRec.fromElement(((PasReferenceExpr) callExpr).getFullyQualifiedIdent()),
                        new ResolveContext(PasField.TYPES_ROUTINE, true), new ResolveProcessor() {
                            @Override
                            public boolean process(PasEntityScope originalScope, PasEntityScope scope, PasField field, PasField.FieldType type) {
                                PascalNamedElement el = field.getElement();
                                if (el instanceof PascalRoutine) {
                                    result.set(!"".equals(((PascalRoutine) el).getFunctionTypeStr()));
                                    return false;
                                }
                                return true;
                            }
                        }
                );
            }
            return result.get();
        } else if (element instanceof PasReferenceExpr) {
            if (element.getParent() instanceof PasCallExpr) {  // Filter out reference expression before ()
                return false;
            } else {
                PasFullyQualifiedIdent fqn = ((PasReferenceExpr) element).getFullyQualifiedIdent();
                return fqn.getSubIdentList().size() > 1;      // Filter out non qualified names
            }
        } else if ((element instanceof PasExpr) && (element.getParent() instanceof PasParenExpr)) {  // Filter expression inside ()
            return false;
        }
        return true;
    }

}
