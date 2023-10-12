package com.siberika.idea.pascal.lang.stub;

import com.intellij.psi.stubs.StringStubIndexExtension;
import com.intellij.psi.stubs.StubIndexKey;
import com.siberika.idea.pascal.lang.parser.PascalFileElementType;
import com.siberika.idea.pascal.lang.psi.PascalModule;
import org.jetbrains.annotations.NotNull;

/**
 * Author: George Bakhtadze
 * Date: 11/02/2018
 */
public class PascalModuleIndex extends StringStubIndexExtension {

    public static final StubIndexKey<String, PascalModule> KEY =
            StubIndexKey.createIndexKey("pascal.module");

    @NotNull
    @Override
    public StubIndexKey getKey() {
        return KEY;
    }

    @Override
    public int getVersion() {
        return PascalFileElementType.getStubIndexVersion();
    }
}
