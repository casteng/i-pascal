package com.siberika.idea.pascal.lang.references.impl;

import com.intellij.openapi.util.io.FileUtil;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.refactoring.rename.RenamePsiFileProcessor;
import com.siberika.idea.pascal.PascalFileType;
import com.siberika.idea.pascal.lang.psi.PasModule;
import com.siberika.idea.pascal.util.PsiUtil;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

/**
 * Author: George Bakhtadze
 * Date: 20/03/2015
 */
public class PascalRenameProcessor extends RenamePsiFileProcessor {
    @Override
    public boolean canProcessElement(@NotNull PsiElement element) {
        return (element instanceof PsiFile) || (element instanceof PasModule);
    }

    @Override
    public boolean isInplaceRenameSupported() {
        return true;
    }

    @Override
    public void prepareRenaming(PsiElement element, String newName, Map<PsiElement, String> allRenames) {
        if (element instanceof PsiFile) {
            PasModule module = PsiUtil.getElementPasModule(element);
            if (module != null && module.isValid()) {
                final String moduleName = FileUtil.getNameWithoutExtension(newName);
                allRenames.put(module, moduleName);
            }
        } else if (element instanceof PasModule) {
            PsiFile file = element.getContainingFile();
            if (file != null && file.isValid()) {
                final String fileName = FileUtil.getNameWithoutExtension(newName) + "." + PascalFileType.INSTANCE.getDefaultExtension();
                allRenames.put(file, fileName);
            }
        }
    }

}
