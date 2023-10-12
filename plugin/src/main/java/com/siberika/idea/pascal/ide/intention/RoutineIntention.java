package com.siberika.idea.pascal.ide.intention;

import com.intellij.codeInsight.intention.BaseElementAtCaretIntentionAction;
import com.intellij.lang.ASTNode;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Pair;
import com.intellij.psi.PsiComment;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiErrorElement;
import com.intellij.psi.PsiWhiteSpace;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.tree.TokenSet;
import com.intellij.psi.util.PsiTreeUtil;
import com.siberika.idea.pascal.ide.actions.SectionToggle;
import com.siberika.idea.pascal.lang.psi.PasCustomAttributeDecl;
import com.siberika.idea.pascal.lang.psi.PasProcBodyBlock;
import com.siberika.idea.pascal.lang.psi.PasRoutineImplDecl;
import com.siberika.idea.pascal.lang.psi.PasTypeDecl;
import com.siberika.idea.pascal.lang.psi.PasTypes;
import com.siberika.idea.pascal.lang.psi.PascalExportedRoutine;
import com.siberika.idea.pascal.lang.psi.PascalRoutine;
import com.siberika.idea.pascal.lang.psi.impl.PasElementFactory;
import org.jetbrains.annotations.Nullable;

abstract class RoutineIntention extends BaseElementAtCaretIntentionAction {

    private static final TokenSet TOKENS_ATTRIBUTE = TokenSet.create(PasTypes.COMMA, PasTypes.LBRACK, PasTypes.RBRACK, PasTypes.LPAREN, PasTypes.RPAREN);

    PascalRoutine getTargetRoutine(Editor editor, PsiElement element) {
        PascalRoutine routine = getRoutineHeader(editor, element);
        PsiElement target = SectionToggle.getRoutineTarget(routine);
        return target instanceof PascalRoutine ? (PascalRoutine) target : null;
    }

    PascalRoutine getRoutineHeader(Editor editor, PsiElement element) {
        PascalRoutine routine = PsiTreeUtil.getParentOfType(element, PascalRoutine.class);
        if (routine instanceof PascalExportedRoutine) {
            return routine;
        } else if (routine instanceof PasRoutineImplDecl) {
            PasProcBodyBlock block = ((PasRoutineImplDecl) routine).getProcBodyBlock();
            Integer blockOffs = block != null ? block.getTextOffset() : null;
            return (null == blockOffs) || (editor.getCaretModel().getOffset() < blockOffs) ? routine : null;
        } else {
            return null;
        }
    }

    void changeToProcedure(Project project, PascalRoutine routine) {
        clearReturnType(routine);
        switchKeyword(project, routine, PasTypes.FUNCTION, "procedure");
    }

    void switchKeyword(Project project, PascalRoutine routine, IElementType keywordType, String newKeyword) {
        ASTNode routineKey = routine.getNode().findChildByType(keywordType);
        if (routineKey != null) {
            routine.getNode().replaceChild(routineKey, PasElementFactory.createLeafFromText(project, newKeyword).getNode());
        }
    }

    void clearReturnType(PascalRoutine routine) {
        Pair<PsiElement, PsiElement> returnType = findReturnType(routine);
        if (returnType != null) {
            routine.deleteChildRange(returnType.first, returnType.second);
        }
    }

    Pair<PsiElement, PsiElement> findReturnType(@Nullable PascalRoutine routine) {
        ASTNode typeStartNode = routine != null ? routine.getNode().findChildByType(PasTypes.COLON) : null;
        if (typeStartNode != null) {
            PsiElement typeStart = typeStartNode.getPsi();
            PsiElement typeEnd = typeStart;
            do {
                typeEnd = typeEnd.getNextSibling();
                if (typeEnd instanceof PasTypeDecl) {
                    typeEnd = typeEnd.getNextSibling();
                    break;
                }
            } while (belongsToReturnType(typeEnd));
            return typeStart != typeEnd.getPrevSibling() ? Pair.create(typeStart, typeEnd.getPrevSibling()) : null;
        }
        return null;
    }

    private boolean belongsToReturnType(PsiElement element) {
        if (null == element) {
            return false;
        }
        if (PsiTreeUtil.instanceOf(element, PasTypeDecl.class, PasCustomAttributeDecl.class, PsiComment.class)) {
            return true;
        }
        if (element instanceof PsiWhiteSpace) {
            return !element.textContains('\n');
        }
        if (element instanceof PsiErrorElement) {
            return !";".equals(element.getText());
        }
        return TOKENS_ATTRIBUTE.contains(element.getNode().getElementType());
    }

}
