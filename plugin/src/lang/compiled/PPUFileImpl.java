package com.siberika.idea.pascal.lang.compiled;

import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.FileViewProvider;
import com.intellij.psi.PsiManager;
import com.siberika.idea.pascal.PPUFileType;
import org.jetbrains.annotations.NotNull;

/**
 * Author: George Bakhtadze
 * Date: 21/05/2015
 */
public class PPUFileImpl extends CompiledFileImpl {
    public PPUFileImpl(PsiManager myManager, FileViewProvider provider) {
        super(myManager, provider);
    }

    @Override
    public String decompile(PsiManager manager, VirtualFile file) {
        return PPUFileDecompiler.decompileText(manager.getProject(), file);
    }

    @NotNull
    @Override
    public FileType getFileType() {
        return PPUFileType.INSTANCE;
    }

}
