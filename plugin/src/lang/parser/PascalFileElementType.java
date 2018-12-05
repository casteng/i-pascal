package com.siberika.idea.pascal.lang.parser;

import com.intellij.psi.stubs.PsiFileStub;
import com.intellij.psi.tree.IStubFileElementType;
import com.siberika.idea.pascal.PascalLanguage;

/**
 * Author: George Bakhtadze
 * Date: 27/10/2015
 */
public class PascalFileElementType extends IStubFileElementType<PsiFileStub<PascalFile>> {
    public PascalFileElementType(String debugName, PascalLanguage language) {
        super(debugName, language);
    }

    @Override
    public int getStubVersion() {
        return getStubIndexVersion();
    }

    public static int getStubIndexVersion() {
        return 86;
    }

}
