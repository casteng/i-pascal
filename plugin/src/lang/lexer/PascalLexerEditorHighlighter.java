package com.siberika.idea.pascal.lang.lexer;

import com.intellij.lexer.FlexLexer;
import com.intellij.lexer.Lexer;
import com.intellij.openapi.editor.colors.EditorColorsScheme;
import com.intellij.openapi.editor.event.DocumentEvent;
import com.intellij.openapi.editor.ex.util.LexerEditorHighlighter;
import com.intellij.openapi.fileTypes.SyntaxHighlighter;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Author: George Bakhtadze
 * Date: 28/08/2013
 */
public class PascalLexerEditorHighlighter extends LexerEditorHighlighter {

    public PascalLexerEditorHighlighter(@NotNull SyntaxHighlighter highlighter, @NotNull EditorColorsScheme scheme, @Nullable Project project, @Nullable VirtualFile virtualFile) {
        super(highlighter, scheme);
        PascalFlexLexerImpl pascalFlexLexer = getPascalFlexLexerImpl();
    }

    @Override
    public synchronized void documentChanged(DocumentEvent e) {
        super.documentChanged(e);

        if (getDocument() != null) {
            // reset highlighting lexer cache
            //noinspection ConstantConditions
            setText(getDocument().getCharsSequence());
            // clear conditional defines
            PascalFlexLexerImpl pascalFlexLexer = getPascalFlexLexerImpl();
        }
    }

    public PascalFlexLexerImpl getPascalFlexLexerImpl() {
        Lexer lexer = getLexer();
        if (lexer instanceof PascalLexer) {
            FlexLexer flexLexer = ((PascalLexer) lexer).getFlexLexer();
            if (flexLexer instanceof PascalFlexLexerImpl) {
                return (PascalFlexLexerImpl) flexLexer;
            }
        }
        return null;
    }
}
