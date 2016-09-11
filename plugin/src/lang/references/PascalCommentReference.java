package com.siberika.idea.pascal.lang.references;

import com.intellij.openapi.util.Pair;
import com.intellij.openapi.util.TextRange;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiComment;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.PsiReferenceBase;
import com.intellij.psi.util.PsiUtilCore;
import com.siberika.idea.pascal.lang.lexer.PascalFlexLexerImpl;
import com.siberika.idea.pascal.lang.psi.PasTypes;
import com.siberika.idea.pascal.sdk.Define;
import com.siberika.idea.pascal.util.ModuleUtil;
import com.siberika.idea.pascal.util.StrUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

/**
 * Date: 3/13/13
 * Author: George Bakhtadze
 */
class PascalCommentReference extends PsiReferenceBase<PsiComment> {
    private final Pair<String, String> directive;

    PascalCommentReference(@NotNull PsiElement element) {
        super((PsiComment) element);
        directive = StrUtil.getDirectivePair(element.getText());
        if (directive != null) {
            setRangeInElement(TextRange.from(element.getText().indexOf(directive.second, directive.first.length()), directive.second.length()));
        }
    }

    @Nullable
    @Override
    public PsiElement resolve() {
        if (null == directive) {
            return null;
        }
        if (getElement().getNode().getElementType() == PasTypes.CT_DEFINE) {
            PascalFlexLexerImpl lexer = PascalFlexLexerImpl.processFile(myElement.getProject(), myElement.getContainingFile().getVirtualFile());
            if (lexer != null) {
                for (Map.Entry<String, Define> entry : lexer.getAllDefines().entrySet()) {
                    if (entry.getKey().equalsIgnoreCase(directive.second)) {
                        if (entry.getValue().virtualFile != null) {
                            PsiFile file = PsiManager.getInstance(myElement.getProject()).findFile(entry.getValue().virtualFile);
                            if (file != null) {
                                return PsiUtilCore.getElementAtOffset(file, entry.getValue().offset);
                            }
                        }
                    }
                }
            }
            return getElement();
        }
        VirtualFile file = ModuleUtil.getIncludedFile(myElement.getProject(), myElement.getContainingFile().getVirtualFile(), directive.second);
        return file != null ? com.intellij.psi.util.PsiUtil.getPsiFile(myElement.getProject(), file) : null;
    }

    @NotNull
    @Override
    public Object[] getVariants() {
        return EMPTY_ARRAY;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        PascalCommentReference that = (PascalCommentReference) o;

        if (!getElement().equals(that.getElement())) return false;
        if (!directive.equals(that.directive)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = directive != null ? directive.hashCode() : 0;
        result = 31 * result + getElement().hashCode();
        return result;
    }
}
