package com.siberika.idea.pascal.lang.lexer;

import com.intellij.lexer.FlexAdapter;
import com.intellij.lexer.MergingLexerAdapter;
import com.intellij.psi.tree.TokenSet;
import com.siberika.idea.pascal.lang.psi.PasTypes;

import java.io.Reader;

public class PascalLexer extends MergingLexerAdapter implements PasTypes {
    public static final TokenSet KEYWORDS = TokenSet.create(
            ARRAY, BEGIN, CASE, CONST,
            DO, ELSE, END,
            FALSE, FOR, FUNCTION,
            IF, IMPLEMENTATION, INTERFACE,
            OF, PROCEDURE, PROGRAM, RECORD,
            THEN, TRUE, TYPE, UNIT, UNTIL, VAR, WHILE
    );

    public static final TokenSet SYMBOLS = TokenSet.create(
            ASSIGN,
            GE, GT, LE, LT, EQ, NE, MINUS, PLUS, SEMI, COLON, COMMA, DOT, DEREF, DIV, MULT
    );

    public static final TokenSet OPERATORS = TokenSet.create(
            AND, OR, MOD, IDIV, IN, SHR, SHL
    );

    public static final TokenSet PARENS = TokenSet.create(LBRACK, LPAREN, RPAREN, RBRACK);

    public static final TokenSet COMMENTS = TokenSet.create(COMMENT);

    public static _PascalLexer getFlexLexer() {
        return new _PascalLexer((Reader) null);
    }

    public PascalLexer() {
        super(new FlexAdapter(new _PascalLexer((Reader) null)), TokenSet.create());
    }
}