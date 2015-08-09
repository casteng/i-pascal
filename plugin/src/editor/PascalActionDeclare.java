package com.siberika.idea.pascal.editor;

import com.intellij.codeInsight.intention.impl.BaseIntentionAction;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.Result;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.codeStyle.CodeStyleManager;
import com.intellij.util.IncorrectOperationException;
import com.siberika.idea.pascal.PascalBundle;
import com.siberika.idea.pascal.lang.psi.PasBlockGlobal;
import com.siberika.idea.pascal.lang.psi.PasBlockLocal;
import com.siberika.idea.pascal.lang.psi.PasConstSection;
import com.siberika.idea.pascal.lang.psi.PasImplDeclSection;
import com.siberika.idea.pascal.lang.psi.PasTypeSection;
import com.siberika.idea.pascal.lang.psi.PasVarSection;
import com.siberika.idea.pascal.lang.psi.PascalNamedElement;
import com.siberika.idea.pascal.util.PsiUtil;
import org.jetbrains.annotations.NotNull;

/**
 * Author: George Bakhtadze
 * Date: 06/10/2013
 */
public class PascalActionDeclare extends BaseIntentionAction {

    private final PascalNamedElement element;
    private final FixActionData fixActionData;

    public PascalActionDeclare(PascalNamedElement namedElement, FixActionData fixActionData) {
        this.element = namedElement;
        this.fixActionData = fixActionData;
    }

    @NotNull
    @Override
    public String getText() {
        return fixActionData.getActionName();
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
    public void invoke(@NotNull final Project project, final Editor editor, final PsiFile file) throws IncorrectOperationException {
        final Document document = editor.getDocument();
        ApplicationManager.getApplication().invokeLater(new Runnable() {
            @Override
            public void run() {
                PsiElement root = PsiUtil.getNearestAffectingDeclarationsRoot(element);
                if (null == root) { root = file; }
                final PsiElement section = root;

                new WriteCommandAction(project) {
                    @Override
                    protected void run(@NotNull Result result) throws Throwable {
                        fixActionData.calcData(section, element);

                        if ((fixActionData.parent != null)) {
                            document.insertString(fixActionData.offset, fixActionData.text);
                            editor.getCaretModel().moveToOffset(fixActionData.offset + fixActionData.text.length() - 1 - (fixActionData.text.endsWith("\n") ? 1 : 0));
                            PsiDocumentManager.getInstance(project).commitDocument(document);
                            PsiManager manager = fixActionData.parent.getManager();
                            if (manager != null) {
                                CodeStyleManager.getInstance(manager).reformat(fixActionData.parent, true);
                            }
                        }
                    }
                }.execute();
            }
        });
    }

    /*private void moveCaretToAdded(Editor editor, @Nullable PsiElement block, int offsetFromEnd) {
        if (null == block) { return; }
        ((Navigatable) block.getNavigationElement()).navigate(true);
        // = FileEditorManager.getInstance(block.getProject()).getSelectedTextEditor();
        if (editor != null) {
            editor.getCaretModel().moveToOffset(editor.getCaretModel().getOffset() + offsetFromEnd);
        }
    }*/

    @Override
    public boolean startInWriteAction() {
        return false;
    }

    public static final FixActionData CREATE_VAR = new FixActionData() {
        @SuppressWarnings("unchecked")
        @Override
        void calcData(final PsiElement section, final PascalNamedElement element) {
            parent = PsiUtil.findInSameSection(section, PasVarSection.class);
            if (!canAffect(parent, element)) {
                parent = PsiUtil.findInSameSection(section, PasImplDeclSection.class, PasBlockGlobal.class, PasBlockLocal.class);
                if (canAffect(parent, element)) {
                    text = "var " + element.getName() + ": T;\n";
                    offset = parent.getTextOffset();
                }
            } else {
                text = "\n" + element.getName() + ": T;";
                offset = parent.getTextRange().getEndOffset();
            }
        }

        @Override
        String getActionName() {
            return PascalBundle.message("action.createVar");
        }
    };

    public static final FixActionData CREATE_CONST = new FixActionData() {
        @SuppressWarnings("unchecked")
        @Override
        void calcData(final PsiElement section, final PascalNamedElement element) {
            parent = PsiUtil.findInSameSection(section, PasConstSection.class);
            if (!canAffect(parent, element)) {
                parent = PsiUtil.findInSameSection(section, PasImplDeclSection.class, PasBlockGlobal.class, PasBlockLocal.class);
                if (canAffect(parent, element)) {
                    text = "const " + element.getName() + " = ;\n";
                    offset = parent.getTextOffset();
                }
            } else {
                text = "\n" + element.getName() + " = ;";
                offset = parent.getTextRange().getEndOffset();
            }
        }

        @Override
        String getActionName() {
            return PascalBundle.message("action.createConst");
        }
    };

    public static final FixActionData CREATE_TYPE = new FixActionData() {
        @SuppressWarnings("unchecked")
        @Override
        void calcData(final PsiElement section, final PascalNamedElement element) {
            parent = PsiUtil.findInSameSection(section, PasTypeSection.class);
            if (!canAffect(parent, element)) {
                parent = PsiUtil.findInSameSection(section, PasImplDeclSection.class, PasBlockGlobal.class, PasBlockLocal.class);
                if (canAffect(parent, element)) {
                    text = "type " + element.getName() + " = ;\n";
                    offset = parent.getTextOffset();
                }
            } else {
                text = "\n" + element.getName() + "  = ;";
                offset = parent.getTextRange().getEndOffset();
            }

        }

        @Override
        String getActionName() {
            return PascalBundle.message("action.createType");
        }
    };

    private static boolean canAffect(PsiElement parent, PascalNamedElement element) {
        return (parent != null) && (parent.getTextOffset() <= element.getTextOffset());
    }

}
