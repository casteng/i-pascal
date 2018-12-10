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
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.patterns.PlatformPatterns;
import com.intellij.psi.PsiComment;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.PsiWhiteSpace;
import com.intellij.psi.impl.source.tree.LeafPsiElement;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.tree.TokenSet;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.ProcessingContext;
import com.intellij.util.Processor;
import com.siberika.idea.pascal.PascalIcons;
import com.siberika.idea.pascal.PascalLanguage;
import com.siberika.idea.pascal.editor.ContextAwareVirtualFile;
import com.siberika.idea.pascal.editor.refactoring.PascalNameSuggestionProvider;
import com.siberika.idea.pascal.ide.actions.GotoSuper;
import com.siberika.idea.pascal.lang.context.Context;
import com.siberika.idea.pascal.lang.parser.NamespaceRec;
import com.siberika.idea.pascal.lang.psi.PasClassField;
import com.siberika.idea.pascal.lang.psi.PasClassProperty;
import com.siberika.idea.pascal.lang.psi.PasConstDeclaration;
import com.siberika.idea.pascal.lang.psi.PasEntityScope;
import com.siberika.idea.pascal.lang.psi.PasExportedRoutine;
import com.siberika.idea.pascal.lang.psi.PasFormalParameter;
import com.siberika.idea.pascal.lang.psi.PasFullyQualifiedIdent;
import com.siberika.idea.pascal.lang.psi.PasFunctionDirective;
import com.siberika.idea.pascal.lang.psi.PasLibraryModuleHead;
import com.siberika.idea.pascal.lang.psi.PasModule;
import com.siberika.idea.pascal.lang.psi.PasPackageModuleHead;
import com.siberika.idea.pascal.lang.psi.PasProgramModuleHead;
import com.siberika.idea.pascal.lang.psi.PasRecordDecl;
import com.siberika.idea.pascal.lang.psi.PasRoutineImplDecl;
import com.siberika.idea.pascal.lang.psi.PasStatement;
import com.siberika.idea.pascal.lang.psi.PasTypeDecl;
import com.siberika.idea.pascal.lang.psi.PasTypes;
import com.siberika.idea.pascal.lang.psi.PasUnitModuleHead;
import com.siberika.idea.pascal.lang.psi.PascalModule;
import com.siberika.idea.pascal.lang.psi.PascalNamedElement;
import com.siberika.idea.pascal.lang.psi.PascalRoutine;
import com.siberika.idea.pascal.lang.psi.PascalStructType;
import com.siberika.idea.pascal.lang.psi.impl.HasUniqueName;
import com.siberika.idea.pascal.lang.psi.impl.PasField;
import com.siberika.idea.pascal.lang.references.PasReferenceUtil;
import com.siberika.idea.pascal.lang.references.PascalChooseByNameContributor;
import com.siberika.idea.pascal.lang.references.ResolveContext;
import com.siberika.idea.pascal.lang.references.ResolveUtil;
import com.siberika.idea.pascal.lang.stub.PascalUnitSymbolIndex;
import com.siberika.idea.pascal.util.DocUtil;
import com.siberika.idea.pascal.util.PsiUtil;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import static com.siberika.idea.pascal.lang.context.CodePlace.ASSIGN_LEFT;
import static com.siberika.idea.pascal.lang.context.CodePlace.CONST_EXPRESSION;
import static com.siberika.idea.pascal.lang.context.CodePlace.DECL_CONST;
import static com.siberika.idea.pascal.lang.context.CodePlace.DECL_FIELD;
import static com.siberika.idea.pascal.lang.context.CodePlace.DECL_PROPERTY;
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

    private static final Map<IElementType, TokenSet> DO_THEN_OF_MAP = initDoThenOfMap();

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

                EntityCompletionContext completionContext = new EntityCompletionContext(ctx, parameters);
                CompletionUtil.fillBoost(completionContext);

                if (handleInherited(result, completionContext)) {
                    result.stopHere();
                    return;
                }

                if (isContextAwareVirtualFile(result, completionContext)) {
                    return;
                } else if (parameters.getOriginalPosition() instanceof PsiComment) {
                    PascalCompletionInComment.handleComments(result, parameters);
                    result.stopHere();
                    return;
                }

                handleSuggestions(result, completionContext);

                handleExpressionAndStatement(result, completionContext);

                if ((ctx.getPosition() instanceof PasFormalParameter) && (((PasFormalParameter) ctx.getPosition()).getParamType() == null)) {
                    CompletionUtil.appendText(result, "const ");
                    CompletionUtil.appendText(result, "var ");
                    CompletionUtil.appendText(result, "out ");
                }

                handleStruct(result, completionContext);

                if (ctx.getPrimary() == UNKNOWN) {
                    CompletionUtil.appendTokenSetIfAbsent(result, CompletionUtil.MODULE_HEADERS, parameters.getOriginalFile(),
                            PasProgramModuleHead.class, PasUnitModuleHead.class, PasLibraryModuleHead.class, PasPackageModuleHead.class);
                    result.caseInsensitive().addElement(CompletionUtil.getElement("begin "));
                } else if (ctx.getPrimary() == MODULE_HEADER) {
                    handleModuleSection(result, parameters);
                }
                if (ctx.getPrimary() == USES) {
                    CompletionUtil.handleUses(result, parameters.getPosition());
                }

                handleDeclarations(result, completionContext);

                result.restartCompletionWhenNothingMatches();
            }
        });
    }

    private static PasEntityScope getInheritedScope(PsiElement originalPos) {
        PasEntityScope nearestScope = PsiTreeUtil.getParentOfType(originalPos, PasEntityScope.class);

        if (nearestScope instanceof PasRoutineImplDecl) {
            PsiElement oPrev = PsiTreeUtil.skipSiblingsBackward(originalPos, PsiWhiteSpace.class, PsiComment.class);

            if (oPrev != null) {
                PsiElement deepestFirst = PsiTreeUtil.getDeepestFirst(oPrev);
                if (deepestFirst instanceof LeafPsiElement && ((LeafPsiElement) deepestFirst).getElementType() == PasTypes.INHERITED) {
                    return nearestScope.getContainingScope();
                }
            } else if (originalPos instanceof LeafPsiElement) {
                if (((LeafPsiElement) originalPos).getElementType() == PasTypes.NAME) {
                    PasStatement parent = PsiTreeUtil.getParentOfType(originalPos, PasStatement.class);
                    if (parent != null) {
                        PsiElement deepestFirst = PsiTreeUtil.getDeepestFirst(parent);
                        if (deepestFirst instanceof LeafPsiElement && ((LeafPsiElement) deepestFirst).getElementType() == PasTypes.INHERITED) {
                            return nearestScope.getContainingScope();
                        }
                    }
                }
            }
        }
        return null;
    }

    //TODO: move the logic to resolving code
    private boolean handleInherited(CompletionResultSet result, EntityCompletionContext completionContext) {
        PasEntityScope scope = getInheritedScope(completionContext.completionParameters.getOriginalPosition());
        if (null == scope) {
            return false;
        }

        Collection<PasEntityScope> parents = new LinkedHashSet<>();
        GotoSuper.retrieveParentStructs(parents, scope, 0);
        for (PasEntityScope parent : parents) {
            for (PasField field : parent.getAllFields()) {
                if (field.fieldType == PasField.FieldType.ROUTINE) {
                    //filter out strict private methods as well as private ones from other units
                    if (field.visibility != PasField.Visibility.STRICT_PRIVATE && (field.visibility != PasField.Visibility.PRIVATE || isFromSameUnit(field, completionContext.completionParameters.getOriginalFile()))) {
                        fieldToEntity(result, field, completionContext);
                    }
                }
            }
        }
        return true;
    }

    private boolean isFromSameUnit(PasField field, PsiFile file) {
        return PsiUtil.isSmartPointerValid(field.getElementPtr()) && (PsiManager.getInstance(file.getProject()).areElementsEquivalent(file, field.getElementPtr().getContainingFile()));
    }

    private static void handleSuggestions(CompletionResultSet result, EntityCompletionContext completionContext) {
        for (String name : PascalNameSuggestionProvider.suggestForElement(completionContext.context)) {
            result.caseInsensitive().addElement(CompletionUtil.getElement(name, null));
        }
    }

    private static void handleDeclarations(CompletionResultSet result, EntityCompletionContext completionContext) {
        Context ctx = completionContext.context;
        if (ctx.getPrimary() == TYPE_ID) {
            completionContext.likelyTypes = EnumSet.of(PasField.FieldType.TYPE);
            addEntities(result, completionContext, PasField.TYPES_TYPE_UNIT);
            if (ctx.contains(DECL_TYPE)) {
                CompletionUtil.appendTokenSet(result, CompletionUtil.TYPE_DECLARATIONS);
                result.caseInsensitive().addElement(CompletionUtil.getElement("interface "));
                result.caseInsensitive().addElement(CompletionUtil.getElement("class helper"));
                result.caseInsensitive().addElement(CompletionUtil.getElement("record helper"));
                result.caseInsensitive().addElement(CompletionUtil.getElement("class of"));
                result.caseInsensitive().addElement(CompletionUtil.getElement("type "));
                result.caseInsensitive().addElement(CompletionUtil.getElement("array["));
            } else if (ctx.contains(DECL_VAR)) {
                CompletionUtil.appendTokenSet(result, TokenSet.create(
                        PasTypes.RECORD, PasTypes.PACKED, PasTypes.SET, PasTypes.FILE, PasTypes.ARRAY
                ));
                result.caseInsensitive().addElement(CompletionUtil.getElement("array["));
            }
        } else if ((ctx.getPrimary() == DECL_TYPE) || (ctx.getPrimary() == DECL_VAR) || (ctx.getPrimary() == DECL_CONST) || (ctx.getPrimary() == GLOBAL_DECLARATION) || (ctx.getPrimary() == LOCAL_DECLARATION)) {
            boolean firstOnLine = DocUtil.isFirstOnLine(completionContext.completionParameters.getEditor(), completionContext.completionParameters.getPosition());
            if ((ctx.getPrimary() == DECL_CONST) && !firstOnLine) {
                handleConstant(result, completionContext);
            }
            if (ctx.getPrimary() == GLOBAL_DECLARATION || ctx.contains(GLOBAL)) {
                if (firstOnLine) {
                    CompletionUtil.appendTokenSet(result, CompletionUtil.DECLARATIONS_INTF);
                    CompletionUtil.appendTokenSetUnique(result, PasTypes.USES, PsiUtil.skipToExpressionParent(completionContext.completionParameters.getPosition()));
                    if (!ctx.contains(INTERFACE)) {
                        result.caseInsensitive().addElement(CompletionUtil.getElement("begin  "));
                        CompletionUtil.appendTokenSet(result, CompletionUtil.DECLARATIONS_IMPL);
                        PasModule mod = PsiUtil.getElementPasModule(ctx.getPosition());
                        if ((mod != null) && (mod.getModuleType() == PascalModule.ModuleType.UNIT)) {
                            CompletionUtil.appendTokenSetUnique(result, TokenSet.create(PasTypes.INITIALIZATION, PasTypes.FINALIZATION),
                                    PsiUtil.getModuleImplementationSection(completionContext.completionParameters.getOriginalFile()));
                        }
                    }
                }
            } else if (ctx.getPrimary() == LOCAL_DECLARATION || ctx.contains(LOCAL)) {
                if (firstOnLine) {
                    CompletionUtil.appendTokenSet(result, CompletionUtil.DECLARATIONS_LOCAL);
                } else if (ctx.getPosition() instanceof PascalRoutine) {
                    PascalRoutine routine = (PascalRoutine) ctx.getPosition();
                    if (routine != null) {
                        if (routine.getContainingScope() instanceof PascalStructType) {
                            if (routine instanceof PasExportedRoutine) {                   // Directives should appear in the class declaration only, not in the defining declaration
                                CompletionUtil.appendTokenSet(result, CompletionUtil.DIRECTIVE_METHOD);
                            }
                        } else {
                            CompletionUtil.appendTokenSet(result, CompletionUtil.DIRECTIVE_ROUTINE);
                        }
                    }
                }
                CompletionUtil.appendTokenSet(result, TokenSet.create(PasTypes.BEGIN));
            }
        }
    }

    private static void handleConstant(CompletionResultSet result, EntityCompletionContext completionContext) {
        PsiElement decl = completionContext.context.getPosition();
        if (decl instanceof PasConstDeclaration) {
            PasTypeDecl typeDecl = ((PasConstDeclaration) decl).getTypeDecl();
            PasFullyQualifiedIdent fqi = typeDecl != null ? PsiTreeUtil.findChildOfType(typeDecl, PasFullyQualifiedIdent.class) : null;
            if (null == fqi) {
                return;
            }
            PasEntityScope scope = PasReferenceUtil.resolveTypeScope(NamespaceRec.fromElement(fqi), null, true);
            if (scope instanceof PasRecordDecl) {
                LookupElement el = LookupElementBuilder.create(scope, "").withPresentableText("Complete record constant").withIcon(PascalIcons.RECORD).withInsertHandler(CompletionUtil.RECORD_INSERT_HANDLER);
                result.caseInsensitive().addElement(el);
            }
        }
    }

    private static void handleStruct(CompletionResultSet result, EntityCompletionContext completionContext) {
        Context ctx = completionContext.context;
        if (ctx.getPrimary() == PROPERTY_SPECIFIER) {
            completionContext.likelyTypes = EnumSet.of(PasField.FieldType.VARIABLE);
            addEntities(result, completionContext, PasField.TYPES_PROPERTY_SPECIFIER);
        } else if (ctx.getPrimary() == DECL_PROPERTY) {
            if (ctx.getPosition() instanceof PasClassProperty) {
                CompletionUtil.appendTokenSetUnique(result, CompletionUtil.PROPERTY_SPECIFIERS, ctx.getPosition());
            }
        } else if (ctx.getPrimary() == DECL_FIELD) {
            if (DocUtil.isFirstOnLine(completionContext.completionParameters.getEditor(), completionContext.completionParameters.getPosition())) {
                CompletionUtil.appendTokenSet(result, CompletionUtil.VISIBILITY);
                CompletionUtil.appendText(result, "strict private");
                CompletionUtil.appendText(result, "strict protected");
                CompletionUtil.appendTokenSet(result, CompletionUtil.STRUCT_DECLARATIONS);
                CompletionUtil.appendText(result, "class ");
                CompletionUtil.appendTokenSet(result, CompletionUtil.DECLARATIONS_LOCAL);
            } else {
                if (ctx.getPosition() instanceof PasClassField) {
                    PsiElement routine = PsiTreeUtil.skipSiblingsBackward(ctx.getPosition(), PsiWhiteSpace.class, PsiComment.class);
                    if (routine instanceof PasExportedRoutine) {
                        TokenSet directives = CompletionUtil.DIRECTIVE_METHOD;
                        for (PasFunctionDirective functionDirective : ((PasExportedRoutine) routine).getFunctionDirectiveList()) {
                            PsiElement dirElement = functionDirective.getFirstChild();
                            if (dirElement != null) {
                                directives = TokenSet.andNot(directives, TokenSet.create(dirElement.getNode().getElementType()));
                            }
                        }
                        CompletionUtil.appendTokenSet(result, directives);
                    }
                }
            }
        }
    }

    private static boolean isContextAwareVirtualFile(CompletionResultSet result, EntityCompletionContext completionContext) {
        Context ctx = completionContext.context;
        PsiFile file = ctx.getFile();
        if ((ctx.getPrimary() == UNKNOWN) && (file != null) && (file.getVirtualFile() instanceof ContextAwareVirtualFile) && (ctx.getDummyIdent() != null)) {
            NamespaceRec namespace = NamespaceRec.fromFQN(ctx.getDummyIdent(), ctx.getDummyIdent().getText().replace(PasField.DUMMY_IDENTIFIER, "")); // TODO: refactor
            namespace.setIgnoreVisibility(true);
            namespace.clearTarget();
            ResolveContext resolveContext = new ResolveContext(PsiUtil.getNearestAffectingScope(((ContextAwareVirtualFile) file.getVirtualFile()).getContextElement()),
                    PasField.TYPES_ALL, false, null, null);
            fieldsToEntities(result, PasReferenceUtil.resolve(namespace, resolveContext, 0), completionContext);
            result.stopHere();
            return true;
        } else {
            return false;
        }
    }

    private static void handleExpressionAndStatement(CompletionResultSet result, EntityCompletionContext completionContext) {
        Context ctx = completionContext.context;
        if (ctx.getPrimary() == EXPR) {
            if (ctx.contains(FIRST_IN_NAME) && ctx.contains(FIRST_IN_EXPR) && !ctx.withinBraces() && ctx.contains(STATEMENT)) {
                CompletionUtil.appendTokenSet(result, CompletionUtil.STATEMENTS);
                if (ctx.contains(STMT_TRY)) {
                    CompletionUtil.appendTokenSetUnique(result, PasTypes.EXCEPT, ctx.getPosition().getParent());
                    CompletionUtil.appendTokenSetUnique(result, PasTypes.FINALLY, ctx.getPosition().getParent());
                    CompletionUtil.appendTokenSet(result, TokenSet.create(PasTypes.ON));
                }
                if (ctx.contains(STMT_REPEAT)) {
                    CompletionUtil.appendTokenSetUnique(result, PasTypes.UNTIL, ctx.getPosition().getParent());
                }
                if (DO_THEN_OF_MAP.containsKey(ctx.getPosition().getParent().getNode().getElementType())) {
                    CompletionUtil.appendTokenSetUnique(result, CompletionUtil.TS_BEGIN, ctx.getPosition());
                }
                if (ctx.contains(STMT_FOR) || ctx.contains(STMT_WHILE) || ctx.contains(STMT_REPEAT)) {
                    CompletionUtil.appendTokenSet(result, CompletionUtil.STATEMENTS_IN_CYCLE);
                }
            }
            if (ctx.contains(CONST_EXPRESSION)) {
                completionContext.likelyTypes = EnumSet.of(PasField.FieldType.CONSTANT);
                completionContext.deniedTypes = EnumSet.of(PasField.FieldType.TYPE);
                addEntities(result, completionContext, PasField.TYPES_STATIC);
            } else if (ctx.contains(ASSIGN_LEFT)) {
                addEntities(result, completionContext, PasField.TYPES_LEFT_SIDE);
            } else {
                addEntities(result, completionContext, PasField.TYPES_ALL);
                if (ctx.contains(FIRST_IN_NAME)) {
                    CompletionUtil.appendTokenSet(result, CompletionUtil.VALUES);
                }
            }
        } else if ((ctx.getPrimary() == STATEMENT) || (ctx.getPrimary() == STMT_EXCEPT)) {
            if (!getDoThenOf(result, ctx, completionContext.completionParameters.getOffset()) && (ctx.contains(STMT_IF_THEN))) {
                CompletionUtil.appendTokenSet(result, CompletionUtil.TS_ELSE);
            }
        } else if (ctx.getPrimary() == STMT_CASE_ITEM) {
            CompletionUtil.appendTokenSetUnique(result, PasTypes.ELSE, ctx.getPosition().getParent());
            completionContext.likelyTypes = EnumSet.of(PasField.FieldType.CONSTANT);
            completionContext.deniedTypes = EnumSet.of(PasField.FieldType.TYPE);
            addEntities(result, completionContext, PasField.TYPES_STATIC);
        }
    }

    private static boolean getDoThenOf(CompletionResultSet result, Context ctx, int offset) {
        PsiElement controlStmt = ctx.getPosition();
        TokenSet ts = controlStmt != null ? DO_THEN_OF_MAP.get(controlStmt.getNode().getElementType()) : null;
        if (ts != null) {
            ASTNode doThenOf = CompletionUtil.getDoThenOf(ctx.getPosition());
            if (doThenOf != null) {
                if (doThenOf.getStartOffset() < offset) {
                    CompletionUtil.appendTokenSet(result, CompletionUtil.TS_BEGIN);
                }
            } else {
                CompletionUtil.appendTokenSet(result, ts);
                return true;
            }
        }
        return false;
    }

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

    private static void handleModuleSection(CompletionResultSet result, CompletionParameters parameters) {
        PsiElement pos = PsiUtil.skipToExpressionParent(parameters.getPosition());
        if (pos instanceof PascalModule) {
            PascalModule module = (PascalModule) pos;
            switch (module.getModuleType()) {
                case UNIT: {
                    if (pos.getTextRange().getStartOffset() < parameters.getOffset()) {
                        CompletionUtil.appendTokenSetUnique(result, CompletionUtil.UNIT_SECTIONS, pos);
                    }
                    break;
                }
                case PACKAGE: {
                    CompletionUtil.appendTokenSetUnique(result, CompletionUtil.TOP_LEVEL_DECLARATIONS, parameters.getOriginalFile());
                }
                case LIBRARY:
                    result.caseInsensitive().addElement(CompletionUtil.getElement(PasTypes.EXPORTS.toString()));
                    result.caseInsensitive().addElement(CompletionUtil.getElement("begin  "));
                case PROGRAM:
                    CompletionUtil.appendTokenSetUnique(result, TokenSet.create(PasTypes.USES), pos);
                    CompletionUtil.appendTokenSet(result, CompletionUtil.DECLARATIONS_INTF);
                    CompletionUtil.appendTokenSet(result, CompletionUtil.DECLARATIONS_IMPL);
                    result.caseInsensitive().addElement(CompletionUtil.getElement("begin  "));
            }
        }
    }

    private static void addEntities(CompletionResultSet result, EntityCompletionContext completionContext, Set<PasField.FieldType> fieldTypes) {
        NamespaceRec namespace = NamespaceRec.fromFQN(completionContext.context.getDummyIdent(), PasField.DUMMY_IDENTIFIER);
        if (completionContext.context.getNamedElement() != null) {
            if (completionContext.context.getNamedElement().getParent() instanceof PascalNamedElement) {
                namespace = NamespaceRec.fromElement(completionContext.context.getNamedElement());
            } else {
                namespace = NamespaceRec.fromFQN(completionContext.context.getDummyIdent(), completionContext.context.getNamedElement().getName());
            }
        }
        String pattern = namespace.getCurrentName();
        namespace.clearTarget();
        Collection<PasField> fields = PasReferenceUtil.resolveExpr(namespace, new ResolveContext(fieldTypes, true), 0);

        addFromUnrelatedUnits(result, completionContext, fieldTypes, pattern);

        fieldsToEntities(result, fields, completionContext);
    }

    private static void addFromUnrelatedUnits(CompletionResultSet result, EntityCompletionContext completionContext, Set<PasField.FieldType> fieldTypes, String pattern) {
        if (StringUtil.isNotEmpty(pattern) && completionContext.isUnrelatedUnitsEnabled()) {
            PascalChooseByNameContributor.processByName(PascalUnitSymbolIndex.KEY, pattern, completionContext.completionParameters.getOriginalFile().getProject(), true, new Processor<PascalNamedElement>() {
                @Override
                public boolean process(PascalNamedElement namedElement) {
                    if (!fieldTypes.contains(namedElement.getType()) || !(namedElement instanceof HasUniqueName) || namedElement.isLocal()) {
                        return true;
                    }
                    String name = ((HasUniqueName) namedElement).getUniqueName();
                    String unitName = ResolveUtil.calcContainingUnitName(namedElement);
                    if ((null == name) || (null == unitName) || !name.startsWith(unitName)) {
                        return true;
                    }
                    LookupElement lookupElement;
                    LookupElementBuilder el = CompletionUtil.createLookupElement(completionContext.completionParameters, namedElement, unitName);
                    if (el != null) {
                        lookupElement = el.appendTailText(" : " + namedElement.getType().toString().toLowerCase(), true).
                                withCaseSensitivity(true).withTypeText("+ " + unitName, false);
                        int priority = completionContext.calcPriority(lookupElement.getLookupString(), namedElement.getName(), namedElement.getType(), true);
                        lookupElement = priority != 0 ? PrioritizedLookupElement.withPriority(lookupElement, priority) : lookupElement;
                        result.caseInsensitive().addElement(lookupElement);
                    }
                    return true;
                }
            });
        }
    }

    private static void fieldsToEntities(CompletionResultSet result, Collection<PasField> fields, EntityCompletionContext completionContext) {
        for (PasField pasField : fields) {
            fieldToEntity(result, pasField, completionContext);
        }
    }

    private static void fieldToEntity(CompletionResultSet result, PasField field, EntityCompletionContext completionContext) {
        if ((field.name != null) && !field.name.contains(ResolveUtil.STRUCT_SUFFIX)) {
            LookupElement lookupElement;
            LookupElementBuilder el = CompletionUtil.buildFromElement(field) ? CompletionUtil.createLookupElement(completionContext.completionParameters.getEditor(), field) : LookupElementBuilder.create(field.name);
            if (null == el) {
                return;
            }
            lookupElement = el.appendTailText(" : " + field.fieldType.toString().toLowerCase(), true).
                    withCaseSensitivity(true).withTypeText(field.owner != null ? field.owner.getName() : "-", false);
            int priority = completionContext.calcPriority(lookupElement.getLookupString(), field.name, field.fieldType, completionContext.isFromOtherFile(field));
            lookupElement = priority != 0 ? PrioritizedLookupElement.withPriority(lookupElement, priority) : lookupElement;
            result.caseInsensitive().addElement(lookupElement);
        }
    }

}
