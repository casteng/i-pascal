package com.siberika.idea.pascal.lang;

import com.intellij.codeInsight.editorActions.moveUpDown.LineMover;
import com.intellij.codeInsight.editorActions.moveUpDown.LineRange;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.LogicalPosition;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.util.PsiTreeUtil;
import com.siberika.idea.pascal.lang.psi.PasBlockGlobal;
import com.siberika.idea.pascal.lang.psi.PasImplDeclSection;
import com.siberika.idea.pascal.util.DocUtil;
import com.siberika.idea.pascal.util.PsiUtil;
import org.jetbrains.annotations.NotNull;

public class PascalStatementMover extends LineMover {
    @Override
    public boolean checkAvailable(@NotNull Editor editor, @NotNull PsiFile file, @NotNull MoveInfo info, boolean down) {
        if (super.checkAvailable(editor, file, info, down)) {
            info.indentTarget = false;
            PsiElement el = getFirstElementOnLine(editor, file, info.toMove.startLine);
            Document d = editor.getDocument();
            int endLine = info.toMove.endLine;
            if ((el != null) && !DocUtil.isSingleLine(editor.getDocument(), el)) {
                endLine = d.getLineNumber(el.getTextRange().getEndOffset()) + 1;
            }
            info.toMove = new LineRange(info.toMove.startLine, endLine);
            if (down) {
                info.toMove2 = findLineRangeDown(editor, file, endLine);
            } else {
                info.toMove2 = findLineRangeUp(editor, file, info.toMove2.startLine);
            }
            expandRanges(editor.getDocument(), file, info, down);
            return true;
        } else {
            return false;
        }
    }

    private void expandRanges(Document document, PsiFile file, MoveInfo info, boolean down) {
        if ((down || (info.toMove.startLine > info.toMove2.endLine)) && DocUtil.isLineEmpty(document, file, info.toMove.startLine - 1)) {
            info.toMove = new LineRange(info.toMove.startLine-1, info.toMove.endLine);
        }
        if (DocUtil.isLineEmpty(document, file, info.toMove2.startLine - 1)) {
            info.toMove2 = new LineRange(info.toMove2.startLine-1, info.toMove2.endLine);
        }
    }

    @Override
    public void afterMove(@NotNull Editor editor, @NotNull PsiFile file, @NotNull MoveInfo info, boolean down) {
        super.afterMove(editor, file, info, down);
        DocUtil.reparsePsi(file.getProject(), file.getVirtualFile());
    }

    private LineRange findLineRangeUp(Editor editor, PsiFile file, int startLine) {
        int endLine = startLine + 1;
        PsiElement el = getLastElementOnLine(editor, file, startLine);
        Document d = editor.getDocument();
        if ((el != null) && !DocUtil.isSingleLine(editor.getDocument(), el)) {
            startLine = d.getLineNumber(el.getTextRange().getStartOffset());
            endLine = d.getLineNumber(el.getTextRange().getEndOffset()) + 1;
        }
        return new LineRange(startLine, endLine);
    }

    private LineRange findLineRangeDown(Editor editor, PsiFile file, int startLine) {
        int endLine = startLine + 1;
        PsiElement el = getFirstElementOnLine(editor, file, startLine);
        Document d = editor.getDocument();
        if ((el != null) && !DocUtil.isSingleLine(editor.getDocument(), el)) {
            startLine = d.getLineNumber(el.getTextRange().getStartOffset());
            endLine = d.getLineNumber(el.getTextRange().getEndOffset()) + 1;
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
        return PsiUtil.isInstanceOfAny(parent, PasBlockGlobal.class, PasImplDeclSection.class);
    }
}
