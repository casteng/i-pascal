package com.siberika.idea.pascal.lang.compiled;

import com.intellij.lang.Language;
import com.intellij.openapi.fileTypes.ContentBasedClassFileProcessor;
import com.intellij.openapi.fileTypes.SyntaxHighlighter;
import com.intellij.openapi.fileTypes.SyntaxHighlighterFactory;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.siberika.idea.pascal.PPUFileType;
import com.siberika.idea.pascal.PascalLanguage;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Author: George Bakhtadze
 * Date: 13/11/2013
 */
public class ContentBasedClassFileProcessorPPU implements ContentBasedClassFileProcessor {
    @NotNull
    @Override
    public SyntaxHighlighter createHighlighter(Project project, VirtualFile vFile) {
        return SyntaxHighlighterFactory.getSyntaxHighlighter(PascalLanguage.INSTANCE, project, vFile);
    }

    @Override
    public boolean isApplicable(Project project, VirtualFile vFile) {
        return vFile.getFileType() == PPUFileType.INSTANCE;
    }

    @NotNull
    @Override
    public String obtainFileText(Project project, VirtualFile file) {
        return PPUFileDecompiler.decompileText(file);
    }

    @Nullable
    @Override
    public Language obtainLanguageForFile(VirtualFile file) {
        return null;//"ppu".equalsIgnoreCase(file.getExtension()) ? PascalLanguage.INSTANCE : null;
    }
}
