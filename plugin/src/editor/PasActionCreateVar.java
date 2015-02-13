package com.siberika.idea.pascal.editor;

import com.intellij.codeInsight.intention.impl.BaseIntentionAction;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.command.CommandProcessor;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.project.Project;
import com.intellij.pom.Navigatable;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.util.IncorrectOperationException;
import com.siberika.idea.pascal.PascalBundle;
import com.siberika.idea.pascal.lang.psi.PasBlockGlobal;
import com.siberika.idea.pascal.lang.psi.PasBlockLocal;
import com.siberika.idea.pascal.lang.psi.PasImplDeclSection;
import com.siberika.idea.pascal.lang.psi.PasTypes;
import com.siberika.idea.pascal.lang.psi.PasVarSection;
import com.siberika.idea.pascal.lang.psi.PascalNamedElement;
import com.siberika.idea.pascal.util.PsiUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Author: George Bakhtadze
 * Date: 06/10/2013
 */
public class PasActionCreateVar extends BaseIntentionAction {

    private final PascalNamedElement element;

    public PasActionCreateVar(PascalNamedElement namedElement) {
        this.element = namedElement;
    }

    @NotNull
    @Override
    public String getText() {
        return PascalBundle.message("action.createVar");
    }

    @NotNull
    @Override
    public String getFamilyName() {
        return PascalBundle.message("action.familyName");
    }

    @Override
    public boolean isAvailable(@NotNull Project project, Editor editor, PsiFile file) {
        return true;
    }

    @Override
    @SuppressWarnings("unchecked")
    public void invoke(@NotNull final Project project, Editor editor, final PsiFile file) throws IncorrectOperationException {
        ApplicationManager.getApplication().invokeLater(new Runnable() {
            @Override
            public void run() {
                PsiElement root = PsiUtil.getNearestAffectingDeclarationsRoot(element);
                if (null == root) { root = file; }
                final PsiElement section = root;
                ApplicationManager.getApplication().runWriteAction(new Runnable() {
                    @Override
                    public void run() {
                        CommandProcessor.getInstance().executeCommand(project, new Runnable() {
                            @Override
                            public void run() {
                                PasVarSection varSection = PsiUtil.findInSameSection(section, PasVarSection.class);
                                if (null == varSection) {
                                    PsiElement block = PsiUtil.findInSameSection(section,
                                            PasImplDeclSection.class, PasBlockGlobal.class, PasBlockLocal.class);
                                    if (block != null) {
                                        block.getParent().getNode().addLeaf(PasTypes.VAR_SECTION, "var " + element.getName() + ": ;\n", block.getNode());
                                        moveCaretToAdded(block, -2);
                                        PsiUtil.rebuildPsi(block.getParent());
                                    }
                                } else {
                                    varSection.getNode().addLeaf(PasTypes.NAME, "\n" + element.getName() + ": ;", null);
                                    moveCaretToAdded(varSection.getLastChild(), 1 + element.getName().length() + 2);
                                    PsiUtil.rebuildPsi(varSection.getParent());
                                }
                            }
                        }, getText(), null);
                    }
                });
            }
        });
    }

    private void moveCaretToAdded(@Nullable PsiElement block, int offsetFromEnd) {
        if (null == block) { return; }
        ((Navigatable) block.getNavigationElement()).navigate(true);
        Editor editor = FileEditorManager.getInstance(block.getProject()).getSelectedTextEditor();
        if (editor != null) {
            editor.getCaretModel().moveToOffset(editor.getCaretModel().getOffset() + offsetFromEnd);
        }
    }

    @Override
    public boolean startInWriteAction() {
        return false;
    }

}
