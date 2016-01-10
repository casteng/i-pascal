package com.siberika.idea.pascal.lang.lexer;

import com.intellij.lang.Language;
import com.intellij.lang.PerFileMappings;
import com.intellij.openapi.editor.colors.EditorColorsScheme;
import com.intellij.openapi.editor.highlighter.EditorHighlighter;
import com.intellij.openapi.fileTypes.EditorHighlighterProvider;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.fileTypes.SyntaxHighlighter;
import com.intellij.openapi.fileTypes.SyntaxHighlighterFactory;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.siberika.idea.pascal.DCUFileType;
import com.siberika.idea.pascal.PPUFileType;
import com.siberika.idea.pascal.PascalFileType;
import com.siberika.idea.pascal.PascalLanguage;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Method;

/**
 * Author: George Bakhtadze
 * Date: 28/08/2013
 */
public class PascalEditorHighlighterProvider implements EditorHighlighterProvider {
    @Override
    public EditorHighlighter getEditorHighlighter(@Nullable Project project, @NotNull FileType fileType, @Nullable VirtualFile virtualFile, @NotNull EditorColorsScheme colors) {
        if ((fileType == PascalFileType.INSTANCE) || (fileType == PPUFileType.INSTANCE) || (fileType == DCUFileType.INSTANCE)
                || isPascalScratchFile(virtualFile)) {
            SyntaxHighlighter syntaxHighlighter = SyntaxHighlighterFactory.getSyntaxHighlighter(fileType, project, virtualFile);
            if (syntaxHighlighter != null) {
                return new PascalLexerEditorHighlighter(syntaxHighlighter, colors, project, virtualFile);
            }
        }
        return null;
    }

    private boolean isPascalScratchFile(VirtualFile virtualFile) {
        try {
            // Issue: #14 scratch files have to be matched differently
            //ScratchFileService fileService = ScratchFileService.getInstance();
            //PerFileMappings<Language> scratchesMapping = fileService.getScratchesMapping();
            //Language language = scratchesMapping.getMapping(file);
            //doAccept = language instanceof MultiMarkdownLanguage;

            // Issue: #15 class not found ScratchFileService, so we take care of it through reflection
            Class<?> ScratchFileService = Class.forName("com.intellij.ide.scratch.ScratchFileService");
            Method getInstance = ScratchFileService.getMethod("getInstance");
            Method getScratchesMapping = ScratchFileService.getMethod("getScratchesMapping");
            Object fileService = getInstance.invoke(ScratchFileService);
            PerFileMappings<Language> mappings = (PerFileMappings<Language>) getScratchesMapping.invoke(fileService);
            Language language = mappings.getMapping(virtualFile);
            return language instanceof PascalLanguage;
        } catch (Exception ex) {
            return false;
        }
    }
}
