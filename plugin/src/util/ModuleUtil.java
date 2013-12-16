package com.siberika.idea.pascal.util;

import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.module.ModuleUtilCore;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.search.FilenameIndex;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.util.indexing.FileBasedIndex;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Collections;

/**
 * Author: George Bakhtadze
 * Date: 26/11/2013
 */
public class ModuleUtil {
    public static Collection<VirtualFile> getAllModuleFilesByExt(@Nullable Module module, @NotNull String extension) {
        if (module != null) {
            return FilenameIndex.getAllFilesByExt(module.getProject(), extension,
                    GlobalSearchScope.moduleWithDependenciesAndLibrariesScope(module));
        } else {
            return Collections.emptyList();
        }
    }

    public static Collection<VirtualFile> getAllCompiledModuleFilesByName(Module module, String name) {
        if (module != null) {
            return FileBasedIndex.getInstance().getContainingFiles(FilenameIndex.NAME, name + ".ppu",
                    GlobalSearchScope.moduleWithDependenciesAndLibrariesScope(module));
        }
        return Collections.emptyList();
    }

    /**
     * Returns module for the specified file including library
     * @param project - project which the file belongs to
     * @param file    - file to check
     * @return module for the file or null if not found
     */
    public static Module getModuleForFile(Project project, VirtualFile file) {
        for (Module module : ModuleManager.getInstance(project).getModules()) {
            if (ModuleUtilCore.moduleContainsFile(module, file, true)) {
                return module;
            }
        }
        return null;
    }
}
