package com.siberika.idea.pascal.lang.lexer;

import com.intellij.psi.TokenType;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.tree.TokenSet;

/**
 * Interface that contains all tokens returned by PascalLexer
 *
 * @author sylvanaar
 */
public interface PascalTokenTypes {
    //IFileElementType FILE = new IFileElementType(Language.findInstance(PascalLanguage.class));
    /**
     * Wrong token. Use for debugger needs
     */
    IElementType WRONG = TokenType.BAD_CHARACTER;


    /* **************************************************************************************************
   *  Whitespaces & NewLines
   * ****************************************************************************************************/

    IElementType NL_BEFORE_LONGSTRING = new PascalElementType("newline after longstring stert bracket");
    IElementType WS = TokenType.WHITE_SPACE;
    IElementType NEWLINE = new PascalElementType("new line");

    TokenSet WHITE_SPACES_SET = TokenSet.create(WS, NEWLINE, TokenType.WHITE_SPACE, NL_BEFORE_LONGSTRING);

    /* **************************************************************************************************
   *  Comments
   * ****************************************************************************************************/

    IElementType SHEBANG = new PascalElementType("shebang - should ignore");

    IElementType LONGCOMMENT = new PascalElementType("long comment");
    IElementType SHORTCOMMENT = new PascalElementType("short comment");

    IElementType LONGCOMMENT_BEGIN = new PascalElementType("long comment start bracket");
    IElementType LONGCOMMENT_END = new PascalElementType("long comment end bracket");

    TokenSet COMMENT_SET = TokenSet.create(SHORTCOMMENT, LONGCOMMENT,  SHEBANG, LONGCOMMENT_BEGIN,
            LONGCOMMENT_END);

    /* **************************************************************************************************
   *  Identifiers
   * ****************************************************************************************************/

    IElementType NAME = new PascalElementType("identifier");

    /* **************************************************************************************************
   *  Integers & floats
   * ****************************************************************************************************/

    IElementType NUMBER = new PascalElementType("number");

    /* **************************************************************************************************
   *  Strings & regular expressions
   * ****************************************************************************************************/

    IElementType STRING = new PascalElementType("string");

    IElementType UNTERMINATED_STRING = new PascalElementType("unterminated string");


    /* **************************************************************************************************
   *  Common tokens: operators, braces etc.
   * ****************************************************************************************************/


    IElementType DIV = new PascalElementType("/");
    IElementType MULT = new PascalElementType("*");
    IElementType LPAREN = new PascalElementType("(");
    IElementType RPAREN = new PascalElementType(")");
    IElementType LBRACK = new PascalElementType("[");
    IElementType RBRACK = new PascalElementType("]");
    IElementType COLON = new PascalElementType(":");
    IElementType COMMA = new PascalElementType(",");
    IElementType DOT = new PascalElementType(".");
    IElementType ASSIGN = new PascalElementType(":=");
    IElementType SEMI = new PascalElementType(";");
    IElementType EQ = new PascalElementType("=");
    IElementType NE = new PascalElementType("<>");
    IElementType PLUS = new PascalElementType("+");
    IElementType MINUS = new PascalElementType("-");
    IElementType GE = new PascalElementType(">=");
    IElementType GT = new PascalElementType(">");
    IElementType DEREF = new PascalElementType("^");
    IElementType LE = new PascalElementType("<=");
    IElementType LT = new PascalElementType("<");
    IElementType MOD = new PascalElementType("MOD");

    /* **************************************************************************************************
   *  Keywords
   * ****************************************************************************************************/


    IElementType BEGIN = new PascalElementType("begin");
    IElementType IF = new PascalElementType("if");
    IElementType ELSE = new PascalElementType("else");
    IElementType ELSEIF = new PascalElementType("elseif");
    IElementType WHILE = new PascalElementType("while");
    IElementType WITH = new PascalElementType("with");

    IElementType VAR = new PascalElementType("var");
    IElementType CONST = new PascalElementType("const");
    IElementType TYPE = new PascalElementType("type");
    IElementType ARRAY = new PascalElementType("array");
    IElementType RECORD = new PascalElementType("record");

    IElementType THEN = new PascalElementType("then");
    IElementType FOR = new PascalElementType("for");
    IElementType IN = new PascalElementType("in");
    IElementType RETURN = new PascalElementType("return");
    IElementType BREAK = new PascalElementType("break");

    IElementType CONTINUE = new PascalElementType("continue");
    IElementType TRUE = new PascalElementType("true");
    IElementType FALSE = new PascalElementType("false");
    IElementType NIL = new PascalElementType("nil");
    IElementType FUNCTION = new PascalElementType("function");
    IElementType PROCEDURE = new PascalElementType("procedure");

    IElementType OF = new PascalElementType("of");
    IElementType DO = new PascalElementType("do");
    IElementType NOT = new PascalElementType("not");
    IElementType AND = new PascalElementType("and");
    IElementType OR = new PascalElementType("or");

    IElementType REPEAT = new PascalElementType("repeat");
    IElementType UNTIL = new PascalElementType("until");
    IElementType END = new PascalElementType("end");


    IElementType UNIT = new PascalElementType("unit");
    IElementType LIBRARY = new PascalElementType("library");
    IElementType PROGRAM = new PascalElementType("program");
    IElementType USES = new PascalElementType("uses");



    TokenSet STRING_LITERAL_SET = TokenSet.create(STRING);

    TokenSet KEYWORDS = TokenSet.create(BEGIN, DO, PROCEDURE, FUNCTION, NOT, AND, OR,
            WITH, IF, THEN, ELSEIF, THEN, ELSE,
            WHILE, FOR, IN, RETURN, BREAK,
            CONTINUE,
            REPEAT, UNTIL, END,
            UNIT, LIBRARY, PROGRAM, USES,
            TYPE, RECORD, ARRAY, VAR, CONST);

    TokenSet PARENS = TokenSet.create(LPAREN, RPAREN);
    TokenSet BRACKS = TokenSet.create(LBRACK, RBRACK);

    TokenSet BAD_INPUT = TokenSet.create(WRONG, UNTERMINATED_STRING);
    
    TokenSet DEFINED_CONSTANTS = TokenSet.create(NIL, TRUE, FALSE);

    TokenSet UNARY_OP_SET = TokenSet.create(MINUS);

    TokenSet BINARY_OP_SET = TokenSet.create(MINUS, PLUS, DIV, MULT, MOD);

    TokenSet COMPARE_OPS = TokenSet.create(EQ, GE, GT, LT, LE, NE);
    TokenSet LOGICAL_OPS = TokenSet.create(AND, OR, NOT);
    TokenSet ARITHMETIC_OPS = TokenSet.create(MINUS, PLUS, DIV, MOD);

    TokenSet TABLE_ACCESS = TokenSet.create(DOT, LBRACK, DEREF);

    TokenSet LITERALS_SET = TokenSet.create(NUMBER, NIL, TRUE, FALSE, STRING);

    TokenSet IDENTIFIERS_SET = TokenSet.create(NAME);

    TokenSet WHITE_SPACES_OR_COMMENTS = TokenSet.orSet(WHITE_SPACES_SET, COMMENT_SET);

    TokenSet OPERATORS_SET = TokenSet.orSet(BINARY_OP_SET, UNARY_OP_SET, COMPARE_OPS, TokenSet.create(ASSIGN));
}
