package com.siberika.idea.pascal.lang.lexer;

import com.intellij.lexer.FlexLexer;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.TokenType;
import com.siberika.idea.pascal.lang.psi.PasTypes;
@SuppressWarnings("all")
%%
/*-*
 * LEXICAL FUNCTIONS:
 */

%unicode
%ignorecase
%class _PascalLexer
%implements FlexLexer, PasTypes, PascalFlexLexer
%abstract

%function advance
%type IElementType

%eof{ return;
%eof}

%{
  public abstract CharSequence getIncludeContent(CharSequence text);
%}


WHITESPACE      = [\ \n\r\t\f]+

INCLUDE_START   = "{$INCLUDE " | "{$I "
INCLUDE         = {INCLUDE_START} [^}]+ "}"

COMP_OPTION     = "{$" !([^]* "}" [^]*) ("}")?

LINE_COMMENT    = "/""/"[^\r\n]*
BLOCK_COMMENT   = "(*" !([^]* "*)" [^]*) ("*)")?
BRACE_COMMENT   = "{" !([^]* "}" [^]*) ("}")?
COMMENT         = {LINE_COMMENT}|{BLOCK_COMMENT}|{BRACE_COMMENT}

IDENTIFIER=[:jletter:] [:jletterdigit:]{0,126}
//identifier      = [_a-zA-Z][_a-zA-Z0-9]{0,126}

STRING_ELEMENT  = "''"|[^'\n\r]
STRING_LITERAL  = "'"{STRING_ELEMENT}*"'"

CT_DEFINE          = "{$DEFINE " !([^]* "}" [^]*) ("}")?
CT_UNDEFINE        = "{$UNDEF " !([^]* "}" [^]*) ("}")?

CT_IF           = "{$IF " !([^]* "}" [^]*) ("}")?
CT_IFDEF        = "{$IFDEF " !([^]* "}" [^]*) ("}")?
CT_IFNDEF       = "{$IFNDEF " !([^]* "}" [^]*) ("}")?
CT_IFOPT        = "{$IFOPT " !([^]* "}" [^]*) ("}")?
CT_ELSE         = "{$ELSE" !([^]* "}" [^]*) ("}")?
CT_ENDIF        = "{$ENDIF" !([^]* "}" [^]*) ("}")?
CT_IFEND        = "{$IFEND" !([^]* "}" [^]*) ("}")?

N               = [0-9]+
NUM_INT         = {N}
EXP             = [Ee][+-]?{N}
NUM_REAL        = {N}(\.{N})?{EXP}?
//\d+(\.\d+)?([eE][+-]?\d+)?'
//NUM_REAL        = (({N}?(\.{N})?{EXP}?)|{N}[.][^.])
NUM_HEX         = \$[0-9a-fA-F]+
NUM_BIN         = (\%[01]+) | ({N}[bB])
NUM_OCT         = \&[0-7]+

%state INACTIVE_BRANCH

%%
<INACTIVE_BRANCH> {
    {CT_IF}         { return handleIf(yytext()); }
    {CT_IFDEF}      { return handleIfDef(yytext()); }
    {CT_IFNDEF}     { return handleIfNDef(yytext()); }
    {CT_IFOPT}      { return handleIfOpt(yytext()); }
    {CT_ELSE}       { return handleElse(); }
    {CT_ENDIF}      { return handleEndIf(); }
    {CT_IFEND}      { return handleEndIf(); }
    {IDENTIFIER}    { return COMMENT; }
    {WHITESPACE}    { return TokenType.WHITE_SPACE; }
    .               { return COMMENT; }
}

<YYINITIAL> {
    "and"               { return getElement(AND); }
    "or"                { return getElement(OR); }
    "not"               { return getElement(NOT); }
    "xor"               { return getElement(XOR); }
    "div"               { return getElement(IDIV); }
    "mod"               { return getElement(MOD); }
    "shr"               { return getElement(SHR); }
    "shl"               { return getElement(SHL); }
    "in"                { return getElement(IN); }

    "as"                { return getElement(AS); }
    "is"                { return getElement(IS); }

    "class"             { return getElement(CLASS); }
    "dispinterface"     { return getElement(DISPINTERFACE); }

    "program"           { return getElement(PROGRAM); }
    "unit"              { return getElement(UNIT); }
    "library"           { return getElement(LIBRARY); }
    "package"           { return getElement(PACKAGE); }

    "uses"              { return getElement(USES); }
    "interface"         { return getElement(INTERFACE); }
    "implementation"    { return getElement(IMPLEMENTATION); }
    "exports"           { return getElement(EXPORTS); }
    "initialization"    { return getElement(INITIALIZATION); }
    "finalization"      { return getElement(FINALIZATION); }
    "contains"          { return getElement(CONTAINS); }
    "requires"          { return getElement(REQUIRES); }

    "try"               { return getElement(TRY); }
    "raise"             { return getElement(RAISE); }
    "except"            { return getElement(EXCEPT); }
    "on"                { return getElement(ON); }
    "finally"           { return getElement(FINALLY); }

    "var"               { return getElement(VAR); }
    "const"             { return getElement(CONST); }
    "type"              { return getElement(TYPE); }
    "threadvar"         { return getElement(THREADVAR); }
    "resourcestring"    { return getElement(RESOURCESTRING); }
    "constref"          { return getElement(CONSTREF); }

    "procedure"         { return getElement(PROCEDURE); }
    "function"          { return getElement(FUNCTION); }
    "array"             { return getElement(ARRAY); }
    "record"            { return getElement(RECORD); }
    "set"               { return getElement(SET); }
    "file"              { return getElement(FILE); }
    "object"            { return getElement(OBJECT); }

    "of"                { return getElement(OF); }
    "absolute"          { return getElement(ABSOLUTE); }
    "packed"            { return getElement(PACKED); }
    "operator"          { return getElement(OPERATOR); }

    "constructor"       { return getElement(CONSTRUCTOR); }
    "destructor"        { return getElement(DESTRUCTOR); }
    "property"          { return getElement(PROPERTY); }

    "label"             { return getElement(LABEL); }
    "goto"              { return getElement(GOTO); }
    "exit"              { return getElement(EXIT); }
    "break"             { return getElement(BREAK); }
    "continue"          { return getElement(CONTINUE); }

    "strict"            { return getElement(STRICT); }
    "private"           { return getElement(PRIVATE); }
    "protected"         { return getElement(PROTECTED); }
    "public"            { return getElement(PUBLIC); }
    "published"         { return getElement(PUBLISHED); }
    "automated"         { return getElement(AUTOMATED); }

    "virtual"           { return getElement(VIRTUAL); }
    "dynamic"           { return getElement(DYNAMIC); }
    "abstract"          { return getElement(ABSTRACT); }
    "overload"          { return getElement(OVERLOAD); }
    "override"          { return getElement(OVERRIDE); }
    "reintroduce"       { return getElement(REINTRODUCE); }

    "message"           { return getElement(MESSAGE); }
    "static"            { return getElement(STATIC); }
    "sealed"            { return getElement(SEALED); }
    "final"             { return getElement(FINAL); }
    "assembler"         { return getElement(ASSEMBLER); }

    "cdecl"             { return getElement(CDECL); }
    "pascal"            { return getElement(PASCAL); }
    "register"          { return getElement(REGISTER); }
    "safecall"          { return getElement(SAFECALL); }
    "stdcall"           { return getElement(STDCALL); }
    "export"            { return getElement(EXPORT); }
    "inline"            { return getElement(INLINE); }

    "dispid"            { return getElement(DISPID); }
    "external"          { return getElement(EXTERNAL); }
    "forward"           { return getElement(FORWARD); }
    "helper"            { return getElement(HELPER); }
    "implements"        { return getElement(IMPLEMENTS); }

    "default"           { return getElement(DEFAULT); }
    "index"             { return getElement(INDEX); }
    "read"              { return getElement(READ); }
    "write"             { return getElement(WRITE); }

    "deprecated"        { return getElement(DEPRECATED); }
    "experimental"      { return getElement(EXPERIMENTAL); }
    "platform"          { return getElement(PLATFORM); }
    "reference"         { return getElement(REFERENCE); }

    "for"               { return getElement(FOR); }
    "to"                { return getElement(TO); }
    "downto"            { return getElement(DOWNTO); }
    "repeat"            { return getElement(REPEAT); }
    "until"             { return getElement(UNTIL); }
    "while"             { return getElement(WHILE); }
    "do"                { return getElement(DO); }
    "with"              { return getElement(WITH); }

    "begin"             { return getElement(BEGIN); }
    "end"               { return getElement(END); }
    "if"                { return getElement(IF); }
    "then"              { return getElement(THEN); }
    "else"              { return getElement(ELSE); }
    "case"              { return getElement(CASE); }

    "nil"               { return getElement(NIL); }
    "false"             { return getElement(FALSE); }
    "true"              { return getElement(TRUE); }

    "asm"               { return getElement(ASM); }
    "inherited"         { return getElement(INHERITED); }
    "out"               { return getElement(OUT); }
    "self"              { return getElement(SELF); }
    "new"               { return getElement(NEW); }

    ":="            { return getElement(ASSIGN); }
    "+="            { return getElement(PLUS_ASSIGN); }
    "-="            { return getElement(MINUS_ASSIGN); }
    "*="            { return getElement(MULT_ASSIGN); }
    "/="            { return getElement(DIV_ASSIGN); }
    ".."            { return getElement(RANGE); }

    "*"             { return getElement(MULT); }
    "/"             { return getElement(DIV); }
    "+"             { return getElement(PLUS); }
    "-"             { return getElement(MINUS); }

    "="             { return getElement(EQ); }
    ">"             { return getElement(GT); }
    "<"             { return getElement(LT); }
    ">="            { return getElement(GE); }
    "<="            { return getElement(LE); }
    "<>"            { return getElement(NE); }

    ":"             { return getElement(COLON); }
    ","             { return getElement(COMMA); }
    "."             { return getElement(DOT); }
    "^"             { return getElement(DEREF); }
    "@"             { return getElement(AT); }
    "$"             { return getElement(HEXNUM); }
    "#"             { return getElement(CHARNUM); }
    "&"             { return getElement(KEYWORDESCAPE); }

    ";"             { return getElement(SEMI); }

    "("             { return getElement(LPAREN); }
    ")"             { return getElement(RPAREN); }
    "["             { return getElement(LBRACK); }
    "]"             { return getElement(RBRACK); }
    "(."            { return getElement(LBRACK); }
    ".)"            { return getElement(RBRACK); }

    {CT_DEFINE}     { define(yytext()); return CT_DEFINE; }
    {CT_UNDEFINE}   { unDefine(yytext()); return CT_UNDEFINE; }

    {CT_IF}         { return handleIf(yytext()); }
    {CT_IFDEF}      { return handleIfDef(yytext()); }
    {CT_IFNDEF}     { return handleIfNDef(yytext()); }
    {CT_IFOPT}      { return handleIfOpt(yytext()); }
    {CT_ELSE}       { return handleElse(); }
    {CT_ENDIF}      { return handleEndIf(); }
    {CT_IFEND}      { return handleEndIf(); }

    {INCLUDE}       { return handleInclude(yytext()); }

    {COMP_OPTION}   { return getElement(COMP_OPTION); }

    {STRING_LITERAL} { return getElement(STRING_LITERAL); }

    {NUM_INT}       { return getElement(NUMBER_INT); }
    {NUM_REAL}      { return getElement(NUMBER_REAL); }
    {NUM_HEX}       { return getElement(NUMBER_HEX); }
    {NUM_BIN}       { return getElement(NUMBER_BIN); }
    {NUM_OCT}       { return getElement(NUMBER_OCT); }

    {COMMENT}       { return getElement(COMMENT); }
    {IDENTIFIER}    { return getElement(NAME); }
    {WHITESPACE}    { return TokenType.WHITE_SPACE; }
    .               { return TokenType.BAD_CHARACTER; }
}


//<<EOF>>        { if (yymoreStreams()) { yypopStream(); } else { zzAtEOF = true; zzDoEOF(); return null; }}
