package com.siberika.idea.pascal;

import com.intellij.lang.ASTNode;
import com.intellij.lang.ParserDefinition;
import com.intellij.lang.PsiParser;
import com.intellij.lexer.Lexer;
import com.intellij.openapi.project.Project;
import com.intellij.psi.FileViewProvider;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.TokenType;
import com.intellij.psi.tree.IFileElementType;
import com.intellij.psi.tree.TokenSet;
import com.siberika.idea.pascal.lang.lexer.PascalLexer;
import com.siberika.idea.pascal.lang.parser.PascalFileElementType;
import com.siberika.idea.pascal.lang.parser.PascalParser;
import com.siberika.idea.pascal.lang.parser.impl.PascalFileImpl;
import com.siberika.idea.pascal.lang.psi.PasTypes;
import org.jetbrains.annotations.NotNull;

/**
 * Author: George Bakhtadze
 * Date: 12/9/12
 */
public class PascalParserDefinition implements ParserDefinition {

    public static final IFileElementType PAS_FILE_ELEMENT_TYPE = new PascalFileElementType("PAS_FILE", PascalLanguage.INSTANCE);
    public static final TokenSet WS = TokenSet.create(TokenType.WHITE_SPACE);
    public static final TokenSet LITERALS = TokenSet.create(PasTypes.STRING_FACTOR, PasTypes.STRING_FACTOR);

    @NotNull
    @Override
    public Lexer createLexer(Project project) {
        return new PascalLexer.ParsingPascalLexer(project, null);
    }

    @Override
    public PsiParser createParser(Project project) {
        return new PascalParser();
    }

    @Override
    public IFileElementType getFileNodeType() {
        return PAS_FILE_ELEMENT_TYPE;
    }

    @NotNull
    @Override
    public TokenSet getWhitespaceTokens() {
        return WS;
    }

    @NotNull
    @Override
    public TokenSet getCommentTokens() {
        return PascalLexer.COMMENTS;
    }

    @NotNull
    @Override
    public TokenSet getStringLiteralElements() {
        return LITERALS;
    }

    @NotNull
    @Override
    public PsiElement createElement(ASTNode astNode) {
        return PasTypes.Factory.createElement(astNode);
    }

    @Override
    public PsiFile createFile(FileViewProvider fileViewProvider) {
        return new PascalFileImpl(fileViewProvider);
    }

    @Override
    public SpaceRequirements spaceExistanceTypeBetweenTokens(ASTNode astNode, ASTNode astNode1) {
        return SpaceRequirements.MAY;
    }
}
