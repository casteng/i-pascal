package com.siberika.idea.pascal.lang.compiled;

import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.FileViewProvider;
import com.intellij.psi.PsiManager;
import com.intellij.psi.compiled.ClassFileDecompilers;
import com.intellij.psi.compiled.ClsStubBuilder;
import com.siberika.idea.pascal.DCUFileType;
import com.siberika.idea.pascal.PPUFileType;
import org.jetbrains.annotations.NotNull;

public class PascalClassFileDecompiler extends ClassFileDecompilers.Full {
    @NotNull
    @Override
    public ClsStubBuilder getStubBuilder() {
        return PascalCompiledStubBuilder.INSTANCE;
    }

    @NotNull
    @Override
    public FileViewProvider createFileViewProvider(@NotNull VirtualFile file, @NotNull PsiManager manager, boolean physical) {
        return new PPUViewProvider(manager, file, physical);
    }

    @Override
    public boolean accepts(@NotNull VirtualFile file) {
        return (file.getFileType() == PPUFileType.INSTANCE) || (file.getFileType() == DCUFileType.INSTANCE);
    }
}
