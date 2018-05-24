package com.siberika.idea.pascal.lang.context;

import com.intellij.psi.PsiComment;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiWhiteSpace;
import com.intellij.psi.util.PsiTreeUtil;
import com.siberika.idea.pascal.lang.psi.PasArrayIndex;
import com.siberika.idea.pascal.lang.psi.PasAssignPart;
import com.siberika.idea.pascal.lang.psi.PasBlockGlobal;
import com.siberika.idea.pascal.lang.psi.PasBlockLocal;
import com.siberika.idea.pascal.lang.psi.PasCaseElse;
import com.siberika.idea.pascal.lang.psi.PasCaseItem;
import com.siberika.idea.pascal.lang.psi.PasCaseStatement;
import com.siberika.idea.pascal.lang.psi.PasClassField;
import com.siberika.idea.pascal.lang.psi.PasClassProperty;
import com.siberika.idea.pascal.lang.psi.PasClassPropertyArray;
import com.siberika.idea.pascal.lang.psi.PasClassPropertySpecifier;
import com.siberika.idea.pascal.lang.psi.PasCompoundStatement;
import com.siberika.idea.pascal.lang.psi.PasConstDeclaration;
import com.siberika.idea.pascal.lang.psi.PasConstSection;
import com.siberika.idea.pascal.lang.psi.PasExportedRoutine;
import com.siberika.idea.pascal.lang.psi.PasExpr;
import com.siberika.idea.pascal.lang.psi.PasForStatement;
import com.siberika.idea.pascal.lang.psi.PasFormalParameter;
import com.siberika.idea.pascal.lang.psi.PasFormalParameterSection;
import com.siberika.idea.pascal.lang.psi.PasHandler;
import com.siberika.idea.pascal.lang.psi.PasIfStatement;
import com.siberika.idea.pascal.lang.psi.PasIfThenStatement;
import com.siberika.idea.pascal.lang.psi.PasImplDeclSection;
import com.siberika.idea.pascal.lang.psi.PasModule;
import com.siberika.idea.pascal.lang.psi.PasNamespaceIdent;
import com.siberika.idea.pascal.lang.psi.PasRaiseStatement;
import com.siberika.idea.pascal.lang.psi.PasReferenceExpr;
import com.siberika.idea.pascal.lang.psi.PasRepeatStatement;
import com.siberika.idea.pascal.lang.psi.PasRoutineImplDecl;
import com.siberika.idea.pascal.lang.psi.PasRoutineImplDeclNested1;
import com.siberika.idea.pascal.lang.psi.PasRoutineImplDeclWoNested;
import com.siberika.idea.pascal.lang.psi.PasStatement;
import com.siberika.idea.pascal.lang.psi.PasSubIdent;
import com.siberika.idea.pascal.lang.psi.PasTryStatement;
import com.siberika.idea.pascal.lang.psi.PasTypeDecl;
import com.siberika.idea.pascal.lang.psi.PasTypeDeclaration;
import com.siberika.idea.pascal.lang.psi.PasTypeID;
import com.siberika.idea.pascal.lang.psi.PasTypeSection;
import com.siberika.idea.pascal.lang.psi.PasUnitImplementation;
import com.siberika.idea.pascal.lang.psi.PasUnitInterface;
import com.siberika.idea.pascal.lang.psi.PasUsesClause;
import com.siberika.idea.pascal.lang.psi.PasVarDeclaration;
import com.siberika.idea.pascal.lang.psi.PasVarSection;
import com.siberika.idea.pascal.lang.psi.PasWhileStatement;
import com.siberika.idea.pascal.lang.psi.PascalOperation;
import com.siberika.idea.pascal.lang.psi.PascalStructType;
import com.siberika.idea.pascal.lang.psi.impl.PasStatementImpl;
import com.siberika.idea.pascal.util.PsiUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;

public class Context {

    private static final Class[] GLOBAL_DECL = {PasUnitInterface.class, PasUnitImplementation.class, PasImplDeclSection.class, PasBlockGlobal.class};
    private static final Class[] LOCAL_DECL = {PasRoutineImplDecl.class, PasBlockLocal.class, PasRoutineImplDeclNested1.class, PasRoutineImplDeclWoNested.class};

    private final Set<CodePlace> context;
    private final CodePlace primary;
    private PsiElement position;
    private PsiElement tempPos;
    private CodePlace tempFirst;

    public Context(@Nullable PsiElement originalPos, @Nullable PsiElement ident, @Nullable PsiFile file) {
        PsiElement element;
        PsiElement origPos;
        if (ident != null) {
            element = ident;
            origPos = originalPos;
        } else {
            element = originalPos;
            origPos = null;
        }
        context = EnumSet.noneOf(CodePlace.class);
        if (element != null) {
            PsiElement prev = PsiTreeUtil.skipSiblingsBackward(element, PsiWhiteSpace.class, PsiComment.class);
            PsiElement oPrev = PsiTreeUtil.skipSiblingsBackward(originalPos, PsiWhiteSpace.class, PsiComment.class);
            System.out.println(String.format("=== oPos: %s, pos: %s, oPrev: %s, prev: %s, opar: %s, par: %s", originalPos, element, oPrev, prev, originalPos != null ? originalPos.getParent() : null, element.getParent()));

            file = file != null ? file : element.getContainingFile();
            primary = retrieveContext(element, origPos, file, context);
        } else {
            primary = CodePlace.UNKNOWN;
        }
        System.out.println(String.format("=== Context: %s, %s, %s", primary, context, position));
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

    @NotNull
    private CodePlace retrieveContext(PsiElement element, PsiElement originalPos, PsiFile file, Set<CodePlace> context) {
        if (PsiTreeUtil.findChildOfType(file, PasModule.class) == null) {
            return CodePlace.UNKNOWN;
        }
        if (element instanceof PsiComment) {
            return CodePlace.COMMENT;
        }

        position = PsiUtil.skipToExpressionParent(element);
        tempPos = position;
        originalPos = PsiUtil.skipToExpressionParent(originalPos);

        printContext(tempPos, originalPos);

        if (tempPos instanceof PasModule) {
            return CodePlace.MODULE_HEADER;
        }

        CodePlace res = CodePlace.UNKNOWN;

        if (tempPos instanceof PasStatement || tempPos instanceof PasAssignPart) {
            PsiElement expr = PsiUtil.skipToExpression(element);
            if (expr instanceof PasExpr) {
                res = CodePlace.EXPR;
                if (expr.getPrevSibling() instanceof PascalOperation) {
                    context.add(CodePlace.EXPR_AFTER_OPERATION);
                }
                if (expr instanceof PasReferenceExpr) {
                    expr = expr.getParent();
                }
                if (expr == tempPos.getFirstChild()) {
                    context.add(CodePlace.STATEMENT_START);
                }
            } else {
                res = CodePlace.STATEMENT;
            }
            if (tempPos.getParent() instanceof PasCompoundStatement) {
                List<PasStatement> statements = ((PasCompoundStatement) tempPos.getParent()).getStatementList();
                if ((statements.size() > 0) && (statements.get(0) == tempPos)) {
                    context.add(CodePlace.STATEMENT_FIRST);
                }
            }
        } else if (tempPos instanceof PasCaseItem) {
            res = CodePlace.STMT_CASE_ITEM;
        } else if (PsiUtil.isInstanceOfAny(tempPos, GLOBAL_DECL)) {
            if (originalPos instanceof PasRoutineImplDecl) {
                res = CodePlace.LOCAL_DECLARATION;
                position = originalPos;
            } else {
                res = CodePlace.GLOBAL_DECLARATION;
            }
        } else if (PsiUtil.isInstanceOfAny(tempPos, LOCAL_DECL)) {
            res = CodePlace.LOCAL_DECLARATION;
        } else if (tempPos instanceof PasUsesClause) {
            if (originalPos instanceof PasUsesClause && element.getParent() instanceof PasSubIdent && element.getParent().getParent() instanceof PasNamespaceIdent) {
                res = CodePlace.USES;
            } else {
                res = CodePlace.GLOBAL_DECLARATION;
                position = originalPos;
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

        if (tempPos instanceof PasClassPropertySpecifier) {
            addToContext(CodePlace.PROPERTY_SPECIFIER);
        }

        if (tempPos instanceof PasClassProperty) {
            position = tempPos;
            addToContext(CodePlace.DECL_PROPERTY);
        }

        if (tempPos instanceof PasClassField) {
            addToContext(CodePlace.DECL_FIELD);
        }

        if (tempPos instanceof PascalStructType) {
            addToContext(CodePlace.STRUCT);
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
            addToContext(PsiUtil.isInstanceOfAny(originalPos, LOCAL_DECL) ? CodePlace.LOCAL : CodePlace.GLOBAL);
        } else if (PsiUtil.isInstanceOfAny(tempPos, LOCAL_DECL)) {
            addToContext(CodePlace.LOCAL);
        }

        return res != CodePlace.UNKNOWN ? res : tempFirst;
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
        System.out.println(String.format("=== skipped. oPos: %s, pos: %s, oPrev: %s, prev: %s, opar: %s, par: %s, lvl: %d", originalPos, pos, oPrev, prev, originalPos != null ? originalPos.getParent() : null, pos.getParent(), level));
    }

}
