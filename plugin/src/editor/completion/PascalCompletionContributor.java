package com.siberika.idea.pascal.editor.completion;

import com.intellij.codeInsight.completion.*;
import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.ide.DataManager;
import com.intellij.lang.ASTNode;
import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.patterns.PlatformPatterns;
import com.intellij.psi.*;
import com.intellij.psi.impl.source.tree.LeafPsiElement;
import com.intellij.psi.impl.source.tree.TreeUtil;
import com.intellij.psi.tree.TokenSet;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.ProcessingContext;
import com.siberika.idea.pascal.PascalLanguage;
import com.siberika.idea.pascal.editor.ContextAwareVirtualFile;
import com.siberika.idea.pascal.ide.actions.GotoSuper;
import com.siberika.idea.pascal.lang.lexer.PascalLexer;
import com.siberika.idea.pascal.lang.parser.NamespaceRec;
import com.siberika.idea.pascal.lang.parser.PascalFile;
import com.siberika.idea.pascal.lang.psi.PasArgumentList;
import com.siberika.idea.pascal.lang.psi.PasAssignPart;
import com.siberika.idea.pascal.lang.psi.PasBlockGlobal;
import com.siberika.idea.pascal.lang.psi.PasBlockLocal;
import com.siberika.idea.pascal.lang.psi.PasCaseItem;
import com.siberika.idea.pascal.lang.psi.PasCaseStatement;
import com.siberika.idea.pascal.lang.psi.PasClassField;
import com.siberika.idea.pascal.lang.psi.PasClassParent;
import com.siberika.idea.pascal.lang.psi.PasClassProperty;
import com.siberika.idea.pascal.lang.psi.PasClassPropertySpecifier;
import com.siberika.idea.pascal.lang.psi.PasClassTypeDecl;
import com.siberika.idea.pascal.lang.psi.PasConstDeclaration;
import com.siberika.idea.pascal.lang.psi.PasConstExpressionOrd;
import com.siberika.idea.pascal.lang.psi.PasEntityScope;
import com.siberika.idea.pascal.lang.psi.PasExportedRoutine;
import com.siberika.idea.pascal.lang.psi.PasExpr;
import com.siberika.idea.pascal.lang.psi.PasForStatement;
import com.siberika.idea.pascal.lang.psi.PasFormalParameter;
import com.siberika.idea.pascal.lang.psi.PasFullyQualifiedIdent;
import com.siberika.idea.pascal.lang.psi.PasGenericTypeIdent;
import com.siberika.idea.pascal.lang.psi.PasIfStatement;
import com.siberika.idea.pascal.lang.psi.PasIfThenStatement;
import com.siberika.idea.pascal.lang.psi.PasImplDeclSection;
import com.siberika.idea.pascal.lang.psi.PasLibraryModuleHead;
import com.siberika.idea.pascal.lang.psi.PasModule;
import com.siberika.idea.pascal.lang.psi.PasNamedIdent;
import com.siberika.idea.pascal.lang.psi.PasNamedIdentDecl;
import com.siberika.idea.pascal.lang.psi.PasPackageModuleHead;
import com.siberika.idea.pascal.lang.psi.PasProgramModuleHead;
import com.siberika.idea.pascal.lang.psi.PasRepeatStatement;
import com.siberika.idea.pascal.lang.psi.PasRoutineImplDecl;
import com.siberika.idea.pascal.lang.psi.PasStatement;
import com.siberika.idea.pascal.lang.psi.PasTryStatement;
import com.siberika.idea.pascal.lang.psi.PasTypeDeclaration;
import com.siberika.idea.pascal.lang.psi.PasTypeID;
import com.siberika.idea.pascal.lang.psi.PasTypeSection;
import com.siberika.idea.pascal.lang.psi.PasTypes;
import com.siberika.idea.pascal.lang.psi.PasUnitImplementation;
import com.siberika.idea.pascal.lang.psi.PasUnitInterface;
import com.siberika.idea.pascal.lang.psi.PasUnitModuleHead;
import com.siberika.idea.pascal.lang.psi.PasUsesClause;
import com.siberika.idea.pascal.lang.psi.PasVarDeclaration;
import com.siberika.idea.pascal.lang.psi.PasWhileStatement;
import com.siberika.idea.pascal.lang.psi.PascalIdentDecl;
import com.siberika.idea.pascal.lang.psi.PascalModule;
import com.siberika.idea.pascal.lang.psi.PascalNamedElement;
import com.siberika.idea.pascal.lang.psi.PascalQualifiedIdent;
import com.siberika.idea.pascal.lang.psi.PascalRoutine;
import com.siberika.idea.pascal.lang.psi.PascalStructType;
import com.siberika.idea.pascal.lang.psi.impl.PasField;
import com.siberika.idea.pascal.lang.psi.impl.PascalExpression;
import com.siberika.idea.pascal.lang.references.PasReferenceUtil;
import com.siberika.idea.pascal.lang.references.ResolveContext;
import com.siberika.idea.pascal.lang.references.ResolveUtil;
import com.siberika.idea.pascal.util.DocUtil;
import com.siberika.idea.pascal.util.PsiUtil;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

/**
 * Author: George Bakhtadze
 * Date: 20/09/2013
 */
public class PascalCompletionContributor extends CompletionContributor {

    private static final TokenSet DECLARATIONS_LOCAL = TokenSet.create(
            PasTypes.VAR, PasTypes.CONST, PasTypes.TYPE, PasTypes.PROCEDURE, PasTypes.FUNCTION
    );
    private static final TokenSet TYPE_DECLARATIONS = TokenSet.create(
            PasTypes.TYPE, PasTypes.CLASS, PasTypes.OBJC_CLASS, PasTypes.DISPINTERFACE, PasTypes.RECORD, PasTypes.OBJECT,
            PasTypes.PACKED, PasTypes.SET, PasTypes.FILE, PasTypes.HELPER, PasTypes.ARRAY
    );
    private static final double PRIORITY_HIGHER = 10.0;
    private static final double PRIORITY_LOWER = -10.0;
    private static final double PRIORITY_LOWEST = -100.0;

    private static final String TYPE_UNTYPED = "<untyped>";

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
//                System.out.println(String.format("=== oPos: %s, pos: %s, oPrev: %s, prev: %s, opar: %s, par: %s", originalPos, pos, oPrev, prev, originalPos != null ? originalPos.getParent() : null, pos.getParent()));

                boolean isInherited = false;

                PasEntityScope nearestScope = PsiTreeUtil.getParentOfType(originalPos, PasEntityScope.class);
                PasEntityScope containingScope = null;

                if (nearestScope != null) {
                    containingScope = nearestScope.getContainingScope();

                    if (oPrev != null) {
                        PsiElement deepestFirst = PsiTreeUtil.getDeepestFirst(oPrev);
                        if (deepestFirst != null && (deepestFirst instanceof LeafPsiElement) && ((LeafPsiElement)deepestFirst).getElementType() == PasTypes.INHERITED) {
                            isInherited = true;
                        }
                    }
                    else if (originalPos instanceof LeafPsiElement) {
                        if (((LeafPsiElement)originalPos).getElementType() == PasTypes.NAME) {
                            PasStatement parent = PsiTreeUtil.getParentOfType(originalPos, PasStatement.class);
                            if (parent != null) {
                                PsiElement deepestFirst = PsiTreeUtil.getDeepestFirst(parent);
                                if (deepestFirst != null && (deepestFirst instanceof LeafPsiElement) && ((LeafPsiElement)deepestFirst).getElementType() == PasTypes.INHERITED) {
                                    isInherited = true;
                                }
                            }
                        }
                    }
                }


                originalPos = PsiUtil.skipToExpressionParent(parameters.getOriginalPosition());
                pos = PsiUtil.skipToExpressionParent(parameters.getPosition());
                prev = PsiTreeUtil.skipSiblingsBackward(pos, PsiWhiteSpace.class, PsiComment.class);
                oPrev = PsiTreeUtil.skipSiblingsBackward(originalPos, PsiWhiteSpace.class, PsiComment.class);
                PsiElement expr = PsiUtil.skipToExpression(parameters.getOriginalPosition());
                int level = PsiUtil.getElementLevel(originalPos);
//                System.out.println(String.format("=== skipped. oPos: %s, pos: %s, oPrev: %s, prev: %s, opar: %s, par: %s, lvl: %d", originalPos, pos, oPrev, prev, originalPos != null ? originalPos.getParent() : null, pos.getParent(), level));

                Collection<PasField> entities = new HashSet<PasField>();

                if ((pos instanceof PsiFile) && (((PsiFile) pos).getVirtualFile() instanceof ContextAwareVirtualFile)) {
                    NamespaceRec namespace = NamespaceRec.fromFQN(pos, pos.getText().replace(PasField.DUMMY_IDENTIFIER, "")); // TODO: refactor
                    namespace.setIgnoreVisibility(true);
                    namespace.clearTarget();
                    ResolveContext resolveContext = new ResolveContext(PsiUtil.getNearestAffectingScope(((ContextAwareVirtualFile) ((PsiFile) pos).getVirtualFile()).getContextElement()),
                            PasField.TYPES_ALL, false, null);
                    entities.addAll(PasReferenceUtil.resolve(namespace, resolveContext, 0));
                    addEntitiesToResult(result, entities, parameters, originalPos, containingScope, false);
                    result.stopHere();
                    return;
                }

                if (isInherited) {
                    handleInherited(pos, entities);
                    addEntitiesToResult(result, entities, parameters, originalPos, containingScope, true);
                    result.stopHere();
                    return;
                }

                if (parameters.getOriginalPosition() instanceof PsiComment) {
                    PascalCompletionInComment.handleComments(result, parameters);
                    result.stopHere();
                    return;
                }

                if (!(prev instanceof PasTypeSection) && !(PsiUtil.isInstanceOfAny(originalPos, PasTypeSection.class, PasRoutineImplDecl.class, PasBlockLocal.class))) {
                    handleModuleHeader(result, parameters, pos);
                    handleModuleSection(result, parameters, pos, originalPos);
                    if ((pos instanceof PasUsesClause)) {
                        /*if ((parameters.getPosition().getParent() instanceof PsiErrorElement) || (parameters.getOriginalPosition().getParent() instanceof PsiErrorElement)) {
                            return;
                        }*/
                        CompletionUtil.handleUses(result, pos);
                    }
                }
                handleDeclarations(result, parameters, pos, originalPos);
                handleStructured(result, parameters, pos, originalPos);
                handleParameters(result, pos, originalPos);

                handleEntities(result, parameters, pos, originalPos, expr, entities);

                handleStatement(result, parameters, pos, originalPos, entities);
                handleInsideStatement(result, parameters, pos, originalPos);

                handleDirectives(result, parameters, originalPos, pos, prev);
                addEntitiesToResult(result, entities, parameters, originalPos, containingScope);
                result.restartCompletionWhenNothingMatches();
            }
        });
    }

    private void addEntitiesToResult(CompletionResultSet result, Collection<PasField> entities, CompletionParameters parameters, PsiElement position, PasEntityScope containingScope)
    {
        addEntitiesToResult(result, entities, parameters, position, containingScope, false);
    }

    private void addEntitiesToResult(CompletionResultSet result, Collection<PasField> entities, CompletionParameters parameters, PsiElement position, PasEntityScope containingScope, boolean inheritedCall) {
        PasModule pasModule = position instanceof PasModule ? (PasModule)position : PsiTreeUtil.getParentOfType(position, PasModule.class);
        String pasModuleName = null;
        List<PasEntityScope> parents = null;
        if (pasModule != null) {
            pasModuleName = pasModule.getName();
            parents = new ArrayList<>();
            if (containingScope != null) {
                // Get parent classes list
                GotoSuper.getParentStructs(parents, containingScope);
            }
        }

        Set<String> nameSet = new HashSet<String>();                                  // TODO: replace with proper implementation of LookupElement
        Collection<LookupElement> lookupElements = new HashSet<LookupElement>();
        for (PasField field : entities) if (field.getElement() != null) {
            PasModule fieldPasModule = PsiTreeUtil.getParentOfType(field.getElement(), PasModule.class);

            if (fieldPasModule == null || pasModuleName == null || pasModuleName.compareToIgnoreCase(fieldPasModule.getName()) != 0) {
                if (field.visibility == PasField.Visibility.PROTECTED) {
                    boolean isInParent = false;
                    PasEntityScope fieldScope = PsiTreeUtil.getParentOfType(field.getElement(), PasEntityScope.class);
                    if (parents != null && fieldScope.getContainingScope() != containingScope) {
                        for (PasEntityScope parent : parents) {
                            if (parent == fieldScope) {
                                isInParent = true;
                                break;
                            }
                        }
                        if (!isInParent) {
                            continue;
                        }
                    }
                }
                else  if (field.visibility != PasField.Visibility.PUBLIC && field.visibility != PasField.Visibility.PUBLISHED) {
                    // Unit names are marked as private, but they should be accessible
                    if (field.fieldType != PasField.FieldType.UNIT) {
                        continue;
                    }
                }
            }
            else
            {
                PasEntityScope fieldScope = PsiTreeUtil.getParentOfType(field.getElement(), PasEntityScope.class);
                if (fieldScope != null && containingScope != null && fieldScope.getUniqueName().compareToIgnoreCase(containingScope.getUniqueName()) != 0) {
                    if (inheritedCall || field.visibility == PasField.Visibility.STRICT_PRIVATE || field.visibility == PasField.Visibility.STRICT_PROTECTED) {
                        continue;
                    }
                }
            }

            String name = getFieldName(field).toUpperCase();
            if (!nameSet.contains(name) && StringUtil.isNotEmpty(name)) {
                lookupElements.add(getLookupElement(parameters.getOriginalFile().getVirtualFile(), parameters.getEditor(), field, null));
                nameSet.add(name);
            }
        }
        result.caseInsensitive().addAllElements(lookupElements);
    }

    private static final TokenSet TS_BEGIN = TokenSet.create(PasTypes.BEGIN);
    private static final TokenSet TS_UNTIL = TokenSet.create(PasTypes.UNTIL);
    private static final TokenSet TS_ELSE = TokenSet.create(PasTypes.ELSE);
    private static final TokenSet TS_CLASS = TokenSet.create(PasTypes.CLASS);

    private void handleInsideStatement(CompletionResultSet result, CompletionParameters parameters, PsiElement pos, PsiElement originalPos) {
        if (((pos instanceof PasStatement) || (pos instanceof PasCaseItem))
                && (parameters.getOriginalPosition() != null)) {
            if (!isPartOfExpression(parameters.getPosition()) && !isQualified(parameters.getPosition())) {
                CompletionUtil.appendTokenSet(result, PascalLexer.STATEMENTS);
            }
            if ((pos.getParent() instanceof PasIfThenStatement) || (originalPos instanceof PasCaseStatement) || (originalPos instanceof PasCaseItem)) {
                CompletionUtil.appendTokenSet(result, TS_ELSE);
            }
            if (pos.getParent() instanceof PasTryStatement) {
                CompletionUtil.appendTokenSetUnique(result, PasTypes.EXCEPT, pos.getParent());
            }
            if (pos.getParent() instanceof PasRepeatStatement) {
                CompletionUtil.appendTokenSetUnique(result, PasTypes.UNTIL, pos.getParent());
            }

            if (!CompletionUtil.isControlStatement(pos)) {
                pos = pos.getParent();
            }
            if (pos instanceof PasIfThenStatement) {
                pos = pos.getParent();
            }
            if (CompletionUtil.isControlStatement(pos)) {
                ASTNode doThenOf = CompletionUtil.getDoThenOf(pos);
                if (doThenOf != null) {
                    if (doThenOf.getStartOffset() < parameters.getOffset()) {
                        CompletionUtil.appendTokenSet(result, TS_BEGIN);
                    }
                } else {
                    CompletionUtil.appendTokenSet(result, CompletionUtil.TS_DO_THEN_OF);
                }
            } else if (pos instanceof PasCaseItem) {
                CompletionUtil.appendTokenSet(result, TS_BEGIN);
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
                CompletionUtil.appendTokenSet(result, TS_UNTIL);
            }
        }
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
        PascalQualifiedIdent fqi = PsiUtil.getFQI(pos);
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
            addEntities(result, entities, parameters.getPosition(), PasField.TYPES_ALL, parameters);
            PsiElement prev = PsiTreeUtil.skipSiblingsBackward(parameters.getOriginalPosition(), PsiWhiteSpace.class, PsiComment.class);
            if ((null == prev) || (!prev.getText().equals("."))) {
                CompletionUtil.appendTokenSet(result, PascalLexer.VALUES);
            }
        } else if (originalPos instanceof PasStatement) {
            pos = originalPos;
        }
        if (pos instanceof PasStatement) {                                                                          // identifier completion in left part of assignment
            addEntities(result, entities, parameters.getPosition(), PasField.TYPES_LEFT_SIDE, parameters);        // complete identifier variants
            //noinspection unchecked
            if (PsiTreeUtil.getParentOfType(parameters.getOriginalPosition(), PasForStatement.class, PasWhileStatement.class, PasRepeatStatement.class) != null) {
                CompletionUtil.appendTokenSet(result, PascalLexer.STATEMENTS_IN_CYCLE);
            }
        }
    }

    private void handleEntities(CompletionResultSet result, CompletionParameters parameters, PsiElement pos, PsiElement originalPos, PsiElement expr, Collection<PasField> entities) {
        if ((pos instanceof PasTypeID) || (originalPos instanceof PasTypeID)) {                                                                          // Type declaration
            addEntities(result, entities, parameters.getPosition(), PasField.TYPES_TYPE_UNIT, parameters);
            if (!PsiUtil.isInstanceOfAny(pos.getParent(), PasClassParent.class)) {
                CompletionUtil.appendTokenSet(result, TYPE_DECLARATIONS);
                result.caseInsensitive().addElement(CompletionUtil.getElement("interface "));
            }
        } else if (pos instanceof PasClassPropertySpecifier || pos instanceof PasClassProperty) {
            addEntities(result, entities, parameters.getPosition(), PasField.TYPES_PROPERTY_SPECIFIER, parameters);
        } else if (pos instanceof PasConstExpressionOrd) {
            addEntities(result, entities, parameters.getPosition(), PasField.TYPES_STATIC, parameters);
        } else if (expr instanceof PascalExpression) {
            addEntities(result, entities, parameters.getPosition(), PasField.TYPES_ALL, parameters);
        }
    }

    private static void handleStructured(CompletionResultSet result, CompletionParameters parameters, PsiElement pos, PsiElement originalPos) {
        if (pos instanceof PasClassField || originalPos instanceof PasClassTypeDecl
         || (pos instanceof PasExportedRoutine && isMethod((PascalRoutine) pos))) {
            if (DocUtil.isFirstOnLine(parameters.getEditor(), parameters.getPosition())) {
                CompletionUtil.appendTokenSet(result, PascalLexer.VISIBILITY);
                CompletionUtil.appendTokenSet(result, TokenSet.andNot(PascalLexer.STRUCT_DECLARATIONS, TS_CLASS));
                CompletionUtil.appendText(result, "class ");
                CompletionUtil.appendTokenSet(result, DECLARATIONS_LOCAL);
            }
        }
    }

    private void handleParameters(CompletionResultSet result, PsiElement pos, PsiElement originalPos) {
        if ((pos instanceof PasFormalParameter) && (((PasFormalParameter) pos).getParamType() == null)) {
            CompletionUtil.appendText(result, "const ");
            CompletionUtil.appendText(result, "var ");
            CompletionUtil.appendText(result, "out ");
        }
    }

    private void handleDeclarations(CompletionResultSet result, CompletionParameters parameters, PsiElement pos, PsiElement originalPos) {
        if (PsiUtil.isInstanceOfAny(PsiTreeUtil.skipSiblingsBackward(parameters.getOriginalPosition(), PsiWhiteSpace.class, PsiComment.class),
                PasGenericTypeIdent.class, PasNamedIdent.class, PasNamedIdentDecl.class)) {
            return;                                                                                                       // Inside type declaration
        }
        if (PsiUtil.isInstanceOfAny(pos, PasUnitInterface.class, PasUnitImplementation.class,
                PasTypeDeclaration.class, PasConstDeclaration.class, PasVarDeclaration.class,
                PasRoutineImplDecl.class, PasBlockLocal.class, PasBlockGlobal.class, PasImplDeclSection.class)) {
            PsiElement scope = pos instanceof PasEntityScope ? (PasEntityScope) pos : PsiUtil.getNearestSection(pos);
            if ((scope instanceof PasUnitImplementation) || (pos instanceof PasUnitImplementation)) {
                CompletionUtil.appendTokenSetUnique(result, PascalLexer.UNIT_SECTIONS, scope.getParent());
            }
            if (scope instanceof PascalRoutine) {
                if (DocUtil.isFirstOnLine(parameters.getEditor(), parameters.getPosition())) {
                    CompletionUtil.appendTokenSet(result, DECLARATIONS_LOCAL);
                }
            } else {
                CompletionUtil.appendTokenSet(result, PascalLexer.DECLARATIONS_INTF);
                CompletionUtil.appendTokenSet(result, PascalLexer.DECLARATIONS_IMPL);
                CompletionUtil.appendTokenSetUnique(result, PascalLexer.USES, scope);
            }
            CompletionUtil.appendTokenSet(result, TS_BEGIN);
        }
    }

    private void handleModuleSection(CompletionResultSet result, CompletionParameters parameters, PsiElement pos, PsiElement originalPos) {
        if ((pos instanceof PascalModule)) {
            PascalModule module = (PascalModule) pos;
            switch (module.getModuleType()) {
                case UNIT: {
                    if (!(originalPos instanceof PascalFile)) {
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
                    CompletionUtil.appendTokenSetUnique(result, TokenSet.create(PascalLexer.USES), parameters.getOriginalFile());
                    CompletionUtil.appendTokenSet(result, PascalLexer.DECLARATIONS_INTF);
                    CompletionUtil.appendTokenSet(result, PascalLexer.DECLARATIONS_IMPL);
                    result.caseInsensitive().addElement(CompletionUtil.getElement("begin  "));
            }
        }
    }

    private void handleInherited(PsiElement pos, Collection<PasField> entities) {
        NamespaceRec namespace = NamespaceRec.fromFQN(pos, PasField.DUMMY_IDENTIFIER);
        if (PsiUtil.isIdent(pos.getParent())) {
            if (pos.getParent().getParent() instanceof PascalNamedElement) {
                namespace = NamespaceRec.fromElement(pos.getParent());
            } else {
                namespace = NamespaceRec.fromFQN(pos, ((PascalNamedElement) pos).getName());
            }
        }
        namespace.clearTarget();

        for (PasField pasField : PasReferenceUtil.resolveExpr(namespace, new ResolveContext(EnumSet.of(PasField.FieldType.ROUTINE), true), 0)) {
            if ((pasField.name != null) && !pasField.name.contains(ResolveUtil.STRUCT_SUFFIX)) {
                if (pasField.owner instanceof PasClassTypeDecl) {
                    PasClassTypeDecl owner = (PasClassTypeDecl)pasField.owner;
                    System.out.println(owner.getClassMethodResolutionList());
                    entities.add(pasField);
                }
            }
        }
    }

    private void handleModuleHeader(CompletionResultSet result, CompletionParameters parameters, PsiElement pos) {
        if (PsiTreeUtil.findChildOfType(parameters.getOriginalFile(), PasModule.class) == null) {                     // no module found
            CompletionUtil.appendTokenSetIfAbsent(result, PascalLexer.MODULE_HEADERS, parameters.getOriginalFile(),
                    PasProgramModuleHead.class, PasUnitModuleHead.class, PasLibraryModuleHead.class, PasPackageModuleHead.class);
            if ((parameters.getOriginalPosition() != null) && (PsiTreeUtil.skipSiblingsForward(parameters.getOriginalPosition(), PsiWhiteSpace.class, PsiComment.class) != null)) {
                result.caseInsensitive().addElement(CompletionUtil.getElement("begin"));
            } else {
                result.caseInsensitive().addElement(CompletionUtil.getElement("begin "));
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

    private static LookupElement getLookupElement(VirtualFile virtualFile, Editor editor, @NotNull PasField field, @Nullable InsertHandler<LookupElement> handler) {
        String scope = field.owner != null ? field.owner.getName() : "-";
        LookupElementBuilder lookupElement = buildFromElement(field) ? createLookupElement(editor, field) : LookupElementBuilder.create(field.name);
        LookupElement element = lookupElement.appendTailText(" : " + field.fieldType.toString().toLowerCase(), true).
                withCaseSensitivity(true).withTypeText(scope, false).withInsertHandler(handler);
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

    private static LookupElement priority(LookupElement element, double priority) {
        return PrioritizedLookupElement.withPriority(element, priority);
    }

    private static boolean buildFromElement(@NotNull PasField field) {
        return (field.getElementPtr() != null) && (field.fieldType != PasField.FieldType.PSEUDO_VARIABLE);
    }

    private static LookupElementBuilder createLookupElement(final Editor editor, @NotNull PasField field) {
        assert field.getElement() != null;
        LookupElementBuilder res = LookupElementBuilder.create(field.getElement()).withPresentableText(getFieldText(field));
        if (field.fieldType == PasField.FieldType.ROUTINE) {
            PascalNamedElement el = field.getElement();
            final String content = (el instanceof PascalRoutine && ((PascalRoutine) el).hasParameters()) ? "(" + DocUtil.PLACEHOLDER_CARET + ")" : "()" + DocUtil.PLACEHOLDER_CARET;
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

    private static String getFieldText(PasField field) {
        PascalNamedElement el = field.getElement();
        if (el instanceof PascalIdentDecl) {
            String type = ((PascalIdentDecl) el).getTypeString();
            return PsiUtil.getFieldName(el) + ": " + (type != null ? type : TYPE_UNTYPED);
        }
        return PsiUtil.getFieldName(el);
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
    private static void addEntities(CompletionResultSet result, Collection<PasField> entities, PsiElement position, Set<PasField.FieldType> fieldTypes, CompletionParameters parameters) {
        NamespaceRec namespace = NamespaceRec.fromFQN(position, PasField.DUMMY_IDENTIFIER);
        if (PsiUtil.isIdent(position.getParent())) {
            if (position.getParent().getParent() instanceof PascalNamedElement) {
                namespace = NamespaceRec.fromElement(position.getParent());
            } else {
                namespace = NamespaceRec.fromFQN(position, ((PascalNamedElement) position).getName());
            }
        }
        //String key = namespace.getCurrentName().replaceFirst(PasField.DUMMY_IDENTIFIER.substring(0, 4), "");
        //if (!namespace.isFirst() || StringUtils.isEmpty(key)) {
            namespace.clearTarget();
            for (PasField pasField : PasReferenceUtil.resolveExpr(namespace, new ResolveContext(fieldTypes, true), 0)) {
                if ((pasField.name != null) && !pasField.name.contains(ResolveUtil.STRUCT_SUFFIX)) {
                    entities.add(pasField);
                }
            }
        /*} else {
            addSymbolsToResult(result, findSymbols(position.getProject(), key), parameters);
        }*/

    }

    private static void handleDirectives(CompletionResultSet result, CompletionParameters parameters, PsiElement originalPos, PsiElement pos, PsiElement prev) {
        if (DocUtil.isFirstOnLine(parameters.getEditor(), parameters.getPosition())) {
            return;
        }
        PascalRoutine routine = null;
        if (originalPos instanceof PascalRoutine) {
            routine = (PascalRoutine) originalPos;
        } else if (prev instanceof PascalRoutine) {
            routine = (PascalRoutine) prev;
        }
        if (routine != null) {
            if (isMethod(routine)) {
                if (routine instanceof PasExportedRoutine) {                   // Directives should appear in the class declaration only, not in the defining declaration
                    CompletionUtil.appendTokenSet(result, PascalLexer.DIRECTIVE_METHOD);
                }
            } else {
                CompletionUtil.appendTokenSet(result, PascalLexer.DIRECTIVE_ROUTINE);
            }
        }
    }

    private static boolean isMethod(PascalRoutine routine) {
        return routine.getContainingScope() instanceof PascalStructType;
    }

    @Override
    public void beforeCompletion(@NotNull CompletionInitializationContext context) {
        super.beforeCompletion(context);
        context.setDummyIdentifier(PasField.DUMMY_IDENTIFIER);
    }

}
