package com.siberika.idea.pascal.lang.references;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiComment;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReferenceBase;
import com.siberika.idea.pascal.util.ModuleUtil;
import com.siberika.idea.pascal.util.StrUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Date: 3/13/13
 * Author: George Bakhtadze
 */
class PascalCommentReference extends PsiReferenceBase<PsiComment> {
    private static final Logger LOG = Logger.getInstance(PascalCommentReference.class);
    private final String key;

    PascalCommentReference(@NotNull PsiElement element) {
        super((PsiComment) element, StrUtil.getIncludeNameRange(element.getText()));
        key = getRangeInElement() != null ? getRangeInElement().substring(element.getText()) : null;
    }

    @Nullable
    @Override
    public PsiElement resolve() {
        VirtualFile file = ModuleUtil.getIncludedFile(myElement.getProject(), myElement.getContainingFile().getVirtualFile(), key);
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
        if (!key.equals(that.key)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = key.hashCode();
        result = 31 * result + getElement().hashCode();
        return result;
    }
}
