package com.siberika.idea.pascal.lang.stub.struct;

import com.siberika.idea.pascal.lang.psi.PascalStructType;
import com.siberika.idea.pascal.lang.stub.PasNamedStub;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public interface PasStructStub<T extends PascalStructType> extends PasNamedStub<T> {
    @NotNull
    List<String> getParentNames();

    // to support anonymous structure declarations with more than one identifier in list
    List<String> getAliases();

    @NotNull
    List<String> getTypeParameters();
}
