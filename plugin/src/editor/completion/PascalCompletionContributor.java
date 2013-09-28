package com.siberika.idea.pascal.editor.completion;

import com.intellij.codeInsight.completion.CompletionContributor;
import com.intellij.codeInsight.completion.CompletionParameters;
import com.intellij.codeInsight.completion.CompletionProvider;
import com.intellij.codeInsight.completion.CompletionResultSet;
import com.intellij.codeInsight.completion.CompletionType;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.patterns.PlatformPatterns;
import com.intellij.patterns.StandardPatterns;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiErrorElement;
import com.intellij.psi.PsiWhiteSpace;
import com.intellij.psi.impl.source.tree.LeafPsiElement;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.tree.TokenSet;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.ProcessingContext;
import com.siberika.idea.pascal.PascalIcons;
import com.siberika.idea.pascal.lang.lexer.PascalLexer;
import com.siberika.idea.pascal.lang.psi.PasAssignPart;
import com.siberika.idea.pascal.lang.psi.PasBlockGlobal;
import com.siberika.idea.pascal.lang.psi.PasBlockLocal;
import com.siberika.idea.pascal.lang.psi.PasClassMethod;
import com.siberika.idea.pascal.lang.psi.PasConstSection;
import com.siberika.idea.pascal.lang.psi.PasContainsClause;
import com.siberika.idea.pascal.lang.psi.PasDeclSection;
import com.siberika.idea.pascal.lang.psi.PasDeclSectionLocal;
import com.siberika.idea.pascal.lang.psi.PasDesignator;
import com.siberika.idea.pascal.lang.psi.PasEntityID;
import com.siberika.idea.pascal.lang.psi.PasExportedRoutine;
import com.siberika.idea.pascal.lang.psi.PasExpression;
import com.siberika.idea.pascal.lang.psi.PasForStatement;
import com.siberika.idea.pascal.lang.psi.PasFullyQualifiedIdent;
import com.siberika.idea.pascal.lang.psi.PasImplDeclSection;
import com.siberika.idea.pascal.lang.psi.PasLibraryModuleHead;
import com.siberika.idea.pascal.lang.psi.PasMethodDecl;
import com.siberika.idea.pascal.lang.psi.PasMethodDirective;
import com.siberika.idea.pascal.lang.psi.PasModule;
import com.siberika.idea.pascal.lang.psi.PasModuleProgram;
import com.siberika.idea.pascal.lang.psi.PasPackageModuleHead;
import com.siberika.idea.pascal.lang.psi.PasProcForwardDecl;
import com.siberika.idea.pascal.lang.psi.PasProgramModuleHead;
import com.siberika.idea.pascal.lang.psi.PasRepeatStatement;
import com.siberika.idea.pascal.lang.psi.PasRequiresClause;
import com.siberika.idea.pascal.lang.psi.PasRoutineDecl;
import com.siberika.idea.pascal.lang.psi.PasStatementList;
import com.siberika.idea.pascal.lang.psi.PasStmtSimpleOrAssign;
import com.siberika.idea.pascal.lang.psi.PasStruct;
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
import com.siberika.idea.pascal.util.PsiUtil;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

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
                PsiElement originalPos = skipParents(parameters.getOriginalPosition());
                PsiElement pos = skipParents(parameters.getPosition());
                int level = PsiUtil.getElementLevel(originalPos);
                if ((originalPos instanceof PasAssignPart) || (pos instanceof PasAssignPart)) {
                    appendTokenSet(result, PascalLexer.VALUES);
                } else if (originalPos instanceof PasStatementList) {
                    appendTokenSet(result, PascalLexer.STATEMENTS);
                    if (PsiTreeUtil.getParentOfType(parameters.getPosition(), PasRepeatStatement.class, PasWhileStatement.class, PasForStatement.class) != null) {
                        appendTokenSet(result, PascalLexer.STATEMENTS_IN_CYCLE);
                    }
                    if ((level <= 3) && (originalPos.getParent() instanceof PasUnitInitialization)) {
                        appendTokenSetUnique(result, TokenSet.create(PascalLexer.FINALIZATION), parameters.getOriginalFile());
                    }
                }
                if ((PsiTreeUtil.findChildOfType(parameters.getOriginalFile(), PasUnitModuleHead.class) != null)) {
                    if ((originalPos != null) && (originalPos.getParent()) instanceof PasModule) {
                        appendTokenSetIfAbsent(result, PascalLexer.UNIT_SECTIONS, parameters.getOriginalFile(),
                                PasProgramModuleHead.class, PasUnitModuleHead.class, PasLibraryModuleHead.class, PasPackageModuleHead.class);
                    }
                } else if (level <= 3) {
                    appendTokenSetUnique(result, PascalLexer.TOP_LEVEL_DECLARATIONS, parameters.getOriginalFile());
                    appendTokenSetUnique(result, TokenSet.create(PascalLexer.USES), parameters.getOriginalFile());
                    PasModuleProgram program = pos instanceof PasModuleProgram ? (PasModuleProgram) pos : PsiTreeUtil.getPrevSiblingOfType(pos.getNextSibling(), PasModuleProgram.class);
                    if ((program != null) && (program.getProgramModuleHead() == null)) {
                        appendTokenSet(result, PascalLexer.MODULE_HEADERS);
                    }
                }
                if (posIs(originalPos, pos, PasUnitInterface.class, PasImplDeclSection.class, PasDeclSection.class, PasBlockLocal.class, PasBlockGlobal.class)) {
                    appendTokenSet(result, PascalLexer.DECLARATIONS);
                } else if (posIs(originalPos, pos, PasVarSection.class, PasConstSection.class, PasTypeSection.class, PasRoutineDecl.class) && parameters.getPosition() instanceof LeafPsiElement) {
                    appendTokenSet(result, PascalLexer.DECLARATIONS);
                } else if (posIs(originalPos, pos, PasExportedRoutine.class, PasDeclSectionLocal.class) && parameters.getPosition().getParent() instanceof PsiErrorElement) {
                    appendTokenSet(result, PascalLexer.DECLARATIONS);
                } else if (pos instanceof PasTypeID) {
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

    private void handleDirectives(CompletionResultSet result, CompletionParameters parameters, PsiElement originalPos, PsiElement pos) {
        if (PsiTreeUtil.instanceOf(originalPos, PasStruct.class, PasMethodDecl.class, PasRoutineDecl.class, PasProcForwardDecl.class, PasExportedRoutine.class)) {
            pos = originalPos;
        }
        if (pos instanceof PasStruct) {
            appendTokenSet(result, PascalLexer.VISIBILITY);
            appendTokenSet(result, PascalLexer.STRUCT_DECLARATIONS);
            PsiElement el = PsiTreeUtil.skipSiblingsBackward(parameters.getOriginalPosition(), PsiWhiteSpace.class);
            if ((el instanceof PasClassMethod) || (el instanceof PasMethodDirective)) {
                appendTokenSet(result, PascalLexer.DIRECTIVE_METHOD);
            }
        } else if (pos instanceof PasMethodDecl) {
            appendTokenSet(result, PascalLexer.DIRECTIVE_METHOD);
        } else if (PsiTreeUtil.instanceOf(pos, PasRoutineDecl.class, PasProcForwardDecl.class, PasExportedRoutine.class)) {
            appendTokenSet(result, PascalLexer.DIRECTIVE_ROUTINE);
        }
    }

    private static PsiElement skipParents(PsiElement element) {
        return PsiTreeUtil.skipParentsOfType(element,
                PasSubIdent.class, PasFullyQualifiedIdent.class, PasEntityID.class, PasDesignator.class,
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

    private void appendTokenSet(CompletionResultSet result, TokenSet tokenSet) {
        for (IElementType op : tokenSet.getTypes()) {
            result.addElement(LookupElementBuilder.create(op.toString()).withIcon(PascalIcons.GENERAL).withStrikeoutness(op.equals(PasTypes.GOTO)));
        }
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
            if (PsiTreeUtil.findChildOfType(position, TOKEN_TO_PSI.get(op), true) == null) {
                result.addElement(LookupElementBuilder.create(op.toString()).withIcon(PascalIcons.GENERAL).withStrikeoutness(op.equals(PasTypes.GOTO)));
            }
        }
    }

    private void appendTokenSetIfAbsent(CompletionResultSet result, TokenSet tokenSet, PsiElement position, Class<? extends PsiElement>...classes) {
        for (IElementType op : tokenSet.getTypes()) {
            if (PsiTreeUtil.findChildOfAnyType(position, classes) == null) {
                result.addElement(LookupElementBuilder.create(op.toString()).withIcon(PascalIcons.GENERAL).withStrikeoutness(op.equals(PasTypes.GOTO)));
            }
        }
    }

}
