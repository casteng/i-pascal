package com.siberika.idea.pascal.lang.psi.impl;

import com.intellij.lang.ASTNode;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.SmartList;
import com.siberika.idea.pascal.lang.psi.PasTypeDecl;
import com.siberika.idea.pascal.lang.psi.PasTypeID;
import com.siberika.idea.pascal.lang.psi.PascalRoutineEntity;
import com.siberika.idea.pascal.lang.psi.field.ParamModifier;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public abstract class PascalProcedureTypeImpl extends PascalPsiElementImpl implements PascalRoutineEntity {
    volatile private String canonicalName;
    volatile private Boolean isFunction;
    volatile private String functionTypeStr;
    volatile private List<String> formalParameterNames;
    volatile private List<String> formalParameterTypes;
    volatile private List<ParamModifier> formalParameterAccess;

    public PascalProcedureTypeImpl(ASTNode node) {
        super(node);
    }

    @Override
    public String getName() {
        if (null == canonicalName) {
            canonicalName = RoutineUtil.calcCanonicalName("", getFormalParameterTypes(), getFormalParameterAccess(), getFunctionTypeStr());
        }
        return canonicalName;
    }

    @Override
    public boolean isConstructor() {
        return false;
    }

    @Override
    public boolean isFunction() {
        if (null == isFunction) {
            isFunction = findChildByFilter(RoutineUtil.FUNCTION_KEYWORDS) != null;
        }
        return isFunction;
    }

    @Override
    public boolean hasParameters() {
        return !getFormalParameterNames().isEmpty();
    }

    @NotNull
    @Override
    public String getFunctionTypeStr() {
        if (null == functionTypeStr) {
            PasTypeDecl type = findChildByClass(PasTypeDecl.class);
            PasTypeID typeId = PsiTreeUtil.findChildOfType(type, PasTypeID.class);
            if (typeId != null) {
                return typeId.getFullyQualifiedIdent().getName();
            }
            functionTypeStr = type != null ? RoutineUtil.calcCanonicalTypeName(type.getText()) : "";
        }
        return functionTypeStr;
    }

    @NotNull
    @Override
    public List<String> getFormalParameterNames() {
        calcFormalParameters();
        return formalParameterNames;
    }

    @NotNull
    @Override
    public List<String> getFormalParameterTypes() {
        calcFormalParameters();
        return formalParameterTypes;
    }

    @NotNull
    @Override
    public List<ParamModifier> getFormalParameterAccess() {
        calcFormalParameters();
        return formalParameterAccess;
    }

    private void calcFormalParameters() {
        if (null == formalParameterNames) {
            SmartList<String> formalParamNames = new SmartList<>();
            SmartList<String> formalParamTypes = new SmartList<>();
            SmartList<ParamModifier> formalParamAccess = new SmartList<>();
            RoutineUtil.calcFormalParameterNames(getFormalParameterSection(), formalParamNames, formalParamTypes, formalParamAccess);
            formalParameterAccess = formalParamAccess;
            formalParameterTypes = formalParamTypes;
            formalParameterNames = formalParamNames;
        }
    }

    @Override
    public void subtreeChanged() {
        super.subtreeChanged();
        canonicalName = null;
        isFunction = null;
        functionTypeStr = null;
        formalParameterNames = null;
        formalParameterTypes = null;
        formalParameterAccess = null;
    }
}
