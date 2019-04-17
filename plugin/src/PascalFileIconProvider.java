package com.siberika.idea.pascal;

import com.intellij.ide.FileIconProvider;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.io.FileUtilRt;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.stubs.StubIndex;
import com.intellij.util.Processor;
import com.siberika.idea.pascal.lang.psi.PascalModule;
import com.siberika.idea.pascal.lang.stub.PascalModuleIndex;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.concurrent.atomic.AtomicReference;

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
            AtomicReference<Icon> result = new AtomicReference<>();
            StubIndex.getInstance().processElements(PascalModuleIndex.KEY, file.getNameWithoutExtension().toUpperCase(), project, GlobalSearchScope.allScope(project), PascalModule.class,
                    new Processor<PascalModule>() {
                        @Override
                        public boolean process(PascalModule module) {
                            PascalModule.ModuleType moduleType = module.getModuleType();
                            if (moduleType == PascalModule.ModuleType.UNIT) {
                            } else if ((moduleType == PascalModule.ModuleType.LIBRARY) || (moduleType == PascalModule.ModuleType.PACKAGE)) {
                                result.set(PascalIcons.FILE_LIBRARY);
                            } else if (moduleType == PascalModule.ModuleType.PROGRAM) {
                                result.set(PascalIcons.FILE_PROGRAM);
                            } else {
                                result.set(PascalIcons.FILE_INCLUDE);
                            }
                            return false;
                        }
                    });
            return result.get();
        }
        return null;
    }
}
