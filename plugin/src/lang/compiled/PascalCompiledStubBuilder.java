package com.siberika.idea.pascal.lang.compiled;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.vfs.VirtualFile;
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

    private static final Logger LOG = Logger.getInstance(PascalCompiledStubBuilder.class);

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
        PsiFile file = vp != null ? vp.getPsi(PascalLanguage.INSTANCE) : null;
        if (file instanceof CompiledFileImpl) {
            return (PsiFileStub<?>) ((CompiledFileImpl) file).calcStubTree().getRoot();
        } else {
            VirtualFile vf = fileContent.getFile();
            if (file != null) {
                throw new IllegalArgumentException(String.format("buildFileStub: Invalid file class: %s, viewProvider: %s, content: %s",
                        file.getClass().getName(), vp.getClass().getSimpleName(), vf.getPath()));
            } else {
                LOG.info(String.format("WARN: can't index file %s. Probably decompiler is not properly setup.", vf.getPath()));
                return null;
            }
        }
    }

}
