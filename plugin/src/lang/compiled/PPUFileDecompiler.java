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

        return decompileText(file.getPath(), ModuleUtil.getModuleForFile(project, file));
    }

    public static String decompileText(String filename, Module module) {
        if (null == module) {
            return PascalBundle.message("decompile.no.module", filename);
        }
        return PPUDecompilerCache.decompile(module, filename);
    }

    /*@Override
    public boolean accepts(@NotNull VirtualFile file) {
        return PPUFileType.INSTANCE.equals(file.getFileType());
    }

    @NotNull
    @Override
    public ClsStubBuilder getStubBuilder() {
        return new ClsStubBuilder() {
            @Override
            public int getStubVersion() {
                return 1;
            }

            @Nullable
            @Override
            public PsiFileStub<?> buildFileStub(@NotNull FileContent fileContent) throws ClsFormatException {
                return null;
            }
        };
    }

    @NotNull
    @Override
    public FileViewProvider createFileViewProvider(@NotNull VirtualFile file, @NotNull PsiManager manager, boolean physical) {
        return new PPUViewProvider(manager, file, physical);
    }*/
}
