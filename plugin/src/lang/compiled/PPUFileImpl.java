package com.siberika.idea.pascal.lang.compiled;

import com.intellij.extapi.psi.LightPsiFileBase;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.ClassFileViewProvider;
import com.intellij.psi.FileViewProvider;
import com.intellij.psi.PsiCompiledFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiFileFactory;
import com.intellij.psi.PsiManager;
import com.intellij.psi.impl.PsiFileEx;
import com.intellij.psi.impl.PsiManagerEx;
import com.intellij.psi.impl.PsiManagerImpl;
import com.intellij.psi.impl.source.LightPsiFileImpl;
import com.intellij.psi.impl.source.SourceTreeToPsiMap;
import com.intellij.psi.impl.source.tree.TreeElement;
import com.siberika.idea.pascal.PPUFileType;
import com.siberika.idea.pascal.PascalFileType;
import com.siberika.idea.pascal.PascalLanguage;
import org.jetbrains.annotations.NotNull;

/**
 * Author: George Bakhtadze
 * Date: 14/11/2013
 */
public class PPUFileImpl extends LightPsiFileBase implements PsiFileEx, PsiCompiledFile {
    private volatile TreeElement myMirrorFileElement;
    private final PsiManager myManager;

    public PPUFileImpl(PsiManager myManager, FileViewProvider provider) {
        super(provider, PascalLanguage.INSTANCE);
        this.myManager = myManager;
    }

    @Override
    public void clearCaches() {
        synchronized (PPUFileImpl.class) {
            myMirrorFileElement = null;
        }
    }

    @NotNull
    @Override
    public PsiElement[] getChildren() {
        return getMirror().getChildren();
    }

    @Override
    public LightPsiFileImpl copyLight(FileViewProvider viewProvider) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public PsiFile getDecompiledPsiFile() {
        return (PsiFile) getMirror();
    }

    @Override
    @NotNull
    public VirtualFile getVirtualFile() {
        return getViewProvider().getVirtualFile();
    }

    @Override
    public PsiElement getMirror() {
        synchronized (PPUFileImpl.class) {
            if (myMirrorFileElement == null) {
                String ext = PascalFileType.INSTANCE.getDefaultExtension();
                String fileName = getVirtualFile().getNameWithoutExtension() + "." + ext;
                PsiFileFactory factory = PsiFileFactory.getInstance(getManager().getProject());
                PsiFile mirror = factory.createFileFromText(fileName, PascalLanguage.INSTANCE, decompile(getManager(), getVirtualFile()), false, false);
                TreeElement mirrorTreeElement = SourceTreeToPsiMap.psiToTreeNotNull(mirror);
                myMirrorFileElement = mirrorTreeElement;
            }

            return myMirrorFileElement.getPsi();
        }
    }

    @NotNull
    @Override
    public FileType getFileType() {
        return PPUFileType.INSTANCE;
    }

    public static String decompile(PsiManager manager, VirtualFile file) {
        PPUFileImpl psiFile = null;

        final FileViewProvider provider = ((PsiManagerEx)manager).getFileManager().findViewProvider(file);
        if (provider != null) {
            final PsiFile psi = provider.getPsi(provider.getBaseLanguage());
            if (psi instanceof PPUFileImpl) {
                psiFile = (PPUFileImpl)psi;
            }
        }

        if (psiFile == null) {
            psiFile = new PPUFileImpl((PsiManagerImpl)manager, new ClassFileViewProvider(manager, file));
        }

        return PPUFileDecompiler.decompileText(file);
    }
}
