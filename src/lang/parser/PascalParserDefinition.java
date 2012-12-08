package com.siberika.idea.pascal.lang.parser;

import com.intellij.lang.*;
import com.intellij.lexer.*;
import com.intellij.openapi.project.*;
import com.intellij.psi.*;
import com.intellij.psi.tree.*;
import com.siberika.idea.pascal.lang.lexer.*;
import com.siberika.idea.pascal.lang.psi.impl.LuaPsiFileImpl;
import org.jetbrains.annotations.*;

import static com.siberika.idea.pascal.lang.parser.PascalElementTypes.*;

/**
 * Author: George Bakhtadze
 * Date: 12/5/12
 */
public class PascalParserDefinition implements ParserDefinition {
    public static final IStubFileElementType LUA_FILE = null;//new LuaStubFileElementType();
    //public static final IFileElementType LUA_FILE = new IFileElementType("Lua Script", PascalFileType.PASCAL_LANGUAGE);

    @NotNull
    public Lexer createLexer(Project project) {
        //return new PascalParsingLexerMergingAdapter(new PascalLexer());
        return new PascalLexer();
    }

    public PsiParser createParser(Project project) {
        return new PascalParser();
    }

    public IFileElementType getFileNodeType() {
        return LUA_FILE;
    }

    @NotNull
    public TokenSet getWhitespaceTokens() {
        return WHITE_SPACES_SET;
    }

    @NotNull
    public TokenSet getCommentTokens() {
        return COMMENT_SET;
    }

    @NotNull
    public TokenSet getStringLiteralElements() {
        return STRING_LITERAL_SET;
    }

    @NotNull
    public PsiElement createElement(ASTNode node) {
        final PsiElement element = LuaPsiCreator.createElement(node);

        return element;
    }


    public PsiFile createFile(FileViewProvider fileViewProvider) {
        return new LuaPsiFileImpl(fileViewProvider);
    }

    public SpaceRequirements spaceExistanceTypeBetweenTokens(ASTNode left, ASTNode right) {
        if (left.getElementType() == PascalTokenTypes.SHORTCOMMENT) return SpaceRequirements.MUST_LINE_BREAK;

        if (left.getElementType() == PascalTokenTypes.NAME && KEYWORDS.contains(right.getElementType()))
            return SpaceRequirements.MUST;

        Lexer lexer = new PascalLexer();

        return LanguageUtil.canStickTokensTogetherByLexer(left, right, lexer);
    }
}
