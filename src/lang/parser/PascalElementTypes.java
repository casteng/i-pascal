/*
 * Copyright 2010 Jon S Akhtar (Sylvanaar)
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */

package com.siberika.idea.pascal.lang.parser;

import com.intellij.psi.tree.IElementType;
import com.intellij.psi.tree.TokenSet;
import com.siberika.idea.pascal.lang.lexer.PascalElementType;
import com.siberika.idea.pascal.lang.lexer.PascalTokenTypes;

/**
 * Created by IntelliJ IDEA.
 * User: Jon S Akhtar
 * Date: Apr 10, 2010
 * Time: 3:54:46 PM
 */
public interface PascalElementTypes extends PascalTokenTypes {
    IElementType EMPTY_INPUT = new PascalElementType("empty input");

    

    IElementType FUNCTION_DEFINITION = new PascalElementType("Function Definition");

    IElementType LOCAL_NAME = new PascalElementType("local name");
    IElementType LOCAL_NAME_DECL = new PascalElementType("local name declaration");

    IElementType GLOBAL_NAME = new PascalElementType("global name");
  //  IElementType GLOBAL_NAME_DECL = new PascalElementType("global name declaration");
 // IElementType GETTABLE = new PascalElementType("get table");
//IElementType GETSELF = new PascalElementType("get self");
    //LuaStubElementType<LuaCompoundIdentifierStub, LuaCompoundIdentifier> GETSELF = new LuaStubCompoundIdentifierType();


    

    IElementType TABLE_INDEX = new PascalElementType("table index");
    IElementType KEY_ASSIGNMENT = new PascalElementType("keyed field initializer");
    IElementType IDX_ASSIGNMENT = new PascalElementType("indexed field initializer");

    IElementType REFERENCE = new PascalElementType("Reference");

    IElementType COMPOUND_REFERENCE = new PascalElementType("Compound Reference");
    IElementType IDENTIFIER_LIST = new PascalElementType("Identifier List");

    IElementType STATEMENT = new PascalElementType("Statment");
    IElementType LAST_STATEMENT = new PascalElementType("LastStatement");
    IElementType EXPR = new PascalElementType("Expression");
    IElementType EXPR_LIST = new PascalElementType("Expression List");

    IElementType LITERAL_EXPRESSION = new PascalElementType("Literal Expression");
    IElementType PARENTHEICAL_EXPRESSION = new PascalElementType("Parentheical Expression");

    IElementType FUNCTION_CALL_ARGS = new PascalElementType("Function Call Args");
    IElementType FUNCTION_CALL = new PascalElementType("Function Call Statement");
    IElementType FUNCTION_CALL_EXPR = new PascalElementType("Function Call Expression");
    IElementType ANONYMOUS_FUNCTION_EXPRESSION = new PascalElementType("Anonymous function expression");

    IElementType ASSIGN_STMT = new PascalElementType("Assignment Statement");
    IElementType CONDITIONAL_EXPR = new PascalElementType("Conditional Expression");

    IElementType LOCAL_DECL_WITH_ASSIGNMENT = new PascalElementType("Local Declaration With Assignment Statement");
    IElementType LOCAL_DECL = new PascalElementType("Local Declaration");

    IElementType SELF_PARAMETER = new PascalElementType("Implied parameter (self)");

    IElementType BLOCK = new PascalElementType("Block");

    IElementType UNARY_EXP = new PascalElementType("UnExp");
    IElementType BINARY_EXP = new PascalElementType("BinExp");
    IElementType UNARY_OP = new PascalElementType("UnOp");
    IElementType BINARY_OP = new PascalElementType("BinOp");

    IElementType DO_BLOCK = new PascalElementType("Do Block");

    IElementType WHILE_BLOCK = new PascalElementType("While Block");

    IElementType REPEAT_BLOCK = new PascalElementType("Repeat Block");
    IElementType GENERIC_FOR_BLOCK = new PascalElementType("Generic For Block");
    IElementType IF_THEN_BLOCK = new PascalElementType("If-Then Block");
    IElementType NUMERIC_FOR_BLOCK = new PascalElementType("Numeric For Block");

    TokenSet EXPRESSION_SET = TokenSet.create(LITERAL_EXPRESSION, BINARY_EXP, UNARY_EXP, EXPR);
    IElementType RETURN_STATEMENT = new PascalElementType("Return statement");
    IElementType RETURN_STATEMENT_WITH_TAIL_CALL = new PascalElementType("Tailcall Return statement");

    IElementType LOCAL_FUNCTION = new PascalElementType("local function def");

    TokenSet BLOCK_SET = TokenSet.create(FUNCTION_DEFINITION, LOCAL_FUNCTION, ANONYMOUS_FUNCTION_EXPRESSION,
            WHILE_BLOCK,
            GENERIC_FOR_BLOCK,
            IF_THEN_BLOCK,
            NUMERIC_FOR_BLOCK,
            REPEAT_BLOCK,
            DO_BLOCK);

    IElementType PARAMETER = new PascalElementType("function parameters");
    IElementType PARAMETER_LIST = new PascalElementType("function parameter");

    IElementType UPVAL_NAME = new PascalElementType("upvalue name");
    IElementType MAIN_CHUNK_VARARGS = new PascalElementType("main chunk args");
}
