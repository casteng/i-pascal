package com.siberika.idea.pascal.lang.compiled;

import com.intellij.openapi.fileTypes.BinaryFileDecompiler;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.vfs.VirtualFile;
import com.siberika.idea.pascal.PascalBundle;
import com.siberika.idea.pascal.jps.sdk.PascalSdkData;
import com.siberika.idea.pascal.module.ModuleService;
import com.siberika.idea.pascal.sdk.BasePascalSdkType;
import com.siberika.idea.pascal.util.ModuleUtil;

/**
 * Author: George Bakhtadze
 * Date: 13/11/2013
 */
abstract class PascalUnitDecompiler implements BinaryFileDecompiler {

    abstract PascalCachingUnitDecompiler createCache(Module module, Sdk sdk);

    String doDecompile(VirtualFile file) {
        final Project[] projects = ProjectManager.getInstance().getOpenProjects();
        if (projects.length == 0) return "";
        final Project project = projects[0];
        ModuleService.ensureNameFileCache(file, project, true);     // Do not check TTL to avoid reentrant indexing errors
        Module module = ModuleUtil.getModuleForLibraryFile(project, file);
        if (null == module) {
            return PascalBundle.message("decompile.no.module", file.getPath());
        }
        return decompile(module, file);
    }

    private String decompile(Module module, VirtualFile file) {
        Sdk sdk = ModuleRootManager.getInstance(module).getSdk();
        if (null == sdk) {
            return PascalBundle.message("decompile.wrong.sdk");
        }
        PascalCachingUnitDecompiler decompilerCache;
        //noinspection SynchronizationOnLocalVariableOrMethodParameter
        synchronized (sdk) {
            decompilerCache = (PascalCachingUnitDecompiler) BasePascalSdkType.getAdditionalData(sdk).getValue(PascalSdkData.Keys.DECOMPILER_CACHE.getKey());
            if (null == decompilerCache) {
                decompilerCache = createCache(module, sdk);
                BasePascalSdkType.getAdditionalData(sdk).setValue(PascalSdkData.Keys.DECOMPILER_CACHE.getKey(), decompilerCache);
            }
        }
        return decompilerCache.getSource(file);
    }

}
