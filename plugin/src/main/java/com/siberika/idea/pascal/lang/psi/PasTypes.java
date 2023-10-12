// This is a generated file. Not intended for manual editing.
package com.siberika.idea.pascal.lang.psi;

import com.intellij.psi.tree.IElementType;
import com.intellij.psi.PsiElement;
import com.intellij.lang.ASTNode;
import com.siberika.idea.pascal.lang.stub.struct.PasClassHelperDeclStubElementType;
import com.siberika.idea.pascal.lang.stub.struct.PasClassDeclStubElementType;
import com.siberika.idea.pascal.lang.stub.PasExportedRoutineStubElementType;
import com.siberika.idea.pascal.lang.stub.struct.PasInterfaceDeclStubElementType;
import com.siberika.idea.pascal.lang.stub.PasIdentStubElementType;
import com.siberika.idea.pascal.lang.stub.struct.PasObjectDeclStubElementType;
import com.siberika.idea.pascal.lang.stub.struct.PasRecordDeclStubElementType;
import com.siberika.idea.pascal.lang.stub.struct.PasRecordHelperDeclStubElementType;
import com.siberika.idea.pascal.lang.lexer.PascalElementType;
import com.siberika.idea.pascal.lang.psi.impl.*;

public interface PasTypes {

  IElementType ADD_OP = new PascalPsiElementType("ADD_OP");
  IElementType ARGUMENT_LIST = new PascalPsiElementType("ARGUMENT_LIST");
  IElementType ARRAY_CONST_EXPR = new PascalPsiElementType("ARRAY_CONST_EXPR");
  IElementType ARRAY_INDEX = new PascalPsiElementType("ARRAY_INDEX");
  IElementType ARRAY_TYPE = new PascalPsiElementType("ARRAY_TYPE");
  IElementType ASSEMBLER_STATEMENT = new PascalPsiElementType("ASSEMBLER_STATEMENT");
  IElementType ASSIGN_OP = new PascalPsiElementType("ASSIGN_OP");
  IElementType ASSIGN_PART = new PascalPsiElementType("ASSIGN_PART");
  IElementType ATTRIBUTE_PARAM_LIST = new PascalPsiElementType("ATTRIBUTE_PARAM_LIST");
  IElementType BLOCK_BODY = new PascalPsiElementType("BLOCK_BODY");
  IElementType BLOCK_GLOBAL = new PascalPsiElementType("BLOCK_GLOBAL");
  IElementType BLOCK_LOCAL = new PascalPsiElementType("BLOCK_LOCAL");
  IElementType BLOCK_LOCAL_NESTED_1 = new PascalPsiElementType("BLOCK_LOCAL_NESTED_1");
  IElementType BLOCK_LOCAL_WO_NESTED = new PascalPsiElementType("BLOCK_LOCAL_WO_NESTED");
  IElementType BREAK_STATEMENT = new PascalPsiElementType("BREAK_STATEMENT");
  IElementType CALL_EXPR = new PascalPsiElementType("CALL_EXPR");
  IElementType CASE_ELSE = new PascalPsiElementType("CASE_ELSE");
  IElementType CASE_ITEM = new PascalPsiElementType("CASE_ITEM");
  IElementType CASE_STATEMENT = new PascalPsiElementType("CASE_STATEMENT");
  IElementType CLASS_FIELD = new PascalPsiElementType("CLASS_FIELD");
  IElementType CLASS_HELPER_DECL = new PasClassHelperDeclStubElementType("CLASS_HELPER_DECL");
  IElementType CLASS_METHOD_RESOLUTION = new PascalPsiElementType("CLASS_METHOD_RESOLUTION");
  IElementType CLASS_PARENT = new PascalPsiElementType("CLASS_PARENT");
  IElementType CLASS_PROPERTY = new PascalPsiElementType("CLASS_PROPERTY");
  IElementType CLASS_PROPERTY_ARRAY = new PascalPsiElementType("CLASS_PROPERTY_ARRAY");
  IElementType CLASS_PROPERTY_INDEX = new PascalPsiElementType("CLASS_PROPERTY_INDEX");
  IElementType CLASS_PROPERTY_SPECIFIER = new PascalPsiElementType("CLASS_PROPERTY_SPECIFIER");
  IElementType CLASS_QUALIFIED_IDENT = new PascalPsiElementType("CLASS_QUALIFIED_IDENT");
  IElementType CLASS_STATE = new PascalPsiElementType("CLASS_STATE");
  IElementType CLASS_TYPE_DECL = new PasClassDeclStubElementType("CLASS_TYPE_DECL");
  IElementType CLASS_TYPE_TYPE_DECL = new PascalPsiElementType("CLASS_TYPE_TYPE_DECL");
  IElementType CLOSURE_EXPR = new PascalPsiElementType("CLOSURE_EXPR");
  IElementType CLOSURE_ROUTINE = new PascalPsiElementType("CLOSURE_ROUTINE");
  IElementType COLON_CONSTRUCT = new PascalPsiElementType("COLON_CONSTRUCT");
  IElementType COMPOUND_STATEMENT = new PascalPsiElementType("COMPOUND_STATEMENT");
  IElementType CONSTRAINED_TYPE_PARAM = new PascalPsiElementType("CONSTRAINED_TYPE_PARAM");
  IElementType CONST_DECLARATION = new PascalPsiElementType("CONST_DECLARATION");
  IElementType CONST_EXPRESSION = new PascalPsiElementType("CONST_EXPRESSION");
  IElementType CONST_EXPRESSION_ORD = new PascalPsiElementType("CONST_EXPRESSION_ORD");
  IElementType CONST_SECTION = new PascalPsiElementType("CONST_SECTION");
  IElementType CONTAINS_CLAUSE = new PascalPsiElementType("CONTAINS_CLAUSE");
  IElementType CONTINUE_STATEMENT = new PascalPsiElementType("CONTINUE_STATEMENT");
  IElementType CUSTOM_ATTRIBUTE_DECL = new PascalPsiElementType("CUSTOM_ATTRIBUTE_DECL");
  IElementType DEREFERENCE_EXPR = new PascalPsiElementType("DEREFERENCE_EXPR");
  IElementType ENUM_TYPE = new PascalPsiElementType("ENUM_TYPE");
  IElementType ESCAPED_IDENT = new PascalPsiElementType("ESCAPED_IDENT");
  IElementType EXIT_STATEMENT = new PascalPsiElementType("EXIT_STATEMENT");
  IElementType EXPORTED_ROUTINE = new PasExportedRoutineStubElementType("EXPORTED_ROUTINE");
  IElementType EXPORTS_SECTION = new PascalPsiElementType("EXPORTS_SECTION");
  IElementType EXPR = new PascalPsiElementType("EXPR");
  IElementType EXPRESSION = new PascalPsiElementType("EXPRESSION");
  IElementType EXTERNAL_DIRECTIVE = new PascalPsiElementType("EXTERNAL_DIRECTIVE");
  IElementType FILE_TYPE = new PascalPsiElementType("FILE_TYPE");
  IElementType FORMAL_PARAMETER = new PascalPsiElementType("FORMAL_PARAMETER");
  IElementType FORMAL_PARAMETER_SECTION = new PascalPsiElementType("FORMAL_PARAMETER_SECTION");
  IElementType FOR_INLINE_DECLARATION = new PascalPsiElementType("FOR_INLINE_DECLARATION");
  IElementType FOR_STATEMENT = new PascalPsiElementType("FOR_STATEMENT");
  IElementType FROM_EXPRESSION = new PascalPsiElementType("FROM_EXPRESSION");
  IElementType FULLY_QUALIFIED_IDENT = new PascalPsiElementType("FULLY_QUALIFIED_IDENT");
  IElementType FUNCTION_DIRECTIVE = new PascalPsiElementType("FUNCTION_DIRECTIVE");
  IElementType GENERIC_CONSTRAINT = new PascalPsiElementType("GENERIC_CONSTRAINT");
  IElementType GENERIC_POSTFIX = new PascalPsiElementType("GENERIC_POSTFIX");
  IElementType GENERIC_TYPE_IDENT = new PascalPsiElementType("GENERIC_TYPE_IDENT");
  IElementType GOTO_STATEMENT = new PascalPsiElementType("GOTO_STATEMENT");
  IElementType HANDLER = new PascalPsiElementType("HANDLER");
  IElementType IF_ELSE_STATEMENT = new PascalPsiElementType("IF_ELSE_STATEMENT");
  IElementType IF_STATEMENT = new PascalPsiElementType("IF_STATEMENT");
  IElementType IF_THEN_STATEMENT = new PascalPsiElementType("IF_THEN_STATEMENT");
  IElementType IMPL_DECL_SECTION = new PascalPsiElementType("IMPL_DECL_SECTION");
  IElementType INDEX_EXPR = new PascalPsiElementType("INDEX_EXPR");
  IElementType INDEX_LIST = new PascalPsiElementType("INDEX_LIST");
  IElementType INHERITED_CALL = new PascalPsiElementType("INHERITED_CALL");
  IElementType INLINE_CONST_DECLARATION = new PascalPsiElementType("INLINE_CONST_DECLARATION");
  IElementType INLINE_VAR_DECLARATION = new PascalPsiElementType("INLINE_VAR_DECLARATION");
  IElementType INTERFACE_TYPE_DECL = new PasInterfaceDeclStubElementType("INTERFACE_TYPE_DECL");
  IElementType IN_OPERATOR_QUALIFIED_IDENT = new PascalPsiElementType("IN_OPERATOR_QUALIFIED_IDENT");
  IElementType KEYWORD_IDENT = new PascalPsiElementType("KEYWORD_IDENT");
  IElementType LABEL_DECL_SECTION = new PascalPsiElementType("LABEL_DECL_SECTION");
  IElementType LABEL_ID = new PascalPsiElementType("LABEL_ID");
  IElementType LIBRARY_MODULE_HEAD = new PascalPsiElementType("LIBRARY_MODULE_HEAD");
  IElementType LITERAL_EXPR = new PascalPsiElementType("LITERAL_EXPR");
  IElementType MUL_OP = new PascalPsiElementType("MUL_OP");
  IElementType NAMED_IDENT = new PascalPsiElementType("NAMED_IDENT");
  IElementType NAMED_IDENT_DECL = new PasIdentStubElementType("NAMED_IDENT_DECL");
  IElementType NAMESPACE_IDENT = new PascalPsiElementType("NAMESPACE_IDENT");
  IElementType NEW_STATEMENT = new PascalPsiElementType("NEW_STATEMENT");
  IElementType OBJECT_DECL = new PasObjectDeclStubElementType("OBJECT_DECL");
  IElementType OPERATOR_SUB_IDENT = new PascalPsiElementType("OPERATOR_SUB_IDENT");
  IElementType PACKAGE_MODULE_HEAD = new PascalPsiElementType("PACKAGE_MODULE_HEAD");
  IElementType PARAM_TYPE = new PascalPsiElementType("PARAM_TYPE");
  IElementType PAREN_EXPR = new PascalPsiElementType("PAREN_EXPR");
  IElementType POINTER_TYPE = new PascalPsiElementType("POINTER_TYPE");
  IElementType PROCEDURE_TYPE = new PascalPsiElementType("PROCEDURE_TYPE");
  IElementType PROC_BODY_BLOCK = new PascalPsiElementType("PROC_BODY_BLOCK");
  IElementType PROC_FORWARD_DECL = new PascalPsiElementType("PROC_FORWARD_DECL");
  IElementType PRODUCT_EXPR = new PascalPsiElementType("PRODUCT_EXPR");
  IElementType PROGRAM_MODULE_HEAD = new PascalPsiElementType("PROGRAM_MODULE_HEAD");
  IElementType PROGRAM_PARAM_LIST = new PascalPsiElementType("PROGRAM_PARAM_LIST");
  IElementType RAISE_STATEMENT = new PascalPsiElementType("RAISE_STATEMENT");
  IElementType RANGE_BOUND = new PascalPsiElementType("RANGE_BOUND");
  IElementType RECORD_CONST_EXPR = new PascalPsiElementType("RECORD_CONST_EXPR");
  IElementType RECORD_DECL = new PasRecordDeclStubElementType("RECORD_DECL");
  IElementType RECORD_HELPER_DECL = new PasRecordHelperDeclStubElementType("RECORD_HELPER_DECL");
  IElementType RECORD_VARIANT = new PascalPsiElementType("RECORD_VARIANT");
  IElementType REFERENCE_EXPR = new PascalPsiElementType("REFERENCE_EXPR");
  IElementType REF_NAMED_IDENT = new PascalPsiElementType("REF_NAMED_IDENT");
  IElementType RELATIONAL_EXPR = new PascalPsiElementType("RELATIONAL_EXPR");
  IElementType REL_OP = new PascalPsiElementType("REL_OP");
  IElementType REPEAT_STATEMENT = new PascalPsiElementType("REPEAT_STATEMENT");
  IElementType REQUIRES_CLAUSE = new PascalPsiElementType("REQUIRES_CLAUSE");
  IElementType ROUTINE_IMPL_DECL = new PascalPsiElementType("ROUTINE_IMPL_DECL");
  IElementType ROUTINE_IMPL_DECL_NESTED_1 = new PascalPsiElementType("ROUTINE_IMPL_DECL_NESTED_1");
  IElementType ROUTINE_IMPL_DECL_WO_NESTED = new PascalPsiElementType("ROUTINE_IMPL_DECL_WO_NESTED");
  IElementType SET_EXPR = new PascalPsiElementType("SET_EXPR");
  IElementType SET_TYPE = new PascalPsiElementType("SET_TYPE");
  IElementType STATEMENT = new PascalPsiElementType("STATEMENT");
  IElementType STMT_EMPTY = new PascalPsiElementType("STMT_EMPTY");
  IElementType STRING_FACTOR = new PascalPsiElementType("STRING_FACTOR");
  IElementType STRING_TYPE = new PascalPsiElementType("STRING_TYPE");
  IElementType SUB_IDENT = new PascalPsiElementType("SUB_IDENT");
  IElementType SUB_RANGE_TYPE = new PascalPsiElementType("SUB_RANGE_TYPE");
  IElementType SUM_EXPR = new PascalPsiElementType("SUM_EXPR");
  IElementType TRY_STATEMENT = new PascalPsiElementType("TRY_STATEMENT");
  IElementType TYPE_DECL = new PascalPsiElementType("TYPE_DECL");
  IElementType TYPE_DECLARATION = new PascalPsiElementType("TYPE_DECLARATION");
  IElementType TYPE_ID = new PascalPsiElementType("TYPE_ID");
  IElementType TYPE_SECTION = new PascalPsiElementType("TYPE_SECTION");
  IElementType UNARY_EXPR = new PascalPsiElementType("UNARY_EXPR");
  IElementType UNARY_OP = new PascalPsiElementType("UNARY_OP");
  IElementType UNIT_FINALIZATION = new PascalPsiElementType("UNIT_FINALIZATION");
  IElementType UNIT_IMPLEMENTATION = new PascalPsiElementType("UNIT_IMPLEMENTATION");
  IElementType UNIT_INITIALIZATION = new PascalPsiElementType("UNIT_INITIALIZATION");
  IElementType UNIT_INTERFACE = new PascalPsiElementType("UNIT_INTERFACE");
  IElementType UNIT_MODULE_HEAD = new PascalPsiElementType("UNIT_MODULE_HEAD");
  IElementType USES_CLAUSE = new PascalPsiElementType("USES_CLAUSE");
  IElementType VAR_DECLARATION = new PascalPsiElementType("VAR_DECLARATION");
  IElementType VAR_SECTION = new PascalPsiElementType("VAR_SECTION");
  IElementType VAR_VALUE_SPEC = new PascalPsiElementType("VAR_VALUE_SPEC");
  IElementType VISIBILITY = new PascalPsiElementType("VISIBILITY");
  IElementType WHILE_STATEMENT = new PascalPsiElementType("WHILE_STATEMENT");
  IElementType WITH_STATEMENT = new PascalPsiElementType("WITH_STATEMENT");

  IElementType ABSOLUTE = new PascalElementType("ABSOLUTE");
  IElementType ABSOLUTE_ = new PascalElementType("ABSOLUTE_");
  IElementType ABSTRACT = new PascalElementType("ABSTRACT");
  IElementType ABSTRACT_ = new PascalElementType("ABSTRACT_");
  IElementType AND = new PascalElementType("AND");
  IElementType AND_ = new PascalElementType("AND_");
  IElementType ARRAY = new PascalElementType("ARRAY");
  IElementType ARRAY_ = new PascalElementType("ARRAY_");
  IElementType AS = new PascalElementType("AS");
  IElementType ASM = new PascalElementType("ASM");
  IElementType ASM_ = new PascalElementType("ASM_");
  IElementType ASSEMBLER = new PascalElementType("ASSEMBLER");
  IElementType ASSEMBLER_ = new PascalElementType("ASSEMBLER_");
  IElementType ASSIGN = new PascalElementType(":=");
  IElementType AS_ = new PascalElementType("AS_");
  IElementType AT = new PascalElementType("@");
  IElementType AUTOMATED = new PascalElementType("AUTOMATED");
  IElementType AUTOMATED_ = new PascalElementType("AUTOMATED_");
  IElementType BEGIN = new PascalElementType("BEGIN");
  IElementType BEGIN_ = new PascalElementType("BEGIN_");
  IElementType BREAK = new PascalElementType("BREAK");
  IElementType BREAK_ = new PascalElementType("BREAK_");
  IElementType CASE = new PascalElementType("CASE");
  IElementType CASE_ = new PascalElementType("CASE_");
  IElementType CDECL = new PascalElementType("CDECL");
  IElementType CDECL_ = new PascalElementType("CDECL_");
  IElementType CHARNUM = new PascalElementType("#");
  IElementType CLASS = new PascalElementType("CLASS");
  IElementType CLASS_ = new PascalElementType("CLASS_");
  IElementType COLON = new PascalElementType(":");
  IElementType COMMA = new PascalElementType(",");
  IElementType COMMENT = new PascalElementType("COMMENT");
  IElementType COMP_OPTION = new PascalElementType("comp_option");
  IElementType CONST = new PascalElementType("CONST");
  IElementType CONSTREF = new PascalElementType("CONSTREF");
  IElementType CONSTREF_ = new PascalElementType("CONSTREF_");
  IElementType CONSTRUCTOR = new PascalElementType("CONSTRUCTOR");
  IElementType CONSTRUCTOR_ = new PascalElementType("CONSTRUCTOR_");
  IElementType CONST_ = new PascalElementType("CONST_");
  IElementType CONTAINS = new PascalElementType("CONTAINS");
  IElementType CONTAINS_ = new PascalElementType("CONTAINS_");
  IElementType CONTINUE = new PascalElementType("CONTINUE");
  IElementType CONTINUE_ = new PascalElementType("CONTINUE_");
  IElementType CT_DEFINE = new PascalElementType("ct_define");
  IElementType CT_ELSE = new PascalElementType("ct_else");
  IElementType CT_ENDIF = new PascalElementType("ct_endif");
  IElementType CT_IF = new PascalElementType("ct_if");
  IElementType CT_IFDEF = new PascalElementType("ct_ifdef");
  IElementType CT_IFNDEF = new PascalElementType("ct_ifndef");
  IElementType CT_IFOPT = new PascalElementType("ct_ifopt");
  IElementType CT_UNDEFINE = new PascalElementType("ct_undefine");
  IElementType DEFAULT = new PascalElementType("DEFAULT");
  IElementType DEFAULT_ = new PascalElementType("DEFAULT_");
  IElementType DEPRECATED = new PascalElementType("DEPRECATED");
  IElementType DEPRECATED_ = new PascalElementType("DEPRECATED_");
  IElementType DEREF = new PascalElementType("^");
  IElementType DESTRUCTOR = new PascalElementType("DESTRUCTOR");
  IElementType DESTRUCTOR_ = new PascalElementType("DESTRUCTOR_");
  IElementType DISPID = new PascalElementType("DISPID");
  IElementType DISPID_ = new PascalElementType("DISPID_");
  IElementType DISPINTERFACE = new PascalElementType("DISPINTERFACE");
  IElementType DISPINTERFACE_ = new PascalElementType("DISPINTERFACE_");
  IElementType DIV = new PascalElementType("/");
  IElementType DIV_ = new PascalElementType("DIV_");
  IElementType DIV_ASSIGN = new PascalElementType("/=");
  IElementType DO = new PascalElementType("DO");
  IElementType DOT = new PascalElementType(".");
  IElementType DOWNTO = new PascalElementType("DOWNTO");
  IElementType DOWNTO_ = new PascalElementType("DOWNTO_");
  IElementType DO_ = new PascalElementType("DO_");
  IElementType DYNAMIC = new PascalElementType("DYNAMIC");
  IElementType DYNAMIC_ = new PascalElementType("DYNAMIC_");
  IElementType ELSE = new PascalElementType("ELSE");
  IElementType ELSE_ = new PascalElementType("ELSE_");
  IElementType END = new PascalElementType("END");
  IElementType END_ = new PascalElementType("END_");
  IElementType EQ = new PascalElementType("=");
  IElementType EXCEPT = new PascalElementType("EXCEPT");
  IElementType EXCEPT_ = new PascalElementType("EXCEPT_");
  IElementType EXIT = new PascalElementType("EXIT");
  IElementType EXIT_ = new PascalElementType("EXIT_");
  IElementType EXPERIMENTAL = new PascalElementType("EXPERIMENTAL");
  IElementType EXPERIMENTAL_ = new PascalElementType("EXPERIMENTAL_");
  IElementType EXPORT = new PascalElementType("EXPORT");
  IElementType EXPORTS = new PascalElementType("EXPORTS");
  IElementType EXPORTS_ = new PascalElementType("EXPORTS_");
  IElementType EXPORT_ = new PascalElementType("EXPORT_");
  IElementType EXTERNAL = new PascalElementType("EXTERNAL");
  IElementType EXTERNAL_ = new PascalElementType("EXTERNAL_");
  IElementType FALSE = new PascalElementType("FALSE");
  IElementType FALSE_ = new PascalElementType("FALSE_");
  IElementType FILE = new PascalElementType("FILE");
  IElementType FILE_ = new PascalElementType("FILE_");
  IElementType FINAL = new PascalElementType("FINAL");
  IElementType FINALIZATION = new PascalElementType("FINALIZATION");
  IElementType FINALIZATION_ = new PascalElementType("FINALIZATION_");
  IElementType FINALLY = new PascalElementType("FINALLY");
  IElementType FINALLY_ = new PascalElementType("FINALLY_");
  IElementType FINAL_ = new PascalElementType("FINAL_");
  IElementType FOR = new PascalElementType("FOR");
  IElementType FORWARD = new PascalElementType("FORWARD");
  IElementType FORWARD_ = new PascalElementType("FORWARD_");
  IElementType FOR_ = new PascalElementType("FOR_");
  IElementType FUNCTION = new PascalElementType("FUNCTION");
  IElementType FUNCTION_ = new PascalElementType("FUNCTION_");
  IElementType GENERIC = new PascalElementType("GENERIC");
  IElementType GOTO = new PascalElementType("GOTO");
  IElementType GOTO_ = new PascalElementType("GOTO_");
  IElementType GT = new PascalElementType(">");
  IElementType HELPER = new PascalElementType("HELPER");
  IElementType HELPER_ = new PascalElementType("HELPER_");
  IElementType HEXNUM = new PascalElementType("$");
  IElementType IDIV = new PascalElementType("IDIV");
  IElementType IDIV_ = new PascalElementType("IDIV_");
  IElementType IF = new PascalElementType("IF");
  IElementType IF_ = new PascalElementType("IF_");
  IElementType IMPLEMENTATION = new PascalElementType("IMPLEMENTATION");
  IElementType IMPLEMENTATION_ = new PascalElementType("IMPLEMENTATION_");
  IElementType IMPLEMENTS = new PascalElementType("IMPLEMENTS");
  IElementType IMPLEMENTS_ = new PascalElementType("IMPLEMENTS_");
  IElementType IN = new PascalElementType("IN");
  IElementType INCLUDE = new PascalElementType("include");
  IElementType INDEX = new PascalElementType("INDEX");
  IElementType INDEX_ = new PascalElementType("INDEX_");
  IElementType INHERITED = new PascalElementType("INHERITED");
  IElementType INHERITED_ = new PascalElementType("INHERITED_");
  IElementType INITIALIZATION = new PascalElementType("INITIALIZATION");
  IElementType INITIALIZATION_ = new PascalElementType("INITIALIZATION_");
  IElementType INLINE = new PascalElementType("INLINE");
  IElementType INLINE_ = new PascalElementType("INLINE_");
  IElementType INTERFACE = new PascalElementType("INTERFACE");
  IElementType INTERFACE_ = new PascalElementType("INTERFACE_");
  IElementType IN_ = new PascalElementType("IN_");
  IElementType IS = new PascalElementType("IS");
  IElementType IS_ = new PascalElementType("IS_");
  IElementType LABEL = new PascalElementType("LABEL");
  IElementType LABEL_ = new PascalElementType("LABEL_");
  IElementType LBRACK = new PascalElementType("[");
  IElementType LIBRARY = new PascalElementType("LIBRARY");
  IElementType LIBRARY_ = new PascalElementType("LIBRARY_");
  IElementType LPAREN = new PascalElementType("(");
  IElementType LT = new PascalElementType("<");
  IElementType LTEQ = new PascalElementType("<=");
  IElementType MESSAGE = new PascalElementType("MESSAGE");
  IElementType MESSAGE_ = new PascalElementType("MESSAGE_");
  IElementType MINUS = new PascalElementType("-");
  IElementType MINUS_ASSIGN = new PascalElementType("-=");
  IElementType MOD = new PascalElementType("MOD");
  IElementType MOD_ = new PascalElementType("MOD_");
  IElementType MULT = new PascalElementType("*");
  IElementType MULT_ASSIGN = new PascalElementType("*=");
  IElementType NAME = new PascalElementType("NAME");
  IElementType NAME_ = new PascalElementType("NAME_");
  IElementType NE = new PascalElementType("<>");
  IElementType NEW = new PascalElementType("NEW");
  IElementType NEW_ = new PascalElementType("NEW_");
  IElementType NIL = new PascalElementType("NIL");
  IElementType NIL_ = new PascalElementType("NIL_");
  IElementType NOT = new PascalElementType("NOT");
  IElementType NOT_ = new PascalElementType("NOT_");
  IElementType NUMBER_BIN = new PascalElementType("NUMBER_BIN");
  IElementType NUMBER_HEX = new PascalElementType("NUMBER_HEX");
  IElementType NUMBER_INT = new PascalElementType("NUMBER_INT");
  IElementType NUMBER_OCT = new PascalElementType("NUMBER_OCT");
  IElementType NUMBER_REAL = new PascalElementType("NUMBER_REAL");
  IElementType OBJC_CATEGORY = new PascalElementType("objccategory");
  IElementType OBJC_CLASS = new PascalElementType("OBJC_CLASS");
  IElementType OBJC_PROTOCOL = new PascalElementType("objcprotocol");
  IElementType OBJECT = new PascalElementType("OBJECT");
  IElementType OBJECT_ = new PascalElementType("OBJECT_");
  IElementType OF = new PascalElementType("OF");
  IElementType OF_ = new PascalElementType("OF_");
  IElementType ON = new PascalElementType("ON");
  IElementType ON_ = new PascalElementType("ON_");
  IElementType OPERATOR = new PascalElementType("OPERATOR");
  IElementType OPERATOR_ = new PascalElementType("OPERATOR_");
  IElementType OR = new PascalElementType("OR");
  IElementType OR_ = new PascalElementType("OR_");
  IElementType OUT = new PascalElementType("OUT");
  IElementType OUT_ = new PascalElementType("OUT_");
  IElementType OVERLOAD = new PascalElementType("OVERLOAD");
  IElementType OVERLOAD_ = new PascalElementType("OVERLOAD_");
  IElementType OVERRIDE = new PascalElementType("OVERRIDE");
  IElementType OVERRIDE_ = new PascalElementType("OVERRIDE_");
  IElementType PACKAGE = new PascalElementType("PACKAGE");
  IElementType PACKAGE_ = new PascalElementType("PACKAGE_");
  IElementType PACKED = new PascalElementType("PACKED");
  IElementType PACKED_ = new PascalElementType("PACKED_");
  IElementType PASCAL = new PascalElementType("PASCAL");
  IElementType PASCAL_ = new PascalElementType("PASCAL_");
  IElementType PLATFORM = new PascalElementType("PLATFORM");
  IElementType PLATFORM_ = new PascalElementType("PLATFORM_");
  IElementType PLUS = new PascalElementType("+");
  IElementType PLUS_ASSIGN = new PascalElementType("+=");
  IElementType POWER = new PascalElementType("**");
  IElementType PRIVATE = new PascalElementType("PRIVATE");
  IElementType PRIVATE_ = new PascalElementType("PRIVATE_");
  IElementType PROCEDURE = new PascalElementType("PROCEDURE");
  IElementType PROCEDURE_ = new PascalElementType("PROCEDURE_");
  IElementType PROGRAM = new PascalElementType("PROGRAM");
  IElementType PROGRAM_ = new PascalElementType("PROGRAM_");
  IElementType PROPERTY = new PascalElementType("PROPERTY");
  IElementType PROPERTY_ = new PascalElementType("PROPERTY_");
  IElementType PROTECTED = new PascalElementType("PROTECTED");
  IElementType PROTECTED_ = new PascalElementType("PROTECTED_");
  IElementType PUBLIC = new PascalElementType("PUBLIC");
  IElementType PUBLIC_ = new PascalElementType("PUBLIC_");
  IElementType PUBLISHED = new PascalElementType("PUBLISHED");
  IElementType PUBLISHED_ = new PascalElementType("PUBLISHED_");
  IElementType RAISE = new PascalElementType("RAISE");
  IElementType RAISE_ = new PascalElementType("RAISE_");
  IElementType RANGE = new PascalElementType("..");
  IElementType RBRACK = new PascalElementType("]");
  IElementType READ = new PascalElementType("READ");
  IElementType READ_ = new PascalElementType("READ_");
  IElementType RECORD = new PascalElementType("RECORD");
  IElementType RECORD_ = new PascalElementType("RECORD_");
  IElementType REFERENCE = new PascalElementType("REFERENCE");
  IElementType REFERENCE_ = new PascalElementType("REFERENCE_");
  IElementType REGISTER = new PascalElementType("REGISTER");
  IElementType REGISTER_ = new PascalElementType("REGISTER_");
  IElementType REINTRODUCE = new PascalElementType("REINTRODUCE");
  IElementType REINTRODUCE_ = new PascalElementType("REINTRODUCE_");
  IElementType REPEAT = new PascalElementType("REPEAT");
  IElementType REPEAT_ = new PascalElementType("REPEAT_");
  IElementType REQUIRES = new PascalElementType("REQUIRES");
  IElementType REQUIRES_ = new PascalElementType("REQUIRES_");
  IElementType RESOURCESTRING = new PascalElementType("RESOURCESTRING");
  IElementType RESOURCESTRING_ = new PascalElementType("RESOURCESTRING_");
  IElementType RPAREN = new PascalElementType(")");
  IElementType SAFECALL = new PascalElementType("SAFECALL");
  IElementType SAFECALL_ = new PascalElementType("SAFECALL_");
  IElementType SEALED = new PascalElementType("SEALED");
  IElementType SEALED_ = new PascalElementType("SEALED_");
  IElementType SELF = new PascalElementType("SELF");
  IElementType SELF_ = new PascalElementType("SELF_");
  IElementType SEMI = new PascalElementType(";");
  IElementType SET = new PascalElementType("SET");
  IElementType SETTAIL_0_2_0 = new PascalElementType("setTail_0_2_0");
  IElementType SET_ = new PascalElementType("SET_");
  IElementType SHL = new PascalElementType("SHL");
  IElementType SHL_ = new PascalElementType("SHL_");
  IElementType SHR = new PascalElementType("SHR");
  IElementType SHR_ = new PascalElementType("SHR_");
  IElementType SPECIALIZE = new PascalElementType("SPECIALIZE");
  IElementType STATIC = new PascalElementType("STATIC");
  IElementType STATIC_ = new PascalElementType("STATIC_");
  IElementType STDCALL = new PascalElementType("STDCALL");
  IElementType STDCALL_ = new PascalElementType("STDCALL_");
  IElementType STRICT = new PascalElementType("STRICT");
  IElementType STRICT_ = new PascalElementType("STRICT_");
  IElementType STRING_LITERAL = new PascalElementType("STRING_LITERAL");
  IElementType STRING_LITERAL_UNC = new PascalElementType("STRING_LITERAL_UNC");
  IElementType THEN = new PascalElementType("THEN");
  IElementType THEN_ = new PascalElementType("THEN_");
  IElementType THREADVAR = new PascalElementType("THREADVAR");
  IElementType THREADVAR_ = new PascalElementType("THREADVAR_");
  IElementType TO = new PascalElementType("TO");
  IElementType TO_ = new PascalElementType("TO_");
  IElementType TRUE = new PascalElementType("TRUE");
  IElementType TRUE_ = new PascalElementType("TRUE_");
  IElementType TRY = new PascalElementType("TRY");
  IElementType TRY_ = new PascalElementType("TRY_");
  IElementType TYPE = new PascalElementType("TYPE");
  IElementType TYPE_ = new PascalElementType("TYPE_");
  IElementType UNIT = new PascalElementType("UNIT");
  IElementType UNIT_ = new PascalElementType("UNIT_");
  IElementType UNTIL = new PascalElementType("UNTIL");
  IElementType UNTIL_ = new PascalElementType("UNTIL_");
  IElementType USES = new PascalElementType("USES");
  IElementType USES_ = new PascalElementType("USES_");
  IElementType VAR = new PascalElementType("VAR");
  IElementType VAR_ = new PascalElementType("VAR_");
  IElementType VIRTUAL = new PascalElementType("VIRTUAL");
  IElementType VIRTUAL_ = new PascalElementType("VIRTUAL_");
  IElementType WHILE = new PascalElementType("WHILE");
  IElementType WHILE_ = new PascalElementType("WHILE_");
  IElementType WITH = new PascalElementType("WITH");
  IElementType WITH_ = new PascalElementType("WITH_");
  IElementType WRITE = new PascalElementType("WRITE");
  IElementType WRITE_ = new PascalElementType("WRITE_");
  IElementType XOR = new PascalElementType("XOR");
  IElementType XOR_ = new PascalElementType("XOR_");
  IElementType MODULE = new PascalElementType("MODULE_");

  class Factory {
    public static PsiElement createElement(ASTNode node) {
      IElementType type = node.getElementType();
      if (type == ADD_OP) {
        return new PasAddOpImpl(node);
      }
      else if (type == ARGUMENT_LIST) {
        return new PasArgumentListImpl(node);
      }
      else if (type == ARRAY_CONST_EXPR) {
        return new PasArrayConstExprImpl(node);
      }
      else if (type == ARRAY_INDEX) {
        return new PasArrayIndexImpl(node);
      }
      else if (type == ARRAY_TYPE) {
        return new PasArrayTypeImpl(node);
      }
      else if (type == ASSEMBLER_STATEMENT) {
        return new PasAssemblerStatementImpl(node);
      }
      else if (type == ASSIGN_OP) {
        return new PasAssignOpImpl(node);
      }
      else if (type == ASSIGN_PART) {
        return new PasAssignPartImpl(node);
      }
      else if (type == ATTRIBUTE_PARAM_LIST) {
        return new PasAttributeParamListImpl(node);
      }
      else if (type == BLOCK_BODY) {
        return new PasBlockBodyImpl(node);
      }
      else if (type == BLOCK_GLOBAL) {
        return new PasBlockGlobalImpl(node);
      }
      else if (type == BLOCK_LOCAL) {
        return new PasBlockLocalImpl(node);
      }
      else if (type == BLOCK_LOCAL_NESTED_1) {
        return new PasBlockLocalNested1Impl(node);
      }
      else if (type == BLOCK_LOCAL_WO_NESTED) {
        return new PasBlockLocalWONestedImpl(node);
      }
      else if (type == BREAK_STATEMENT) {
        return new PasBreakStatementImpl(node);
      }
      else if (type == CALL_EXPR) {
        return new PasCallExprImpl(node);
      }
      else if (type == CASE_ELSE) {
        return new PasCaseElseImpl(node);
      }
      else if (type == CASE_ITEM) {
        return new PasCaseItemImpl(node);
      }
      else if (type == CASE_STATEMENT) {
        return new PasCaseStatementImpl(node);
      }
      else if (type == CLASS_FIELD) {
        return new PasClassFieldImpl(node);
      }
      else if (type == CLASS_HELPER_DECL) {
        return new PasClassHelperDeclImpl(node);
      }
      else if (type == CLASS_METHOD_RESOLUTION) {
        return new PasClassMethodResolutionImpl(node);
      }
      else if (type == CLASS_PARENT) {
        return new PasClassParentImpl(node);
      }
      else if (type == CLASS_PROPERTY) {
        return new PasClassPropertyImpl(node);
      }
      else if (type == CLASS_PROPERTY_ARRAY) {
        return new PasClassPropertyArrayImpl(node);
      }
      else if (type == CLASS_PROPERTY_INDEX) {
        return new PasClassPropertyIndexImpl(node);
      }
      else if (type == CLASS_PROPERTY_SPECIFIER) {
        return new PasClassPropertySpecifierImpl(node);
      }
      else if (type == CLASS_QUALIFIED_IDENT) {
        return new PasClassQualifiedIdentImpl(node);
      }
      else if (type == CLASS_STATE) {
        return new PasClassStateImpl(node);
      }
      else if (type == CLASS_TYPE_DECL) {
        return new PasClassTypeDeclImpl(node);
      }
      else if (type == CLASS_TYPE_TYPE_DECL) {
        return new PasClassTypeTypeDeclImpl(node);
      }
      else if (type == CLOSURE_EXPR) {
        return new PasClosureExprImpl(node);
      }
      else if (type == CLOSURE_ROUTINE) {
        return new PasClosureRoutineImpl(node);
      }
      else if (type == COLON_CONSTRUCT) {
        return new PasColonConstructImpl(node);
      }
      else if (type == COMPOUND_STATEMENT) {
        return new PasCompoundStatementImpl(node);
      }
      else if (type == CONSTRAINED_TYPE_PARAM) {
        return new PasConstrainedTypeParamImpl(node);
      }
      else if (type == CONST_DECLARATION) {
        return new PasConstDeclarationImpl(node);
      }
      else if (type == CONST_EXPRESSION) {
        return new PasConstExpressionImpl(node);
      }
      else if (type == CONST_EXPRESSION_ORD) {
        return new PasConstExpressionOrdImpl(node);
      }
      else if (type == CONST_SECTION) {
        return new PasConstSectionImpl(node);
      }
      else if (type == CONTAINS_CLAUSE) {
        return new PasContainsClauseImpl(node);
      }
      else if (type == CONTINUE_STATEMENT) {
        return new PasContinueStatementImpl(node);
      }
      else if (type == CUSTOM_ATTRIBUTE_DECL) {
        return new PasCustomAttributeDeclImpl(node);
      }
      else if (type == DEREFERENCE_EXPR) {
        return new PasDereferenceExprImpl(node);
      }
      else if (type == ENUM_TYPE) {
        return new PasEnumTypeImpl(node);
      }
      else if (type == ESCAPED_IDENT) {
        return new PasEscapedIdentImpl(node);
      }
      else if (type == EXIT_STATEMENT) {
        return new PasExitStatementImpl(node);
      }
      else if (type == EXPORTED_ROUTINE) {
        return new PasExportedRoutineImpl(node);
      }
      else if (type == EXPORTS_SECTION) {
        return new PasExportsSectionImpl(node);
      }
      else if (type == EXPRESSION) {
        return new PasExpressionImpl(node);
      }
      else if (type == EXTERNAL_DIRECTIVE) {
        return new PasExternalDirectiveImpl(node);
      }
      else if (type == FILE_TYPE) {
        return new PasFileTypeImpl(node);
      }
      else if (type == FORMAL_PARAMETER) {
        return new PasFormalParameterImpl(node);
      }
      else if (type == FORMAL_PARAMETER_SECTION) {
        return new PasFormalParameterSectionImpl(node);
      }
      else if (type == FOR_INLINE_DECLARATION) {
        return new PasForInlineDeclarationImpl(node);
      }
      else if (type == FOR_STATEMENT) {
        return new PasForStatementImpl(node);
      }
      else if (type == FROM_EXPRESSION) {
        return new PasFromExpressionImpl(node);
      }
      else if (type == FULLY_QUALIFIED_IDENT) {
        return new PasFullyQualifiedIdentImpl(node);
      }
      else if (type == FUNCTION_DIRECTIVE) {
        return new PasFunctionDirectiveImpl(node);
      }
      else if (type == GENERIC_CONSTRAINT) {
        return new PasGenericConstraintImpl(node);
      }
      else if (type == GENERIC_POSTFIX) {
        return new PasGenericPostfixImpl(node);
      }
      else if (type == GENERIC_TYPE_IDENT) {
        return new PasGenericTypeIdentImpl(node);
      }
      else if (type == GOTO_STATEMENT) {
        return new PasGotoStatementImpl(node);
      }
      else if (type == HANDLER) {
        return new PasHandlerImpl(node);
      }
      else if (type == IF_ELSE_STATEMENT) {
        return new PasIfElseStatementImpl(node);
      }
      else if (type == IF_STATEMENT) {
        return new PasIfStatementImpl(node);
      }
      else if (type == IF_THEN_STATEMENT) {
        return new PasIfThenStatementImpl(node);
      }
      else if (type == IMPL_DECL_SECTION) {
        return new PasImplDeclSectionImpl(node);
      }
      else if (type == INDEX_EXPR) {
        return new PasIndexExprImpl(node);
      }
      else if (type == INDEX_LIST) {
        return new PasIndexListImpl(node);
      }
      else if (type == INHERITED_CALL) {
        return new PasInheritedCallImpl(node);
      }
      else if (type == INLINE_CONST_DECLARATION) {
        return new PasInlineConstDeclarationImpl(node);
      }
      else if (type == INLINE_VAR_DECLARATION) {
        return new PasInlineVarDeclarationImpl(node);
      }
      else if (type == INTERFACE_TYPE_DECL) {
        return new PasInterfaceTypeDeclImpl(node);
      }
      else if (type == IN_OPERATOR_QUALIFIED_IDENT) {
        return new PasInOperatorQualifiedIdentImpl(node);
      }
      else if (type == KEYWORD_IDENT) {
        return new PasKeywordIdentImpl(node);
      }
      else if (type == LABEL_DECL_SECTION) {
        return new PasLabelDeclSectionImpl(node);
      }
      else if (type == LABEL_ID) {
        return new PasLabelIdImpl(node);
      }
      else if (type == LIBRARY_MODULE_HEAD) {
        return new PasLibraryModuleHeadImpl(node);
      }
      else if (type == LITERAL_EXPR) {
        return new PasLiteralExprImpl(node);
      }
      else if (type == MUL_OP) {
        return new PasMulOpImpl(node);
      }
      else if (type == NAMED_IDENT) {
        return new PasNamedIdentImpl(node);
      }
      else if (type == NAMED_IDENT_DECL) {
        return new PasNamedIdentDeclImpl(node);
      }
      else if (type == NAMESPACE_IDENT) {
        return new PasNamespaceIdentImpl(node);
      }
      else if (type == NEW_STATEMENT) {
        return new PasNewStatementImpl(node);
      }
      else if (type == OBJECT_DECL) {
        return new PasObjectDeclImpl(node);
      }
      else if (type == OPERATOR_SUB_IDENT) {
        return new PasOperatorSubIdentImpl(node);
      }
      else if (type == PACKAGE_MODULE_HEAD) {
        return new PasPackageModuleHeadImpl(node);
      }
      else if (type == PARAM_TYPE) {
        return new PasParamTypeImpl(node);
      }
      else if (type == PAREN_EXPR) {
        return new PasParenExprImpl(node);
      }
      else if (type == POINTER_TYPE) {
        return new PasPointerTypeImpl(node);
      }
      else if (type == PROCEDURE_TYPE) {
        return new PasProcedureTypeImpl(node);
      }
      else if (type == PROC_BODY_BLOCK) {
        return new PasProcBodyBlockImpl(node);
      }
      else if (type == PROC_FORWARD_DECL) {
        return new PasProcForwardDeclImpl(node);
      }
      else if (type == PRODUCT_EXPR) {
        return new PasProductExprImpl(node);
      }
      else if (type == PROGRAM_MODULE_HEAD) {
        return new PasProgramModuleHeadImpl(node);
      }
      else if (type == PROGRAM_PARAM_LIST) {
        return new PasProgramParamListImpl(node);
      }
      else if (type == RAISE_STATEMENT) {
        return new PasRaiseStatementImpl(node);
      }
      else if (type == RANGE_BOUND) {
        return new PasRangeBoundImpl(node);
      }
      else if (type == RECORD_CONST_EXPR) {
        return new PasRecordConstExprImpl(node);
      }
      else if (type == RECORD_DECL) {
        return new PasRecordDeclImpl(node);
      }
      else if (type == RECORD_HELPER_DECL) {
        return new PasRecordHelperDeclImpl(node);
      }
      else if (type == RECORD_VARIANT) {
        return new PasRecordVariantImpl(node);
      }
      else if (type == REFERENCE_EXPR) {
        return new PasReferenceExprImpl(node);
      }
      else if (type == REF_NAMED_IDENT) {
        return new PasRefNamedIdentImpl(node);
      }
      else if (type == RELATIONAL_EXPR) {
        return new PasRelationalExprImpl(node);
      }
      else if (type == REL_OP) {
        return new PasRelOpImpl(node);
      }
      else if (type == REPEAT_STATEMENT) {
        return new PasRepeatStatementImpl(node);
      }
      else if (type == REQUIRES_CLAUSE) {
        return new PasRequiresClauseImpl(node);
      }
      else if (type == ROUTINE_IMPL_DECL) {
        return new PasRoutineImplDeclImpl(node);
      }
      else if (type == ROUTINE_IMPL_DECL_NESTED_1) {
        return new PasRoutineImplDeclNested1Impl(node);
      }
      else if (type == ROUTINE_IMPL_DECL_WO_NESTED) {
        return new PasRoutineImplDeclWoNestedImpl(node);
      }
      else if (type == SET_EXPR) {
        return new PasSetExprImpl(node);
      }
      else if (type == SET_TYPE) {
        return new PasSetTypeImpl(node);
      }
      else if (type == STATEMENT) {
        return new PasStatementImpl(node);
      }
      else if (type == STMT_EMPTY) {
        return new PasStmtEmptyImpl(node);
      }
      else if (type == STRING_FACTOR) {
        return new PasStringFactorImpl(node);
      }
      else if (type == STRING_TYPE) {
        return new PasStringTypeImpl(node);
      }
      else if (type == SUB_IDENT) {
        return new PasSubIdentImpl(node);
      }
      else if (type == SUB_RANGE_TYPE) {
        return new PasSubRangeTypeImpl(node);
      }
      else if (type == SUM_EXPR) {
        return new PasSumExprImpl(node);
      }
      else if (type == TRY_STATEMENT) {
        return new PasTryStatementImpl(node);
      }
      else if (type == TYPE_DECL) {
        return new PasTypeDeclImpl(node);
      }
      else if (type == TYPE_DECLARATION) {
        return new PasTypeDeclarationImpl(node);
      }
      else if (type == TYPE_ID) {
        return new PasTypeIDImpl(node);
      }
      else if (type == TYPE_SECTION) {
        return new PasTypeSectionImpl(node);
      }
      else if (type == UNARY_EXPR) {
        return new PasUnaryExprImpl(node);
      }
      else if (type == UNARY_OP) {
        return new PasUnaryOpImpl(node);
      }
      else if (type == UNIT_FINALIZATION) {
        return new PasUnitFinalizationImpl(node);
      }
      else if (type == UNIT_IMPLEMENTATION) {
        return new PasUnitImplementationImpl(node);
      }
      else if (type == UNIT_INITIALIZATION) {
        return new PasUnitInitializationImpl(node);
      }
      else if (type == UNIT_INTERFACE) {
        return new PasUnitInterfaceImpl(node);
      }
      else if (type == UNIT_MODULE_HEAD) {
        return new PasUnitModuleHeadImpl(node);
      }
      else if (type == USES_CLAUSE) {
        return new PasUsesClauseImpl(node);
      }
      else if (type == VAR_DECLARATION) {
        return new PasVarDeclarationImpl(node);
      }
      else if (type == VAR_SECTION) {
        return new PasVarSectionImpl(node);
      }
      else if (type == VAR_VALUE_SPEC) {
        return new PasVarValueSpecImpl(node);
      }
      else if (type == VISIBILITY) {
        return new PasVisibilityImpl(node);
      }
      else if (type == WHILE_STATEMENT) {
        return new PasWhileStatementImpl(node);
      }
      else if (type == WITH_STATEMENT) {
        return new PasWithStatementImpl(node);
      }
      throw new AssertionError("Unknown element type: " + type);
    }
  }
}
