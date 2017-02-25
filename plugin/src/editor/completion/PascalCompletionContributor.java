package com.siberika.idea.pascal.editor.completion;

import com.intellij.codeInsight.completion.CompletionContributor;
import com.intellij.codeInsight.completion.CompletionInitializationContext;
import com.intellij.codeInsight.completion.CompletionParameters;
import com.intellij.codeInsight.completion.CompletionProvider;
import com.intellij.codeInsight.completion.CompletionResultSet;
import com.intellij.codeInsight.completion.CompletionType;
import com.intellij.codeInsight.completion.InsertHandler;
import com.intellij.codeInsight.completion.InsertionContext;
import com.intellij.codeInsight.completion.PrioritizedLookupElement;
import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.ide.DataManager;
import com.intellij.lang.ASTNode;
import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.util.io.FileUtilRt;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.patterns.PlatformPatterns;
import com.intellij.psi.PsiComment;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiErrorElement;
import com.intellij.psi.PsiWhiteSpace;
import com.intellij.psi.SmartPsiElementPointer;
import com.intellij.psi.impl.source.tree.TreeUtil;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.tree.TokenSet;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.ProcessingContext;
import com.siberika.idea.pascal.PascalIcons;
import com.siberika.idea.pascal.PascalLanguage;
import com.siberika.idea.pascal.lang.lexer.PascalLexer;
import com.siberika.idea.pascal.lang.parser.NamespaceRec;
import com.siberika.idea.pascal.lang.parser.PascalFile;
import com.siberika.idea.pascal.lang.psi.*;
import com.siberika.idea.pascal.lang.psi.impl.PasField;
import com.siberika.idea.pascal.lang.psi.impl.PascalExpression;
import com.siberika.idea.pascal.lang.psi.impl.PascalModule;
import com.siberika.idea.pascal.lang.psi.impl.PascalRoutineImpl;
import com.siberika.idea.pascal.lang.references.PasReferenceUtil;
import com.siberika.idea.pascal.util.DocUtil;
import com.siberika.idea.pascal.util.PsiUtil;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
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
    private static final TokenSet DECLARATIONS_LOCAL = TokenSet.create(
            PasTypes.VAR, PasTypes.CONST, PasTypes.TYPE, PasTypes.PROCEDURE, PasTypes.FUNCTION
    );
    private static final TokenSet TYPE_DECLARATIONS = TokenSet.create(
            PasTypes.TYPE, PasTypes.CLASS, PasTypes.DISPINTERFACE, PasTypes.RECORD, PasTypes.OBJECT,
            PasTypes.PACKED, PasTypes.SET, PasTypes.FILE, PasTypes.HELPER, PasTypes.ARRAY
    );
    private static final double PRIORITY_HIGHER = 10.0;
    private static final double PRIORITY_LOWER = -10.0;
    private static final double PRIORITY_LOWEST = -100.0;

    private static Map<String, String> getInsertMap() {
        Map<String, String> res = new HashMap<String, String>();
        res.put(PasTypes.UNIT.toString(), String.format(" %s;\n\ninterface\n\n  %s\nimplementation\n\nend.\n", PLACEHOLDER_FILENAME, DocUtil.PLACEHOLDER_CARET));
        res.put(PasTypes.PROGRAM.toString(), String.format(" %s;\nbegin\n  %s\nend.\n", PLACEHOLDER_FILENAME, DocUtil.PLACEHOLDER_CARET));
        res.put(PasTypes.LIBRARY.toString(), String.format(" %s;\n\nexports %s\n\nbegin\n\nend.\n", PLACEHOLDER_FILENAME, DocUtil.PLACEHOLDER_CARET));
        res.put(PasTypes.PACKAGE.toString(), String.format(" %s;\n\nrequires\n\n contains %s\n\nend.\n", PLACEHOLDER_FILENAME, DocUtil.PLACEHOLDER_CARET));
        res.put(PasTypes.BEGIN.toString(),       String.format("\n%s\nend;\n", DocUtil.PLACEHOLDER_CARET));
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
        res.put(PasTypes.INTERFACE.toString() + " ", String.format("(IUnknown)\n%s\nend;", DocUtil.PLACEHOLDER_CARET));
        res.put(PasTypes.ARRAY.toString(), String.format("[0..%s] of ;", DocUtil.PLACEHOLDER_CARET));
        res.put(PasTypes.SET.toString(), String.format(" of %s;", DocUtil.PLACEHOLDER_CARET));

        res.put(PasTypes.CONSTRUCTOR.toString(), String.format(" Create(%s);", DocUtil.PLACEHOLDER_CARET));
        res.put(PasTypes.DESTRUCTOR.toString(), String.format(" Destroy(%s); override;", DocUtil.PLACEHOLDER_CARET));
        res.put(PasTypes.FUNCTION.toString(), String.format(" %s(): ;", DocUtil.PLACEHOLDER_CARET));
        res.put(PasTypes.PROCEDURE.toString(), String.format(" %s();", DocUtil.PLACEHOLDER_CARET));

        res.put(PasTypes.VAR.toString(), String.format(" %s: ;", DocUtil.PLACEHOLDER_CARET));
        res.put(PasTypes.THREADVAR.toString(), String.format(" %s: ;", DocUtil.PLACEHOLDER_CARET));
        res.put(PasTypes.CONST.toString(), String.format(" %s = ;", DocUtil.PLACEHOLDER_CARET));
        res.put(PasTypes.RESOURCESTRING.toString(), String.format(" %s = '';", DocUtil.PLACEHOLDER_CARET));
        res.put(PasTypes.TYPE.toString(), String.format(" T%s = ;", DocUtil.PLACEHOLDER_CARET));

        res.put(PasTypes.PROPERTY.toString(), String.format(" %s: read ;", DocUtil.PLACEHOLDER_CARET));

        res.put(PasTypes.PACKED.toString(), " ");

        res.put(PasTypes.UNTIL.toString(), String.format(" %s;", DocUtil.PLACEHOLDER_CARET));
        res.put(PasTypes.EXCEPT.toString(), "\n");
        return res;
    }

    @Override
    public boolean invokeAutoPopup(@NotNull PsiElement position, char typeChar) {
        return position instanceof PsiComment && typeChar == '$';
    }

    @SuppressWarnings("unchecked")
    public PascalCompletionContributor() {
        extend(CompletionType.BASIC, PlatformPatterns.psiElement().withLanguage(PascalLanguage.INSTANCE), new CompletionProvider<CompletionParameters>() {
            @Override
            protected void addCompletions(@NotNull CompletionParameters parameters, ProcessingContext context, @NotNull CompletionResultSet result) {
                PsiElement originalPos = parameters.getOriginalPosition();
                PsiElement pos = parameters.getPosition();
                PsiElement prev = PsiTreeUtil.skipSiblingsBackward(pos, PsiWhiteSpace.class, PsiComment.class);
                PsiElement oPrev = PsiTreeUtil.skipSiblingsBackward(originalPos, PsiWhiteSpace.class, PsiComment.class);
                System.out.println(String.format("=== oPos: %s, pos: %s, oPrev: %s, prev: %s, opar: %s, par: %s", originalPos, pos, oPrev, prev, originalPos != null ? originalPos.getParent() : null, pos.getParent()));

                originalPos = PsiUtil.skipToExpressionParent(parameters.getOriginalPosition());
                pos = PsiUtil.skipToExpressionParent(parameters.getPosition());
                prev = PsiTreeUtil.skipSiblingsBackward(pos, PsiWhiteSpace.class, PsiComment.class);
                oPrev = PsiTreeUtil.skipSiblingsBackward(originalPos, PsiWhiteSpace.class, PsiComment.class);
                PsiElement expr = PsiUtil.skipToExpression(parameters.getOriginalPosition());
                int level = PsiUtil.getElementLevel(originalPos);
                System.out.println(String.format("=== skipped. oPos: %s, pos: %s, oPrev: %s, prev: %s, opar: %s, par: %s, lvl: %d", originalPos, pos, oPrev, prev, originalPos != null ? originalPos.getParent() : null, pos.getParent(), level));

                if (parameters.getOriginalPosition() instanceof PsiComment) {
                    PascalCompletionInComment.handleComments(result, parameters);
                    result.stopHere();
                    return;
                }

                if (!(prev instanceof PasTypeSection) && !(PsiUtil.isInstanceOfAny(originalPos, PasTypeSection.class, PasRoutineImplDecl.class, PasBlockLocal.class))) {
                    handleModuleHeader(result, parameters, pos);
                    handleModuleSection(result, parameters, pos, originalPos);
                    handleUses(result, parameters, pos, originalPos);
                }
                handleDeclarations(result, parameters, pos, originalPos);
                handleStructured(result, parameters, pos, originalPos);
                handleParameters(result, pos, originalPos);

                Collection<PasField> entities = new HashSet<PasField>();
                handleEntities(result, parameters, pos, originalPos, expr, entities);

                handleStatement(result, parameters, pos, originalPos, entities);
                handleInsideStatement(result, parameters, pos, originalPos);

                handleDirectives(result, parameters, originalPos, pos);
                Set<String> nameSet = new HashSet<String>();                                  // TODO: replace with proper implementation of LookupElement
                Collection<LookupElement> lookupElements = new HashSet<LookupElement>();
                for (PasField field : entities) {
                    String name = getFieldName(field).toUpperCase();
                    if (!nameSet.contains(name) && StringUtil.isNotEmpty(name)) {
                        lookupElements.add(getLookupElement(pos.getContainingFile().getVirtualFile(), parameters.getEditor(), field));
                        nameSet.add(name);
                    }
                }
                result.caseInsensitive().addAllElements(lookupElements);
                result.restartCompletionWhenNothingMatches();
            }
        });
    }

    private TokenSet TS_BEGIN = TokenSet.create(PasTypes.BEGIN);
    private TokenSet TS_UNTIL = TokenSet.create(PasTypes.UNTIL);
    private TokenSet TS_DO_THEN_OF = TokenSet.create(PasTypes.DO, PasTypes.THEN, PasTypes.OF);
    private TokenSet TS_ELSE = TokenSet.create(PasTypes.ELSE);
    private TokenSet TS_CLASS = TokenSet.create(PasTypes.CLASS);

    private void handleInsideStatement(CompletionResultSet result, CompletionParameters parameters, PsiElement pos, PsiElement originalPos) {
        if ((pos instanceof PasStatement) && (parameters.getOriginalPosition() != null)) {
            if (!isPartOfExpression(parameters.getPosition()) && !isQualified(parameters.getPosition())) {
                appendTokenSet(result, PascalLexer.STATEMENTS);
            }
            if ((pos.getParent() instanceof PasIfThenStatement) || (originalPos instanceof PasCaseStatement)) {
                appendTokenSet(result, TS_ELSE);
            }
            if (pos.getParent() instanceof PasTryStatement) {
                appendTokenSetUnique(result, PasTypes.EXCEPT, pos.getParent());
            }
            if (pos.getParent() instanceof PasRepeatStatement) {
                appendTokenSetUnique(result, PasTypes.UNTIL, pos.getParent());
            }

            if (!isControlStatement(pos)) {
                pos = pos.getParent();
            }
            if (pos instanceof PasIfThenStatement) {
                pos = pos.getParent();
            }
            if (isControlStatement(pos)) {
                ASTNode doThenOf = getDoThenOf(pos);
                if (doThenOf != null) {
                    if (doThenOf.getStartOffset() < parameters.getOffset()) {
                        appendTokenSet(result, TS_BEGIN);
                    }
                } else {
                    appendTokenSet(result, TS_DO_THEN_OF);
                }
            } else if (pos instanceof PasCaseItem) {
                appendTokenSet(result, TS_BEGIN);
            }
            /*ASTNode node = skipToExpressionParent(getPrevNode(parameters.getOriginalPosition().getNode()).getPsi()).getNode();
            if (node != null) {
                if (node.getElementType() == PasTypes.EXPRESSION) {
                    node = getPrevNode(node);
                }
                if (node != null) {
                    if (TS_DO_THEN_OF.contains(node.getElementType())) {
                        if (!isPartOfExpression(getFQI(parameters.getPosition()))) {
                            appendTokenSet(result, TS_BEGIN);
                        }
                    } else if (TS_STRUCT_OPERATORS.contains(node.getElementType())) {

                    }
                }
            }*/
            if (PsiTreeUtil.getParentOfType(pos, PasRepeatStatement.class) != null) {
                appendTokenSet(result, TS_UNTIL);
            }
        }
    }

    private TokenSet TS_CONTROL_STATEMENT = TokenSet.create(PasTypes.IF_STATEMENT, PasTypes.FOR_STATEMENT, PasTypes.WHILE_STATEMENT, PasTypes.WITH_STATEMENT, PasTypes.CASE_STATEMENT);
    private boolean isControlStatement(PsiElement pos) {
        return TS_CONTROL_STATEMENT.contains(pos.getNode().getElementType());
    }

    private ASTNode getDoThenOf(PsiElement statement) {
        ASTNode[] cand = statement.getNode().getChildren(TS_DO_THEN_OF);
/*        for (ASTNode astNode : cand) {
            if (inSameStatement(statement, astNode)) {
                return astNode;
            }
        }*/
        return cand.length > 0 ? cand[0] : null;
    }

    private boolean inSameStatement(PsiElement statement, ASTNode cand) {
        return statement == PsiTreeUtil.getParentOfType(cand.getPsi(), PasStatement.class);
    }

    @Nullable
    private ASTNode getPrevNode(ASTNode node) {
        ASTNode prev = TreeUtil.prevLeaf(node);
        while ((prev != null) && ((prev instanceof PsiWhiteSpace) || (prev instanceof PsiComment) || (prev instanceof PsiErrorElement))) {
            prev = TreeUtil.prevLeaf(prev);
        }
        return prev;
    }

    private boolean isQualified(PsiElement pos) {
        PasFullyQualifiedIdent fqi = PsiUtil.getFQI(pos);
        return (fqi != null) && (fqi.getSubIdentList().size() > 1);
    }

    private boolean isPartOfExpression(PsiElement element) {
        PasExpr expr = PsiTreeUtil.getParentOfType(element, PasExpr.class);
        if (null == expr) {
            return false;
        }
        if (expr.getChildren().length > 1) {
            return true;
        }
        PsiElement parent = expr.getParent();
        return (parent != null) && (parent.getChildren().length > 1) && (parent instanceof PasExpr);
    }

    private boolean isAtAssignmentRightPart(PsiElement pos, PsiElement originalPos) {
        if (PsiTreeUtil.getParentOfType(originalPos, PasCaseStatement.class) != null) {
            return true;
        }
        return PsiUtil.isInstanceOfAny(pos, PasAssignPart.class, PasArgumentList.class,
                                            PasIfStatement.class, PasWhileStatement.class, PasCaseStatement.class);
    }

    private void handleStatement(CompletionResultSet result, CompletionParameters parameters, PsiElement pos, PsiElement originalPos, Collection<PasField> entities) {
        if (isAtAssignmentRightPart(pos, parameters.getOriginalPosition())) {                           // identifier completion in right part of assignment
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
            //noinspection unchecked
            if (PsiTreeUtil.getParentOfType(parameters.getOriginalPosition(), PasForStatement.class, PasWhileStatement.class, PasRepeatStatement.class) != null) {
                appendTokenSet(result, PascalLexer.STATEMENTS_IN_CYCLE);
            }
        }
    }

    private void handleEntities(CompletionResultSet result, CompletionParameters parameters, PsiElement pos, PsiElement originalPos, PsiElement expr, Collection<PasField> entities) {
        if ((pos instanceof PasTypeID) || (originalPos instanceof PasTypeID)) {                                                                          // Type declaration
            addEntities(entities, parameters.getPosition(), PasField.TYPES_TYPE_UNIT, parameters.isExtendedCompletion());
            if (!PsiUtil.isInstanceOfAny(pos.getParent(), PasClassParent.class)) {
                appendTokenSet(result, TYPE_DECLARATIONS);
                result.caseInsensitive().addElement(getElement("interface "));
            }
        } else if (pos instanceof PasClassPropertySpecifier || pos instanceof PasClassProperty) {
            addEntities(entities, parameters.getPosition(), PasField.TYPES_PROPERTY_SPECIFIER, parameters.isExtendedCompletion());
        } else if (pos instanceof PasConstExpressionOrd) {
            addEntities(entities, parameters.getPosition(), PasField.TYPES_STATIC, parameters.isExtendedCompletion());
        } else if (expr instanceof PascalExpression) {
            addEntities(entities, parameters.getPosition(), PasField.TYPES_ALL, parameters.isExtendedCompletion());
        }
    }

    private void handleStructured(CompletionResultSet result, CompletionParameters parameters, PsiElement pos, PsiElement originalPos) {
        if (pos instanceof PasClassField || originalPos instanceof PasClassTypeDecl
         || (pos instanceof PasExportedRoutine && isMethod((PascalRoutineImpl) pos))) {
            if (DocUtil.isFirstOnLine(parameters.getEditor(), parameters.getPosition())) {
                appendTokenSet(result, PascalLexer.VISIBILITY);
                appendTokenSet(result, TokenSet.andNot(PascalLexer.STRUCT_DECLARATIONS, TS_CLASS));
                appendText(result, "class ");
                appendTokenSet(result, DECLARATIONS_LOCAL);
            }
        }
    }

    private void handleParameters(CompletionResultSet result, PsiElement pos, PsiElement originalPos) {
        if ((pos instanceof PasFormalParameter) && (((PasFormalParameter) pos).getParamType() == null)) {
            appendText(result, "const ");
            appendText(result, "var ");
            appendText(result, "out ");
        }
    }

    private void appendText(CompletionResultSet result, String s) {
        result.caseInsensitive().addElement(getElement(s));
    }

    private void handleDeclarations(CompletionResultSet result, CompletionParameters parameters, PsiElement pos, PsiElement originalPos) {
        if (PsiUtil.isInstanceOfAny(PsiTreeUtil.skipSiblingsBackward(parameters.getOriginalPosition(), PsiWhiteSpace.class, PsiComment.class), PasGenericTypeIdent.class, PasNamedIdent.class)) {
            return;                                                                                                       // Inside type declaration
        }
        if (PsiUtil.isInstanceOfAny(pos, PasUnitInterface.class, PasUnitImplementation.class,
                PasTypeDeclaration.class, PasConstDeclaration.class, PasVarDeclaration.class,
                PasRoutineImplDecl.class, PasBlockLocal.class, PasBlockGlobal.class, PasImplDeclSection.class)) {
            PsiElement scope = pos instanceof PasEntityScope ? (PasEntityScope) pos : PsiUtil.getNearestSection(pos);
            if ((scope instanceof PasUnitImplementation) || (pos instanceof PasUnitImplementation)) {
                appendTokenSetUnique(result, PascalLexer.UNIT_SECTIONS, scope.getParent());
            }
            if (scope instanceof PascalRoutineImpl) {
                if (DocUtil.isFirstOnLine(parameters.getEditor(), parameters.getPosition())) {
                    appendTokenSet(result, DECLARATIONS_LOCAL);
                }
            } else {
                appendTokenSet(result, PascalLexer.DECLARATIONS);
                appendTokenSetUnique(result, PascalLexer.USES, scope);
            }
            appendTokenSet(result, TS_BEGIN);
        }
    }

    private void handleModuleSection(CompletionResultSet result, CompletionParameters parameters, PsiElement pos, PsiElement originalPos) {
        if ((pos instanceof PascalModule)) {
            PascalModule module = (PascalModule) pos;
            switch (module.getModuleType()) {
                case UNIT: {
                    if (!(originalPos instanceof PascalFile)) {
                        appendTokenSetUnique(result, PascalLexer.UNIT_SECTIONS, pos);
                    }
                    break;
                }
                case PACKAGE: {
                    appendTokenSetUnique(result, PascalLexer.TOP_LEVEL_DECLARATIONS, parameters.getOriginalFile());
                }
                case LIBRARY:
                    result.caseInsensitive().addElement(getElement(PasTypes.EXPORTS.toString()));
                    result.caseInsensitive().addElement(getElement("begin  "));
                case PROGRAM:
                    appendTokenSetUnique(result, TokenSet.create(PascalLexer.USES), parameters.getOriginalFile());
                    appendTokenSet(result, PascalLexer.DECLARATIONS);
                    result.caseInsensitive().addElement(getElement("begin  "));
            }
        }
    }

    private void handleModuleHeader(CompletionResultSet result, CompletionParameters parameters, PsiElement pos) {
        if (PsiTreeUtil.findChildOfType(parameters.getOriginalFile(), PasModule.class) == null) {                     // no module found
            appendTokenSetIfAbsent(result, PascalLexer.MODULE_HEADERS, parameters.getOriginalFile(),
                    PasProgramModuleHead.class, PasUnitModuleHead.class, PasLibraryModuleHead.class, PasPackageModuleHead.class);
            if ((parameters.getOriginalPosition() != null) && (PsiTreeUtil.skipSiblingsForward(parameters.getOriginalPosition(), PsiWhiteSpace.class, PsiComment.class) != null)) {
                result.caseInsensitive().addElement(getElement("begin"));
            } else {
                result.caseInsensitive().addElement(getElement("begin "));
            }
        }
    }

    private static String getFieldName(PasField field) {
        if ((field.fieldType == PasField.FieldType.ROUTINE) && (field.getElement() != null)) {
            return PsiUtil.getFieldName(field.getElement());
        } else {
            return field.name;
        }
    }

    private static void handleUses(CompletionResultSet result, CompletionParameters parameters, @NotNull PsiElement pos, PsiElement originalPos) {
        if ((pos instanceof PasUsesClause)) {
            /*if ((parameters.getPosition().getParent() instanceof PsiErrorElement) || (parameters.getOriginalPosition().getParent() instanceof PsiErrorElement)) {
                return;
            }*/
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
    }

    private LookupElement getLookupElement(VirtualFile virtualFile, Editor editor, @NotNull PasField field) {
        String scope = field.owner != null ? field.owner.getName() : "-";
        LookupElementBuilder lookupElement = buildFromElement(field) ? createLookupElement(editor, field) : LookupElementBuilder.create(field.name);
        LookupElement element = lookupElement.appendTailText(" : " + field.fieldType.toString().toLowerCase(), true).
                withCaseSensitivity(true).withTypeText(scope, false);
        if (field.name.startsWith("__")) {
            return priority(element, PRIORITY_LOWEST);
        }
        if (field.name.startsWith("_")) {
            return priority(element, PRIORITY_LOWEST);
        }
        if (virtualFile != null && !virtualFile.equals(field.getElementPtr().getVirtualFile())) {
            return priority(element, PRIORITY_LOWER);
        }
        return element;
    }

    private LookupElement priority(LookupElement element, double priority) {
        return PrioritizedLookupElement.withPriority(element, priority);
    }

    private static boolean buildFromElement(@NotNull PasField field) {
        return (field.getElementPtr() != null) && (StringUtils.isEmpty(field.name) || (field.fieldType == PasField.FieldType.ROUTINE));
    }

    private LookupElementBuilder createLookupElement(final Editor editor, @NotNull PasField field) {
        assert field.getElement() != null;
        LookupElementBuilder res = LookupElementBuilder.create(field.getElement()).withPresentableText(PsiUtil.getFieldName(field.getElement()));
        if (field.fieldType == PasField.FieldType.ROUTINE) {
            PascalNamedElement el = field.getElement();
            final String content = (el instanceof PascalRoutineImpl && PsiUtil.hasParameters((PascalRoutineImpl) el)) ? "(" + DocUtil.PLACEHOLDER_CARET + ")" : "()" + DocUtil.PLACEHOLDER_CARET;
            res = res.withInsertHandler(new InsertHandler<LookupElement>() {
                @Override
                public void handleInsert(InsertionContext context, LookupElement item) {
                    DocUtil.adjustDocument(context.getEditor(), context.getEditor().getCaretModel().getOffset(), content);
                    AnAction act = ActionManager.getInstance().getAction("ParameterInfo");
                    DataContext dataContext = DataManager.getInstance().getDataContext(editor.getContentComponent());
                    act.actionPerformed(new AnActionEvent(null, dataContext, "", act.getTemplatePresentation(), ActionManager.getInstance(), 0));
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
        result.addAll(PasReferenceUtil.resolveExpr(null, namespace, fieldTypes, true, 0));
    }

    private static void handleDirectives(CompletionResultSet result, CompletionParameters parameters, PsiElement originalPos, PsiElement pos) {
        if (DocUtil.isFirstOnLine(parameters.getEditor(), parameters.getPosition())) {
            return;
        }
        if (pos instanceof PascalRoutineImpl) {
            if (isMethod((PascalRoutineImpl) pos)) {
                if (pos instanceof PasExportedRoutine) {                   // Directives should appear in the class declaration only, not in the defining declaration
                    appendTokenSet(result, PascalLexer.DIRECTIVE_METHOD);
                }
            } else {
                appendTokenSet(result, PascalLexer.DIRECTIVE_ROUTINE);
            }
        }
    }

    private static boolean isMethod(PascalRoutineImpl routine) {
        return routine.getContainingScope() instanceof PascalStructType;
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
                    PsiElement el = context.getFile().findElementAt(caretPos-1);
                    PsiElement block = PsiTreeUtil.getParentOfType(el, PasCompoundStatement.class, PasTryStatement.class, PasRepeatStatement.class);
                    if (block != null)  {
                        DocUtil.reformat(block, true);
                    }
                } else {
                    DocUtil.reformatInSeparateCommand(context.getProject(), context.getFile(), context.getEditor());
                }
            }
        }
    };

}
