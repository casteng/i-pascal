package com.siberika.idea.pascal.editor.completion;

import com.intellij.codeInsight.completion.CompletionContributor;
import com.intellij.codeInsight.completion.CompletionParameters;
import com.intellij.codeInsight.completion.CompletionProvider;
import com.intellij.codeInsight.completion.CompletionResultSet;
import com.intellij.codeInsight.completion.CompletionType;
import com.intellij.lang.ASTNode;
import com.intellij.patterns.PlatformPatterns;
import com.intellij.psi.PsiComment;
import com.intellij.psi.PsiElement;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.tree.TokenSet;
import com.intellij.util.ProcessingContext;
import com.siberika.idea.pascal.PascalLanguage;
import com.siberika.idea.pascal.lang.context.Context;
import com.siberika.idea.pascal.lang.lexer.PascalLexer;
import com.siberika.idea.pascal.lang.psi.PasFormalParameter;
import com.siberika.idea.pascal.lang.psi.PasLibraryModuleHead;
import com.siberika.idea.pascal.lang.psi.PasModule;
import com.siberika.idea.pascal.lang.psi.PasPackageModuleHead;
import com.siberika.idea.pascal.lang.psi.PasProgramModuleHead;
import com.siberika.idea.pascal.lang.psi.PasTypes;
import com.siberika.idea.pascal.lang.psi.PasUnitModuleHead;
import com.siberika.idea.pascal.lang.psi.PascalModule;
import com.siberika.idea.pascal.util.DocUtil;
import com.siberika.idea.pascal.util.PsiUtil;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

import static com.siberika.idea.pascal.lang.context.CodePlace.DECL_FIELD;
import static com.siberika.idea.pascal.lang.context.CodePlace.EXPR;
import static com.siberika.idea.pascal.lang.context.CodePlace.GLOBAL_DECLARATION;
import static com.siberika.idea.pascal.lang.context.CodePlace.INTERFACE;
import static com.siberika.idea.pascal.lang.context.CodePlace.LOCAL_DECLARATION;
import static com.siberika.idea.pascal.lang.context.CodePlace.MODULE_HEADER;
import static com.siberika.idea.pascal.lang.context.CodePlace.STATEMENT;
import static com.siberika.idea.pascal.lang.context.CodePlace.STATEMENT_START;
import static com.siberika.idea.pascal.lang.context.CodePlace.STMT_CASE_ITEM;
import static com.siberika.idea.pascal.lang.context.CodePlace.STMT_EXCEPT;
import static com.siberika.idea.pascal.lang.context.CodePlace.STMT_FOR;
import static com.siberika.idea.pascal.lang.context.CodePlace.STMT_IF_THEN;
import static com.siberika.idea.pascal.lang.context.CodePlace.STMT_REPEAT;
import static com.siberika.idea.pascal.lang.context.CodePlace.STMT_TRY;
import static com.siberika.idea.pascal.lang.context.CodePlace.STMT_WHILE;
import static com.siberika.idea.pascal.lang.context.CodePlace.UNKNOWN;
import static com.siberika.idea.pascal.lang.context.CodePlace.USES;

public class PascalCtxCompletionContributor extends CompletionContributor {

    private static final TokenSet TS_BEGIN = TokenSet.create(PasTypes.BEGIN);
    private static final TokenSet TS_ELSE = TokenSet.create(PasTypes.ELSE);
    private static final TokenSet TS_CLASS = TokenSet.create(PasTypes.CLASS);
    private static final TokenSet DECLARATIONS_LOCAL = TokenSet.create(PasTypes.VAR, PasTypes.CONST, PasTypes.TYPE, PasTypes.PROCEDURE, PasTypes.FUNCTION);

    @Override
    public boolean invokeAutoPopup(@NotNull PsiElement position, char typeChar) {
        return position instanceof PsiComment && typeChar == '$';
    }

    @SuppressWarnings("unchecked")
    public PascalCtxCompletionContributor() {
        extend(CompletionType.BASIC, PlatformPatterns.psiElement().withLanguage(PascalLanguage.INSTANCE), new CompletionProvider<CompletionParameters>() {
            @Override
            protected void addCompletions(@NotNull CompletionParameters parameters, ProcessingContext context, @NotNull CompletionResultSet result) {
                Context ctx = new Context(parameters.getOriginalPosition(), parameters.getPosition(), parameters.getOriginalFile());

                if (ctx.getPrimary() == EXPR && ctx.contains(STATEMENT_START)) {
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
                } else if ((ctx.getPrimary() == STATEMENT) || (ctx.getPrimary() == STMT_EXCEPT)) {
                    if (!getDoThenOf(result, ctx, parameters.getOffset()) && (ctx.contains(STMT_IF_THEN))) {
                        CompletionUtil.appendTokenSet(result, TS_ELSE);
                    }
                } else if (ctx.getPrimary() == STMT_CASE_ITEM) {
                    CompletionUtil.appendTokenSetUnique(result, PasTypes.ELSE, ctx.getPosition().getParent());
                }

                if ((ctx.getPosition() instanceof PasFormalParameter) && (((PasFormalParameter) ctx.getPosition()).getParamType() == null)) {
                    CompletionUtil.appendText(result, "const ");
                    CompletionUtil.appendText(result, "var ");
                    CompletionUtil.appendText(result, "out ");
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
                if (ctx.getPrimary() == GLOBAL_DECLARATION) {
                    CompletionUtil.appendTokenSet(result, PascalLexer.DECLARATIONS_INTF);
                    CompletionUtil.appendTokenSetUnique(result, PascalLexer.USES, PsiUtil.skipToExpressionParent(parameters.getPosition()));
                    if (!ctx.contains(INTERFACE)) {
                        result.caseInsensitive().addElement(CompletionUtil.getElement("begin  "));
                        CompletionUtil.appendTokenSet(result, PascalLexer.DECLARATIONS_IMPL);
                        PasModule mod = PsiUtil.getElementPasModule(ctx.getPosition());
                        if ((mod != null) && (mod.getModuleType() == PascalModule.ModuleType.UNIT)) {
                            CompletionUtil.appendTokenSetUnique(result, TokenSet.create(PasTypes.INITIALIZATION,PasTypes.FINALIZATION),
                                    PsiUtil.getModuleImplementationSection(parameters.getOriginalFile()));
                        }
                    }
                } else if (ctx.getPrimary() == LOCAL_DECLARATION) {
                    CompletionUtil.appendTokenSet(result, DECLARATIONS_LOCAL);
                    CompletionUtil.appendTokenSet(result, TokenSet.create(PasTypes.BEGIN));
                }

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

}
