package com.siberika.idea.pascal.lang.lexer;

import com.intellij.lexer.FlexLexer;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.TokenType;
import java.util.*;
import java.lang.reflect.Field;
import org.jetbrains.annotations.NotNull;
import com.siberika.idea.pascal.lang.psi.PasTypes;

%%
/*-*
 * LEXICAL FUNCTIONS:
 */

%unicode
%class _PascalLexer
%implements FlexLexer, PasTypes

%function advance
%type IElementType

%eof{ return;
%eof}

%{
    ExtendedSyntaxStrCommentHandler longCommentOrStringHandler = new ExtendedSyntaxStrCommentHandler();
%}

%init{
%init}

/*-*
 * PATTERN DEFINITIONS:
 */
line_comment    = "//" .*
block_comment   = "(*" !([^]* "*)" [^]*) ("*)")?
brace_comment   = "{" !([^]* "}" [^]*) ("}")?
strElement      = "''"|.
//char            = "'"{strElement}*?"'"
n               = [0-9]+
exp             = [Ee][+-]?{n}
letter          = [A-Za-z]
digit           = [0-9]
alphanumeric    = {letter}|{digit}
other_id_char   = [_]
identifier      = [_a-zA-Z][_a-zA-Z0-9]{0,126}
integer         = n|\$[0-9a-fA-F]+
real            = (({n}|{n}[.]{n}){exp}?|[.]{n}|{n}[.])
comment         = {line_comment}|{block_comment}|{brace_comment}
whitespace      = [ \t\n]

newline         =   \r\n|\n|\r

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
"out"               { return OUT; }
"resourcestring"    { return RESOURCESTRING; }
"inline"            { return INLINE; }
"packed"            { return PACKED; }
"property"          { return PROPERTY; }
"array"             { return ARRAY; }
"set"               { return SET; }
"file"              { return FILE; }
"string"            { return STRING; }

"asm"               { return ASM; }
"goto"              { return GOTO; }
"label"             { return LABEL; }
"with"              { return WITH; }

"constructor"       { return CONSTRUCTOR; }
"destructor"        { return DESTRUCTOR; }
"inherited"         { return INHERITED; }
"object"            { return OBJECT; }
"operator"          { return OPERATOR; }
"reintroduce"       { return REINTRODUCE; }
"self"              { return SELF; }

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
"@"             { return ADDR; }
"$"             { return HEXNUM; }
"#"             { return CHARNUM; }
"&"             { return KEYWORDESCAPE; }

"'"           { yybegin(XSTRING); return STRING; }
<XSTRING>
{
  "''"        { return STRING; }
  "'"         { yybegin(YYINITIAL); return STRING; }
  {newline}   { yybegin(YYINITIAL); return TokenType.BAD_CHARACTER;  }
  .           { return STRING; }
}

//{char}          { return STRING; }
{integer}       { return NUMBER; }
{real}          { return NUMBER; }
{comment}       { return COMMENT; }
{identifier}    { return NAME; }
{whitespace}    {yybegin(YYINITIAL); return TokenType.WHITE_SPACE; }
.               {yybegin(YYINITIAL); return TokenType.BAD_CHARACTER; }