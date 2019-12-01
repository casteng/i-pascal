package com.siberika.idea.pascal.lang.compiled;

import com.intellij.openapi.module.Module;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.vfs.VirtualFile;
import com.siberika.idea.pascal.DCUFileType;
import org.jetbrains.annotations.NotNull;

/**
 * Author: George Bakhtadze
 * Date: 21/05/2015
 */
public class DCUFileDecompiler extends PascalUnitDecompiler {

    @NotNull
    @Override
    public CharSequence decompile(VirtualFile file) {
        assert file.getFileType() == DCUFileType.INSTANCE;
        return doDecompile(file);
    }

    @Override
    PascalCachingUnitDecompiler createCache(Module module, Sdk sdk) {
        return new DCUCachingDecompiler(sdk);
    }

}
