package com.siberika.idea.pascal.editor;

import com.intellij.codeInsight.intention.impl.BaseIntentionAction;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.Result;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.pom.Navigatable;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.codeStyle.CodeStyleManager;
import com.intellij.util.IncorrectOperationException;
import com.siberika.idea.pascal.PascalBundle;
import com.siberika.idea.pascal.lang.psi.PasBlockGlobal;
import com.siberika.idea.pascal.lang.psi.PasBlockLocal;
import com.siberika.idea.pascal.lang.psi.PasImplDeclSection;
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
    public void invoke(@NotNull final Project project, final Editor editor, final PsiFile file) throws IncorrectOperationException {
        final Document document = editor.getDocument();
        ApplicationManager.getApplication().invokeLater(new Runnable() {
            @Override
            public void run() {
                PsiElement root = PsiUtil.getNearestAffectingDeclarationsRoot(element);
                if (null == root) { root = file; }
                final PsiElement section = root;

                new WriteCommandAction(project){
                    @Override
                    protected void run(@NotNull Result result) throws Throwable {
                        PsiElement parent = PsiUtil.findInSameSection(section, PasVarSection.class);
                        String text = null;
                        int offset = 0;
                        if (null == parent) {
                            parent = PsiUtil.findInSameSection(section, PasImplDeclSection.class, PasBlockGlobal.class, PasBlockLocal.class);
                            if (parent != null) {
                                text = "var " + element.getName() + ": T;\n";
                                offset = parent.getTextOffset();
                            }
                        } else {
                            text = "\n" + element.getName() + ": T;";
                            offset = parent.getTextRange().getEndOffset();
                        }

                        if ((parent != null)) {
                            document.insertString(offset, text);
                            editor.getCaretModel().moveToOffset(offset + text.length() - 1 - (text.endsWith("\n") ? 1 : 0));
                            PsiDocumentManager.getInstance(project).commitDocument(document);
                            CodeStyleManager.getInstance(parent.getManager()).reformat(parent, true);
                        }
                    }
                }.execute();
            }
        });
    }

    private void moveCaretToAdded(Editor editor, @Nullable PsiElement block, int offsetFromEnd) {
        if (null == block) { return; }
        ((Navigatable) block.getNavigationElement()).navigate(true);
        // = FileEditorManager.getInstance(block.getProject()).getSelectedTextEditor();
        if (editor != null) {
            editor.getCaretModel().moveToOffset(editor.getCaretModel().getOffset() + offsetFromEnd);
        }
    }

    @Override
    public boolean startInWriteAction() {
        return false;
    }

}
