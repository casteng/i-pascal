package com.siberika.idea.pascal;

import com.intellij.ide.FileIconProvider;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.io.FileUtilRt;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.siberika.idea.pascal.lang.psi.PasModule;
import com.siberika.idea.pascal.lang.psi.PascalModule;
import com.siberika.idea.pascal.util.PsiUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

public class PascalFileIconProvider implements FileIconProvider {
    @Nullable
    @Override
    public Icon getIcon(@NotNull VirtualFile file, int flags, @Nullable Project project) {
        String ext = FileUtilRt.getExtension(file.getName());
        if (PascalFileType.PROGRAM_EXTENSIONS.contains(ext)) {
            return PascalIcons.FILE_PROGRAM;
        } else if ("inc".equalsIgnoreCase(ext)) {
            return PascalIcons.FILE_INCLUDE;
        } else if (project != null) {
            PsiFile psiFile = PsiManager.getInstance(project).findFile(file);
            PasModule module = psiFile != null ? PsiUtil.getElementPasModule(psiFile) : null;
            if (module != null) {
                PascalModule.ModuleType moduleType = module.getModuleType();
                if (moduleType == PascalModule.ModuleType.UNIT) {
                    return null;
                } else if ((moduleType == PascalModule.ModuleType.LIBRARY) || (moduleType == PascalModule.ModuleType.PACKAGE)) {
                    return PascalIcons.FILE_LIBRARY;
                } else if (moduleType == PascalModule.ModuleType.PROGRAM) {
                    return PascalIcons.FILE_PROGRAM;
                } else {
                    return PascalIcons.FILE_INCLUDE;
                }
            }
        }
        return null;
    }
}
