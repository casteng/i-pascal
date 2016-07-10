package com.siberika.idea.pascal.util;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.module.ModuleUtilCore;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Computable;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.search.FileTypeIndex;
import com.intellij.psi.search.FilenameIndex;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.util.SmartList;
import com.intellij.util.containers.ArrayListSet;
import com.intellij.util.indexing.FileBasedIndex;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;

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
       If the unit is not found with this name, the name will be truncated to 8 characters. */
    public static Collection<VirtualFile> getAllCompiledModuleFilesByName(@NotNull final Module module, @NotNull final String name, final FileType fileType) {
        return ApplicationManager.getApplication().runReadAction(new Computable<Collection<VirtualFile>>() {
            @Override
            public Collection<VirtualFile> compute() {
                Collection<VirtualFile> res = new SmartList<VirtualFile>();
                Set<String> nameVariants = new ArrayListSet<String>();
                String nameExt = name + "." + fileType.getDefaultExtension();
                nameVariants.add(nameExt);
                nameVariants.add(nameExt.toUpperCase());
                nameVariants.add(nameExt.toLowerCase());
                if (name.length() > 8) {
                    nameVariants.add(name.substring(0, 8) + "." + fileType.getDefaultExtension());
                }
                for (String unitName : nameVariants) {
                    res.addAll(FileBasedIndex.getInstance().getContainingFiles(FilenameIndex.NAME, unitName,
                            GlobalSearchScope.moduleWithDependenciesAndLibrariesScope(module)));
                }
                return res;
            }
        });
    }

    /**
     * Returns module for the specified library file
     * @param project - project which the file belongs to
     * @param file    - library file to check
     * @return module for the file or null if not found
     */
    public static Module getModuleForLibraryFile(Project project, VirtualFile file) {
        for (Module module : ModuleManager.getInstance(project).getModules()) {
            if (ModuleUtilCore.moduleContainsFile(module, file, true)) {
                return module;
            }
        }
        return null;
    }

    public static Collection<VirtualFile> getCompiledByNameNoCase(final Module module, String unitName, final FileType fileType) {
        final String fullName = unitName + "." + fileType.getDefaultExtension();
        return ApplicationManager.getApplication().runReadAction(new Computable<Collection<VirtualFile>>() {
            @Override
            public Collection<VirtualFile> compute() {
                Collection<VirtualFile> res = new SmartList<VirtualFile>();
                for (VirtualFile file : FileBasedIndex.getInstance().getContainingFiles(FileTypeIndex.NAME, fileType,
                        GlobalSearchScope.moduleWithDependenciesAndLibrariesScope(module))) {
                    if (fullName.equalsIgnoreCase(file.getName())) {
                        res.add(file);
                    }
                }
                return res;
            }
        });
    }
}
