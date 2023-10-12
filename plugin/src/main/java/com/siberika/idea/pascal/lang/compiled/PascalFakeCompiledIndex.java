package com.siberika.idea.pascal.lang.compiled;

import com.intellij.openapi.fileTypes.FileType;
import com.intellij.util.indexing.DataIndexer;
import com.intellij.util.indexing.DefaultFileTypeSpecificInputFilter;
import com.intellij.util.indexing.FileBasedIndex;
import com.intellij.util.indexing.FileContent;
import com.intellij.util.indexing.ID;
import com.intellij.util.indexing.ScalarIndexExtension;
import com.intellij.util.io.EnumeratorStringDescriptor;
import com.intellij.util.io.KeyDescriptor;
import com.siberika.idea.pascal.DCUFileType;
import com.siberika.idea.pascal.PPUFileType;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

// Fake index class needed to disable intellisense file size limit on indexing for compiled files via getFileTypesWithSizeLimitNotApplicable() method
public class PascalFakeCompiledIndex extends ScalarIndexExtension<String> {

    public static final ID<String,Void> NAME = ID.create("PascalFakeIndex");
    private static final List<FileType> NO_SIZE_LIMIT_FILE_TYPES = Arrays.asList(PPUFileType.INSTANCE, DCUFileType.INSTANCE);

    @NotNull
    @Override
    public ID<String, Void> getName() {
        return NAME;
    }

    @NotNull
    @Override
    public DataIndexer<String, Void, FileContent> getIndexer() {
        return inputData -> Collections.emptyMap();
    }

    @NotNull
    @Override
    public KeyDescriptor<String> getKeyDescriptor() {
        return EnumeratorStringDescriptor.INSTANCE;
    }

    @Override
    public int getVersion() {
        return 0;
    }

    @NotNull
    @Override
    public FileBasedIndex.InputFilter getInputFilter() {
        return new DefaultFileTypeSpecificInputFilter();
    }

    @Override
    public boolean dependsOnFileContent() {
        return false;
    }

    @NotNull
    @Override
    public Collection<FileType> getFileTypesWithSizeLimitNotApplicable() {
        return NO_SIZE_LIMIT_FILE_TYPES;
    }
}
