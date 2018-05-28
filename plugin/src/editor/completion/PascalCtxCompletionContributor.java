package com.siberika.idea.pascal.editor.completion;

import com.intellij.codeInsight.completion.CompletionContributor;
import com.intellij.codeInsight.completion.CompletionInitializationContext;
import com.intellij.codeInsight.completion.CompletionParameters;
import com.intellij.codeInsight.completion.CompletionProvider;
import com.intellij.codeInsight.completion.CompletionResultSet;
import com.intellij.codeInsight.completion.CompletionType;
import com.intellij.codeInsight.completion.PrioritizedLookupElement;
import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.lang.ASTNode;
import com.intellij.patterns.PlatformPatterns;
import com.intellij.psi.PsiComment;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.tree.TokenSet;
import com.intellij.util.ProcessingContext;
import com.siberika.idea.pascal.PascalLanguage;
import com.siberika.idea.pascal.editor.ContextAwareVirtualFile;
import com.siberika.idea.pascal.lang.context.Context;
import com.siberika.idea.pascal.lang.lexer.PascalLexer;
import com.siberika.idea.pascal.lang.parser.NamespaceRec;
import com.siberika.idea.pascal.lang.psi.PasExportedRoutine;
import com.siberika.idea.pascal.lang.psi.PasFormalParameter;
import com.siberika.idea.pascal.lang.psi.PasLibraryModuleHead;
import com.siberika.idea.pascal.lang.psi.PasModule;
import com.siberika.idea.pascal.lang.psi.PasPackageModuleHead;
import com.siberika.idea.pascal.lang.psi.PasProgramModuleHead;
import com.siberika.idea.pascal.lang.psi.PasTypes;
import com.siberika.idea.pascal.lang.psi.PasUnitModuleHead;
import com.siberika.idea.pascal.lang.psi.PascalModule;
import com.siberika.idea.pascal.lang.psi.PascalNamedElement;
import com.siberika.idea.pascal.lang.psi.PascalRoutine;
import com.siberika.idea.pascal.lang.psi.PascalStructType;
import com.siberika.idea.pascal.lang.psi.impl.PasField;
import com.siberika.idea.pascal.lang.references.PasReferenceUtil;
import com.siberika.idea.pascal.lang.references.ResolveContext;
import com.siberika.idea.pascal.lang.references.ResolveUtil;
import com.siberika.idea.pascal.util.DocUtil;
import com.siberika.idea.pascal.util.PsiUtil;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static com.siberika.idea.pascal.lang.context.CodePlace.ASSIGN_LEFT;
import static com.siberika.idea.pascal.lang.context.CodePlace.CONST_EXPRESSION;
import static com.siberika.idea.pascal.lang.context.CodePlace.DECL_CONST;
import static com.siberika.idea.pascal.lang.context.CodePlace.DECL_FIELD;
import static com.siberika.idea.pascal.lang.context.CodePlace.DECL_TYPE;
import static com.siberika.idea.pascal.lang.context.CodePlace.DECL_VAR;
import static com.siberika.idea.pascal.lang.context.CodePlace.EXPR;
import static com.siberika.idea.pascal.lang.context.CodePlace.FIRST_IN_EXPR;
import static com.siberika.idea.pascal.lang.context.CodePlace.FIRST_IN_NAME;
import static com.siberika.idea.pascal.lang.context.CodePlace.GLOBAL;
import static com.siberika.idea.pascal.lang.context.CodePlace.GLOBAL_DECLARATION;
import static com.siberika.idea.pascal.lang.context.CodePlace.INTERFACE;
import static com.siberika.idea.pascal.lang.context.CodePlace.LOCAL;
import static com.siberika.idea.pascal.lang.context.CodePlace.LOCAL_DECLARATION;
import static com.siberika.idea.pascal.lang.context.CodePlace.MODULE_HEADER;
import static com.siberika.idea.pascal.lang.context.CodePlace.PROPERTY_SPECIFIER;
import static com.siberika.idea.pascal.lang.context.CodePlace.STATEMENT;
import static com.siberika.idea.pascal.lang.context.CodePlace.STMT_CASE_ITEM;
import static com.siberika.idea.pascal.lang.context.CodePlace.STMT_EXCEPT;
import static com.siberika.idea.pascal.lang.context.CodePlace.STMT_FOR;
import static com.siberika.idea.pascal.lang.context.CodePlace.STMT_IF_THEN;
import static com.siberika.idea.pascal.lang.context.CodePlace.STMT_REPEAT;
import static com.siberika.idea.pascal.lang.context.CodePlace.STMT_TRY;
import static com.siberika.idea.pascal.lang.context.CodePlace.STMT_WHILE;
import static com.siberika.idea.pascal.lang.context.CodePlace.TYPE_ID;
import static com.siberika.idea.pascal.lang.context.CodePlace.UNKNOWN;
import static com.siberika.idea.pascal.lang.context.CodePlace.USES;

public class PascalCtxCompletionContributor extends CompletionContributor {

    private static final TokenSet TS_BEGIN = TokenSet.create(PasTypes.BEGIN);
    private static final TokenSet TS_ELSE = TokenSet.create(PasTypes.ELSE);
    private static final TokenSet TS_CLASS = TokenSet.create(PasTypes.CLASS);
    private static final TokenSet DECLARATIONS_LOCAL = TokenSet.create(PasTypes.VAR, PasTypes.CONST, PasTypes.TYPE, PasTypes.PROCEDURE, PasTypes.FUNCTION);

    private static final TokenSet TYPE_DECLARATIONS = TokenSet.create(
            PasTypes.CLASS, PasTypes.OBJC_CLASS, PasTypes.DISPINTERFACE, PasTypes.RECORD, PasTypes.OBJECT,
            PasTypes.PACKED, PasTypes.SET, PasTypes.FILE, PasTypes.ARRAY
    );

    @Override
    public boolean invokeAutoPopup(@NotNull PsiElement position, char typeChar) {
        return position instanceof PsiComment && typeChar == '$';
    }

    @Override
    public void beforeCompletion(@NotNull CompletionInitializationContext context) {
        super.beforeCompletion(context);
        context.setDummyIdentifier(PasField.DUMMY_IDENTIFIER);
    }

    @SuppressWarnings("unchecked")
    public PascalCtxCompletionContributor() {
        extend(CompletionType.BASIC, PlatformPatterns.psiElement().withLanguage(PascalLanguage.INSTANCE), new CompletionProvider<CompletionParameters>() {
            @Override
            protected void addCompletions(@NotNull CompletionParameters parameters, ProcessingContext context, @NotNull CompletionResultSet result) {
                Context ctx = new Context(parameters.getOriginalPosition(), parameters.getPosition(), parameters.getOriginalFile());

                Map<String, LookupElement> entities = new HashMap<>();

                if ((ctx.getPosition() instanceof PsiFile) && (((PsiFile) ctx.getPosition()).getVirtualFile() instanceof ContextAwareVirtualFile)) {
                    NamespaceRec namespace = NamespaceRec.fromFQN(ctx.getPosition(), ctx.getPosition().getText().replace(PasField.DUMMY_IDENTIFIER, "")); // TODO: refactor
                    namespace.setIgnoreVisibility(true);
                    namespace.clearTarget();
                    ResolveContext resolveContext = new ResolveContext(PsiUtil.getNearestAffectingScope(((ContextAwareVirtualFile) ((PsiFile) ctx.getPosition()).getVirtualFile()).getContextElement()),
                            PasField.TYPES_ALL, false, null);
                    fieldsToEntities(entities, PasReferenceUtil.resolve(namespace, resolveContext, 0), parameters);
                    addEntitiesToResult(result, entities);
                    result.stopHere();
                    return;
                }

                if (parameters.getOriginalPosition() instanceof PsiComment) {
                    PascalCompletionInComment.handleComments(result, parameters);
                    result.stopHere();
                    return;
                }

                if (ctx.getPrimary() == EXPR) {
                    if (ctx.contains(FIRST_IN_NAME) && ctx.contains(FIRST_IN_EXPR) && !ctx.withinBraces()) {
                        CompletionUtil.appendTokenSet(result, PascalLexer.STATEMENTS);
                        if (ctx.contains(STMT_TRY)) {
                            CompletionUtil.appendTokenSetUnique(result, PasTypes.EXCEPT, ctx.getPosition().getParent());
                            CompletionUtil.appendTokenSetUnique(result, PasTypes.FINALLY, ctx.getPosition().getParent());
                            CompletionUtil.appendTokenSet(result, TokenSet.create(PasTypes.ON));
                        }
                        if (ctx.contains(STMT_REPEAT)) {
                            CompletionUtil.appendTokenSetUnique(result, PasTypes.UNTIL, ctx.getPosition().getParent());
                        }
                        if (DO_THEN_OF_MAP.containsKey(ctx.getPosition().getParent().getNode().getElementType())) {
                            CompletionUtil.appendTokenSetUnique(result, TS_BEGIN, ctx.getPosition());
                        }
                        if (ctx.contains(STMT_FOR) || ctx.contains(STMT_WHILE) || ctx.contains(STMT_REPEAT)) {
                            CompletionUtil.appendTokenSet(result, PascalLexer.STATEMENTS_IN_CYCLE);
                        }
                    }
                    if (ctx.contains(CONST_EXPRESSION)) {
                        addEntities(result, entities, ctx, PasField.TYPES_STATIC, parameters);
                    } else if (ctx.contains(ASSIGN_LEFT)) {
                        addEntities(result, entities, ctx, PasField.TYPES_LEFT_SIDE, parameters);
                    } else {
                        addEntities(result, entities, ctx, PasField.TYPES_ALL, parameters);
                        if (ctx.contains(FIRST_IN_NAME)) {
                            CompletionUtil.appendTokenSet(result, PascalLexer.VALUES);
                        }
                    }
                } else if ((ctx.getPrimary() == STATEMENT) || (ctx.getPrimary() == STMT_EXCEPT)) {
                    if (!getDoThenOf(result, ctx, parameters.getOffset()) && (ctx.contains(STMT_IF_THEN))) {
                        CompletionUtil.appendTokenSet(result, TS_ELSE);
                    }
                } else if (ctx.getPrimary() == STMT_CASE_ITEM) {
                    CompletionUtil.appendTokenSetUnique(result, PasTypes.ELSE, ctx.getPosition().getParent());
                    addEntities(result, entities, ctx, PasField.TYPES_STATIC, parameters);
                }

                if ((ctx.getPosition() instanceof PasFormalParameter) && (((PasFormalParameter) ctx.getPosition()).getParamType() == null)) {
                    CompletionUtil.appendText(result, "const ");
                    CompletionUtil.appendText(result, "var ");
                    CompletionUtil.appendText(result, "out ");
                }

                if (ctx.getPrimary() == TYPE_ID ) {
                    addEntities(result, entities, ctx, PasField.TYPES_TYPE_UNIT, parameters);
                    if (ctx.contains(DECL_TYPE)) {
                        CompletionUtil.appendTokenSet(result, TYPE_DECLARATIONS);
                        result.caseInsensitive().addElement(CompletionUtil.getElement("interface "));
                        result.caseInsensitive().addElement(CompletionUtil.getElement("class helper"));
                        result.caseInsensitive().addElement(CompletionUtil.getElement("record helper"));
                        result.caseInsensitive().addElement(CompletionUtil.getElement("class of"));
                        result.caseInsensitive().addElement(CompletionUtil.getElement("type "));
                    } else if (ctx.contains(DECL_VAR)) {
                        CompletionUtil.appendTokenSet(result, TokenSet.create(
                                PasTypes.RECORD, PasTypes.PACKED, PasTypes.SET, PasTypes.FILE, PasTypes.ARRAY
                        ));
                    }
                } else if (ctx.getPrimary() == PROPERTY_SPECIFIER) {
                    addEntities(result, entities, ctx, PasField.TYPES_PROPERTY_SPECIFIER, parameters);
                }

                if (ctx.getPrimary() == DECL_FIELD) {
                    if (DocUtil.isFirstOnLine(parameters.getEditor(), parameters.getPosition())) {
                        CompletionUtil.appendTokenSet(result, PascalLexer.VISIBILITY);
                        CompletionUtil.appendText(result, "strict private");
                        CompletionUtil.appendText(result, "strict protected");
                    }
                    CompletionUtil.appendTokenSet(result, TokenSet.andNot(PascalLexer.STRUCT_DECLARATIONS, TS_CLASS));
                    CompletionUtil.appendText(result, "class ");
                    CompletionUtil.appendTokenSet(result, DECLARATIONS_LOCAL);
                }

                if (ctx.getPrimary() == UNKNOWN) {
                    CompletionUtil.appendTokenSetIfAbsent(result, PascalLexer.MODULE_HEADERS, parameters.getOriginalFile(),
                            PasProgramModuleHead.class, PasUnitModuleHead.class, PasLibraryModuleHead.class, PasPackageModuleHead.class);
                    result.caseInsensitive().addElement(CompletionUtil.getElement("begin "));
                } else if (ctx.getPrimary() == MODULE_HEADER) {
                    handleModuleSection(result, parameters);
                }
                if (ctx.getPrimary() == USES) {
                    CompletionUtil.handleUses(result, parameters.getPosition());
                }

                if ((ctx.getPrimary() == DECL_TYPE) || (ctx.getPrimary() == DECL_VAR) || (ctx.getPrimary() == DECL_CONST) || (ctx.getPrimary() == GLOBAL_DECLARATION) || (ctx.getPrimary() == LOCAL_DECLARATION)) {
                    if (ctx.getPrimary() == GLOBAL_DECLARATION || ctx.contains(GLOBAL)) {
                        CompletionUtil.appendTokenSet(result, PascalLexer.DECLARATIONS_INTF);
                        CompletionUtil.appendTokenSetUnique(result, PascalLexer.USES, PsiUtil.skipToExpressionParent(parameters.getPosition()));
                        if (!ctx.contains(INTERFACE)) {
                            result.caseInsensitive().addElement(CompletionUtil.getElement("begin  "));
                            CompletionUtil.appendTokenSet(result, PascalLexer.DECLARATIONS_IMPL);
                            PasModule mod = PsiUtil.getElementPasModule(ctx.getPosition());
                            if ((mod != null) && (mod.getModuleType() == PascalModule.ModuleType.UNIT)) {
                                CompletionUtil.appendTokenSetUnique(result, TokenSet.create(PasTypes.INITIALIZATION, PasTypes.FINALIZATION),
                                        PsiUtil.getModuleImplementationSection(parameters.getOriginalFile()));
                            }
                        }
                    } else if (ctx.getPrimary() == LOCAL_DECLARATION || ctx.contains(LOCAL)) {
                        if (DocUtil.isFirstOnLine(parameters.getEditor(), parameters.getPosition())) {
                            CompletionUtil.appendTokenSet(result, DECLARATIONS_LOCAL);
                        } else if (ctx.getPosition() instanceof PascalRoutine) {
                            PascalRoutine routine = (PascalRoutine) ctx.getPosition();
                            if (routine != null) {
                                if (routine.getContainingScope() instanceof PascalStructType) {
                                    if (routine instanceof PasExportedRoutine) {                   // Directives should appear in the class declaration only, not in the defining declaration
                                        CompletionUtil.appendTokenSet(result, PascalLexer.DIRECTIVE_METHOD);
                                    }
                                } else {
                                    CompletionUtil.appendTokenSet(result, PascalLexer.DIRECTIVE_ROUTINE);
                                }
                            }
                        }
                        CompletionUtil.appendTokenSet(result, TokenSet.create(PasTypes.BEGIN));
                    }
                }

                addEntitiesToResult(result, entities);

                result.restartCompletionWhenNothingMatches();
            }
        });
    }

    private static Map<IElementType, TokenSet> DO_THEN_OF_MAP = initDoThenOfMap();

    private static Map<IElementType, TokenSet> initDoThenOfMap() {
        Map<IElementType, TokenSet> result = new HashMap<>();
        result.put(PasTypes.IF_STATEMENT, TokenSet.create(PasTypes.THEN));
        result.put(PasTypes.IF_THEN_STATEMENT, TokenSet.create(PasTypes.IF_THEN_STATEMENT));
        result.put(PasTypes.IF_ELSE_STATEMENT, TokenSet.create(PasTypes.IF_ELSE_STATEMENT));
        result.put(PasTypes.FOR_STATEMENT, TokenSet.create(PasTypes.DO));
        result.put(PasTypes.WHILE_STATEMENT, TokenSet.create(PasTypes.DO));
        result.put(PasTypes.WITH_STATEMENT, TokenSet.create(PasTypes.DO));
        result.put(PasTypes.CASE_STATEMENT, TokenSet.create(PasTypes.OF));
        result.put(PasTypes.CASE_ITEM, TokenSet.create(PasTypes.CASE_ITEM));
        result.put(PasTypes.CASE_ELSE, TokenSet.create(PasTypes.CASE_ELSE));
        result.put(PasTypes.HANDLER, TokenSet.create(PasTypes.DO));
        return result;
    }

    private boolean getDoThenOf(CompletionResultSet result, Context ctx, int offset) {
        PsiElement controlStmt = ctx.getPosition();
        TokenSet ts = controlStmt != null ? DO_THEN_OF_MAP.get(controlStmt.getNode().getElementType()) : null;
        if (ts != null) {
            ASTNode doThenOf = CompletionUtil.getDoThenOf(ctx.getPosition());
            if (doThenOf != null) {
                if (doThenOf.getStartOffset() < offset) {
                    CompletionUtil.appendTokenSet(result, TS_BEGIN);
                }
            } else {
                CompletionUtil.appendTokenSet(result, ts);
                return true;
            }
        }
        return false;
    }

    private void handleModuleSection(CompletionResultSet result, CompletionParameters parameters) {
        PsiElement pos = PsiUtil.skipToExpressionParent(parameters.getPosition());
        if (pos instanceof PascalModule) {
            PascalModule module = (PascalModule) pos;
            switch (module.getModuleType()) {
                case UNIT: {
                    if (pos.getTextRange().getStartOffset() < parameters.getOffset()) {
                        CompletionUtil.appendTokenSetUnique(result, PascalLexer.UNIT_SECTIONS, pos);
                    }
                    break;
                }
                case PACKAGE: {
                    CompletionUtil.appendTokenSetUnique(result, PascalLexer.TOP_LEVEL_DECLARATIONS, parameters.getOriginalFile());
                }
                case LIBRARY:
                    result.caseInsensitive().addElement(CompletionUtil.getElement(PasTypes.EXPORTS.toString()));
                    result.caseInsensitive().addElement(CompletionUtil.getElement("begin  "));
                case PROGRAM:
                    CompletionUtil.appendTokenSetUnique(result, TokenSet.create(PascalLexer.USES), pos);
                    CompletionUtil.appendTokenSet(result, PascalLexer.DECLARATIONS_INTF);
                    CompletionUtil.appendTokenSet(result, PascalLexer.DECLARATIONS_IMPL);
                    result.caseInsensitive().addElement(CompletionUtil.getElement("begin  "));
            }
        }
    }

    private static final double PRIORITY_HIGHER = 10.0;
    private static final double PRIORITY_LOWER = -10.0;
    private static final double PRIORITY_LOWEST = -100.0;

    private static void addEntities(CompletionResultSet result, Map<String, LookupElement> entities, Context context, Set<PasField.FieldType> fieldTypes, CompletionParameters parameters) {
        NamespaceRec namespace = NamespaceRec.fromFQN(parameters.getPosition(), PasField.DUMMY_IDENTIFIER);
        if (PsiUtil.isIdent(parameters.getPosition().getParent())) {
            if (parameters.getPosition().getParent().getParent() instanceof PascalNamedElement) {
                namespace = NamespaceRec.fromElement(parameters.getPosition().getParent());
            } else {
                namespace = NamespaceRec.fromFQN(parameters.getPosition(), ((PascalNamedElement) parameters.getPosition()).getName());
            }
        }
        namespace.clearTarget();
        Collection<PasField> fields = PasReferenceUtil.resolveExpr(namespace, new ResolveContext(fieldTypes, true), 0);
        fieldsToEntities(entities, fields, parameters);
    }

    private static void fieldsToEntities(Map<String, LookupElement> entities, Collection<PasField> fields, CompletionParameters parameters) {
        for (PasField pasField : fields) {
            if ((pasField.name != null) && !pasField.name.contains(ResolveUtil.STRUCT_SUFFIX)) {
                LookupElement lookupElement;
                LookupElementBuilder el = buildFromElement(pasField) ? CompletionUtil.createLookupElement(parameters.getEditor(), pasField) : LookupElementBuilder.create(pasField.name);
                lookupElement = el.appendTailText(" : " + pasField.fieldType.toString().toLowerCase(), true).
                        withCaseSensitivity(true).withTypeText(pasField.owner != null ? pasField.owner.getName() : "-", false);
                if (lookupElement.getLookupString().startsWith("_")) {
                    lookupElement = PrioritizedLookupElement.withPriority(lookupElement, PRIORITY_LOWEST);
                }
                if ((pasField.getElementPtr() != null) && (parameters.getOriginalFile().getVirtualFile() != null)
                        && !parameters.getOriginalFile().getVirtualFile().equals(pasField.getElementPtr().getVirtualFile())) {
                    lookupElement = PrioritizedLookupElement.withPriority(lookupElement, PRIORITY_LOWER);
                }
                entities.put(el.getLookupString(), lookupElement);
            }
        }
    }

    private static boolean buildFromElement(@NotNull PasField field) {
        return (field.getElementPtr() != null) && (field.fieldType != PasField.FieldType.PSEUDO_VARIABLE);
    }

    private void addEntitiesToResult(CompletionResultSet result, Map<String, LookupElement> entities) {
        result.caseInsensitive().addAllElements(entities.values());
    }

}
