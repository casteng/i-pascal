package com.siberika.idea.pascal.util;

import com.intellij.openapi.fileTypes.FileType;
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

import java.util.ArrayList;
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

    /* 1. Search for the original file name,
       2. Search for the filename all lowercased.
       3. Search for the filename all uppercased.
       Unit  names that are longer than 8 characters will first be looked for with  their  full length.
       If the unit is not found with this name, the name will be truncated to 8 characters */
    public static Collection<VirtualFile> getAllCompiledModuleFilesByName(@NotNull Module module, @NotNull String name, FileType fileType) {
        Collection<VirtualFile> res = new ArrayList<VirtualFile>();
        String[] nameVariants = name.length() > 8 ? new String[] {name, name.toLowerCase(), name.toUpperCase(), name.substring(0, 8)} : new String[] {name, name.toLowerCase(), name.toUpperCase()};
        for (String unitName : nameVariants) {
            res.addAll(FileBasedIndex.getInstance().getContainingFiles(FilenameIndex.NAME, unitName + "." + fileType.getDefaultExtension(),
                    GlobalSearchScope.moduleWithDependenciesAndLibrariesScope(module)));
        }
        return res;
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
