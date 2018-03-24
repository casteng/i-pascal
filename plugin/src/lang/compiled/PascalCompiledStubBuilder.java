package com.siberika.idea.pascal.lang.compiled;

import com.intellij.psi.FileViewProvider;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.compiled.ClsStubBuilder;
import com.intellij.psi.stubs.PsiFileStub;
import com.intellij.util.indexing.FileContent;
import com.siberika.idea.pascal.PascalLanguage;
import com.siberika.idea.pascal.lang.parser.PascalFileElementType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class PascalCompiledStubBuilder extends ClsStubBuilder {

    static final ClsStubBuilder INSTANCE = new PascalCompiledStubBuilder();

    @Override
    public int getStubVersion() {
        return PascalFileElementType.getStubIndexVersion();
    }

    @Nullable
    @Override
    public PsiFileStub<?> buildFileStub(@NotNull FileContent fileContent) {
        PsiManager manager = PsiManager.getInstance(fileContent.getProject());
        FileViewProvider vp = manager.findViewProvider(fileContent.getFile());
        PsiFile file = vp.getPsi(PascalLanguage.INSTANCE);
        if (file instanceof CompiledFileImpl) {
            return (PsiFileStub<?>) ((CompiledFileImpl) file).calcStubTree().getRoot();
        } else {
            throw new IllegalArgumentException("buildFileStub: Invalid file class: " + file.getClass().getName());
        }
    }

}
