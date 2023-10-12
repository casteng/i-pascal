// This is a generated file. Not intended for manual editing.
package com.siberika.idea.pascal.lang.psi.impl;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.util.PsiTreeUtil;
import static com.siberika.idea.pascal.lang.psi.PasTypes.*;
import com.siberika.idea.pascal.lang.psi.*;

public class PasEscapedIdentImpl extends PascalNamedElementImpl implements PasEscapedIdent {

  public PasEscapedIdentImpl(ASTNode node) {
    super(node);
  }

  public void accept(@NotNull PasVisitor visitor) {
    visitor.visitEscapedIdent(this);
  }

  @Override
  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof PasVisitor) accept((PasVisitor)visitor);
    else super.accept(visitor);
  }

  @Override
  @Nullable
  public PsiElement getAbsolute_() {
    return findChildByType(ABSOLUTE_);
  }

  @Override
  @Nullable
  public PsiElement getAbstract_() {
    return findChildByType(ABSTRACT_);
  }

  @Override
  @Nullable
  public PsiElement getAnd_() {
    return findChildByType(AND_);
  }

  @Override
  @Nullable
  public PsiElement getArray_() {
    return findChildByType(ARRAY_);
  }

  @Override
  @Nullable
  public PsiElement getAsm_() {
    return findChildByType(ASM_);
  }

  @Override
  @Nullable
  public PsiElement getAssembler_() {
    return findChildByType(ASSEMBLER_);
  }

  @Override
  @Nullable
  public PsiElement getAs_() {
    return findChildByType(AS_);
  }

  @Override
  @Nullable
  public PsiElement getAutomated_() {
    return findChildByType(AUTOMATED_);
  }

  @Override
  @Nullable
  public PsiElement getBegin_() {
    return findChildByType(BEGIN_);
  }

  @Override
  @Nullable
  public PsiElement getBreak_() {
    return findChildByType(BREAK_);
  }

  @Override
  @Nullable
  public PsiElement getCase_() {
    return findChildByType(CASE_);
  }

  @Override
  @Nullable
  public PsiElement getCdecl_() {
    return findChildByType(CDECL_);
  }

  @Override
  @Nullable
  public PsiElement getClass_() {
    return findChildByType(CLASS_);
  }

  @Override
  @Nullable
  public PsiElement getConstref_() {
    return findChildByType(CONSTREF_);
  }

  @Override
  @Nullable
  public PsiElement getConstructor_() {
    return findChildByType(CONSTRUCTOR_);
  }

  @Override
  @Nullable
  public PsiElement getConst_() {
    return findChildByType(CONST_);
  }

  @Override
  @Nullable
  public PsiElement getContains_() {
    return findChildByType(CONTAINS_);
  }

  @Override
  @Nullable
  public PsiElement getContinue_() {
    return findChildByType(CONTINUE_);
  }

  @Override
  @Nullable
  public PsiElement getDefault_() {
    return findChildByType(DEFAULT_);
  }

  @Override
  @Nullable
  public PsiElement getDeprecated_() {
    return findChildByType(DEPRECATED_);
  }

  @Override
  @Nullable
  public PsiElement getDestructor_() {
    return findChildByType(DESTRUCTOR_);
  }

  @Override
  @Nullable
  public PsiElement getDispid_() {
    return findChildByType(DISPID_);
  }

  @Override
  @Nullable
  public PsiElement getDownto_() {
    return findChildByType(DOWNTO_);
  }

  @Override
  @Nullable
  public PsiElement getDo_() {
    return findChildByType(DO_);
  }

  @Override
  @Nullable
  public PsiElement getDynamic_() {
    return findChildByType(DYNAMIC_);
  }

  @Override
  @Nullable
  public PsiElement getElse_() {
    return findChildByType(ELSE_);
  }

  @Override
  @Nullable
  public PsiElement getEnd_() {
    return findChildByType(END_);
  }

  @Override
  @Nullable
  public PsiElement getExcept_() {
    return findChildByType(EXCEPT_);
  }

  @Override
  @Nullable
  public PsiElement getExit_() {
    return findChildByType(EXIT_);
  }

  @Override
  @Nullable
  public PsiElement getExperimental_() {
    return findChildByType(EXPERIMENTAL_);
  }

  @Override
  @Nullable
  public PsiElement getExports_() {
    return findChildByType(EXPORTS_);
  }

  @Override
  @Nullable
  public PsiElement getExport_() {
    return findChildByType(EXPORT_);
  }

  @Override
  @Nullable
  public PsiElement getExternal_() {
    return findChildByType(EXTERNAL_);
  }

  @Override
  @Nullable
  public PsiElement getFalse_() {
    return findChildByType(FALSE_);
  }

  @Override
  @Nullable
  public PsiElement getFile_() {
    return findChildByType(FILE_);
  }

  @Override
  @Nullable
  public PsiElement getFinalization_() {
    return findChildByType(FINALIZATION_);
  }

  @Override
  @Nullable
  public PsiElement getFinally_() {
    return findChildByType(FINALLY_);
  }

  @Override
  @Nullable
  public PsiElement getFinal_() {
    return findChildByType(FINAL_);
  }

  @Override
  @Nullable
  public PsiElement getForward_() {
    return findChildByType(FORWARD_);
  }

  @Override
  @Nullable
  public PsiElement getFor_() {
    return findChildByType(FOR_);
  }

  @Override
  @Nullable
  public PsiElement getFunction_() {
    return findChildByType(FUNCTION_);
  }

  @Override
  @Nullable
  public PsiElement getGoto_() {
    return findChildByType(GOTO_);
  }

  @Override
  @Nullable
  public PsiElement getHelper_() {
    return findChildByType(HELPER_);
  }

  @Override
  @Nullable
  public PsiElement getIf_() {
    return findChildByType(IF_);
  }

  @Override
  @Nullable
  public PsiElement getImplementation_() {
    return findChildByType(IMPLEMENTATION_);
  }

  @Override
  @Nullable
  public PsiElement getImplements_() {
    return findChildByType(IMPLEMENTS_);
  }

  @Override
  @Nullable
  public PsiElement getIndex_() {
    return findChildByType(INDEX_);
  }

  @Override
  @Nullable
  public PsiElement getInherited_() {
    return findChildByType(INHERITED_);
  }

  @Override
  @Nullable
  public PsiElement getInitialization_() {
    return findChildByType(INITIALIZATION_);
  }

  @Override
  @Nullable
  public PsiElement getInline_() {
    return findChildByType(INLINE_);
  }

  @Override
  @Nullable
  public PsiElement getInterface_() {
    return findChildByType(INTERFACE_);
  }

  @Override
  @Nullable
  public PsiElement getIn_() {
    return findChildByType(IN_);
  }

  @Override
  @Nullable
  public PsiElement getIs_() {
    return findChildByType(IS_);
  }

  @Override
  @Nullable
  public PsiElement getLabel_() {
    return findChildByType(LABEL_);
  }

  @Override
  @Nullable
  public PsiElement getLibrary_() {
    return findChildByType(LIBRARY_);
  }

  @Override
  @Nullable
  public PsiElement getMessage_() {
    return findChildByType(MESSAGE_);
  }

  @Override
  @Nullable
  public PsiElement getMod_() {
    return findChildByType(MOD_);
  }

  @Override
  @Nullable
  public PsiElement getName_() {
    return findChildByType(NAME_);
  }

  @Override
  @Nullable
  public PsiElement getNil_() {
    return findChildByType(NIL_);
  }

  @Override
  @Nullable
  public PsiElement getNot_() {
    return findChildByType(NOT_);
  }

  @Override
  @Nullable
  public PsiElement getObject_() {
    return findChildByType(OBJECT_);
  }

  @Override
  @Nullable
  public PsiElement getOf_() {
    return findChildByType(OF_);
  }

  @Override
  @Nullable
  public PsiElement getOn_() {
    return findChildByType(ON_);
  }

  @Override
  @Nullable
  public PsiElement getOperator_() {
    return findChildByType(OPERATOR_);
  }

  @Override
  @Nullable
  public PsiElement getOr_() {
    return findChildByType(OR_);
  }

  @Override
  @Nullable
  public PsiElement getOut_() {
    return findChildByType(OUT_);
  }

  @Override
  @Nullable
  public PsiElement getOverload_() {
    return findChildByType(OVERLOAD_);
  }

  @Override
  @Nullable
  public PsiElement getOverride_() {
    return findChildByType(OVERRIDE_);
  }

  @Override
  @Nullable
  public PsiElement getPackage_() {
    return findChildByType(PACKAGE_);
  }

  @Override
  @Nullable
  public PsiElement getPascal_() {
    return findChildByType(PASCAL_);
  }

  @Override
  @Nullable
  public PsiElement getPlatform_() {
    return findChildByType(PLATFORM_);
  }

  @Override
  @Nullable
  public PsiElement getPrivate_() {
    return findChildByType(PRIVATE_);
  }

  @Override
  @Nullable
  public PsiElement getProcedure_() {
    return findChildByType(PROCEDURE_);
  }

  @Override
  @Nullable
  public PsiElement getProgram_() {
    return findChildByType(PROGRAM_);
  }

  @Override
  @Nullable
  public PsiElement getProperty_() {
    return findChildByType(PROPERTY_);
  }

  @Override
  @Nullable
  public PsiElement getProtected_() {
    return findChildByType(PROTECTED_);
  }

  @Override
  @Nullable
  public PsiElement getPublic_() {
    return findChildByType(PUBLIC_);
  }

  @Override
  @Nullable
  public PsiElement getPublished_() {
    return findChildByType(PUBLISHED_);
  }

  @Override
  @Nullable
  public PsiElement getRaise_() {
    return findChildByType(RAISE_);
  }

  @Override
  @Nullable
  public PsiElement getRead_() {
    return findChildByType(READ_);
  }

  @Override
  @Nullable
  public PsiElement getRecord_() {
    return findChildByType(RECORD_);
  }

  @Override
  @Nullable
  public PsiElement getReference_() {
    return findChildByType(REFERENCE_);
  }

  @Override
  @Nullable
  public PsiElement getRegister_() {
    return findChildByType(REGISTER_);
  }

  @Override
  @Nullable
  public PsiElement getReintroduce_() {
    return findChildByType(REINTRODUCE_);
  }

  @Override
  @Nullable
  public PsiElement getRepeat_() {
    return findChildByType(REPEAT_);
  }

  @Override
  @Nullable
  public PsiElement getRequires_() {
    return findChildByType(REQUIRES_);
  }

  @Override
  @Nullable
  public PsiElement getResourcestring_() {
    return findChildByType(RESOURCESTRING_);
  }

  @Override
  @Nullable
  public PsiElement getSafecall_() {
    return findChildByType(SAFECALL_);
  }

  @Override
  @Nullable
  public PsiElement getSealed_() {
    return findChildByType(SEALED_);
  }

  @Override
  @Nullable
  public PsiElement getSelf_() {
    return findChildByType(SELF_);
  }

  @Override
  @Nullable
  public PsiElement getSet_() {
    return findChildByType(SET_);
  }

  @Override
  @Nullable
  public PsiElement getShl_() {
    return findChildByType(SHL_);
  }

  @Override
  @Nullable
  public PsiElement getShr_() {
    return findChildByType(SHR_);
  }

  @Override
  @Nullable
  public PsiElement getStatic_() {
    return findChildByType(STATIC_);
  }

  @Override
  @Nullable
  public PsiElement getStdcall_() {
    return findChildByType(STDCALL_);
  }

  @Override
  @Nullable
  public PsiElement getStrict_() {
    return findChildByType(STRICT_);
  }

  @Override
  @Nullable
  public PsiElement getThen_() {
    return findChildByType(THEN_);
  }

  @Override
  @Nullable
  public PsiElement getThreadvar_() {
    return findChildByType(THREADVAR_);
  }

  @Override
  @Nullable
  public PsiElement getTo_() {
    return findChildByType(TO_);
  }

  @Override
  @Nullable
  public PsiElement getTrue_() {
    return findChildByType(TRUE_);
  }

  @Override
  @Nullable
  public PsiElement getTry_() {
    return findChildByType(TRY_);
  }

  @Override
  @Nullable
  public PsiElement getType_() {
    return findChildByType(TYPE_);
  }

  @Override
  @Nullable
  public PsiElement getUnit_() {
    return findChildByType(UNIT_);
  }

  @Override
  @Nullable
  public PsiElement getUntil_() {
    return findChildByType(UNTIL_);
  }

  @Override
  @Nullable
  public PsiElement getUses_() {
    return findChildByType(USES_);
  }

  @Override
  @Nullable
  public PsiElement getVar_() {
    return findChildByType(VAR_);
  }

  @Override
  @Nullable
  public PsiElement getVirtual_() {
    return findChildByType(VIRTUAL_);
  }

  @Override
  @Nullable
  public PsiElement getWhile_() {
    return findChildByType(WHILE_);
  }

  @Override
  @Nullable
  public PsiElement getWith_() {
    return findChildByType(WITH_);
  }

  @Override
  @Nullable
  public PsiElement getWrite_() {
    return findChildByType(WRITE_);
  }

  @Override
  @Nullable
  public PsiElement getXor_() {
    return findChildByType(XOR_);
  }

}
