package com.siberika.idea.pascal.lang.stub;

import com.intellij.psi.stubs.StringStubIndexExtension;
import com.intellij.psi.stubs.StubIndexKey;
import com.siberika.idea.pascal.lang.parser.PascalFileElementType;
import com.siberika.idea.pascal.lang.psi.PascalNamedElement;
import org.jetbrains.annotations.NotNull;

public class PascalUnitSymbolIndex extends StringStubIndexExtension {

    public static final StubIndexKey<String, PascalNamedElement> KEY =
            StubIndexKey.createIndexKey("pascal.unit.symbol");

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
