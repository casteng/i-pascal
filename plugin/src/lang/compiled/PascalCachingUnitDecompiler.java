package com.siberika.idea.pascal.lang.compiled;

import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;

public interface PascalCachingUnitDecompiler {
    /**
     * Retrieves decompiled contents from cache.
     * Seems that compiled modules can't be found during PSI reparse. For that case virtualFile parameter is used.
     * @param virtualFile    compiled unit file
     * @return               decompiled data
     */
    String getSource(@NotNull VirtualFile virtualFile);
}
