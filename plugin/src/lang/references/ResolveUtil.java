package com.siberika.idea.pascal.lang.references;

import com.intellij.extapi.psi.StubBasedPsiElementBase;
import com.intellij.openapi.module.ModuleUtilCore;
import com.intellij.psi.util.PsiTreeUtil;
import com.siberika.idea.pascal.lang.psi.PasGenericTypeIdent;
import com.siberika.idea.pascal.lang.psi.PasTypeDecl;
import com.siberika.idea.pascal.lang.psi.PasTypeID;
import com.siberika.idea.pascal.lang.psi.PascalIdentDecl;
import com.siberika.idea.pascal.lang.psi.PascalModule;
import com.siberika.idea.pascal.lang.psi.PascalNamedElement;
import com.siberika.idea.pascal.lang.psi.PascalPsiElement;
import com.siberika.idea.pascal.lang.psi.PascalQualifiedIdent;
import com.siberika.idea.pascal.lang.psi.PascalRoutine;
import com.siberika.idea.pascal.lang.psi.impl.PasField;
import com.siberika.idea.pascal.util.PsiUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;

public class ResolveUtil {

    @Nullable
    public static String getDeclarationTypeString(@NotNull PascalNamedElement el) {
        PascalQualifiedIdent ident = null;
        if (PsiUtil.isVariableDecl(el) || PsiUtil.isFieldDecl(el) || PsiUtil.isPropertyDecl(el) || PsiUtil.isConstDecl(el)) {   // variable declaration case
            PascalPsiElement varDecl = PsiTreeUtil.getNextSiblingOfType(el, PasTypeDecl.class);
            if (null == varDecl) {
                varDecl = PsiTreeUtil.getNextSiblingOfType(el, PasTypeID.class);
            }
            if (varDecl != null) {
                ident = PsiTreeUtil.findChildOfType(varDecl, PascalQualifiedIdent.class, true);
            }
        } else if (el.getParent() instanceof PasGenericTypeIdent) {                                                             // type declaration case
            el = (PascalNamedElement) el.getParent();
            PasTypeDecl res = PsiUtil.getTypeDeclaration(el);
            if (res != null) {
                PasTypeID typeId = PsiTreeUtil.findChildOfType(res, PasTypeID.class);
                ident = typeId != null ? typeId.getFullyQualifiedIdent() : null;
            }
        } else if (el.getParent() instanceof PascalRoutine) {                                     // routine declaration case
            PasTypeID type = ((PascalRoutine) el.getParent()).getFunctionTypeIdent();
            ident = type != null ? type.getFullyQualifiedIdent() : null;
        }
        return ident != null ? ident.getName() : null;
    }

    static PasField.ValueType resolveFieldType(PasField field, boolean includeLibrary, int recursionCount) {
        PascalNamedElement el = field.getElement();
        if ((el instanceof StubBasedPsiElementBase) && (((StubBasedPsiElementBase) el).getStub() != null)) {
            PasField.ValueType valueType = resolveTypeWithStub((StubBasedPsiElementBase) el, includeLibrary, recursionCount);
            valueType.field = field;
            return valueType;
        } else {
            return PasReferenceUtil.resolveFieldType(field, includeLibrary, recursionCount);
        }
    }

    private static PasField.ValueType resolveTypeWithStub(StubBasedPsiElementBase element, boolean includeLibrary, int recursionCount) {
        if (element instanceof PascalIdentDecl) {
            return resolveIdentDeclType((PascalIdentDecl) element, includeLibrary, recursionCount);
        }
        return null;
    }

    private static PasField.ValueType resolveIdentDeclType(PascalIdentDecl element, boolean includeLibrary, int recursionCount) {
        if (element.getStub().getType() == PasField.FieldType.TYPE) {
            PascalNamedElement typeEl = null;
            String type = getDeclarationTypeString(element);
            if (type != null) {
                Collection<PascalModule> units = PasReferenceUtil.findUnitFilesStub(element.getProject(), ModuleUtilCore.findModuleForPsiElement(element), null);
                for (PascalModule unit : units) {
                    PasField field = unit.getField(type);
                    if ((field != null) && (field.fieldType == PasField.FieldType.TYPE)) {
                        typeEl = field.getElement();
                    }
                }
            }
            return typeEl instanceof PasTypeDecl ? new PasField.ValueType(null, null, null, (PasTypeDecl) typeEl) : null;
        }
        return null;
    }
}
