package com.siberika.idea.pascal.lang.compiled;

import com.intellij.openapi.extensions.Extensions;
import com.intellij.openapi.fileTypes.BinaryFileDecompiler;
import com.intellij.openapi.fileTypes.ContentBasedFileSubstitutor;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiManager;
import com.siberika.idea.pascal.PPUFileType;
import org.jetbrains.annotations.NotNull;

/**
 * Author: George Bakhtadze
 * Date: 13/11/2013
 */
public class PPUFileDecompiler implements BinaryFileDecompiler {

    @NotNull
    @Override
    public CharSequence decompile(VirtualFile file) {
        assert file.getFileType() == PPUFileType.INSTANCE;

        final Project[] projects = ProjectManager.getInstance().getOpenProjects();
        if (projects.length == 0) return "";
        final Project project = projects[0];

        final ContentBasedFileSubstitutor[] processors = Extensions.getExtensions(ContentBasedFileSubstitutor.EP_NAME);
        for (ContentBasedFileSubstitutor processor : processors) {
            if (processor.isApplicable(project, file)) {
                return processor.obtainFileText(project, file);
            }
        }

        return PPUFileImpl.decompile(PsiManager.getInstance(project), file);
    }

    public static String decompileText(String filename, Module module) {
        return PPUDecompilerCache.decompile(module, filename);
    }
}
