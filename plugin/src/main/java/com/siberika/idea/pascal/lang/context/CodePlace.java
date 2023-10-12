package com.siberika.idea.pascal.lang.context;

public enum CodePlace {

                // Primary
    // No Pascal module found
    UNKNOWN,
    // header of a module
    MODULE_HEADER,
    // in USES clause
    USES,
    // in interface or implementation global declaration section
    GLOBAL_DECLARATION,
    // in local declaration section
    LOCAL_DECLARATION,

    // inside a statement
    STATEMENT,
    // inside a comment
    COMMENT,
    // inside a string literal
    STRING,


                // Secondary
    // place where an identifier is exists or may be
    NAMED_IDENT,
    // place where a type identifier is exists or needed
    TYPE_ID,

    // first referenced namespace in an FQN including indexed and dereferenced parts
    FIRST_IN_NAME,
    // first operand in expression
    FIRST_IN_EXPR,

    // inside expression
    EXPR,
    // inside expression and after operation
    EXPR_AFTER_OPERATION,
    // on actual parameter of a call expression
    EXPR_ARGUMENT,
    // expression within parentheses
    EXPR_PAREN,
    // set expression (within [])
    EXPR_SET,
    // array indexing expression (within [])
    EXPR_INDEX,

    // inside left part of assignment
    ASSIGN_LEFT,
    // inside right part of assignment
    ASSIGN_RIGHT,

    // inside if statement
    STMT_IF,
    // inside then part of if statement
    STMT_IF_THEN,
    // inside for statement
    STMT_FOR,
    // inside while statement
    STMT_WHILE,
    // inside repeat statement
    STMT_REPEAT,
    // inside case statement
    STMT_CASE,
    // inside case item
    STMT_CASE_ITEM,
    // inside case else branch
    STMT_CASE_ELSE,
    // inside raise statement
    STMT_RAISE,
    // inside try statement
    STMT_TRY,
    // inside except statement
    STMT_EXCEPT,

    // variables section
    SECTION_VAR,
    // constants section
    SECTION_CONST,
    // type declaration section
    SECTION_TYPE,
    // exports section of a library
    SECTION_EXPORTS,
    // variable declaration
    DECL_VAR,
    // constant declaration
    DECL_CONST,
    // type declaration
    DECL_TYPE,
    // property declaration
    DECL_PROPERTY,
    // structured type field declaration
    DECL_FIELD,

    // in array declaration inside []
    ARRAY_INDEX,
    // property access specifier
    PROPERTY_SPECIFIER,
    // array property parameters inside []
    PROPERTY_ARRAY_PARAM,
    // inside a generic definition
    GENERIC_DEFINITION,

    // constant expression
    CONST_EXPRESSION,
    // within "()" in record constant specifier
    CONST_RECORD,
    // within "()" in array constant specifier
    CONST_ARRAY,

    // routine/method declaration in interface section or within structured type declaration
    ROUTINE_DECL,
    // inside formal parameters
    FORMAL_PARAMETER,

    // inside header (before statement block) of a routine declaration
    ROUTINE_HEADER,
    // inside a routine implementation
    ROUTINE,
    // inside header (before field declarations) of a structured type declaration
    STRUCT_HEADER,
    // inside parent clause of a structured type declaration
    STRUCT_PARENT,
    // inside a structured type declaration
    STRUCT,
    // somewhere in interface or implementation global declaration section
    GLOBAL,
    // somewhere in local declaration section
    LOCAL,
    // somewhere in interface part of a unit
    INTERFACE

}
