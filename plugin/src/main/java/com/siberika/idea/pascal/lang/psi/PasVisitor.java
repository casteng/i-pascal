// This is a generated file. Not intended for manual editing.
package com.siberika.idea.pascal.lang.psi;

import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.ContributedReferenceHost;

public class PasVisitor extends PsiElementVisitor {

  public void visitArgumentList(@NotNull PasArgumentList o) {
    visitcalPsiElement(o);
  }

  public void visitArrayConstExpr(@NotNull PasArrayConstExpr o) {
    visitExpr(o);
  }

  public void visitArrayIndex(@NotNull PasArrayIndex o) {
    visitcalPsiElement(o);
  }

  public void visitArrayType(@NotNull PasArrayType o) {
    visitcalPsiElement(o);
  }

  public void visitAssemblerStatement(@NotNull PasAssemblerStatement o) {
    visitStatement(o);
  }

  public void visitAssignOp(@NotNull PasAssignOp o) {
    visitcalOperation(o);
  }

  public void visitAssignPart(@NotNull PasAssignPart o) {
    visitcalPsiElement(o);
  }

  public void visitAttributeParamList(@NotNull PasAttributeParamList o) {
    visitcalPsiElement(o);
  }

  public void visitBlockBody(@NotNull PasBlockBody o) {
    visitcalPsiElement(o);
  }

  public void visitBlockGlobal(@NotNull PasBlockGlobal o) {
    visitcalPsiElement(o);
  }

  public void visitBlockLocal(@NotNull PasBlockLocal o) {
    visitcalPsiElement(o);
  }

  public void visitBlockLocalNested1(@NotNull PasBlockLocalNested1 o) {
    visitcalPsiElement(o);
  }

  public void visitBlockLocalWONested(@NotNull PasBlockLocalWONested o) {
    visitcalPsiElement(o);
  }

  public void visitBreakStatement(@NotNull PasBreakStatement o) {
    visitStatement(o);
  }

  public void visitCaseElse(@NotNull PasCaseElse o) {
    visitcalPsiElement(o);
  }

  public void visitCaseItem(@NotNull PasCaseItem o) {
    visitcalPsiElement(o);
  }

  public void visitCaseStatement(@NotNull PasCaseStatement o) {
    visitStatement(o);
  }

  public void visitClassField(@NotNull PasClassField o) {
    visitcalVariableDeclaration(o);
  }

  public void visitClassHelperDecl(@NotNull PasClassHelperDecl o) {
    visitcalHelperDecl(o);
  }

  public void visitClassMethodResolution(@NotNull PasClassMethodResolution o) {
    visitDeclSection(o);
  }

  public void visitClassParent(@NotNull PasClassParent o) {
    visitcalPsiElement(o);
  }

  public void visitClassProperty(@NotNull PasClassProperty o) {
    visitcalPsiElement(o);
  }

  public void visitClassPropertyArray(@NotNull PasClassPropertyArray o) {
    visitcalPsiElement(o);
  }

  public void visitClassPropertyIndex(@NotNull PasClassPropertyIndex o) {
    visitcalPsiElement(o);
  }

  public void visitClassPropertySpecifier(@NotNull PasClassPropertySpecifier o) {
    visitcalPsiElement(o);
  }

  public void visitClassQualifiedIdent(@NotNull PasClassQualifiedIdent o) {
    visitcalQualifiedIdent(o);
    // visitcalNamedElement(o);
  }

  public void visitClassState(@NotNull PasClassState o) {
    visitcalPsiElement(o);
  }

  public void visitClassTypeDecl(@NotNull PasClassTypeDecl o) {
    visitcalClassDecl(o);
  }

  public void visitClassTypeTypeDecl(@NotNull PasClassTypeTypeDecl o) {
    visitcalPsiElement(o);
  }

  public void visitClosureExpr(@NotNull PasClosureExpr o) {
    visitExpr(o);
  }

  public void visitClosureRoutine(@NotNull PasClosureRoutine o) {
    visitDeclSection(o);
    // visitcalRoutine(o);
  }

  public void visitColonConstruct(@NotNull PasColonConstruct o) {
    visitcalPsiElement(o);
  }

  public void visitCompoundStatement(@NotNull PasCompoundStatement o) {
    visitStatement(o);
  }

  public void visitConstDeclaration(@NotNull PasConstDeclaration o) {
    visitcalNamedElement(o);
  }

  public void visitConstExpression(@NotNull PasConstExpression o) {
    visitcalPsiElement(o);
  }

  public void visitConstExpressionOrd(@NotNull PasConstExpressionOrd o) {
    visitConstExpression(o);
  }

  public void visitConstSection(@NotNull PasConstSection o) {
    visitDeclSection(o);
  }

  public void visitConstrainedTypeParam(@NotNull PasConstrainedTypeParam o) {
    visitcalPsiElement(o);
  }

  public void visitContainsClause(@NotNull PasContainsClause o) {
    visitcalPsiElement(o);
  }

  public void visitContinueStatement(@NotNull PasContinueStatement o) {
    visitStatement(o);
  }

  public void visitCustomAttributeDecl(@NotNull PasCustomAttributeDecl o) {
    visitDeclSection(o);
  }

  public void visitEnumType(@NotNull PasEnumType o) {
    visitcalPsiElement(o);
  }

  public void visitEscapedIdent(@NotNull PasEscapedIdent o) {
    visitcalNamedElement(o);
  }

  public void visitExitStatement(@NotNull PasExitStatement o) {
    visitStatement(o);
  }

  public void visitExportedRoutine(@NotNull PasExportedRoutine o) {
    visitcalExportedRoutine(o);
    // visitDeclSection(o);
    // visitcalRoutine(o);
  }

  public void visitExportsSection(@NotNull PasExportsSection o) {
    visitDeclSection(o);
  }

  public void visitExpr(@NotNull PasExpr o) {
    visitcalPsiElement(o);
  }

  public void visitExpression(@NotNull PasExpression o) {
    visitcalPsiElement(o);
  }

  public void visitExternalDirective(@NotNull PasExternalDirective o) {
    visitcalPsiElement(o);
  }

  public void visitFileType(@NotNull PasFileType o) {
    visitcalPsiElement(o);
  }

  public void visitForInlineDeclaration(@NotNull PasForInlineDeclaration o) {
    visitcalVariableDeclaration(o);
    // visitcalInlineDeclaration(o);
  }

  public void visitForStatement(@NotNull PasForStatement o) {
    visitStatement(o);
  }

  public void visitFormalParameter(@NotNull PasFormalParameter o) {
    visitcalVariableDeclaration(o);
  }

  public void visitFormalParameterSection(@NotNull PasFormalParameterSection o) {
    visitcalPsiElement(o);
  }

  public void visitFromExpression(@NotNull PasFromExpression o) {
    visitcalPsiElement(o);
  }

  public void visitFullyQualifiedIdent(@NotNull PasFullyQualifiedIdent o) {
    visitcalQualifiedIdent(o);
    // visitcalNamedElement(o);
  }

  public void visitFunctionDirective(@NotNull PasFunctionDirective o) {
    visitcalPsiElement(o);
  }

  public void visitGenericConstraint(@NotNull PasGenericConstraint o) {
    visitcalPsiElement(o);
  }

  public void visitGenericPostfix(@NotNull PasGenericPostfix o) {
    visitcalPsiElement(o);
  }

  public void visitGenericTypeIdent(@NotNull PasGenericTypeIdent o) {
    visitcalNamedElement(o);
  }

  public void visitGotoStatement(@NotNull PasGotoStatement o) {
    visitStatement(o);
  }

  public void visitHandler(@NotNull PasHandler o) {
    visitcalPsiElement(o);
  }

  public void visitIfElseStatement(@NotNull PasIfElseStatement o) {
    visitStatement(o);
  }

  public void visitIfStatement(@NotNull PasIfStatement o) {
    visitStatement(o);
  }

  public void visitIfThenStatement(@NotNull PasIfThenStatement o) {
    visitStatement(o);
  }

  public void visitImplDeclSection(@NotNull PasImplDeclSection o) {
    visitcalPsiElement(o);
  }

  public void visitInOperatorQualifiedIdent(@NotNull PasInOperatorQualifiedIdent o) {
    visitClassQualifiedIdent(o);
  }

  public void visitInheritedCall(@NotNull PasInheritedCall o) {
    visitContributedReferenceHost(o);
  }

  public void visitInlineConstDeclaration(@NotNull PasInlineConstDeclaration o) {
    visitcalInlineDeclaration(o);
  }

  public void visitInlineVarDeclaration(@NotNull PasInlineVarDeclaration o) {
    visitcalVariableDeclaration(o);
    // visitcalInlineDeclaration(o);
  }

  public void visitInterfaceTypeDecl(@NotNull PasInterfaceTypeDecl o) {
    visitcalInterfaceDecl(o);
  }

  public void visitKeywordIdent(@NotNull PasKeywordIdent o) {
    visitcalNamedElement(o);
  }

  public void visitLabelDeclSection(@NotNull PasLabelDeclSection o) {
    visitDeclSection(o);
  }

  public void visitLabelId(@NotNull PasLabelId o) {
    visitcalPsiElement(o);
  }

  public void visitLibraryModuleHead(@NotNull PasLibraryModuleHead o) {
    visitcalNamedElement(o);
  }

  public void visitNamedIdent(@NotNull PasNamedIdent o) {
    visitcalNamedElement(o);
  }

  public void visitNamedIdentDecl(@NotNull PasNamedIdentDecl o) {
    visitcalIdentDecl(o);
  }

  public void visitNamespaceIdent(@NotNull PasNamespaceIdent o) {
    visitcalQualifiedIdent(o);
    // visitcalNamedElement(o);
  }

  public void visitNewStatement(@NotNull PasNewStatement o) {
    visitStatement(o);
  }

  public void visitObjectDecl(@NotNull PasObjectDecl o) {
    visitcalObjectDecl(o);
  }

  public void visitOperatorSubIdent(@NotNull PasOperatorSubIdent o) {
    visitSubIdent(o);
  }

  public void visitPackageModuleHead(@NotNull PasPackageModuleHead o) {
    visitcalNamedElement(o);
  }

  public void visitParamType(@NotNull PasParamType o) {
    visitcalPsiElement(o);
  }

  public void visitPointerType(@NotNull PasPointerType o) {
    visitcalPsiElement(o);
  }

  public void visitProcBodyBlock(@NotNull PasProcBodyBlock o) {
    visitcalPsiElement(o);
  }

  public void visitProcForwardDecl(@NotNull PasProcForwardDecl o) {
    visitcalPsiElement(o);
  }

  public void visitProcedureType(@NotNull PasProcedureType o) {
    visitcalRoutineEntity(o);
  }

  public void visitProgramModuleHead(@NotNull PasProgramModuleHead o) {
    visitcalNamedElement(o);
  }

  public void visitProgramParamList(@NotNull PasProgramParamList o) {
    visitcalPsiElement(o);
  }

  public void visitRaiseStatement(@NotNull PasRaiseStatement o) {
    visitStatement(o);
  }

  public void visitRangeBound(@NotNull PasRangeBound o) {
    visitcalPsiElement(o);
  }

  public void visitRecordConstExpr(@NotNull PasRecordConstExpr o) {
    visitExpr(o);
  }

  public void visitRecordDecl(@NotNull PasRecordDecl o) {
    visitcalRecordDecl(o);
  }

  public void visitRecordHelperDecl(@NotNull PasRecordHelperDecl o) {
    visitcalHelperDecl(o);
  }

  public void visitRecordVariant(@NotNull PasRecordVariant o) {
    visitcalPsiElement(o);
  }

  public void visitRefNamedIdent(@NotNull PasRefNamedIdent o) {
    visitcalNamedElement(o);
  }

  public void visitRepeatStatement(@NotNull PasRepeatStatement o) {
    visitStatement(o);
  }

  public void visitRequiresClause(@NotNull PasRequiresClause o) {
    visitcalPsiElement(o);
  }

  public void visitRoutineImplDecl(@NotNull PasRoutineImplDecl o) {
    visitcalRoutine(o);
  }

  public void visitRoutineImplDeclNested1(@NotNull PasRoutineImplDeclNested1 o) {
    visitRoutineImplDecl(o);
  }

  public void visitRoutineImplDeclWoNested(@NotNull PasRoutineImplDeclWoNested o) {
    visitRoutineImplDecl(o);
  }

  public void visitSetType(@NotNull PasSetType o) {
    visitcalPsiElement(o);
  }

  public void visitStatement(@NotNull PasStatement o) {
    visitcalPsiElement(o);
  }

  public void visitStringFactor(@NotNull PasStringFactor o) {
    visitcalPsiElement(o);
  }

  public void visitStringType(@NotNull PasStringType o) {
    visitcalPsiElement(o);
  }

  public void visitSubIdent(@NotNull PasSubIdent o) {
    visitcalNamedElement(o);
  }

  public void visitSubRangeType(@NotNull PasSubRangeType o) {
    visitcalPsiElement(o);
  }

  public void visitTryStatement(@NotNull PasTryStatement o) {
    visitStatement(o);
  }

  public void visitTypeDecl(@NotNull PasTypeDecl o) {
    visitcalPsiElement(o);
  }

  public void visitTypeDeclaration(@NotNull PasTypeDeclaration o) {
    visitcalPsiElement(o);
  }

  public void visitTypeID(@NotNull PasTypeID o) {
    visitcalPsiElement(o);
  }

  public void visitTypeSection(@NotNull PasTypeSection o) {
    visitDeclSection(o);
  }

  public void visitUnitFinalization(@NotNull PasUnitFinalization o) {
    visitcalPsiElement(o);
  }

  public void visitUnitImplementation(@NotNull PasUnitImplementation o) {
    visitcalPsiElement(o);
  }

  public void visitUnitInitialization(@NotNull PasUnitInitialization o) {
    visitcalPsiElement(o);
  }

  public void visitUnitInterface(@NotNull PasUnitInterface o) {
    visitcalPsiElement(o);
  }

  public void visitUnitModuleHead(@NotNull PasUnitModuleHead o) {
    visitcalNamedElement(o);
  }

  public void visitUsesClause(@NotNull PasUsesClause o) {
    visitcalPsiElement(o);
  }

  public void visitVarDeclaration(@NotNull PasVarDeclaration o) {
    visitcalVariableDeclaration(o);
  }

  public void visitVarSection(@NotNull PasVarSection o) {
    visitDeclSection(o);
  }

  public void visitVarValueSpec(@NotNull PasVarValueSpec o) {
    visitcalPsiElement(o);
  }

  public void visitVisibility(@NotNull PasVisibility o) {
    visitcalPsiElement(o);
  }

  public void visitWhileStatement(@NotNull PasWhileStatement o) {
    visitStatement(o);
  }

  public void visitWithStatement(@NotNull PasWithStatement o) {
    visitStatement(o);
  }

  public void visitAddOp(@NotNull PasAddOp o) {
    visitcalOperation(o);
  }

  public void visitCallExpr(@NotNull PasCallExpr o) {
    visitExpr(o);
  }

  public void visitDereferenceExpr(@NotNull PasDereferenceExpr o) {
    visitExpr(o);
  }

  public void visitIndexExpr(@NotNull PasIndexExpr o) {
    visitExpr(o);
  }

  public void visitIndexList(@NotNull PasIndexList o) {
    visitcalPsiElement(o);
  }

  public void visitLiteralExpr(@NotNull PasLiteralExpr o) {
    visitExpr(o);
  }

  public void visitMulOp(@NotNull PasMulOp o) {
    visitcalOperation(o);
  }

  public void visitParenExpr(@NotNull PasParenExpr o) {
    visitExpr(o);
  }

  public void visitProductExpr(@NotNull PasProductExpr o) {
    visitExpr(o);
  }

  public void visitReferenceExpr(@NotNull PasReferenceExpr o) {
    visitExpr(o);
  }

  public void visitRelOp(@NotNull PasRelOp o) {
    visitcalOperation(o);
  }

  public void visitRelationalExpr(@NotNull PasRelationalExpr o) {
    visitExpr(o);
  }

  public void visitSetExpr(@NotNull PasSetExpr o) {
    visitExpr(o);
  }

  public void visitStmtEmpty(@NotNull PasStmtEmpty o) {
    visitcalPsiElement(o);
  }

  public void visitSumExpr(@NotNull PasSumExpr o) {
    visitExpr(o);
  }

  public void visitUnaryExpr(@NotNull PasUnaryExpr o) {
    visitExpr(o);
  }

  public void visitUnaryOp(@NotNull PasUnaryOp o) {
    visitcalOperation(o);
  }

  public void visitContributedReferenceHost(@NotNull ContributedReferenceHost o) {
    visitElement(o);
  }

  public void visitDeclSection(@NotNull PasDeclSection o) {
    visitcalPsiElement(o);
  }

  public void visitcalClassDecl(@NotNull PascalClassDecl o) {
    visitcalPsiElement(o);
  }

  public void visitcalExportedRoutine(@NotNull PascalExportedRoutine o) {
    visitcalPsiElement(o);
  }

  public void visitcalHelperDecl(@NotNull PascalHelperDecl o) {
    visitcalPsiElement(o);
  }

  public void visitcalIdentDecl(@NotNull PascalIdentDecl o) {
    visitcalPsiElement(o);
  }

  public void visitcalInlineDeclaration(@NotNull PascalInlineDeclaration o) {
    visitcalPsiElement(o);
  }

  public void visitcalInterfaceDecl(@NotNull PascalInterfaceDecl o) {
    visitcalPsiElement(o);
  }

  public void visitcalNamedElement(@NotNull PascalNamedElement o) {
    visitcalPsiElement(o);
  }

  public void visitcalObjectDecl(@NotNull PascalObjectDecl o) {
    visitcalPsiElement(o);
  }

  public void visitcalOperation(@NotNull PascalOperation o) {
    visitcalPsiElement(o);
  }

  public void visitcalQualifiedIdent(@NotNull PascalQualifiedIdent o) {
    visitcalPsiElement(o);
  }

  public void visitcalRecordDecl(@NotNull PascalRecordDecl o) {
    visitcalPsiElement(o);
  }

  public void visitcalRoutine(@NotNull PascalRoutine o) {
    visitcalPsiElement(o);
  }

  public void visitcalRoutineEntity(@NotNull PascalRoutineEntity o) {
    visitcalPsiElement(o);
  }

  public void visitcalVariableDeclaration(@NotNull PascalVariableDeclaration o) {
    visitcalPsiElement(o);
  }

  public void visitcalPsiElement(@NotNull PascalPsiElement o) {
    visitElement(o);
  }

  public void visitModule(@NotNull PascalPsiElement o) {
    visitElement(o);
  }

}
