package com.siberika.idea.pascal.jps.builder;

import org.jetbrains.jps.builders.BuildRootDescriptor;
import org.jetbrains.jps.builders.BuildTarget;

import java.io.File;

public class PascalSourceRootDescriptor extends BuildRootDescriptor {
    private File myRoot;
    private final PascalTarget myPascalTarget;

    public PascalSourceRootDescriptor(File root, PascalTarget pascalTarget) {
        myRoot = root;
        myPascalTarget = pascalTarget;
    }

    @Override
    public String getRootId() {
        return myRoot.getAbsolutePath();
    }

    @Override
    public File getRootFile() {
        return myRoot;
    }

    @Override
    public BuildTarget<?> getTarget() {
        return myPascalTarget;
    }

}