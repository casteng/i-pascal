package com.siberika.idea.pascal.sdk;

import com.intellij.openapi.vfs.VirtualFile;

/**
 * Author: George Bakhtadze
 * Date: 06/09/2016
 */
public class Define {
    public final String name;
    public final VirtualFile virtualFile;
    public final int offset;

    public Define(String name, VirtualFile virtualFile, int offset) {
        this.name = name;
        this.virtualFile = virtualFile;
        this.offset = offset;
    }
}
