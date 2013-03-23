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
"or"                { return OR; }
"not"               { return NOT; }
"xor"               { return XOR; }
"div"               { return IDIV; }
"mod"               { return MOD; }
"shr"               { return SHR; }
"shl"               { return SHL; }
"in"                { return IN; }

"as"                { return AS; }
"is"                { return IS; }

"class"             { return CLASS; }
"dispinterface"     { return DISPINTERFACE; }

"program"           { return PROGRAM; }
"unit"              { return UNIT; }
"library"           { return LIBRARY; }
"package"           { return PACKAGE; }

"uses"              { return USES; }
"interface"         { return INTERFACE; }
"implementation"    { return IMPLEMENTATION; }
"exports"           { return EXPORTS; }
"initialization"    { return INITIALIZATION; }
"finalization"      { return FINALIZATION; }
"contains"          { return CONTAINS; }
"requires"          { return REQUIRES; }

"try"               { return TRY; }
"raise"             { return RAISE; }
"except"            { return EXCEPT; }
"on"                { return ON; }
"finally"           { return FINALLY; }

"var"               { return VAR; }
"const"             { return CONST; }
"type"              { return TYPE; }
"threadvar"         { return THREADVAR; }
"resourcestring"    { return RESOURCESTRING; }

"procedure"         { return PROCEDURE; }
"function"          { return FUNCTION; }
"array"             { return ARRAY; }
"record"            { return RECORD; }
"set"               { return SET; }
"file"              { return FILE; }
"object"            { return OBJECT; }

"of"                { return OF; }
"absolute"          { return ABSOLUTE; }
"packed"            { return PACKED; }
"operator"          { return OPERATOR; }

"constructor"       { return CONSTRUCTOR; }
"destructor"        { return DESTRUCTOR; }
"property"          { return PROPERTY; }

"label"             { return LABEL; }
"goto"              { return GOTO; }
"exit"              { return EXIT; }
"break"             { return BREAK; }
"continue"          { return CONTINUE; }

"strict"            { return STRICT; }
"private"           { return PRIVATE; }
"protected"         { return PROTECTED; }
"public"            { return PUBLIC; }
"published"         { return PUBLISHED; }
"automated"         { return AUTOMATED; }

"virtual"           { return VIRTUAL; }
"dynamic"           { return DYNAMIC; }
"abstract"          { return ABSTRACT; }
"overload"          { return OVERLOAD; }
"override"          { return OVERRIDE; }
"reintroduce"       { return REINTRODUCE; }

"message"           { return MESSAGE; }
"static"            { return STATIC; }
"sealed"            { return SEALED; }
"final"             { return FINAL; }
"assembler"         { return ASSEMBLER; }

"cdecl"             { return CDECL; }
"pascal"            { return PASCAL; }
"register"          { return REGISTER; }
"safecall"          { return SAFECALL; }
"stdcall"           { return STDCALL; }
"export"            { return EXPORT; }
"inline"            { return INLINE; }

"dispid"            { return DISPID; }
"external"          { return EXTERNAL; }
"forward"           { return FORWARD; }
"helper"            { return HELPER; }
"implements"        { return IMPLEMENTS; }

"default"           { return DEFAULT; }
"index"             { return INDEX; }
"read"              { return READ; }
"write"             { return WRITE; }

"deprecated"        { return DEPRECATED; }
"experimental"      { return EXPERIMENTAL; }
"platform"          { return PLATFORM; }
"reference"         { return REFERENCE; }

"for"               { return FOR; }
"to"                { return TO; }
"downto"            { return DOWNTO; }
"repeat"            { return REPEAT; }
"until"             { return UNTIL; }
"while"             { return WHILE; }
"do"                { return DO; }
"with"              { return WITH; }

"begin"             { return BEGIN; }
"end"               { return END; }
"if"                { return IF; }
"then"              { return THEN; }
"else"              { return ELSE; }
"case"              { return CASE; }

"nil"               { return NIL; }
"false"             { return FALSE; }
"true"              { return TRUE; }

"asm"               { return ASM; }
"inherited"         { return INHERITED; }
"out"               { return OUT; }
"self"              { return SELF; }
"new"               { return NEW; }

":="            { return ASSIGN; }
".."            { return RANGE; }

"*"             { return MULT; }
"/"             { return DIV; }
"+"             { return PLUS; }
"-"             { return MINUS; }

"="             { return EQ; }
">"             { return GT; }
"<"             { return LT; }
">="            { return GE; }
"<="            { return LE; }
"<>"            { return NE; }

":"             { return COLON; }
","             { return COMMA; }
"."             { return DOT; }
"^"             { return DEREF; }
"@"             { return AT; }
"$"             { return HEXNUM; }
"#"             { return CHARNUM; }
"&"             { return KEYWORDESCAPE; }

";"             { return SEMI; }

"("             { return LPAREN; }
")"             { return RPAREN; }
"["             { return LBRACK; }
"]"             { return RBRACK; }
"(."            { return LBRACK; }
".)"            { return RBRACK; }

{STRING_LITERAL} { return STRING_LITERAL;}

{NUM_INT}       { return NUMBER_INT; }
{NUM_REAL}      { return NUMBER_REAL; }
{NUM_HEX}       { return NUMBER_HEX; }
{NUM_BIN}       { return NUMBER_BIN; }

{COMMENT}       { return COMMENT; }

{IDENTIFIER}    { return NAME; }

{WHITESPACE}    {yybegin(YYINITIAL); return TokenType.WHITE_SPACE; }
.               {yybegin(YYINITIAL); return TokenType.BAD_CHARACTER; }