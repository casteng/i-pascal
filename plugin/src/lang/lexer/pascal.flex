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
%}


WHITESPACE      = [\ \n\r\t\f]+

INCLUDE_START   = "{$INCLUDE " | "{$I "
INCLUDE         = {INCLUDE_START} [^}]+ "}"

COMP_OPTION     = "{$" !([^]* "}" [^]*) ("}")?

LINE_COMMENT    = "/""/"[^\r\n]*
BLOCK_COMMENT   = "(*" !([^]* "*)" [^]*) ("*)")?
BRACE_COMMENT   = "{" !([^]* "}" [^]*) ("}")?
COMMENT         = {LINE_COMMENT}|{BLOCK_COMMENT}|{BRACE_COMMENT}

INACTIVE_CODE   = [^{\r\n]*

IDENTIFIER=[:jletter:] [:jletterdigit:]{0,126}
IDENTIFIER_="&"{IDENTIFIER}
//identifier      = [_a-zA-Z][_a-zA-Z0-9]{0,126}

STRING_ELEMENT  = "''"|[^'\n\r]
STRING_LITERAL  = "'"{STRING_ELEMENT}*"'"
STRING_LITERAL_UNC  = "'"{STRING_ELEMENT}*[\n\r]

CT_DEFINE          = "{$DEFINE " !([^]* "}" [^]*) ("}")?
CT_UNDEFINE        = "{$UNDEF " !([^]* "}" [^]*) ("}")?

CT_IF           = "{$IF " !([^]* "}" [^]*) ("}")?
CT_IFDEF        = "{$IFDEF " !([^]* "}" [^]*) ("}")?
CT_IFNDEF       = "{$IFNDEF " !([^]* "}" [^]*) ("}")?
CT_IFOPT        = "{$IFOPT " !([^]* "}" [^]*) ("}")?
CT_ELSEIF       = "{$ELSEIF " !([^]* "}" [^]*) ("}")?
CT_ELSE         = "{$ELSE}" | ("{$ELSE " !([^]* "}" [^]*) ("}")?)
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
    {CT_IF}         { return handleIf(zzCurrentPos, yytext()); }
    {CT_IFDEF}      { return handleIfDef(zzCurrentPos, yytext()); }
    {CT_IFNDEF}     { return handleIfNDef(zzCurrentPos, yytext()); }
    {CT_IFOPT}      { return handleIfOpt(zzCurrentPos, yytext()); }
    {CT_ELSE}       { return handleElse(zzCurrentPos); }
    {CT_ELSEIF}     { return handleElseIf(zzCurrentPos, yytext()); }
    {CT_ENDIF}      { return handleEndIf(zzCurrentPos); }
    {CT_IFEND}      { return handleEndIf(zzCurrentPos); }
    {INACTIVE_CODE} { return COMMENT; }
    {WHITESPACE}    { return TokenType.WHITE_SPACE; }
    .               { return COMMENT; }
}

<YYINITIAL> {
    "and"               { return getElement(AND); }
    "&and"               { return getElement(AND_); }
    "or"                { return getElement(OR); }
    "&or"                { return getElement(OR_); }
    "not"               { return getElement(NOT); }
    "&not"               { return getElement(NOT_); }
    "xor"               { return getElement(XOR); }
    "&xor"               { return getElement(XOR_); }
    "div"               { return getElement(IDIV); }
    "&div"               { return getElement(IDIV_); }
    "mod"               { return getElement(MOD); }
    "&mod"               { return getElement(MOD_); }
    "shr"               { return getElement(SHR); }
    "&shr"               { return getElement(SHR_); }
    "shl"               { return getElement(SHL); }
    "&shl"               { return getElement(SHL_); }
    "<<"                { return getElement(SHL); }
    "in"                { return getElement(IN); }
    "&in"                { return getElement(IN_); }

    "as"                { return getElement(AS); }
    "&as"                { return getElement(AS_); }
    "is"                { return getElement(IS); }
    "&is"                { return getElement(IS_); }

    "class"             { return getElement(CLASS); }
    "&class"             { return getElement(CLASS_); }
    "dispinterface"     { return getElement(DISPINTERFACE); }
    "&dispinterface"     { return getElement(DISPINTERFACE_); }
    "objcclass"        { return getElement(OBJC_CLASS); }
    "objcprotocol"        { return getElement(OBJC_PROTOCOL); }
    "objccategory"        { return getElement(OBJC_CATEGORY); }

    "program"           { return getElement(PROGRAM); }
    "&program"           { return getElement(PROGRAM_); }
    "unit"              { return getElement(UNIT); }
    "&unit"              { return getElement(UNIT_); }
    "library"           { return getElement(LIBRARY); }
    "&library"           { return getElement(LIBRARY_); }
    "package"           { return getElement(PACKAGE); }
    "&package"           { return getElement(PACKAGE_); }

    "uses"              { return getElement(USES); }
    "&uses"              { return getElement(USES_); }
    "interface"         { return getElement(INTERFACE); }
    "&interface"         { return getElement(INTERFACE_); }
    "implementation"    { return getElement(IMPLEMENTATION); }
    "&implementation"    { return getElement(IMPLEMENTATION_); }
    "exports"           { return getElement(EXPORTS); }
    "&exports"           { return getElement(EXPORTS_); }
    "initialization"    { return getElement(INITIALIZATION); }
    "&initialization"    { return getElement(INITIALIZATION_); }
    "finalization"      { return getElement(FINALIZATION); }
    "&finalization"      { return getElement(FINALIZATION_); }
    "contains"          { return getElement(CONTAINS); }
    "&contains"          { return getElement(CONTAINS_); }
    "requires"          { return getElement(REQUIRES); }
    "&requires"          { return getElement(REQUIRES_); }

    "try"               { return getElement(TRY); }
    "&try"               { return getElement(TRY_); }
    "raise"             { return getElement(RAISE); }
    "&raise"             { return getElement(RAISE_); }
    "except"            { return getElement(EXCEPT); }
    "&except"            { return getElement(EXCEPT_); }
    "on"                { return getElement(ON); }
    "&on"                { return getElement(ON_); }
    "finally"           { return getElement(FINALLY); }
    "&finally"           { return getElement(FINALLY_); }

    "var"               { return getElement(VAR); }
    "&var"               { return getElement(VAR_); }
    "const"             { return getElement(CONST); }
    "&const"             { return getElement(CONST_); }
    "type"              { return getElement(TYPE); }
    "&type"              { return getElement(TYPE_); }
    "threadvar"         { return getElement(THREADVAR); }
    "&threadvar"         { return getElement(THREADVAR_); }
    "resourcestring"    { return getElement(RESOURCESTRING); }
    "&resourcestring"    { return getElement(RESOURCESTRING_); }
    "constref"          { return getElement(CONSTREF); }
    "&constref"          { return getElement(CONSTREF_); }

    "procedure"         { return getElement(PROCEDURE); }
    "&procedure"         { return getElement(PROCEDURE_); }
    "function"          { return getElement(FUNCTION); }
    "&function"          { return getElement(FUNCTION_); }
    "array"             { return getElement(ARRAY); }
    "&array"             { return getElement(ARRAY_); }
    "record"            { return getElement(RECORD); }
    "&record"            { return getElement(RECORD_); }
    "set"               { return getElement(SET); }
    "&set"               { return getElement(SET_); }
    "file"              { return getElement(FILE); }
    "&file"              { return getElement(FILE_); }
    "object"            { return getElement(OBJECT); }
    "&object"            { return getElement(OBJECT_); }

    "of"                { return getElement(OF); }
    "&of"                { return getElement(OF_); }
    "absolute"          { return getElement(ABSOLUTE); }
    "&absolute"          { return getElement(ABSOLUTE_); }
    "packed"            { return getElement(PACKED); }
    "&packed"            { return getElement(PACKED_); }
    "operator"          { return getElement(OPERATOR); }
    "&operator"          { return getElement(OPERATOR_); }

    "constructor"       { return getElement(CONSTRUCTOR); }
    "&constructor"       { return getElement(CONSTRUCTOR_); }
    "destructor"        { return getElement(DESTRUCTOR); }
    "&destructor"        { return getElement(DESTRUCTOR_); }
    "property"          { return getElement(PROPERTY); }
    "&property"          { return getElement(PROPERTY_); }

    "label"             { return getElement(LABEL); }
    "&label"             { return getElement(LABEL_); }
    "goto"              { return getElement(GOTO); }
    "&goto"              { return getElement(GOTO_); }
    "exit"              { return getElement(EXIT); }
    "&exit"              { return getElement(EXIT_); }
    "break"             { return getElement(BREAK); }
    "&break"             { return getElement(BREAK_); }
    "continue"          { return getElement(CONTINUE); }
    "&continue"          { return getElement(CONTINUE_); }

    "strict"            { return getElement(STRICT); }
    "&strict"            { return getElement(STRICT_); }
    "private"           { return getElement(PRIVATE); }
    "&private"           { return getElement(PRIVATE_); }
    "protected"         { return getElement(PROTECTED); }
    "&protected"         { return getElement(PROTECTED_); }
    "public"            { return getElement(PUBLIC); }
    "&public"            { return getElement(PUBLIC_); }
    "published"         { return getElement(PUBLISHED); }
    "&published"         { return getElement(PUBLISHED_); }
    "automated"         { return getElement(AUTOMATED); }
    "&automated"         { return getElement(AUTOMATED_); }

    "virtual"           { return getElement(VIRTUAL); }
    "&virtual"           { return getElement(VIRTUAL_); }
    "dynamic"           { return getElement(DYNAMIC); }
    "&dynamic"           { return getElement(DYNAMIC_); }
    "abstract"          { return getElement(ABSTRACT); }
    "&abstract"          { return getElement(ABSTRACT_); }
    "overload"          { return getElement(OVERLOAD); }
    "&overload"          { return getElement(OVERLOAD_); }
    "override"          { return getElement(OVERRIDE); }
    "&override"          { return getElement(OVERRIDE_); }
    "reintroduce"       { return getElement(REINTRODUCE); }
    "&reintroduce"       { return getElement(REINTRODUCE_); }

    "message"           { return getElement(MESSAGE); }
    "&message"           { return getElement(MESSAGE_); }
    "static"            { return getElement(STATIC); }
    "&static"            { return getElement(STATIC_); }
    "sealed"            { return getElement(SEALED); }
    "&sealed"            { return getElement(SEALED_); }
    "final"             { return getElement(FINAL); }
    "&final"             { return getElement(FINAL_); }
    "assembler"         { return getElement(ASSEMBLER); }
    "&assembler"         { return getElement(ASSEMBLER_); }

    "cdecl"             { return getElement(CDECL); }
    "&cdecl"             { return getElement(CDECL_); }
    "pascal"            { return getElement(PASCAL); }
    "&pascal"            { return getElement(PASCAL_); }
    "register"          { return getElement(REGISTER); }
    "&register"          { return getElement(REGISTER_); }
    "safecall"          { return getElement(SAFECALL); }
    "&safecall"          { return getElement(SAFECALL_); }
    "stdcall"           { return getElement(STDCALL); }
    "&stdcall"           { return getElement(STDCALL_); }
    "export"            { return getElement(EXPORT); }
    "&export"            { return getElement(EXPORT_); }
    "inline"            { return getElement(INLINE); }
    "&inline"            { return getElement(INLINE_); }

    "dispid"            { return getElement(DISPID); }
    "&dispid"            { return getElement(DISPID_); }
    "external"          { return getElement(EXTERNAL); }
    "&external"          { return getElement(EXTERNAL_); }
    "forward"           { return getElement(FORWARD); }
    "&forward"           { return getElement(FORWARD_); }
    "helper"            { return getElement(HELPER); }
    "&helper"            { return getElement(HELPER_); }
    "implements"        { return getElement(IMPLEMENTS); }
    "&implements"        { return getElement(IMPLEMENTS_); }

    "default"           { return getElement(DEFAULT); }
    "&default"           { return getElement(DEFAULT_); }
    "index"             { return getElement(INDEX); }
    "&index"             { return getElement(INDEX_); }
    "read"              { return getElement(READ); }
    "&read"              { return getElement(READ_); }
    "write"             { return getElement(WRITE); }
    "&write"             { return getElement(WRITE_); }

    "deprecated"        { return getElement(DEPRECATED); }
    "&deprecated"        { return getElement(DEPRECATED_); }
    "experimental"      { return getElement(EXPERIMENTAL); }
    "&experimental"      { return getElement(EXPERIMENTAL_); }
    "platform"          { return getElement(PLATFORM); }
    "&platform"          { return getElement(PLATFORM_); }
    "reference"         { return getElement(REFERENCE); }
    "&reference"         { return getElement(REFERENCE_); }

    "for"               { return getElement(FOR); }
    "&for"               { return getElement(FOR_); }
    "to"                { return getElement(TO); }
    "&to"                { return getElement(TO_); }
    "downto"            { return getElement(DOWNTO); }
    "&downto"            { return getElement(DOWNTO_); }
    "repeat"            { return getElement(REPEAT); }
    "&repeat"            { return getElement(REPEAT_); }
    "until"             { return getElement(UNTIL); }
    "&until"             { return getElement(UNTIL_); }
    "while"             { return getElement(WHILE); }
    "&while"             { return getElement(WHILE_); }
    "do"                { return getElement(DO); }
    "&do"                { return getElement(DO_); }
    "with"              { return getElement(WITH); }
    "&with"              { return getElement(WITH_); }

    "begin"             { return getElement(BEGIN); }
    "&begin"             { return getElement(BEGIN_); }
    "end"               { return getElement(END); }
    "&end"               { return getElement(END_); }
    "if"                { return getElement(IF); }
    "&if"                { return getElement(IF_); }
    "then"              { return getElement(THEN); }
    "&then"              { return getElement(THEN_); }
    "else"              { return getElement(ELSE); }
    "&else"              { return getElement(ELSE_); }
    "case"              { return getElement(CASE); }
    "&case"              { return getElement(CASE_); }

    "nil"               { return getElement(NIL); }
    "&nil"               { return getElement(NIL_); }
    "false"             { return getElement(FALSE); }
    "&false"             { return getElement(FALSE_); }
    "true"              { return getElement(TRUE); }
    "&true"              { return getElement(TRUE_); }

    "asm"               { return getElement(ASM); }
    "&asm"               { return getElement(ASM_); }
    "inherited"         { return getElement(INHERITED); }
    "&inherited"         { return getElement(INHERITED_); }
    "out"               { return getElement(OUT); }
    "&out"               { return getElement(OUT_); }
    "self"              { return getElement(SELF); }
    "&self"              { return getElement(SELF_); }
    "new"               { return getElement(NEW); }
    "&new"               { return getElement(NEW_); }
    "specialize"         { return getElement(SPECIALIZE); }

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
    "<>"            { return getElement(NE); }
    "<="             { return getElement(LTEQ); }

    ":"             { return getElement(COLON); }
    ","             { return getElement(COMMA); }
    "."             { return getElement(DOT); }
    "^"             { return getElement(DEREF); }
    "@"             { return getElement(AT); }
    "$"             { return getElement(HEXNUM); }
    "#"             { return getElement(CHARNUM); }
//    "&"             { return getElement(KEYWORDESCAPE); }

    ";"             { return getElement(SEMI); }

    "("             { return getElement(LPAREN); }
    ")"             { return getElement(RPAREN); }
    "["             { return getElement(LBRACK); }
    "]"             { return getElement(RBRACK); }
    "(."            { return getElement(LBRACK); }
    ".)"            { return getElement(RBRACK); }

    {CT_DEFINE}     { define(zzCurrentPos, yytext()); return CT_DEFINE; }
    {CT_UNDEFINE}   { unDefine(zzCurrentPos, yytext()); return CT_UNDEFINE; }

    {CT_IF}         { return handleIf(zzCurrentPos, yytext()); }
    {CT_IFDEF}      { return handleIfDef(zzCurrentPos, yytext()); }
    {CT_IFNDEF}     { return handleIfNDef(zzCurrentPos, yytext()); }
    {CT_IFOPT}      { return handleIfOpt(zzCurrentPos, yytext()); }
    {CT_ELSE}       { return handleElse(zzCurrentPos); }
    {CT_ELSEIF}     { return handleElseIf(zzCurrentPos, yytext()); }
    {CT_ENDIF}      { return handleEndIf(zzCurrentPos); }
    {CT_IFEND}      { return handleEndIf(zzCurrentPos); }

    {INCLUDE}       { return handleInclude(zzCurrentPos, yytext()); }

    {COMP_OPTION}   { return getElement(COMP_OPTION); }

    {STRING_LITERAL} { return getElement(STRING_LITERAL); }
    {STRING_LITERAL_UNC} { return getElement(STRING_LITERAL_UNC); }

    {NUM_INT}       { return getElement(NUMBER_INT); }
    {NUM_REAL}      { return getElement(NUMBER_REAL); }
    {NUM_HEX}       { return getElement(NUMBER_HEX); }
    {NUM_BIN}       { return getElement(NUMBER_BIN); }
    {NUM_OCT}       { return getElement(NUMBER_OCT); }

    {COMMENT}       { return getElement(COMMENT); }
    {IDENTIFIER}    { return getElement(NAME); }
    {IDENTIFIER_}    { return getElement(NAME_); }
    {WHITESPACE}    { return TokenType.WHITE_SPACE; }
    .               { return TokenType.BAD_CHARACTER; }
}


//<<EOF>>        { if (yymoreStreams()) { yypopStream(); } else { zzAtEOF = true; zzDoEOF(); return null; }}
