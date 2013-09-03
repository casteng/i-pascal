package com.siberika.idea.pascal.editor.highlighter;

import com.intellij.openapi.fileTypes.SyntaxHighlighter;
import com.intellij.openapi.fileTypes.SyntaxHighlighterFactory;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;

/**
 * Author: George Bakhtadze
 * Date: 12/5/12
 */
public class PascalSyntaxHighlighterFactory extends SyntaxHighlighterFactory {
    private SyntaxHighlighter myValue;

    @NotNull
    public final SyntaxHighlighter getSyntaxHighlighter(final Project project, final VirtualFile virtualFile) {
        if (myValue == null) {
            myValue = new PascalSyntaxHighlighter(project, virtualFile);
        }
        return myValue;
    }

}