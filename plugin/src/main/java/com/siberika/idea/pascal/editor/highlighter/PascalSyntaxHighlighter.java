package com.siberika.idea.pascal.editor.highlighter;

import com.intellij.lexer.Lexer;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.siberika.idea.pascal.lang.lexer.PascalLexer;
import org.jetbrains.annotations.NotNull;

/**
 * Author: George Bakhtadze
 * Date: 12/9/12
 */
public class PascalSyntaxHighlighter extends PascalSyntaxHighlighterBase {

    final Project project;
    final VirtualFile virtualFile;

    @SuppressWarnings("deprecation")
    public PascalSyntaxHighlighter(final Project project, final VirtualFile virtualFile) {
        super();
        this.project = project;
        this.virtualFile = virtualFile;
    }

    @NotNull
    public Lexer getHighlightingLexer() {
        return new PascalLexer.SyntaxHighlightingPascalLexer(project, virtualFile);
    }

}
