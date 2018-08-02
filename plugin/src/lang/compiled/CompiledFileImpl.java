package com.siberika.idea.pascal.lang.compiled;

import com.intellij.extapi.psi.PsiFileBase;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.FileViewProvider;
import com.intellij.psi.PsiCompiledFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiFileFactory;
import com.intellij.psi.PsiManager;
import com.intellij.psi.impl.PsiFileEx;
import com.intellij.psi.impl.source.PsiFileImpl;
import com.intellij.psi.impl.source.PsiFileWithStubSupport;
import com.intellij.psi.impl.source.SourceTreeToPsiMap;
import com.intellij.psi.impl.source.tree.TreeElement;
import com.intellij.psi.stubs.PsiFileStubImpl;
import com.intellij.psi.stubs.StubTree;
import com.intellij.reference.SoftReference;
import com.siberika.idea.pascal.PascalFileType;
import com.siberika.idea.pascal.PascalLanguage;
import org.jetbrains.annotations.NotNull;

import java.lang.ref.Reference;

/**
 * Author: George Bakhtadze
 * Date: 14/11/2013
 */
public abstract class CompiledFileImpl extends PsiFileBase implements PsiFileEx, PsiCompiledFile, PsiFileWithStubSupport {

    private static final String DECOMPILED_FILENAME_PREFIX = "$";

    private volatile Reference<TreeElement> myMirrorFileElement;
    private final Object myMirrorLock = new Object();

    private volatile SoftReference<StubTree> myStub;
    private final Object myStubLock = new Object();

    private final PsiManager myManager;

    public CompiledFileImpl(PsiManager myManager, FileViewProvider provider) {
        super(provider, PascalLanguage.INSTANCE);
        this.myManager = myManager;
    }

    @Override
    public StubTree getStubTree() {
        ApplicationManager.getApplication().assertReadAccessAllowed();

        StubTree stubTree = SoftReference.dereference(myStub);
        if (stubTree != null) return stubTree;

        stubTree = super.getStubTree();
        if (null == stubTree) {
            return null;
        }

        synchronized (myStubLock) {
            myStub = new SoftReference<>(stubTree);
        }

        return stubTree;
    }

    @Override
    public void clearCaches() {
        super.clearCaches();
        synchronized (myMirrorLock) {
            myMirrorFileElement = null;
        }
        synchronized (myStubLock) {
            myStub = null;
        }
    }

    @NotNull
    @Override
    public PsiElement[] getChildren() {
        return getMirror().getChildren();
    }

    @Override
    public PsiFile getDecompiledPsiFile() {
        return (PsiFile) getMirror();
    }

    @Override
    public PsiElement getMirror() {
        TreeElement mirrorTreeElement = SoftReference.dereference(myMirrorFileElement);
        if (mirrorTreeElement == null) {
            synchronized (myMirrorLock) {
                mirrorTreeElement = SoftReference.dereference(myMirrorFileElement);
                if (mirrorTreeElement == null) {
                    VirtualFile file = getVirtualFile();
                    String ext = PascalFileType.INSTANCE.getDefaultExtension();
                    String fileName = DECOMPILED_FILENAME_PREFIX + file.getNameWithoutExtension() + "." + ext;

                    final Document document = FileDocumentManager.getInstance().getDocument(file);
                    assert document != null : file.getUrl();

                    CharSequence mirrorText = document.getImmutableCharSequence();
                    PsiFileFactory factory = PsiFileFactory.getInstance(getManager().getProject());
                    PsiFile mirror = factory.createFileFromText(fileName, PascalLanguage.INSTANCE, mirrorText, false, false);

                    mirrorTreeElement = SourceTreeToPsiMap.psiToTreeNotNull(mirror);
                    ((PsiFileImpl)mirror).setOriginalFile(this);
                    myMirrorFileElement = new SoftReference<>(mirrorTreeElement);
                }
            }
        }
        return mirrorTreeElement.getPsi();
    }

    @Override
    public boolean isContentsLoaded() {
        return myStub != null;
    }

    @Override
    public void onContentReload() {
        ApplicationManager.getApplication().assertWriteAccessAllowed();

        synchronized (myStubLock) {
            StubTree stubTree = SoftReference.dereference(myStub);
            myStub = null;
            if (stubTree != null) {
                //noinspection unchecked
                ((PsiFileStubImpl)stubTree.getRoot()).clearPsi("cls onContentReload");
            }
        }

        synchronized (myMirrorLock) {
            myMirrorFileElement = null;
        }
    }

}
