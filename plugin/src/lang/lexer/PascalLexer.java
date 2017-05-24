package com.siberika.idea.pascal.lang.lexer;

import com.intellij.lexer.DelegateLexer;
import com.intellij.lexer.FlexAdapter;
import com.intellij.lexer.FlexLexer;
import com.intellij.lexer.Lexer;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.tree.TokenSet;
import com.siberika.idea.pascal.lang.psi.PasTypes;
import org.jetbrains.annotations.NotNull;

public abstract class PascalLexer extends DelegateLexer implements PasTypes {
    public static final TokenSet KEYWORDS = TokenSet.create(
            BEGIN, END,
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
            NUMBER_INT, NUMBER_REAL, NUMBER_HEX, NUMBER_BIN, NUMBER_OCT
    );

    public static final TokenSet SYMBOLS = TokenSet.create(
            ASSIGN, RANGE,
            MULT, DIV, PLUS, MINUS,
            EQ, GT, LT, NE,
            COLON, COMMA, DOT, DEREF, AT,
            HEXNUM, CHARNUM
    );

    public static final TokenSet OPERATORS = TokenSet.create(
            AND, OR, NOT, XOR, IDIV, MOD, SHR, SHL, IN, AS, IS
    );

    public static final TokenSet STATEMENTS = TokenSet.create(
            FOR, WHILE, REPEAT,
            IF, CASE, WITH,
            GOTO, EXIT,
            TRY, RAISE,
            END
    );

    public static final TokenSet STATEMENTS_IN_CYCLE = TokenSet.create(BREAK, CONTINUE);

    public static final TokenSet VALUES = TokenSet.create(NIL, FALSE, TRUE);

    public static final TokenSet MODULE_HEADERS = TokenSet.create(PROGRAM, UNIT, LIBRARY, PACKAGE);

    public static final TokenSet TOP_LEVEL_DECLARATIONS = TokenSet.create(CONTAINS, REQUIRES);

    public static final TokenSet UNIT_SECTIONS = TokenSet.create(
            INTERFACE, IMPLEMENTATION,
            INITIALIZATION, FINALIZATION
    );

    public static final TokenSet VAR_KEY = TokenSet.create(
            VAR, THREADVAR
    );

    public static final TokenSet CONST_KEY = TokenSet.create(
            CONST, RESOURCESTRING
    );

    public static final TokenSet DECL_SECTION_KEY = TokenSet.create(
            VAR, CONST, TYPE, THREADVAR, RESOURCESTRING
    );

    public static final TokenSet DECLARATIONS = TokenSet.create(
            VAR, CONST, TYPE, THREADVAR, RESOURCESTRING,
            PROCEDURE, FUNCTION, CONSTRUCTOR, DESTRUCTOR
    );

    public static final TokenSet STRUCT_DECLARATIONS = TokenSet.create(
            PROCEDURE, FUNCTION, CONSTRUCTOR, DESTRUCTOR,
            CLASS, OPERATOR, PROPERTY, END
    );

    public static final TokenSet DIRECTIVE_ROUTINE = TokenSet.create(
            OVERLOAD, INLINE, ASSEMBLER,
            CDECL, PASCAL, REGISTER, SAFECALL, STDCALL, EXPORT,
            DEPRECATED, EXPERIMENTAL, PLATFORM, LIBRARY
    );

    public static final TokenSet DIRECTIVE_METHOD = TokenSet.create(
            REINTRODUCE, OVERLOAD, MESSAGE, STATIC, DYNAMIC, OVERRIDE, VIRTUAL,
            CDECL, PASCAL, REGISTER, SAFECALL, STDCALL, EXPORT,
            ABSTRACT, FINAL, INLINE, ASSEMBLER,
            DEPRECATED, EXPERIMENTAL, PLATFORM, LIBRARY, DISPID
    );

    public static final TokenSet VISIBILITY = TokenSet.create(STRICT, PRIVATE, PROTECTED, PUBLIC, PUBLISHED, AUTOMATED);

    /*EXPORTS,
    EXCEPT, ON, FINALLY,
    OF, ABSOLUTE, OPERATOR,
    CONSTRUCTOR, DESTRUCTOR, PROPERTY,
    LABEL,
    STRICT, PRIVATE, PROTECTED, PUBLIC, PUBLISHED, AUTOMATED,

    MESSAGE, STATIC, SEALED, FINAL,

    DISPID, EXTERNAL, FORWARD, HELPER, IMPLEMENTS,
            OUT, DEFAULT, INDEX, READ, WRITE,

    TO, DOWNTO, UNTIL, DO,
    THEN, ELSE,
    ASM, INHERITED, SELF, NEW*/

    public static final TokenSet PARENS = TokenSet.create(LBRACK, LPAREN, RPAREN, RBRACK);

    public static final TokenSet COMPILER_DIRECTIVES = TokenSet.create(PasTypes.INCLUDE, PasTypes.CT_DEFINE, PasTypes.CT_ELSE, PasTypes.CT_ENDIF, PasTypes.CT_IF,
            PasTypes.CT_IFDEF, PasTypes.CT_IFNDEF, PasTypes.CT_IFOPT, PasTypes.CT_UNDEFINE, PasTypes.COMP_OPTION);

    public static final TokenSet COMMENTS = TokenSet.orSet(TokenSet.create(PasTypes.COMMENT), COMPILER_DIRECTIVES);

    protected final PascalFlexLexerImpl pascalFlexLexer;

    public PascalLexer(@NotNull Lexer delegate) {
        super(delegate);
        pascalFlexLexer = (PascalFlexLexerImpl) getFlexLexer();
    }

    public FlexLexer getFlexLexer() {
        return myDelegate instanceof FlexAdapter ? ((FlexAdapter) myDelegate).getFlex() : null;
    }

    public static class ParsingPascalLexer extends PascalLexer {
        public ParsingPascalLexer(Project project, VirtualFile virtualFile) {
            super(new FlexAdapter(new PascalFlexLexerImpl(null, project, virtualFile, false)));
        }
    }

    public static class SyntaxHighlightingPascalLexer extends PascalLexer {

        public SyntaxHighlightingPascalLexer(Project project, VirtualFile virtualFile) {
            super(new FlexAdapter(new PascalFlexLexerImpl(null, project, virtualFile, true)));
        }

        @Override
        public int getState() {
            return pascalFlexLexer.getStateWithConditionals();
        }
    }
}