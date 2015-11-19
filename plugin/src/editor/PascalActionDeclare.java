package com.siberika.idea.pascal.editor;

import com.intellij.codeInsight.intention.impl.BaseIntentionAction;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.Result;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.ScrollType;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.TextRange;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.codeStyle.CodeStyleManager;
import com.intellij.util.IncorrectOperationException;
import com.intellij.util.SmartList;
import com.siberika.idea.pascal.PascalBundle;
import com.siberika.idea.pascal.lang.psi.PasBlockGlobal;
import com.siberika.idea.pascal.lang.psi.PasBlockLocal;
import com.siberika.idea.pascal.lang.psi.PasConstSection;
import com.siberika.idea.pascal.lang.psi.PasImplDeclSection;
import com.siberika.idea.pascal.lang.psi.PascalNamedElement;
import com.siberika.idea.pascal.util.PsiUtil;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Author: George Bakhtadze
 * Date: 06/10/2013
 */
public abstract class PascalActionDeclare extends BaseIntentionAction {

    public static final int MAX_SECTION_LEVELS = 20;
    private final List<FixActionData> fixActionDataArray;
    private final String name;

    abstract void calcData(final PsiFile file, final FixActionData data);

    public PascalActionDeclare(String name, PascalNamedElement element) {
        this.name = name;
        this.fixActionDataArray = new SmartList<FixActionData>(data(element));
    }

    public static FixActionData data(PascalNamedElement element) {
        return new FixActionData(element);
    }

    public void addData(FixActionData data) {
        fixActionDataArray.add(data);
    }

    @NotNull
    @Override
    public String getText() {
        return name;
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
                for (final FixActionData actionData : fixActionDataArray) {
                    calcData(file, actionData);
                    if (!StringUtil.isEmpty(actionData.text)) {
                        new WriteCommandAction(project) {
                            @Override
                            protected void run(@NotNull Result result) throws Throwable {
                                cutLFs(document, actionData);
                                document.insertString(actionData.offset, actionData.text);
                                editor.getCaretModel().moveToOffset(actionData.offset + actionData.text.length() - 1 - (actionData.text.endsWith("\n") ? 1 : 0));
                                editor.getScrollingModel().scrollToCaret(ScrollType.MAKE_VISIBLE);
                                PsiDocumentManager.getInstance(project).commitDocument(document);
                                if (actionData.parent != null) {
                                    PsiManager manager = actionData.parent.getManager();
                                    if (manager != null) {
                                        CodeStyleManager.getInstance(manager).reformat(actionData.parent, true);
                                    }
                                }
                            }
                        }.execute();
                    }
                }
            }
        });
    }

    private void cutLFs(Document document, FixActionData data) {
        final int MAX = 2;
        int l = Math.max(0, data.offset - MAX);
        int r = Math.min(document.getTextLength(), data.offset + 1 + MAX);
        String chars = document.getText(TextRange.create(l, r));
        // get text [offset - MAX, offset + MAX]
        // remove from data.text start sequence of linefeeds if it present both at data.text start and text starting back from offset
        // remove from data.text end sequence of linefeeds if it present both at data.text end and text starting from offset
        // TODO: possible reference chars[-1]?
        int cl = 0;
        while ((cl < Math.min(MAX, Math.min(data.offset - l, data.text.length())))
                && (chars.charAt(data.offset - l - cl - 1) == '\n') && (data.text.charAt(cl) == '\n')) {
            cl++;
        }
        int cr = 0;
        while ((cr < Math.min(MAX, Math.min(chars.length() - r + data.offset + 1, data.text.length() - cl)))
                && (chars.charAt(r - data.offset - 1 + cr) == '\n') && (data.text.charAt(data.text.length() - 1 - cr) == '\n')) {
            cr++;
        }
        data.text = data.text.substring(cl, data.text.length() - cr);
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

    public static class ActionCreateVar extends PascalActionDeclare {
        public ActionCreateVar(String name, PascalNamedElement element) {
            super(name, element);
        }

        @SuppressWarnings("unchecked")
        @Override
        void calcData(final PsiFile file, final FixActionData data) {
            if (findParent(file, data, PasConstSection.class)) {
                data.text = "\n" + data.element.getName() + ": T;";
            } else if (data.parent != null) {
                data.text = "var " + data.element.getName() + ": T;\n";
            }
        }
    }

    public static class ActionCreateConst extends PascalActionDeclare {
        public ActionCreateConst(String name, PascalNamedElement element) {
            super(name, element);
        }

        @SuppressWarnings("unchecked")
        @Override
        void calcData(final PsiFile file, final FixActionData data) {
            if (findParent(file, data, PasConstSection.class)) {
                data.text = "\n" + data.element.getName() + " = ;";
            } else if (data.parent != null) {
                data.text = "const " + data.element.getName() + " = ;\n";
            }
        }
    }

    public static class ActionCreateType extends PascalActionDeclare {
        public ActionCreateType(String name, PascalNamedElement element) {
            super(name, element);
        }

        @SuppressWarnings("unchecked")
        @Override
        void calcData(final PsiFile file, final FixActionData data) {
            if (findParent(file, data, PasConstSection.class)) {
                data.text = "\n" + data.element.getName() + "  = ;";
            } else if (data.parent != null) {
                data.text = "type " + data.element.getName() + " = ;\n";
            }
        }
    }

    private static boolean findParent(PsiFile file, FixActionData data, Class<? extends PsiElement> sectionClass) {
        PsiElement section = PsiUtil.getNearestAffectingDeclarationsRoot(data.element);
        for (int i = 0; i < MAX_SECTION_LEVELS; i++) {
            section = section != null ? section : file;
            data.parent = PsiUtil.findInSameSection(section, sectionClass);
            if (!canAffect(data.parent, data.element)) {
                data.parent = PsiUtil.findInSameSection(section, PasImplDeclSection.class, PasBlockGlobal.class, PasBlockLocal.class);
                if (canAffect(data.parent, data.element)) {
                    data.offset = data.parent.getTextOffset();
                    return false;
                }
            } else {
                data.offset = data.parent.getTextRange().getEndOffset();
                return true;
            }
            section = PsiUtil.getNearestAffectingDeclarationsRoot(section);
        }
        data.parent = null;
        return false;
    }

    private static boolean canAffect(PsiElement parent, PascalNamedElement element) {
        return (parent != null) && (parent.getTextOffset() <= element.getTextOffset());
    }

    /**
     * Author: George Bakhtadze
     * Date: 24/03/2015
     */
    static final class FixActionData {
        final PascalNamedElement element;
        // The parent will be formatterd
        PsiElement parent;
        String text = null;
        int offset = 0;

        public FixActionData(PascalNamedElement element) {
            this.element = element;
        }
    }
}
