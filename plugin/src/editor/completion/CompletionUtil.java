package com.siberika.idea.pascal.editor.completion;

import com.intellij.codeInsight.completion.CompletionParameters;
import com.intellij.codeInsight.completion.CompletionResultSet;
import com.intellij.codeInsight.completion.InsertHandler;
import com.intellij.codeInsight.completion.InsertionContext;
import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.ide.DataManager;
import com.intellij.lang.ASTNode;
import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.io.FileUtilRt;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiErrorElement;
import com.intellij.psi.SmartPsiElementPointer;
import com.intellij.psi.codeStyle.MinusculeMatcher;
import com.intellij.psi.codeStyle.NameUtil;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.search.ProjectScope;
import com.intellij.psi.stubs.StubIndex;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.tree.TokenSet;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.Processor;
import com.intellij.util.SmartList;
import com.siberika.idea.pascal.PascalBundle;
import com.siberika.idea.pascal.PascalIcons;
import com.siberika.idea.pascal.ide.actions.UsesActions;
import com.siberika.idea.pascal.lang.context.CodePlace;
import com.siberika.idea.pascal.lang.context.Context;
import com.siberika.idea.pascal.lang.context.ContextUtil;
import com.siberika.idea.pascal.lang.lexer.PascalLexer;
import com.siberika.idea.pascal.lang.psi.PasArgumentList;
import com.siberika.idea.pascal.lang.psi.PasAssignPart;
import com.siberika.idea.pascal.lang.psi.PasCallExpr;
import com.siberika.idea.pascal.lang.psi.PasClassField;
import com.siberika.idea.pascal.lang.psi.PasCompoundStatement;
import com.siberika.idea.pascal.lang.psi.PasEntityScope;
import com.siberika.idea.pascal.lang.psi.PasExpr;
import com.siberika.idea.pascal.lang.psi.PasModule;
import com.siberika.idea.pascal.lang.psi.PasNamedIdentDecl;
import com.siberika.idea.pascal.lang.psi.PasRecordDecl;
import com.siberika.idea.pascal.lang.psi.PasReferenceExpr;
import com.siberika.idea.pascal.lang.psi.PasRepeatStatement;
import com.siberika.idea.pascal.lang.psi.PasStatement;
import com.siberika.idea.pascal.lang.psi.PasTryStatement;
import com.siberika.idea.pascal.lang.psi.PasTypes;
import com.siberika.idea.pascal.lang.psi.PascalIdentDecl;
import com.siberika.idea.pascal.lang.psi.PascalNamedElement;
import com.siberika.idea.pascal.lang.psi.PascalRoutine;
import com.siberika.idea.pascal.lang.psi.PascalRoutineEntity;
import com.siberika.idea.pascal.lang.psi.PascalStubElement;
import com.siberika.idea.pascal.lang.psi.field.ParamModifier;
import com.siberika.idea.pascal.lang.psi.impl.PasField;
import com.siberika.idea.pascal.lang.references.PasReferenceUtil;
import com.siberika.idea.pascal.lang.stub.PascalSymbolIndex;
import com.siberika.idea.pascal.util.DocUtil;
import com.siberika.idea.pascal.util.EditorUtil;
import com.siberika.idea.pascal.util.PsiUtil;
import com.siberika.idea.pascal.util.StrUtil;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.util.Arrays;
import java.util.Collection;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Author: George Bakhtadze
 * Date: 09/05/2018
 */
class CompletionUtil {
    private static final Map<String, String> INSERT_MAP = getInsertMap();
    private static final String PLACEHOLDER_FILENAME = "__FILENAME__";
    private static final Collection<String> CLOSING_STATEMENTS = Arrays.asList(PasTypes.END.toString(), PasTypes.EXCEPT.toString(), PasTypes.UNTIL.toString());

    private static final InsertHandler<LookupElement> INSERT_HANDLER = new InsertHandler<LookupElement>() {
        @Override
        public void handleInsert(final InsertionContext context, LookupElement item) {
            String content = INSERT_MAP.get(item.getLookupString());
            if (null != content) {
                content = content.replaceAll(PLACEHOLDER_FILENAME, FileUtilRt.getNameWithoutExtension(context.getFile().getName()));
                int caretPos = context.getEditor().getCaretModel().getOffset();
                DocUtil.adjustDocument(context.getEditor(), caretPos, content);
                context.commitDocument();
                if (CLOSING_STATEMENTS.contains(item.getLookupString())) {
                    PsiElement el = context.getFile().findElementAt(caretPos - 1);
                    PsiElement block = PsiTreeUtil.getParentOfType(el, PasCompoundStatement.class, PasTryStatement.class, PasRepeatStatement.class);
                    if (block != null) {
                        DocUtil.reformat(block, true);
                    }
                } else {
                    DocUtil.reformatInSeparateCommand(context.getProject(), context.getFile(), context.getEditor());
                }
            }
        }
    };
    private static final TokenSet TS_DO_THEN_OF = TokenSet.create(PasTypes.DO, PasTypes.THEN, PasTypes.OF, PasTypes.ELSE);
    private static final String TYPE_UNTYPED = "<untyped>";
    static final TokenSet UNIT_SECTIONS = TokenSet.create(
            PasTypes.INTERFACE, PasTypes.IMPLEMENTATION,
            PasTypes.INITIALIZATION, PasTypes.FINALIZATION
    );
    static final TokenSet TOP_LEVEL_DECLARATIONS = TokenSet.create(PasTypes.CONTAINS, PasTypes.REQUIRES);
    static final TokenSet DIRECTIVE_METHOD = TokenSet.create(
            PasTypes.REINTRODUCE, PasTypes.OVERLOAD, PasTypes.MESSAGE, PasTypes.STATIC, PasTypes.DYNAMIC, PasTypes.OVERRIDE, PasTypes.VIRTUAL,
            PasTypes.CDECL, PasTypes.PASCAL, PasTypes.REGISTER, PasTypes.SAFECALL, PasTypes.STDCALL, PasTypes.EXPORT,
            PasTypes.ABSTRACT, PasTypes.FINAL, PasTypes.INLINE, PasTypes.ASSEMBLER,
            PasTypes.DEPRECATED, PasTypes.EXPERIMENTAL, PasTypes.PLATFORM, PasTypes.LIBRARY, PasTypes.DISPID
    );
    static final TokenSet DIRECTIVE_ROUTINE = TokenSet.create(
            PasTypes.OVERLOAD, PasTypes.INLINE, PasTypes.ASSEMBLER,
            PasTypes.CDECL, PasTypes.PASCAL, PasTypes.REGISTER, PasTypes.SAFECALL, PasTypes.STDCALL, PasTypes.EXPORT,
            PasTypes.DEPRECATED, PasTypes.EXPERIMENTAL, PasTypes.PLATFORM, PasTypes.LIBRARY
    );
    static final TokenSet VALUES = TokenSet.create(PasTypes.NIL, PasTypes.FALSE, PasTypes.TRUE);
    static final TokenSet STATEMENTS_IN_CYCLE = TokenSet.create(PasTypes.BREAK, PasTypes.CONTINUE);
    static final TokenSet STATEMENTS = TokenSet.create(
            PasTypes.FOR, PasTypes.WHILE, PasTypes.REPEAT,
            PasTypes.IF, PasTypes.CASE, PasTypes.WITH,
            PasTypes.GOTO, PasTypes.EXIT,
            PasTypes.TRY, PasTypes.RAISE,
            PasTypes.END
    );
    static final TokenSet DECLARATIONS_IMPL = TokenSet.create(
            PasTypes.CONSTRUCTOR, PasTypes.DESTRUCTOR
    );
    static final TokenSet DECLARATIONS_INTF = TokenSet.create(
            PasTypes.VAR, PasTypes.CONST, PasTypes.TYPE, PasTypes.THREADVAR, PasTypes.RESOURCESTRING,
            PasTypes.PROCEDURE, PasTypes.FUNCTION
    );
    static final TokenSet MODULE_HEADERS = TokenSet.create(PasTypes.PROGRAM, PasTypes.UNIT, PasTypes.LIBRARY, PasTypes.PACKAGE);
    static final TokenSet STRUCT_DECLARATIONS = TokenSet.create(
            PasTypes.PROCEDURE, PasTypes.FUNCTION, PasTypes.CONSTRUCTOR, PasTypes.DESTRUCTOR,
            PasTypes.OPERATOR, PasTypes.PROPERTY, PasTypes.END
    );
    static final TokenSet VISIBILITY = TokenSet.create(PasTypes.PRIVATE, PasTypes.PROTECTED, PasTypes.PUBLIC, PasTypes.PUBLISHED, PasTypes.AUTOMATED);
    static final TokenSet TS_BEGIN = TokenSet.create(PasTypes.BEGIN);
    static final TokenSet TS_ELSE = TokenSet.create(PasTypes.ELSE);
    static final TokenSet DECLARATIONS_LOCAL = TokenSet.create(PasTypes.VAR, PasTypes.CONST, PasTypes.TYPE, PasTypes.PROCEDURE, PasTypes.FUNCTION);
    static final TokenSet TYPE_DECLARATIONS = TokenSet.create(
            PasTypes.CLASS, PasTypes.OBJC_CLASS, PasTypes.DISPINTERFACE, PasTypes.RECORD, PasTypes.OBJECT,
            PasTypes.PACKED, PasTypes.SET, PasTypes.FILE, PasTypes.ARRAY
    );
    static final TokenSet PROPERTY_SPECIFIERS = TokenSet.create(PasTypes.READ, PasTypes.WRITE);

    static final InsertHandler<LookupElement> RECORD_INSERT_HANDLER = new InsertHandler<LookupElement>() {
        @Override
        public void handleInsert(final InsertionContext context, LookupElement item) {
            PasRecordDecl record = (PasRecordDecl) item.getObject();
            StringBuilder sb = new StringBuilder("(");
            for (PasClassField field : record.getClassFieldList()) {
                for (PasNamedIdentDecl namedIdentDecl : field.getNamedIdentDeclList()) {
                    String caretPH;
                    if (sb.length() != 1) {
                        caretPH = "";
                        sb.append("; ");
                    } else {
                        caretPH = DocUtil.PLACEHOLDER_CARET;
                    }
                    sb.append(namedIdentDecl.getName()).append(": ").append(caretPH);
                }
            }
            sb.append(");");
            int caretPos = context.getEditor().getCaretModel().getOffset();
            DocUtil.adjustDocument(context.getEditor(), caretPos, sb.toString());
            context.commitDocument();
        }
    };

    private static Map<IElementType, TokenSet> TOKEN_TO_PAS = initTokenToPasToken();

    private static Map<IElementType, TokenSet> initTokenToPasToken() {
        Map<IElementType, TokenSet> result = new HashMap<>();
        result.put(PascalLexer.PROGRAM, TokenSet.create(PascalLexer.PROGRAM, PasTypes.PROGRAM_MODULE_HEAD));
        result.put(PascalLexer.UNIT, TokenSet.create(PascalLexer.UNIT, PasTypes.UNIT_MODULE_HEAD));
        result.put(PascalLexer.LIBRARY, TokenSet.create(PascalLexer.LIBRARY, PasTypes.LIBRARY_MODULE_HEAD));
        result.put(PascalLexer.PACKAGE, TokenSet.create(PascalLexer.PACKAGE, PasTypes.PACKAGE_MODULE_HEAD));
        result.put(PascalLexer.CONTAINS, TokenSet.create(PascalLexer.CONTAINS, PasTypes.CONTAINS_CLAUSE));
        result.put(PascalLexer.REQUIRES, TokenSet.create(PascalLexer.REQUIRES, PasTypes.REQUIRES_CLAUSE));

        result.put(PascalLexer.INTERFACE, TokenSet.create(PascalLexer.INTERFACE, PasTypes.UNIT_INTERFACE));
        result.put(PascalLexer.IMPLEMENTATION, TokenSet.create(PascalLexer.IMPLEMENTATION, PasTypes.UNIT_IMPLEMENTATION));
        result.put(PascalLexer.INITIALIZATION, TokenSet.create(PascalLexer.INITIALIZATION, PasTypes.UNIT_INITIALIZATION));
        result.put(PascalLexer.FINALIZATION, TokenSet.create(PascalLexer.FINALIZATION, PasTypes.UNIT_FINALIZATION));

        result.put(PascalLexer.USES, TokenSet.create(PascalLexer.USES, PasTypes.USES_CLAUSE));
        result.put(PascalLexer.EXCEPT, TokenSet.create(PasTypes.EXCEPT, PasTypes.FINALLY));
        result.put(PascalLexer.FINALLY, TokenSet.create(PasTypes.FINALLY, PasTypes.EXCEPT));

        result.put(PascalLexer.ELSE, TokenSet.create(PascalLexer.ELSE, PasTypes.CASE_ELSE));
        result.put(PascalLexer.UNTIL, TokenSet.create(PascalLexer.UNTIL));

        result.put(PascalLexer.BEGIN, TokenSet.create(PascalLexer.BEGIN, PasTypes.COMPOUND_STATEMENT, PasTypes.BLOCK_BODY, PasTypes.PROC_BODY_BLOCK));
        return result;
    }

    private static Collection<PascalStubElement> findSymbols(Project project, String key) {
        Collection<PascalStubElement> result = new SmartList<>();
        final MinusculeMatcher matcher = NameUtil.buildMatcher(key).build();
        final GlobalSearchScope scope = ProjectScope.getAllScope(project);
        StubIndex.getInstance().processAllKeys(PascalSymbolIndex.KEY, new Processor<String>() {
            @Override
            public boolean process(final String key) {
                if (matcher.matches(key)) {
                    StubIndex.getInstance().processElements(PascalSymbolIndex.KEY, key, project, scope,
                            PascalNamedElement.class, new Processor<PascalNamedElement>() {
                                @Override
                                public boolean process(PascalNamedElement namedElement) {
                                    result.add((PascalStubElement) namedElement);
                                    return true;
                                }
                            });
                }
                return true;
            }
        }, scope, null);
        return result;
    }

    private static Map<String, String> getInsertMap() {
        Map<String, String> res = new HashMap<String, String>();
        res.put(PasTypes.UNIT.toString(), String.format(" %s;\n\ninterface\n\n  %s\nimplementation\n\nend.\n", PLACEHOLDER_FILENAME, DocUtil.PLACEHOLDER_CARET));
        res.put(PasTypes.PROGRAM.toString(), String.format(" %s;\nbegin\n  %s\nend.\n", PLACEHOLDER_FILENAME, DocUtil.PLACEHOLDER_CARET));
        res.put(PasTypes.LIBRARY.toString(), String.format(" %s;\n\nexports %s\n\nbegin\n\nend.\n", PLACEHOLDER_FILENAME, DocUtil.PLACEHOLDER_CARET));
        res.put(PasTypes.PACKAGE.toString(), String.format(" %s;\n\nrequires\n\n contains %s\n\nend.\n", PLACEHOLDER_FILENAME, DocUtil.PLACEHOLDER_CARET));
        res.put(PasTypes.BEGIN.toString(), String.format("\n%s\nend;\n", DocUtil.PLACEHOLDER_CARET));
        res.put(PasTypes.BEGIN.toString() + " ", String.format("\n%s\nend.\n", DocUtil.PLACEHOLDER_CARET));
        res.put(PasTypes.END.toString(), ";");
        res.put(PasTypes.INTERFACE.toString(), String.format("\n  %s\nimplementation\n", DocUtil.PLACEHOLDER_CARET));
        res.put(PasTypes.INITIALIZATION.toString(), String.format("\n  %s\nfinalization\n", DocUtil.PLACEHOLDER_CARET));
        res.put(PasTypes.USES.toString(), String.format(" %s;", DocUtil.PLACEHOLDER_CARET));

        res.put(PasTypes.FOR.toString(), String.format(" %s to do ;", DocUtil.PLACEHOLDER_CARET));
        res.put(PasTypes.WHILE.toString(), String.format(" %s do ;", DocUtil.PLACEHOLDER_CARET));
        res.put(PasTypes.REPEAT.toString(), String.format("\nuntil %s;", DocUtil.PLACEHOLDER_CARET));

        res.put(PasTypes.IF.toString(), String.format(" %s then ;\n", DocUtil.PLACEHOLDER_CARET));
        res.put(PasTypes.CASE.toString(), String.format(" %s of\nend;", DocUtil.PLACEHOLDER_CARET));

        res.put(PasTypes.THEN.toString(), String.format(" %s", DocUtil.PLACEHOLDER_CARET));
        res.put(PasTypes.DO.toString(), String.format(" %s", DocUtil.PLACEHOLDER_CARET));

        res.put(PasTypes.WITH.toString(), String.format(" %s do ;", DocUtil.PLACEHOLDER_CARET));

        res.put(PasTypes.TRY.toString(), String.format("\n  %s\nfinally\nend;", DocUtil.PLACEHOLDER_CARET));

        res.put(PasTypes.RECORD.toString(), String.format("  %s\nend;", DocUtil.PLACEHOLDER_CARET));
        res.put(PasTypes.OBJECT.toString(), String.format("  %s\nend;", DocUtil.PLACEHOLDER_CARET));
        res.put(PasTypes.CLASS.toString(), String.format("(TObject)\nprivate\n%s\npublic\nend;", DocUtil.PLACEHOLDER_CARET));
        res.put(PasTypes.OBJC_CLASS.toString(), String.format("(NSObject)\n%s\npublic\nend;", DocUtil.PLACEHOLDER_CARET));
        res.put(PasTypes.INTERFACE.toString() + " ", String.format("(IUnknown)\n%s\nend;", DocUtil.PLACEHOLDER_CARET));
        res.put(PasTypes.ARRAY.toString(), String.format(" of %s;", DocUtil.PLACEHOLDER_CARET));
        res.put(PasTypes.ARRAY.toString() + "[", String.format("[0..%s] of ;", DocUtil.PLACEHOLDER_CARET));
        res.put(PasTypes.SET.toString(), String.format(" of %s;", DocUtil.PLACEHOLDER_CARET));
        res.put(PasTypes.CLASS.toString() + " of", String.format(" %s;", DocUtil.PLACEHOLDER_CARET));
        res.put(PasTypes.CLASS.toString() + " helper", String.format(" for T%s\n\nend;", DocUtil.PLACEHOLDER_CARET));
        res.put(PasTypes.RECORD.toString() + " helper", String.format(" for T%s\n\nend;", DocUtil.PLACEHOLDER_CARET));

        res.put(PasTypes.CONSTRUCTOR.toString(), String.format(" Create(%s);", DocUtil.PLACEHOLDER_CARET));
        res.put(PasTypes.DESTRUCTOR.toString(), String.format(" Destroy(%s); override;", DocUtil.PLACEHOLDER_CARET));
        res.put(PasTypes.CONSTRUCTOR.toString() + " ", String.format(" Create(%s);", DocUtil.PLACEHOLDER_CARET));
        res.put(PasTypes.DESTRUCTOR.toString() + " ", String.format(" Destroy(%s); override;", DocUtil.PLACEHOLDER_CARET));
        res.put(PasTypes.FUNCTION.toString(), String.format(" %s(): ;", DocUtil.PLACEHOLDER_CARET));
        res.put(PasTypes.PROCEDURE.toString(), String.format(" %s();", DocUtil.PLACEHOLDER_CARET));

        res.put(PasTypes.VAR.toString(), String.format(" %s: ;", DocUtil.PLACEHOLDER_CARET));
        res.put(PasTypes.THREADVAR.toString(), String.format(" %s: ;", DocUtil.PLACEHOLDER_CARET));
        res.put(PasTypes.CONST.toString(), String.format(" %s = ;", DocUtil.PLACEHOLDER_CARET));
        res.put(PasTypes.RESOURCESTRING.toString(), String.format(" %s = '';", DocUtil.PLACEHOLDER_CARET));
        res.put(PasTypes.TYPE.toString(), String.format(" T%s = ;", DocUtil.PLACEHOLDER_CARET));
        res.put(PasTypes.TYPE.toString() + " ", String.format(" T%s;", DocUtil.PLACEHOLDER_CARET));

        res.put(PasTypes.PROPERTY.toString(), String.format(" %s: read ;", DocUtil.PLACEHOLDER_CARET));

        res.put(PasTypes.PACKED.toString(), " ");

        res.put(PasTypes.UNTIL.toString(), String.format(" %s;", DocUtil.PLACEHOLDER_CARET));
        res.put(PasTypes.ON.toString(), String.format(" E: %s do", DocUtil.PLACEHOLDER_CARET));
        res.put(PasTypes.EXCEPT.toString(), "\n");
        return res;
    }

    static void appendTokenSet(CompletionResultSet result, TokenSet tokenSet) {
        for (IElementType op : tokenSet.getTypes()) {
            result.caseInsensitive().addElement(getElement(op.toString()));
        }
    }

    static LookupElement getElement(String s) {
        return getElement(s, PascalIcons.GENERAL);
    }

    static LookupElement getElement(String s, Icon icon) {
        return LookupElementBuilder.create(s).withIcon(icon).withStrikeoutness(s.equals(PasTypes.GOTO.toString())).withInsertHandler(INSERT_HANDLER);
    }

    static void appendTokenSetIfAbsent(CompletionResultSet result, TokenSet tokenSet, PsiElement position, Class... classes) {
        if (PsiTreeUtil.findChildOfAnyType(position, classes) == null) {
            for (IElementType op : tokenSet.getTypes()) {
                LookupElementBuilder el = LookupElementBuilder.create(op.toString()).withIcon(PascalIcons.GENERAL).withStrikeoutness(op.equals(PasTypes.GOTO)).withInsertHandler(INSERT_HANDLER);
                result.caseInsensitive().addElement(el);
            }
        }
    }

    static void appendTokenSetUnique(CompletionResultSet result, TokenSet tokenSet, PsiElement position) {
        for (IElementType op : tokenSet.getTypes()) {
            appendTokenSetUnique(result, op, position);
        }
    }

    static void appendTokenSetUnique(CompletionResultSet result, IElementType op, PsiElement position) {
        TokenSet tokensToFind = TOKEN_TO_PAS.get(op) != null ? TOKEN_TO_PAS.get(op) : TokenSet.create(op);
        if (position.getNode().getChildren(tokensToFind).length > 0) {  //===*** TODO: remove
            return;
        }
        PsiErrorElement error = PsiTreeUtil.getChildOfType(position, PsiErrorElement.class);
        if (error != null && error.getNode().getChildren(tokensToFind).length > 0) {
            return;
        }
        /*for (PsiElement psiElement : position.getChildren()) {
            if (psiElement.getNode().findLeafElementAt(0).getElementType() == op) {
                return;
            }
        }*/
//        if ((TOKEN_TO_PSI.get(op) == null) || (PsiTreeUtil.findChildOfType(position, TOKEN_TO_PSI.get(op), true) == null)) {
        LookupElementBuilder el = LookupElementBuilder.create(op.toString()).withIcon(PascalIcons.GENERAL).withStrikeoutness(op.equals(PasTypes.GOTO)).withInsertHandler(INSERT_HANDLER);
        result.caseInsensitive().addElement(el);
//        }
    }

    static void handleUses(CompletionResultSet result, @NotNull PsiElement pos) {
        PasModule module = PsiUtil.getElementPasModule(pos);
        Set<String> excludedUnits = new HashSet<String>();
        if (module != null) {
            excludedUnits.add(module.getName().toUpperCase());
            for (SmartPsiElementPointer<PasEntityScope> scopePtr : module.getPublicUnits()) {
                if (scopePtr.getElement() != null) {
                    excludedUnits.add(scopePtr.getElement().getName().toUpperCase());
                }
            }
            for (SmartPsiElementPointer<PasEntityScope> scopePtr : module.getPrivateUnits()) {
                if (scopePtr.getElement() != null) {
                    excludedUnits.add(scopePtr.getElement().getName().toUpperCase());
                }
            }
        }
        for (VirtualFile file : PasReferenceUtil.findUnitFiles(pos.getProject(), com.intellij.openapi.module.ModuleUtil.findModuleForPsiElement(pos))) {
            if (!excludedUnits.contains(file.getNameWithoutExtension().toUpperCase())) {
                LookupElementBuilder lookupElement = LookupElementBuilder.create(file.getNameWithoutExtension());
                result.caseInsensitive().addElement(lookupElement.withTypeText(file.getExtension() != null ? file.getExtension() : "", false));
            }
        }
    }

    static ASTNode getDoThenOf(PsiElement statement) {
        ASTNode[] cand = statement.getNode().getChildren(TS_DO_THEN_OF);
        return cand.length > 0 ? cand[0] : null;
    }

    static void appendText(CompletionResultSet result, String s) {
        result.caseInsensitive().addElement(getElement(s));
    }

    static LookupElementBuilder createLookupElement(final Editor editor, @NotNull PasField field) {
        if (!PsiUtil.isElementUsable(field.getElement())) {
            return null;
        }
        LookupElementBuilder res = LookupElementBuilder.create(field.getElement()).withPresentableText(getFieldText(field));
        if (field.fieldType == PasField.FieldType.ROUTINE) {
            PascalNamedElement el = field.getElement();
            final String content = (el instanceof PascalRoutine && ((PascalRoutine) el).hasParameters()) ? "(" + DocUtil.PLACEHOLDER_CARET + ")" : "()" + DocUtil.PLACEHOLDER_CARET;
            res = res.withInsertHandler(new InsertHandler<LookupElement>() {
                @Override
                public void handleInsert(InsertionContext context, LookupElement item) {
                    handleRoutineNameInsertion(editor, content);
                }
            });
        }
        return res;
    }

    static LookupElementBuilder createLookupElement(final CompletionParameters parameters, @NotNull PascalNamedElement namedElement, String unitName) {
        if (!PsiUtil.isElementUsable(namedElement)) {
            return null;
        }
        LookupElementBuilder res = LookupElementBuilder.create(namedElement).withPresentableText(namedElement.getNamePart());
        final String content = namedElement.getType() == PasField.FieldType.ROUTINE ? (namedElement instanceof PascalRoutine && ((PascalRoutine) namedElement).hasParameters()) ? "(" + DocUtil.PLACEHOLDER_CARET + ")" : "()" + DocUtil.PLACEHOLDER_CARET : null;
        res = res.withInsertHandler(new InsertHandler<LookupElement>() {
            @Override
            public void handleInsert(InsertionContext context, LookupElement item) {
                boolean toInterface = ContextUtil.belongsToInterface(parameters.getPosition());
                UsesActions.AddUnitAction actAddUnit = new UsesActions.AddUnitAction(PascalBundle.message("action.add.uses", unitName),
                        unitName, toInterface);
                actAddUnit.invoke(parameters.getOriginalFile().getProject(), parameters.getEditor(), parameters.getOriginalFile());
                EditorUtil.showInformationHint(parameters.getEditor(), PascalBundle.message("action.unit.search.added", unitName,
                        PascalBundle.message(toInterface ? "unit.section.interface": "unit.section.implementation")));
                handleRoutineNameInsertion(context.getEditor(), content);
            }
        });
        return res;
    }

    private static void handleRoutineNameInsertion(Editor editor, String content) {
        if (content != null) {
            DocUtil.adjustDocument(editor, editor.getCaretModel().getOffset(), content);
            AnAction act = ActionManager.getInstance().getAction("ParameterInfo");
            DataContext dataContext = DataManager.getInstance().getDataContext(editor.getContentComponent());
            act.actionPerformed(new AnActionEvent(null, dataContext, "", act.getTemplatePresentation(), ActionManager.getInstance(), 0));
        }
    }

    private static String getFieldText(PasField field) {
        PascalNamedElement el = field.getElement();
        if (el instanceof PascalIdentDecl) {
            String type = ((PascalIdentDecl) el).getTypeString();
            return PsiUtil.getFieldName(el) + ": " + (type != null ? type : TYPE_UNTYPED);
        }
        return PsiUtil.getFieldName(el);
    }

    static boolean buildFromElement(@NotNull PasField field) {
        return (field.getElementPtr() != null) && (field.fieldType != PasField.FieldType.PSEUDO_VARIABLE);
    }

    static void addEntitiesToResult(CompletionResultSet result, Map<String, LookupElement> entities) {
        result.caseInsensitive().addAllElements(entities.values());
    }

    static void fillBoost(EntityCompletionContext completionContext) {
        Context ctx = completionContext.context;
        Collection<String> result = completionContext.boostNames;
        if (ctx.getPrimary() == CodePlace.EXPR) {
            if (ctx.contains(CodePlace.EXPR_ARGUMENT) && ctx.getPosition() instanceof PasArgumentList) {
                int parNum = getParameterIndex((PasArgumentList) ctx.getPosition(), completionContext.completionParameters.getOffset());
                PsiElement parent = ctx.getPosition().getParent();
                if (parent instanceof PasCallExpr) {
                    ParamModifier mod = ParamModifier.NONE;
                    for (PascalRoutineEntity routineEntity : PasReferenceUtil.resolveRoutines((PasCallExpr) parent)) {
                        List<String> names = routineEntity.getFormalParameterNames();
                        if (names.size() > parNum) {
                            result.add(names.get(parNum));
                            mod = routineEntity.getFormalParameterAccess().get(parNum);
                        }
                    }
                    if (result.isEmpty()) {
                        completionContext.deniedName = retrieveNames(result, parent);
                    }
                    if ((mod != ParamModifier.VAR) && (mod != ParamModifier.OUT)) {
                        completionContext.likelyTypes = EnumSet.of(PasField.FieldType.VARIABLE, PasField.FieldType.CONSTANT, PasField.FieldType.PROPERTY, PasField.FieldType.ROUTINE);
                    } else {
                        completionContext.likelyTypes = EnumSet.of(PasField.FieldType.VARIABLE);
                        completionContext.deniedTypes = EnumSet.of(PasField.FieldType.CONSTANT, PasField.FieldType.PROPERTY, PasField.FieldType.ROUTINE, PasField.FieldType.TYPE, PasField.FieldType.UNIT);
                    }
                }
            } else if (ctx.contains(CodePlace.ASSIGN_LEFT) && ctx.getPosition() instanceof PasStatement) {
                PasAssignPart assignPart = PsiTreeUtil.findChildOfType(ctx.getPosition(), PasAssignPart.class);
                if (assignPart != null) {
                    completionContext.deniedName = retrieveNames(result, assignPart.getExpression());
                    completionContext.likelyTypes = EnumSet.of(PasField.FieldType.VARIABLE, PasField.FieldType.PROPERTY);
                    completionContext.deniedTypes = EnumSet.of(PasField.FieldType.CONSTANT, PasField.FieldType.TYPE, PasField.FieldType.ROUTINE, PasField.FieldType.UNIT);
                }
            } else if (ctx.contains(CodePlace.ASSIGN_RIGHT) && ctx.getPosition() instanceof PasAssignPart) {
                PsiElement parent = ctx.getPosition().getParent();
                if (parent instanceof PasStatement) {
                    completionContext.deniedName = retrieveNames(result, ((PasStatement) parent).getExpression());
                    completionContext.likelyTypes = EnumSet.of(PasField.FieldType.VARIABLE, PasField.FieldType.PROPERTY, PasField.FieldType.CONSTANT, PasField.FieldType.ROUTINE);
                }
            }
        }
    }

    private static String retrieveNames(Collection<String> result, PsiElement expression) {
        PasReferenceExpr ref = PsiTreeUtil.findChildOfType(expression, PasReferenceExpr.class);
        if (ref != null) {
            String name = ref.getFullyQualifiedIdent().getNamePart();
            if (StringUtil.isNotEmpty(name)) {
                result.addAll(Arrays.asList(StrUtil.extractWords(name, StrUtil.ElementType.VAR)));
            }
            return name;
        }
        return null;
    }

    private static int getParameterIndex(PasArgumentList argList, int offset) {
        List<PasExpr> exprList = argList.getExprList();
        for (int i = 0; i < exprList.size(); i++) {
            if (exprList.get(i).getTextRange().contains(offset)) {
                return i;
            }
        }
        return 0;
    }
}
