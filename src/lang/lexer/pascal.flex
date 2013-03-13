package com.siberika.idea.pascal.lang.lexer;

import com.intellij.lexer.FlexLexer;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.TokenType;
import com.siberika.idea.pascal.lang.psi.PasTypes;

%%
/*-*
 * LEXICAL FUNCTIONS:
 */

%unicode
%ignorecase
%class _PascalLexer
%implements FlexLexer, PasTypes

%function advance
%type IElementType

%eof{ return;
%eof}

WHITESPACE      = [\ \n\r\t\f]
NEWLINE         = \r\n|\n|\r

LINE_COMMENT    = "/""/"[^\r\n]*
BLOCK_COMMENT   = "(*" !([^]* "*)" [^]*) ("*)")?
BRACE_COMMENT   = "{" !([^]* "}" [^]*) ("}")?
COMMENT         = {LINE_COMMENT}|{BLOCK_COMMENT}|{BRACE_COMMENT}

IDENTIFIER=[:jletter:] [:jletterdigit:]{0,126}
//identifier      = [_a-zA-Z][_a-zA-Z0-9]{0,126}

STRING_ELEMENT  = "''"|[^'\n\r]
STRING_LITERAL  = "'"{STRING_ELEMENT}*"'"

N               = [0-9]+
NUM_INT         = {N}
EXP             = [Ee]["+""-"]?{N}
NUM_REAL        = (({N}?[.]{N}){EXP}?|{N}[.][^.])
NUM_HEX         = \$[0-9a-fA-F]+
NUM_BIN         = {N}[bB]


%x XSTRING

%%

"and"               { return AND; }
"mod"               { return MOD; }
"or"                { return OR; }
"div"               { return IDIV; }
"shr"               { return SHR; }
"shl"               { return SHL; }
"in"                { return IN; }

"as"                { return AS; }
"is"                { return IS; }
"nil"               { return NIL; }

"class"             { return CLASS; }
"dispinterface"     { return DISPINTERFACE; }
"inline"            { return INLINE; }

"try"               { return TRY; }
"raise"             { return RAISE; }
"except"            { return EXCEPT; }
"on"                { return ON; }
"finally"           { return FINALLY; }

"library"           { return LIBRARY; }
"uses"              { return USES; }
"exports"           { return EXPORTS; }
"finalization"      { return FINALIZATION; }
"initialization"    { return INITIALIZATION; }

"threadvar"         { return THREADVAR; }
"absolute"          { return ABSOLUTE; }
"resourcestring"    { return RESOURCESTRING; }
"packed"            { return PACKED; }
"property"          { return PROPERTY; }
"array"             { return ARRAY; }
"set"               { return SET; }
"file"              { return FILE; }

"asm"               { return ASM; }
"goto"              { return GOTO; }
"label"             { return LABEL; }
"with"              { return WITH; }

"constructor"       { return CONSTRUCTOR; }
"destructor"        { return DESTRUCTOR; }
"inherited"         { return INHERITED; }
"object"            { return OBJECT; }

    "package"           { return PACKAGE; }
    "contains"          { return CONTAINS; }
    "requires"          { return REQUIRES; }

    "out"               { return OUT; }
    "exit"              { return EXIT; }
    "break"             { return BREAK; }
    "continue"          { return CONTINUE; }

    "operator"          { return OPERATOR; }
    "self"              { return SELF; }
    "new"               { return NEW; }

    "reintroduce"       { return REINTRODUCE; }
    "overload"          { return OVERLOAD; }
    "message"           { return MESSAGE; }
    "static"            { return STATIC; }
    "dynamic"           { return DYNAMIC; }
    "override"          { return OVERRIDE; }
    "virtual"           { return VIRTUAL; }
    "abstract"          { return ABSTRACT; }
    "sealed"            { return SEALED; }
    "final"             { return FINAL; }
    "assembler"         { return ASSEMBLER; }

    "cdecl"             { return CDECL; }
    "pascal"            { return PASCAL; }
    "register"          { return REGISTER; }
    "safecall"          { return SAFECALL; }
    "stdcall"           { return STDCALL; }
    "export"            { return EXPORT; }

    "STRICT"            { return STRICT; }
    "PRIVATE"           { return PRIVATE; }
    "PROTECTED"         { return PROTECTED; }
    "PUBLIC"            { return PUBLIC; }
    "PUBLISHED"         { return PUBLISHED; }
    "AUTOMATED"         { return AUTOMATED; }

    "dispid"            { return DISPID; }
    "external"          { return EXTERNAL; }
    "forward"           { return FORWARD; }
    "helper"            { return HELPER; }
    "default"           { return DEFAULT; }
    "implements"        { return IMPLEMENTS; }
    "index"             { return INDEX; }
    "read"              { return READ; }
    "write"             { return WRITE; }

    "deprecated"        { return DEPRECATED; }
    "experimental"      { return EXPERIMENTAL; }
    "platform"          { return PLATFORM; }
    "reference"         { return REFERENCE; }

"not"               { return NOT; }
"xor"               { return XOR; }

"to"                { return TO; }
"downto"            { return DOWNTO; }
"repeat"            { return REPEAT; }


"false"             { return FALSE; }
"true"              { return TRUE; }

"program"           { return PROGRAM; }
"unit"              { return UNIT; }
"interface"         { return INTERFACE; }
"implementation"    { return IMPLEMENTATION; }

"var"               { return VAR; }
"const"             { return CONST; }
"type"              { return TYPE; }
"array"             { return ARRAY; }
"record"            { return RECORD; }
"procedure"         { return PROCEDURE; }
"function"          { return FUNCTION; }
"of"                { return OF; }

"case"              { return CASE; }
"begin"             { return BEGIN; }
"end"               { return END; }
"for"               { return FOR; }
"until"             { return UNTIL; }
"while"             { return WHILE; }
"do"                { return DO; }
"if"                { return IF; }
"then"              { return THEN; }
"else"              { return ELSE; }

".."            { return RANGE; }
"*"             { return MULT; }
"+"             { return PLUS; }
"-"             { return MINUS; }
"/"             { return DIV; }
";"             { return SEMI; }
","             { return COMMA; }
"("             { return LPAREN; }
")"             { return RPAREN; }
"["             { return LBRACK; }
"]"             { return RBRACK; }
"(."            { return LBRACK; }
".)"            { return RBRACK; }
"="             { return EQ; }
"<"             { return LT; }
">"             { return GT; }
">="            { return GE; }
"<="            { return LE; }
"<>"            { return NE; }
":"             { return COLON; }
":="            { return ASSIGN; }
"."             { return DOT; }
"^"             { return DEREF; }
"@"             { return AT; }
"$"             { return HEXNUM; }
"#"             { return CHARNUM; }
"&"             { return KEYWORDESCAPE; }

//"'"           { yybegin(XSTRING); return STRING; }
//<XSTRING>
//{
//  "''"        { return STRING_; } //===*** remove
//  "'"         { yybegin(YYINITIAL); return STRING_LITERAL; }
//  {NEWLINE}   { yybegin(YYINITIAL); return TokenType.BAD_CHARACTER;  }
//  .           { return STRING_; }
//}

{STRING_LITERAL} { return STRING_LITERAL;}

{NUM_INT}       { return NUMBER_INT; }
{NUM_REAL}      { return NUMBER_REAL; }
{NUM_HEX}       { return NUMBER_HEX; }
{NUM_BIN}       { return NUMBER_BIN; }

{COMMENT}       { return COMMENT; }

{IDENTIFIER}    { return NAME; }

{WHITESPACE}    {yybegin(YYINITIAL); return TokenType.WHITE_SPACE; }
.               {yybegin(YYINITIAL); return TokenType.BAD_CHARACTER; }