package com.siberika.idea.pascal.editor.structure;

import com.intellij.ide.structureView.StructureView;
import com.intellij.ide.structureView.StructureViewModel;
import com.intellij.ide.structureView.StructureViewTreeElement;
import com.intellij.ide.structureView.TreeBasedStructureViewBuilder;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.fileEditor.FileEditor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.testFramework.LightVirtualFile;
import com.siberika.idea.pascal.lang.psi.PasModule;
import com.siberika.idea.pascal.sdk.BuiltinsParser;
import com.siberika.idea.pascal.util.PsiUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Author: George Bakhtadze
 * Date: 07/05/2015
 */
public class PascalStructureViewBuilder extends TreeBasedStructureViewBuilder {

    private static final Logger log = Logger.getInstance(PascalStructureViewBuilder.class);

    private Project project;
    private PsiFile file;

    public void setFile(PsiFile file) {
        this.file = file;
    }

    @NotNull
    @Override
    public StructureView createStructureView(FileEditor fileEditor, @NotNull Project project) {
        this.project = project;
        return super.createStructureView(fileEditor, project);
    }

    @NotNull
    @Override
    public StructureViewModel createStructureViewModel(@Nullable Editor editor) {
        StructureViewTreeElement te;
        PsiElement psiFile = file != null ? file : retrievePsiFile(editor);
        PasModule mod = PsiUtil.getElementPasModule(psiFile);
        if (mod != null) {
            te = new PasModuleStructureTreeElement(mod);
        } else {
            te = new PasStructureViewTreeElement(psiFile, null);
        }
        return new PascalStructureViewModel(psiFile.getContainingFile(), te);
    }

    private PsiElement retrievePsiFile(Editor editor) {
        Project prj = project;
        PsiFile res = null;
        if (editor != null) {
            VirtualFile vf = FileDocumentManager.getInstance().getFile(editor.getDocument());
            prj = editor.getProject() != null ? editor.getProject() : prj;
            if ((vf != null) && (prj != null)) {
                res = PsiManager.getInstance(prj).findFile(vf);
            }
        }
        if (null != res) {
            return res;
        } else {
            log.warn("Didn't able to determine PsiFile. Using builtins.");
            LightVirtualFile vf = BuiltinsParser.getBuiltinsSource();
            return PsiManager.getInstance(prj).findFile(vf);
        }
    }

}
