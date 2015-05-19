package com.siberika.idea.pascal.compiler;

import com.intellij.openapi.compiler.CompileContext;
import com.intellij.openapi.compiler.CompileTask;
import com.intellij.openapi.compiler.CompilerMessageCategory;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.search.FilenameIndex;
import com.intellij.psi.search.GlobalSearchScope;
import com.siberika.idea.pascal.PascalBundle;
import com.siberika.idea.pascal.PascalFileType;
import com.siberika.idea.pascal.module.PascalModuleType;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Author: George Bakhtadze
 * Date: 19/05/2015
 */
public class Task implements CompileTask {
    @Override
    public boolean execute(CompileContext context) {
        checkMainFile(context);
        return true;
    }

    private void checkMainFile(CompileContext context) {
        Project prj = context.getProject();
        Collection<Module> modules = com.intellij.openapi.module.ModuleUtil.getModulesOfType(prj, PascalModuleType.getInstance());
        for (Module module : modules) {
            if (null == PascalModuleType.getMainFile(module)) {
                msg(context, PascalBundle.message("compile.no.mainfile", module.getName()));
                VirtualFile file = retrieveMainFile(module);
                if (file != null) {
                    PascalModuleType.setMainFile(module, file);
                    msg(context, PascalBundle.message("compile.mainfile.found", file.getName()));
                } else {
                    msg(context, PascalBundle.message("compile.mainfile.notfound"));
                }
            }
        }
    }

    private void msg(CompileContext context, String message) {
        context.addMessage(CompilerMessageCategory.WARNING, message, null, -1, -1);
    }

    private VirtualFile retrieveMainFile(Module module) {
        List<String> extensions = new ArrayList<String>(PascalFileType.PROGRAM_EXTENSIONS.size() + PascalFileType.UNIT_EXTENSIONS.size());
        extensions.addAll(PascalFileType.PROGRAM_EXTENSIONS);
        extensions.addAll(PascalFileType.UNIT_EXTENSIONS);
        for (String extension : extensions) {
            Collection<VirtualFile> files = FilenameIndex.getAllFilesByExt(module.getProject(), extension, GlobalSearchScope.moduleScope(module));
            if (!files.isEmpty()) {
                return files.iterator().next();
            }
        }
        return null;
    }
}
