package com.siberika.idea.pascal;

import com.intellij.ide.FileIconProvider;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.io.FileUtilRt;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.stubs.StubIndex;
import com.intellij.util.Processors;
import com.intellij.util.SmartList;
import com.siberika.idea.pascal.lang.psi.PascalModule;
import com.siberika.idea.pascal.lang.stub.PascalModuleIndex;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.Collection;

public class PascalFileIconProvider implements FileIconProvider {
    @Nullable
    @Override
    public Icon getIcon(@NotNull VirtualFile file, int flags, @Nullable Project project) {
        String ext = FileUtilRt.getExtension(file.getName());
        if (PascalFileType.PROGRAM_EXTENSIONS.contains(ext)) {
            return PascalIcons.FILE_PROGRAM;
        } else if ("inc".equalsIgnoreCase(ext)) {
            return PascalIcons.FILE_INCLUDE;
        } else if (project != null) {
            Collection<PascalModule> modules = new SmartList<>();
            StubIndex.getInstance().processElements(PascalModuleIndex.KEY, file.getNameWithoutExtension().toUpperCase(), project, GlobalSearchScope.allScope(project),
                    PascalModule.class, Processors.cancelableCollectProcessor(modules));
            for (PascalModule module : modules) {
                if (file.getName().equalsIgnoreCase(module.getContainingFile().getName())) {
                    PascalModule.ModuleType moduleType = module.getModuleType();
                    if (moduleType == PascalModule.ModuleType.UNIT) {
                        return null;
                    } else if ((moduleType == PascalModule.ModuleType.LIBRARY) || (moduleType == PascalModule.ModuleType.PACKAGE)) {
                        return PascalIcons.FILE_LIBRARY;
                    } else if (moduleType == PascalModule.ModuleType.PROGRAM) {
                        return PascalIcons.FILE_PROGRAM;
                    } else {
                        return PascalIcons.FILE_INCLUDE;
                    }
                }
            }
        }
        return null;
    }
}
