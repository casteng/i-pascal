package com.siberika.idea.pascal.util;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.module.ModuleUtilCore;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.projectRoots.SdkAdditionalData;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.roots.ProjectFileIndex;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.util.Computable;
import com.intellij.openapi.util.io.FileUtilRt;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.search.FileTypeIndex;
import com.intellij.psi.search.FilenameIndex;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.util.CommonProcessors;
import com.intellij.util.SmartList;
import com.intellij.util.containers.ArrayListSet;
import com.intellij.util.indexing.FileBasedIndex;
import com.siberika.idea.pascal.jps.sdk.PascalSdkData;
import com.siberika.idea.pascal.jps.util.FileUtil;
import com.siberika.idea.pascal.module.PascalModuleType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * Author: George Bakhtadze
 * Date: 26/11/2013
 */
public class ModuleUtil {
    private static final Logger LOG = Logger.getInstance(ModuleUtil.class);
    private static final List<String> INCLUDE_EXTENSIONS = Arrays.asList(null, "pas", "pp", "Pas", "Pp", "PAS", "PP");

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

    public static VirtualFile getCompiledByNameNoCase(final Module module, String unitName, final FileType fileType) {
        final String fullName = unitName + "." + fileType.getDefaultExtension();
        return ApplicationManager.getApplication().runReadAction(new Computable<VirtualFile>() {
            @Override
            public VirtualFile compute() {
                final CommonProcessors.FindProcessor<VirtualFile> processor = new CommonProcessors.FindProcessor<VirtualFile>() {
                    @Override
                    protected boolean accept(VirtualFile virtualFile) {
                        return fullName.equalsIgnoreCase(virtualFile.getName());
                    }
                };
                FileTypeIndex.processFiles(fileType, processor, GlobalSearchScope.moduleWithDependenciesAndLibrariesScope(module));
                return processor.getFoundValue();
            }
        });
    }

    /**
     * Locates file specified in include directive and its full file name trying the following:
     *   1. if name specifies an absolute path return it
     *   2. search in the directory where the current source file is located
     *   3. search in all paths in search path
     *   4. if not found, name is relative and referencing file is not specified, search index for name
     *   if name doesn't include file extension and file doesn't exists ".pas" and ".pp" are tried sequentially
     * @param project - used to retrieve list of search paths project
     * @param referencing - file which references to the include
     * @param name - name found in include directive
     * @return file name or null if not found
     */
    public static VirtualFile getIncludedFile(Project project, VirtualFile referencing, String name) {
        name = name.replace('\\', '/');
        File file = new File(name);
        if (file.isAbsolute()) {
            return tryExtensions(file);
        }

        if (referencing != null) {                                                             // if referencing virtual file is known
            if (referencing.getParent() != null) {
                String path = referencing.getParent().getPath();
                VirtualFile res = tryExtensions(new File(path, name));
                if (res != null) {
                    return res;
                }
            }

            Module module = com.intellij.openapi.module.ModuleUtil.findModuleForFile(referencing, project);

//            return module != null ? trySearchPath(name, GlobalSearchScope.moduleWithDependenciesScope(module)) : null;
            return module != null ? trySearchPath(project, name, GlobalSearchScope.projectScope(project)) : null;
        } else {                                                                               // often lexer can't determine which virtual file is referencing the include
            return trySearchPath(project, name, GlobalSearchScope.projectScope(project));
        }
    }

    public static Sdk getSdk(Project project, VirtualFile virtualFile) {
        Module module = virtualFile != null ? com.intellij.openapi.module.ModuleUtil.findModuleForFile(virtualFile, project) : null;
        Sdk sdk = module != null ? ModuleRootManager.getInstance(module).getSdk() : null;
        return sdk != null ? sdk : ProjectRootManager.getInstance(project).getProjectSdk();
    }

    public static boolean hasPascalModules(Project project) {
        Module[] modules = ModuleManager.getInstance(project).getModules();
        for (Module module : modules) {
            if (PascalModuleType.isPascalModule(module)) {
                return true;
            }
        }
        return false;
    }

    private static VirtualFile trySearchPath(Project project, String name, GlobalSearchScope scope) {
        name = FileUtil.getFilename(name);
        Collection<VirtualFile> files = searchInPathByName(project, name, scope);
        if (!files.isEmpty()) {
            return files.iterator().next();
        } else {
            String nameWoExt = FileUtilRt.getNameWithoutExtension(name);
            for (String ext : INCLUDE_EXTENSIONS) {
                files = searchInPathByName(project, nameWoExt + "." + ext, scope);
                if (!files.isEmpty()) {
                    return files.iterator().next();
                }
            }
        }
        return null;
    }

    private static Collection<VirtualFile> searchInPathByName(Project project, String name, GlobalSearchScope scope) {
        try {
            return FilenameIndex.getVirtualFilesByName(project, name, scope);
        } catch (AssertionError e) {
            LOG.info("Error using filename index. Falling back to ProjectFileIndex.");
            List<VirtualFile> result = new SmartList<>();
            ProjectFileIndex.getInstance(project).iterateContent(
                    fileOrDir -> {
                        result.add(fileOrDir);
                        return false;
                    }, file -> !file.isDirectory() && name.equalsIgnoreCase(file.getName()));
            return result;
        }
    }

    private static VirtualFile tryExtensions(File file) {
        if (!file.isFile() && (FileUtil.getExtension(file.getName()) == null)) {
            String filename = file.getPath();
            file = new File(filename + "." + INCLUDE_EXTENSIONS.get(1));
            if (!file.isFile()) {
                file = new File(filename + "." + INCLUDE_EXTENSIONS.get(2));
            }
        }
        return FileUtil.getVirtualFile(file.getPath());
    }

    public static List<String> retrieveUnitNamespaces(PsiElement parentIdent) {
        return retrieveUnitNamespaces(com.intellij.openapi.module.ModuleUtil.findModuleForPsiElement(parentIdent), parentIdent.getProject());
    }

    // TODO: cache at SDK
    public static List<String> retrieveUnitNamespaces(@Nullable Module module, Project project) {
        Sdk sdk = module != null ? ModuleRootManager.getInstance(module).getSdk() : ProjectRootManager.getInstance(project).getProjectSdk();
        if (sdk != null) {
            final SdkAdditionalData data = sdk.getSdkAdditionalData();
            if (data instanceof PascalSdkData) {
                String namespaces = (String) ((PascalSdkData) data).getValue(PascalSdkData.Keys.COMPILER_NAMESPACES.getKey());
                if (StringUtil.isNotEmpty(namespaces)) {
                    return Arrays.asList(namespaces.split(";"));
                }
            }
        }
        return Collections.emptyList();
    }

    // Searches for files by name with the specified list of extensions
    public static List<VirtualFile> findFileByName(Project project, String name, GlobalSearchScope scope, String...extensions) {
        List<VirtualFile> result = new SmartList<>();
        for (String ext : extensions) {
            result.addAll(FilenameIndex.getVirtualFilesByName(project, name + "." + ext, false, scope));
        }
        return result;
    }

}
