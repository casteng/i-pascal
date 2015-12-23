package com.siberika.idea.pascal.editor;

import com.google.common.collect.Iterables;
import com.intellij.codeInsight.intention.impl.BaseIntentionAction;
import com.intellij.lang.ASTNode;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.Result;
import com.intellij.openapi.command.CommandProcessor;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.fileEditor.OpenFileDescriptor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.TextRange;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.IncorrectOperationException;
import com.intellij.util.SmartList;
import com.intellij.util.containers.SortedList;
import com.siberika.idea.pascal.PascalBundle;
import com.siberika.idea.pascal.lang.psi.PasBlockGlobal;
import com.siberika.idea.pascal.lang.psi.PasBlockLocal;
import com.siberika.idea.pascal.lang.psi.PasConstDeclaration;
import com.siberika.idea.pascal.lang.psi.PasConstSection;
import com.siberika.idea.pascal.lang.psi.PasEnumType;
import com.siberika.idea.pascal.lang.psi.PasImplDeclSection;
import com.siberika.idea.pascal.lang.psi.PasTypeDeclaration;
import com.siberika.idea.pascal.lang.psi.PasTypeSection;
import com.siberika.idea.pascal.lang.psi.PasTypes;
import com.siberika.idea.pascal.lang.psi.PasUnitInterface;
import com.siberika.idea.pascal.lang.psi.PasVarSection;
import com.siberika.idea.pascal.lang.psi.PascalNamedElement;
import com.siberika.idea.pascal.lang.psi.PascalStructType;
import com.siberika.idea.pascal.lang.psi.impl.PasField;
import com.siberika.idea.pascal.util.DocUtil;
import com.siberika.idea.pascal.util.PosUtil;
import com.siberika.idea.pascal.util.PsiUtil;
import org.jetbrains.annotations.NotNull;

import java.util.Comparator;
import java.util.List;

/**
 * Author: George Bakhtadze
 * Date: 06/10/2013
 */
public abstract class PascalActionDeclare extends BaseIntentionAction {

    public static final int MAX_SECTION_LEVELS = 20;
    final List<FixActionData> fixActionDataArray;
    private final String name;
    protected final PsiElement scope;

    abstract void calcData(final PsiFile file, final FixActionData data);

    public PascalActionDeclare(String name, PascalNamedElement element, PsiElement scope) {
        this.name = name;
        this.scope = scope;
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
                List<FixActionData> sorted = new SortedList<FixActionData>(new Comparator<FixActionData>() {
                    @Override
                    public int compare(FixActionData o1, FixActionData o2) {
                        return o2.offset - o1.offset;
                    }
                });
                sorted.addAll(fixActionDataArray);
                for (final FixActionData actionData : sorted) {
                    calcData(file, actionData);
                    if (!StringUtil.isEmpty(actionData.text)) {
                        new WriteCommandAction(project) {
                            @Override
                            protected void run(@NotNull Result result) throws Throwable {
                                CommandProcessor.getInstance().setCurrentCommandName(name);
                                Document doc = DocUtil.getDocument(actionData.parent);
                                Editor edit;
                                if ((doc != null) && (doc != document)) {                                        // Another document, open editor
                                    edit = FileEditorManager.getInstance(project).openTextEditor(new OpenFileDescriptor(project, actionData.parent.getContainingFile().getVirtualFile()), true);
                                } else {
                                    if (actionData.parent.getContainingFile() != file) {
                                        return;                                                                  // Another file without document
                                    }
                                    doc = document;
                                    edit = editor;
                                }
                                cutLFs(doc, actionData);
                                actionData.offset = DocUtil.expandRangeStart(doc, actionData.offset, DocUtil.RE_WHITESPACE);
                                if (edit != null) {
                                    DocUtil.adjustDocument(edit, actionData.offset, actionData.text);
                                } else {
                                    DocUtil.adjustDocument(doc, actionData.offset, actionData.text.replace(DocUtil.PLACEHOLDER_CARET, ""));
                                }
                                PsiDocumentManager.getInstance(project).commitDocument(doc);
                            }
                        }.execute();
                    }
                    DocUtil.reformat(actionData.parent);
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

    private static boolean findPlaceInStruct(PsiElement scope, FixActionData data, PasField.FieldType type, int targetVisibility) {
        if (scope instanceof PascalStructType) {
            PascalStructType struct = (PascalStructType) scope;
            data.text = "\n" + PLACEHOLDER_DATA;
            data.parent = struct;
            data.offset = PosUtil.findPosInStruct(struct, type, targetVisibility);
            return true;
        }
        return false;
    }

    public static class ActionCreateVar extends PascalActionDeclare {
        public ActionCreateVar(String name, PascalNamedElement element, PsiElement scope) {
            super(name, element, scope);
        }

        @Override
        void calcData(final PsiFile file, final FixActionData data) {
            if (findPlaceInStruct(scope, data, PasField.FieldType.VARIABLE, PasField.Visibility.PRIVATE.ordinal()) || findParent(file, data, PasVarSection.class, null)) {
                data.text = data.text.replace(PLACEHOLDER_DATA, data.element.getName() + ": T" + DocUtil.PLACEHOLDER_CARET + ";");
            } else if (data.parent != null) {
                data.text = data.text.replace(PLACEHOLDER_DATA, "var " + data.element.getName() + ": T" + DocUtil.PLACEHOLDER_CARET + ";");
            }
        }
    }

    public static class ActionCreateProperty extends PascalActionDeclare {

        private FixActionData varData;

        public ActionCreateProperty(String name, PascalNamedElement element, @NotNull PsiElement scope) {
            super(name, element, scope);
            varData = new FixActionData(element);
            addData(varData);
        }

        @Override
        void calcData(final PsiFile file, final FixActionData data) {
            if (data == varData) {
                findPlaceInStruct(scope, varData, PasField.FieldType.PROPERTY, PasField.Visibility.PRIVATE.ordinal());
                varData.text = varData.text.replace(PLACEHOLDER_DATA, String.format("F%s: T" + DocUtil.PLACEHOLDER_CARET + ";", varData.element.getName()));
            } else {
                findPlaceInStruct(scope, data, PasField.FieldType.PROPERTY, PasField.Visibility.PUBLIC.ordinal());
                data.text = data.text.replace(PLACEHOLDER_DATA, String.format("property %1$s: T read F%1$s write F%1$s;", data.element.getName()));
            }
        }
    }

    public static class ActionCreateConst extends PascalActionDeclare {
        public ActionCreateConst(String name, PascalNamedElement element, PsiElement scope) {
            super(name, element, scope);
        }

        @Override
        void calcData(final PsiFile file, final FixActionData data) {
            if (findParent(file, data, PasConstSection.class, PasConstDeclaration.class)) {
                data.text = data.text.replace(PLACEHOLDER_DATA, data.element.getName() + " = " + DocUtil.PLACEHOLDER_CARET + ";");
            } else if (data.parent != null) {
                data.text = data.text.replace(PLACEHOLDER_DATA, "const " + data.element.getName() + " = " + DocUtil.PLACEHOLDER_CARET + ";");
            }
        }
    }

    public static class ActionCreateEnum extends PascalActionDeclare {
        public ActionCreateEnum(String name, PascalNamedElement element, PsiElement scope) {
            super(name, element, scope);
        }

        @Override
        void calcData(final PsiFile file, final FixActionData data) {
            PasEnumType enumType = (PasEnumType) scope;
            PsiElement last = PsiUtil.sortByStart(Iterables.getLast(enumType.getNamedIdentList(), null), Iterables.getLast(enumType.getExpressionList(), null), false).getFirst();
            data.parent = enumType;
            data.offset = -1;
            if (last != null) {
                data.text = ", " + data.element.getName();
                data.offset = last.getTextRange().getEndOffset();
            } else {
                data.text = data.element.getName();
                ASTNode rParen = enumType.getNode().findChildByType(PasTypes.RPAREN);
                if (rParen != null) {
                    data.offset = rParen.getTextRange().getStartOffset();
                }
            }
        }
    }

    public static class ActionCreateType extends PascalActionDeclare {
        public ActionCreateType(String name, PascalNamedElement element, PsiElement scope) {
            super(name, element, scope);
        }

        @Override
        void calcData(final PsiFile file, final FixActionData data) {
            if (findParent(file, data, PasTypeSection.class, PasTypeDeclaration.class)) {
                data.text = data.text.replace(PLACEHOLDER_DATA, data.element.getName() + " = " + DocUtil.PLACEHOLDER_CARET + ";");
            } else if (data.parent != null) {
                data.text = data.text.replace(PLACEHOLDER_DATA, "type " + data.element.getName() + " = " + DocUtil.PLACEHOLDER_CARET + ";");
            }
        }
    }

    private static final String PLACEHOLDER_DATA = "---";

    @SuppressWarnings("unchecked")
    private static boolean findParent(PsiFile file, FixActionData data, Class<? extends PsiElement> sectionClass, Class<? extends PsiElement> sectionItemClass) {
        PsiElement section = PsiUtil.getNearestAffectingDeclarationsRoot(data.element);
        data.text = PLACEHOLDER_DATA + "\n";
        for (int i = 0; i < MAX_SECTION_LEVELS; i++) {
            section = section != null ? section : file;
            data.parent = PsiUtil.findInSameSection(section, sectionClass);
            if (!canAffect(data.parent, data.element)) {
                data.parent = PsiUtil.findInSameSection(section, PasImplDeclSection.class, PasBlockGlobal.class, PasBlockLocal.class, PasUnitInterface.class);
                if (canAffect(data.parent, data.element)) {
                    data.offset = data.parent.getTextOffset();
                    PasUnitInterface intf = PsiTreeUtil.findChildOfType(data.parent, PasUnitInterface.class, false);    // Move after INTERFACE
                    if (intf != null) {
                        data.offset += intf.getTextLength();
                        data.offset = intf.getUsesClause() != null ? intf.getUsesClause().getTextRange().getEndOffset() : data.offset;
                        data.text = "\n" + data.text;
                    }
                    return false;
                }
            } else {
                data.offset = data.parent.getTextRange().getEndOffset();
                data.text = "\n" + PLACEHOLDER_DATA;
                if ((sectionItemClass != null) && (PsiTreeUtil.getParentOfType(data.element, sectionClass) == data.parent)) {
                    PsiElement sectionItem = PsiTreeUtil.getParentOfType(data.element, sectionItemClass);
                    if (sectionItem != null) {
                        data.offset = sectionItem.getTextRange().getStartOffset();
                        data.text = PLACEHOLDER_DATA + "\n";
                    }
                }
                return true;
            }
            section = PsiUtil.getNearestAffectingDeclarationsRoot(section);
            if (i == MAX_SECTION_LEVELS - 1) {
                throw new RuntimeException("Error finding section");
            }
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
        // The parent will be formatted
        PsiElement parent;
        String text = null;
        int offset = 0;

        public FixActionData(PascalNamedElement element) {
            this.element = element;
        }
    }
}
