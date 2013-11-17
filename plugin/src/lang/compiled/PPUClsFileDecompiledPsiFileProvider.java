package com.siberika.idea.pascal.lang.compiled;

import com.intellij.psi.ClsFileDecompiledPsiFileProvider;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiJavaFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Author: George Bakhtadze
 * Date: 13/11/2013
 */
public class PPUClsFileDecompiledPsiFileProvider implements ClsFileDecompiledPsiFileProvider {
    @Nullable
    @Override
    public PsiFile getDecompiledPsiFile(@NotNull PsiJavaFile clsFile) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }
}
