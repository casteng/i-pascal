package com.siberika.idea.pascal.lang.parser.impl;

import com.intellij.extapi.psi.PsiFileBase;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.FileViewProvider;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiNameIdentifierOwner;
import com.siberika.idea.pascal.PascalFileType;
import com.siberika.idea.pascal.PascalLanguage;
import com.siberika.idea.pascal.lang.parser.PascalFile;
import com.siberika.idea.pascal.util.PsiUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Author: George Bakhtadze
 * Date: 12/9/12
 */
public class PascalFileImpl extends PsiFileBase implements PascalFile, PsiNameIdentifierOwner {
    volatile private PsiElement cachedSection;

    public PascalFileImpl(@NotNull FileViewProvider viewProvider) {
        super(viewProvider, PascalLanguage.INSTANCE);
    }

    @NotNull
    @Override
    public FileType getFileType() {
        return PascalFileType.INSTANCE;
    }

    @Nullable
    @Override
    public PsiElement getNameIdentifier() {
        return null;
    }

    @Override
    public VirtualFile getVirtualFile() {
        VirtualFile file = super.getVirtualFile();
        if (file != null) {
            if (file.getName().startsWith("$")) {
                return myOriginalFile != null ? myOriginalFile.getVirtualFile() : file;
            } else {
                return file;
            }
        } else {
            return myOriginalFile != null ? myOriginalFile.getVirtualFile() : null;
        }
    }

    @Override
    public void subtreeChanged() {
        super.subtreeChanged();
        cachedSection = null;
    }

    @Override
    public PsiElement getImplementationSection() {
        if (!PsiUtil.isElementUsable(cachedSection)) {
            cachedSection = PsiUtil.getModuleImplementationSection(this);
        }
        return cachedSection;
    }
}
