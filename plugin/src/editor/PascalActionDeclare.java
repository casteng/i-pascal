package com.siberika.idea.pascal.editor;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;
import com.intellij.codeInsight.intention.HighPriorityAction;
import com.intellij.codeInsight.intention.IntentionAction;
import com.intellij.codeInsight.intention.LowPriorityAction;
import com.intellij.codeInsight.intention.impl.BaseIntentionAction;
import com.intellij.codeInsight.template.Template;
import com.intellij.codeInsight.template.TemplateEditingListener;
import com.intellij.codeInsight.template.TemplateManager;
import com.intellij.codeInspection.SmartHashMap;
import com.intellij.lang.ASTNode;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.Result;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.RangeMarker;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.fileEditor.OpenFileDescriptor;
import com.intellij.openapi.progress.ProcessCanceledException;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.util.TextRange;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.impl.source.tree.TreeUtil;
import com.intellij.psi.tree.TokenSet;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.IncorrectOperationException;
import com.intellij.util.SmartList;
import com.siberika.idea.pascal.PascalBundle;
import com.siberika.idea.pascal.ide.actions.SectionToggle;
import com.siberika.idea.pascal.lang.context.ContextUtil;
import com.siberika.idea.pascal.lang.psi.PasArgumentList;
import com.siberika.idea.pascal.lang.psi.PasAssignPart;
import com.siberika.idea.pascal.lang.psi.PasBlockBody;
import com.siberika.idea.pascal.lang.psi.PasBlockGlobal;
import com.siberika.idea.pascal.lang.psi.PasBlockLocal;
import com.siberika.idea.pascal.lang.psi.PasClassProperty;
import com.siberika.idea.pascal.lang.psi.PasClassPropertySpecifier;
import com.siberika.idea.pascal.lang.psi.PasCompoundStatement;
import com.siberika.idea.pascal.lang.psi.PasConstDeclaration;
import com.siberika.idea.pascal.lang.psi.PasConstSection;
import com.siberika.idea.pascal.lang.psi.PasEnumType;
import com.siberika.idea.pascal.lang.psi.PasExpr;
import com.siberika.idea.pascal.lang.psi.PasFormalParameter;
import com.siberika.idea.pascal.lang.psi.PasFormalParameterSection;
import com.siberika.idea.pascal.lang.psi.PasFullyQualifiedIdent;
import com.siberika.idea.pascal.lang.psi.PasImplDeclSection;
import com.siberika.idea.pascal.lang.psi.PasInterfaceTypeDecl;
import com.siberika.idea.pascal.lang.psi.PasReferenceExpr;
import com.siberika.idea.pascal.lang.psi.PasRoutineImplDecl;
import com.siberika.idea.pascal.lang.psi.PasTypeDeclaration;
import com.siberika.idea.pascal.lang.psi.PasTypeSection;
import com.siberika.idea.pascal.lang.psi.PasTypes;
import com.siberika.idea.pascal.lang.psi.PasUnitInterface;
import com.siberika.idea.pascal.lang.psi.PasVarSection;
import com.siberika.idea.pascal.lang.psi.PascalNamedElement;
import com.siberika.idea.pascal.lang.psi.PascalRoutine;
import com.siberika.idea.pascal.lang.psi.PascalStructType;
import com.siberika.idea.pascal.lang.psi.impl.PasField;
import com.siberika.idea.pascal.lang.psi.impl.PascalExpression;
import com.siberika.idea.pascal.util.DocUtil;
import com.siberika.idea.pascal.util.EditorUtil;
import com.siberika.idea.pascal.util.PosUtil;
import com.siberika.idea.pascal.util.PreserveCaretTemplateAdapter;
import com.siberika.idea.pascal.util.PsiUtil;
import com.siberika.idea.pascal.util.StrUtil;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static com.siberika.idea.pascal.PascalBundle.message;

/**
 * Author: George Bakhtadze
 * Date: 06/10/2013
 */
public abstract class PascalActionDeclare extends BaseIntentionAction {

    private static final Logger LOG = Logger.getInstance(PascalActionDeclare.class.getName());

    public static final int MAX_SECTION_LEVELS = 20;
    final List<FixActionData> fixActionDataArray;
    private final String name;
    protected final PsiElement scope;
    private static final String TPL_VAR_RETURN_TYPE = "RETURN_TYPE";

    private static final String TPL_VAR_CODE = "CODE";
    private static final String TPL_VAR_TYPE = "TYPE";
    private static final String TPL_VAR_TYPES = "TYPE";
    private static final String TPL_VAR_ARGS = "ARG";
    private static final String TPL_VAR_CONST_EXPR = "CONST_EXPR";
    private static final String TPL_VAR_PARAMS = "PARAMS";

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

    protected void onInvoke() {
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
        onInvoke();
        final Document document = editor.getDocument();
        ApplicationManager.getApplication().invokeLater(new Runnable() {
            @Override
            public void run() {
                final List<FixActionData> sorted = new SmartList<FixActionData>(fixActionDataArray);
                Collections.sort(sorted);
                final RangeMarker marker = document.createRangeMarker(editor.getCaretModel().getOffset(), editor.getCaretModel().getOffset());
                new WriteCommandAction(project, name) {
                    @Override
                    protected void run(@NotNull Result result) throws Throwable {
                        Editor globalTemplateEditor = null;
                        boolean templated = false;
                        for (final FixActionData actionData : sorted) {
                            calcData(file, actionData);
                            templated = templated | (actionData.dataType == FixActionData.DataType.TEMPLATE) | (actionData.dataType == FixActionData.DataType.COMPLEX_TEMPLATE);
                            if (!StringUtil.isEmpty(actionData.text)) {
                                Document doc = DocUtil.getDocument(actionData.parent);
                                final Editor edit;
                                if ((doc != null) && (doc != document)) {                                        // Another document, open editor
                                    edit = FileEditorManager.getInstance(project).openTextEditor(
                                            new OpenFileDescriptor(project, actionData.parent.getContainingFile().getVirtualFile(), actionData.offset), true);
                                } else {
                                    if (actionData.parent.getContainingFile() != file) {
                                        EditorUtil.showErrorHint(PascalBundle.message("action.error.cantmodify"), EditorUtil.getHintPos(editor));
                                        return;                                                                  // Another file without document
                                    }
                                    doc = document;
                                    edit = editor;
                                }
                                if (actionData.dataType == FixActionData.DataType.COMPLEX_TEMPLATE) {
                                    globalTemplateEditor = edit;
                                }
                                if (doc.isWritable()) {
                                    doModify(project, edit, doc, actionData, new PreserveCaretTemplateAdapter(editor, file, marker, actionData.parent, PascalActionDeclare.this));
                                    if ((actionData.dataType == FixActionData.DataType.TEXT) && (globalTemplateEditor == null)) {
                                        DocUtil.reformat(actionData.parent, true);
                                    }
                                } else {
                                    EditorUtil.showErrorHint(PascalBundle.message("action.error.cantmodify"), EditorUtil.getHintPos(edit));
                                }
                            }
                        }
                        if (globalTemplateEditor != null) {
                            FixActionData globalTemplateData = sorted.iterator().next();
                            handleGlobalTemplateEditor(project, globalTemplateEditor, globalTemplateData,
                                    new PreserveCaretTemplateAdapter(editor, file, marker, globalTemplateData.parent, PascalActionDeclare.this));
                        }
                        if (!templated) {
                            afterExecution(editor, file);
                        }
                    }
                }.execute();
            }
        });
    }

    public void afterExecution(Editor editor, PsiFile file) {
    }

    private void handleGlobalTemplateEditor(Project project, final Editor editor, final FixActionData data, final TemplateEditingListener templateEditingListener) {
        final TemplateManager templateManager = TemplateManager.getInstance(project);
        final Template template = DocUtil.createTemplate(data.parent.getText(), data.variableDefaults, true);
        DocUtil.removeTemplateVariables(editor.getDocument(), data.parent.getTextRange());
        editor.getCaretModel().moveToOffset(data.parent.getTextRange().getStartOffset());
        templateManager.startTemplate(editor, template, false, null, templateEditingListener);
    }

    private void doModify(final Project project, final Editor editor, final Document doc, final FixActionData actionData, final TemplateEditingListener templateEditingListener) {
        final TemplateManager templateManager = TemplateManager.getInstance(project);
        final Template template = actionData.dataType == FixActionData.DataType.TEMPLATE ? DocUtil.createTemplate(actionData.text, actionData.variableDefaults, false) : null;
        actionData.offset = DocUtil.expandRangeStart(doc, actionData.offset, DocUtil.RE_WHITESPACE);
        cutLFs(doc, actionData);
        if (editor != null) {
            if (template != null) {
                editor.getCaretModel().moveToOffset(actionData.offset);
                templateManager.startTemplate(editor, template, templateEditingListener);
            } else {
                DocUtil.adjustDocument(editor, actionData.offset, actionData.text);
            }
        } else {
            DocUtil.adjustDocument(doc, actionData.offset, actionData.text.replace(DocUtil.PLACEHOLDER_CARET, ""));
        }
        PsiDocumentManager.getInstance(project).commitDocument(doc);
    }

    private void cutLFs(Document document, FixActionData data) {
        if (StringUtil.isEmpty(data.text)) {
            return;
        }
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
        data.text = data.text.substring(cl, data.text.length() - cr - cl);
    }

    @Override
    public boolean startInWriteAction() {
        return false;
    }

    // Fills data parent and offset and returns True if section for new member already exists
    private static boolean fillMemberPlace(PsiElement scope, FixActionData data, PasField.Visibility targetVisibility, PasField.FieldType type, Class<? extends PsiElement> sectionClass, Class<? extends PsiElement> sectionItemClass) {
        if (scope instanceof PascalStructType) {
            data.text = "\n" + PLACEHOLDER_DATA;
            data.parent = scope;
            Pair<Integer, Boolean> res = PosUtil.findPosInStruct((PascalStructType) scope, type, targetVisibility);
            data.offset = res.first;
            return res.second;
        } else if (scope instanceof PsiFile) {
            return fillParent(scope, data, sectionClass, sectionItemClass);
        }
        return false;
    }

    private static PsiElement getScope(PsiFile file, PsiElement scope) {
        return (scope instanceof PascalStructType) ? scope : file;
    }

    public static final Map<String, String> TYPE_VAR_DEFAULTS = StrUtil.getParams(Collections.singletonList(Pair.create(TPL_VAR_TYPE, "T")));

    String calcType(FixActionData data) {
        String res = "T";
        if (ContextUtil.isAssignLeftPart(data.element)) {
            String type = PascalExpression.calcAssignStatementType(PsiUtil.skipToExpressionParent(data.element));
            res = type != null ? type : res;
        }
        return res;
    }

    public static class ActionCreateParameter extends PascalActionDeclare {

        private FixActionData otherSectionData = null;
        private PascalRoutine routine;

        public ActionCreateParameter(String name, PascalNamedElement element, PsiElement scope) {
            super(name, element, scope);
        }

        @Override
        protected void onInvoke() {
            if (scope instanceof PascalRoutine) {
                routine = (PascalRoutine) scope;
                PsiElement other = SectionToggle.retrieveDeclaration((PascalRoutine) scope, true);
                if (other instanceof PascalRoutine) {
                    if (!SectionToggle.hasParametersOrReturnType((PascalRoutine) scope) && SectionToggle.hasParametersOrReturnType((PascalRoutine) other)) {
                        routine = (PascalRoutine) other;
                    } else {
                        otherSectionData = new FixActionData(fixActionDataArray.get(0).element);
                        otherSectionData.parent = other;
                        addData(otherSectionData);
                    }
                }
            }
        }

        @Override
        void calcData(final PsiFile file, final FixActionData data) {
            if (null == routine) {
                return;
            }
            final String tpl = "%s: $%s$";
            if (data == otherSectionData) {
                routine = (PascalRoutine) data.parent;
            }
            PasFormalParameterSection section = routine.getFormalParameterSection();
            if (section != null) {
                data.parent = section;
                if (section.getFormalParameterList().isEmpty()) {
                    data.offset = section.getTextRange().getStartOffset() + 1;
                    data.text = tpl;
                } else {
                    List<PasFormalParameter> params = section.getFormalParameterList();
                    data.offset = params.get(params.size() - 1).getTextRange().getEndOffset();
                    data.text = "; " + tpl;
                }
            } else if (routine.getNameIdentifier() != null) {
                data.parent = routine;
                data.offset = routine.getNameIdentifier().getTextRange().getEndOffset();
                data.text = "(" + tpl + ")";
            }
            if (data.parent != null) {
                data.text = String.format(data.text, data.element.getName(), TPL_VAR_TYPE);
                data.variableDefaults = StrUtil.getParams(Collections.singletonList(Pair.create(TPL_VAR_TYPE, calcType(data))));
                if (otherSectionData != null) {          // Modify both sections
                    data.parent = data.element.getContainingFile();
                    if (data == otherSectionData) {
                        data.dataType = FixActionData.DataType.COMPLEX_TEMPLATE;
                    }
                } else {
                    data.dataType = FixActionData.DataType.TEMPLATE;
                }
            }
        }
    }

    public static class ActionCreateVar extends PascalActionDeclare {

        private final String defaultType;

        public ActionCreateVar(String name, PascalNamedElement element, PsiElement scope, String defaultType) {
            super(name, element, scope);
            this.defaultType = defaultType;
        }

        @Override
        void calcData(final PsiFile file, final FixActionData data) {
            String prefix = "";
            if (!fillMemberPlace(getScope(file, scope), data, PasField.Visibility.PRIVATE, PasField.FieldType.VARIABLE, PasVarSection.class, null)) {
                prefix = "\nvar ";
            }
            String type = defaultType != null ? defaultType : calcType(data);
            if (data.parent != null) {
                data.createTemplate(data.text.replace(PLACEHOLDER_DATA, String.format("%s%s: $%s$;", prefix, data.element.getName(), TPL_VAR_TYPE)),
                        StrUtil.getParams(Collections.singletonList(Pair.create(TPL_VAR_TYPE, type))));
            }
        }

    }

    public static class ActionCreateVarHP extends ActionCreateVar implements HighPriorityAction {
        public ActionCreateVarHP(String name, PascalNamedElement element, PsiElement scope, String defaultType) {
            super(name, element, scope, defaultType);
        }
    }

    public static IntentionAction newActionCreateVar(String message, PascalNamedElement namedElement, PsiElement scope, boolean priority, String defaultType) {
        if (priority) {
            return new PascalActionDeclare.ActionCreateVarHP(message, namedElement, scope, defaultType);
        } else {
            return new PascalActionDeclare.ActionCreateVar(message, namedElement, scope, defaultType);
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
                if (fillMemberPlace(scope, data, PasField.Visibility.PRIVATE, PasField.FieldType.VARIABLE, PasVarSection.class, null)) {
                    data.text = data.text.replace(PLACEHOLDER_DATA, String.format("F%s: $%s$;", StringUtil.capitalize(data.element.getName()), TPL_VAR_TYPE));
                    data.dataType = FixActionData.DataType.COMPLEX_TEMPLATE;
                }
            } else {
                if (fillMemberPlace(scope, data, PasField.Visibility.PUBLIC, PasField.FieldType.PROPERTY, PasVarSection.class, null)) {
                    data.text = data.text.replace(PLACEHOLDER_DATA, String.format("property %1$s: $%2$s$ read F%1$s write F%1$s;", StringUtil.capitalize(data.element.getName()), TPL_VAR_TYPE));
                }
            }
            data.variableDefaults = StrUtil.getParams(Collections.singletonList(Pair.create(TPL_VAR_TYPE, calcType(data))));
        }
    }

    public static class ActionCreatePropertyHP extends ActionCreateProperty implements HighPriorityAction {
        public ActionCreatePropertyHP(String name, PascalNamedElement element, @NotNull PsiElement scope) {
            super(name, element, scope);
        }
    }

    public static IntentionAction newActionCreateProperty(String message, PascalNamedElement namedElement, PsiElement scope, boolean priority) {
        if (priority) {
            return new PascalActionDeclare.ActionCreatePropertyHP(message, namedElement, scope);
        } else {
            return new PascalActionDeclare.ActionCreateProperty(message, namedElement, scope);
        }
    }

    public static class ActionCreateConst extends PascalActionDeclare {
        public ActionCreateConst(String name, PascalNamedElement element, PsiElement scope) {
            super(name, element, scope);
        }

        @Override
        void calcData(final PsiFile file, final FixActionData data) {
            String prefix = "";
            if (!fillMemberPlace(getScope(file, scope), data, PasField.Visibility.PRIVATE, PasField.FieldType.CONSTANT, PasConstSection.class, PasConstDeclaration.class)) {
                prefix = "\nconst ";
            }
            if (data.parent != null) {
                data.createTemplate(data.text.replace(PLACEHOLDER_DATA, String.format("%s%s = $%s$;", prefix, data.element.getName(), TPL_VAR_CONST_EXPR)), null);
            }
        }

    }

    public static class ActionCreateConstHP extends ActionCreateConst implements HighPriorityAction {
        public ActionCreateConstHP(String name, PascalNamedElement element, PsiElement scope) {
            super(name, element, scope);
        }
    }

    public static class ActionCreateConstLP extends ActionCreateConst implements LowPriorityAction {
        public ActionCreateConstLP(String name, PascalNamedElement element, PsiElement scope) {
            super(name, element, scope);
        }
    }

    public static IntentionAction newActionCreateConst(PascalNamedElement namedElement, PsiElement scope, boolean priority) {
        String name = (scope instanceof PascalStructType) ? message("action.create.nestedConst") : message("action.create.const");
        if (priority) {
            return new PascalActionDeclare.ActionCreateConstHP(name, namedElement, scope);
        } else {
            return new PascalActionDeclare.ActionCreateConstLP(name, namedElement, scope);
        }
    }

    public static class ActionCreateEnum extends PascalActionDeclare {
        public ActionCreateEnum(String name, PascalNamedElement element, PsiElement scope) {
            super(name, element, scope);
        }

        @Override
        void calcData(final PsiFile file, final FixActionData data) {
            PasEnumType enumType = (PasEnumType) scope;
            PsiElement last = PsiUtil.sortByStart(Iterables.getLast(enumType.getNamedIdentDeclList(), null), Iterables.getLast(enumType.getExpressionList(), null), false).getFirst();
            data.parent = enumType;
            data.offset = -1;
            if (last != null) {
                data.createTemplate(", " + data.element.getName(), null);
                data.offset = last.getTextRange().getEndOffset();
            } else {
                data.createTemplate(data.element.getName(), null);
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
            String prefix = "";
            if (!fillMemberPlace(getScope(file, scope), data, PasField.Visibility.PRIVATE, PasField.FieldType.TYPE, PasTypeSection.class, PasTypeDeclaration.class)) {
                prefix = "\ntype ";
            }
            if (data.parent != null) {
                data.createTemplate(data.text.replace(PLACEHOLDER_DATA, String.format("%s%s = $%s$;", prefix, data.element.getName(), TPL_VAR_TYPE)), null);
            }
        }
    }

    public static class ActionCreateTypeHP extends ActionCreateType implements HighPriorityAction {
        public ActionCreateTypeHP(String name, PascalNamedElement element, PsiElement scope) {
            super(name, element, scope);
        }
    }

    public static class ActionCreateTypeLP extends ActionCreateType implements LowPriorityAction {
        public ActionCreateTypeLP(String name, PascalNamedElement element, PsiElement scope) {
            super(name, element, scope);
        }
    }

    public static IntentionAction newActionCreateType(PascalNamedElement namedElement, PsiElement scope, boolean priority) {
        String name = (scope instanceof PascalStructType) ? message("action.create.nestedType") : message("action.create.type");
        if (priority) {
            return new PascalActionDeclare.ActionCreateTypeHP(name, namedElement, scope);
        } else {
            return new PascalActionDeclare.ActionCreateTypeLP(name, namedElement, scope);
        }
    }

    public static class ActionCreateRoutine extends PascalActionDeclare {
        private final PsiElement callScope;
        private final PascalNamedElement namedElement;
        private final PasClassPropertySpecifier spec;

        public ActionCreateRoutine(String name, PascalNamedElement element, PsiElement scope, PsiElement callScope, PasClassPropertySpecifier spec) {
            super(name, element, scope);
            this.callScope = callScope;
            this.namedElement = element;
            this.spec = spec;
        }

        @Override
        void calcData(final PsiFile file, final FixActionData data) {
            String type = null;
            if (spec != null) {
                PasClassProperty prop = (PasClassProperty) spec.getParent();
                type = prop.getTypeID() != null ? prop.getTypeID().getText() : null;
            } else {
                PsiElement parent = PsiUtil.skipToExpressionParent(namedElement);
                if (parent instanceof PasAssignPart) {
                    type = PascalExpression.calcAssignExpectedType(parent.getParent());
                    type = type != null ? type : "";
                } else if (parent instanceof PasArgumentList) {
                    type = "";  // TODO: infere actual type
                }
            }
            if ((scope instanceof PascalStructType) && (null == callScope)) {
                addToInterface(data, type);
            } else {
                addToImplementation(data, type);
            }
        }

        private void addToImplementation(FixActionData data, String returnType) {
            PsiElement block;
            if (scope instanceof PasRoutineImplDecl) {
                block = scope;
            } else {
                block = (callScope instanceof PascalRoutine) ? callScope : PsiTreeUtil.getParentOfType(data.element, PasBlockBody.class);
            }
            block = block != null ? block : PsiTreeUtil.getParentOfType(data.element, PasCompoundStatement.class);
            if (block != null) {
                data.offset = block.getTextRange().getStartOffset();
                data.parent = block.getParent();
                Pair<String, Map<String, String>> arguments = calcArguments(data);
                String params = (arguments.second != null) && (arguments.second.size() > 0) ? arguments.first : "";
                String prefix = scope instanceof PascalStructType ? ((PascalStructType) scope).getName() + "." : "";
                if (returnType != null) {
                    arguments.getSecond().put(TPL_VAR_RETURN_TYPE, returnType);
                    data.createTemplate(String.format("\n\nfunction %s%s(%s): $%s$;\nbegin\n$%s$\nend;",
                            prefix, data.element.getName(), arguments.getFirst(), TPL_VAR_RETURN_TYPE, TPL_VAR_CODE), arguments.getSecond());
                } else {
                    data.createTemplate(String.format("\n\nprocedure %s%s(%s);\nbegin\n$%s$\nend;",
                            prefix, data.element.getName(), params, TPL_VAR_CODE), arguments.getSecond());
                }
            }
        }

        private void addToInterface(FixActionData data, String returnType) {
            fillMemberPlace(scope, data, PasField.Visibility.PUBLIC, PasField.FieldType.ROUTINE, null, null);
            Pair<String, Map<String, String>> arguments;
            if (spec != null) {
                if (ContextUtil.isPropertyGetter(spec)) {
                    Map<String, String> defaults = new SmartHashMap<String, String>();
                    arguments = Pair.create("", defaults);
                } else {
                    Map<String, String> defaults = ImmutableMap.of(TPL_VAR_TYPE, returnType);
                    arguments = Pair.create(String.format("const value: $%s$", TPL_VAR_TYPE), defaults);
                    returnType = null;
                }
            } else {
                arguments = calcArguments(data);
            }
            if (returnType != null) {
                arguments.getSecond().put(TPL_VAR_RETURN_TYPE, returnType);
                data.createTemplate(String.format("\nfunction %s(%s): $%s$;",
                        data.element.getName(), arguments.getFirst(), TPL_VAR_RETURN_TYPE), arguments.getSecond());
            } else {
                data.createTemplate(String.format("\nprocedure %s(%s);", data.element.getName(), arguments.getFirst()), arguments.getSecond());
            }
        }

        private Pair<String, Map<String, String>> calcArguments(FixActionData data) {
            PasExpr expression = PsiTreeUtil.getParentOfType(data.element, PasExpr.class);
            StringBuilder params = new StringBuilder();
            Map<String, String> defaults = new SmartHashMap<String, String>();
            if ((expression != null) && (expression.getNextSibling() instanceof PasArgumentList)) {
                PasArgumentList args = (PasArgumentList) expression.getNextSibling();
                int count = 0;
                for (PasExpr expr : args.getExprList()) {
                    if (count != 0) {
                        params.append("; ");
                    }
                    params.append("const $").append(TPL_VAR_ARGS).append(count).append("$").append(": $").append(TPL_VAR_TYPES).append(count).append("$");
                    String type = PascalExpression.infereType(expr);
                    defaults.put(TPL_VAR_TYPES + count, type);
                    if (expr instanceof PasReferenceExpr) {
                        PasFullyQualifiedIdent ident = ((PasReferenceExpr) expr).getFullyQualifiedIdent();
                        defaults.put(TPL_VAR_ARGS + count, ident.getNamePart());
                    } else if (StringUtil.isNotEmpty(type)) {
                        if (type.startsWith("T")) {
                            defaults.put(TPL_VAR_ARGS + count, type.substring(1));
                        } else {
                            defaults.put(TPL_VAR_ARGS + count, type.substring(0, 1).toLowerCase() + (count+1));
                        }
                    }
                    count++;
                }
            }
            return Pair.create(params.length() != 0 ? params.toString() : "$" + TPL_VAR_PARAMS + "$", defaults);
        }

        @Override
        public void afterExecution(Editor editor, PsiFile file) {
            if ((null == editor.getProject()) || fixActionDataArray.isEmpty()) {
                return;
            }
            FixActionData data = fixActionDataArray.iterator().next();
            try {
                if (data.parent != null) {
                    PsiElement routine = PsiUtil.findElementAt(data.parent, data.offset - data.parent.getTextRange().getStartOffset());
                    if (!(routine instanceof PascalRoutine)) {
                        routine = routine != null ? routine.getParent() : null;
                    }
                    if (scope instanceof PascalStructType) {
                        if (!(scope instanceof PasInterfaceTypeDecl) && null == callScope) {                // Scope specified as FQN part
                            if (routine instanceof PascalRoutine) {
                                PascalRoutineActions.ActionImplement act = new PascalRoutineActions.ActionImplement(message("action.implement"), (PascalNamedElement) routine);
                                act.invoke(editor.getProject(), editor, routine.getContainingFile());
                            }
                        } else {                                                                            // Called within method
                            if (routine instanceof PascalRoutine) {
                                PascalRoutineActions.ActionDeclare act = new PascalRoutineActions.ActionDeclare(message("action.declare.routine"), (PascalNamedElement) routine);
                                act.invoke(editor.getProject(), editor, routine.getContainingFile());
                            }
                        }
                    }
                }
            } catch (ProcessCanceledException e) {
                throw e;
            } catch (Exception e) {
                LOG.info("Error in PascalActionDeclare.afterExecution()", e);
            }
        }
    }

    public static class ActionCreateRoutineHP extends ActionCreateRoutine implements HighPriorityAction {
        public ActionCreateRoutineHP(String name, PascalNamedElement element, PsiElement scope, PsiElement callScope, PasClassPropertySpecifier spec) {
            super(name, element, scope, callScope, spec);
        }
    }

    public static IntentionAction newActionCreateRoutine(String message, PascalNamedElement namedElement, PsiElement scope, PsiElement callScope, boolean priority, PasClassPropertySpecifier spec) {
        if (priority) {
            return new PascalActionDeclare.ActionCreateRoutineHP(message, namedElement, scope, callScope, spec);
        } else {
            return new PascalActionDeclare.ActionCreateRoutine(message, namedElement, scope, callScope, spec);
        }
    }

    private static final String PLACEHOLDER_DATA = "---";

    @SuppressWarnings("unchecked")
    // Fills data parent and offset and returns True if section for new member already exists
    private static boolean fillParent(PsiElement file, FixActionData data, Class<? extends PsiElement> sectionClass, Class<? extends PsiElement> sectionItemClass) {
        PsiElement section = PsiUtil.getNearestAffectingDeclarationsRoot(data.element);
        data.text = PLACEHOLDER_DATA + "\n";
        for (int i = 0; i < MAX_SECTION_LEVELS; i++) {
            section = section != null ? section : file;
            data.parent = PsiUtil.findInSameSection(section, sectionClass);
            if (!canAffect(data.parent, data.element)) {
                data.parent = PsiUtil.findInSameSection(section, PasImplDeclSection.class, PasBlockGlobal.class, PasBlockLocal.class, PasUnitInterface.class);
                if (canAffect(data.parent, data.element)) {
                    data.offset = data.parent.getTextOffset();
                    PasUnitInterface intf = PsiTreeUtil.findChildOfType(data.parent, PasUnitInterface.class, false);    // Move after "INTERFACE"
                    ASTNode pos = intf != null ? TreeUtil.skipElements(intf.getNode().getFirstChildNode(), TokenSet.create(PasTypes.INTERFACE)) : null;
                    if (pos != null) {
                        data.offset = intf.getUsesClause() != null ? intf.getUsesClause().getTextRange().getEndOffset() : pos.getTextRange().getStartOffset();
                        data.text = "\n" + data.text;
                    }
                    return false;
                }
            } else {                                                                                  // section found and can affect target
                data.offset = data.parent.getTextRange().getEndOffset();
                data.text = "\n" + PLACEHOLDER_DATA;
                if ((sectionItemClass != null) && (PsiTreeUtil.getParentOfType(data.element, sectionClass) == data.parent)) {
                    PsiElement sectionItem = PsiTreeUtil.getParentOfType(data.element, sectionItemClass);
                    if (sectionItem != null) {
                        data.offset = sectionItem.getTextRange().getStartOffset();
                        data.text = data.text + "\n";
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
    static final class FixActionData implements Comparable<FixActionData> {
        final PascalNamedElement element;
        // The parent will be formatted
        PsiElement parent;
        String text = null;
        int offset = 0;
        DataType dataType = DataType.TEXT;
        Map<String, String> variableDefaults;

        public FixActionData(PascalNamedElement element) {
            this.element = element;
        }

        @Override
        public int compareTo(@NotNull FixActionData data) {
            return data.offset - offset;
        }

        public void createTemplate(String text, Map<String, String> variableDefaults) {
            assert dataType != DataType.COMPLEX_TEMPLATE;
            this.variableDefaults = variableDefaults;
            dataType = DataType.TEMPLATE;
            this.text = text;
        }

        private enum DataType {
            // the action data will insert some text
            TEXT,
            // the action data will use template engine
            TEMPLATE,
            // all data of an action will form a complex inline template (parent fields should be the same)
            COMPLEX_TEMPLATE
        }
    }

}
