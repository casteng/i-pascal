package com.siberika.idea.pascal.editor.completion;

import com.intellij.codeInsight.completion.CompletionContributor;
import com.intellij.codeInsight.completion.CompletionInitializationContext;
import com.intellij.codeInsight.completion.CompletionParameters;
import com.intellij.codeInsight.completion.CompletionProvider;
import com.intellij.codeInsight.completion.CompletionResultSet;
import com.intellij.codeInsight.completion.CompletionType;
import com.intellij.codeInsight.completion.InsertHandler;
import com.intellij.codeInsight.completion.InsertionContext;
import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.util.io.FileUtilRt;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.patterns.PlatformPatterns;
import com.intellij.psi.PsiComment;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiErrorElement;
import com.intellij.psi.PsiWhiteSpace;
import com.intellij.psi.codeStyle.CodeStyleManager;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.tree.TokenSet;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.ProcessingContext;
import com.siberika.idea.pascal.PascalIcons;
import com.siberika.idea.pascal.lang.lexer.PascalLexer;
import com.siberika.idea.pascal.lang.parser.NamespaceRec;
import com.siberika.idea.pascal.lang.psi.PasAssignPart;
import com.siberika.idea.pascal.lang.psi.PasClassParent;
import com.siberika.idea.pascal.lang.psi.PasCompoundStatement;
import com.siberika.idea.pascal.lang.psi.PasConstDeclaration;
import com.siberika.idea.pascal.lang.psi.PasContainsClause;
import com.siberika.idea.pascal.lang.psi.PasEntityScope;
import com.siberika.idea.pascal.lang.psi.PasExpression;
import com.siberika.idea.pascal.lang.psi.PasFullyQualifiedIdent;
import com.siberika.idea.pascal.lang.psi.PasGenericTypeIdent;
import com.siberika.idea.pascal.lang.psi.PasHintingDirective;
import com.siberika.idea.pascal.lang.psi.PasLibraryModuleHead;
import com.siberika.idea.pascal.lang.psi.PasModule;
import com.siberika.idea.pascal.lang.psi.PasNamedIdent;
import com.siberika.idea.pascal.lang.psi.PasNamespaceIdent;
import com.siberika.idea.pascal.lang.psi.PasPackageModuleHead;
import com.siberika.idea.pascal.lang.psi.PasProcForwardDecl;
import com.siberika.idea.pascal.lang.psi.PasProgramModuleHead;
import com.siberika.idea.pascal.lang.psi.PasRefNamedIdent;
import com.siberika.idea.pascal.lang.psi.PasRequiresClause;
import com.siberika.idea.pascal.lang.psi.PasRoutineImplDecl;
import com.siberika.idea.pascal.lang.psi.PasStatement;
import com.siberika.idea.pascal.lang.psi.PasSubIdent;
import com.siberika.idea.pascal.lang.psi.PasTypeDeclaration;
import com.siberika.idea.pascal.lang.psi.PasTypeID;
import com.siberika.idea.pascal.lang.psi.PasTypeSection;
import com.siberika.idea.pascal.lang.psi.PasTypes;
import com.siberika.idea.pascal.lang.psi.PasUnitFinalization;
import com.siberika.idea.pascal.lang.psi.PasUnitImplementation;
import com.siberika.idea.pascal.lang.psi.PasUnitInitialization;
import com.siberika.idea.pascal.lang.psi.PasUnitInterface;
import com.siberika.idea.pascal.lang.psi.PasUnitModuleHead;
import com.siberika.idea.pascal.lang.psi.PasUsesClause;
import com.siberika.idea.pascal.lang.psi.PasVarDeclaration;
import com.siberika.idea.pascal.lang.psi.PascalNamedElement;
import com.siberika.idea.pascal.lang.psi.PascalPsiElement;
import com.siberika.idea.pascal.lang.psi.PascalStructType;
import com.siberika.idea.pascal.lang.psi.impl.PasField;
import com.siberika.idea.pascal.lang.psi.impl.PasRoutineImplDeclImpl;
import com.siberika.idea.pascal.lang.psi.impl.PascalExpression;
import com.siberika.idea.pascal.lang.psi.impl.PascalModuleImpl;
import com.siberika.idea.pascal.lang.psi.impl.PascalRoutineImpl;
import com.siberika.idea.pascal.lang.references.PasReferenceUtil;
import com.siberika.idea.pascal.util.PsiUtil;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Author: George Bakhtadze
 * Date: 20/09/2013
 */
public class PascalCompletionContributor extends CompletionContributor {

    private static final Map<String, String> INSERT_MAP = getInsertMap();

    private static final String PLACEHOLDER_FILENAME = "__FILENAME__";
    private static final String PLACEHOLDER_CARET = "__CARET__";
    private static final TokenSet DECLARATIONS_LOCAL = TokenSet.create(
            PasTypes.VAR, PasTypes.CONST, PasTypes.TYPE, PasTypes.PROCEDURE, PasTypes.FUNCTION
    );
    public static final TokenSet TYPE_DECLARATIONS = TokenSet.create(
            PasTypes.TYPE, PasTypes.CLASS, PasTypes.DISPINTERFACE, PasTypes.RECORD, PasTypes.OBJECT,
            PasTypes.PACKED, PasTypes.SET, PasTypes.FILE, PasTypes.HELPER, PasTypes.ARRAY
    );

    private static Map<String, String> getInsertMap() {
        Map<String, String> res = new HashMap<String, String>();
        res.put(PasTypes.UNIT.toString(), String.format(" %s;\n\ninterface\n\n  %s\nimplementation\n\nend.\n", PLACEHOLDER_FILENAME, PLACEHOLDER_CARET));
        res.put(PasTypes.PROGRAM.toString(), String.format(" %s;\nbegin\n  %s\nend.\n", PLACEHOLDER_FILENAME, PLACEHOLDER_CARET));
        res.put(PasTypes.LIBRARY.toString(), String.format(" %s;\n\nexports %s\n\nbegin\n\nend.\n", PLACEHOLDER_FILENAME, PLACEHOLDER_CARET));
        res.put(PasTypes.PACKAGE.toString(), String.format(" %s;\n\nrequires\n\n contains %s\n\nend.\n", PLACEHOLDER_FILENAME, PLACEHOLDER_CARET));
        res.put(PasTypes.BEGIN.toString(), String.format("\n  %s\nend;\n", PLACEHOLDER_CARET));
        res.put(PasTypes.END.toString(), ";");
        res.put(PasTypes.INTERFACE.toString(), String.format("\n  %s\nimplementation\n", PLACEHOLDER_CARET));
        res.put(PasTypes.INITIALIZATION.toString(), String.format("\n  %s\nfinalization\n", PLACEHOLDER_CARET));
        res.put(PasTypes.USES.toString(), String.format(" %s;", PLACEHOLDER_CARET));

        res.put(PasTypes.FOR.toString(), String.format(" %s do ;", PLACEHOLDER_CARET));
        res.put(PasTypes.WHILE.toString(), String.format(" %s do ;", PLACEHOLDER_CARET));
        res.put(PasTypes.REPEAT.toString(), String.format("\nuntil %s;", PLACEHOLDER_CARET));

        res.put(PasTypes.IF.toString(), String.format(" %s then ;\n", PLACEHOLDER_CARET));
        res.put(PasTypes.CASE.toString(), String.format(" %s of\nend;", PLACEHOLDER_CARET));

        res.put(PasTypes.WITH.toString(), String.format(" %s do ;", PLACEHOLDER_CARET));

        res.put(PasTypes.TRY.toString(), String.format("\n  %s\nfinally\nend;", PLACEHOLDER_CARET));

        res.put(PasTypes.RECORD.toString(), String.format("  %s\nend;", PLACEHOLDER_CARET));
        res.put(PasTypes.OBJECT.toString(), String.format("  %s\nend;", PLACEHOLDER_CARET));
        res.put(PasTypes.CLASS.toString(), String.format("(TObject)\nprivate\n%s\nprotected\npublic\npublished\nend;", PLACEHOLDER_CARET));
        res.put(PasTypes.INTERFACE.toString() + " ", String.format("(IUnknown)\n%s\nend;", PLACEHOLDER_CARET));
        res.put(PasTypes.ARRAY.toString(), String.format("[0..%s] of ;", PLACEHOLDER_CARET));
        res.put(PasTypes.SET.toString(), String.format(" of %s;", PLACEHOLDER_CARET));

        res.put(PasTypes.CONSTRUCTOR.toString(), String.format(" Create(%s);", PLACEHOLDER_CARET));
        res.put(PasTypes.DESTRUCTOR.toString(), String.format(" Destroy(%s);", PLACEHOLDER_CARET));
        res.put(PasTypes.FUNCTION.toString(), String.format(" %s(): ;", PLACEHOLDER_CARET));
        res.put(PasTypes.PROCEDURE.toString(), String.format(" %s();", PLACEHOLDER_CARET));
        return res;
    }

    @SuppressWarnings("unchecked")
    public PascalCompletionContributor() {
        extend(CompletionType.BASIC, PlatformPatterns.psiElement(), new CompletionProvider<CompletionParameters>() {
            @Override
            protected void addCompletions(@NotNull CompletionParameters parameters, ProcessingContext context, @NotNull CompletionResultSet result) {
                PsiElement originalPos = parameters.getOriginalPosition();
                PsiElement pos = parameters.getPosition();
                PsiElement prev = PsiTreeUtil.skipSiblingsBackward(pos, PsiWhiteSpace.class, PsiComment.class);
                PsiElement oPrev = PsiTreeUtil.skipSiblingsBackward(originalPos, PsiWhiteSpace.class, PsiComment.class);
                System.out.println(String.format("=== oPos: %s, pos: %s, oPrev: %s, prev: %s, opar: %s, par: %s", originalPos, pos, oPrev, prev, originalPos != null ? originalPos.getParent() : null, pos.getParent()));

                originalPos = skipToExpressionParent(parameters.getOriginalPosition());
                pos = skipToExpressionParent(parameters.getPosition());
                prev = PsiTreeUtil.skipSiblingsBackward(pos, PsiWhiteSpace.class, PsiComment.class);
                oPrev = PsiTreeUtil.skipSiblingsBackward(originalPos, PsiWhiteSpace.class, PsiComment.class);
                int level = PsiUtil.getElementLevel(originalPos);
                System.out.println(String.format("=== skipped. oPos: %s, pos: %s, oPrev: %s, prev: %s, opar: %s, par: %s, lvl: %d", originalPos, pos, oPrev, prev,
                        originalPos != null ? originalPos.getParent() : null, pos.getParent(), level));

                if (!(prev instanceof PasTypeSection) && !(posIs(originalPos, PasTypeSection.class, PasRoutineImplDecl.class))) {
                    handleModuleHeader(result, parameters, pos);
                    handleModuleSection(result, parameters, pos);
                    handleUnitSection(result, parameters, pos, originalPos);
                    handleUses(result, parameters, pos, originalPos);
                }
                handleDeclarations(result, parameters, pos, originalPos);
                handleStructured(result, parameters, pos, originalPos);

                Collection<PasField> entities = new HashSet<PasField>();
                handleEntities(result, parameters, pos, originalPos, entities);

                handleStatement(result, parameters, pos, originalPos, entities);

                //handleDirectives(result, parameters, originalPos, pos);
                Set<String> nameSet = new HashSet<String>();                                  // TODO: replace with proper implementation of LookupElement
                Collection<LookupElement> lookupElements = new HashSet<LookupElement>();
                for (PasField field : entities) {
                    String name = getFieldName(field).toUpperCase();
                    if (!nameSet.contains(name)) {
                        lookupElements.add(getLookupElement(field));
                        nameSet.add(name);
                    }
                }
                result.caseInsensitive().addAllElements(lookupElements);
                result.restartCompletionWhenNothingMatches();
            }
        });

    }

    private void handleStatement(CompletionResultSet result, CompletionParameters parameters, PsiElement pos, PsiElement originalPos, Collection<PasField> entities) {
        if ((pos instanceof PasAssignPart)) {                                 // identifier completion in right part of assignment
            addEntities(entities, parameters.getPosition(), PasField.TYPES_ALL, parameters.isExtendedCompletion());
            PsiElement prev = PsiTreeUtil.skipSiblingsBackward(parameters.getOriginalPosition(), PsiWhiteSpace.class, PsiComment.class);
            if ((null == prev) || (!prev.getText().equals("."))) {
                appendTokenSet(result, PascalLexer.VALUES);
            }
        } else if (originalPos instanceof PasStatement) {
            pos = originalPos;
        }
        if (pos instanceof PasStatement) {                                                                          // identifier completion in left part of assignment
            addEntities(entities, parameters.getPosition(), PasField.TYPES_LEFT_SIDE, parameters.isExtendedCompletion());        // complete identifier variants
            /*if (!PsiUtil.isIdent(parameters.getOriginalPosition().getParent()) && (pos instanceof PasCompoundStatement)) {
                appendTokenSet(result, PascalLexer.STATEMENTS);                                                     // statements variants
            }
            if (PsiTreeUtil.getParentOfType(parameters.getOriginalPosition(), PasForStatement.class, PasWhileStatement.class, PasRepeatStatement.class) != null) {
                appendTokenSet(result, PascalLexer.STATEMENTS_IN_CYCLE);
            }*/
        }

    }

    private void handleEntities(CompletionResultSet result, CompletionParameters parameters, PsiElement pos, PsiElement originalPos, Collection<PasField> entities) {
        if (pos instanceof PasTypeID) {                                                                          // Type declaration
            addEntities(entities, parameters.getPosition(), PasField.TYPES_TYPE_UNIT, parameters.isExtendedCompletion());
            if (!posIs(pos.getParent(), PasClassParent.class)) {
                appendTokenSet(result, TYPE_DECLARATIONS);
                result.addElement(getElement("interface "));
            }
        }
    }

    private void handleStructured(CompletionResultSet result, CompletionParameters parameters, PsiElement pos, PsiElement originalPos) {
        if (originalPos instanceof PascalStructType) {
            appendTokenSet(result, PascalLexer.VISIBILITY);
            appendTokenSet(result, PascalLexer.STRUCT_DECLARATIONS);
        }
    }

    private void handleDeclarations(CompletionResultSet result, CompletionParameters parameters, PsiElement pos, PsiElement originalPos) {
        if (posIs(PsiTreeUtil.skipSiblingsBackward(parameters.getOriginalPosition(), PsiWhiteSpace.class, PsiComment.class), PasGenericTypeIdent.class, PasNamedIdent.class)) {
            return;                                                                                                       // Inside type declaration
        }
        if (posIs(pos, PasUnitInterface.class, PasUnitImplementation.class, PasTypeDeclaration.class, PasConstDeclaration.class, PasVarDeclaration.class, PasRoutineImplDecl.class)
          || (posIs(originalPos, PasUnitInterface.class, PasUnitImplementation.class, PasModule.class) && (pos instanceof PasUsesClause))
          || ((originalPos instanceof PasUsesClause) && (originalPos.getParent() instanceof PasModule))) {
            PasEntityScope scope = pos instanceof PasEntityScope ? (PasEntityScope) pos : PsiUtil.getNearestAffectingScope(pos);
            if (scope instanceof PasModule) {
                appendTokenSet(result, PascalLexer.DECLARATIONS);
            } else if (scope instanceof PascalRoutineImpl) {
                appendTokenSet(result, DECLARATIONS_LOCAL);
            }
            appendTokenSetUnique(result, PasTypes.BEGIN, scope);
        }
    }

    private void handleUnitSection(CompletionResultSet result, CompletionParameters parameters, PsiElement pos, PsiElement originalPos) {
        if (((pos instanceof PasUnitInterface) && posIs(originalPos, PasUnitInterface.class, PasModule.class))
          || ((pos instanceof PasUnitImplementation) && (originalPos instanceof PasUnitImplementation))) {
            appendTokenSetUnique(result, PascalLexer.USES, pos);
            appendTokenSet(result, PascalLexer.DECLARATIONS);
        }
    }

    private void handleModuleSection(CompletionResultSet result, CompletionParameters parameters, PsiElement pos) {
        if ((pos instanceof PascalModuleImpl)) {
            PascalModuleImpl module = (PascalModuleImpl) pos;
            switch (module.getModuleType()) {
                case UNIT: {
                    appendTokenSetUnique(result, PascalLexer.UNIT_SECTIONS, pos);
                    break;
                }
                case PACKAGE: {
                    appendTokenSetUnique(result, PascalLexer.TOP_LEVEL_DECLARATIONS, parameters.getOriginalFile());
                }
                case LIBRARY:
                    result.caseInsensitive().addElement(getElement(PasTypes.EXPORTS.toString()));
                    appendTokenSetUnique(result, PasTypes.BEGIN, pos);
                case PROGRAM:
                    appendTokenSetUnique(result, TokenSet.create(PascalLexer.USES), parameters.getOriginalFile());
                    appendTokenSet(result, PascalLexer.DECLARATIONS);
                    appendTokenSetUnique(result, PasTypes.BEGIN, pos);
            }
        }
    }

    private void handleModuleHeader(CompletionResultSet result, CompletionParameters parameters, PsiElement pos) {
        if (PsiTreeUtil.findChildOfType(parameters.getOriginalFile(), PasModule.class) == null) {                     // no module found
            appendTokenSetIfAbsent(result, PascalLexer.MODULE_HEADERS, parameters.getOriginalFile(),
                    PasProgramModuleHead.class, PasUnitModuleHead.class, PasLibraryModuleHead.class, PasPackageModuleHead.class);
        }
        //PsiTreeUtil.findChildOfType(parameters.getOriginalFile(), PasUnitModuleHead.class) != null)) {
    }

    private static String getFieldName(PasField field) {
        if ((field.fieldType == PasField.FieldType.ROUTINE) && (field.element != null)) {
            return PsiUtil.getFieldName(field.element);
        } else {
            return field.name;
        }
    }

    private static void handleUses(CompletionResultSet result, CompletionParameters parameters, @NotNull PsiElement pos, PsiElement originalPos) {
        if ((pos instanceof PasUsesClause) && (originalPos instanceof PasUsesClause)) {
            if ((parameters.getPosition().getParent() instanceof PsiErrorElement) || (parameters.getOriginalPosition().getParent() instanceof PsiErrorElement)) {
                return;
            }
            PasModule module = PsiUtil.getElementPasModule(pos);
            Set<String> excludedUnits = new HashSet<String>();
            if (module instanceof PascalModuleImpl) {
                excludedUnits.add(module.getName().toUpperCase());
                for (PasEntityScope scope : ((PascalModuleImpl) module).getPublicUnits()) {
                    excludedUnits.add(scope.getName().toUpperCase());
                }
                for (PasEntityScope scope : ((PascalModuleImpl) module).getPrivateUnits()) {
                    excludedUnits.add(scope.getName().toUpperCase());
                }
            }
            for (VirtualFile file : PasReferenceUtil.findUnitFiles(pos.getProject(), com.intellij.openapi.module.ModuleUtil.findModuleForPsiElement(pos))) {
                if (!excludedUnits.contains(file.getNameWithoutExtension().toUpperCase())) {
                    LookupElementBuilder lookupElement = LookupElementBuilder.create(file.getNameWithoutExtension());
                    result.caseInsensitive().addElement(lookupElement.withTypeText(file.getExtension() != null ? file.getExtension() : "", false));
                }
            }
        }
    }

    private LookupElement getLookupElement(@NotNull PasField field) {
        String scope = field.owner != null ? field.owner.getName() : "-";
        LookupElementBuilder lookupElement = buildFromElement(field) ? createLookupElement(field) : LookupElementBuilder.create(field.name);
        return lookupElement.appendTailText(" : " + field.fieldType.toString().toLowerCase(), true).
                withCaseSensitivity(false).withTypeText(scope, false);
    }

    private static boolean buildFromElement(@NotNull PasField field) {
        return (field.element != null) && (StringUtils.isEmpty(field.name) || (field.fieldType == PasField.FieldType.ROUTINE));
    }

    private LookupElementBuilder createLookupElement(@NotNull PasField field) {
        assert field.element != null;
        LookupElementBuilder res = LookupElementBuilder.create(field.element).withPresentableText(PsiUtil.getFieldName(field.element));
        if (field.fieldType == PasField.FieldType.ROUTINE) {
            res = res.withInsertHandler(new InsertHandler<LookupElement>() {
                @Override
                public void handleInsert(InsertionContext context, LookupElement item) {
                    adjustDocument(context, "()", 1);
                }
            });
        }
        return res;
    }

    private boolean isQualifiedIdent(PsiElement parent) {
        if (PsiUtil.isIdent(parent)) {
            PsiElement par = parent.getParent();
            if (par instanceof PasFullyQualifiedIdent) {
                return !StringUtils.isEmpty(((PasFullyQualifiedIdent) par).getNamespace());
            }
        }
        return false;
    }

    /*
    * _: ____
    * abc_: ____
    * a.bc_ a.____
    * a_.bc ____
    * a.b_.c a.____
    */
    private static void addEntities(Collection<PasField> result, PsiElement position, Set<PasField.FieldType> fieldTypes, boolean extendedCompletion) {
        NamespaceRec namespace = NamespaceRec.fromFQN(position, PasField.DUMMY_IDENTIFIER);
        if (PsiUtil.isIdent(position.getParent())) {
            if (position.getParent().getParent() instanceof PascalNamedElement) {
                namespace = NamespaceRec.fromElement(position.getParent());
            } else {
                namespace = NamespaceRec.fromFQN(position, ((PascalNamedElement) position).getName());
            }
        }
        namespace.clearTarget();
        result.addAll(PasReferenceUtil.resolveExpr(namespace, fieldTypes, extendedCompletion, 0));
    }

    private static void handleDirectives(CompletionResultSet result, CompletionParameters parameters, PsiElement originalPos, PsiElement pos) {
        if (PsiTreeUtil.instanceOf(originalPos, PasEntityScope.class, PascalRoutineImpl.class, PasProcForwardDecl.class)) {
            pos = originalPos;
        }
        if (pos instanceof PasEntityScope) {
            PsiElement el = PsiTreeUtil.skipSiblingsBackward(parameters.getOriginalPosition(), PsiWhiteSpace.class);
            if (!(pos instanceof PascalStructType) || (el instanceof PascalRoutineImpl) || (el instanceof PasHintingDirective)) {
                appendTokenSet(result, PascalLexer.DIRECTIVE_METHOD);
                if (pos.getClass() == PasRoutineImplDeclImpl.class) {
                    result.addElement(getElement("begin"));
                }
            } else {
                appendTokenSet(result, PascalLexer.VISIBILITY);
                appendTokenSet(result, PascalLexer.STRUCT_DECLARATIONS);
            }
        }
    }

    private static PsiElement skipToExpressionParent(PsiElement element) {
        return PsiTreeUtil.skipParentsOfType(element,
                PasSubIdent.class, PasFullyQualifiedIdent.class, PasRefNamedIdent.class, PasNamedIdent.class, PasNamespaceIdent.class, PasGenericTypeIdent.class,
                PasExpression.class, PsiWhiteSpace.class, PsiErrorElement.class,
                PascalExpression.class,
                PasUnitModuleHead.class);
    }

    private static boolean posIs(PsiElement element, Class...classes) {
        for (Class clazz : classes) {
            if (clazz.isInstance(element)) {
                return true;
            }
        }
        return false;
    }

    private static void appendTokenSet(CompletionResultSet result, TokenSet tokenSet) {
        for (IElementType op : tokenSet.getTypes()) {
            result.caseInsensitive().addElement(getElement(op.toString()));
        }
    }

    private static LookupElement getElement(String s) {
        return LookupElementBuilder.create(s).withIcon(PascalIcons.GENERAL).withStrikeoutness(s.equals(PasTypes.GOTO.toString())).withInsertHandler(INSERT_HANDLER);
    }

    private static Map<IElementType, Class<? extends PascalPsiElement>> TOKEN_TO_PSI = new HashMap<IElementType, Class<? extends PascalPsiElement>>();
    static {
        TOKEN_TO_PSI.put(PascalLexer.PROGRAM, PasProgramModuleHead.class);
        TOKEN_TO_PSI.put(PascalLexer.UNIT, PasUnitModuleHead.class);
        TOKEN_TO_PSI.put(PascalLexer.LIBRARY, PasLibraryModuleHead.class);
        TOKEN_TO_PSI.put(PascalLexer.PACKAGE, PasPackageModuleHead.class);
        TOKEN_TO_PSI.put(PascalLexer.CONTAINS, PasContainsClause.class);
        TOKEN_TO_PSI.put(PascalLexer.REQUIRES, PasRequiresClause.class);

        TOKEN_TO_PSI.put(PascalLexer.INTERFACE, PasUnitInterface.class);
        TOKEN_TO_PSI.put(PascalLexer.IMPLEMENTATION, PasUnitImplementation.class);
        TOKEN_TO_PSI.put(PascalLexer.INITIALIZATION, PasUnitInitialization.class);
        TOKEN_TO_PSI.put(PascalLexer.FINALIZATION, PasUnitFinalization.class);

        TOKEN_TO_PSI.put(PascalLexer.USES, PasUsesClause.class);

        TOKEN_TO_PSI.put(PascalLexer.BEGIN, PasCompoundStatement.class);
    }

    private void appendTokenSetUnique(CompletionResultSet result, TokenSet tokenSet, PsiElement position) {
        for (IElementType op : tokenSet.getTypes()) {
            appendTokenSetUnique(result, op, position);
        }
    }

    private void appendTokenSetUnique(CompletionResultSet result, IElementType op, PsiElement position) {
        if ((TOKEN_TO_PSI.get(op) == null) || (PsiTreeUtil.findChildOfType(position, TOKEN_TO_PSI.get(op), true) == null)) {
            LookupElementBuilder el = LookupElementBuilder.create(op.toString()).withIcon(PascalIcons.GENERAL).withStrikeoutness(op.equals(PasTypes.GOTO)).withInsertHandler(INSERT_HANDLER);
            result.caseInsensitive().addElement(el);
        }
    }

    private void appendTokenSetIfAbsent(CompletionResultSet result, TokenSet tokenSet, PsiElement position, Class...classes) {
        if (PsiTreeUtil.findChildOfAnyType(position, classes) == null) {
            for (IElementType op : tokenSet.getTypes()) {
                LookupElementBuilder el = LookupElementBuilder.create(op.toString()).withIcon(PascalIcons.GENERAL).withStrikeoutness(op.equals(PasTypes.GOTO)).withInsertHandler(INSERT_HANDLER);
                result.caseInsensitive().addElement(el);
            }
        }
    }

    @Override
    public void beforeCompletion(@NotNull CompletionInitializationContext context) {
        super.beforeCompletion(context);
        context.setDummyIdentifier(PasField.DUMMY_IDENTIFIER);
    }

    private static final InsertHandler<LookupElement> INSERT_HANDLER = new InsertHandler<LookupElement>() {
        @Override
        public void handleInsert(InsertionContext context, LookupElement item) {
            String content = INSERT_MAP.get(item.getLookupString());
            if (null != content) {
                content = content.replaceAll(PLACEHOLDER_FILENAME, FileUtilRt.getNameWithoutExtension(context.getFile().getName()));
                int caretPos = content.indexOf(PLACEHOLDER_CARET);
                content = content.replaceAll(PLACEHOLDER_CARET, "");
                adjustDocument(context, content, caretPos >= 0 ? caretPos : null);
            }
            context.commitDocument();
            PsiElement el = context.getFile().findElementAt(context.getEditor().getCaretModel().getOffset());
            el = skipToExpressionParent(el);
            if (el != null) {
                CodeStyleManager.getInstance(context.getFile().getManager()).reformat(el, true);
            }
        }
    };

    private static void adjustDocument(InsertionContext context, String content, Integer caretOffset) {
        final Document document = context.getEditor().getDocument();
        document.insertString(context.getEditor().getCaretModel().getOffset(), content);
        if (caretOffset != null) {
            context.getEditor().getCaretModel().moveToOffset(context.getEditor().getCaretModel().getOffset() + caretOffset);
        }
    }

}
