package com.siberika.idea.pascal.lang.compiled;

import com.intellij.openapi.module.Module;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.vfs.VirtualFile;
import com.siberika.idea.pascal.PPUFileType;
import org.jetbrains.annotations.NotNull;

/**
 * Author: George Bakhtadze
 * Date: 13/11/2013
 */
public class PPUFileDecompiler extends PascalUnitDecompiler {

    @NotNull
    @Override
    public CharSequence decompile(VirtualFile file) {
        assert file.getFileType() == PPUFileType.INSTANCE;
        return doDecompile(file);
    }

    @Override
    PascalCachingUnitDecompiler createCache(Module module, Sdk sdk) {
        return new PPUDecompilerCache(module, sdk);
    }

}
