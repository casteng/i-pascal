package com.siberika.idea.pascal.editor.completion;

import com.intellij.codeInsight.completion.CompletionContributor;
import com.intellij.codeInsight.completion.CompletionParameters;
import com.intellij.codeInsight.completion.CompletionProvider;
import com.intellij.codeInsight.completion.CompletionResultSet;
import com.intellij.codeInsight.completion.CompletionType;
import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.patterns.PlatformPatterns;
import com.intellij.patterns.StandardPatterns;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiErrorElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiWhiteSpace;
import com.intellij.psi.impl.source.tree.LeafPsiElement;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.tree.TokenSet;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.ProcessingContext;
import com.siberika.idea.pascal.PascalIcons;
import com.siberika.idea.pascal.lang.lexer.PascalLexer;
import com.siberika.idea.pascal.lang.parser.NamespaceRec;
import com.siberika.idea.pascal.lang.psi.PasAssignPart;
import com.siberika.idea.pascal.lang.psi.PasBlockGlobal;
import com.siberika.idea.pascal.lang.psi.PasClassMethod;
import com.siberika.idea.pascal.lang.psi.PasCompoundStatement;
import com.siberika.idea.pascal.lang.psi.PasConstSection;
import com.siberika.idea.pascal.lang.psi.PasContainsClause;
import com.siberika.idea.pascal.lang.psi.PasDeclSection;
import com.siberika.idea.pascal.lang.psi.PasEntityID;
import com.siberika.idea.pascal.lang.psi.PasEntityScope;
import com.siberika.idea.pascal.lang.psi.PasExportedRoutine;
import com.siberika.idea.pascal.lang.psi.PasExpression;
import com.siberika.idea.pascal.lang.psi.PasForStatement;
import com.siberika.idea.pascal.lang.psi.PasFullyQualifiedIdent;
import com.siberika.idea.pascal.lang.psi.PasHintingDirective;
import com.siberika.idea.pascal.lang.psi.PasImplDeclSection;
import com.siberika.idea.pascal.lang.psi.PasLibraryModuleHead;
import com.siberika.idea.pascal.lang.psi.PasModule;
import com.siberika.idea.pascal.lang.psi.PasModuleProgram;
import com.siberika.idea.pascal.lang.psi.PasPackageModuleHead;
import com.siberika.idea.pascal.lang.psi.PasProcForwardDecl;
import com.siberika.idea.pascal.lang.psi.PasProgramModuleHead;
import com.siberika.idea.pascal.lang.psi.PasRepeatStatement;
import com.siberika.idea.pascal.lang.psi.PasRequiresClause;
import com.siberika.idea.pascal.lang.psi.PasStatementList;
import com.siberika.idea.pascal.lang.psi.PasStmtSimpleOrAssign;
import com.siberika.idea.pascal.lang.psi.PasSubIdent;
import com.siberika.idea.pascal.lang.psi.PasTypeID;
import com.siberika.idea.pascal.lang.psi.PasTypeSection;
import com.siberika.idea.pascal.lang.psi.PasTypes;
import com.siberika.idea.pascal.lang.psi.PasUnitFinalization;
import com.siberika.idea.pascal.lang.psi.PasUnitImplementation;
import com.siberika.idea.pascal.lang.psi.PasUnitInitialization;
import com.siberika.idea.pascal.lang.psi.PasUnitInterface;
import com.siberika.idea.pascal.lang.psi.PasUnitModuleHead;
import com.siberika.idea.pascal.lang.psi.PasUsesFileClause;
import com.siberika.idea.pascal.lang.psi.PasVarSection;
import com.siberika.idea.pascal.lang.psi.PasWhileStatement;
import com.siberika.idea.pascal.lang.psi.PascalPsiElement;
import com.siberika.idea.pascal.lang.psi.PascalStructType;
import com.siberika.idea.pascal.lang.psi.impl.PasField;
import com.siberika.idea.pascal.lang.psi.impl.PascalRoutineImpl;
import com.siberika.idea.pascal.lang.references.PasReferenceUtil;
import com.siberika.idea.pascal.util.PsiUtil;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static com.intellij.patterns.PlatformPatterns.psiElement;

/**
 * Author: George Bakhtadze
 * Date: 20/09/2013
 */
public class PascalCompletionContributor extends CompletionContributor {
    @SuppressWarnings("unchecked")
    public PascalCompletionContributor() {
        extend(CompletionType.BASIC, PlatformPatterns.psiElement(), new CompletionProvider<CompletionParameters>() {
            @Override
            protected void addCompletions(@NotNull CompletionParameters parameters, ProcessingContext context, @NotNull CompletionResultSet result) {
                PsiElement originalPos = parameters.getOriginalPosition();
                PsiElement pos = parameters.getPosition();
                PsiElement prev = pos.getPrevSibling();
                PsiElement oPrev = originalPos.getPrevSibling();
                System.out.println(String.format("=== oPos: %s, pos: %s, oPrev: %s, prev: %s, opar: %s, par: %s", originalPos, pos, oPrev, prev, originalPos.getParent(), pos.getParent()));
                originalPos = skipToExpressionParent(parameters.getOriginalPosition());
                pos = skipToExpressionParent(parameters.getPosition());
                prev = pos.getPrevSibling();
                oPrev = originalPos.getPrevSibling();
                int level = PsiUtil.getElementLevel(originalPos);
                System.out.println(String.format("=== skipped. oPos: %s, pos: %s, oPrev: %s, prev: %s, opar: %s, par: %s, lvl: %d", originalPos, pos, oPrev, prev, originalPos.getParent(), pos.getParent(), level));
                if ((originalPos instanceof PasAssignPart) || (pos instanceof PasAssignPart)) {                                 // identifier completion in right part of assignment
                    if (isIdent(parameters.getPosition().getParent()) || isIdent(parameters.getOriginalPosition().getParent())) {
                        addEntities(result, parameters.getPosition(), PasField.TYPES_ALL);
                    }
                    appendTokenSet(result, PascalLexer.VALUES);
                } else if (PsiTreeUtil.instanceOf(originalPos, PasStatementList.class, PasCompoundStatement.class) ||           // identifier completion in left part of assignment
                        PsiTreeUtil.instanceOf(pos, PasStatementList.class, PasCompoundStatement.class)) {
                    if (isIdent(parameters.getPosition().getParent())) {                                                        // complete identifier variants
                        addEntities(result, parameters.getPosition(), PasField.TYPES_ASSIGNABLE);
                    }
                    if (!isQualifiedIdent(parameters.getPosition().getParent())) {                                              // statements variants
                        appendTokenSet(result, PascalLexer.STATEMENTS);
                    }
                    if (PsiTreeUtil.getParentOfType(parameters.getPosition(), PasRepeatStatement.class, PasWhileStatement.class, PasForStatement.class) != null) {
                        appendTokenSet(result, PascalLexer.STATEMENTS_IN_CYCLE);
                    }
                    if ((level <= 3) && (originalPos.getParent() instanceof PasUnitInitialization)) {                           //
                        appendTokenSetUnique(result, TokenSet.create(PascalLexer.FINALIZATION), parameters.getOriginalFile());
                    }
                } else {
                    result.addElement(getElement("begin"));
                }
                if ((PsiTreeUtil.findChildOfType(parameters.getOriginalFile(), PasUnitModuleHead.class) != null)) {
                    if ((originalPos instanceof PasModule) || ((originalPos != null) && (originalPos.getParent()) instanceof PasModule)) {
                        appendTokenSetUnique(result, PascalLexer.UNIT_SECTIONS, parameters.getOriginalFile());
                    }
                } else if (level <= 3) {
                    appendTokenSetUnique(result, PascalLexer.TOP_LEVEL_DECLARATIONS, parameters.getOriginalFile());
                    appendTokenSetUnique(result, TokenSet.create(PascalLexer.USES), parameters.getOriginalFile());
                    PasModuleProgram program = pos instanceof PasModuleProgram ? (PasModuleProgram) pos : PsiTreeUtil.getPrevSiblingOfType(pos.getNextSibling(), PasModuleProgram.class);
                    if ((pos instanceof PsiFile) || ((program != null) && (program.getProgramModuleHead() == null))) {
                        appendTokenSetIfAbsent(result, PascalLexer.MODULE_HEADERS, parameters.getOriginalFile(),
                                PasProgramModuleHead.class, PasUnitModuleHead.class, PasLibraryModuleHead.class, PasPackageModuleHead.class);
                    }
                }
                if (posIs(originalPos, pos, PasUnitInterface.class, PasImplDeclSection.class, PasDeclSection.class, PasBlockGlobal.class, PasUnitImplementation.class)) {
                    appendTokenSet(result, PascalLexer.DECLARATIONS);
                } else if (posIs(originalPos, pos, PasVarSection.class, PasConstSection.class, PasTypeSection.class, PascalRoutineImpl.class) && parameters.getPosition() instanceof LeafPsiElement) {
                    appendTokenSet(result, PascalLexer.DECLARATIONS);
                } else if (posIs(originalPos, pos, PasExportedRoutine.class, PasDeclSection.class) && parameters.getPosition().getParent() instanceof PsiErrorElement) {
                    appendTokenSet(result, PascalLexer.DECLARATIONS);
                } else if (pos instanceof PasTypeID) {
                    addEntities(result, parameters.getPosition(), PasField.TYPES_TYPE);
                    appendTokenSet(result, PascalLexer.TYPE_DECLARATIONS);
                }
                handleDirectives(result, parameters, originalPos, pos);
            }
        });

        extend(CompletionType.BASIC, psiElement().afterLeafSkipping(psiElement().whitespaceCommentEmptyOrError(),
                StandardPatterns.or(psiElement().withText("interface"), psiElement().withText("implementation"))), new CompletionProvider<CompletionParameters>() {
            @Override
            protected void addCompletions(@NotNull CompletionParameters parameters, ProcessingContext context, @NotNull CompletionResultSet result) {
                appendTokenSetUnique(result, TokenSet.create(PascalLexer.USES), PsiTreeUtil.getParentOfType(parameters.getOriginalPosition(), PasUnitInterface.class));
            }
        });

    }

    private boolean isQualifiedIdent(PsiElement parent) {
        if (isIdent(parent)) {
            PsiElement par = parent.getParent();
            if (par instanceof PasFullyQualifiedIdent) {
                return !StringUtils.isEmpty(((PasFullyQualifiedIdent) par).getNamespace());
            }
        }
        return false;
    }

    private boolean isIdent(PsiElement parent) {
        return parent instanceof PasSubIdent;
    }

    private static void addEntities(CompletionResultSet result, PsiElement originalPosition, Set<PasField.Type> types) {
        NamespaceRec namespace = new NamespaceRec((PasSubIdent) originalPosition.getParent());
        result.caseInsensitive().addAllElements(PasReferenceUtil.getEntities(namespace, types));
    }

    private static void handleDirectives(CompletionResultSet result, CompletionParameters parameters, PsiElement originalPos, PsiElement pos) {
        if (PsiTreeUtil.instanceOf(originalPos, PasEntityScope.class, PascalRoutineImpl.class, PasProcForwardDecl.class, PasExportedRoutine.class)) {
            pos = originalPos;
        }
        if (pos instanceof PasEntityScope) {
            if (pos instanceof PascalStructType) {
                appendTokenSet(result, PascalLexer.VISIBILITY);
            } else {
                appendTokenSet(result, PascalLexer.STRUCT_DECLARATIONS);
            }
            PsiElement el = PsiTreeUtil.skipSiblingsBackward(parameters.getOriginalPosition(), PsiWhiteSpace.class);
            if ((el instanceof PasClassMethod) || (el instanceof PasHintingDirective)) {
                appendTokenSet(result, PascalLexer.DIRECTIVE_METHOD);
            }
        } else if (pos instanceof PascalRoutineImpl) {
            appendTokenSet(result, PascalLexer.DIRECTIVE_METHOD);
        } else if (PsiTreeUtil.instanceOf(pos, PascalRoutineImpl.class, PasProcForwardDecl.class, PasExportedRoutine.class)) {
            appendTokenSet(result, PascalLexer.DIRECTIVE_ROUTINE);
        }
    }

    private static PsiElement skipToExpressionParent(PsiElement element) {
        return PsiTreeUtil.skipParentsOfType(element,
                PasSubIdent.class, PasFullyQualifiedIdent.class, PasEntityID.class,
                PasStmtSimpleOrAssign.class, PasExpression.class, PsiWhiteSpace.class, PsiErrorElement.class);
    }

    private static boolean posIs(PsiElement originalPos, PsiElement pos, Class<? extends PsiElement>...classes) {
        for (Class<? extends PsiElement> clazz : classes) {
            if (clazz.isInstance(originalPos) || clazz.isInstance(pos)) {
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
        return LookupElementBuilder.create(s).withIcon(PascalIcons.GENERAL).withStrikeoutness(s.equals(PasTypes.GOTO));
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

        TOKEN_TO_PSI.put(PascalLexer.USES, PasUsesFileClause.class);
    }

    private void appendTokenSetUnique(CompletionResultSet result, TokenSet tokenSet, PsiElement position) {
        for (IElementType op : tokenSet.getTypes()) {
            if ((TOKEN_TO_PSI.get(op) == null) || (PsiTreeUtil.findChildOfType(position, TOKEN_TO_PSI.get(op), true) == null)) {
                result.caseInsensitive().addElement(LookupElementBuilder.create(op.toString()).withIcon(PascalIcons.GENERAL).withStrikeoutness(op.equals(PasTypes.GOTO)));
            }
        }
    }

    private void appendTokenSetIfAbsent(CompletionResultSet result, TokenSet tokenSet, PsiElement position, Class<? extends PsiElement>...classes) {
        for (IElementType op : tokenSet.getTypes()) {
            if (PsiTreeUtil.findChildOfAnyType(position, classes) == null) {
                result.caseInsensitive().addElement(LookupElementBuilder.create(op.toString()).withIcon(PascalIcons.GENERAL).withStrikeoutness(op.equals(PasTypes.GOTO)));
            }
        }
    }

}
