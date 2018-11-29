package com.siberika.idea.pascal.lang.context;

import com.intellij.psi.PsiComment;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiErrorElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiWhiteSpace;
import com.intellij.psi.util.PsiTreeUtil;
import com.siberika.idea.pascal.lang.psi.*;
import com.siberika.idea.pascal.lang.psi.impl.PasStatementImpl;
import com.siberika.idea.pascal.util.PsiUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.EnumSet;
import java.util.Set;

public class Context {

    private static final Class[] GLOBAL_DECL = {PasUnitInterface.class, PasUnitImplementation.class, PasImplDeclSection.class, PasBlockGlobal.class};
    private static final Class[] LOCAL_DECL = {PasRoutineImplDecl.class, PasBlockLocal.class, PasRoutineImplDeclNested1.class, PasRoutineImplDeclWoNested.class};
    private static final Class[] EXPRESION_CLASSES = {PasExpr.class, PasArgumentList.class, PasIndexList.class, PasExpression.class};

    private final Set<CodePlace> context;
    private final CodePlace primary;
    @Nullable
    private final PsiElement dummyIdent;
    private PsiElement position;
    private PsiElement tempPos;
    private PascalNamedElement namedElement;
    private CodePlace tempFirst;
    private final PsiFile file;

    public Context(@Nullable PsiElement originalPos, @Nullable PsiElement dummyIdent, @Nullable PsiFile file) {
        this.dummyIdent = dummyIdent;
        PsiElement element;
        PsiElement origPos;
        if (dummyIdent != null) {
            element = dummyIdent;
            origPos = originalPos;
        } else {
            element = originalPos;
            origPos = null;
        }
        context = EnumSet.noneOf(CodePlace.class);
        if (element != null) {
            file = file != null ? file : element.getContainingFile();
            primary = retrieveContext(element, origPos, file, context);
        } else {
            primary = CodePlace.UNKNOWN;
        }
        this.file = file;
//        System.out.println(String.format("=== Context: %s, %s, %s", primary, context, position));
    }

    public boolean contains(CodePlace place) {
        return context.contains(place);
    }

    public CodePlace getPrimary() {
        return primary;
    }

    public PsiElement getPosition() {
        return position;
    }

    @Nullable
    public PsiElement getDummyIdent() {
        return dummyIdent;
    }

    public PascalNamedElement getNamedElement() {
        return namedElement;
    }

    public PsiFile getFile() {
        return file;
    }

    @NotNull
    private CodePlace retrieveContext(PsiElement element, PsiElement originalPos, PsiFile file, Set<CodePlace> context) {
        if (!PsiUtil.isElementUsable(element)) {
            return CodePlace.UNKNOWN;
        }
        if (PsiTreeUtil.findChildOfType(file, PasModule.class) == null) {
            return CodePlace.UNKNOWN;
        }
        if (element instanceof PsiComment) {
            return CodePlace.COMMENT;
        }

        position = PsiUtil.skipToExpressionParent(element);
        tempPos = position;
        PsiElement originalExprParent = PsiUtil.skipToExpressionParent(originalPos);

        printContext(tempPos, originalExprParent);

        if (tempPos instanceof PasModule) {
            return CodePlace.MODULE_HEADER;
        }

        checkIdent(element, originalPos);

        CodePlace res = CodePlace.UNKNOWN;

        if ((tempPos instanceof PasStatement) || (tempPos instanceof PasAssignPart) || (tempPos instanceof PasArgumentList)
                || (tempPos instanceof PasIndexList) || (originalExprParent instanceof PasRangeBound)) {
            PsiElement expr = skipToExpression(originalExprParent instanceof PasRangeBound ? originalPos : element);
            if ((expr instanceof PasExpr) || (expr instanceof PasExpression)) {
                res = CodePlace.EXPR;
                if (expr.getPrevSibling() instanceof PascalOperation) {
                    context.add(CodePlace.EXPR_AFTER_OPERATION);
                }
            } else {
                res = CodePlace.STATEMENT;
            }
            if (tempPos instanceof PasCaseStatement) {
                context.add(CodePlace.ASSIGN_RIGHT);
            } else if ((originalExprParent instanceof PasStatement) && (PsiUtil.findImmChildOfAnyType(tempPos, PasAssignPart.class) != null)) {
                context.add(CodePlace.ASSIGN_LEFT);
            }
        } else if (tempPos instanceof PasCaseItem) {
            res = CodePlace.STMT_CASE_ITEM;
        } else if (PsiUtil.isInstanceOfAny(tempPos, GLOBAL_DECL)) {
            if (originalExprParent instanceof PasRoutineImplDecl) {
                res = CodePlace.LOCAL_DECLARATION;
                position = originalExprParent;
            } else {
                res = CodePlace.GLOBAL_DECLARATION;
            }
        } else if (PsiUtil.isInstanceOfAny(tempPos, LOCAL_DECL)) {
            res = CodePlace.LOCAL_DECLARATION;
            if (originalExprParent instanceof PasRoutineImplDecl) {
                position = originalExprParent;
            }
        } else if (tempPos instanceof PasUsesClause) {
            if (originalExprParent instanceof PasUsesClause && element.getParent() instanceof PasSubIdent && element.getParent().getParent() instanceof PasNamespaceIdent) {
                res = CodePlace.USES;
            } else {
                res = CodePlace.GLOBAL_DECLARATION;
                position = originalExprParent;
            }
        } else if (tempPos instanceof PasFormalParameter) {
            res = CodePlace.FORMAL_PARAMETER;
        }

        tempFirst = CodePlace.UNKNOWN;

        // ============== Secondary ===============

        if (tempPos instanceof PasAssignPart) {
            addToContext(CodePlace.ASSIGN_RIGHT);
        }
        if (tempPos.getClass() == PasStatementImpl.class) {
            addToContext(CodePlace.STATEMENT);
        }
        if (tempPos instanceof PasIfThenStatement) {
            addToContext(CodePlace.STMT_IF_THEN);
        }
        if (tempPos instanceof PasIfStatement) {
            addToContext(CodePlace.STMT_IF);
        }
        if (tempPos instanceof PasForStatement) {
            addToContext(CodePlace.STMT_FOR);
        }
        if (tempPos instanceof PasWhileStatement) {
            addToContext(CodePlace.STMT_WHILE);
        }
        if (tempPos instanceof PasRepeatStatement) {
            addToContext(CodePlace.STMT_REPEAT);
        }
        if (tempPos instanceof PasTryStatement) {
            addToContext(CodePlace.STMT_TRY);
        }
        if (tempPos instanceof PasRaiseStatement) {
            addToContext(CodePlace.STMT_RAISE);
        }
        if (tempPos instanceof PasHandler) {
            addToContext(CodePlace.STMT_EXCEPT);
        }
        if (tempPos instanceof PasCaseStatement) {
            addToContext(CodePlace.STMT_CASE);
        }
        if (tempPos instanceof PasCaseItem) {
            addToContext(CodePlace.STMT_CASE_ITEM);
        }
        if (tempPos instanceof PasCaseElse) {
            addToContext(CodePlace.STMT_CASE_ELSE);
        }

        if (tempPos instanceof PasClassPropertyArray) {
            position = tempPos;
            addToContext(CodePlace.PROPERTY_ARRAY_PARAM);
        } else if (tempPos instanceof PasFormalParameterSection) {
            position = tempPos;
            tempPos = tempPos.getParent();
        }

        if (tempPos instanceof PasFormalParameter) {
            addToContext(CodePlace.FORMAL_PARAMETER);
        }
        if (tempPos instanceof PasExportedRoutine) {
            position = tempPos;
            context.add(CodePlace.ROUTINE_DECL);
            addToContext(CodePlace.ROUTINE_HEADER);
        } else if (tempPos instanceof PasRoutineImplDecl) {
            position = tempPos;
            context.add(CodePlace.ROUTINE);
            addToContext(CodePlace.ROUTINE_HEADER);
        }

        if (tempPos instanceof PasTypeID) {
            addToContext(CodePlace.TYPE_ID);
        }
        if (tempPos instanceof PasTypeDecl) {
            tempPos = tempPos.getParent();
        }

        if (tempPos instanceof PasArrayIndex) {
            addToContext(CodePlace.ARRAY_INDEX);
            tempPos = tempPos.getParent();
        }

        if (tempPos instanceof PasClassProperty) {
            position = tempPos;
            addToContext(CodePlace.DECL_PROPERTY);
        }

        if (tempPos instanceof PasClassPropertySpecifier) {
            position = tempPos;
            addToContext(CodePlace.PROPERTY_SPECIFIER);
        }

        if (tempPos instanceof PasClassField) {
            addToContext(CodePlace.DECL_FIELD);
        }

        if (tempPos instanceof PascalStructType) {
            addToContext(CodePlace.STRUCT);
        }

        if (tempPos instanceof PasClassParent) {
            addToContext(CodePlace.STRUCT_PARENT);
        }

        if (tempPos instanceof PasVarDeclaration) {
            addToContext(CodePlace.DECL_VAR);
        }
        if (tempPos instanceof PasConstDeclaration) {
            addToContext(CodePlace.DECL_CONST);
        }
        if (tempPos instanceof PasTypeDeclaration) {
            addToContext(CodePlace.DECL_TYPE);
        }

        if (tempPos instanceof PasVarSection) {
            addToContext(CodePlace.SECTION_VAR);
        }
        if (tempPos instanceof PasConstSection) {
            addToContext(CodePlace.SECTION_CONST);
        }
        if (tempPos instanceof PasTypeSection) {
            addToContext(CodePlace.SECTION_TYPE);
        }
        if (tempPos instanceof PasUnitInterface) {
            addToContext(CodePlace.INTERFACE);
        }
        if (PsiUtil.isInstanceOfAny(tempPos, GLOBAL_DECL)) {
            addToContext(PsiUtil.isInstanceOfAny(originalExprParent, LOCAL_DECL) ? CodePlace.LOCAL : CodePlace.GLOBAL);
        } else if (PsiUtil.isInstanceOfAny(tempPos, LOCAL_DECL)) {
            addToContext(CodePlace.LOCAL);
        }

        return res != CodePlace.UNKNOWN ? res : tempFirst;
    }

    private PsiElement skipToExpression(PsiElement element) {
        PsiElement pos = PsiTreeUtil.skipParentsOfType(element, PascalNamedElement.class,
                PasReferenceExpr.class, PasDereferenceExpr.class,
                PsiWhiteSpace.class, PsiErrorElement.class);
        PsiElement res = pos;
        PsiElement parent = pos != null ? pos.getParent() : null;
        boolean contextModified = false;
        while (PsiUtil.isInstanceOfAny(parent, EXPRESION_CLASSES)) {
            if (!contextModified) {
                if (pos instanceof PasArgumentList) {
                    context.add(CodePlace.EXPR_ARGUMENT);
                    contextModified = true;
                }
                if (pos instanceof PasParenExpr) {
                    context.add(CodePlace.EXPR_PAREN);
                    contextModified = true;
                }
                if (pos instanceof PasSetExpr) {
                    context.add(CodePlace.EXPR_SET);
                    contextModified = true;
                }
                if (pos instanceof PasIndexList) {
                    context.add(CodePlace.EXPR_INDEX);
                    contextModified = true;
                }
                if ((pos instanceof PasConstExpressionOrd) || (pos instanceof PasConstExpression)) {
                    context.add(CodePlace.CONST_EXPRESSION);
                }
            }
            res = pos;
            pos = pos != null ? pos.getParent() : null;
            parent = pos != null ? pos.getParent() : null;
        }
        return res;
    }

    private void checkIdent(PsiElement element, PsiElement originalPos) {
        PsiElement pos;
        if (originalPos instanceof PascalNamedElement) {
            pos = originalPos;
        } else if ((originalPos != null) && (originalPos.getParent() instanceof PasStringFactor)) {
            pos = originalPos.getParent().getParent();
        } else if ((originalPos != null) && originalPos.getParent() instanceof PascalNamedElement) {
            pos = originalPos.getParent();
        } else {
            pos = element.getParent();
        }
        if (pos instanceof PascalNamedElement) {
            namedElement = (PascalNamedElement) pos;
            context.add(CodePlace.NAMED_IDENT);
        } else if (pos instanceof PasLiteralExpr) {
            context.add(CodePlace.STRING);
        } else {
            return;
        }
        while (PsiUtil.isInstanceOfAny(pos, PasSubIdent.class, PascalQualifiedIdent.class, PasReferenceExpr.class)) {
            PsiElement par = pos.getParent();
            if (!(par.getChildren()[0] == pos)) {
                return;
            }
            pos = par;
        }
        context.add(CodePlace.FIRST_IN_NAME);
        while (PsiUtil.isInstanceOfAny(pos, PasExpr.class, PasIndexList.class, PasArgumentList.class)) {
            PsiElement par = pos.getParent();
            if (!(par.getChildren()[0] == pos)) {
                return;
            }
            pos = par;
        }
        context.add(CodePlace.FIRST_IN_EXPR);
    }

    private void addToContext(CodePlace place) {
        context.add(place);
        tempFirst = tempFirst != CodePlace.UNKNOWN ? tempFirst : place;
        tempPos = tempPos.getParent();
    }

    private void printContext(PsiElement pos, PsiElement originalPos) {
        PsiElement expr = PsiUtil.skipToExpression(originalPos);
        PsiElement prev = PsiTreeUtil.skipSiblingsBackward(pos, PsiWhiteSpace.class, PsiComment.class);
        PsiElement oPrev = PsiTreeUtil.skipSiblingsBackward(originalPos, PsiWhiteSpace.class, PsiComment.class);
        int level = PsiUtil.getElementLevel(originalPos);
//        System.out.println(String.format("=== skipped. oPos: %s, pos: %s, oPrev: %s, prev: %s, opar: %s, par: %s, lvl: %d", originalPos, pos, oPrev, prev, originalPos != null ? originalPos.getParent() : null, pos.getParent(), level));
    }

    public boolean withinBraces() {
        return contains(CodePlace.EXPR_ARGUMENT) || contains(CodePlace.EXPR_PAREN) || contains(CodePlace.EXPR_INDEX) || contains(CodePlace.EXPR_SET);
    }
}
