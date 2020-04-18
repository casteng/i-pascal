package com.siberika.idea.pascal.sdk;

import com.intellij.codeInsight.daemon.ProjectSdkSetupValidator;
import com.intellij.openapi.application.WriteAction;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtilCore;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectBundle;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.roots.ModuleRootModificationUtil;
import com.intellij.openapi.roots.ui.configuration.ProjectSettingsService;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.siberika.idea.pascal.PascalFileType;
import com.siberika.idea.pascal.PascalLanguage;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class PascalProjectSdkValidator implements ProjectSdkSetupValidator {
    @Override
    public boolean isApplicableFor(@NotNull Project project, @NotNull VirtualFile file) {
        if (file.getFileType() == PascalFileType.INSTANCE) {
            final PsiFile psiFile = PsiManager.getInstance(project).findFile(file);
            if (psiFile != null) {
                return psiFile.getLanguage().isKindOf(PascalLanguage.INSTANCE);
            }
        }
        return false;
    }

    @Nullable
    @Override
    public String getErrorMessage(@NotNull Project project, @NotNull VirtualFile file) {
        final Module module = ModuleUtilCore.findModuleForFile(file, project);
        if (module != null && !module.isDisposed()) {
            final Sdk sdk = ModuleRootManager.getInstance(module).getSdk();
            if (sdk == null) {
                if (ModuleRootManager.getInstance(module).isSdkInherited()) {
                    return ProjectBundle.message("project.sdk.not.defined");
                }
                else {
                    return ProjectBundle.message("module.sdk.not.defined");
                }
            }
        }
        return null;
    }

    @Override
    public void doFix(@NotNull Project project, @NotNull VirtualFile file) {
        Sdk projectSdk = ProjectSettingsService.getInstance(project).chooseAndSetSdk();
        if (projectSdk != null) {
            Module module = ModuleUtilCore.findModuleForFile(file, project);
            if (module != null) {
                WriteAction.run(() -> ModuleRootModificationUtil.setSdkInherited(module));
            }
        }
    }

}
