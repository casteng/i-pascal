package com.siberika.idea.pascal.lang.psi;

import com.intellij.psi.StubBasedPsiElement;
import com.siberika.idea.pascal.lang.psi.impl.HasUniqueName;
import com.siberika.idea.pascal.lang.stub.PasNamedStub;
import org.jetbrains.annotations.Nullable;

public interface PascalStubElement<B extends PasNamedStub> extends StubBasedPsiElement<B>, PascalNamedElement, HasUniqueName {

    @Nullable
    B retrieveStub();

    @Nullable
    String getContainingUnitName();

}
