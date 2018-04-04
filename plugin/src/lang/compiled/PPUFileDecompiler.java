package com.siberika.idea.pascal.lang.compiled;

import com.intellij.openapi.fileTypes.BinaryFileDecompiler;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import com.intellij.openapi.vfs.VirtualFile;
import com.siberika.idea.pascal.PPUFileType;
import com.siberika.idea.pascal.PascalBundle;
import com.siberika.idea.pascal.util.ModuleUtil;
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
        return decompileText(project, file);
    }

    static String decompileText(Project project, VirtualFile file) {
        Module module = ModuleUtil.getModuleForLibraryFile(project, file);
        if (null == module) {
            return PascalBundle.message("decompile.no.module", file.getPath());
        }
        return PPUDecompilerCache.decompile(module, file.getPath(), file);
    }

}
