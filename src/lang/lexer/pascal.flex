package com.siberika.idea.pascal.lang.lexer;

import com.intellij.lexer.FlexLexer;
import com.intellij.psi.tree.IElementType;
import java.util.*;
import java.lang.reflect.Field;
import org.jetbrains.annotations.NotNull;

%%
/*-*
 * LEXICAL FUNCTIONS:
 */

%unicode
%class _PascalLexer
%implements FlexLexer, PascalTokenTypes

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
n               = [0-9]+
exp             = [Ee][+-]?{n}
letter          = [A-Za-z]
digit           = [0-9]
alphanumeric    = {letter}|{digit}
other_id_char   = [_]
identifier      = [_a-zA-Z][_a-zA-Z0-9]*
integer         = n|\$[0-9a-fA-F]+
real            = (({n}|{n}[.]{n}){exp}?|[.]{n}|{n}[.])
char            = '.'
leftbrace       = \{
rightbrace      = \}
nonrightbrace   = [^}]
comment_body    = {nonrightbrace}*
comment         = {leftbrace}{comment_body}{rightbrace}
whitespace      = [ \n\t]


%%
/**
 * LEXICAL RULES:
 */
"and"           { return AND; }
"array"         { return ARRAY; }
"begin"         { return BEGIN; }
"const"         { return CONST; }
"do"            { return DO; }
"else"          { return ELSE; }
"end"           { return END; }
"false"         { return FALSE; }
"for"           { return FOR; }
"function"      { return FUNCTION; }
"if"            { return IF; }
"library"       { return LIBRARY; }
"mod"           { return MOD; }
"of"            { return OF; }
"or"            { return OR; }
"program"       { return PROGRAM; }
"procedure"     { return PROCEDURE; }
"record"        { return RECORD; }
"then"          { return THEN; }
"type"          { return TYPE; }
"var"           { return VAR; }
"true"          { return TRUE; }
"unit"          { return UNIT; }
"until"         { return UNTIL; }
"while"         { return WHILE; }

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

{identifier}    { return NAME; }
{integer}       { return NUMBER; }
{real}          { return NUMBER; }
{char}          { return STRING; }
{comment}       { return SHORTCOMMENT; }
{whitespace}    { /* Ignore whitespace. */ }
.               { return WRONG; }