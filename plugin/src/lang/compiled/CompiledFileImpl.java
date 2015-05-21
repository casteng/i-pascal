package com.siberika.idea.pascal.lang.compiled;

import com.intellij.extapi.psi.LightPsiFileBase;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.FileViewProvider;
import com.intellij.psi.PsiCompiledFile;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiFileFactory;
import com.intellij.psi.PsiManager;
import com.intellij.psi.impl.PsiFileEx;
import com.intellij.psi.impl.file.PsiFileImplUtil;
import com.intellij.psi.impl.source.LightPsiFileImpl;
import com.intellij.psi.impl.source.PsiFileImpl;
import com.intellij.psi.impl.source.SourceTreeToPsiMap;
import com.intellij.psi.impl.source.tree.TreeElement;
import com.intellij.util.IncorrectOperationException;
import com.siberika.idea.pascal.PPUFileType;
import com.siberika.idea.pascal.PascalFileType;
import com.siberika.idea.pascal.PascalLanguage;
import org.jetbrains.annotations.NotNull;

/**
 * Author: George Bakhtadze
 * Date: 14/11/2013
 */
abstract class CompiledFileImpl extends LightPsiFileBase implements PsiFileEx, PsiCompiledFile {

    private static final String DECOMPILED_FILENAME_PREFIX = "$";

    private volatile TreeElement myMirrorFileElement;
    private final PsiManager myManager;

    public CompiledFileImpl(PsiManager myManager, FileViewProvider provider) {
        super(provider, PascalLanguage.INSTANCE);
        this.myManager = myManager;
    }

    @Override
    public void clearCaches() {
        synchronized (CompiledFileImpl.class) {
            myMirrorFileElement = null;
        }
    }

    @NotNull
    @Override
    public PsiElement[] getChildren() {
        return getMirror().getChildren();
    }

    @Override
    public void delete() throws IncorrectOperationException {
        checkDelete();
        PsiFileImplUtil.doDelete(this);
    }

    @Override
    public LightPsiFileImpl copyLight(FileViewProvider viewProvider) {
        return null;
    }

    @Override
    public PsiFile getDecompiledPsiFile() {
        return (PsiFile) getMirror();
    }

    @Override
    public PsiDirectory getContainingDirectory() {
        VirtualFile parentFile = getVirtualFile().getParent();
        if (parentFile == null) return null;
        return getManager().findDirectory(parentFile);
    }

    @Override
    @NotNull
    public VirtualFile getVirtualFile() {
        return getViewProvider().getVirtualFile();
    }

    @Override
    public PsiElement getMirror() {
        synchronized (CompiledFileImpl.class) {
            if (myMirrorFileElement == null) {
                String ext = PascalFileType.INSTANCE.getDefaultExtension();
                String fileName = DECOMPILED_FILENAME_PREFIX + getVirtualFile().getNameWithoutExtension() + "." + ext;
                PsiFileFactory factory = PsiFileFactory.getInstance(getManager().getProject());
                PsiFile mirror = factory.createFileFromText(fileName, PascalLanguage.INSTANCE, decompile(getManager(), getVirtualFile()), false, false);

                if (mirror instanceof PsiFileImpl) {
                    ((PsiFileImpl) mirror).setOriginalFile(this);
                }

                myMirrorFileElement = SourceTreeToPsiMap.psiToTreeNotNull(mirror);
            }

            return myMirrorFileElement.getPsi();
        }
    }

    @NotNull
    @Override
    public FileType getFileType() {
        return PPUFileType.INSTANCE;
    }

    public abstract String decompile(PsiManager manager, VirtualFile file);
}
