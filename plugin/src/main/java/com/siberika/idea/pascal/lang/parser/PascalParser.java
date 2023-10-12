// This is a generated file. Not intended for manual editing.
package com.siberika.idea.pascal.lang.parser;

import com.intellij.lang.PsiBuilder;
import com.intellij.lang.PsiBuilder.Marker;
import static com.siberika.idea.pascal.lang.psi.PasTypes.*;
import static com.siberika.idea.pascal.lang.parser.PascalParserUtil.*;
import com.intellij.psi.tree.IElementType;
import com.intellij.lang.ASTNode;
import com.intellij.psi.tree.TokenSet;
import com.intellij.lang.PsiParser;
import com.intellij.lang.LightPsiParser;

@SuppressWarnings({"SimplifiableIfStatement", "UnusedAssignment"})
public class PascalParser implements PsiParser, LightPsiParser {

  public ASTNode parse(IElementType t, PsiBuilder b) {
    parseLight(t, b);
    return b.getTreeBuilt();
  }

  public void parseLight(IElementType t, PsiBuilder b) {
    boolean r;
    b = adapt_builder_(t, b, this, EXTENDS_SETS_);
    Marker m = enter_section_(b, 0, _COLLAPSE_, null);
    r = parse_root_(t, b);
    exit_section_(b, 0, m, t, r, true, TRUE_CONDITION);
  }

  protected boolean parse_root_(IElementType t, PsiBuilder b) {
    return parse_root_(t, b, 0);
  }

  static boolean parse_root_(IElementType t, PsiBuilder b, int l) {
    return Module(b, l + 1);
  }

  public static final TokenSet[] EXTENDS_SETS_ = new TokenSet[] {
    create_token_set_(CLASS_QUALIFIED_IDENT, IN_OPERATOR_QUALIFIED_IDENT),
    create_token_set_(OPERATOR_SUB_IDENT, SUB_IDENT),
    create_token_set_(CONST_EXPRESSION, CONST_EXPRESSION_ORD),
    create_token_set_(ROUTINE_IMPL_DECL, ROUTINE_IMPL_DECL_NESTED_1, ROUTINE_IMPL_DECL_WO_NESTED),
    create_token_set_(ARRAY_CONST_EXPR, CALL_EXPR, CLOSURE_EXPR, DEREFERENCE_EXPR,
      EXPR, INDEX_EXPR, LITERAL_EXPR, PAREN_EXPR,
      PRODUCT_EXPR, RECORD_CONST_EXPR, REFERENCE_EXPR, RELATIONAL_EXPR,
      SET_EXPR, SUM_EXPR, UNARY_EXPR),
  };

  /* ********************************************************** */
  // '(' [ !')' Expr [ColonConstruct]  (',' Expr [ColonConstruct]) * ] ')'
  public static boolean ArgumentList(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ArgumentList")) return false;
    if (!nextTokenIs(b, LPAREN)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, ARGUMENT_LIST, null);
    r = consumeToken(b, LPAREN);
    p = r; // pin = 1
    r = r && report_error_(b, ArgumentList_1(b, l + 1));
    r = p && consumeToken(b, RPAREN) && r;
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  // [ !')' Expr [ColonConstruct]  (',' Expr [ColonConstruct]) * ]
  private static boolean ArgumentList_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ArgumentList_1")) return false;
    ArgumentList_1_0(b, l + 1);
    return true;
  }

  // !')' Expr [ColonConstruct]  (',' Expr [ColonConstruct]) *
  private static boolean ArgumentList_1_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ArgumentList_1_0")) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_);
    r = ArgumentList_1_0_0(b, l + 1);
    p = r; // pin = 1
    r = r && report_error_(b, Expr(b, l + 1, -1));
    r = p && report_error_(b, ArgumentList_1_0_2(b, l + 1)) && r;
    r = p && ArgumentList_1_0_3(b, l + 1) && r;
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  // !')'
  private static boolean ArgumentList_1_0_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ArgumentList_1_0_0")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NOT_);
    r = !consumeToken(b, RPAREN);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // [ColonConstruct]
  private static boolean ArgumentList_1_0_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ArgumentList_1_0_2")) return false;
    ColonConstruct(b, l + 1);
    return true;
  }

  // (',' Expr [ColonConstruct]) *
  private static boolean ArgumentList_1_0_3(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ArgumentList_1_0_3")) return false;
    while (true) {
      int c = current_position_(b);
      if (!ArgumentList_1_0_3_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "ArgumentList_1_0_3", c)) break;
    }
    return true;
  }

  // ',' Expr [ColonConstruct]
  private static boolean ArgumentList_1_0_3_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ArgumentList_1_0_3_0")) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_);
    r = consumeToken(b, COMMA);
    p = r; // pin = 1
    r = r && report_error_(b, Expr(b, l + 1, -1));
    r = p && ArgumentList_1_0_3_0_2(b, l + 1) && r;
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  // [ColonConstruct]
  private static boolean ArgumentList_1_0_3_0_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ArgumentList_1_0_3_0_2")) return false;
    ColonConstruct(b, l + 1);
    return true;
  }

  /* ********************************************************** */
  // "(" constExpr ("," constExpr)+ ")"
  public static boolean ArrayConstExpr(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ArrayConstExpr")) return false;
    if (!nextTokenIs(b, LPAREN)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, LPAREN);
    r = r && constExpr(b, l + 1);
    r = r && ArrayConstExpr_2(b, l + 1);
    r = r && consumeToken(b, RPAREN);
    exit_section_(b, m, ARRAY_CONST_EXPR, r);
    return r;
  }

  // ("," constExpr)+
  private static boolean ArrayConstExpr_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ArrayConstExpr_2")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = ArrayConstExpr_2_0(b, l + 1);
    while (r) {
      int c = current_position_(b);
      if (!ArrayConstExpr_2_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "ArrayConstExpr_2", c)) break;
    }
    exit_section_(b, m, null, r);
    return r;
  }

  // "," constExpr
  private static boolean ArrayConstExpr_2_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ArrayConstExpr_2_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, COMMA);
    r = r && constExpr(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // SubRangeType | TypeID
  public static boolean ArrayIndex(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ArrayIndex")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, ARRAY_INDEX, "<array index>");
    r = SubRangeType(b, l + 1);
    if (!r) r = TypeID(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // [PACKED | "bitpacked"] ARRAY [arrayIndexes] OF arraySubType
  public static boolean ArrayType(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ArrayType")) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, ARRAY_TYPE, "<array type>");
    r = ArrayType_0(b, l + 1);
    r = r && consumeToken(b, ARRAY);
    p = r; // pin = 2
    r = r && report_error_(b, ArrayType_2(b, l + 1));
    r = p && report_error_(b, consumeToken(b, OF)) && r;
    r = p && arraySubType(b, l + 1) && r;
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  // [PACKED | "bitpacked"]
  private static boolean ArrayType_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ArrayType_0")) return false;
    ArrayType_0_0(b, l + 1);
    return true;
  }

  // PACKED | "bitpacked"
  private static boolean ArrayType_0_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ArrayType_0_0")) return false;
    boolean r;
    r = consumeToken(b, PACKED);
    if (!r) r = consumeToken(b, "bitpacked");
    return r;
  }

  // [arrayIndexes]
  private static boolean ArrayType_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ArrayType_2")) return false;
    arrayIndexes(b, l + 1);
    return true;
  }

  /* ********************************************************** */
  // ASM asmBlock END
  public static boolean AssemblerStatement(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "AssemblerStatement")) return false;
    if (!nextTokenIs(b, ASM)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, ASSEMBLER_STATEMENT, null);
    r = consumeToken(b, ASM);
    p = r; // pin = 1
    r = r && report_error_(b, asmBlock(b, l + 1));
    r = p && consumeToken(b, END) && r;
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  /* ********************************************************** */
  // ":=" | PLUS_ASSIGN | MINUS_ASSIGN | MULT_ASSIGN | DIV_ASSIGN
  public static boolean AssignOp(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "AssignOp")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, ASSIGN_OP, "<assign op>");
    r = consumeToken(b, ASSIGN);
    if (!r) r = consumeToken(b, PLUS_ASSIGN);
    if (!r) r = consumeToken(b, MINUS_ASSIGN);
    if (!r) r = consumeToken(b, MULT_ASSIGN);
    if (!r) r = consumeToken(b, DIV_ASSIGN);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // AssignOp (NewStatement | Expression)
  public static boolean AssignPart(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "AssignPart")) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, ASSIGN_PART, "<assign part>");
    r = AssignOp(b, l + 1);
    p = r; // pin = 1
    r = r && AssignPart_1(b, l + 1);
    exit_section_(b, l, m, r, p, PascalParser::rec_statement);
    return r || p;
  }

  // NewStatement | Expression
  private static boolean AssignPart_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "AssignPart_1")) return false;
    boolean r;
    r = NewStatement(b, l + 1);
    if (!r) r = Expression(b, l + 1);
    return r;
  }

  /* ********************************************************** */
  // Expression (COMMA Expression)*
  public static boolean AttributeParamList(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "AttributeParamList")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, ATTRIBUTE_PARAM_LIST, "<attribute param list>");
    r = Expression(b, l + 1);
    r = r && AttributeParamList_1(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // (COMMA Expression)*
  private static boolean AttributeParamList_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "AttributeParamList_1")) return false;
    while (true) {
      int c = current_position_(b);
      if (!AttributeParamList_1_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "AttributeParamList_1", c)) break;
    }
    return true;
  }

  // COMMA Expression
  private static boolean AttributeParamList_1_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "AttributeParamList_1_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, COMMA);
    r = r && Expression(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // CompoundStatement |	AssemblerStatement
  public static boolean BlockBody(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "BlockBody")) return false;
    if (!nextTokenIs(b, "<block body>", ASM, BEGIN)) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, BLOCK_BODY, "<block body>");
    r = CompoundStatement(b, l + 1);
    if (!r) r = AssemblerStatement(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // declSection* BlockBody
  public static boolean BlockGlobal(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "BlockGlobal")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, BLOCK_GLOBAL, "<block global>");
    r = BlockGlobal_0(b, l + 1);
    r = r && BlockBody(b, l + 1);
    exit_section_(b, l, m, r, false, PascalParser::rec_block_global_end);
    return r;
  }

  // declSection*
  private static boolean BlockGlobal_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "BlockGlobal_0")) return false;
    while (true) {
      int c = current_position_(b);
      if (!declSection(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "BlockGlobal_0", c)) break;
    }
    return true;
  }

  /* ********************************************************** */
  // [';'] declSectionNested* BlockBody
  public static boolean BlockLocal(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "BlockLocal")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, BLOCK_LOCAL, "<block local>");
    r = BlockLocal_0(b, l + 1);
    r = r && BlockLocal_1(b, l + 1);
    r = r && BlockBody(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // [';']
  private static boolean BlockLocal_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "BlockLocal_0")) return false;
    consumeToken(b, SEMI);
    return true;
  }

  // declSectionNested*
  private static boolean BlockLocal_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "BlockLocal_1")) return false;
    while (true) {
      int c = current_position_(b);
      if (!declSectionNested(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "BlockLocal_1", c)) break;
    }
    return true;
  }

  /* ********************************************************** */
  // [';'] declSectionNested1* BlockBody
  public static boolean BlockLocalNested1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "BlockLocalNested1")) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, BLOCK_LOCAL_NESTED_1, "<block local nested 1>");
    r = BlockLocalNested1_0(b, l + 1);
    r = r && BlockLocalNested1_1(b, l + 1);
    p = r; // pin = 2
    r = r && BlockBody(b, l + 1);
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  // [';']
  private static boolean BlockLocalNested1_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "BlockLocalNested1_0")) return false;
    consumeToken(b, SEMI);
    return true;
  }

  // declSectionNested1*
  private static boolean BlockLocalNested1_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "BlockLocalNested1_1")) return false;
    while (true) {
      int c = current_position_(b);
      if (!declSectionNested1(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "BlockLocalNested1_1", c)) break;
    }
    return true;
  }

  /* ********************************************************** */
  // [';'] declSectionWONested* BlockBody
  public static boolean BlockLocalWONested(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "BlockLocalWONested")) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, BLOCK_LOCAL_WO_NESTED, "<block local wo nested>");
    r = BlockLocalWONested_0(b, l + 1);
    r = r && BlockLocalWONested_1(b, l + 1);
    p = r; // pin = 2
    r = r && BlockBody(b, l + 1);
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  // [';']
  private static boolean BlockLocalWONested_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "BlockLocalWONested_0")) return false;
    consumeToken(b, SEMI);
    return true;
  }

  // declSectionWONested*
  private static boolean BlockLocalWONested_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "BlockLocalWONested_1")) return false;
    while (true) {
      int c = current_position_(b);
      if (!declSectionWONested(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "BlockLocalWONested_1", c)) break;
    }
    return true;
  }

  /* ********************************************************** */
  // BREAK
  public static boolean BreakStatement(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "BreakStatement")) return false;
    if (!nextTokenIs(b, BREAK)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, BREAK);
    exit_section_(b, m, BREAK_STATEMENT, r);
    return r;
  }

  /* ********************************************************** */
  // ELSE statementList
  public static boolean CaseElse(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "CaseElse")) return false;
    if (!nextTokenIs(b, ELSE)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, CASE_ELSE, null);
    r = consumeToken(b, ELSE);
    p = r; // pin = 1
    r = r && statementList(b, l + 1);
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  /* ********************************************************** */
  // caseLabel Statement [";"]
  public static boolean CaseItem(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "CaseItem")) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, CASE_ITEM, "<case item>");
    r = caseLabel(b, l + 1);
    p = r; // pin = 1
    r = r && report_error_(b, Statement(b, l + 1));
    r = p && CaseItem_2(b, l + 1) && r;
    exit_section_(b, l, m, r, p, PascalParser::rec_caseItem);
    return r || p;
  }

  // [";"]
  private static boolean CaseItem_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "CaseItem_2")) return false;
    consumeToken(b, SEMI);
    return true;
  }

  /* ********************************************************** */
  // caseBody [CaseElse] END
  public static boolean CaseStatement(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "CaseStatement")) return false;
    if (!nextTokenIs(b, CASE)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = caseBody(b, l + 1);
    r = r && CaseStatement_1(b, l + 1);
    r = r && consumeToken(b, END);
    exit_section_(b, m, CASE_STATEMENT, r);
    return r;
  }

  // [CaseElse]
  private static boolean CaseStatement_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "CaseStatement_1")) return false;
    CaseElse(b, l + 1);
    return true;
  }

  /* ********************************************************** */
  // field
  public static boolean ClassField(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ClassField")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, CLASS_FIELD, "<class field>");
    r = field(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // CLASS HELPER classHelperBody END
  public static boolean ClassHelperDecl(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ClassHelperDecl")) return false;
    if (!nextTokenIs(b, "<class helper declaration>", CLASS)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, CLASS_HELPER_DECL, "<class helper declaration>");
    r = consumeTokens(b, 2, CLASS, HELPER);
    p = r; // pin = 2
    r = r && report_error_(b, classHelperBody(b, l + 1));
    r = p && consumeToken(b, END) && r;
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  /* ********************************************************** */
  // [CLASS] procKey GenericTypeIdent "." identifier "=" RefNamedIdent ";"
  public static boolean ClassMethodResolution(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ClassMethodResolution")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, CLASS_METHOD_RESOLUTION, "<class method resolution>");
    r = ClassMethodResolution_0(b, l + 1);
    r = r && procKey(b, l + 1);
    r = r && GenericTypeIdent(b, l + 1);
    r = r && consumeToken(b, DOT);
    r = r && identifier(b, l + 1);
    r = r && consumeToken(b, EQ);
    r = r && RefNamedIdent(b, l + 1);
    r = r && consumeToken(b, SEMI);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // [CLASS]
  private static boolean ClassMethodResolution_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ClassMethodResolution_0")) return false;
    consumeToken(b, CLASS);
    return true;
  }

  /* ********************************************************** */
  // "(" TypeID? classParentRest ")"
  public static boolean ClassParent(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ClassParent")) return false;
    if (!nextTokenIs(b, LPAREN)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, CLASS_PARENT, null);
    r = consumeToken(b, LPAREN);
    p = r; // pin = 1
    r = r && report_error_(b, ClassParent_1(b, l + 1));
    r = p && report_error_(b, classParentRest(b, l + 1)) && r;
    r = p && consumeToken(b, RPAREN) && r;
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  // TypeID?
  private static boolean ClassParent_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ClassParent_1")) return false;
    TypeID(b, l + 1);
    return true;
  }

  /* ********************************************************** */
  // customAttributes* [CLASS] PROPERTY NamedIdentDecl [ClassPropertyArray] [":" TypeID] [ClassPropertyIndex] ClassPropertySpecifier* ";" [DEFAULT] hintingDirective* [";"]
  public static boolean ClassProperty(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ClassProperty")) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, CLASS_PROPERTY, "<class property>");
    r = ClassProperty_0(b, l + 1);
    r = r && ClassProperty_1(b, l + 1);
    r = r && consumeToken(b, PROPERTY);
    p = r; // pin = 3
    r = r && report_error_(b, NamedIdentDecl(b, l + 1));
    r = p && report_error_(b, ClassProperty_4(b, l + 1)) && r;
    r = p && report_error_(b, ClassProperty_5(b, l + 1)) && r;
    r = p && report_error_(b, ClassProperty_6(b, l + 1)) && r;
    r = p && report_error_(b, ClassProperty_7(b, l + 1)) && r;
    r = p && report_error_(b, consumeToken(b, SEMI)) && r;
    r = p && report_error_(b, ClassProperty_9(b, l + 1)) && r;
    r = p && report_error_(b, ClassProperty_10(b, l + 1)) && r;
    r = p && ClassProperty_11(b, l + 1) && r;
    exit_section_(b, l, m, r, p, PascalParser::rec_routine_decl);
    return r || p;
  }

  // customAttributes*
  private static boolean ClassProperty_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ClassProperty_0")) return false;
    while (true) {
      int c = current_position_(b);
      if (!customAttributes(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "ClassProperty_0", c)) break;
    }
    return true;
  }

  // [CLASS]
  private static boolean ClassProperty_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ClassProperty_1")) return false;
    consumeToken(b, CLASS);
    return true;
  }

  // [ClassPropertyArray]
  private static boolean ClassProperty_4(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ClassProperty_4")) return false;
    ClassPropertyArray(b, l + 1);
    return true;
  }

  // [":" TypeID]
  private static boolean ClassProperty_5(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ClassProperty_5")) return false;
    ClassProperty_5_0(b, l + 1);
    return true;
  }

  // ":" TypeID
  private static boolean ClassProperty_5_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ClassProperty_5_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, COLON);
    r = r && TypeID(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // [ClassPropertyIndex]
  private static boolean ClassProperty_6(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ClassProperty_6")) return false;
    ClassPropertyIndex(b, l + 1);
    return true;
  }

  // ClassPropertySpecifier*
  private static boolean ClassProperty_7(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ClassProperty_7")) return false;
    while (true) {
      int c = current_position_(b);
      if (!ClassPropertySpecifier(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "ClassProperty_7", c)) break;
    }
    return true;
  }

  // [DEFAULT]
  private static boolean ClassProperty_9(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ClassProperty_9")) return false;
    consumeToken(b, DEFAULT);
    return true;
  }

  // hintingDirective*
  private static boolean ClassProperty_10(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ClassProperty_10")) return false;
    while (true) {
      int c = current_position_(b);
      if (!hintingDirective(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "ClassProperty_10", c)) break;
    }
    return true;
  }

  // [";"]
  private static boolean ClassProperty_11(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ClassProperty_11")) return false;
    consumeToken(b, SEMI);
    return true;
  }

  /* ********************************************************** */
  // "[" formalParameterList "]"
  public static boolean ClassPropertyArray(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ClassPropertyArray")) return false;
    if (!nextTokenIs(b, LBRACK)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, CLASS_PROPERTY_ARRAY, null);
    r = consumeToken(b, LBRACK);
    p = r; // pin = 1
    r = r && report_error_(b, formalParameterList(b, l + 1));
    r = p && consumeToken(b, RBRACK) && r;
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  /* ********************************************************** */
  // INDEX ConstExpressionOrd
  public static boolean ClassPropertyIndex(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ClassPropertyIndex")) return false;
    if (!nextTokenIs(b, INDEX)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, CLASS_PROPERTY_INDEX, null);
    r = consumeToken(b, INDEX);
    p = r; // pin = 1
    r = r && ConstExpressionOrd(b, l + 1);
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  /* ********************************************************** */
  // classPropertyReadWrite | classPropertyDispInterface | "stored" Expression
  //                          | DEFAULT Expression | "nodefault" | IMPLEMENTS TypeID
  public static boolean ClassPropertySpecifier(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ClassPropertySpecifier")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, CLASS_PROPERTY_SPECIFIER, "<class property specifier>");
    r = classPropertyReadWrite(b, l + 1);
    if (!r) r = classPropertyDispInterface(b, l + 1);
    if (!r) r = ClassPropertySpecifier_2(b, l + 1);
    if (!r) r = ClassPropertySpecifier_3(b, l + 1);
    if (!r) r = consumeToken(b, "nodefault");
    if (!r) r = ClassPropertySpecifier_5(b, l + 1);
    exit_section_(b, l, m, r, false, PascalParser::rec_propspec);
    return r;
  }

  // "stored" Expression
  private static boolean ClassPropertySpecifier_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ClassPropertySpecifier_2")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, "stored");
    r = r && Expression(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // DEFAULT Expression
  private static boolean ClassPropertySpecifier_3(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ClassPropertySpecifier_3")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, DEFAULT);
    r = r && Expression(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // IMPLEMENTS TypeID
  private static boolean ClassPropertySpecifier_5(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ClassPropertySpecifier_5")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, IMPLEMENTS);
    r = r && TypeID(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // (SubIdent [GenericPostfix] ".")+ SubIdent
  public static boolean ClassQualifiedIdent(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ClassQualifiedIdent")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, CLASS_QUALIFIED_IDENT, "<Identifier>");
    r = ClassQualifiedIdent_0(b, l + 1);
    r = r && SubIdent(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // (SubIdent [GenericPostfix] ".")+
  private static boolean ClassQualifiedIdent_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ClassQualifiedIdent_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = ClassQualifiedIdent_0_0(b, l + 1);
    while (r) {
      int c = current_position_(b);
      if (!ClassQualifiedIdent_0_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "ClassQualifiedIdent_0", c)) break;
    }
    exit_section_(b, m, null, r);
    return r;
  }

  // SubIdent [GenericPostfix] "."
  private static boolean ClassQualifiedIdent_0_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ClassQualifiedIdent_0_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = SubIdent(b, l + 1);
    r = r && ClassQualifiedIdent_0_0_1(b, l + 1);
    r = r && consumeToken(b, DOT);
    exit_section_(b, m, null, r);
    return r;
  }

  // [GenericPostfix]
  private static boolean ClassQualifiedIdent_0_0_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ClassQualifiedIdent_0_0_1")) return false;
    GenericPostfix(b, l + 1);
    return true;
  }

  /* ********************************************************** */
  // SEALED | ABSTRACT
  public static boolean ClassState(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ClassState")) return false;
    if (!nextTokenIs(b, "<class state>", ABSTRACT, SEALED)) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, CLASS_STATE, "<class state>");
    r = consumeToken(b, SEALED);
    if (!r) r = consumeToken(b, ABSTRACT);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // [PACKED] classDecl
  public static boolean ClassTypeDecl(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ClassTypeDecl")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, CLASS_TYPE_DECL, "<class declaration>");
    r = ClassTypeDecl_0(b, l + 1);
    r = r && classDecl(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // [PACKED]
  private static boolean ClassTypeDecl_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ClassTypeDecl_0")) return false;
    consumeToken(b, PACKED);
    return true;
  }

  /* ********************************************************** */
  // CLASS OF TypeID
  public static boolean ClassTypeTypeDecl(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ClassTypeTypeDecl")) return false;
    if (!nextTokenIs(b, "<metaclass declaration>", CLASS)) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, CLASS_TYPE_TYPE_DECL, "<metaclass declaration>");
    r = consumeTokens(b, 0, CLASS, OF);
    r = r && TypeID(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // (funcHeading | procHeading) BlockLocal
  public static boolean ClosureRoutine(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ClosureRoutine")) return false;
    if (!nextTokenIs(b, "<closure routine>", FUNCTION, PROCEDURE)) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, CLOSURE_ROUTINE, "<closure routine>");
    r = ClosureRoutine_0(b, l + 1);
    r = r && BlockLocal(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // funcHeading | procHeading
  private static boolean ClosureRoutine_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ClosureRoutine_0")) return false;
    boolean r;
    r = funcHeading(b, l + 1);
    if (!r) r = procHeading(b, l + 1);
    return r;
  }

  /* ********************************************************** */
  // ':' Expr [':' Expr]
  public static boolean ColonConstruct(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ColonConstruct")) return false;
    if (!nextTokenIs(b, COLON)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, COLON);
    r = r && Expr(b, l + 1, -1);
    r = r && ColonConstruct_2(b, l + 1);
    exit_section_(b, m, COLON_CONSTRUCT, r);
    return r;
  }

  // [':' Expr]
  private static boolean ColonConstruct_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ColonConstruct_2")) return false;
    ColonConstruct_2_0(b, l + 1);
    return true;
  }

  // ':' Expr
  private static boolean ColonConstruct_2_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ColonConstruct_2_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, COLON);
    r = r && Expr(b, l + 1, -1);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // BEGIN statementBlock END
  public static boolean CompoundStatement(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "CompoundStatement")) return false;
    if (!nextTokenIs(b, BEGIN)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, COMPOUND_STATEMENT, null);
    r = consumeToken(b, BEGIN);
    p = r; // pin = 1
    r = r && report_error_(b, statementBlock(b, l + 1));
    r = p && consumeToken(b, END) && r;
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  /* ********************************************************** */
  // customAttributes* NamedIdentDecl [":" TypeDecl] "=" ConstExpression hintingDirective* ";"
  public static boolean ConstDeclaration(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ConstDeclaration")) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, CONST_DECLARATION, "<const declaration>");
    r = ConstDeclaration_0(b, l + 1);
    r = r && NamedIdentDecl(b, l + 1);
    p = r; // pin = 2
    r = r && report_error_(b, ConstDeclaration_2(b, l + 1));
    r = p && report_error_(b, consumeToken(b, EQ)) && r;
    r = p && report_error_(b, ConstExpression(b, l + 1)) && r;
    r = p && report_error_(b, ConstDeclaration_5(b, l + 1)) && r;
    r = p && consumeToken(b, SEMI) && r;
    exit_section_(b, l, m, r, p, PascalParser::rec_name);
    return r || p;
  }

  // customAttributes*
  private static boolean ConstDeclaration_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ConstDeclaration_0")) return false;
    while (true) {
      int c = current_position_(b);
      if (!customAttributes(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "ConstDeclaration_0", c)) break;
    }
    return true;
  }

  // [":" TypeDecl]
  private static boolean ConstDeclaration_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ConstDeclaration_2")) return false;
    ConstDeclaration_2_0(b, l + 1);
    return true;
  }

  // ":" TypeDecl
  private static boolean ConstDeclaration_2_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ConstDeclaration_2_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, COLON);
    r = r && TypeDecl(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // hintingDirective*
  private static boolean ConstDeclaration_5(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ConstDeclaration_5")) return false;
    while (true) {
      int c = current_position_(b);
      if (!hintingDirective(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "ConstDeclaration_5", c)) break;
    }
    return true;
  }

  /* ********************************************************** */
  // constExpr
  public static boolean ConstExpression(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ConstExpression")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, CONST_EXPRESSION, "<constant expression>");
    r = constExpr(b, l + 1);
    exit_section_(b, l, m, r, false, PascalParser::rec_expr_colon);
    return r;
  }

  /* ********************************************************** */
  // expressionOrd
  public static boolean ConstExpressionOrd(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ConstExpressionOrd")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, CONST_EXPRESSION_ORD, "<integer constant expression>");
    r = expressionOrd(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // constKey constDeclarations [";"]
  public static boolean ConstSection(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ConstSection")) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, CONST_SECTION, "<const section>");
    r = constKey(b, l + 1);
    p = r; // pin = 1
    r = r && report_error_(b, constDeclarations(b, l + 1));
    r = p && ConstSection_2(b, l + 1) && r;
    exit_section_(b, l, m, r, p, PascalParser::rec_decl_section);
    return r || p;
  }

  // [";"]
  private static boolean ConstSection_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ConstSection_2")) return false;
    consumeToken(b, SEMI);
    return true;
  }

  /* ********************************************************** */
  // typeParamIdentList [ ":" genericConstraintList]
  public static boolean ConstrainedTypeParam(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ConstrainedTypeParam")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, CONSTRAINED_TYPE_PARAM, "<constrained type param>");
    r = typeParamIdentList(b, l + 1);
    r = r && ConstrainedTypeParam_1(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // [ ":" genericConstraintList]
  private static boolean ConstrainedTypeParam_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ConstrainedTypeParam_1")) return false;
    ConstrainedTypeParam_1_0(b, l + 1);
    return true;
  }

  // ":" genericConstraintList
  private static boolean ConstrainedTypeParam_1_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ConstrainedTypeParam_1_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, COLON);
    r = r && genericConstraintList(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // CONTAINS namespaceNameList ";"
  public static boolean ContainsClause(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ContainsClause")) return false;
    if (!nextTokenIs(b, CONTAINS)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, CONTAINS_CLAUSE, null);
    r = consumeToken(b, CONTAINS);
    p = r; // pin = 1
    r = r && report_error_(b, namespaceNameList(b, l + 1));
    r = p && consumeToken(b, SEMI) && r;
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  /* ********************************************************** */
  // CONTINUE
  public static boolean ContinueStatement(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ContinueStatement")) return false;
    if (!nextTokenIs(b, CONTINUE)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, CONTINUE);
    exit_section_(b, m, CONTINUE_STATEMENT, r);
    return r;
  }

  /* ********************************************************** */
  // FullyQualifiedIdent [LPAREN AttributeParamList RPAREN]
  public static boolean CustomAttributeDecl(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "CustomAttributeDecl")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, CUSTOM_ATTRIBUTE_DECL, "<custom attribute decl>");
    r = FullyQualifiedIdent(b, l + 1);
    r = r && CustomAttributeDecl_1(b, l + 1);
    exit_section_(b, l, m, r, false, PascalParser::rec_attr);
    return r;
  }

  // [LPAREN AttributeParamList RPAREN]
  private static boolean CustomAttributeDecl_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "CustomAttributeDecl_1")) return false;
    CustomAttributeDecl_1_0(b, l + 1);
    return true;
  }

  // LPAREN AttributeParamList RPAREN
  private static boolean CustomAttributeDecl_1_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "CustomAttributeDecl_1_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, LPAREN);
    r = r && AttributeParamList(b, l + 1);
    r = r && consumeToken(b, RPAREN);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // CustomAttributeDecl (COMMA CustomAttributeDecl)*
  static boolean CustomAttributeList(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "CustomAttributeList")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_);
    r = CustomAttributeDecl(b, l + 1);
    r = r && CustomAttributeList_1(b, l + 1);
    exit_section_(b, l, m, r, false, PascalParser::rec_attr2);
    return r;
  }

  // (COMMA CustomAttributeDecl)*
  private static boolean CustomAttributeList_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "CustomAttributeList_1")) return false;
    while (true) {
      int c = current_position_(b);
      if (!CustomAttributeList_1_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "CustomAttributeList_1", c)) break;
    }
    return true;
  }

  // COMMA CustomAttributeDecl
  private static boolean CustomAttributeList_1_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "CustomAttributeList_1_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, COMMA);
    r = r && CustomAttributeDecl(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // "(" enumEl enumRest* ")"
  public static boolean EnumType(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "EnumType")) return false;
    if (!nextTokenIs(b, LPAREN)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, ENUM_TYPE, null);
    r = consumeToken(b, LPAREN);
    p = r; // pin = 1
    r = r && report_error_(b, enumEl(b, l + 1));
    r = p && report_error_(b, EnumType_2(b, l + 1)) && r;
    r = p && consumeToken(b, RPAREN) && r;
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  // enumRest*
  private static boolean EnumType_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "EnumType_2")) return false;
    while (true) {
      int c = current_position_(b);
      if (!enumRest(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "EnumType_2", c)) break;
    }
    return true;
  }

  /* ********************************************************** */
  // PROGRAM_ | UNIT_ | LIBRARY_ | INTERFACE_ | IMPLEMENTATION_ | INITIALIZATION_ | FINALIZATION_
  //                               | EXPORTS_ | USES_ | VAR_ | CONST_ | TYPE_ | THREADVAR_ | RESOURCESTRING_ | CONSTREF_ | ABSOLUTE_
  //                               | PROCEDURE_ | FUNCTION_ | OPERATOR_ | CONSTRUCTOR_ | DESTRUCTOR_ | STRICT_ | PRIVATE_ | PROTECTED_ | PUBLIC_ | PUBLISHED_
  //                               | ARRAY_ | RECORD_ | SET_ | FILE_ | OBJECT_ | CLASS_ | OF_ | PROPERTY_ | LABEL_
  //                               | TRY_ | RAISE_ | EXCEPT_ | FINALLY_ | ON_ | GOTO_
  //                               | FOR_ | TO_ | DOWNTO_ | REPEAT_ | UNTIL_ | WHILE_ | DO_ | WITH_ | BEGIN_ | END_ | IF_ | THEN_ | ELSE_ | CASE_
  //                               | NIL_ | FALSE_ | TRUE_ | ASM_ | INHERITED_
  //                               | AND_ | OR_ | XOR_ | NOT_ | SHL_ | SHR_ | DIV_ | MOD_ | IN_ | AS_ | IS_
  //                               | INLINE_
  //                               | OUT_ | SELF_ | NEW_
  //                               | EXIT_ | BREAK_ | CONTINUE_
  //                               | VIRTUAL_ | DYNAMIC_ | ABSTRACT_ | OVERLOAD_ | OVERRIDE_ | REINTRODUCE_ | MESSAGE_ | STATIC_ | SEALED_ | FINAL_ | ASSEMBLER_
  //                               | CDECL_ | PASCAL_ | REGISTER_ | SAFECALL_ | STDCALL_ | EXPORT_
  //                               | AUTOMATED_ | DISPID_ | EXTERNAL_ | FORWARD_ | HELPER_ | IMPLEMENTS_
  //                               | DEFAULT_ | INDEX_ | READ_ | WRITE_ | DEPRECATED_ | EXPERIMENTAL_ | PLATFORM_ | REFERENCE_
  //                               | PACKAGE_ | CONTAINS_ | REQUIRES_
  //                               | NAME_
  public static boolean EscapedIdent(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "EscapedIdent")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, ESCAPED_IDENT, "<Identifier>");
    r = consumeToken(b, PROGRAM_);
    if (!r) r = consumeToken(b, UNIT_);
    if (!r) r = consumeToken(b, LIBRARY_);
    if (!r) r = consumeToken(b, INTERFACE_);
    if (!r) r = consumeToken(b, IMPLEMENTATION_);
    if (!r) r = consumeToken(b, INITIALIZATION_);
    if (!r) r = consumeToken(b, FINALIZATION_);
    if (!r) r = consumeToken(b, EXPORTS_);
    if (!r) r = consumeToken(b, USES_);
    if (!r) r = consumeToken(b, VAR_);
    if (!r) r = consumeToken(b, CONST_);
    if (!r) r = consumeToken(b, TYPE_);
    if (!r) r = consumeToken(b, THREADVAR_);
    if (!r) r = consumeToken(b, RESOURCESTRING_);
    if (!r) r = consumeToken(b, CONSTREF_);
    if (!r) r = consumeToken(b, ABSOLUTE_);
    if (!r) r = consumeToken(b, PROCEDURE_);
    if (!r) r = consumeToken(b, FUNCTION_);
    if (!r) r = consumeToken(b, OPERATOR_);
    if (!r) r = consumeToken(b, CONSTRUCTOR_);
    if (!r) r = consumeToken(b, DESTRUCTOR_);
    if (!r) r = consumeToken(b, STRICT_);
    if (!r) r = consumeToken(b, PRIVATE_);
    if (!r) r = consumeToken(b, PROTECTED_);
    if (!r) r = consumeToken(b, PUBLIC_);
    if (!r) r = consumeToken(b, PUBLISHED_);
    if (!r) r = consumeToken(b, ARRAY_);
    if (!r) r = consumeToken(b, RECORD_);
    if (!r) r = consumeToken(b, SET_);
    if (!r) r = consumeToken(b, FILE_);
    if (!r) r = consumeToken(b, OBJECT_);
    if (!r) r = consumeToken(b, CLASS_);
    if (!r) r = consumeToken(b, OF_);
    if (!r) r = consumeToken(b, PROPERTY_);
    if (!r) r = consumeToken(b, LABEL_);
    if (!r) r = consumeToken(b, TRY_);
    if (!r) r = consumeToken(b, RAISE_);
    if (!r) r = consumeToken(b, EXCEPT_);
    if (!r) r = consumeToken(b, FINALLY_);
    if (!r) r = consumeToken(b, ON_);
    if (!r) r = consumeToken(b, GOTO_);
    if (!r) r = consumeToken(b, FOR_);
    if (!r) r = consumeToken(b, TO_);
    if (!r) r = consumeToken(b, DOWNTO_);
    if (!r) r = consumeToken(b, REPEAT_);
    if (!r) r = consumeToken(b, UNTIL_);
    if (!r) r = consumeToken(b, WHILE_);
    if (!r) r = consumeToken(b, DO_);
    if (!r) r = consumeToken(b, WITH_);
    if (!r) r = consumeToken(b, BEGIN_);
    if (!r) r = consumeToken(b, END_);
    if (!r) r = consumeToken(b, IF_);
    if (!r) r = consumeToken(b, THEN_);
    if (!r) r = consumeToken(b, ELSE_);
    if (!r) r = consumeToken(b, CASE_);
    if (!r) r = consumeToken(b, NIL_);
    if (!r) r = consumeToken(b, FALSE_);
    if (!r) r = consumeToken(b, TRUE_);
    if (!r) r = consumeToken(b, ASM_);
    if (!r) r = consumeToken(b, INHERITED_);
    if (!r) r = consumeToken(b, AND_);
    if (!r) r = consumeToken(b, OR_);
    if (!r) r = consumeToken(b, XOR_);
    if (!r) r = consumeToken(b, NOT_);
    if (!r) r = consumeToken(b, SHL_);
    if (!r) r = consumeToken(b, SHR_);
    if (!r) r = consumeToken(b, DIV_);
    if (!r) r = consumeToken(b, MOD_);
    if (!r) r = consumeToken(b, IN_);
    if (!r) r = consumeToken(b, AS_);
    if (!r) r = consumeToken(b, IS_);
    if (!r) r = consumeToken(b, INLINE_);
    if (!r) r = consumeToken(b, OUT_);
    if (!r) r = consumeToken(b, SELF_);
    if (!r) r = consumeToken(b, NEW_);
    if (!r) r = consumeToken(b, EXIT_);
    if (!r) r = consumeToken(b, BREAK_);
    if (!r) r = consumeToken(b, CONTINUE_);
    if (!r) r = consumeToken(b, VIRTUAL_);
    if (!r) r = consumeToken(b, DYNAMIC_);
    if (!r) r = consumeToken(b, ABSTRACT_);
    if (!r) r = consumeToken(b, OVERLOAD_);
    if (!r) r = consumeToken(b, OVERRIDE_);
    if (!r) r = consumeToken(b, REINTRODUCE_);
    if (!r) r = consumeToken(b, MESSAGE_);
    if (!r) r = consumeToken(b, STATIC_);
    if (!r) r = consumeToken(b, SEALED_);
    if (!r) r = consumeToken(b, FINAL_);
    if (!r) r = consumeToken(b, ASSEMBLER_);
    if (!r) r = consumeToken(b, CDECL_);
    if (!r) r = consumeToken(b, PASCAL_);
    if (!r) r = consumeToken(b, REGISTER_);
    if (!r) r = consumeToken(b, SAFECALL_);
    if (!r) r = consumeToken(b, STDCALL_);
    if (!r) r = consumeToken(b, EXPORT_);
    if (!r) r = consumeToken(b, AUTOMATED_);
    if (!r) r = consumeToken(b, DISPID_);
    if (!r) r = consumeToken(b, EXTERNAL_);
    if (!r) r = consumeToken(b, FORWARD_);
    if (!r) r = consumeToken(b, HELPER_);
    if (!r) r = consumeToken(b, IMPLEMENTS_);
    if (!r) r = consumeToken(b, DEFAULT_);
    if (!r) r = consumeToken(b, INDEX_);
    if (!r) r = consumeToken(b, READ_);
    if (!r) r = consumeToken(b, WRITE_);
    if (!r) r = consumeToken(b, DEPRECATED_);
    if (!r) r = consumeToken(b, EXPERIMENTAL_);
    if (!r) r = consumeToken(b, PLATFORM_);
    if (!r) r = consumeToken(b, REFERENCE_);
    if (!r) r = consumeToken(b, PACKAGE_);
    if (!r) r = consumeToken(b, CONTAINS_);
    if (!r) r = consumeToken(b, REQUIRES_);
    if (!r) r = consumeToken(b, NAME_);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // EXIT [ "(" [Expression] ")" ]
  public static boolean ExitStatement(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ExitStatement")) return false;
    if (!nextTokenIs(b, EXIT)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, EXIT);
    r = r && ExitStatement_1(b, l + 1);
    exit_section_(b, m, EXIT_STATEMENT, r);
    return r;
  }

  // [ "(" [Expression] ")" ]
  private static boolean ExitStatement_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ExitStatement_1")) return false;
    ExitStatement_1_0(b, l + 1);
    return true;
  }

  // "(" [Expression] ")"
  private static boolean ExitStatement_1_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ExitStatement_1_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, LPAREN);
    r = r && ExitStatement_1_0_1(b, l + 1);
    r = r && consumeToken(b, RPAREN);
    exit_section_(b, m, null, r);
    return r;
  }

  // [Expression]
  private static boolean ExitStatement_1_0_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ExitStatement_1_0_1")) return false;
    Expression(b, l + 1);
    return true;
  }

  /* ********************************************************** */
  // routineDeclaration [ExternalDirective]
  public static boolean ExportedRoutine(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ExportedRoutine")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, EXPORTED_ROUTINE, "<exported routine>");
    r = routineDeclaration(b, l + 1);
    r = r && ExportedRoutine_1(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // [ExternalDirective]
  private static boolean ExportedRoutine_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ExportedRoutine_1")) return false;
    ExternalDirective(b, l + 1);
    return true;
  }

  /* ********************************************************** */
  // EXPORTS RefNamedIdent exportItem ("," RefNamedIdent exportItem)* ";"
  public static boolean ExportsSection(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ExportsSection")) return false;
    if (!nextTokenIs(b, EXPORTS)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, EXPORTS_SECTION, null);
    r = consumeToken(b, EXPORTS);
    p = r; // pin = 1
    r = r && report_error_(b, RefNamedIdent(b, l + 1));
    r = p && report_error_(b, exportItem(b, l + 1)) && r;
    r = p && report_error_(b, ExportsSection_3(b, l + 1)) && r;
    r = p && consumeToken(b, SEMI) && r;
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  // ("," RefNamedIdent exportItem)*
  private static boolean ExportsSection_3(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ExportsSection_3")) return false;
    while (true) {
      int c = current_position_(b);
      if (!ExportsSection_3_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "ExportsSection_3", c)) break;
    }
    return true;
  }

  // "," RefNamedIdent exportItem
  private static boolean ExportsSection_3_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ExportsSection_3_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, COMMA);
    r = r && RefNamedIdent(b, l + 1);
    r = r && exportItem(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // Expr
  public static boolean Expression(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "Expression")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, EXPRESSION, "<expression>");
    r = Expr(b, l + 1, -1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // EXTERNAL ";" | EXTERNAL externalSpecifier [libLoadSpec] ";" | EXTERNAL ConstExpression [externalSpecifier] [libLoadSpec] ";"
  public static boolean ExternalDirective(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ExternalDirective")) return false;
    if (!nextTokenIs(b, EXTERNAL)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = parseTokens(b, 0, EXTERNAL, SEMI);
    if (!r) r = ExternalDirective_1(b, l + 1);
    if (!r) r = ExternalDirective_2(b, l + 1);
    exit_section_(b, m, EXTERNAL_DIRECTIVE, r);
    return r;
  }

  // EXTERNAL externalSpecifier [libLoadSpec] ";"
  private static boolean ExternalDirective_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ExternalDirective_1")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, EXTERNAL);
    r = r && externalSpecifier(b, l + 1);
    r = r && ExternalDirective_1_2(b, l + 1);
    r = r && consumeToken(b, SEMI);
    exit_section_(b, m, null, r);
    return r;
  }

  // [libLoadSpec]
  private static boolean ExternalDirective_1_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ExternalDirective_1_2")) return false;
    libLoadSpec(b, l + 1);
    return true;
  }

  // EXTERNAL ConstExpression [externalSpecifier] [libLoadSpec] ";"
  private static boolean ExternalDirective_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ExternalDirective_2")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, EXTERNAL);
    r = r && ConstExpression(b, l + 1);
    r = r && ExternalDirective_2_2(b, l + 1);
    r = r && ExternalDirective_2_3(b, l + 1);
    r = r && consumeToken(b, SEMI);
    exit_section_(b, m, null, r);
    return r;
  }

  // [externalSpecifier]
  private static boolean ExternalDirective_2_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ExternalDirective_2_2")) return false;
    externalSpecifier(b, l + 1);
    return true;
  }

  // [libLoadSpec]
  private static boolean ExternalDirective_2_3(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ExternalDirective_2_3")) return false;
    libLoadSpec(b, l + 1);
    return true;
  }

  /* ********************************************************** */
  // FILE [OF TypeDecl]
  public static boolean FileType(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "FileType")) return false;
    if (!nextTokenIs(b, FILE)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, FILE_TYPE, null);
    r = consumeToken(b, FILE);
    p = r; // pin = 1
    r = r && FileType_1(b, l + 1);
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  // [OF TypeDecl]
  private static boolean FileType_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "FileType_1")) return false;
    FileType_1_0(b, l + 1);
    return true;
  }

  // OF TypeDecl
  private static boolean FileType_1_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "FileType_1_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, OF);
    r = r && TypeDecl(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // VAR NamedIdent [":" TypeDecl]
  public static boolean ForInlineDeclaration(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ForInlineDeclaration")) return false;
    if (!nextTokenIs(b, VAR)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, FOR_INLINE_DECLARATION, null);
    r = consumeToken(b, VAR);
    p = r; // pin = 1
    r = r && report_error_(b, NamedIdent(b, l + 1));
    r = p && ForInlineDeclaration_2(b, l + 1) && r;
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  // [":" TypeDecl]
  private static boolean ForInlineDeclaration_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ForInlineDeclaration_2")) return false;
    ForInlineDeclaration_2_0(b, l + 1);
    return true;
  }

  // ":" TypeDecl
  private static boolean ForInlineDeclaration_2_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ForInlineDeclaration_2_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, COLON);
    r = r && TypeDecl(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // FOR forCycleIdent forCycleCondition doStatement
  public static boolean ForStatement(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ForStatement")) return false;
    if (!nextTokenIs(b, FOR)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, FOR_STATEMENT, null);
    r = consumeToken(b, FOR);
    p = r; // pin = 1
    r = r && report_error_(b, forCycleIdent(b, l + 1));
    r = p && report_error_(b, forCycleCondition(b, l + 1)) && r;
    r = p && doStatement(b, l + 1) && r;
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  /* ********************************************************** */
  // customAttributes* [ ParamType ] identList [ ":" TypeDecl ] [ "=" ConstExpression ]
  public static boolean FormalParameter(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "FormalParameter")) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, FORMAL_PARAMETER, "<formal parameter>");
    r = FormalParameter_0(b, l + 1);
    r = r && FormalParameter_1(b, l + 1);
    r = r && identList(b, l + 1);
    p = r; // pin = 3
    r = r && report_error_(b, FormalParameter_3(b, l + 1));
    r = p && FormalParameter_4(b, l + 1) && r;
    exit_section_(b, l, m, r, p, PascalParser::rec_formal_param);
    return r || p;
  }

  // customAttributes*
  private static boolean FormalParameter_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "FormalParameter_0")) return false;
    while (true) {
      int c = current_position_(b);
      if (!customAttributes(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "FormalParameter_0", c)) break;
    }
    return true;
  }

  // [ ParamType ]
  private static boolean FormalParameter_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "FormalParameter_1")) return false;
    ParamType(b, l + 1);
    return true;
  }

  // [ ":" TypeDecl ]
  private static boolean FormalParameter_3(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "FormalParameter_3")) return false;
    FormalParameter_3_0(b, l + 1);
    return true;
  }

  // ":" TypeDecl
  private static boolean FormalParameter_3_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "FormalParameter_3_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, COLON);
    r = r && TypeDecl(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // [ "=" ConstExpression ]
  private static boolean FormalParameter_4(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "FormalParameter_4")) return false;
    FormalParameter_4_0(b, l + 1);
    return true;
  }

  // "=" ConstExpression
  private static boolean FormalParameter_4_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "FormalParameter_4_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, EQ);
    r = r && ConstExpression(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // formalParameterSect
  public static boolean FormalParameterSection(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "FormalParameterSection")) return false;
    if (!nextTokenIs(b, LPAREN)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = formalParameterSect(b, l + 1);
    exit_section_(b, m, FORMAL_PARAMETER_SECTION, r);
    return r;
  }

  /* ********************************************************** */
  // Expression
  public static boolean FromExpression(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "FromExpression")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, FROM_EXPRESSION, "<from expression>");
    r = Expression(b, l + 1);
    exit_section_(b, l, m, r, false, PascalParser::rec_statement_mid);
    return r;
  }

  /* ********************************************************** */
  // namespacePart? SubIdent
  public static boolean FullyQualifiedIdent(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "FullyQualifiedIdent")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, FULLY_QUALIFIED_IDENT, "<Identifier>");
    r = FullyQualifiedIdent_0(b, l + 1);
    r = r && SubIdent(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // namespacePart?
  private static boolean FullyQualifiedIdent_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "FullyQualifiedIdent_0")) return false;
    namespacePart(b, l + 1);
    return true;
  }

  /* ********************************************************** */
  // functionDirective | dispIDDirective
  public static boolean FunctionDirective(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "FunctionDirective")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, FUNCTION_DIRECTIVE, "<routine directive>");
    r = functionDirective(b, l + 1);
    if (!r) r = dispIDDirective(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // TypeID | RECORD | CLASS | CONSTRUCTOR | INTERFACE | OBJECT
  public static boolean GenericConstraint(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "GenericConstraint")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, GENERIC_CONSTRAINT, "<generic constraint>");
    r = TypeID(b, l + 1);
    if (!r) r = consumeToken(b, RECORD);
    if (!r) r = consumeToken(b, CLASS);
    if (!r) r = consumeToken(b, CONSTRUCTOR);
    if (!r) r = consumeToken(b, INTERFACE);
    if (!r) r = consumeToken(b, OBJECT);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // "<" TypeDecl typeDeclsRest* ">"
  public static boolean GenericPostfix(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "GenericPostfix")) return false;
    if (!nextTokenIs(b, LT)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, LT);
    r = r && TypeDecl(b, l + 1);
    r = r && GenericPostfix_2(b, l + 1);
    r = r && consumeToken(b, GT);
    exit_section_(b, m, GENERIC_POSTFIX, r);
    return r;
  }

  // typeDeclsRest*
  private static boolean GenericPostfix_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "GenericPostfix_2")) return false;
    while (true) {
      int c = current_position_(b);
      if (!typeDeclsRest(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "GenericPostfix_2", c)) break;
    }
    return true;
  }

  /* ********************************************************** */
  // [GENERIC] NamedIdentDecl [genericDefinition]
  public static boolean GenericTypeIdent(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "GenericTypeIdent")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, GENERIC_TYPE_IDENT, "<Identifier>");
    r = GenericTypeIdent_0(b, l + 1);
    r = r && NamedIdentDecl(b, l + 1);
    r = r && GenericTypeIdent_2(b, l + 1);
    exit_section_(b, l, m, r, false, PascalParser::rec_typeId);
    return r;
  }

  // [GENERIC]
  private static boolean GenericTypeIdent_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "GenericTypeIdent_0")) return false;
    consumeToken(b, GENERIC);
    return true;
  }

  // [genericDefinition]
  private static boolean GenericTypeIdent_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "GenericTypeIdent_2")) return false;
    genericDefinition(b, l + 1);
    return true;
  }

  /* ********************************************************** */
  // GOTO LabelId
  public static boolean GotoStatement(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "GotoStatement")) return false;
    if (!nextTokenIs(b, GOTO)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, GOTO);
    r = r && LabelId(b, l + 1);
    exit_section_(b, m, GOTO_STATEMENT, r);
    return r;
  }

  /* ********************************************************** */
  // handlerStart doStatement ";"
  public static boolean Handler(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "Handler")) return false;
    if (!nextTokenIs(b, ON)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, HANDLER, null);
    r = handlerStart(b, l + 1);
    p = r; // pin = 1
    r = r && report_error_(b, doStatement(b, l + 1));
    r = p && consumeToken(b, SEMI) && r;
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  /* ********************************************************** */
  // Statement
  public static boolean IfElseStatement(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "IfElseStatement")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, IF_ELSE_STATEMENT, "<if else statement>");
    r = Statement(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // IF ctrlStmtExpression ifThenStatement [ELSE IfElseStatement]
  public static boolean IfStatement(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "IfStatement")) return false;
    if (!nextTokenIs(b, IF)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, IF_STATEMENT, null);
    r = consumeToken(b, IF);
    p = r; // pin = 1
    r = r && report_error_(b, ctrlStmtExpression(b, l + 1));
    r = p && report_error_(b, ifThenStatement(b, l + 1)) && r;
    r = p && IfStatement_3(b, l + 1) && r;
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  // [ELSE IfElseStatement]
  private static boolean IfStatement_3(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "IfStatement_3")) return false;
    IfStatement_3_0(b, l + 1);
    return true;
  }

  // ELSE IfElseStatement
  private static boolean IfStatement_3_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "IfStatement_3_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, ELSE);
    r = r && IfElseStatement(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // Statement
  public static boolean IfThenStatement(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "IfThenStatement")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, IF_THEN_STATEMENT, "<if then statement>");
    r = Statement(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // declSection* {}
  public static boolean ImplDeclSection(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ImplDeclSection")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, IMPL_DECL_SECTION, "<impl decl section>");
    r = ImplDeclSection_0(b, l + 1);
    r = r && ImplDeclSection_1(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // declSection*
  private static boolean ImplDeclSection_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ImplDeclSection_0")) return false;
    while (true) {
      int c = current_position_(b);
      if (!declSection(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "ImplDeclSection_0", c)) break;
    }
    return true;
  }

  // {}
  private static boolean ImplDeclSection_1(PsiBuilder b, int l) {
    return true;
  }

  /* ********************************************************** */
  // (SubIdent [GenericPostfix] ".")* OperatorSubIdent
  public static boolean InOperatorQualifiedIdent(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "InOperatorQualifiedIdent")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, IN_OPERATOR_QUALIFIED_IDENT, "<Identifier>");
    r = InOperatorQualifiedIdent_0(b, l + 1);
    r = r && OperatorSubIdent(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // (SubIdent [GenericPostfix] ".")*
  private static boolean InOperatorQualifiedIdent_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "InOperatorQualifiedIdent_0")) return false;
    while (true) {
      int c = current_position_(b);
      if (!InOperatorQualifiedIdent_0_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "InOperatorQualifiedIdent_0", c)) break;
    }
    return true;
  }

  // SubIdent [GenericPostfix] "."
  private static boolean InOperatorQualifiedIdent_0_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "InOperatorQualifiedIdent_0_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = SubIdent(b, l + 1);
    r = r && InOperatorQualifiedIdent_0_0_1(b, l + 1);
    r = r && consumeToken(b, DOT);
    exit_section_(b, m, null, r);
    return r;
  }

  // [GenericPostfix]
  private static boolean InOperatorQualifiedIdent_0_0_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "InOperatorQualifiedIdent_0_0_1")) return false;
    GenericPostfix(b, l + 1);
    return true;
  }

  /* ********************************************************** */
  // INHERITED
  public static boolean InheritedCall(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "InheritedCall")) return false;
    if (!nextTokenIs(b, INHERITED)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, INHERITED);
    exit_section_(b, m, INHERITED_CALL, r);
    return r;
  }

  /* ********************************************************** */
  // CONST customAttributes* NamedIdent [":" TypeDecl] "=" ConstExpression
  public static boolean InlineConstDeclaration(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "InlineConstDeclaration")) return false;
    if (!nextTokenIs(b, CONST)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, INLINE_CONST_DECLARATION, null);
    r = consumeToken(b, CONST);
    p = r; // pin = 1
    r = r && report_error_(b, InlineConstDeclaration_1(b, l + 1));
    r = p && report_error_(b, NamedIdent(b, l + 1)) && r;
    r = p && report_error_(b, InlineConstDeclaration_3(b, l + 1)) && r;
    r = p && report_error_(b, consumeToken(b, EQ)) && r;
    r = p && ConstExpression(b, l + 1) && r;
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  // customAttributes*
  private static boolean InlineConstDeclaration_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "InlineConstDeclaration_1")) return false;
    while (true) {
      int c = current_position_(b);
      if (!customAttributes(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "InlineConstDeclaration_1", c)) break;
    }
    return true;
  }

  // [":" TypeDecl]
  private static boolean InlineConstDeclaration_3(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "InlineConstDeclaration_3")) return false;
    InlineConstDeclaration_3_0(b, l + 1);
    return true;
  }

  // ":" TypeDecl
  private static boolean InlineConstDeclaration_3_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "InlineConstDeclaration_3_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, COLON);
    r = r && TypeDecl(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // VAR customAttributes* identList [":" TypeDecl]
  public static boolean InlineVarDeclaration(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "InlineVarDeclaration")) return false;
    if (!nextTokenIs(b, VAR)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, INLINE_VAR_DECLARATION, null);
    r = consumeToken(b, VAR);
    p = r; // pin = 1
    r = r && report_error_(b, InlineVarDeclaration_1(b, l + 1));
    r = p && report_error_(b, identList(b, l + 1)) && r;
    r = p && InlineVarDeclaration_3(b, l + 1) && r;
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  // customAttributes*
  private static boolean InlineVarDeclaration_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "InlineVarDeclaration_1")) return false;
    while (true) {
      int c = current_position_(b);
      if (!customAttributes(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "InlineVarDeclaration_1", c)) break;
    }
    return true;
  }

  // [":" TypeDecl]
  private static boolean InlineVarDeclaration_3(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "InlineVarDeclaration_3")) return false;
    InlineVarDeclaration_3_0(b, l + 1);
    return true;
  }

  // ":" TypeDecl
  private static boolean InlineVarDeclaration_3_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "InlineVarDeclaration_3_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, COLON);
    r = r && TypeDecl(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // interfaceKey interfaceBody END
  //                               | interfaceKey [classParentWithRecovery]
  public static boolean InterfaceTypeDecl(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "InterfaceTypeDecl")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, INTERFACE_TYPE_DECL, "<interface declaration>");
    r = InterfaceTypeDecl_0(b, l + 1);
    if (!r) r = InterfaceTypeDecl_1(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // interfaceKey interfaceBody END
  private static boolean InterfaceTypeDecl_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "InterfaceTypeDecl_0")) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_);
    r = interfaceKey(b, l + 1);
    p = r; // pin = interfaceKey
    r = r && report_error_(b, interfaceBody(b, l + 1));
    r = p && consumeToken(b, END) && r;
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  // interfaceKey [classParentWithRecovery]
  private static boolean InterfaceTypeDecl_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "InterfaceTypeDecl_1")) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_);
    r = interfaceKey(b, l + 1);
    p = r; // pin = interfaceKey
    r = r && InterfaceTypeDecl_1_1(b, l + 1);
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  // [classParentWithRecovery]
  private static boolean InterfaceTypeDecl_1_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "InterfaceTypeDecl_1_1")) return false;
    classParentWithRecovery(b, l + 1);
    return true;
  }

  /* ********************************************************** */
  // OUT | SELF | NEW
  //                               | EXIT | BREAK | CONTINUE
  //                               | VIRTUAL | DYNAMIC | ABSTRACT | OVERLOAD | OVERRIDE | REINTRODUCE | MESSAGE | STATIC | SEALED | FINAL | ASSEMBLER
  //                               | CDECL | PASCAL | REGISTER | SAFECALL | STDCALL | EXPORT | OPERATOR
  //                               | AUTOMATED | DISPID | EXTERNAL | FORWARD | HELPER | IMPLEMENTS
  //                               | DEFAULT | INDEX | READ | WRITE | DEPRECATED | EXPERIMENTAL | PLATFORM | REFERENCE
  //                               | PACKAGE | CONTAINS | REQUIRES
  public static boolean KeywordIdent(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "KeywordIdent")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, KEYWORD_IDENT, "<Identifier>");
    r = consumeToken(b, OUT);
    if (!r) r = consumeToken(b, SELF);
    if (!r) r = consumeToken(b, NEW);
    if (!r) r = consumeToken(b, EXIT);
    if (!r) r = consumeToken(b, BREAK);
    if (!r) r = consumeToken(b, CONTINUE);
    if (!r) r = consumeToken(b, VIRTUAL);
    if (!r) r = consumeToken(b, DYNAMIC);
    if (!r) r = consumeToken(b, ABSTRACT);
    if (!r) r = consumeToken(b, OVERLOAD);
    if (!r) r = consumeToken(b, OVERRIDE);
    if (!r) r = consumeToken(b, REINTRODUCE);
    if (!r) r = consumeToken(b, MESSAGE);
    if (!r) r = consumeToken(b, STATIC);
    if (!r) r = consumeToken(b, SEALED);
    if (!r) r = consumeToken(b, FINAL);
    if (!r) r = consumeToken(b, ASSEMBLER);
    if (!r) r = consumeToken(b, CDECL);
    if (!r) r = consumeToken(b, PASCAL);
    if (!r) r = consumeToken(b, REGISTER);
    if (!r) r = consumeToken(b, SAFECALL);
    if (!r) r = consumeToken(b, STDCALL);
    if (!r) r = consumeToken(b, EXPORT);
    if (!r) r = consumeToken(b, OPERATOR);
    if (!r) r = consumeToken(b, AUTOMATED);
    if (!r) r = consumeToken(b, DISPID);
    if (!r) r = consumeToken(b, EXTERNAL);
    if (!r) r = consumeToken(b, FORWARD);
    if (!r) r = consumeToken(b, HELPER);
    if (!r) r = consumeToken(b, IMPLEMENTS);
    if (!r) r = consumeToken(b, DEFAULT);
    if (!r) r = consumeToken(b, INDEX);
    if (!r) r = consumeToken(b, READ);
    if (!r) r = consumeToken(b, WRITE);
    if (!r) r = consumeToken(b, DEPRECATED);
    if (!r) r = consumeToken(b, EXPERIMENTAL);
    if (!r) r = consumeToken(b, PLATFORM);
    if (!r) r = consumeToken(b, REFERENCE);
    if (!r) r = consumeToken(b, PACKAGE);
    if (!r) r = consumeToken(b, CONTAINS);
    if (!r) r = consumeToken(b, REQUIRES);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // LABEL label labelsRest* ";"
  public static boolean LabelDeclSection(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "LabelDeclSection")) return false;
    if (!nextTokenIs(b, "<label declaration>", LABEL)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, LABEL_DECL_SECTION, "<label declaration>");
    r = consumeToken(b, LABEL);
    p = r; // pin = 1
    r = r && report_error_(b, label(b, l + 1));
    r = p && report_error_(b, LabelDeclSection_2(b, l + 1)) && r;
    r = p && consumeToken(b, SEMI) && r;
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  // labelsRest*
  private static boolean LabelDeclSection_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "LabelDeclSection_2")) return false;
    while (true) {
      int c = current_position_(b);
      if (!labelsRest(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "LabelDeclSection_2", c)) break;
    }
    return true;
  }

  /* ********************************************************** */
  // label
  public static boolean LabelId(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "LabelId")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, LABEL_ID, "<label id>");
    r = label(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // LIBRARY NamespaceIdent hintingDirective* ";"
  public static boolean LibraryModuleHead(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "LibraryModuleHead")) return false;
    if (!nextTokenIs(b, LIBRARY)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, LIBRARY_MODULE_HEAD, null);
    r = consumeToken(b, LIBRARY);
    p = r; // pin = 1
    r = r && report_error_(b, NamespaceIdent(b, l + 1));
    r = p && report_error_(b, LibraryModuleHead_2(b, l + 1)) && r;
    r = p && consumeToken(b, SEMI) && r;
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  // hintingDirective*
  private static boolean LibraryModuleHead_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "LibraryModuleHead_2")) return false;
    while (true) {
      int c = current_position_(b);
      if (!hintingDirective(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "LibraryModuleHead_2", c)) break;
    }
    return true;
  }

  /* ********************************************************** */
  // [prolog] (moduleUnit | moduleLibrary | modulePackage | moduleProgram | include_impl | include_intf)
  static boolean Module(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "Module")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = Module_0(b, l + 1);
    r = r && Module_1(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // [prolog]
  private static boolean Module_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "Module_0")) return false;
    prolog(b, l + 1);
    return true;
  }

  // moduleUnit | moduleLibrary | modulePackage | moduleProgram | include_impl | include_intf
  private static boolean Module_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "Module_1")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = moduleUnit(b, l + 1);
    if (!r) r = moduleLibrary(b, l + 1);
    if (!r) r = modulePackage(b, l + 1);
    if (!r) r = moduleProgram(b, l + 1);
    if (!r) r = include_impl(b, l + 1);
    if (!r) r = include_intf(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // identifier
  public static boolean NamedIdent(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "NamedIdent")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, NAMED_IDENT, "<Identifier>");
    r = identifier(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // identifier
  public static boolean NamedIdentDecl(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "NamedIdentDecl")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, NAMED_IDENT_DECL, "<named ident decl>");
    r = identifier(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // namespacePart? SubIdent
  public static boolean NamespaceIdent(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "NamespaceIdent")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, NAMESPACE_IDENT, "<Identifier>");
    r = NamespaceIdent_0(b, l + 1);
    r = r && SubIdent(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // namespacePart?
  private static boolean NamespaceIdent_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "NamespaceIdent_0")) return false;
    namespacePart(b, l + 1);
    return true;
  }

  /* ********************************************************** */
  // NEW "(" [ Expression ] ("," [ Expression ])* [ "," ConstExpression ] ")"
  public static boolean NewStatement(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "NewStatement")) return false;
    if (!nextTokenIs(b, NEW)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeTokens(b, 0, NEW, LPAREN);
    r = r && NewStatement_2(b, l + 1);
    r = r && NewStatement_3(b, l + 1);
    r = r && NewStatement_4(b, l + 1);
    r = r && consumeToken(b, RPAREN);
    exit_section_(b, m, NEW_STATEMENT, r);
    return r;
  }

  // [ Expression ]
  private static boolean NewStatement_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "NewStatement_2")) return false;
    Expression(b, l + 1);
    return true;
  }

  // ("," [ Expression ])*
  private static boolean NewStatement_3(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "NewStatement_3")) return false;
    while (true) {
      int c = current_position_(b);
      if (!NewStatement_3_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "NewStatement_3", c)) break;
    }
    return true;
  }

  // "," [ Expression ]
  private static boolean NewStatement_3_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "NewStatement_3_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, COMMA);
    r = r && NewStatement_3_0_1(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // [ Expression ]
  private static boolean NewStatement_3_0_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "NewStatement_3_0_1")) return false;
    Expression(b, l + 1);
    return true;
  }

  // [ "," ConstExpression ]
  private static boolean NewStatement_4(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "NewStatement_4")) return false;
    NewStatement_4_0(b, l + 1);
    return true;
  }

  // "," ConstExpression
  private static boolean NewStatement_4_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "NewStatement_4_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, COMMA);
    r = r && ConstExpression(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // [PACKED] objectTypeDecl
  public static boolean ObjectDecl(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ObjectDecl")) return false;
    if (!nextTokenIs(b, "<object declaration>", OBJECT, PACKED)) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, OBJECT_DECL, "<object declaration>");
    r = ObjectDecl_0(b, l + 1);
    r = r && objectTypeDecl(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // [PACKED]
  private static boolean ObjectDecl_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ObjectDecl_0")) return false;
    consumeToken(b, PACKED);
    return true;
  }

  /* ********************************************************** */
  // IN | operatorRedef
  public static boolean OperatorSubIdent(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "OperatorSubIdent")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, OPERATOR_SUB_IDENT, "<Identifier>");
    r = consumeToken(b, IN);
    if (!r) r = operatorRedef(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // PACKAGE NamespaceIdent ";"
  public static boolean PackageModuleHead(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "PackageModuleHead")) return false;
    if (!nextTokenIs(b, PACKAGE)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, PACKAGE_MODULE_HEAD, null);
    r = consumeToken(b, PACKAGE);
    p = r; // pin = 1
    r = r && report_error_(b, NamespaceIdent(b, l + 1));
    r = p && consumeToken(b, SEMI) && r;
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  /* ********************************************************** */
  // CONST | VAR | OUT | CONSTREF
  public static boolean ParamType(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ParamType")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, PARAM_TYPE, "<param type>");
    r = consumeToken(b, CONST);
    if (!r) r = consumeToken(b, VAR);
    if (!r) r = consumeToken(b, OUT);
    if (!r) r = consumeToken(b, CONSTREF);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // "^" TypeDecl
  public static boolean PointerType(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "PointerType")) return false;
    if (!nextTokenIs(b, DEREF)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, POINTER_TYPE, null);
    r = consumeToken(b, DEREF);
    p = r; // pin = 1
    r = r && TypeDecl(b, l + 1);
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  /* ********************************************************** */
  // ProcForwardDecl | ExternalDirective | BlockLocal [";"]
  public static boolean ProcBodyBlock(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ProcBodyBlock")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, PROC_BODY_BLOCK, "<proc body block>");
    r = ProcForwardDecl(b, l + 1);
    if (!r) r = ExternalDirective(b, l + 1);
    if (!r) r = ProcBodyBlock_2(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // BlockLocal [";"]
  private static boolean ProcBodyBlock_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ProcBodyBlock_2")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = BlockLocal(b, l + 1);
    r = r && ProcBodyBlock_2_1(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // [";"]
  private static boolean ProcBodyBlock_2_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ProcBodyBlock_2_1")) return false;
    consumeToken(b, SEMI);
    return true;
  }

  /* ********************************************************** */
  // FORWARD ";" FunctionDirective*
  public static boolean ProcForwardDecl(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ProcForwardDecl")) return false;
    if (!nextTokenIs(b, FORWARD)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeTokens(b, 0, FORWARD, SEMI);
    r = r && ProcForwardDecl_2(b, l + 1);
    exit_section_(b, m, PROC_FORWARD_DECL, r);
    return r;
  }

  // FunctionDirective*
  private static boolean ProcForwardDecl_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ProcForwardDecl_2")) return false;
    while (true) {
      int c = current_position_(b);
      if (!FunctionDirective(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "ProcForwardDecl_2", c)) break;
    }
    return true;
  }

  /* ********************************************************** */
  // procedureReference | simpleProcedureType
  public static boolean ProcedureType(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ProcedureType")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, PROCEDURE_TYPE, "<procedure type>");
    r = procedureReference(b, l + 1);
    if (!r) r = simpleProcedureType(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // PROGRAM NamespaceIdent ProgramParamList? ";"
  public static boolean ProgramModuleHead(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ProgramModuleHead")) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, PROGRAM_MODULE_HEAD, "<program module head>");
    r = consumeToken(b, PROGRAM);
    p = r; // pin = 1
    r = r && report_error_(b, NamespaceIdent(b, l + 1));
    r = p && report_error_(b, ProgramModuleHead_2(b, l + 1)) && r;
    r = p && consumeToken(b, SEMI) && r;
    exit_section_(b, l, m, r, p, PascalParser::rec_programHead);
    return r || p;
  }

  // ProgramParamList?
  private static boolean ProgramModuleHead_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ProgramModuleHead_2")) return false;
    ProgramParamList(b, l + 1);
    return true;
  }

  /* ********************************************************** */
  // LPAREN progParam? progParamsRest* RPAREN
  public static boolean ProgramParamList(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ProgramParamList")) return false;
    if (!nextTokenIs(b, LPAREN)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, PROGRAM_PARAM_LIST, null);
    r = consumeToken(b, LPAREN);
    p = r; // pin = 1
    r = r && report_error_(b, ProgramParamList_1(b, l + 1));
    r = p && report_error_(b, ProgramParamList_2(b, l + 1)) && r;
    r = p && consumeToken(b, RPAREN) && r;
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  // progParam?
  private static boolean ProgramParamList_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ProgramParamList_1")) return false;
    progParam(b, l + 1);
    return true;
  }

  // progParamsRest*
  private static boolean ProgramParamList_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ProgramParamList_2")) return false;
    while (true) {
      int c = current_position_(b);
      if (!progParamsRest(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "ProgramParamList_2", c)) break;
    }
    return true;
  }

  /* ********************************************************** */
  // RAISE [Expression] ["at" Expression]
  public static boolean RaiseStatement(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "RaiseStatement")) return false;
    if (!nextTokenIs(b, RAISE)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, RAISE_STATEMENT, null);
    r = consumeToken(b, RAISE);
    p = r; // pin = 1
    r = r && report_error_(b, RaiseStatement_1(b, l + 1));
    r = p && RaiseStatement_2(b, l + 1) && r;
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  // [Expression]
  private static boolean RaiseStatement_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "RaiseStatement_1")) return false;
    Expression(b, l + 1);
    return true;
  }

  // ["at" Expression]
  private static boolean RaiseStatement_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "RaiseStatement_2")) return false;
    RaiseStatement_2_0(b, l + 1);
    return true;
  }

  // "at" Expression
  private static boolean RaiseStatement_2_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "RaiseStatement_2_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, "at");
    r = r && Expression(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // ConstExpressionOrd
  public static boolean RangeBound(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "RangeBound")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, RANGE_BOUND, "<range bound>");
    r = ConstExpressionOrd(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // "(" recordConstInner ")"
  public static boolean RecordConstExpr(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "RecordConstExpr")) return false;
    if (!nextTokenIs(b, LPAREN)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, LPAREN);
    r = r && recordConstInner(b, l + 1);
    r = r && consumeToken(b, RPAREN);
    exit_section_(b, m, RECORD_CONST_EXPR, r);
    return r;
  }

  /* ********************************************************** */
  // [PACKED | "bitpacked"] RECORD varRecDecl END
  public static boolean RecordDecl(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "RecordDecl")) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, RECORD_DECL, "<record declaration>");
    r = RecordDecl_0(b, l + 1);
    r = r && consumeToken(b, RECORD);
    p = r; // pin = 2
    r = r && report_error_(b, varRecDecl(b, l + 1));
    r = p && consumeToken(b, END) && r;
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  // [PACKED | "bitpacked"]
  private static boolean RecordDecl_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "RecordDecl_0")) return false;
    RecordDecl_0_0(b, l + 1);
    return true;
  }

  // PACKED | "bitpacked"
  private static boolean RecordDecl_0_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "RecordDecl_0_0")) return false;
    boolean r;
    r = consumeToken(b, PACKED);
    if (!r) r = consumeToken(b, "bitpacked");
    return r;
  }

  /* ********************************************************** */
  // RECORD HELPER classHelperBody END
  public static boolean RecordHelperDecl(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "RecordHelperDecl")) return false;
    if (!nextTokenIs(b, "<record helper declaration>", RECORD)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, RECORD_HELPER_DECL, "<record helper declaration>");
    r = consumeTokens(b, 2, RECORD, HELPER);
    p = r; // pin = 2
    r = r && report_error_(b, classHelperBody(b, l + 1));
    r = p && consumeToken(b, END) && r;
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  /* ********************************************************** */
  // ConstExpressionOrd ("," ConstExpressionOrd)* ":" "(" varRecDeclInner ")"
  public static boolean RecordVariant(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "RecordVariant")) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, RECORD_VARIANT, "<record variant>");
    r = ConstExpressionOrd(b, l + 1);
    r = r && RecordVariant_1(b, l + 1);
    r = r && consumeTokens(b, 2, COLON, LPAREN);
    p = r; // pin = 4
    r = r && report_error_(b, varRecDeclInner(b, l + 1));
    r = p && consumeToken(b, RPAREN) && r;
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  // ("," ConstExpressionOrd)*
  private static boolean RecordVariant_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "RecordVariant_1")) return false;
    while (true) {
      int c = current_position_(b);
      if (!RecordVariant_1_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "RecordVariant_1", c)) break;
    }
    return true;
  }

  // "," ConstExpressionOrd
  private static boolean RecordVariant_1_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "RecordVariant_1_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, COMMA);
    r = r && ConstExpressionOrd(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // identifier
  public static boolean RefNamedIdent(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "RefNamedIdent")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, REF_NAMED_IDENT, "<Identifier>");
    r = identifier(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // REPEAT [statementList] UNTIL Expression
  public static boolean RepeatStatement(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "RepeatStatement")) return false;
    if (!nextTokenIs(b, REPEAT)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, REPEAT_STATEMENT, null);
    r = consumeToken(b, REPEAT);
    p = r; // pin = 1
    r = r && report_error_(b, RepeatStatement_1(b, l + 1));
    r = p && report_error_(b, consumeToken(b, UNTIL)) && r;
    r = p && Expression(b, l + 1) && r;
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  // [statementList]
  private static boolean RepeatStatement_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "RepeatStatement_1")) return false;
    statementList(b, l + 1);
    return true;
  }

  /* ********************************************************** */
  // REQUIRES namespaceNameList ";"
  public static boolean RequiresClause(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "RequiresClause")) return false;
    if (!nextTokenIs(b, REQUIRES)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, REQUIRES_CLAUSE, null);
    r = consumeToken(b, REQUIRES);
    p = r; // pin = 1
    r = r && report_error_(b, namespaceNameList(b, l + 1));
    r = p && consumeToken(b, SEMI) && r;
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  /* ********************************************************** */
  // routineImpl ProcBodyBlock
  public static boolean RoutineImplDecl(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "RoutineImplDecl")) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, ROUTINE_IMPL_DECL, "<procedure or function declaration>");
    r = routineImpl(b, l + 1);
    p = r; // pin = 1
    r = r && ProcBodyBlock(b, l + 1);
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  /* ********************************************************** */
  // routineDeclaration (ProcForwardDecl | BlockLocalNested1) [";"]
  public static boolean RoutineImplDeclNested1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "RoutineImplDeclNested1")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, ROUTINE_IMPL_DECL_NESTED_1, "<routine impl decl nested 1>");
    r = routineDeclaration(b, l + 1);
    r = r && RoutineImplDeclNested1_1(b, l + 1);
    r = r && RoutineImplDeclNested1_2(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // ProcForwardDecl | BlockLocalNested1
  private static boolean RoutineImplDeclNested1_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "RoutineImplDeclNested1_1")) return false;
    boolean r;
    r = ProcForwardDecl(b, l + 1);
    if (!r) r = BlockLocalNested1(b, l + 1);
    return r;
  }

  // [";"]
  private static boolean RoutineImplDeclNested1_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "RoutineImplDeclNested1_2")) return false;
    consumeToken(b, SEMI);
    return true;
  }

  /* ********************************************************** */
  // routineDeclaration (ProcForwardDecl | BlockLocalWONested) [";"]
  public static boolean RoutineImplDeclWoNested(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "RoutineImplDeclWoNested")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, ROUTINE_IMPL_DECL_WO_NESTED, "<routine impl decl wo nested>");
    r = routineDeclaration(b, l + 1);
    r = r && RoutineImplDeclWoNested_1(b, l + 1);
    r = r && RoutineImplDeclWoNested_2(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // ProcForwardDecl | BlockLocalWONested
  private static boolean RoutineImplDeclWoNested_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "RoutineImplDeclWoNested_1")) return false;
    boolean r;
    r = ProcForwardDecl(b, l + 1);
    if (!r) r = BlockLocalWONested(b, l + 1);
    return r;
  }

  // [";"]
  private static boolean RoutineImplDeclWoNested_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "RoutineImplDeclWoNested_2")) return false;
    consumeToken(b, SEMI);
    return true;
  }

  /* ********************************************************** */
  // [PACKED] SET OF TypeDecl
  public static boolean SetType(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "SetType")) return false;
    if (!nextTokenIs(b, "<set type>", PACKED, SET)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, SET_TYPE, "<set type>");
    r = SetType_0(b, l + 1);
    r = r && consumeTokens(b, 1, SET, OF);
    p = r; // pin = 2
    r = r && TypeDecl(b, l + 1);
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  // [PACKED]
  private static boolean SetType_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "SetType_0")) return false;
    consumeToken(b, PACKED);
    return true;
  }

  /* ********************************************************** */
  // [LabelId ":" ] statementPart
  public static boolean Statement(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "Statement")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, STATEMENT, "<statement>");
    r = Statement_0(b, l + 1);
    r = r && statementPart(b, l + 1);
    exit_section_(b, l, m, r, false, PascalParser::rec_statement);
    return r;
  }

  // [LabelId ":" ]
  private static boolean Statement_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "Statement_0")) return false;
    Statement_0_0(b, l + 1);
    return true;
  }

  // LabelId ":"
  private static boolean Statement_0_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "Statement_0_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = LabelId(b, l + 1);
    r = r && consumeToken(b, COLON);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // controlString (quotedString controlString)* [quotedString]
  //                               | quotedString (controlString quotedString)* [controlString]
  public static boolean StringFactor(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "StringFactor")) return false;
    if (!nextTokenIs(b, "<string factor>", CHARNUM, STRING_LITERAL)) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, STRING_FACTOR, "<string factor>");
    r = StringFactor_0(b, l + 1);
    if (!r) r = StringFactor_1(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // controlString (quotedString controlString)* [quotedString]
  private static boolean StringFactor_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "StringFactor_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = controlString(b, l + 1);
    r = r && StringFactor_0_1(b, l + 1);
    r = r && StringFactor_0_2(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // (quotedString controlString)*
  private static boolean StringFactor_0_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "StringFactor_0_1")) return false;
    while (true) {
      int c = current_position_(b);
      if (!StringFactor_0_1_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "StringFactor_0_1", c)) break;
    }
    return true;
  }

  // quotedString controlString
  private static boolean StringFactor_0_1_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "StringFactor_0_1_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = quotedString(b, l + 1);
    r = r && controlString(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // [quotedString]
  private static boolean StringFactor_0_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "StringFactor_0_2")) return false;
    quotedString(b, l + 1);
    return true;
  }

  // quotedString (controlString quotedString)* [controlString]
  private static boolean StringFactor_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "StringFactor_1")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = quotedString(b, l + 1);
    r = r && StringFactor_1_1(b, l + 1);
    r = r && StringFactor_1_2(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // (controlString quotedString)*
  private static boolean StringFactor_1_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "StringFactor_1_1")) return false;
    while (true) {
      int c = current_position_(b);
      if (!StringFactor_1_1_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "StringFactor_1_1", c)) break;
    }
    return true;
  }

  // controlString quotedString
  private static boolean StringFactor_1_1_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "StringFactor_1_1_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = controlString(b, l + 1);
    r = r && quotedString(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // [controlString]
  private static boolean StringFactor_1_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "StringFactor_1_2")) return false;
    controlString(b, l + 1);
    return true;
  }

  /* ********************************************************** */
  // "string" ["[" Expression "]"] | "AnsiString" [codePageNumber]
  public static boolean StringType(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "StringType")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, STRING_TYPE, "<string type>");
    r = StringType_0(b, l + 1);
    if (!r) r = StringType_1(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // "string" ["[" Expression "]"]
  private static boolean StringType_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "StringType_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, "string");
    r = r && StringType_0_1(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // ["[" Expression "]"]
  private static boolean StringType_0_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "StringType_0_1")) return false;
    StringType_0_1_0(b, l + 1);
    return true;
  }

  // "[" Expression "]"
  private static boolean StringType_0_1_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "StringType_0_1_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, LBRACK);
    r = r && Expression(b, l + 1);
    r = r && consumeToken(b, RBRACK);
    exit_section_(b, m, null, r);
    return r;
  }

  // "AnsiString" [codePageNumber]
  private static boolean StringType_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "StringType_1")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, "AnsiString");
    r = r && StringType_1_1(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // [codePageNumber]
  private static boolean StringType_1_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "StringType_1_1")) return false;
    codePageNumber(b, l + 1);
    return true;
  }

  /* ********************************************************** */
  // identifier
  public static boolean SubIdent(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "SubIdent")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, SUB_IDENT, "<Identifier>");
    r = identifier(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // RangeBound RANGE RangeBound
  public static boolean SubRangeType(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "SubRangeType")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, SUB_RANGE_TYPE, "<sub range type>");
    r = RangeBound(b, l + 1);
    r = r && consumeToken(b, RANGE);
    r = r && RangeBound(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // TRY statementList (tryExcept | tryFinally) END
  public static boolean TryStatement(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "TryStatement")) return false;
    if (!nextTokenIs(b, TRY)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, TRY_STATEMENT, null);
    r = consumeToken(b, TRY);
    p = r; // pin = 1
    r = r && report_error_(b, statementList(b, l + 1));
    r = p && report_error_(b, TryStatement_2(b, l + 1)) && r;
    r = p && consumeToken(b, END) && r;
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  // tryExcept | tryFinally
  private static boolean TryStatement_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "TryStatement_2")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = tryExcept(b, l + 1);
    if (!r) r = tryFinally(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // complexType | PointerType | ProcedureType | [TYPE] StringType | simpleType | [TYPE] TypeID
  public static boolean TypeDecl(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "TypeDecl")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, TYPE_DECL, "<type decl>");
    r = complexType(b, l + 1);
    if (!r) r = PointerType(b, l + 1);
    if (!r) r = ProcedureType(b, l + 1);
    if (!r) r = TypeDecl_3(b, l + 1);
    if (!r) r = simpleType(b, l + 1);
    if (!r) r = TypeDecl_5(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // [TYPE] StringType
  private static boolean TypeDecl_3(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "TypeDecl_3")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = TypeDecl_3_0(b, l + 1);
    r = r && StringType(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // [TYPE]
  private static boolean TypeDecl_3_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "TypeDecl_3_0")) return false;
    consumeToken(b, TYPE);
    return true;
  }

  // [TYPE] TypeID
  private static boolean TypeDecl_5(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "TypeDecl_5")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = TypeDecl_5_0(b, l + 1);
    r = r && TypeID(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // [TYPE]
  private static boolean TypeDecl_5_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "TypeDecl_5_0")) return false;
    consumeToken(b, TYPE);
    return true;
  }

  /* ********************************************************** */
  // customAttributes* GenericTypeIdent "=" type
  public static boolean TypeDeclaration(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "TypeDeclaration")) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, TYPE_DECLARATION, "<type declaration>");
    r = TypeDeclaration_0(b, l + 1);
    r = r && GenericTypeIdent(b, l + 1);
    p = r; // pin = 2
    r = r && report_error_(b, consumeToken(b, EQ));
    r = p && type(b, l + 1) && r;
    exit_section_(b, l, m, r, p, PascalParser::rec_name);
    return r || p;
  }

  // customAttributes*
  private static boolean TypeDeclaration_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "TypeDeclaration_0")) return false;
    while (true) {
      int c = current_position_(b);
      if (!customAttributes(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "TypeDeclaration_0", c)) break;
    }
    return true;
  }

  /* ********************************************************** */
  // SPECIALIZE? FullyQualifiedIdent [GenericPostfix]
  public static boolean TypeID(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "TypeID")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, TYPE_ID, "<type id>");
    r = TypeID_0(b, l + 1);
    r = r && FullyQualifiedIdent(b, l + 1);
    r = r && TypeID_2(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // SPECIALIZE?
  private static boolean TypeID_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "TypeID_0")) return false;
    consumeToken(b, SPECIALIZE);
    return true;
  }

  // [GenericPostfix]
  private static boolean TypeID_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "TypeID_2")) return false;
    GenericPostfix(b, l + 1);
    return true;
  }

  /* ********************************************************** */
  // TYPE typeDeclarations [";"]
  public static boolean TypeSection(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "TypeSection")) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, TYPE_SECTION, "<type section>");
    r = consumeToken(b, TYPE);
    p = r; // pin = 1
    r = r && report_error_(b, typeDeclarations(b, l + 1));
    r = p && TypeSection_2(b, l + 1) && r;
    exit_section_(b, l, m, r, p, PascalParser::rec_decl_section);
    return r || p;
  }

  // [";"]
  private static boolean TypeSection_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "TypeSection_2")) return false;
    consumeToken(b, SEMI);
    return true;
  }

  /* ********************************************************** */
  // FINALIZATION statementList
  public static boolean UnitFinalization(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "UnitFinalization")) return false;
    if (!nextTokenIs(b, "<finalization section>", FINALIZATION)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, UNIT_FINALIZATION, "<finalization section>");
    r = consumeToken(b, FINALIZATION);
    p = r; // pin = 1
    r = r && statementList(b, l + 1);
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  /* ********************************************************** */
  // unitImplementationKey [intUsesClause] ImplDeclSection unitBlock
  public static boolean UnitImplementation(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "UnitImplementation")) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, UNIT_IMPLEMENTATION, "<implementation section>");
    r = unitImplementationKey(b, l + 1);
    p = r; // pin = 1
    r = r && report_error_(b, UnitImplementation_1(b, l + 1));
    r = p && report_error_(b, ImplDeclSection(b, l + 1)) && r;
    r = p && unitBlock(b, l + 1) && r;
    exit_section_(b, l, m, r, p, PascalParser::rec_implementation_dot);
    return r || p;
  }

  // [intUsesClause]
  private static boolean UnitImplementation_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "UnitImplementation_1")) return false;
    intUsesClause(b, l + 1);
    return true;
  }

  /* ********************************************************** */
  // INITIALIZATION statementList
  public static boolean UnitInitialization(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "UnitInitialization")) return false;
    if (!nextTokenIs(b, "<initialization section>", INITIALIZATION)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, UNIT_INITIALIZATION, "<initialization section>");
    r = consumeToken(b, INITIALIZATION);
    p = r; // pin = 1
    r = r && statementList(b, l + 1);
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  /* ********************************************************** */
  // unitInterfaceKey [intUsesClause] interfaceDecl*
  public static boolean UnitInterface(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "UnitInterface")) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, UNIT_INTERFACE, "<interface section>");
    r = unitInterfaceKey(b, l + 1);
    p = r; // pin = 1
    r = r && report_error_(b, UnitInterface_1(b, l + 1));
    r = p && UnitInterface_2(b, l + 1) && r;
    exit_section_(b, l, m, r, p, PascalParser::rec_interface);
    return r || p;
  }

  // [intUsesClause]
  private static boolean UnitInterface_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "UnitInterface_1")) return false;
    intUsesClause(b, l + 1);
    return true;
  }

  // interfaceDecl*
  private static boolean UnitInterface_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "UnitInterface_2")) return false;
    while (true) {
      int c = current_position_(b);
      if (!interfaceDecl(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "UnitInterface_2", c)) break;
    }
    return true;
  }

  /* ********************************************************** */
  // UNIT NamespaceIdent hintingDirective* ";"
  public static boolean UnitModuleHead(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "UnitModuleHead")) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, UNIT_MODULE_HEAD, "<unit module head>");
    r = consumeToken(b, UNIT);
    p = r; // pin = 1
    r = r && report_error_(b, NamespaceIdent(b, l + 1));
    r = p && report_error_(b, UnitModuleHead_2(b, l + 1)) && r;
    r = p && consumeToken(b, SEMI) && r;
    exit_section_(b, l, m, r, p, PascalParser::rec_module);
    return r || p;
  }

  // hintingDirective*
  private static boolean UnitModuleHead_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "UnitModuleHead_2")) return false;
    while (true) {
      int c = current_position_(b);
      if (!hintingDirective(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "UnitModuleHead_2", c)) break;
    }
    return true;
  }

  /* ********************************************************** */
  // USES namespaceNameList ";"
  public static boolean UsesClause(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "UsesClause")) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, USES_CLAUSE, "<uses clause>");
    r = consumeToken(b, USES);
    p = r; // pin = 1
    r = r && report_error_(b, namespaceNameList(b, l + 1));
    r = p && consumeToken(b, SEMI) && r;
    exit_section_(b, l, m, r, p, PascalParser::rec_uses);
    return r || p;
  }

  /* ********************************************************** */
  // customAttributes* identListDecl ":" TypeDecl [VarValueSpec] hintingDirective* ";"
  public static boolean VarDeclaration(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "VarDeclaration")) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, VAR_DECLARATION, "<var declaration>");
    r = VarDeclaration_0(b, l + 1);
    r = r && identListDecl(b, l + 1);
    p = r; // pin = 2
    r = r && report_error_(b, consumeToken(b, COLON));
    r = p && report_error_(b, TypeDecl(b, l + 1)) && r;
    r = p && report_error_(b, VarDeclaration_4(b, l + 1)) && r;
    r = p && report_error_(b, VarDeclaration_5(b, l + 1)) && r;
    r = p && consumeToken(b, SEMI) && r;
    exit_section_(b, l, m, r, p, PascalParser::rec_name);
    return r || p;
  }

  // customAttributes*
  private static boolean VarDeclaration_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "VarDeclaration_0")) return false;
    while (true) {
      int c = current_position_(b);
      if (!customAttributes(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "VarDeclaration_0", c)) break;
    }
    return true;
  }

  // [VarValueSpec]
  private static boolean VarDeclaration_4(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "VarDeclaration_4")) return false;
    VarValueSpec(b, l + 1);
    return true;
  }

  // hintingDirective*
  private static boolean VarDeclaration_5(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "VarDeclaration_5")) return false;
    while (true) {
      int c = current_position_(b);
      if (!hintingDirective(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "VarDeclaration_5", c)) break;
    }
    return true;
  }

  /* ********************************************************** */
  // varKey varDeclarations [";"]
  public static boolean VarSection(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "VarSection")) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, VAR_SECTION, "<var section>");
    r = varKey(b, l + 1);
    p = r; // pin = 1
    r = r && report_error_(b, varDeclarations(b, l + 1));
    r = p && VarSection_2(b, l + 1) && r;
    exit_section_(b, l, m, r, p, PascalParser::rec_decl_section);
    return r || p;
  }

  // [";"]
  private static boolean VarSection_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "VarSection_2")) return false;
    consumeToken(b, SEMI);
    return true;
  }

  /* ********************************************************** */
  // ABSOLUTE FullyQualifiedIdent | ABSOLUTE ConstExpressionOrd | EQ ConstExpression
  public static boolean VarValueSpec(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "VarValueSpec")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, VAR_VALUE_SPEC, "<var value spec>");
    r = VarValueSpec_0(b, l + 1);
    if (!r) r = VarValueSpec_1(b, l + 1);
    if (!r) r = VarValueSpec_2(b, l + 1);
    exit_section_(b, l, m, r, false, PascalParser::rec_semi);
    return r;
  }

  // ABSOLUTE FullyQualifiedIdent
  private static boolean VarValueSpec_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "VarValueSpec_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, ABSOLUTE);
    r = r && FullyQualifiedIdent(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // ABSOLUTE ConstExpressionOrd
  private static boolean VarValueSpec_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "VarValueSpec_1")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, ABSOLUTE);
    r = r && ConstExpressionOrd(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // EQ ConstExpression
  private static boolean VarValueSpec_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "VarValueSpec_2")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, EQ);
    r = r && ConstExpression(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // [STRICT] PRIVATE | [STRICT] PROTECTED | PUBLIC | PUBLISHED | AUTOMATED
  public static boolean Visibility(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "Visibility")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, VISIBILITY, "<visibility>");
    r = Visibility_0(b, l + 1);
    if (!r) r = Visibility_1(b, l + 1);
    if (!r) r = consumeToken(b, PUBLIC);
    if (!r) r = consumeToken(b, PUBLISHED);
    if (!r) r = consumeToken(b, AUTOMATED);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // [STRICT] PRIVATE
  private static boolean Visibility_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "Visibility_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = Visibility_0_0(b, l + 1);
    r = r && consumeToken(b, PRIVATE);
    exit_section_(b, m, null, r);
    return r;
  }

  // [STRICT]
  private static boolean Visibility_0_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "Visibility_0_0")) return false;
    consumeToken(b, STRICT);
    return true;
  }

  // [STRICT] PROTECTED
  private static boolean Visibility_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "Visibility_1")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = Visibility_1_0(b, l + 1);
    r = r && consumeToken(b, PROTECTED);
    exit_section_(b, m, null, r);
    return r;
  }

  // [STRICT]
  private static boolean Visibility_1_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "Visibility_1_0")) return false;
    consumeToken(b, STRICT);
    return true;
  }

  /* ********************************************************** */
  // WHILE whileExpression doStatement
  public static boolean WhileStatement(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "WhileStatement")) return false;
    if (!nextTokenIs(b, WHILE)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, WHILE_STATEMENT, null);
    r = consumeToken(b, WHILE);
    p = r; // pin = 1
    r = r && report_error_(b, whileExpression(b, l + 1));
    r = p && doStatement(b, l + 1) && r;
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  /* ********************************************************** */
  // WITH withArgument doStatement
  public static boolean WithStatement(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "WithStatement")) return false;
    if (!nextTokenIs(b, WITH)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, WITH_STATEMENT, null);
    r = consumeToken(b, WITH);
    p = r; // pin = 1
    r = r && report_error_(b, withArgument(b, l + 1));
    r = p && doStatement(b, l + 1) && r;
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  /* ********************************************************** */
  // ABSTRACT | FINAL
  static boolean abstractDirective(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "abstractDirective")) return false;
    if (!nextTokenIs(b, "", ABSTRACT, FINAL)) return false;
    boolean r;
    r = consumeToken(b, ABSTRACT);
    if (!r) r = consumeToken(b, FINAL);
    return r;
  }

  /* ********************************************************** */
  // PLUS | MINUS | OR | XOR
  public static boolean addOp(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "addOp")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, ADD_OP, "<add op>");
    r = consumeToken(b, PLUS);
    if (!r) r = consumeToken(b, MINUS);
    if (!r) r = consumeToken(b, OR);
    if (!r) r = consumeToken(b, XOR);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // "," [ArrayIndex]
  static boolean arrayIndexRest(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "arrayIndexRest")) return false;
    if (!nextTokenIs(b, COMMA)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_);
    r = consumeToken(b, COMMA);
    p = r; // pin = 1
    r = r && arrayIndexRest_1(b, l + 1);
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  // [ArrayIndex]
  private static boolean arrayIndexRest_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "arrayIndexRest_1")) return false;
    ArrayIndex(b, l + 1);
    return true;
  }

  /* ********************************************************** */
  // "[" [ArrayIndex] arrayIndexRest* "]"
  static boolean arrayIndexes(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "arrayIndexes")) return false;
    if (!nextTokenIs(b, LBRACK)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_);
    r = consumeToken(b, LBRACK);
    p = r; // pin = 1
    r = r && report_error_(b, arrayIndexes_1(b, l + 1));
    r = p && report_error_(b, arrayIndexes_2(b, l + 1)) && r;
    r = p && consumeToken(b, RBRACK) && r;
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  // [ArrayIndex]
  private static boolean arrayIndexes_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "arrayIndexes_1")) return false;
    ArrayIndex(b, l + 1);
    return true;
  }

  // arrayIndexRest*
  private static boolean arrayIndexes_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "arrayIndexes_2")) return false;
    while (true) {
      int c = current_position_(b);
      if (!arrayIndexRest(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "arrayIndexes_2", c)) break;
    }
    return true;
  }

  /* ********************************************************** */
  // CONST | TypeDecl
  static boolean arraySubType(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "arraySubType")) return false;
    boolean r;
    r = consumeToken(b, CONST);
    if (!r) r = TypeDecl(b, l + 1);
    return r;
  }

  /* ********************************************************** */
  // assemblerItem*
  static boolean asmBlock(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "asmBlock")) return false;
    Marker m = enter_section_(b, l, _NONE_);
    while (true) {
      int c = current_position_(b);
      if (!assemblerItem(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "asmBlock", c)) break;
    }
    exit_section_(b, l, m, true, false, PascalParser::rec_struct_outer);
    return true;
  }

  /* ********************************************************** */
  // ',' | '[' | ']' | ':' | '+' | '-' | '*' | '/' | '@' | RefNamedIdent | NUMBER_INT | NUMBER_HEX | NUMBER_OCT | NUMBER_BIN | '.NOFRAME'
  static boolean assemblerItem(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "assemblerItem")) return false;
    boolean r;
    r = consumeToken(b, COMMA);
    if (!r) r = consumeToken(b, LBRACK);
    if (!r) r = consumeToken(b, RBRACK);
    if (!r) r = consumeToken(b, COLON);
    if (!r) r = consumeToken(b, PLUS);
    if (!r) r = consumeToken(b, MINUS);
    if (!r) r = consumeToken(b, MULT);
    if (!r) r = consumeToken(b, DIV);
    if (!r) r = consumeToken(b, AT);
    if (!r) r = RefNamedIdent(b, l + 1);
    if (!r) r = consumeToken(b, NUMBER_INT);
    if (!r) r = consumeToken(b, NUMBER_HEX);
    if (!r) r = consumeToken(b, NUMBER_OCT);
    if (!r) r = consumeToken(b, NUMBER_BIN);
    if (!r) r = consumeToken(b, ".NOFRAME");
    return r;
  }

  /* ********************************************************** */
  // "[" "assembly" ":" CustomAttributeDecl "]"
  static boolean assemblyAttribute(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "assemblyAttribute")) return false;
    if (!nextTokenIs(b, LBRACK)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_);
    r = consumeToken(b, LBRACK);
    p = r; // pin = 1
    r = r && report_error_(b, consumeToken(b, "assembly"));
    r = p && report_error_(b, consumeToken(b, COLON)) && r;
    r = p && report_error_(b, CustomAttributeDecl(b, l + 1)) && r;
    r = p && consumeToken(b, RBRACK) && r;
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  /* ********************************************************** */
  // InlineVarDeclaration | Expression
  static boolean assignLeftPart(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "assignLeftPart")) return false;
    boolean r;
    r = InlineVarDeclaration(b, l + 1);
    if (!r) r = Expression(b, l + 1);
    return r;
  }

  /* ********************************************************** */
  // (MESSAGE Expression) | STATIC | DYNAMIC | OVERRIDE | VIRTUAL
  static boolean bindingDirective(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "bindingDirective")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = bindingDirective_0(b, l + 1);
    if (!r) r = consumeToken(b, STATIC);
    if (!r) r = consumeToken(b, DYNAMIC);
    if (!r) r = consumeToken(b, OVERRIDE);
    if (!r) r = consumeToken(b, VIRTUAL);
    exit_section_(b, m, null, r);
    return r;
  }

  // MESSAGE Expression
  private static boolean bindingDirective_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "bindingDirective_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, MESSAGE);
    r = r && Expression(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // CDECL | PASCAL | REGISTER | SAFECALL | STDCALL | EXPORT | varagrs
  static boolean callConvention(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "callConvention")) return false;
    boolean r;
    r = consumeToken(b, CDECL);
    if (!r) r = consumeToken(b, PASCAL);
    if (!r) r = consumeToken(b, REGISTER);
    if (!r) r = consumeToken(b, SAFECALL);
    if (!r) r = consumeToken(b, STDCALL);
    if (!r) r = consumeToken(b, EXPORT);
    if (!r) r = varagrs(b, l + 1);
    return r;
  }

  /* ********************************************************** */
  // CASE ctrlStmtExpression OF CaseItem*
  static boolean caseBody(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "caseBody")) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_);
    r = consumeToken(b, CASE);
    p = r; // pin = 1
    r = r && report_error_(b, ctrlStmtExpression(b, l + 1));
    r = p && report_error_(b, consumeToken(b, OF)) && r;
    r = p && caseBody_3(b, l + 1) && r;
    exit_section_(b, l, m, r, p, PascalParser::rec_case);
    return r || p;
  }

  // CaseItem*
  private static boolean caseBody_3(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "caseBody_3")) return false;
    while (true) {
      int c = current_position_(b);
      if (!CaseItem(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "caseBody_3", c)) break;
    }
    return true;
  }

  /* ********************************************************** */
  // caseLabelPart ("," caseLabelPart)* ":"
  static boolean caseLabel(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "caseLabel")) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_);
    r = caseLabelPart(b, l + 1);
    p = r; // pin = 1
    r = r && report_error_(b, caseLabel_1(b, l + 1));
    r = p && consumeToken(b, COLON) && r;
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  // ("," caseLabelPart)*
  private static boolean caseLabel_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "caseLabel_1")) return false;
    while (true) {
      int c = current_position_(b);
      if (!caseLabel_1_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "caseLabel_1", c)) break;
    }
    return true;
  }

  // "," caseLabelPart
  private static boolean caseLabel_1_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "caseLabel_1_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, COMMA);
    r = r && caseLabelPart(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // ConstExpressionOrd [RANGE ConstExpressionOrd]
  static boolean caseLabelPart(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "caseLabelPart")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = ConstExpressionOrd(b, l + 1);
    r = r && caseLabelPart_1(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // [RANGE ConstExpressionOrd]
  private static boolean caseLabelPart_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "caseLabelPart_1")) return false;
    caseLabelPart_1_0(b, l + 1);
    return true;
  }

  // RANGE ConstExpressionOrd
  private static boolean caseLabelPart_1_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "caseLabelPart_1_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, RANGE);
    r = r && ConstExpressionOrd(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // classHead (classFull | classShort)
  static boolean classDecl(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "classDecl")) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_);
    r = classHead(b, l + 1);
    p = r; // pin = 1
    r = r && classDecl_1(b, l + 1);
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  // classFull | classShort
  private static boolean classDecl_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "classDecl_1")) return false;
    boolean r;
    r = classFull(b, l + 1);
    if (!r) r = classShort(b, l + 1);
    return r;
  }

  /* ********************************************************** */
  // ClassField ";"
  static boolean classFieldSemi(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "classFieldSemi")) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_);
    r = ClassField(b, l + 1);
    p = r; // pin = 1
    r = r && consumeToken(b, SEMI);
    exit_section_(b, l, m, r, p, PascalParser::rec_struct_field);
    return r || p;
  }

  /* ********************************************************** */
  // (CLASS | INTERFACE | OBJC_CLASS | OBJC_PROTOCOL) [EXTERNAL [externalSpecifier]]
  static boolean classForwardDecl(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "classForwardDecl")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = classForwardDecl_0(b, l + 1);
    r = r && classForwardDecl_1(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // CLASS | INTERFACE | OBJC_CLASS | OBJC_PROTOCOL
  private static boolean classForwardDecl_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "classForwardDecl_0")) return false;
    boolean r;
    r = consumeToken(b, CLASS);
    if (!r) r = consumeToken(b, INTERFACE);
    if (!r) r = consumeToken(b, OBJC_CLASS);
    if (!r) r = consumeToken(b, OBJC_PROTOCOL);
    return r;
  }

  // [EXTERNAL [externalSpecifier]]
  private static boolean classForwardDecl_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "classForwardDecl_1")) return false;
    classForwardDecl_1_0(b, l + 1);
    return true;
  }

  // EXTERNAL [externalSpecifier]
  private static boolean classForwardDecl_1_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "classForwardDecl_1_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, EXTERNAL);
    r = r && classForwardDecl_1_0_1(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // [externalSpecifier]
  private static boolean classForwardDecl_1_0_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "classForwardDecl_1_0_1")) return false;
    externalSpecifier(b, l + 1);
    return true;
  }

  /* ********************************************************** */
  // [classParentWithRecovery] structItem* END
  static boolean classFull(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "classFull")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = classFull_0(b, l + 1);
    r = r && classFull_1(b, l + 1);
    r = r && consumeToken(b, END);
    exit_section_(b, m, null, r);
    return r;
  }

  // [classParentWithRecovery]
  private static boolean classFull_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "classFull_0")) return false;
    classParentWithRecovery(b, l + 1);
    return true;
  }

  // structItem*
  private static boolean classFull_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "classFull_1")) return false;
    while (true) {
      int c = current_position_(b);
      if (!structItem(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "classFull_1", c)) break;
    }
    return true;
  }

  /* ********************************************************** */
  // (CLASS | objc_decl) [ClassState]
  static boolean classHead(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "classHead")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = classHead_0(b, l + 1);
    r = r && classHead_1(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // CLASS | objc_decl
  private static boolean classHead_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "classHead_0")) return false;
    boolean r;
    r = consumeToken(b, CLASS);
    if (!r) r = objc_decl(b, l + 1);
    return r;
  }

  // [ClassState]
  private static boolean classHead_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "classHead_1")) return false;
    ClassState(b, l + 1);
    return true;
  }

  /* ********************************************************** */
  // [classParentWithRecovery] FOR TypeID structItem*
  static boolean classHelperBody(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "classHelperBody")) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_);
    r = classHelperBody_0(b, l + 1);
    r = r && consumeToken(b, FOR);
    p = r; // pin = 2
    r = r && report_error_(b, TypeID(b, l + 1));
    r = p && classHelperBody_3(b, l + 1) && r;
    exit_section_(b, l, m, r, p, PascalParser::rec_struct_outer);
    return r || p;
  }

  // [classParentWithRecovery]
  private static boolean classHelperBody_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "classHelperBody_0")) return false;
    classParentWithRecovery(b, l + 1);
    return true;
  }

  // structItem*
  private static boolean classHelperBody_3(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "classHelperBody_3")) return false;
    while (true) {
      int c = current_position_(b);
      if (!structItem(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "classHelperBody_3", c)) break;
    }
    return true;
  }

  /* ********************************************************** */
  // [CLASS] OPERATOR operatorName FormalParameterSection ":" TypeDecl
  static boolean classOperatorDecl(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "classOperatorDecl")) return false;
    if (!nextTokenIs(b, "", CLASS, OPERATOR)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_);
    r = classOperatorDecl_0(b, l + 1);
    r = r && consumeToken(b, OPERATOR);
    p = r; // pin = 2
    r = r && report_error_(b, operatorName(b, l + 1));
    r = p && report_error_(b, FormalParameterSection(b, l + 1)) && r;
    r = p && report_error_(b, consumeToken(b, COLON)) && r;
    r = p && TypeDecl(b, l + 1) && r;
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  // [CLASS]
  private static boolean classOperatorDecl_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "classOperatorDecl_0")) return false;
    consumeToken(b, CLASS);
    return true;
  }

  /* ********************************************************** */
  // ("," TypeID)*
  static boolean classParentRest(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "classParentRest")) return false;
    while (true) {
      int c = current_position_(b);
      if (!classParentRest_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "classParentRest", c)) break;
    }
    return true;
  }

  // "," TypeID
  private static boolean classParentRest_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "classParentRest_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, COMMA);
    r = r && TypeID(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // ClassParent
  static boolean classParentWORec(PsiBuilder b, int l) {
    return ClassParent(b, l + 1);
  }

  /* ********************************************************** */
  // ClassParent
  static boolean classParentWithRecovery(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "classParentWithRecovery")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_);
    r = ClassParent(b, l + 1);
    exit_section_(b, l, m, r, false, PascalParser::rec_classparent);
    return r;
  }

  /* ********************************************************** */
  // "readonly" | "writeonly" | dispIDDirective
  static boolean classPropertyDispInterface(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "classPropertyDispInterface")) return false;
    boolean r;
    r = consumeToken(b, "readonly");
    if (!r) r = consumeToken(b, "writeonly");
    if (!r) r = dispIDDirective(b, l + 1);
    return r;
  }

  /* ********************************************************** */
  // READ RefNamedIdent | WRITE RefNamedIdent | "add" RefNamedIdent | "remove" RefNamedIdent
  static boolean classPropertyReadWrite(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "classPropertyReadWrite")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = classPropertyReadWrite_0(b, l + 1);
    if (!r) r = classPropertyReadWrite_1(b, l + 1);
    if (!r) r = classPropertyReadWrite_2(b, l + 1);
    if (!r) r = classPropertyReadWrite_3(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // READ RefNamedIdent
  private static boolean classPropertyReadWrite_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "classPropertyReadWrite_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, READ);
    r = r && RefNamedIdent(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // WRITE RefNamedIdent
  private static boolean classPropertyReadWrite_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "classPropertyReadWrite_1")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, WRITE);
    r = r && RefNamedIdent(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // "add" RefNamedIdent
  private static boolean classPropertyReadWrite_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "classPropertyReadWrite_2")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, "add");
    r = r && RefNamedIdent(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // "remove" RefNamedIdent
  private static boolean classPropertyReadWrite_3(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "classPropertyReadWrite_3")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, "remove");
    r = r && RefNamedIdent(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // classParentWithRecovery
  static boolean classShort(PsiBuilder b, int l) {
    return classParentWithRecovery(b, l + 1);
  }

  /* ********************************************************** */
  // "(" ConstExpressionOrd ")"
  static boolean codePageNumber(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "codePageNumber")) return false;
    if (!nextTokenIs(b, LPAREN)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_);
    r = consumeToken(b, LPAREN);
    p = r; // pin = 1
    r = r && report_error_(b, ConstExpressionOrd(b, l + 1));
    r = p && consumeToken(b, RPAREN) && r;
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  /* ********************************************************** */
  // [CLASS] VarSection | ClassProperty | ConstSection | TypeSection
  static boolean commonDecl(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "commonDecl")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = commonDecl_0(b, l + 1);
    if (!r) r = ClassProperty(b, l + 1);
    if (!r) r = ConstSection(b, l + 1);
    if (!r) r = TypeSection(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // [CLASS] VarSection
  private static boolean commonDecl_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "commonDecl_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = commonDecl_0_0(b, l + 1);
    r = r && VarSection(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // [CLASS]
  private static boolean commonDecl_0_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "commonDecl_0_0")) return false;
    consumeToken(b, CLASS);
    return true;
  }

  /* ********************************************************** */
  // ClassTypeTypeDecl | structTypeDecl | ArrayType | SetType | FileType
  static boolean complexType(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "complexType")) return false;
    boolean r;
    r = ClassTypeTypeDecl(b, l + 1);
    if (!r) r = structTypeDecl(b, l + 1);
    if (!r) r = ArrayType(b, l + 1);
    if (!r) r = SetType(b, l + 1);
    if (!r) r = FileType(b, l + 1);
    return r;
  }

  /* ********************************************************** */
  // ConstDeclaration+
  static boolean constDeclarations(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "constDeclarations")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_);
    r = ConstDeclaration(b, l + 1);
    while (r) {
      int c = current_position_(b);
      if (!ConstDeclaration(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "constDeclarations", c)) break;
    }
    exit_section_(b, l, m, r, false, PascalParser::rec_block_local_end);
    return r;
  }

  /* ********************************************************** */
  // RecordConstExpr | ArrayConstExpr | Expression
  static boolean constExpr(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "constExpr")) return false;
    boolean r;
    r = RecordConstExpr(b, l + 1);
    if (!r) r = ArrayConstExpr(b, l + 1);
    if (!r) r = Expression(b, l + 1);
    return r;
  }

  /* ********************************************************** */
  // CONST |	RESOURCESTRING
  static boolean constKey(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "constKey")) return false;
    if (!nextTokenIs(b, "", CONST, RESOURCESTRING)) return false;
    boolean r;
    r = consumeToken(b, CONST);
    if (!r) r = consumeToken(b, RESOURCESTRING);
    return r;
  }

  /* ********************************************************** */
  // controlchar+
  static boolean controlString(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "controlString")) return false;
    if (!nextTokenIs(b, CHARNUM)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = controlchar(b, l + 1);
    while (r) {
      int c = current_position_(b);
      if (!controlchar(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "controlString", c)) break;
    }
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // CHARNUM NUMBER_INT | CHARNUM NUMBER_HEX
  static boolean controlchar(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "controlchar")) return false;
    if (!nextTokenIs(b, CHARNUM)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = parseTokens(b, 0, CHARNUM, NUMBER_INT);
    if (!r) r = parseTokens(b, 0, CHARNUM, NUMBER_HEX);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // Expression
  static boolean ctrlStmtExpression(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ctrlStmtExpression")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_);
    r = Expression(b, l + 1);
    exit_section_(b, l, m, r, false, PascalParser::rec_statement_mid);
    return r;
  }

  /* ********************************************************** */
  // LBRACK CustomAttributeList RBRACK | assemblyAttribute
  static boolean customAttributes(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "customAttributes")) return false;
    if (!nextTokenIs(b, LBRACK)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = customAttributes_0(b, l + 1);
    if (!r) r = assemblyAttribute(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // LBRACK CustomAttributeList RBRACK
  private static boolean customAttributes_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "customAttributes_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, LBRACK);
    r = r && CustomAttributeList(b, l + 1);
    r = r && consumeToken(b, RBRACK);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // RoutineImplDecl | commonDecl | LabelDeclSection | ExportsSection | assemblyAttribute
  static boolean declSection(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "declSection")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_);
    r = RoutineImplDecl(b, l + 1);
    if (!r) r = commonDecl(b, l + 1);
    if (!r) r = LabelDeclSection(b, l + 1);
    if (!r) r = ExportsSection(b, l + 1);
    if (!r) r = assemblyAttribute(b, l + 1);
    exit_section_(b, l, m, r, false, PascalParser::rec_decl_section);
    return r;
  }

  /* ********************************************************** */
  // RoutineImplDeclNested1 | commonDecl | LabelDeclSection
  static boolean declSectionNested(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "declSectionNested")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_);
    r = RoutineImplDeclNested1(b, l + 1);
    if (!r) r = commonDecl(b, l + 1);
    if (!r) r = LabelDeclSection(b, l + 1);
    exit_section_(b, l, m, r, false, PascalParser::rec_decl_section);
    return r;
  }

  /* ********************************************************** */
  // RoutineImplDeclWoNested | commonDecl | LabelDeclSection
  static boolean declSectionNested1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "declSectionNested1")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_);
    r = RoutineImplDeclWoNested(b, l + 1);
    if (!r) r = commonDecl(b, l + 1);
    if (!r) r = LabelDeclSection(b, l + 1);
    exit_section_(b, l, m, r, false, PascalParser::rec_decl_section);
    return r;
  }

  /* ********************************************************** */
  // commonDecl | LabelDeclSection
  static boolean declSectionWONested(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "declSectionWONested")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_);
    r = commonDecl(b, l + 1);
    if (!r) r = LabelDeclSection(b, l + 1);
    exit_section_(b, l, m, r, false, PascalParser::rec_section_nested);
    return r;
  }

  /* ********************************************************** */
  // "," Expression {}
  static boolean designatorsRest(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "designatorsRest")) return false;
    if (!nextTokenIs(b, COMMA)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, COMMA);
    r = r && Expression(b, l + 1);
    r = r && designatorsRest_2(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // {}
  private static boolean designatorsRest_2(PsiBuilder b, int l) {
    return true;
  }

  /* ********************************************************** */
  // DISPID Expression [";"]
  static boolean dispIDDirective(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "dispIDDirective")) return false;
    if (!nextTokenIs(b, DISPID)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_);
    r = consumeToken(b, DISPID);
    p = r; // pin = 1
    r = r && report_error_(b, Expression(b, l + 1));
    r = p && dispIDDirective_2(b, l + 1) && r;
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  // [";"]
  private static boolean dispIDDirective_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "dispIDDirective_2")) return false;
    consumeToken(b, SEMI);
    return true;
  }

  /* ********************************************************** */
  // DO
  static boolean doKey(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "doKey")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_);
    r = consumeToken(b, DO);
    exit_section_(b, l, m, r, false, PascalParser::rec_doKey);
    return r;
  }

  /* ********************************************************** */
  // doKey Statement
  static boolean doStatement(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "doStatement")) return false;
    if (!nextTokenIs(b, DO)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_);
    r = doKey(b, l + 1);
    p = r; // pin = 1
    r = r && Statement(b, l + 1);
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  /* ********************************************************** */
  // NamedIdentDecl [(EQ | ASSIGN) Expression] {}
  static boolean enumEl(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "enumEl")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = NamedIdentDecl(b, l + 1);
    r = r && enumEl_1(b, l + 1);
    r = r && enumEl_2(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // [(EQ | ASSIGN) Expression]
  private static boolean enumEl_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "enumEl_1")) return false;
    enumEl_1_0(b, l + 1);
    return true;
  }

  // (EQ | ASSIGN) Expression
  private static boolean enumEl_1_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "enumEl_1_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = enumEl_1_0_0(b, l + 1);
    r = r && Expression(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // EQ | ASSIGN
  private static boolean enumEl_1_0_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "enumEl_1_0_0")) return false;
    boolean r;
    r = consumeToken(b, EQ);
    if (!r) r = consumeToken(b, ASSIGN);
    return r;
  }

  // {}
  private static boolean enumEl_2(PsiBuilder b, int l) {
    return true;
  }

  /* ********************************************************** */
  // "," enumEl
  static boolean enumRest(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "enumRest")) return false;
    if (!nextTokenIs(b, COMMA)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_);
    r = consumeToken(b, COMMA);
    p = r; // pin = 1
    r = r && enumEl(b, l + 1);
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  /* ********************************************************** */
  // EXCEPT
  static boolean exceptKey(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "exceptKey")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_);
    r = consumeToken(b, EXCEPT);
    exit_section_(b, l, m, r, false, PascalParser::rec_doKey);
    return r;
  }

  /* ********************************************************** */
  // [FormalParameterSection] [INDEX Expression] ["name" Expression] ["resident"]
  static boolean exportItem(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "exportItem")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = exportItem_0(b, l + 1);
    r = r && exportItem_1(b, l + 1);
    r = r && exportItem_2(b, l + 1);
    r = r && exportItem_3(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // [FormalParameterSection]
  private static boolean exportItem_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "exportItem_0")) return false;
    FormalParameterSection(b, l + 1);
    return true;
  }

  // [INDEX Expression]
  private static boolean exportItem_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "exportItem_1")) return false;
    exportItem_1_0(b, l + 1);
    return true;
  }

  // INDEX Expression
  private static boolean exportItem_1_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "exportItem_1_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, INDEX);
    r = r && Expression(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // ["name" Expression]
  private static boolean exportItem_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "exportItem_2")) return false;
    exportItem_2_0(b, l + 1);
    return true;
  }

  // "name" Expression
  private static boolean exportItem_2_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "exportItem_2_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, "name");
    r = r && Expression(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // ["resident"]
  private static boolean exportItem_3(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "exportItem_3")) return false;
    consumeToken(b, "resident");
    return true;
  }

  /* ********************************************************** */
  // [GENERIC] [CLASS] FUNCTION procName [genericDefinition] [FormalParameterSection] ":" customAttributes* TypeDecl
  static boolean exportedFunc(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "exportedFunc")) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_);
    r = exportedFunc_0(b, l + 1);
    r = r && exportedFunc_1(b, l + 1);
    r = r && consumeToken(b, FUNCTION);
    p = r; // pin = 3
    r = r && report_error_(b, procName(b, l + 1));
    r = p && report_error_(b, exportedFunc_4(b, l + 1)) && r;
    r = p && report_error_(b, exportedFunc_5(b, l + 1)) && r;
    r = p && report_error_(b, consumeToken(b, COLON)) && r;
    r = p && report_error_(b, exportedFunc_7(b, l + 1)) && r;
    r = p && TypeDecl(b, l + 1) && r;
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  // [GENERIC]
  private static boolean exportedFunc_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "exportedFunc_0")) return false;
    consumeToken(b, GENERIC);
    return true;
  }

  // [CLASS]
  private static boolean exportedFunc_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "exportedFunc_1")) return false;
    consumeToken(b, CLASS);
    return true;
  }

  // [genericDefinition]
  private static boolean exportedFunc_4(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "exportedFunc_4")) return false;
    genericDefinition(b, l + 1);
    return true;
  }

  // [FormalParameterSection]
  private static boolean exportedFunc_5(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "exportedFunc_5")) return false;
    FormalParameterSection(b, l + 1);
    return true;
  }

  // customAttributes*
  private static boolean exportedFunc_7(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "exportedFunc_7")) return false;
    while (true) {
      int c = current_position_(b);
      if (!customAttributes(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "exportedFunc_7", c)) break;
    }
    return true;
  }

  /* ********************************************************** */
  // [GENERIC] [CLASS] FUNCTION procName [genericDefinition] [FormalParameterSection] [":" customAttributes* TypeDecl]
  static boolean exportedFuncImpl(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "exportedFuncImpl")) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_);
    r = exportedFuncImpl_0(b, l + 1);
    r = r && exportedFuncImpl_1(b, l + 1);
    r = r && consumeToken(b, FUNCTION);
    p = r; // pin = 3
    r = r && report_error_(b, procName(b, l + 1));
    r = p && report_error_(b, exportedFuncImpl_4(b, l + 1)) && r;
    r = p && report_error_(b, exportedFuncImpl_5(b, l + 1)) && r;
    r = p && exportedFuncImpl_6(b, l + 1) && r;
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  // [GENERIC]
  private static boolean exportedFuncImpl_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "exportedFuncImpl_0")) return false;
    consumeToken(b, GENERIC);
    return true;
  }

  // [CLASS]
  private static boolean exportedFuncImpl_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "exportedFuncImpl_1")) return false;
    consumeToken(b, CLASS);
    return true;
  }

  // [genericDefinition]
  private static boolean exportedFuncImpl_4(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "exportedFuncImpl_4")) return false;
    genericDefinition(b, l + 1);
    return true;
  }

  // [FormalParameterSection]
  private static boolean exportedFuncImpl_5(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "exportedFuncImpl_5")) return false;
    FormalParameterSection(b, l + 1);
    return true;
  }

  // [":" customAttributes* TypeDecl]
  private static boolean exportedFuncImpl_6(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "exportedFuncImpl_6")) return false;
    exportedFuncImpl_6_0(b, l + 1);
    return true;
  }

  // ":" customAttributes* TypeDecl
  private static boolean exportedFuncImpl_6_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "exportedFuncImpl_6_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, COLON);
    r = r && exportedFuncImpl_6_0_1(b, l + 1);
    r = r && TypeDecl(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // customAttributes*
  private static boolean exportedFuncImpl_6_0_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "exportedFuncImpl_6_0_1")) return false;
    while (true) {
      int c = current_position_(b);
      if (!customAttributes(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "exportedFuncImpl_6_0_1", c)) break;
    }
    return true;
  }

  /* ********************************************************** */
  // [GENERIC] [CLASS] methodKey procName [genericDefinition] [FormalParameterSection]
  static boolean exportedProc(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "exportedProc")) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_);
    r = exportedProc_0(b, l + 1);
    r = r && exportedProc_1(b, l + 1);
    r = r && methodKey(b, l + 1);
    p = r; // pin = 3
    r = r && report_error_(b, procName(b, l + 1));
    r = p && report_error_(b, exportedProc_4(b, l + 1)) && r;
    r = p && exportedProc_5(b, l + 1) && r;
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  // [GENERIC]
  private static boolean exportedProc_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "exportedProc_0")) return false;
    consumeToken(b, GENERIC);
    return true;
  }

  // [CLASS]
  private static boolean exportedProc_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "exportedProc_1")) return false;
    consumeToken(b, CLASS);
    return true;
  }

  // [genericDefinition]
  private static boolean exportedProc_4(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "exportedProc_4")) return false;
    genericDefinition(b, l + 1);
    return true;
  }

  // [FormalParameterSection]
  private static boolean exportedProc_5(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "exportedProc_5")) return false;
    FormalParameterSection(b, l + 1);
    return true;
  }

  /* ********************************************************** */
  // Expression
  static boolean expressionOrd(PsiBuilder b, int l) {
    return Expression(b, l + 1);
  }

  /* ********************************************************** */
  // "name" ConstExpression | INDEX ConstExpressionOrd
  static boolean externalSpecifier(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "externalSpecifier")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = externalSpecifier_0(b, l + 1);
    if (!r) r = externalSpecifier_1(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // "name" ConstExpression
  private static boolean externalSpecifier_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "externalSpecifier_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, "name");
    r = r && ConstExpression(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // INDEX ConstExpressionOrd
  private static boolean externalSpecifier_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "externalSpecifier_1")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, INDEX);
    r = r && ConstExpressionOrd(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // customAttributes* identListDecl ":" TypeDecl hintingDirective*
  static boolean field(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "field")) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_);
    r = field_0(b, l + 1);
    r = r && identListDecl(b, l + 1);
    p = r; // pin = 2
    r = r && report_error_(b, consumeToken(b, COLON));
    r = p && report_error_(b, TypeDecl(b, l + 1)) && r;
    r = p && field_4(b, l + 1) && r;
    exit_section_(b, l, m, r, p, PascalParser::rec_struct_field);
    return r || p;
  }

  // customAttributes*
  private static boolean field_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "field_0")) return false;
    while (true) {
      int c = current_position_(b);
      if (!customAttributes(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "field_0", c)) break;
    }
    return true;
  }

  // hintingDirective*
  private static boolean field_4(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "field_4")) return false;
    while (true) {
      int c = current_position_(b);
      if (!hintingDirective(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "field_4", c)) break;
    }
    return true;
  }

  /* ********************************************************** */
  // ExitStatement | BreakStatement | ContinueStatement | GotoStatement
  static boolean flowStatement(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "flowStatement")) return false;
    boolean r;
    r = ExitStatement(b, l + 1);
    if (!r) r = BreakStatement(b, l + 1);
    if (!r) r = ContinueStatement(b, l + 1);
    if (!r) r = GotoStatement(b, l + 1);
    return r;
  }

  /* ********************************************************** */
  // ":=" FromExpression (TO | DOWNTO)
  static boolean forCycle(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "forCycle")) return false;
    if (!nextTokenIs(b, ASSIGN)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_);
    r = consumeToken(b, ASSIGN);
    p = r; // pin = 1
    r = r && report_error_(b, FromExpression(b, l + 1));
    r = p && forCycle_2(b, l + 1) && r;
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  // TO | DOWNTO
  private static boolean forCycle_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "forCycle_2")) return false;
    boolean r;
    r = consumeToken(b, TO);
    if (!r) r = consumeToken(b, DOWNTO);
    return r;
  }

  /* ********************************************************** */
  // (forCycle | IN) Expression
  static boolean forCycleCondition(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "forCycleCondition")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_);
    r = forCycleCondition_0(b, l + 1);
    r = r && Expression(b, l + 1);
    exit_section_(b, l, m, r, false, PascalParser::rec_statement_mid);
    return r;
  }

  // forCycle | IN
  private static boolean forCycleCondition_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "forCycleCondition_0")) return false;
    boolean r;
    r = forCycle(b, l + 1);
    if (!r) r = consumeToken(b, IN);
    return r;
  }

  /* ********************************************************** */
  // ForInlineDeclaration | FullyQualifiedIdent
  static boolean forCycleIdent(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "forCycleIdent")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, null, "<Identifier>");
    r = ForInlineDeclaration(b, l + 1);
    if (!r) r = FullyQualifiedIdent(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // FormalParameter (";" FormalParameter)*
  static boolean formalParameterList(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "formalParameterList")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = FormalParameter(b, l + 1);
    r = r && formalParameterList_1(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // (";" FormalParameter)*
  private static boolean formalParameterList_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "formalParameterList_1")) return false;
    while (true) {
      int c = current_position_(b);
      if (!formalParameterList_1_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "formalParameterList_1", c)) break;
    }
    return true;
  }

  // ";" FormalParameter
  private static boolean formalParameterList_1_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "formalParameterList_1_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, SEMI);
    r = r && FormalParameter(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // "(" [ formalParameterList ] ")"
  static boolean formalParameterSect(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "formalParameterSect")) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_);
    r = consumeToken(b, LPAREN);
    p = r; // pin = 1
    r = r && report_error_(b, formalParameterSect_1(b, l + 1));
    r = p && consumeToken(b, RPAREN) && r;
    exit_section_(b, l, m, r, p, PascalParser::rec_formal_param_sec);
    return r || p;
  }

  // [ formalParameterList ]
  private static boolean formalParameterSect_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "formalParameterSect_1")) return false;
    formalParameterList(b, l + 1);
    return true;
  }

  /* ********************************************************** */
  // FUNCTION [FormalParameterSection] ":" customAttributes* TypeDecl procTypeDirectives
  static boolean funcHeading(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "funcHeading")) return false;
    if (!nextTokenIs(b, FUNCTION)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_);
    r = consumeToken(b, FUNCTION);
    p = r; // pin = 1
    r = r && report_error_(b, funcHeading_1(b, l + 1));
    r = p && report_error_(b, consumeToken(b, COLON)) && r;
    r = p && report_error_(b, funcHeading_3(b, l + 1)) && r;
    r = p && report_error_(b, TypeDecl(b, l + 1)) && r;
    r = p && procTypeDirectives(b, l + 1) && r;
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  // [FormalParameterSection]
  private static boolean funcHeading_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "funcHeading_1")) return false;
    FormalParameterSection(b, l + 1);
    return true;
  }

  // customAttributes*
  private static boolean funcHeading_3(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "funcHeading_3")) return false;
    while (true) {
      int c = current_position_(b);
      if (!customAttributes(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "funcHeading_3", c)) break;
    }
    return true;
  }

  /* ********************************************************** */
  // (OVERLOAD | oldCallConventionDirective | INLINE | ASSEMBLER | callConvention | hintingDirective | REINTRODUCE | bindingDirective | abstractDirective) ";"
  static boolean functionDirective(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "functionDirective")) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_);
    r = functionDirective_0(b, l + 1);
    p = r; // pin = 1
    r = r && consumeToken(b, SEMI);
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  // OVERLOAD | oldCallConventionDirective | INLINE | ASSEMBLER | callConvention | hintingDirective | REINTRODUCE | bindingDirective | abstractDirective
  private static boolean functionDirective_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "functionDirective_0")) return false;
    boolean r;
    r = consumeToken(b, OVERLOAD);
    if (!r) r = oldCallConventionDirective(b, l + 1);
    if (!r) r = consumeToken(b, INLINE);
    if (!r) r = consumeToken(b, ASSEMBLER);
    if (!r) r = callConvention(b, l + 1);
    if (!r) r = hintingDirective(b, l + 1);
    if (!r) r = consumeToken(b, REINTRODUCE);
    if (!r) r = bindingDirective(b, l + 1);
    if (!r) r = abstractDirective(b, l + 1);
    return r;
  }

  /* ********************************************************** */
  // GenericConstraint genericConstraintsRest*
  static boolean genericConstraintList(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "genericConstraintList")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = GenericConstraint(b, l + 1);
    r = r && genericConstraintList_1(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // genericConstraintsRest*
  private static boolean genericConstraintList_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "genericConstraintList_1")) return false;
    while (true) {
      int c = current_position_(b);
      if (!genericConstraintsRest(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "genericConstraintList_1", c)) break;
    }
    return true;
  }

  /* ********************************************************** */
  // "," GenericConstraint
  static boolean genericConstraintsRest(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "genericConstraintsRest")) return false;
    if (!nextTokenIs(b, COMMA)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, COMMA);
    r = r && GenericConstraint(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // "<" typeParamList ">"
  static boolean genericDefinition(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "genericDefinition")) return false;
    if (!nextTokenIs(b, LT)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_);
    r = consumeToken(b, LT);
    p = r; // pin = 1
    r = r && report_error_(b, typeParamList(b, l + 1));
    r = p && consumeToken(b, GT) && r;
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  /* ********************************************************** */
  // (Handler+ [ELSE statementList]) | statementList
  static boolean handlerList(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "handlerList")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = handlerList_0(b, l + 1);
    if (!r) r = statementList(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // Handler+ [ELSE statementList]
  private static boolean handlerList_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "handlerList_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = handlerList_0_0(b, l + 1);
    r = r && handlerList_0_1(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // Handler+
  private static boolean handlerList_0_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "handlerList_0_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = Handler(b, l + 1);
    while (r) {
      int c = current_position_(b);
      if (!Handler(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "handlerList_0_0", c)) break;
    }
    exit_section_(b, m, null, r);
    return r;
  }

  // [ELSE statementList]
  private static boolean handlerList_0_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "handlerList_0_1")) return false;
    handlerList_0_1_0(b, l + 1);
    return true;
  }

  // ELSE statementList
  private static boolean handlerList_0_1_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "handlerList_0_1_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, ELSE);
    r = r && statementList(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // ON [NamedIdent ":"] TypeID
  static boolean handlerStart(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "handlerStart")) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_);
    r = consumeToken(b, ON);
    p = r; // pin = 1
    r = r && report_error_(b, handlerStart_1(b, l + 1));
    r = p && TypeID(b, l + 1) && r;
    exit_section_(b, l, m, r, p, PascalParser::rec_statement_mid);
    return r || p;
  }

  // [NamedIdent ":"]
  private static boolean handlerStart_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "handlerStart_1")) return false;
    handlerStart_1_0(b, l + 1);
    return true;
  }

  // NamedIdent ":"
  private static boolean handlerStart_1_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "handlerStart_1_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = NamedIdent(b, l + 1);
    r = r && consumeToken(b, COLON);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // (DEPRECATED [StringFactor]) | EXPERIMENTAL | PLATFORM | LIBRARY
  static boolean hintingDirective(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "hintingDirective")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = hintingDirective_0(b, l + 1);
    if (!r) r = consumeToken(b, EXPERIMENTAL);
    if (!r) r = consumeToken(b, PLATFORM);
    if (!r) r = consumeToken(b, LIBRARY);
    exit_section_(b, m, null, r);
    return r;
  }

  // DEPRECATED [StringFactor]
  private static boolean hintingDirective_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "hintingDirective_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, DEPRECATED);
    r = r && hintingDirective_0_1(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // [StringFactor]
  private static boolean hintingDirective_0_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "hintingDirective_0_1")) return false;
    StringFactor(b, l + 1);
    return true;
  }

  /* ********************************************************** */
  // identListItem identListRest*
  static boolean identList(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "identList")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = identListItem(b, l + 1);
    r = r && identList_1(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // identListRest*
  private static boolean identList_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "identList_1")) return false;
    while (true) {
      int c = current_position_(b);
      if (!identListRest(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "identList_1", c)) break;
    }
    return true;
  }

  /* ********************************************************** */
  // identListDeclItem identListDeclRest*
  static boolean identListDecl(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "identListDecl")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = identListDeclItem(b, l + 1);
    r = r && identListDecl_1(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // identListDeclRest*
  private static boolean identListDecl_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "identListDecl_1")) return false;
    while (true) {
      int c = current_position_(b);
      if (!identListDeclRest(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "identListDecl_1", c)) break;
    }
    return true;
  }

  /* ********************************************************** */
  // NamedIdentDecl
  static boolean identListDeclItem(PsiBuilder b, int l) {
    return NamedIdentDecl(b, l + 1);
  }

  /* ********************************************************** */
  // "," identListDeclItem
  static boolean identListDeclRest(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "identListDeclRest")) return false;
    if (!nextTokenIs(b, COMMA)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_);
    r = consumeToken(b, COMMA);
    p = r; // pin = 1
    r = r && identListDeclItem(b, l + 1);
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  /* ********************************************************** */
  // NamedIdent
  static boolean identListItem(PsiBuilder b, int l) {
    return NamedIdent(b, l + 1);
  }

  /* ********************************************************** */
  // "," identListItem
  static boolean identListRest(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "identListRest")) return false;
    if (!nextTokenIs(b, COMMA)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_);
    r = consumeToken(b, COMMA);
    p = r; // pin = 1
    r = r && identListItem(b, l + 1);
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  /* ********************************************************** */
  // EscapedIdent | KeywordIdent | NAME
  static boolean identifier(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "identifier")) return false;
    boolean r;
    r = EscapedIdent(b, l + 1);
    if (!r) r = KeywordIdent(b, l + 1);
    if (!r) r = consumeToken(b, NAME);
    return r;
  }

  /* ********************************************************** */
  // "," NamedIdent
  static boolean identsRest(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "identsRest")) return false;
    if (!nextTokenIs(b, COMMA)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, COMMA);
    r = r && NamedIdent(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // THEN IfThenStatement
  static boolean ifThenStatement(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ifThenStatement")) return false;
    if (!nextTokenIs(b, THEN)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_);
    r = consumeToken(b, THEN);
    p = r; // pin = 1
    r = r && IfThenStatement(b, l + 1);
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  /* ********************************************************** */
  // [intUsesClause] declSection+ [unitBlock]
  static boolean include_impl(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "include_impl")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_);
    r = include_impl_0(b, l + 1);
    r = r && include_impl_1(b, l + 1);
    r = r && include_impl_2(b, l + 1);
    exit_section_(b, l, m, r, false, PascalParser::rec_module_start);
    return r;
  }

  // [intUsesClause]
  private static boolean include_impl_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "include_impl_0")) return false;
    intUsesClause(b, l + 1);
    return true;
  }

  // declSection+
  private static boolean include_impl_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "include_impl_1")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = declSection(b, l + 1);
    while (r) {
      int c = current_position_(b);
      if (!declSection(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "include_impl_1", c)) break;
    }
    exit_section_(b, m, null, r);
    return r;
  }

  // [unitBlock]
  private static boolean include_impl_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "include_impl_2")) return false;
    unitBlock(b, l + 1);
    return true;
  }

  /* ********************************************************** */
  // [intUsesClause] interfaceDecl+
  static boolean include_intf(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "include_intf")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_);
    r = include_intf_0(b, l + 1);
    r = r && include_intf_1(b, l + 1);
    exit_section_(b, l, m, r, false, PascalParser::rec_module_start);
    return r;
  }

  // [intUsesClause]
  private static boolean include_intf_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "include_intf_0")) return false;
    intUsesClause(b, l + 1);
    return true;
  }

  // interfaceDecl+
  private static boolean include_intf_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "include_intf_1")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = interfaceDecl(b, l + 1);
    while (r) {
      int c = current_position_(b);
      if (!interfaceDecl(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "include_intf_1", c)) break;
    }
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // '[' Expr (',' Expr) * ']'
  public static boolean indexList(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "indexList")) return false;
    if (!nextTokenIs(b, LBRACK)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, INDEX_LIST, null);
    r = consumeToken(b, LBRACK);
    p = r; // pin = 1
    r = r && report_error_(b, Expr(b, l + 1, -1));
    r = p && report_error_(b, indexList_2(b, l + 1)) && r;
    r = p && consumeToken(b, RBRACK) && r;
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  // (',' Expr) *
  private static boolean indexList_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "indexList_2")) return false;
    while (true) {
      int c = current_position_(b);
      if (!indexList_2_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "indexList_2", c)) break;
    }
    return true;
  }

  // ',' Expr
  private static boolean indexList_2_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "indexList_2_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, COMMA);
    r = r && Expr(b, l + 1, -1);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // UsesClause
  static boolean intUsesClause(PsiBuilder b, int l) {
    return UsesClause(b, l + 1);
  }

  /* ********************************************************** */
  // [classParentWithRecovery] [interfaceGuid] structItem*
  static boolean interfaceBody(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "interfaceBody")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_);
    r = interfaceBody_0(b, l + 1);
    r = r && interfaceBody_1(b, l + 1);
    r = r && interfaceBody_2(b, l + 1);
    exit_section_(b, l, m, r, false, PascalParser::rec_struct_outer);
    return r;
  }

  // [classParentWithRecovery]
  private static boolean interfaceBody_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "interfaceBody_0")) return false;
    classParentWithRecovery(b, l + 1);
    return true;
  }

  // [interfaceGuid]
  private static boolean interfaceBody_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "interfaceBody_1")) return false;
    interfaceGuid(b, l + 1);
    return true;
  }

  // structItem*
  private static boolean interfaceBody_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "interfaceBody_2")) return false;
    while (true) {
      int c = current_position_(b);
      if (!structItem(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "interfaceBody_2", c)) break;
    }
    return true;
  }

  /* ********************************************************** */
  // commonDecl | routineDecl | ExportsSection | assemblyAttribute
  static boolean interfaceDecl(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "interfaceDecl")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_);
    r = commonDecl(b, l + 1);
    if (!r) r = routineDecl(b, l + 1);
    if (!r) r = ExportsSection(b, l + 1);
    if (!r) r = assemblyAttribute(b, l + 1);
    exit_section_(b, l, m, r, false, PascalParser::rec_block_local_end);
    return r;
  }

  /* ********************************************************** */
  // "[" quotedString "]"
  static boolean interfaceGuid(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "interfaceGuid")) return false;
    if (!nextTokenIs(b, LBRACK)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, LBRACK);
    r = r && quotedString(b, l + 1);
    r = r && consumeToken(b, RBRACK);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // INTERFACE | DISPINTERFACE | (OBJC_PROTOCOL [EXTERNAL [externalSpecifier]])
  static boolean interfaceKey(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "interfaceKey")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, INTERFACE);
    if (!r) r = consumeToken(b, DISPINTERFACE);
    if (!r) r = interfaceKey_2(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // OBJC_PROTOCOL [EXTERNAL [externalSpecifier]]
  private static boolean interfaceKey_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "interfaceKey_2")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, OBJC_PROTOCOL);
    r = r && interfaceKey_2_1(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // [EXTERNAL [externalSpecifier]]
  private static boolean interfaceKey_2_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "interfaceKey_2_1")) return false;
    interfaceKey_2_1_0(b, l + 1);
    return true;
  }

  // EXTERNAL [externalSpecifier]
  private static boolean interfaceKey_2_1_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "interfaceKey_2_1_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, EXTERNAL);
    r = r && interfaceKey_2_1_0_1(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // [externalSpecifier]
  private static boolean interfaceKey_2_1_0_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "interfaceKey_2_1_0_1")) return false;
    externalSpecifier(b, l + 1);
    return true;
  }

  /* ********************************************************** */
  // NamedIdent | NUMBER_INT | NUMBER_HEX
  static boolean label(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "label")) return false;
    boolean r;
    r = NamedIdent(b, l + 1);
    if (!r) r = consumeToken(b, NUMBER_INT);
    if (!r) r = consumeToken(b, NUMBER_HEX);
    return r;
  }

  /* ********************************************************** */
  // "," label {}
  static boolean labelsRest(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "labelsRest")) return false;
    if (!nextTokenIs(b, COMMA)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, COMMA);
    r = r && label(b, l + 1);
    r = r && labelsRest_2(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // {}
  private static boolean labelsRest_2(PsiBuilder b, int l) {
    return true;
  }

  /* ********************************************************** */
  // ImplDeclSection END | BlockGlobal
  static boolean libBlockGlobal(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "libBlockGlobal")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = libBlockGlobal_0(b, l + 1);
    if (!r) r = BlockGlobal(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // ImplDeclSection END
  private static boolean libBlockGlobal_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "libBlockGlobal_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = ImplDeclSection(b, l + 1);
    r = r && consumeToken(b, END);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // COMMA constExpr
  static boolean libListRest(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "libListRest")) return false;
    if (!nextTokenIs(b, COMMA)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, COMMA);
    r = r && constExpr(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // "delayed" | "dependency" constExpr libListRest*
  static boolean libLoadSpec(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "libLoadSpec")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, "delayed");
    if (!r) r = libLoadSpec_1(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // "dependency" constExpr libListRest*
  private static boolean libLoadSpec_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "libLoadSpec_1")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, "dependency");
    r = r && constExpr(b, l + 1);
    r = r && libLoadSpec_1_2(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // libListRest*
  private static boolean libLoadSpec_1_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "libLoadSpec_1_2")) return false;
    while (true) {
      int c = current_position_(b);
      if (!libListRest(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "libLoadSpec_1_2", c)) break;
    }
    return true;
  }

  /* ********************************************************** */
  // PROCEDURE | CONSTRUCTOR | DESTRUCTOR
  static boolean methodKey(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "methodKey")) return false;
    boolean r;
    r = consumeToken(b, PROCEDURE);
    if (!r) r = consumeToken(b, CONSTRUCTOR);
    if (!r) r = consumeToken(b, DESTRUCTOR);
    return r;
  }

  /* ********************************************************** */
  // LibraryModuleHead [intUsesClause] libBlockGlobal "."
  static boolean moduleLibrary(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "moduleLibrary")) return false;
    if (!nextTokenIs(b, LIBRARY)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_);
    r = LibraryModuleHead(b, l + 1);
    p = r; // pin = 1
    r = r && report_error_(b, moduleLibrary_1(b, l + 1));
    r = p && report_error_(b, libBlockGlobal(b, l + 1)) && r;
    r = p && consumeToken(b, DOT) && r;
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  // [intUsesClause]
  private static boolean moduleLibrary_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "moduleLibrary_1")) return false;
    intUsesClause(b, l + 1);
    return true;
  }

  /* ********************************************************** */
  // PackageModuleHead RequiresClause [ContainsClause] END "."
  static boolean modulePackage(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "modulePackage")) return false;
    if (!nextTokenIs(b, PACKAGE)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_);
    r = PackageModuleHead(b, l + 1);
    p = r; // pin = 1
    r = r && report_error_(b, RequiresClause(b, l + 1));
    r = p && report_error_(b, modulePackage_2(b, l + 1)) && r;
    r = p && report_error_(b, consumeTokens(b, -1, END, DOT)) && r;
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  // [ContainsClause]
  private static boolean modulePackage_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "modulePackage_2")) return false;
    ContainsClause(b, l + 1);
    return true;
  }

  /* ********************************************************** */
  // progWithHead | progWoHead
  static boolean moduleProgram(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "moduleProgram")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = progWithHead(b, l + 1);
    if (!r) r = progWoHead(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // UnitModuleHead UnitInterface UnitImplementation "."
  static boolean moduleUnit(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "moduleUnit")) return false;
    if (!nextTokenIs(b, UNIT)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_);
    r = UnitModuleHead(b, l + 1);
    p = r; // pin = 1
    r = r && report_error_(b, UnitInterface(b, l + 1));
    r = p && report_error_(b, UnitImplementation(b, l + 1)) && r;
    r = p && consumeToken(b, DOT) && r;
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  /* ********************************************************** */
  // "*" | "/" | IDIV | MOD | AND | SHL | SHR | (">"">") | AS
  public static boolean mulOp(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "mulOp")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, MUL_OP, "<mul op>");
    r = consumeToken(b, MULT);
    if (!r) r = consumeToken(b, DIV);
    if (!r) r = consumeToken(b, IDIV);
    if (!r) r = consumeToken(b, MOD);
    if (!r) r = consumeToken(b, AND);
    if (!r) r = consumeToken(b, SHL);
    if (!r) r = consumeToken(b, SHR);
    if (!r) r = mulOp_7(b, l + 1);
    if (!r) r = consumeToken(b, AS);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // ">"">"
  private static boolean mulOp_7(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "mulOp_7")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeTokens(b, 0, GT, GT);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // NamespaceIdent [IN quotedString]
  static boolean namespaceFileName(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "namespaceFileName")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_);
    r = NamespaceIdent(b, l + 1);
    r = r && namespaceFileName_1(b, l + 1);
    exit_section_(b, l, m, r, false, PascalParser::rec_semi_section);
    return r;
  }

  // [IN quotedString]
  private static boolean namespaceFileName_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "namespaceFileName_1")) return false;
    namespaceFileName_1_0(b, l + 1);
    return true;
  }

  // IN quotedString
  private static boolean namespaceFileName_1_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "namespaceFileName_1_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, IN);
    r = r && quotedString(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // SubIdent "."
  static boolean namespaceItem(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "namespaceItem")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = SubIdent(b, l + 1);
    r = r && consumeToken(b, DOT);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // namespaceFileName namespaceNamesRest*
  static boolean namespaceNameList(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "namespaceNameList")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = namespaceFileName(b, l + 1);
    r = r && namespaceNameList_1(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // namespaceNamesRest*
  private static boolean namespaceNameList_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "namespaceNameList_1")) return false;
    while (true) {
      int c = current_position_(b);
      if (!namespaceNamesRest(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "namespaceNameList_1", c)) break;
    }
    return true;
  }

  /* ********************************************************** */
  // COMMA namespaceFileName
  static boolean namespaceNamesRest(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "namespaceNamesRest")) return false;
    if (!nextTokenIs(b, COMMA)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_);
    r = consumeToken(b, COMMA);
    p = r; // pin = 1
    r = r && namespaceFileName(b, l + 1);
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  /* ********************************************************** */
  // namespaceItem+
  static boolean namespacePart(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "namespacePart")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = namespaceItem(b, l + 1);
    while (r) {
      int c = current_position_(b);
      if (!namespaceItem(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "namespacePart", c)) break;
    }
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // OBJC_CLASS | OBJC_CATEGORY [EXTERNAL [externalSpecifier]]
  static boolean objc_decl(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "objc_decl")) return false;
    if (!nextTokenIs(b, "", OBJC_CATEGORY, OBJC_CLASS)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, OBJC_CLASS);
    if (!r) r = objc_decl_1(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // OBJC_CATEGORY [EXTERNAL [externalSpecifier]]
  private static boolean objc_decl_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "objc_decl_1")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, OBJC_CATEGORY);
    r = r && objc_decl_1_1(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // [EXTERNAL [externalSpecifier]]
  private static boolean objc_decl_1_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "objc_decl_1_1")) return false;
    objc_decl_1_1_0(b, l + 1);
    return true;
  }

  // EXTERNAL [externalSpecifier]
  private static boolean objc_decl_1_1_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "objc_decl_1_1_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, EXTERNAL);
    r = r && objc_decl_1_1_0_1(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // [externalSpecifier]
  private static boolean objc_decl_1_1_0_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "objc_decl_1_1_0_1")) return false;
    externalSpecifier(b, l + 1);
    return true;
  }

  /* ********************************************************** */
  // [classParentWithRecovery] structItem*
  static boolean objectBody(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "objectBody")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_);
    r = objectBody_0(b, l + 1);
    r = r && objectBody_1(b, l + 1);
    exit_section_(b, l, m, r, false, PascalParser::rec_struct_outer);
    return r;
  }

  // [classParentWithRecovery]
  private static boolean objectBody_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "objectBody_0")) return false;
    classParentWithRecovery(b, l + 1);
    return true;
  }

  // structItem*
  private static boolean objectBody_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "objectBody_1")) return false;
    while (true) {
      int c = current_position_(b);
      if (!structItem(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "objectBody_1", c)) break;
    }
    return true;
  }

  /* ********************************************************** */
  // OBJECT objectBody END
  static boolean objectTypeDecl(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "objectTypeDecl")) return false;
    if (!nextTokenIs(b, OBJECT)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_);
    r = consumeToken(b, OBJECT);
    p = r; // pin = 1
    r = r && report_error_(b, objectBody(b, l + 1));
    r = p && consumeToken(b, END) && r;
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  /* ********************************************************** */
  // "far" | "local" | "near"
  static boolean oldCallConventionDirective(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "oldCallConventionDirective")) return false;
    boolean r;
    r = consumeToken(b, "far");
    if (!r) r = consumeToken(b, "local");
    if (!r) r = consumeToken(b, "near");
    return r;
  }

  /* ********************************************************** */
  // procName ["." operatorRedef]
  static boolean operName(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "operName")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = procName(b, l + 1);
    r = r && operName_1(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // ["." operatorRedef]
  private static boolean operName_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "operName_1")) return false;
    operName_1_0(b, l + 1);
    return true;
  }

  // "." operatorRedef
  private static boolean operName_1_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "operName_1_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, DOT);
    r = r && operatorRedef(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // OPERATOR operatorRedef FormalParameterSection [NamedIdent] ":" TypeDecl
  static boolean operatorDecl(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "operatorDecl")) return false;
    if (!nextTokenIs(b, OPERATOR)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_);
    r = consumeToken(b, OPERATOR);
    p = r; // pin = 1
    r = r && report_error_(b, operatorRedef(b, l + 1));
    r = p && report_error_(b, FormalParameterSection(b, l + 1)) && r;
    r = p && report_error_(b, operatorDecl_3(b, l + 1)) && r;
    r = p && report_error_(b, consumeToken(b, COLON)) && r;
    r = p && TypeDecl(b, l + 1) && r;
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  // [NamedIdent]
  private static boolean operatorDecl_3(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "operatorDecl_3")) return false;
    NamedIdent(b, l + 1);
    return true;
  }

  /* ********************************************************** */
  // InOperatorQualifiedIdent | operName
  static boolean operatorName(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "operatorName")) return false;
    boolean r;
    r = InOperatorQualifiedIdent(b, l + 1);
    if (!r) r = operName(b, l + 1);
    return r;
  }

  /* ********************************************************** */
  // ASSIGN | PLUS | MINUS | MULT | DIV | POWER | EQ | LTEQ | LT | GT | "<>" | IN | "explicit"
  static boolean operatorRedef(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "operatorRedef")) return false;
    boolean r;
    r = consumeToken(b, ASSIGN);
    if (!r) r = consumeToken(b, PLUS);
    if (!r) r = consumeToken(b, MINUS);
    if (!r) r = consumeToken(b, MULT);
    if (!r) r = consumeToken(b, DIV);
    if (!r) r = consumeToken(b, POWER);
    if (!r) r = consumeToken(b, EQ);
    if (!r) r = consumeToken(b, LTEQ);
    if (!r) r = consumeToken(b, LT);
    if (!r) r = consumeToken(b, GT);
    if (!r) r = consumeToken(b, NE);
    if (!r) r = consumeToken(b, IN);
    if (!r) r = consumeToken(b, "explicit");
    return r;
  }

  /* ********************************************************** */
  // '(' [ !')' Expr (',' Expr) * ] ')'
  static boolean parenConstruct(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "parenConstruct")) return false;
    if (!nextTokenIs(b, LPAREN)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_);
    r = consumeToken(b, LPAREN);
    p = r; // pin = 1
    r = r && report_error_(b, parenConstruct_1(b, l + 1));
    r = p && consumeToken(b, RPAREN) && r;
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  // [ !')' Expr (',' Expr) * ]
  private static boolean parenConstruct_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "parenConstruct_1")) return false;
    parenConstruct_1_0(b, l + 1);
    return true;
  }

  // !')' Expr (',' Expr) *
  private static boolean parenConstruct_1_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "parenConstruct_1_0")) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_);
    r = parenConstruct_1_0_0(b, l + 1);
    p = r; // pin = 1
    r = r && report_error_(b, Expr(b, l + 1, -1));
    r = p && parenConstruct_1_0_2(b, l + 1) && r;
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  // !')'
  private static boolean parenConstruct_1_0_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "parenConstruct_1_0_0")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NOT_);
    r = !consumeToken(b, RPAREN);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // (',' Expr) *
  private static boolean parenConstruct_1_0_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "parenConstruct_1_0_2")) return false;
    while (true) {
      int c = current_position_(b);
      if (!parenConstruct_1_0_2_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "parenConstruct_1_0_2", c)) break;
    }
    return true;
  }

  // ',' Expr
  private static boolean parenConstruct_1_0_2_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "parenConstruct_1_0_2_0")) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_);
    r = consumeToken(b, COMMA);
    p = r; // pin = 1
    r = r && Expr(b, l + 1, -1);
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  /* ********************************************************** */
  // PROCEDURE [FormalParameterSection] procTypeDirectives
  static boolean procHeading(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "procHeading")) return false;
    if (!nextTokenIs(b, PROCEDURE)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_);
    r = consumeToken(b, PROCEDURE);
    p = r; // pin = 1
    r = r && report_error_(b, procHeading_1(b, l + 1));
    r = p && procTypeDirectives(b, l + 1) && r;
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  // [FormalParameterSection]
  private static boolean procHeading_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "procHeading_1")) return false;
    FormalParameterSection(b, l + 1);
    return true;
  }

  /* ********************************************************** */
  // FUNCTION | PROCEDURE
  static boolean procKey(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "procKey")) return false;
    if (!nextTokenIs(b, "", FUNCTION, PROCEDURE)) return false;
    boolean r;
    r = consumeToken(b, FUNCTION);
    if (!r) r = consumeToken(b, PROCEDURE);
    return r;
  }

  /* ********************************************************** */
  // ClassQualifiedIdent | NamedIdent
  static boolean procName(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "procName")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_);
    r = ClassQualifiedIdent(b, l + 1);
    if (!r) r = NamedIdent(b, l + 1);
    exit_section_(b, l, m, r, false, PascalParser::rec_procName);
    return r;
  }

  /* ********************************************************** */
  // (";" callConvention)* ([";"] callConvention)*
  static boolean procTypeDirectives(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "procTypeDirectives")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = procTypeDirectives_0(b, l + 1);
    r = r && procTypeDirectives_1(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // (";" callConvention)*
  private static boolean procTypeDirectives_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "procTypeDirectives_0")) return false;
    while (true) {
      int c = current_position_(b);
      if (!procTypeDirectives_0_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "procTypeDirectives_0", c)) break;
    }
    return true;
  }

  // ";" callConvention
  private static boolean procTypeDirectives_0_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "procTypeDirectives_0_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, SEMI);
    r = r && callConvention(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // ([";"] callConvention)*
  private static boolean procTypeDirectives_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "procTypeDirectives_1")) return false;
    while (true) {
      int c = current_position_(b);
      if (!procTypeDirectives_1_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "procTypeDirectives_1", c)) break;
    }
    return true;
  }

  // [";"] callConvention
  private static boolean procTypeDirectives_1_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "procTypeDirectives_1_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = procTypeDirectives_1_0_0(b, l + 1);
    r = r && callConvention(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // [";"]
  private static boolean procTypeDirectives_1_0_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "procTypeDirectives_1_0_0")) return false;
    consumeToken(b, SEMI);
    return true;
  }

  /* ********************************************************** */
  // REFERENCE TO procedureTypeHeading
  static boolean procedureReference(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "procedureReference")) return false;
    if (!nextTokenIs(b, REFERENCE)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeTokens(b, 0, REFERENCE, TO);
    r = r && procedureTypeHeading(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // (funcHeading | procHeading) [OF OBJECT]
  static boolean procedureTypeHeading(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "procedureTypeHeading")) return false;
    if (!nextTokenIs(b, "", FUNCTION, PROCEDURE)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = procedureTypeHeading_0(b, l + 1);
    r = r && procedureTypeHeading_1(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // funcHeading | procHeading
  private static boolean procedureTypeHeading_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "procedureTypeHeading_0")) return false;
    boolean r;
    r = funcHeading(b, l + 1);
    if (!r) r = procHeading(b, l + 1);
    return r;
  }

  // [OF OBJECT]
  private static boolean procedureTypeHeading_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "procedureTypeHeading_1")) return false;
    parseTokens(b, 0, OF, OBJECT);
    return true;
  }

  /* ********************************************************** */
  // identifier {}
  static boolean progParam(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "progParam")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = identifier(b, l + 1);
    r = r && progParam_1(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // {}
  private static boolean progParam_1(PsiBuilder b, int l) {
    return true;
  }

  /* ********************************************************** */
  // "," progParam
  static boolean progParamsRest(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "progParamsRest")) return false;
    if (!nextTokenIs(b, COMMA)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_);
    r = consumeToken(b, COMMA);
    p = r; // pin = 1
    r = r && progParam(b, l + 1);
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  /* ********************************************************** */
  // ProgramModuleHead progWoHead
  static boolean progWithHead(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "progWithHead")) return false;
    if (!nextTokenIs(b, PROGRAM)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_);
    r = ProgramModuleHead(b, l + 1);
    p = r; // pin = 1
    r = r && progWoHead(b, l + 1);
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  /* ********************************************************** */
  // [intUsesClause] BlockGlobal "."
  static boolean progWoHead(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "progWoHead")) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_);
    r = progWoHead_0(b, l + 1);
    r = r && BlockGlobal(b, l + 1);
    p = r; // pin = 2
    r = r && consumeToken(b, DOT);
    exit_section_(b, l, m, r, p, PascalParser::rec_section_global);
    return r || p;
  }

  // [intUsesClause]
  private static boolean progWoHead_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "progWoHead_0")) return false;
    intUsesClause(b, l + 1);
    return true;
  }

  /* ********************************************************** */
  static boolean prolog(PsiBuilder b, int l) {
    Marker m = enter_section_(b, l, _NONE_);
    exit_section_(b, l, m, true, false, PascalParser::rec_module_start);
    return true;
  }

  /* ********************************************************** */
  // STRING_LITERAL
  static boolean quotedString(PsiBuilder b, int l) {
    return consumeToken(b, STRING_LITERAL);
  }

  /* ********************************************************** */
  // GENERIC PROCEDURE | GENERIC FUNCTION | PROCEDURE | FUNCTION | CONSTRUCTOR | DESTRUCTOR | OPERATOR
  static boolean rec__routine_key(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "rec__routine_key")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = parseTokens(b, 0, GENERIC, PROCEDURE);
    if (!r) r = parseTokens(b, 0, GENERIC, FUNCTION);
    if (!r) r = consumeToken(b, PROCEDURE);
    if (!r) r = consumeToken(b, FUNCTION);
    if (!r) r = consumeToken(b, CONSTRUCTOR);
    if (!r) r = consumeToken(b, DESTRUCTOR);
    if (!r) r = consumeToken(b, OPERATOR);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // REPEAT | WHILE | FOR | CASE | WITH | RAISE | IF | TRY | BEGIN | BREAK | CONTINUE | EXIT | GOTO | INHERITED
  //                                   | NUMBER_INT | NUMBER_HEX | NUMBER_REAL | NUMBER_OCT | NUMBER_BIN | identifier | '^' | END "." | '[' | '('
  static boolean rec__stmt_start(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "rec__stmt_start")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, REPEAT);
    if (!r) r = consumeToken(b, WHILE);
    if (!r) r = consumeToken(b, FOR);
    if (!r) r = consumeToken(b, CASE);
    if (!r) r = consumeToken(b, WITH);
    if (!r) r = consumeToken(b, RAISE);
    if (!r) r = consumeToken(b, IF);
    if (!r) r = consumeToken(b, TRY);
    if (!r) r = consumeToken(b, BEGIN);
    if (!r) r = consumeToken(b, BREAK);
    if (!r) r = consumeToken(b, CONTINUE);
    if (!r) r = consumeToken(b, EXIT);
    if (!r) r = consumeToken(b, GOTO);
    if (!r) r = consumeToken(b, INHERITED);
    if (!r) r = consumeToken(b, NUMBER_INT);
    if (!r) r = consumeToken(b, NUMBER_HEX);
    if (!r) r = consumeToken(b, NUMBER_REAL);
    if (!r) r = consumeToken(b, NUMBER_OCT);
    if (!r) r = consumeToken(b, NUMBER_BIN);
    if (!r) r = identifier(b, l + 1);
    if (!r) r = consumeToken(b, DEREF);
    if (!r) r = parseTokens(b, 0, END, DOT);
    if (!r) r = consumeToken(b, LBRACK);
    if (!r) r = consumeToken(b, LPAREN);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // !(COMMA | RBRACK | identifier)
  static boolean rec_attr(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "rec_attr")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NOT_);
    r = !rec_attr_0(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // COMMA | RBRACK | identifier
  private static boolean rec_attr_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "rec_attr_0")) return false;
    boolean r;
    r = consumeToken(b, COMMA);
    if (!r) r = consumeToken(b, RBRACK);
    if (!r) r = identifier(b, l + 1);
    return r;
  }

  /* ********************************************************** */
  // !(RBRACK)
  static boolean rec_attr2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "rec_attr2")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NOT_);
    r = !consumeToken(b, RBRACK);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // !(".")
  static boolean rec_block_global_end(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "rec_block_global_end")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NOT_);
    r = !consumeToken(b, DOT);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // !(END | ";") & rec_section
  static boolean rec_block_local_end(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "rec_block_local_end")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = rec_block_local_end_0(b, l + 1);
    r = r && rec_block_local_end_1(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // !(END | ";")
  private static boolean rec_block_local_end_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "rec_block_local_end_0")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NOT_);
    r = !rec_block_local_end_0_0(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // END | ";"
  private static boolean rec_block_local_end_0_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "rec_block_local_end_0_0")) return false;
    boolean r;
    r = consumeToken(b, END);
    if (!r) r = consumeToken(b, SEMI);
    return r;
  }

  // & rec_section
  private static boolean rec_block_local_end_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "rec_block_local_end_1")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _AND_);
    r = rec_section(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // !(ELSE | END) & rec_section
  static boolean rec_case(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "rec_case")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = rec_case_0(b, l + 1);
    r = r && rec_case_1(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // !(ELSE | END)
  private static boolean rec_case_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "rec_case_0")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NOT_);
    r = !rec_case_0_0(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // ELSE | END
  private static boolean rec_case_0_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "rec_case_0_0")) return false;
    boolean r;
    r = consumeToken(b, ELSE);
    if (!r) r = consumeToken(b, END);
    return r;
  }

  // & rec_section
  private static boolean rec_case_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "rec_case_1")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _AND_);
    r = rec_section(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // !(END | ELSE | caseLabel)
  static boolean rec_caseItem(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "rec_caseItem")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NOT_);
    r = !rec_caseItem_0(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // END | ELSE | caseLabel
  private static boolean rec_caseItem_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "rec_caseItem_0")) return false;
    boolean r;
    r = consumeToken(b, END);
    if (!r) r = consumeToken(b, ELSE);
    if (!r) r = caseLabel(b, l + 1);
    return r;
  }

  /* ********************************************************** */
  // !(CASE | FOR | ";") & rec_name
  static boolean rec_classparent(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "rec_classparent")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = rec_classparent_0(b, l + 1);
    r = r && rec_classparent_1(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // !(CASE | FOR | ";")
  private static boolean rec_classparent_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "rec_classparent_0")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NOT_);
    r = !rec_classparent_0_0(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // CASE | FOR | ";"
  private static boolean rec_classparent_0_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "rec_classparent_0_0")) return false;
    boolean r;
    r = consumeToken(b, CASE);
    if (!r) r = consumeToken(b, FOR);
    if (!r) r = consumeToken(b, SEMI);
    return r;
  }

  // & rec_name
  private static boolean rec_classparent_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "rec_classparent_1")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _AND_);
    r = rec_name(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // !(END) & rec_section
  static boolean rec_decl_section(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "rec_decl_section")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = rec_decl_section_0(b, l + 1);
    r = r && rec_decl_section_1(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // !(END)
  private static boolean rec_decl_section_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "rec_decl_section_0")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NOT_);
    r = !consumeToken(b, END);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // & rec_section
  private static boolean rec_decl_section_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "rec_decl_section_1")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _AND_);
    r = rec_section(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // !(identifier) & rec_statement_mid
  static boolean rec_doKey(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "rec_doKey")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = rec_doKey_0(b, l + 1);
    r = r && rec_doKey_1(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // !(identifier)
  private static boolean rec_doKey_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "rec_doKey_0")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NOT_);
    r = !rec_doKey_0_0(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // (identifier)
  private static boolean rec_doKey_0_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "rec_doKey_0_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = identifier(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // & rec_statement_mid
  private static boolean rec_doKey_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "rec_doKey_1")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _AND_);
    r = rec_statement_mid(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // !(")" | ";" | INDEX | "name" | "delayed" | "dependency" | "deprecated" | "platform") & rec_section
  static boolean rec_expr_colon(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "rec_expr_colon")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = rec_expr_colon_0(b, l + 1);
    r = r && rec_expr_colon_1(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // !(")" | ";" | INDEX | "name" | "delayed" | "dependency" | "deprecated" | "platform")
  private static boolean rec_expr_colon_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "rec_expr_colon_0")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NOT_);
    r = !rec_expr_colon_0_0(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // ")" | ";" | INDEX | "name" | "delayed" | "dependency" | "deprecated" | "platform"
  private static boolean rec_expr_colon_0_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "rec_expr_colon_0_0")) return false;
    boolean r;
    r = consumeToken(b, RPAREN);
    if (!r) r = consumeToken(b, SEMI);
    if (!r) r = consumeToken(b, INDEX);
    if (!r) r = consumeToken(b, "name");
    if (!r) r = consumeToken(b, "delayed");
    if (!r) r = consumeToken(b, "dependency");
    if (!r) r = consumeToken(b, "deprecated");
    if (!r) r = consumeToken(b, "platform");
    return r;
  }

  // & rec_section
  private static boolean rec_expr_colon_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "rec_expr_colon_1")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _AND_);
    r = rec_section(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // !(";" | ")" | "]") & rec_section
  static boolean rec_formal_param(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "rec_formal_param")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = rec_formal_param_0(b, l + 1);
    r = r && rec_formal_param_1(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // !(";" | ")" | "]")
  private static boolean rec_formal_param_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "rec_formal_param_0")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NOT_);
    r = !rec_formal_param_0_0(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // ";" | ")" | "]"
  private static boolean rec_formal_param_0_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "rec_formal_param_0_0")) return false;
    boolean r;
    r = consumeToken(b, SEMI);
    if (!r) r = consumeToken(b, RPAREN);
    if (!r) r = consumeToken(b, RBRACK);
    return r;
  }

  // & rec_section
  private static boolean rec_formal_param_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "rec_formal_param_1")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _AND_);
    r = rec_section(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // !(";" | ":" | ">" | "]" | "," | OF | identifier) & rec_section
  static boolean rec_formal_param_sec(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "rec_formal_param_sec")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = rec_formal_param_sec_0(b, l + 1);
    r = r && rec_formal_param_sec_1(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // !(";" | ":" | ">" | "]" | "," | OF | identifier)
  private static boolean rec_formal_param_sec_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "rec_formal_param_sec_0")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NOT_);
    r = !rec_formal_param_sec_0_0(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // ";" | ":" | ">" | "]" | "," | OF | identifier
  private static boolean rec_formal_param_sec_0_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "rec_formal_param_sec_0_0")) return false;
    boolean r;
    r = consumeToken(b, SEMI);
    if (!r) r = consumeToken(b, COLON);
    if (!r) r = consumeToken(b, GT);
    if (!r) r = consumeToken(b, RBRACK);
    if (!r) r = consumeToken(b, COMMA);
    if (!r) r = consumeToken(b, OF);
    if (!r) r = identifier(b, l + 1);
    return r;
  }

  // & rec_section
  private static boolean rec_formal_param_sec_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "rec_formal_param_sec_1")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _AND_);
    r = rec_section(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // !(">" | "," | ":" | ";" | ">""=") & rec_section
  static boolean rec_generic_def(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "rec_generic_def")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = rec_generic_def_0(b, l + 1);
    r = r && rec_generic_def_1(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // !(">" | "," | ":" | ";" | ">""=")
  private static boolean rec_generic_def_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "rec_generic_def_0")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NOT_);
    r = !rec_generic_def_0_0(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // ">" | "," | ":" | ";" | ">""="
  private static boolean rec_generic_def_0_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "rec_generic_def_0_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, GT);
    if (!r) r = consumeToken(b, COMMA);
    if (!r) r = consumeToken(b, COLON);
    if (!r) r = consumeToken(b, SEMI);
    if (!r) r = parseTokens(b, 0, GT, EQ);
    exit_section_(b, m, null, r);
    return r;
  }

  // & rec_section
  private static boolean rec_generic_def_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "rec_generic_def_1")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _AND_);
    r = rec_section(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // !(INITIALIZATION | FINALIZATION | BEGIN | ASM)
  static boolean rec_implementation(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "rec_implementation")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NOT_);
    r = !rec_implementation_0(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // INITIALIZATION | FINALIZATION | BEGIN | ASM
  private static boolean rec_implementation_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "rec_implementation_0")) return false;
    boolean r;
    r = consumeToken(b, INITIALIZATION);
    if (!r) r = consumeToken(b, FINALIZATION);
    if (!r) r = consumeToken(b, BEGIN);
    if (!r) r = consumeToken(b, ASM);
    return r;
  }

  /* ********************************************************** */
  // !(".") & rec_implementation
  static boolean rec_implementation_dot(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "rec_implementation_dot")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = rec_implementation_dot_0(b, l + 1);
    r = r && rec_implementation_dot_1(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // !(".")
  private static boolean rec_implementation_dot_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "rec_implementation_dot_0")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NOT_);
    r = !consumeToken(b, DOT);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // & rec_implementation
  private static boolean rec_implementation_dot_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "rec_implementation_dot_1")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _AND_);
    r = rec_implementation(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // !(IMPLEMENTATION) & rec_implementation
  static boolean rec_interface(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "rec_interface")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = rec_interface_0(b, l + 1);
    r = r && rec_interface_1(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // !(IMPLEMENTATION)
  private static boolean rec_interface_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "rec_interface_0")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NOT_);
    r = !consumeToken(b, IMPLEMENTATION);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // & rec_implementation
  private static boolean rec_interface_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "rec_interface_1")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _AND_);
    r = rec_implementation(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // !(INTERFACE) & rec_interface
  static boolean rec_module(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "rec_module")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = rec_module_0(b, l + 1);
    r = r && rec_module_1(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // !(INTERFACE)
  private static boolean rec_module_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "rec_module_0")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NOT_);
    r = !consumeToken(b, INTERFACE);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // & rec_interface
  private static boolean rec_module_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "rec_module_1")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _AND_);
    r = rec_interface(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // !(UNIT | LIBRARY | PROGRAM | PACKAGE | USES | END) & rec_section
  static boolean rec_module_start(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "rec_module_start")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = rec_module_start_0(b, l + 1);
    r = r && rec_module_start_1(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // !(UNIT | LIBRARY | PROGRAM | PACKAGE | USES | END)
  private static boolean rec_module_start_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "rec_module_start_0")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NOT_);
    r = !rec_module_start_0_0(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // UNIT | LIBRARY | PROGRAM | PACKAGE | USES | END
  private static boolean rec_module_start_0_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "rec_module_start_0_0")) return false;
    boolean r;
    r = consumeToken(b, UNIT);
    if (!r) r = consumeToken(b, LIBRARY);
    if (!r) r = consumeToken(b, PROGRAM);
    if (!r) r = consumeToken(b, PACKAGE);
    if (!r) r = consumeToken(b, USES);
    if (!r) r = consumeToken(b, END);
    return r;
  }

  // & rec_section
  private static boolean rec_module_start_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "rec_module_start_1")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _AND_);
    r = rec_section(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // !(GENERIC | identifier | END) & rec_section
  static boolean rec_name(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "rec_name")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = rec_name_0(b, l + 1);
    r = r && rec_name_1(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // !(GENERIC | identifier | END)
  private static boolean rec_name_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "rec_name_0")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NOT_);
    r = !rec_name_0_0(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // GENERIC | identifier | END
  private static boolean rec_name_0_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "rec_name_0_0")) return false;
    boolean r;
    r = consumeToken(b, GENERIC);
    if (!r) r = identifier(b, l + 1);
    if (!r) r = consumeToken(b, END);
    return r;
  }

  // & rec_section
  private static boolean rec_name_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "rec_name_1")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _AND_);
    r = rec_section(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // !("(" | "<" | ":" | ";" | ".") & rec_section
  static boolean rec_procName(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "rec_procName")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = rec_procName_0(b, l + 1);
    r = r && rec_procName_1(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // !("(" | "<" | ":" | ";" | ".")
  private static boolean rec_procName_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "rec_procName_0")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NOT_);
    r = !rec_procName_0_0(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // "(" | "<" | ":" | ";" | "."
  private static boolean rec_procName_0_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "rec_procName_0_0")) return false;
    boolean r;
    r = consumeToken(b, LPAREN);
    if (!r) r = consumeToken(b, LT);
    if (!r) r = consumeToken(b, COLON);
    if (!r) r = consumeToken(b, SEMI);
    if (!r) r = consumeToken(b, DOT);
    return r;
  }

  // & rec_section
  private static boolean rec_procName_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "rec_procName_1")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _AND_);
    r = rec_section(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // !(USES) & rec_section
  static boolean rec_programHead(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "rec_programHead")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = rec_programHead_0(b, l + 1);
    r = r && rec_programHead_1(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // !(USES)
  private static boolean rec_programHead_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "rec_programHead_0")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NOT_);
    r = !consumeToken(b, USES);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // & rec_section
  private static boolean rec_programHead_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "rec_programHead_1")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _AND_);
    r = rec_section(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // !(READ | WRITE | "add" | "remove" | classPropertyDispInterface | "stored" | SEMI | DEFAULT | IMPLEMENTS)
  static boolean rec_propspec(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "rec_propspec")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NOT_);
    r = !rec_propspec_0(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // READ | WRITE | "add" | "remove" | classPropertyDispInterface | "stored" | SEMI | DEFAULT | IMPLEMENTS
  private static boolean rec_propspec_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "rec_propspec_0")) return false;
    boolean r;
    r = consumeToken(b, READ);
    if (!r) r = consumeToken(b, WRITE);
    if (!r) r = consumeToken(b, "add");
    if (!r) r = consumeToken(b, "remove");
    if (!r) r = classPropertyDispInterface(b, l + 1);
    if (!r) r = consumeToken(b, "stored");
    if (!r) r = consumeToken(b, SEMI);
    if (!r) r = consumeToken(b, DEFAULT);
    if (!r) r = consumeToken(b, IMPLEMENTS);
    return r;
  }

  /* ********************************************************** */
  // !(")" | END)
  static boolean rec_record_colon(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "rec_record_colon")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NOT_);
    r = !rec_record_colon_0(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // ")" | END
  private static boolean rec_record_colon_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "rec_record_colon_0")) return false;
    boolean r;
    r = consumeToken(b, RPAREN);
    if (!r) r = consumeToken(b, END);
    return r;
  }

  /* ********************************************************** */
  // !(EXTERNAL | FORWARD) & rec_name & rec_interface
  static boolean rec_routine(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "rec_routine")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = rec_routine_0(b, l + 1);
    r = r && rec_routine_1(b, l + 1);
    r = r && rec_routine_2(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // !(EXTERNAL | FORWARD)
  private static boolean rec_routine_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "rec_routine_0")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NOT_);
    r = !rec_routine_0_0(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // EXTERNAL | FORWARD
  private static boolean rec_routine_0_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "rec_routine_0_0")) return false;
    boolean r;
    r = consumeToken(b, EXTERNAL);
    if (!r) r = consumeToken(b, FORWARD);
    return r;
  }

  // & rec_name
  private static boolean rec_routine_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "rec_routine_1")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _AND_);
    r = rec_name(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // & rec_interface
  private static boolean rec_routine_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "rec_routine_2")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _AND_);
    r = rec_interface(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // !(EXTERNAL | FORWARD | CASE | ";") & rec_name & rec_interface
  static boolean rec_routine_decl(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "rec_routine_decl")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = rec_routine_decl_0(b, l + 1);
    r = r && rec_routine_decl_1(b, l + 1);
    r = r && rec_routine_decl_2(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // !(EXTERNAL | FORWARD | CASE | ";")
  private static boolean rec_routine_decl_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "rec_routine_decl_0")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NOT_);
    r = !rec_routine_decl_0_0(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // EXTERNAL | FORWARD | CASE | ";"
  private static boolean rec_routine_decl_0_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "rec_routine_decl_0_0")) return false;
    boolean r;
    r = consumeToken(b, EXTERNAL);
    if (!r) r = consumeToken(b, FORWARD);
    if (!r) r = consumeToken(b, CASE);
    if (!r) r = consumeToken(b, SEMI);
    return r;
  }

  // & rec_name
  private static boolean rec_routine_decl_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "rec_routine_decl_1")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _AND_);
    r = rec_name(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // & rec_interface
  private static boolean rec_routine_decl_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "rec_routine_decl_2")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _AND_);
    r = rec_interface(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // !rec__routine_key & rec_section_nested
  static boolean rec_section(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "rec_section")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = rec_section_0(b, l + 1);
    r = r && rec_section_1(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // !rec__routine_key
  private static boolean rec_section_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "rec_section_0")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NOT_);
    r = !rec__routine_key(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // & rec_section_nested
  private static boolean rec_section_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "rec_section_1")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _AND_);
    r = rec_section_nested(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // !(USES) & rec_section
  static boolean rec_sectionKey(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "rec_sectionKey")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = rec_sectionKey_0(b, l + 1);
    r = r && rec_sectionKey_1(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // !(USES)
  private static boolean rec_sectionKey_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "rec_sectionKey_0")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NOT_);
    r = !consumeToken(b, USES);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // & rec_section
  private static boolean rec_sectionKey_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "rec_sectionKey_1")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _AND_);
    r = rec_section(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // !(".") & rec_section
  static boolean rec_section_global(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "rec_section_global")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = rec_section_global_0(b, l + 1);
    r = r && rec_section_global_1(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // !(".")
  private static boolean rec_section_global_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "rec_section_global_0")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NOT_);
    r = !consumeToken(b, DOT);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // & rec_section
  private static boolean rec_section_global_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "rec_section_global_1")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _AND_);
    r = rec_section(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // !(CLASS | TYPE | EXPORTS | varKey | constKey | PROPERTY | Visibility | LABEL | CASE |
  //                                  (identifier "<") | (END DOT) | "[") & rec_interface
  static boolean rec_section_nested(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "rec_section_nested")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = rec_section_nested_0(b, l + 1);
    r = r && rec_section_nested_1(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // !(CLASS | TYPE | EXPORTS | varKey | constKey | PROPERTY | Visibility | LABEL | CASE |
  //                                  (identifier "<") | (END DOT) | "[")
  private static boolean rec_section_nested_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "rec_section_nested_0")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NOT_);
    r = !rec_section_nested_0_0(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // CLASS | TYPE | EXPORTS | varKey | constKey | PROPERTY | Visibility | LABEL | CASE |
  //                                  (identifier "<") | (END DOT) | "["
  private static boolean rec_section_nested_0_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "rec_section_nested_0_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, CLASS);
    if (!r) r = consumeToken(b, TYPE);
    if (!r) r = consumeToken(b, EXPORTS);
    if (!r) r = varKey(b, l + 1);
    if (!r) r = constKey(b, l + 1);
    if (!r) r = consumeToken(b, PROPERTY);
    if (!r) r = Visibility(b, l + 1);
    if (!r) r = consumeToken(b, LABEL);
    if (!r) r = consumeToken(b, CASE);
    if (!r) r = rec_section_nested_0_0_9(b, l + 1);
    if (!r) r = rec_section_nested_0_0_10(b, l + 1);
    if (!r) r = consumeToken(b, LBRACK);
    exit_section_(b, m, null, r);
    return r;
  }

  // identifier "<"
  private static boolean rec_section_nested_0_0_9(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "rec_section_nested_0_0_9")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = identifier(b, l + 1);
    r = r && consumeToken(b, LT);
    exit_section_(b, m, null, r);
    return r;
  }

  // END DOT
  private static boolean rec_section_nested_0_0_10(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "rec_section_nested_0_0_10")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeTokens(b, 0, END, DOT);
    exit_section_(b, m, null, r);
    return r;
  }

  // & rec_interface
  private static boolean rec_section_nested_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "rec_section_nested_1")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _AND_);
    r = rec_interface(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // !(";" | "deprecated" | "platform") & rec_section
  static boolean rec_semi(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "rec_semi")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = rec_semi_0(b, l + 1);
    r = r && rec_semi_1(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // !(";" | "deprecated" | "platform")
  private static boolean rec_semi_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "rec_semi_0")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NOT_);
    r = !rec_semi_0_0(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // ";" | "deprecated" | "platform"
  private static boolean rec_semi_0_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "rec_semi_0_0")) return false;
    boolean r;
    r = consumeToken(b, SEMI);
    if (!r) r = consumeToken(b, "deprecated");
    if (!r) r = consumeToken(b, "platform");
    return r;
  }

  // & rec_section
  private static boolean rec_semi_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "rec_semi_1")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _AND_);
    r = rec_section(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // !(";" | "=" | "," | ">") & rec_section
  static boolean rec_semi_section(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "rec_semi_section")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = rec_semi_section_0(b, l + 1);
    r = r && rec_semi_section_1(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // !(";" | "=" | "," | ">")
  private static boolean rec_semi_section_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "rec_semi_section_0")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NOT_);
    r = !rec_semi_section_0_0(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // ";" | "=" | "," | ">"
  private static boolean rec_semi_section_0_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "rec_semi_section_0_0")) return false;
    boolean r;
    r = consumeToken(b, SEMI);
    if (!r) r = consumeToken(b, EQ);
    if (!r) r = consumeToken(b, COMMA);
    if (!r) r = consumeToken(b, GT);
    return r;
  }

  // & rec_section
  private static boolean rec_semi_section_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "rec_semi_section_1")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _AND_);
    r = rec_section(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // !(SEMI | ELSE | FINALLY | EXCEPT | UNTIL) & rec_statements
  static boolean rec_statement(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "rec_statement")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = rec_statement_0(b, l + 1);
    r = r && rec_statement_1(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // !(SEMI | ELSE | FINALLY | EXCEPT | UNTIL)
  private static boolean rec_statement_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "rec_statement_0")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NOT_);
    r = !rec_statement_0_0(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // SEMI | ELSE | FINALLY | EXCEPT | UNTIL
  private static boolean rec_statement_0_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "rec_statement_0_0")) return false;
    boolean r;
    r = consumeToken(b, SEMI);
    if (!r) r = consumeToken(b, ELSE);
    if (!r) r = consumeToken(b, FINALLY);
    if (!r) r = consumeToken(b, EXCEPT);
    if (!r) r = consumeToken(b, UNTIL);
    return r;
  }

  // & rec_statements
  private static boolean rec_statement_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "rec_statement_1")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _AND_);
    r = rec_statements(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // !(DO | ELSE | THEN | TO | DOWNTO | OF | ON | SEMI | REPEAT | WHILE | FOR | CASE | WITH | RAISE | IF | TRY | FINALLY | EXCEPT | UNTIL | END) & rec_section
  static boolean rec_statement_mid(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "rec_statement_mid")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = rec_statement_mid_0(b, l + 1);
    r = r && rec_statement_mid_1(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // !(DO | ELSE | THEN | TO | DOWNTO | OF | ON | SEMI | REPEAT | WHILE | FOR | CASE | WITH | RAISE | IF | TRY | FINALLY | EXCEPT | UNTIL | END)
  private static boolean rec_statement_mid_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "rec_statement_mid_0")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NOT_);
    r = !rec_statement_mid_0_0(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // DO | ELSE | THEN | TO | DOWNTO | OF | ON | SEMI | REPEAT | WHILE | FOR | CASE | WITH | RAISE | IF | TRY | FINALLY | EXCEPT | UNTIL | END
  private static boolean rec_statement_mid_0_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "rec_statement_mid_0_0")) return false;
    boolean r;
    r = consumeToken(b, DO);
    if (!r) r = consumeToken(b, ELSE);
    if (!r) r = consumeToken(b, THEN);
    if (!r) r = consumeToken(b, TO);
    if (!r) r = consumeToken(b, DOWNTO);
    if (!r) r = consumeToken(b, OF);
    if (!r) r = consumeToken(b, ON);
    if (!r) r = consumeToken(b, SEMI);
    if (!r) r = consumeToken(b, REPEAT);
    if (!r) r = consumeToken(b, WHILE);
    if (!r) r = consumeToken(b, FOR);
    if (!r) r = consumeToken(b, CASE);
    if (!r) r = consumeToken(b, WITH);
    if (!r) r = consumeToken(b, RAISE);
    if (!r) r = consumeToken(b, IF);
    if (!r) r = consumeToken(b, TRY);
    if (!r) r = consumeToken(b, FINALLY);
    if (!r) r = consumeToken(b, EXCEPT);
    if (!r) r = consumeToken(b, UNTIL);
    if (!r) r = consumeToken(b, END);
    return r;
  }

  // & rec_section
  private static boolean rec_statement_mid_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "rec_statement_mid_1")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _AND_);
    r = rec_section(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // !(END | INITIALIZATION | FINALIZATION)
  static boolean rec_statements(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "rec_statements")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NOT_);
    r = !rec_statements_0(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // END | INITIALIZATION | FINALIZATION
  private static boolean rec_statements_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "rec_statements_0")) return false;
    boolean r;
    r = consumeToken(b, END);
    if (!r) r = consumeToken(b, INITIALIZATION);
    if (!r) r = consumeToken(b, FINALIZATION);
    return r;
  }

  /* ********************************************************** */
  // !(";" | ")" | END | CASE | identifier ) & rec_section
  static boolean rec_struct_field(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "rec_struct_field")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = rec_struct_field_0(b, l + 1);
    r = r && rec_struct_field_1(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // !(";" | ")" | END | CASE | identifier )
  private static boolean rec_struct_field_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "rec_struct_field_0")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NOT_);
    r = !rec_struct_field_0_0(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // ";" | ")" | END | CASE | identifier
  private static boolean rec_struct_field_0_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "rec_struct_field_0_0")) return false;
    boolean r;
    r = consumeToken(b, SEMI);
    if (!r) r = consumeToken(b, RPAREN);
    if (!r) r = consumeToken(b, END);
    if (!r) r = consumeToken(b, CASE);
    if (!r) r = identifier(b, l + 1);
    return r;
  }

  // & rec_section
  private static boolean rec_struct_field_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "rec_struct_field_1")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _AND_);
    r = rec_section(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // !(END | ";" | "(")
  static boolean rec_struct_outer(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "rec_struct_outer")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NOT_);
    r = !rec_struct_outer_0(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // END | ";" | "("
  private static boolean rec_struct_outer_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "rec_struct_outer_0")) return false;
    boolean r;
    r = consumeToken(b, END);
    if (!r) r = consumeToken(b, SEMI);
    if (!r) r = consumeToken(b, LPAREN);
    return r;
  }

  /* ********************************************************** */
  // !(";" | "=" | ">" | ",") & rec_section
  static boolean rec_test(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "rec_test")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = rec_test_0(b, l + 1);
    r = r && rec_test_1(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // !(";" | "=" | ">" | ",")
  private static boolean rec_test_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "rec_test_0")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NOT_);
    r = !rec_test_0_0(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // ";" | "=" | ">" | ","
  private static boolean rec_test_0_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "rec_test_0_0")) return false;
    boolean r;
    r = consumeToken(b, SEMI);
    if (!r) r = consumeToken(b, EQ);
    if (!r) r = consumeToken(b, GT);
    if (!r) r = consumeToken(b, COMMA);
    return r;
  }

  // & rec_section
  private static boolean rec_test_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "rec_test_1")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _AND_);
    r = rec_section(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // !("=" | "." | ";" | DO)
  static boolean rec_typeId(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "rec_typeId")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NOT_);
    r = !rec_typeId_0(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // "=" | "." | ";" | DO
  private static boolean rec_typeId_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "rec_typeId_0")) return false;
    boolean r;
    r = consumeToken(b, EQ);
    if (!r) r = consumeToken(b, DOT);
    if (!r) r = consumeToken(b, SEMI);
    if (!r) r = consumeToken(b, DO);
    return r;
  }

  /* ********************************************************** */
  // !(END) & rec_section
  static boolean rec_uses(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "rec_uses")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = rec_uses_0(b, l + 1);
    r = r && rec_uses_1(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // !(END)
  private static boolean rec_uses_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "rec_uses_0")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NOT_);
    r = !consumeToken(b, END);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // & rec_section
  private static boolean rec_uses_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "rec_uses_1")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _AND_);
    r = rec_section(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // recordValue recordValueRest* [";"]
  static boolean recordConstInner(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "recordConstInner")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_);
    r = recordValue(b, l + 1);
    r = r && recordConstInner_1(b, l + 1);
    r = r && recordConstInner_2(b, l + 1);
    exit_section_(b, l, m, r, false, PascalParser::rec_record_colon);
    return r;
  }

  // recordValueRest*
  private static boolean recordConstInner_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "recordConstInner_1")) return false;
    while (true) {
      int c = current_position_(b);
      if (!recordValueRest(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "recordConstInner_1", c)) break;
    }
    return true;
  }

  // [";"]
  private static boolean recordConstInner_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "recordConstInner_2")) return false;
    consumeToken(b, SEMI);
    return true;
  }

  /* ********************************************************** */
  // identifier ":" constExpr
  static boolean recordValue(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "recordValue")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = identifier(b, l + 1);
    r = r && consumeToken(b, COLON);
    r = r && constExpr(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // ";" recordValue
  static boolean recordValueRest(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "recordValueRest")) return false;
    if (!nextTokenIs(b, SEMI)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, SEMI);
    r = r && recordValue(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // CASE [NamedIdentDecl ":"] TypeDecl OF recordVariants
  static boolean recordVariantSection(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "recordVariantSection")) return false;
    if (!nextTokenIs(b, CASE)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_);
    r = consumeToken(b, CASE);
    p = r; // pin = 1
    r = r && report_error_(b, recordVariantSection_1(b, l + 1));
    r = p && report_error_(b, TypeDecl(b, l + 1)) && r;
    r = p && report_error_(b, consumeToken(b, OF)) && r;
    r = p && recordVariants(b, l + 1) && r;
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  // [NamedIdentDecl ":"]
  private static boolean recordVariantSection_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "recordVariantSection_1")) return false;
    recordVariantSection_1_0(b, l + 1);
    return true;
  }

  // NamedIdentDecl ":"
  private static boolean recordVariantSection_1_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "recordVariantSection_1_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = NamedIdentDecl(b, l + 1);
    r = r && consumeToken(b, COLON);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // (RecordVariant ";")* [RecordVariant]
  static boolean recordVariants(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "recordVariants")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = recordVariants_0(b, l + 1);
    r = r && recordVariants_1(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // (RecordVariant ";")*
  private static boolean recordVariants_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "recordVariants_0")) return false;
    while (true) {
      int c = current_position_(b);
      if (!recordVariants_0_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "recordVariants_0", c)) break;
    }
    return true;
  }

  // RecordVariant ";"
  private static boolean recordVariants_0_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "recordVariants_0_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = RecordVariant(b, l + 1);
    r = r && consumeToken(b, SEMI);
    exit_section_(b, m, null, r);
    return r;
  }

  // [RecordVariant]
  private static boolean recordVariants_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "recordVariants_1")) return false;
    RecordVariant(b, l + 1);
    return true;
  }

  /* ********************************************************** */
  // (GT EQ) | LTEQ | LT | GT | NE | EQ | IN | IS
  public static boolean relOp(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "relOp")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, REL_OP, "<rel op>");
    r = relOp_0(b, l + 1);
    if (!r) r = consumeToken(b, LTEQ);
    if (!r) r = consumeToken(b, LT);
    if (!r) r = consumeToken(b, GT);
    if (!r) r = consumeToken(b, NE);
    if (!r) r = consumeToken(b, EQ);
    if (!r) r = consumeToken(b, IN);
    if (!r) r = consumeToken(b, IS);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // GT EQ
  private static boolean relOp_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "relOp_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeTokens(b, 0, GT, EQ);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // PROGRAM | UNIT | LIBRARY | INTERFACE | IMPLEMENTATION | INITIALIZATION | FINALIZATION
  //                               | EXPORTS | USES | VAR | CONST | TYPE | THREADVAR | RESOURCESTRING | CONSTREF | ABSOLUTE
  //                               | PROCEDURE | FUNCTION | OPERATOR | CONSTRUCTOR | DESTRUCTOR | STRICT | PRIVATE | PROTECTED | PUBLIC | PUBLISHED
  //                               | ARRAY | RECORD | SET | FILE | OBJECT | CLASS | OF | PROPERTY | LABEL
  //                               | TRY | RAISE | EXCEPT | FINALLY | ON | GOTO
  //                               | FOR | TO | DOWNTO | REPEAT | UNTIL | WHILE | DO | WITH | BEGIN | END | IF | THEN | ELSE | CASE
  //                               | NIL | FALSE | TRUE | ASM | INHERITED
  //                               | AND | OR | XOR | NOT | SHL | SHR | DIV | MOD | IN | AS | IS
  //                               | INLINE
  static boolean reservedWord(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "reservedWord")) return false;
    boolean r;
    r = consumeToken(b, PROGRAM);
    if (!r) r = consumeToken(b, UNIT);
    if (!r) r = consumeToken(b, LIBRARY);
    if (!r) r = consumeToken(b, INTERFACE);
    if (!r) r = consumeToken(b, IMPLEMENTATION);
    if (!r) r = consumeToken(b, INITIALIZATION);
    if (!r) r = consumeToken(b, FINALIZATION);
    if (!r) r = consumeToken(b, EXPORTS);
    if (!r) r = consumeToken(b, USES);
    if (!r) r = consumeToken(b, VAR);
    if (!r) r = consumeToken(b, CONST);
    if (!r) r = consumeToken(b, TYPE);
    if (!r) r = consumeToken(b, THREADVAR);
    if (!r) r = consumeToken(b, RESOURCESTRING);
    if (!r) r = consumeToken(b, CONSTREF);
    if (!r) r = consumeToken(b, ABSOLUTE);
    if (!r) r = consumeToken(b, PROCEDURE);
    if (!r) r = consumeToken(b, FUNCTION);
    if (!r) r = consumeToken(b, OPERATOR);
    if (!r) r = consumeToken(b, CONSTRUCTOR);
    if (!r) r = consumeToken(b, DESTRUCTOR);
    if (!r) r = consumeToken(b, STRICT);
    if (!r) r = consumeToken(b, PRIVATE);
    if (!r) r = consumeToken(b, PROTECTED);
    if (!r) r = consumeToken(b, PUBLIC);
    if (!r) r = consumeToken(b, PUBLISHED);
    if (!r) r = consumeToken(b, ARRAY);
    if (!r) r = consumeToken(b, RECORD);
    if (!r) r = consumeToken(b, SET);
    if (!r) r = consumeToken(b, FILE);
    if (!r) r = consumeToken(b, OBJECT);
    if (!r) r = consumeToken(b, CLASS);
    if (!r) r = consumeToken(b, OF);
    if (!r) r = consumeToken(b, PROPERTY);
    if (!r) r = consumeToken(b, LABEL);
    if (!r) r = consumeToken(b, TRY);
    if (!r) r = consumeToken(b, RAISE);
    if (!r) r = consumeToken(b, EXCEPT);
    if (!r) r = consumeToken(b, FINALLY);
    if (!r) r = consumeToken(b, ON);
    if (!r) r = consumeToken(b, GOTO);
    if (!r) r = consumeToken(b, FOR);
    if (!r) r = consumeToken(b, TO);
    if (!r) r = consumeToken(b, DOWNTO);
    if (!r) r = consumeToken(b, REPEAT);
    if (!r) r = consumeToken(b, UNTIL);
    if (!r) r = consumeToken(b, WHILE);
    if (!r) r = consumeToken(b, DO);
    if (!r) r = consumeToken(b, WITH);
    if (!r) r = consumeToken(b, BEGIN);
    if (!r) r = consumeToken(b, END);
    if (!r) r = consumeToken(b, IF);
    if (!r) r = consumeToken(b, THEN);
    if (!r) r = consumeToken(b, ELSE);
    if (!r) r = consumeToken(b, CASE);
    if (!r) r = consumeToken(b, NIL);
    if (!r) r = consumeToken(b, FALSE);
    if (!r) r = consumeToken(b, TRUE);
    if (!r) r = consumeToken(b, ASM);
    if (!r) r = consumeToken(b, INHERITED);
    if (!r) r = consumeToken(b, AND);
    if (!r) r = consumeToken(b, OR);
    if (!r) r = consumeToken(b, XOR);
    if (!r) r = consumeToken(b, NOT);
    if (!r) r = consumeToken(b, SHL);
    if (!r) r = consumeToken(b, SHR);
    if (!r) r = consumeToken(b, DIV);
    if (!r) r = consumeToken(b, MOD);
    if (!r) r = consumeToken(b, IN);
    if (!r) r = consumeToken(b, AS);
    if (!r) r = consumeToken(b, IS);
    if (!r) r = consumeToken(b, INLINE);
    return r;
  }

  /* ********************************************************** */
  // ClassMethodResolution | ExportedRoutine
  static boolean routineDecl(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "routineDecl")) return false;
    boolean r;
    r = ClassMethodResolution(b, l + 1);
    if (!r) r = ExportedRoutine(b, l + 1);
    return r;
  }

  /* ********************************************************** */
  // customAttributes* (exportedProc | exportedFunc | operatorDecl | classOperatorDecl) ";" FunctionDirective*
  static boolean routineDeclaration(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "routineDeclaration")) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_);
    r = routineDeclaration_0(b, l + 1);
    r = r && routineDeclaration_1(b, l + 1);
    p = r; // pin = 2
    r = r && report_error_(b, consumeToken(b, SEMI));
    r = p && routineDeclaration_3(b, l + 1) && r;
    exit_section_(b, l, m, r, p, PascalParser::rec_routine_decl);
    return r || p;
  }

  // customAttributes*
  private static boolean routineDeclaration_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "routineDeclaration_0")) return false;
    while (true) {
      int c = current_position_(b);
      if (!customAttributes(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "routineDeclaration_0", c)) break;
    }
    return true;
  }

  // exportedProc | exportedFunc | operatorDecl | classOperatorDecl
  private static boolean routineDeclaration_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "routineDeclaration_1")) return false;
    boolean r;
    r = exportedProc(b, l + 1);
    if (!r) r = exportedFunc(b, l + 1);
    if (!r) r = operatorDecl(b, l + 1);
    if (!r) r = classOperatorDecl(b, l + 1);
    return r;
  }

  // FunctionDirective*
  private static boolean routineDeclaration_3(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "routineDeclaration_3")) return false;
    while (true) {
      int c = current_position_(b);
      if (!FunctionDirective(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "routineDeclaration_3", c)) break;
    }
    return true;
  }

  /* ********************************************************** */
  // customAttributes* (exportedProc | exportedFuncImpl | operatorDecl | classOperatorDecl) ";" FunctionDirective*
  static boolean routineImpl(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "routineImpl")) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_);
    r = routineImpl_0(b, l + 1);
    r = r && routineImpl_1(b, l + 1);
    p = r; // pin = 2
    r = r && report_error_(b, consumeToken(b, SEMI));
    r = p && routineImpl_3(b, l + 1) && r;
    exit_section_(b, l, m, r, p, PascalParser::rec_routine);
    return r || p;
  }

  // customAttributes*
  private static boolean routineImpl_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "routineImpl_0")) return false;
    while (true) {
      int c = current_position_(b);
      if (!customAttributes(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "routineImpl_0", c)) break;
    }
    return true;
  }

  // exportedProc | exportedFuncImpl | operatorDecl | classOperatorDecl
  private static boolean routineImpl_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "routineImpl_1")) return false;
    boolean r;
    r = exportedProc(b, l + 1);
    if (!r) r = exportedFuncImpl(b, l + 1);
    if (!r) r = operatorDecl(b, l + 1);
    if (!r) r = classOperatorDecl(b, l + 1);
    return r;
  }

  // FunctionDirective*
  private static boolean routineImpl_3(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "routineImpl_3")) return false;
    while (true) {
      int c = current_position_(b);
      if (!FunctionDirective(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "routineImpl_3", c)) break;
    }
    return true;
  }

  /* ********************************************************** */
  // ("," | ".." | ) Expr
  static boolean setTail(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "setTail")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = setTail_0(b, l + 1);
    r = r && Expr(b, l + 1, -1);
    exit_section_(b, m, null, r);
    return r;
  }

  // "," | ".." | 
  private static boolean setTail_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "setTail_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, COMMA);
    if (!r) r = consumeToken(b, RANGE);
    if (!r) r = consumeToken(b, SETTAIL_0_2_0);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // procedureTypeHeading
  static boolean simpleProcedureType(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "simpleProcedureType")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_);
    r = procedureTypeHeading(b, l + 1);
    exit_section_(b, l, m, r, false, PascalParser::rec_semi_section);
    return r;
  }

  /* ********************************************************** */
  // flowStatement | stmtSimpleOrAssign | Expression | InlineConstDeclaration
  static boolean simpleStatement(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "simpleStatement")) return false;
    boolean r;
    r = flowStatement(b, l + 1);
    if (!r) r = stmtSimpleOrAssign(b, l + 1);
    if (!r) r = Expression(b, l + 1);
    if (!r) r = InlineConstDeclaration(b, l + 1);
    return r;
  }

  /* ********************************************************** */
  // EnumType | SubRangeType
  static boolean simpleType(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "simpleType")) return false;
    boolean r;
    r = EnumType(b, l + 1);
    if (!r) r = SubRangeType(b, l + 1);
    return r;
  }

  /* ********************************************************** */
  // [statementList]
  static boolean statementBlock(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "statementBlock")) return false;
    Marker m = enter_section_(b, l, _NONE_);
    statementList(b, l + 1);
    exit_section_(b, l, m, true, false, PascalParser::rec_block_local_end);
    return true;
  }

  /* ********************************************************** */
  // [Statement] statements*
  static boolean statementList(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "statementList")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = statementList_0(b, l + 1);
    r = r && statementList_1(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // [Statement]
  private static boolean statementList_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "statementList_0")) return false;
    Statement(b, l + 1);
    return true;
  }

  // statements*
  private static boolean statementList_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "statementList_1")) return false;
    while (true) {
      int c = current_position_(b);
      if (!statements(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "statementList_1", c)) break;
    }
    return true;
  }

  /* ********************************************************** */
  // RepeatStatement | WhileStatement | ForStatement
  //                               | CaseStatement | WithStatement | RaiseStatement | AssemblerStatement
  //                               | IfStatement | TryStatement | simpleStatement | CompoundStatement | stmtEmpty
  static boolean statementPart(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "statementPart")) return false;
    boolean r;
    r = RepeatStatement(b, l + 1);
    if (!r) r = WhileStatement(b, l + 1);
    if (!r) r = ForStatement(b, l + 1);
    if (!r) r = CaseStatement(b, l + 1);
    if (!r) r = WithStatement(b, l + 1);
    if (!r) r = RaiseStatement(b, l + 1);
    if (!r) r = AssemblerStatement(b, l + 1);
    if (!r) r = IfStatement(b, l + 1);
    if (!r) r = TryStatement(b, l + 1);
    if (!r) r = simpleStatement(b, l + 1);
    if (!r) r = CompoundStatement(b, l + 1);
    if (!r) r = stmtEmpty(b, l + 1);
    return r;
  }

  /* ********************************************************** */
  // ";" [Statement]
  static boolean statements(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "statements")) return false;
    if (!nextTokenIs(b, SEMI)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, SEMI);
    r = r && statements_1(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // [Statement]
  private static boolean statements_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "statements_1")) return false;
    Statement(b, l + 1);
    return true;
  }

  /* ********************************************************** */
  public static boolean stmtEmpty(PsiBuilder b, int l) {
    Marker m = enter_section_(b);
    exit_section_(b, m, STMT_EMPTY, true);
    return true;
  }

  /* ********************************************************** */
  // assignLeftPart [AssignPart]
  static boolean stmtSimpleOrAssign(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "stmtSimpleOrAssign")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = assignLeftPart(b, l + 1);
    r = r && stmtSimpleOrAssign_1(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // [AssignPart]
  private static boolean stmtSimpleOrAssign_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "stmtSimpleOrAssign_1")) return false;
    AssignPart(b, l + 1);
    return true;
  }

  /* ********************************************************** */
  // commonDecl | routineDecl | classFieldSemi | Visibility
  static boolean structItem(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "structItem")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_);
    r = commonDecl(b, l + 1);
    if (!r) r = routineDecl(b, l + 1);
    if (!r) r = classFieldSemi(b, l + 1);
    if (!r) r = Visibility(b, l + 1);
    exit_section_(b, l, m, r, false, PascalParser::rec_struct_field);
    return r;
  }

  /* ********************************************************** */
  // ClassHelperDecl | ClassTypeDecl | InterfaceTypeDecl | ObjectDecl | RecordHelperDecl | RecordDecl
  static boolean structTypeDecl(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "structTypeDecl")) return false;
    boolean r;
    r = ClassHelperDecl(b, l + 1);
    if (!r) r = ClassTypeDecl(b, l + 1);
    if (!r) r = InterfaceTypeDecl(b, l + 1);
    if (!r) r = ObjectDecl(b, l + 1);
    if (!r) r = RecordHelperDecl(b, l + 1);
    if (!r) r = RecordDecl(b, l + 1);
    return r;
  }

  /* ********************************************************** */
  // exceptKey handlerList
  static boolean tryExcept(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "tryExcept")) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_);
    r = exceptKey(b, l + 1);
    p = r; // pin = 1
    r = r && handlerList(b, l + 1);
    exit_section_(b, l, m, r, p, PascalParser::rec_statement_mid);
    return r || p;
  }

  /* ********************************************************** */
  // FINALLY statementList
  static boolean tryFinally(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "tryFinally")) return false;
    if (!nextTokenIs(b, FINALLY)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, FINALLY);
    r = r && statementList(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // typeForwardDecl | typeDecl
  static boolean type(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "type")) return false;
    boolean r;
    r = typeForwardDecl(b, l + 1);
    if (!r) r = typeDecl(b, l + 1);
    return r;
  }

  /* ********************************************************** */
  // TypeDecl hintingDirective* ";"
  static boolean typeDecl(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "typeDecl")) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_);
    r = TypeDecl(b, l + 1);
    p = r; // pin = 1
    r = r && report_error_(b, typeDecl_1(b, l + 1));
    r = p && consumeToken(b, SEMI) && r;
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  // hintingDirective*
  private static boolean typeDecl_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "typeDecl_1")) return false;
    while (true) {
      int c = current_position_(b);
      if (!hintingDirective(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "typeDecl_1", c)) break;
    }
    return true;
  }

  /* ********************************************************** */
  // TypeDeclaration+
  static boolean typeDeclarations(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "typeDeclarations")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_);
    r = TypeDeclaration(b, l + 1);
    while (r) {
      int c = current_position_(b);
      if (!TypeDeclaration(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "typeDeclarations", c)) break;
    }
    exit_section_(b, l, m, r, false, PascalParser::rec_block_local_end);
    return r;
  }

  /* ********************************************************** */
  // "," TypeDecl
  static boolean typeDeclsRest(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "typeDeclsRest")) return false;
    if (!nextTokenIs(b, COMMA)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_);
    r = consumeToken(b, COMMA);
    p = r; // pin = 1
    r = r && TypeDecl(b, l + 1);
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  /* ********************************************************** */
  // classForwardDecl ";"
  static boolean typeForwardDecl(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "typeForwardDecl")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = classForwardDecl(b, l + 1);
    r = r && consumeToken(b, SEMI);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // NamedIdent identsRest*
  static boolean typeParamIdentList(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "typeParamIdentList")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = NamedIdent(b, l + 1);
    r = r && typeParamIdentList_1(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // identsRest*
  private static boolean typeParamIdentList_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "typeParamIdentList_1")) return false;
    while (true) {
      int c = current_position_(b);
      if (!identsRest(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "typeParamIdentList_1", c)) break;
    }
    return true;
  }

  /* ********************************************************** */
  // ConstrainedTypeParam typeParamListRest*
  static boolean typeParamList(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "typeParamList")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = ConstrainedTypeParam(b, l + 1);
    r = r && typeParamList_1(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // typeParamListRest*
  private static boolean typeParamList_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "typeParamList_1")) return false;
    while (true) {
      int c = current_position_(b);
      if (!typeParamListRest(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "typeParamList_1", c)) break;
    }
    return true;
  }

  /* ********************************************************** */
  // ";" ConstrainedTypeParam
  static boolean typeParamListRest(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "typeParamListRest")) return false;
    if (!nextTokenIs(b, SEMI)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, SEMI);
    r = r && ConstrainedTypeParam(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // "+" | "-" | "@" | NOT
  public static boolean unaryOp(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "unaryOp")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, UNARY_OP, "<unary op>");
    r = consumeToken(b, PLUS);
    if (!r) r = consumeToken(b, MINUS);
    if (!r) r = consumeToken(b, AT);
    if (!r) r = consumeToken(b, NOT);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // reservedWord | KeywordIdent | NAME
  static boolean unescapedIdent(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "unescapedIdent")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, null, "<Identifier>");
    r = reservedWord(b, l + 1);
    if (!r) r = KeywordIdent(b, l + 1);
    if (!r) r = consumeToken(b, NAME);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // [UnitInitialization] [UnitFinalization] END | CompoundStatement | END {}
  static boolean unitBlock(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "unitBlock")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = unitBlock_0(b, l + 1);
    if (!r) r = CompoundStatement(b, l + 1);
    if (!r) r = unitBlock_2(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // [UnitInitialization] [UnitFinalization] END
  private static boolean unitBlock_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "unitBlock_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = unitBlock_0_0(b, l + 1);
    r = r && unitBlock_0_1(b, l + 1);
    r = r && consumeToken(b, END);
    exit_section_(b, m, null, r);
    return r;
  }

  // [UnitInitialization]
  private static boolean unitBlock_0_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "unitBlock_0_0")) return false;
    UnitInitialization(b, l + 1);
    return true;
  }

  // [UnitFinalization]
  private static boolean unitBlock_0_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "unitBlock_0_1")) return false;
    UnitFinalization(b, l + 1);
    return true;
  }

  // END {}
  private static boolean unitBlock_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "unitBlock_2")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, END);
    r = r && unitBlock_2_1(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // {}
  private static boolean unitBlock_2_1(PsiBuilder b, int l) {
    return true;
  }

  /* ********************************************************** */
  // IMPLEMENTATION
  static boolean unitImplementationKey(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "unitImplementationKey")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_);
    r = consumeToken(b, IMPLEMENTATION);
    exit_section_(b, l, m, r, false, PascalParser::rec_sectionKey);
    return r;
  }

  /* ********************************************************** */
  // INTERFACE
  static boolean unitInterfaceKey(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "unitInterfaceKey")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_);
    r = consumeToken(b, INTERFACE);
    exit_section_(b, l, m, r, false, PascalParser::rec_sectionKey);
    return r;
  }

  /* ********************************************************** */
  // VarDeclaration+
  static boolean varDeclarations(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "varDeclarations")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_);
    r = VarDeclaration(b, l + 1);
    while (r) {
      int c = current_position_(b);
      if (!VarDeclaration(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "varDeclarations", c)) break;
    }
    exit_section_(b, l, m, r, false, PascalParser::rec_block_local_end);
    return r;
  }

  /* ********************************************************** */
  // VAR | THREADVAR
  static boolean varKey(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "varKey")) return false;
    if (!nextTokenIs(b, "", THREADVAR, VAR)) return false;
    boolean r;
    r = consumeToken(b, VAR);
    if (!r) r = consumeToken(b, THREADVAR);
    return r;
  }

  /* ********************************************************** */
  // [classParentWORec] structItem* [ClassField] [recordVariantSection]
  static boolean varRecDecl(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "varRecDecl")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_);
    r = varRecDecl_0(b, l + 1);
    r = r && varRecDecl_1(b, l + 1);
    r = r && varRecDecl_2(b, l + 1);
    r = r && varRecDecl_3(b, l + 1);
    exit_section_(b, l, m, r, false, PascalParser::rec_struct_field);
    return r;
  }

  // [classParentWORec]
  private static boolean varRecDecl_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "varRecDecl_0")) return false;
    classParentWORec(b, l + 1);
    return true;
  }

  // structItem*
  private static boolean varRecDecl_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "varRecDecl_1")) return false;
    while (true) {
      int c = current_position_(b);
      if (!structItem(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "varRecDecl_1", c)) break;
    }
    return true;
  }

  // [ClassField]
  private static boolean varRecDecl_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "varRecDecl_2")) return false;
    ClassField(b, l + 1);
    return true;
  }

  // [recordVariantSection]
  private static boolean varRecDecl_3(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "varRecDecl_3")) return false;
    recordVariantSection(b, l + 1);
    return true;
  }

  /* ********************************************************** */
  // (ClassField ";")* [ClassField] [recordVariantSection]
  static boolean varRecDeclInner(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "varRecDeclInner")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_);
    r = varRecDeclInner_0(b, l + 1);
    r = r && varRecDeclInner_1(b, l + 1);
    r = r && varRecDeclInner_2(b, l + 1);
    exit_section_(b, l, m, r, false, PascalParser::rec_struct_field);
    return r;
  }

  // (ClassField ";")*
  private static boolean varRecDeclInner_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "varRecDeclInner_0")) return false;
    while (true) {
      int c = current_position_(b);
      if (!varRecDeclInner_0_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "varRecDeclInner_0", c)) break;
    }
    return true;
  }

  // ClassField ";"
  private static boolean varRecDeclInner_0_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "varRecDeclInner_0_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = ClassField(b, l + 1);
    r = r && consumeToken(b, SEMI);
    exit_section_(b, m, null, r);
    return r;
  }

  // [ClassField]
  private static boolean varRecDeclInner_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "varRecDeclInner_1")) return false;
    ClassField(b, l + 1);
    return true;
  }

  // [recordVariantSection]
  private static boolean varRecDeclInner_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "varRecDeclInner_2")) return false;
    recordVariantSection(b, l + 1);
    return true;
  }

  /* ********************************************************** */
  // "varargs"
  static boolean varagrs(PsiBuilder b, int l) {
    return consumeToken(b, "varargs");
  }

  /* ********************************************************** */
  // Expression
  static boolean whileExpression(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "whileExpression")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_);
    r = Expression(b, l + 1);
    exit_section_(b, l, m, r, false, PascalParser::rec_statement_mid);
    return r;
  }

  /* ********************************************************** */
  // Expression designatorsRest*
  static boolean withArgument(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "withArgument")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_);
    r = Expression(b, l + 1);
    r = r && withArgument_1(b, l + 1);
    exit_section_(b, l, m, r, false, PascalParser::rec_statement_mid);
    return r;
  }

  // designatorsRest*
  private static boolean withArgument_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "withArgument_1")) return false;
    while (true) {
      int c = current_position_(b);
      if (!designatorsRest(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "withArgument_1", c)) break;
    }
    return true;
  }

  /* ********************************************************** */
  // Expression root: Expr
  // Operator priority table:
  // 0: BINARY(sumExpr)
  // 1: BINARY(productExpr)
  // 2: BINARY(relationalExpr)
  // 3: PREFIX(unaryExpr)
  // 4: POSTFIX(referenceExpr)
  // 5: POSTFIX(callExpr) POSTFIX(indexExpr) POSTFIX(dereferenceExpr)
  // 6: ATOM(simpleRefExpr) ATOM(literalExpr) ATOM(parenExpr) ATOM(setExpr)
  //    ATOM(ClosureExpr)
  public static boolean Expr(PsiBuilder b, int l, int g) {
    if (!recursion_guard_(b, l, "Expr")) return false;
    addVariant(b, "<expr>");
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, "<expr>");
    r = unaryExpr(b, l + 1);
    if (!r) r = simpleRefExpr(b, l + 1);
    if (!r) r = literalExpr(b, l + 1);
    if (!r) r = parenExpr(b, l + 1);
    if (!r) r = setExpr(b, l + 1);
    if (!r) r = ClosureExpr(b, l + 1);
    p = r;
    r = r && Expr_0(b, l + 1, g);
    exit_section_(b, l, m, null, r, p, null);
    return r || p;
  }

  public static boolean Expr_0(PsiBuilder b, int l, int g) {
    if (!recursion_guard_(b, l, "Expr_0")) return false;
    boolean r = true;
    while (true) {
      Marker m = enter_section_(b, l, _LEFT_, null);
      if (g < 0 && addOp(b, l + 1)) {
        r = Expr(b, l, 0);
        exit_section_(b, l, m, SUM_EXPR, r, true, null);
      }
      else if (g < 1 && mulOp(b, l + 1)) {
        r = Expr(b, l, 1);
        exit_section_(b, l, m, PRODUCT_EXPR, r, true, null);
      }
      else if (g < 2 && relOp(b, l + 1)) {
        r = Expr(b, l, 2);
        exit_section_(b, l, m, RELATIONAL_EXPR, r, true, null);
      }
      else if (g < 4 && referenceExpr_0(b, l + 1)) {
        r = true;
        exit_section_(b, l, m, REFERENCE_EXPR, r, true, null);
      }
      else if (g < 5 && ArgumentList(b, l + 1)) {
        r = true;
        exit_section_(b, l, m, CALL_EXPR, r, true, null);
      }
      else if (g < 5 && indexList(b, l + 1)) {
        r = true;
        exit_section_(b, l, m, INDEX_EXPR, r, true, null);
      }
      else if (g < 5 && consumeTokenSmart(b, DEREF)) {
        r = true;
        exit_section_(b, l, m, DEREFERENCE_EXPR, r, true, null);
      }
      else {
        exit_section_(b, l, m, null, false, false, null);
        break;
      }
    }
    return r;
  }

  public static boolean unaryExpr(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "unaryExpr")) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, null);
    r = unaryOp(b, l + 1);
    p = r;
    r = p && Expr(b, l, 3);
    exit_section_(b, l, m, UNARY_EXPR, r, p, null);
    return r || p;
  }

  // '.' FullyQualifiedIdent [GenericPostfix]
  private static boolean referenceExpr_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "referenceExpr_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeTokenSmart(b, DOT);
    r = r && FullyQualifiedIdent(b, l + 1);
    r = r && referenceExpr_0_2(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // [GenericPostfix]
  private static boolean referenceExpr_0_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "referenceExpr_0_2")) return false;
    GenericPostfix(b, l + 1);
    return true;
  }

  // ((InheritedCall | SPECIALIZE)? FullyQualifiedIdent [GenericPostfix]) | InheritedCall
  public static boolean simpleRefExpr(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "simpleRefExpr")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, REFERENCE_EXPR, "<simple ref expr>");
    r = simpleRefExpr_0(b, l + 1);
    if (!r) r = InheritedCall(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // (InheritedCall | SPECIALIZE)? FullyQualifiedIdent [GenericPostfix]
  private static boolean simpleRefExpr_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "simpleRefExpr_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = simpleRefExpr_0_0(b, l + 1);
    r = r && FullyQualifiedIdent(b, l + 1);
    r = r && simpleRefExpr_0_2(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // (InheritedCall | SPECIALIZE)?
  private static boolean simpleRefExpr_0_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "simpleRefExpr_0_0")) return false;
    simpleRefExpr_0_0_0(b, l + 1);
    return true;
  }

  // InheritedCall | SPECIALIZE
  private static boolean simpleRefExpr_0_0_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "simpleRefExpr_0_0_0")) return false;
    boolean r;
    r = InheritedCall(b, l + 1);
    if (!r) r = consumeTokenSmart(b, SPECIALIZE);
    return r;
  }

  // [GenericPostfix]
  private static boolean simpleRefExpr_0_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "simpleRefExpr_0_2")) return false;
    GenericPostfix(b, l + 1);
    return true;
  }

  // NUMBER_REAL | NUMBER_INT | NUMBER_HEX | NUMBER_OCT | NUMBER_BIN | TRUE | FALSE | NIL | StringFactor
  public static boolean literalExpr(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "literalExpr")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, LITERAL_EXPR, "<literal expr>");
    r = consumeTokenSmart(b, NUMBER_REAL);
    if (!r) r = consumeTokenSmart(b, NUMBER_INT);
    if (!r) r = consumeTokenSmart(b, NUMBER_HEX);
    if (!r) r = consumeTokenSmart(b, NUMBER_OCT);
    if (!r) r = consumeTokenSmart(b, NUMBER_BIN);
    if (!r) r = consumeTokenSmart(b, TRUE);
    if (!r) r = consumeTokenSmart(b, FALSE);
    if (!r) r = consumeTokenSmart(b, NIL);
    if (!r) r = StringFactor(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // parenConstruct
  public static boolean parenExpr(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "parenExpr")) return false;
    if (!nextTokenIsSmart(b, LPAREN)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = parenConstruct(b, l + 1);
    exit_section_(b, m, PAREN_EXPR, r);
    return r;
  }

  // '[' [ !']' Expr setTail * ] ']'
  public static boolean setExpr(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "setExpr")) return false;
    if (!nextTokenIsSmart(b, LBRACK)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, SET_EXPR, null);
    r = consumeTokenSmart(b, LBRACK);
    p = r; // pin = 1
    r = r && report_error_(b, setExpr_1(b, l + 1));
    r = p && consumeToken(b, RBRACK) && r;
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  // [ !']' Expr setTail * ]
  private static boolean setExpr_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "setExpr_1")) return false;
    setExpr_1_0(b, l + 1);
    return true;
  }

  // !']' Expr setTail *
  private static boolean setExpr_1_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "setExpr_1_0")) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_);
    r = setExpr_1_0_0(b, l + 1);
    p = r; // pin = 1
    r = r && report_error_(b, Expr(b, l + 1, -1));
    r = p && setExpr_1_0_2(b, l + 1) && r;
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  // !']'
  private static boolean setExpr_1_0_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "setExpr_1_0_0")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NOT_);
    r = !consumeTokenSmart(b, RBRACK);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // setTail *
  private static boolean setExpr_1_0_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "setExpr_1_0_2")) return false;
    while (true) {
      int c = current_position_(b);
      if (!setTail(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "setExpr_1_0_2", c)) break;
    }
    return true;
  }

  // ClosureRoutine
  public static boolean ClosureExpr(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ClosureExpr")) return false;
    if (!nextTokenIsSmart(b, FUNCTION, PROCEDURE)) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, CLOSURE_EXPR, "<closure expr>");
    r = ClosureRoutine(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

}
