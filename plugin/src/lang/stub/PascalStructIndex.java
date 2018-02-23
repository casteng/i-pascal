package com.siberika.idea.pascal.lang.stub;

import com.intellij.psi.stubs.StringStubIndexExtension;
import com.intellij.psi.stubs.StubIndexKey;
import com.siberika.idea.pascal.lang.parser.PascalFileElementType;
import com.siberika.idea.pascal.lang.psi.PascalStructType;
import org.jetbrains.annotations.NotNull;

public class PascalStructIndex extends StringStubIndexExtension {

    public static final StubIndexKey<String, PascalStructType> KEY =
            StubIndexKey.createIndexKey("pascal.struct");

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
