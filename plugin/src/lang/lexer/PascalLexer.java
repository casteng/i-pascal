package com.siberika.idea.pascal.lang.lexer;

import com.intellij.lexer.FlexAdapter;
import com.intellij.lexer.MergingLexerAdapter;
import com.intellij.psi.tree.TokenSet;
import com.siberika.idea.pascal.lang.psi.PasTypes;

import java.io.Reader;

public class PascalLexer extends MergingLexerAdapter implements PasTypes {
    public static final TokenSet KEYWORDS = TokenSet.create(
            CLASS, DISPINTERFACE,
            PROGRAM, UNIT, LIBRARY, PACKAGE,
            USES, INTERFACE, IMPLEMENTATION,
            EXPORTS, INITIALIZATION, FINALIZATION,
            CONTAINS, REQUIRES,
            TRY, RAISE, EXCEPT, ON, FINALLY,
            VAR, CONST, TYPE, THREADVAR, RESOURCESTRING,
            PROCEDURE, FUNCTION, ARRAY, RECORD, SET, FILE, OBJECT,
            OF, ABSOLUTE, PACKED, OPERATOR,
            CONSTRUCTOR, DESTRUCTOR, PROPERTY,
            LABEL, GOTO, EXIT, BREAK, CONTINUE,
            STRICT, PRIVATE, PROTECTED, PUBLIC, PUBLISHED, AUTOMATED,
            VIRTUAL, DYNAMIC, ABSTRACT, OVERLOAD, OVERRIDE, REINTRODUCE,
            MESSAGE, STATIC, SEALED, FINAL, ASSEMBLER,
            CDECL, PASCAL, REGISTER, SAFECALL, STDCALL, EXPORT, INLINE,
            DISPID, EXTERNAL, FORWARD, HELPER, IMPLEMENTS,
            DEFAULT, INDEX, READ, WRITE,
            DEPRECATED, EXPERIMENTAL, PLATFORM, REFERENCE,
            FOR, TO, DOWNTO, REPEAT, UNTIL, WHILE, DO,
            IF, THEN, ELSE, CASE, WITH,
            NIL, FALSE, TRUE,
            ASM, INHERITED, OUT, SELF, NEW
    );

    public static final TokenSet NUMBERS = TokenSet.create(
            NUMBER_INT, NUMBER_REAL, NUMBER_HEX, NUMBER_BIN
    );

    public static final TokenSet SYMBOLS = TokenSet.create(
            ASSIGN, RANGE,
            MULT, DIV, PLUS, MINUS,
            EQ, GT, LT, GE, LE, NE,
            COLON, COMMA, DOT, DEREF, AT,
            HEXNUM, CHARNUM, KEYWORDESCAPE
    );

    public static final TokenSet OPERATORS = TokenSet.create(
            AND, OR, NOT, XOR, IDIV, MOD, SHR, SHL, IN, AS, IS
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