package com.siberika.idea.pascal.lang;

import com.intellij.codeInsight.editorActions.moveUpDown.LineMover;
import com.intellij.codeInsight.editorActions.moveUpDown.LineRange;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.LogicalPosition;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiComment;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.util.PsiTreeUtil;
import com.siberika.idea.pascal.lang.parser.PascalFile;
import com.siberika.idea.pascal.lang.psi.PasBlockGlobal;
import com.siberika.idea.pascal.lang.psi.PasBlockLocal;
import com.siberika.idea.pascal.lang.psi.PasClassMethodResolution;
import com.siberika.idea.pascal.lang.psi.PasClassProperty;
import com.siberika.idea.pascal.lang.psi.PasCompoundStatement;
import com.siberika.idea.pascal.lang.psi.PasConstDeclaration;
import com.siberika.idea.pascal.lang.psi.PasConstSection;
import com.siberika.idea.pascal.lang.psi.PasExportsSection;
import com.siberika.idea.pascal.lang.psi.PasHandler;
import com.siberika.idea.pascal.lang.psi.PasImplDeclSection;
import com.siberika.idea.pascal.lang.psi.PasLabelDeclSection;
import com.siberika.idea.pascal.lang.psi.PasProcBodyBlock;
import com.siberika.idea.pascal.lang.psi.PasStatement;
import com.siberika.idea.pascal.lang.psi.PasTypeDeclaration;
import com.siberika.idea.pascal.lang.psi.PasTypeSection;
import com.siberika.idea.pascal.lang.psi.PasVarDeclaration;
import com.siberika.idea.pascal.lang.psi.PasVarSection;
import com.siberika.idea.pascal.lang.psi.PascalRoutine;
import com.siberika.idea.pascal.util.DocUtil;
import com.siberika.idea.pascal.util.PsiUtil;
import org.jetbrains.annotations.NotNull;

public class PascalStatementMover extends LineMover {
    @Override
    public boolean checkAvailable(@NotNull Editor editor, @NotNull PsiFile file, @NotNull MoveInfo info, boolean down) {
        if ((file instanceof PascalFile) && super.checkAvailable(editor, file, info, down)) {
            info.indentTarget = false;
            PsiElement el = getFirstElementOnLine(editor, file, info.toMove.startLine);
            if (isMovable(el)) {
                TextRange outerRange = getOuterRange(el);
                LineRange range = findElementRange(editor, file, info.toMove.startLine, el);
                LineRange range2;
                boolean crossSection;
                if (down) {
                    range2 = findElementRange(editor, file, range.endLine, getFirstElementOnLine(editor, file, range.endLine));
                    crossSection = editor.getDocument().getLineNumber(outerRange.getEndOffset()) < range2.endLine;
                } else {
                    range2 = findElementRange(editor, file, range.startLine-1, getLastElementOnLine(editor, file, range.startLine-1));
                    crossSection = editor.getDocument().getLineNumber(outerRange.getStartOffset()) >= range2.startLine;
                }
                if (!crossSection) {
                    info.toMove = range;
                    info.toMove2 = range2;
                }
                expandRanges(editor.getDocument(), file, info, down);
            }
            return true;
        }
        return false;
    }

    private TextRange getOuterRange(PsiElement el) {
        TextRange range = el.getTextRange();
        PsiElement parent = el.getParent();
        if (parent != null) {
            PsiElement start = parent;
            PsiElement end = parent;
            start = PsiTreeUtil.skipSiblingsBackward(start, PsiUtil.ELEMENT_WS_COMMENTS);
            end = PsiTreeUtil.skipSiblingsForward(end, PsiUtil.ELEMENT_WS_COMMENTS);
            range = TextRange.create(start != null ? start.getTextRange().getEndOffset() : parent.getTextRange().getStartOffset(),
                    end != null ? end.getTextRange().getStartOffset() : parent.getTextRange().getEndOffset());
        }
        return range;
    }

    private boolean isMovable(PsiElement el) {
        return PsiUtil.isInstanceOfAny(el, PasVarSection.class, PasClassProperty.class, PasConstSection.class, PasTypeSection.class,
                PasVarDeclaration.class, PasConstDeclaration.class, PasTypeDeclaration.class,
                PasHandler.class,
                PasClassMethodResolution.class, PascalRoutine.class,
                PasExportsSection.class, PasLabelDeclSection.class,
                PasStatement.class, PsiComment.class) && !(el instanceof PasCompoundStatement);
    }

    private void expandRanges(Document document, PsiFile file, MoveInfo info, boolean down) {
        if ((down || (info.toMove.startLine > info.toMove2.endLine)) && (info.toMove.startLine > 0) && DocUtil.isLineEmpty(document, file, info.toMove.startLine - 1)) {
            info.toMove = new LineRange(info.toMove.startLine-1, info.toMove.endLine);
        }
        if ((info.toMove2.startLine > 0) && DocUtil.isLineEmpty(document, file, info.toMove2.startLine - 1)) {
            info.toMove2 = new LineRange(info.toMove2.startLine-1, info.toMove2.endLine);
        }
    }

    @Override
    public void afterMove(@NotNull Editor editor, @NotNull PsiFile file, @NotNull MoveInfo info, boolean down) {
        super.afterMove(editor, file, info, down);
        file.subtreeChanged();
    }

    private LineRange findElementRange(Editor editor, PsiFile file, int startLine, PsiElement el) {
        int endLine = startLine + 1;
        if (isMovable(el)) {
            TextRange range = el.getTextRange();
            TextRange commentRange = PascalDocumentationProvider.findElementCommentRange(file, el);
            if (commentRange != null) {
                range = commentRange.union(range);
            }
            Document d = editor.getDocument();
            if (!DocUtil.isSingleLine(d, range)) {
                startLine = d.getLineNumber(range.getStartOffset());
                endLine = d.getLineNumber(range.getEndOffset()) + 1;
            }
        }
        return new LineRange(startLine, endLine);
    }

    private PsiElement getFirstElementOnLine(Editor editor, PsiFile file, int line) {
        int startOffset = editor.logicalPositionToOffset(new LogicalPosition(line, 0));
        PsiElement el = file.findElementAt(startOffset);
        if (PsiTreeUtil.instanceOf(el, PsiUtil.ELEMENT_WS_COMMENTS)) {
            el = PsiTreeUtil.skipSiblingsForward(el, PsiUtil.ELEMENT_WS_COMMENTS);
        }
        el = el != null ? findTopmostStartingHere(el) : null;
        return el;
    }

    private PsiElement getLastElementOnLine(Editor editor, PsiFile file, int line) {
        Document d = editor.getDocument();
        int endOffset = d.getLineEndOffset(line);
        PsiElement el = file.findElementAt(endOffset);
        if (PsiTreeUtil.instanceOf(el, PsiUtil.ELEMENT_WS_COMMENTS)) {
            el = PsiTreeUtil.skipSiblingsBackward(el, PsiUtil.ELEMENT_WS_COMMENTS);
        }
        el = el != null ? findTopmostEndingHere(el) : null;
        return el;
    }

    private PsiElement findTopmostStartingHere(PsiElement el) {
        while ((el.getParent() != null) && !isStopper(el.getParent())
                && (el.getParent().getTextRange().getStartOffset() == el.getTextRange().getStartOffset())) {
            el = el.getParent();
        }
        return el;
    }

    private PsiElement findTopmostEndingHere(PsiElement el) {
        while ((el.getParent() != null) && (el.getParent().getTextRange().getEndOffset() == el.getTextRange().getEndOffset())) {
            el = el.getParent();
        }
        return el;
    }

    private boolean isStopper(PsiElement parent) {
        return PsiUtil.isInstanceOfAny(parent, PsiFile.class, PasBlockLocal.class, PasBlockGlobal.class, PasImplDeclSection.class, PasProcBodyBlock.class);
    }
}
