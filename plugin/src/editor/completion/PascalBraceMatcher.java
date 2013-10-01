package com.siberika.idea.pascal.editor.completion;

import com.intellij.codeInsight.highlighting.BraceMatcher;
import com.intellij.openapi.editor.highlighter.HighlighterIterator;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.psi.PsiFile;
import com.intellij.psi.tree.IElementType;
import com.siberika.idea.pascal.lang.psi.PasTypes;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Author: George Bakhtadze
 * Date: 01/10/2013
 */
public class PascalBraceMatcher implements BraceMatcher {
    @Override
    public int getBraceTokenGroupId(IElementType tokenType) {
        return 0;  //To change body of implemented methods use File | Settings | File Templates.
    }

    /*
    LPAREN="("
        RPAREN=")"
        LBRACK="["
        RBRACK="]"
     */

    @Override
    public boolean isLBraceToken(HighlighterIterator iterator, CharSequence fileText, FileType fileType) {
        return (iterator.getTokenType() == PasTypes.BEGIN) || (iterator.getTokenType() == PasTypes.LBRACK) || (iterator.getTokenType() == PasTypes.LPAREN);
    }

    @Override
    public boolean isRBraceToken(HighlighterIterator iterator, CharSequence fileText, FileType fileType) {
        return (iterator.getTokenType() == PasTypes.END) || (iterator.getTokenType() == PasTypes.RBRACK) || (iterator.getTokenType() == PasTypes.RPAREN);
    }

    @Override
    public boolean isPairBraces(IElementType tokenType, IElementType tokenType2) {
        return isPairBracesInternal(tokenType, tokenType2) || isPairBracesInternal(tokenType2, tokenType);
    }

    private boolean isPairBracesInternal(IElementType tokenType, IElementType tokenType2) {
        return ((tokenType == PasTypes.BEGIN)  && (tokenType2 == PasTypes.END))
            || ((tokenType == PasTypes.LBRACK) && (tokenType2 == PasTypes.RBRACK))
            || ((tokenType == PasTypes.LPAREN) && (tokenType2 == PasTypes.RPAREN));
    }

    @Override
    public boolean isStructuralBrace(HighlighterIterator iterator, CharSequence text, FileType fileType) {
        return (iterator.getTokenType() == PasTypes.BEGIN) || (iterator.getTokenType() == PasTypes.END);
    }

    @Nullable
    @Override
    public IElementType getOppositeBraceTokenType(@NotNull IElementType type) {
        if (type == PasTypes.BEGIN)  { return PasTypes.END; }
        if (type == PasTypes.END)    { return PasTypes.BEGIN; }
        if (type == PasTypes.LBRACK) { return PasTypes.RBRACK; }
        if (type == PasTypes.RBRACK) { return PasTypes.LBRACK; }
        if (type == PasTypes.LPAREN) { return PasTypes.RPAREN; }
        if (type == PasTypes.RPAREN) { return PasTypes.LPAREN; }
        return null;
    }

    @Override
    public boolean isPairedBracesAllowedBeforeType(@NotNull IElementType lbraceType, @Nullable IElementType contextType) {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public int getCodeConstructStart(PsiFile file, int openingBraceOffset) {
        return 0;  //To change body of implemented methods use File | Settings | File Templates.
    }
}
