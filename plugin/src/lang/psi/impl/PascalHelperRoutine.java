package com.siberika.idea.pascal.lang.psi.impl;

import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiElement;
import com.intellij.psi.SmartPsiElementPointer;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.SmartList;
import com.siberika.idea.pascal.lang.psi.PasEntityScope;
import com.siberika.idea.pascal.lang.psi.PasTypeDecl;
import com.siberika.idea.pascal.lang.psi.PasTypeID;
import com.siberika.idea.pascal.lang.psi.PascalNamedElement;
import com.siberika.idea.pascal.lang.psi.PascalRoutine;
import com.siberika.idea.pascal.lang.psi.field.ParamModifier;
import com.siberika.idea.pascal.lang.references.ResolveUtil;
import com.siberika.idea.pascal.util.PsiUtil;
import com.siberika.idea.pascal.util.SyncUtil;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

class PascalHelperRoutine extends PascalHelperScope {
    volatile private String canonicalName;
    volatile private String reducedName;
    volatile private SmartPsiElementPointer<PascalNamedElement>[] reducedParameterTypes;
    volatile List<String> formalParameterNames;
    volatile List<String> formalParameterTypes;
    volatile List<ParamModifier> formalParameterAccess;
    volatile private String functionTypeStr;
    private ReentrantLock calcParamLock = new ReentrantLock();

    public PascalHelperRoutine(PascalRoutine self) {
        super(self);
    }

    void invalidateCaches() {
        SyncUtil.doWithLock(calcParamLock, () -> {
            formalParameterNames = null;
            formalParameterTypes = null;
            formalParameterAccess = null;
            canonicalName = null;
            reducedName = null;
            functionTypeStr = null;
        });
        super.invalidateCaches();
    }

    String getCanonicalName() {
        ensureCacheActual();
        if (null == canonicalName) {
            canonicalName = RoutineUtil.calcCanonicalName(self.getName(), getSelf().getFormalParameterTypes(), getSelf().getFormalParameterAccess(), getSelf().getFunctionTypeStr());
        }
        return canonicalName;
    }

    String getReducedName() {
        ensureCacheActual();
        if (isReducedNameDirty()) {
            String name = null;
            while (name == null) {
                resolveParameterTypes(getSelf().getFormalParameterTypes());
                name = RoutineUtil.calcReducedName(self.getName(), reducedParameterTypes);
            }
            reducedName = name;
        }
        return reducedName;
    }

    void calcFormalParameters() {
        ensureCacheActual();
        SyncUtil.doWithLock(calcParamLock, () -> {
            if (null == formalParameterNames) {
                List<String> parameterNames = new SmartList<>();
                List<String> parameterTypes = new SmartList<>();
                List<ParamModifier> parameterAccess = new SmartList<>();
                RoutineUtil.calcFormalParameterNames(getSelf().getFormalParameterSection(), parameterNames, parameterTypes, parameterAccess);
                formalParameterNames = parameterNames;
                formalParameterTypes = parameterTypes;
                formalParameterAccess = parameterAccess;
            }
        });
    }

    private void resolveParameterTypes(List<String> formalParameterTypes) {
        ensureCacheActual();
        SmartPsiElementPointer<PascalNamedElement>[] parameterTypes = new SmartPsiElementPointer[formalParameterTypes.size()];
        for (int i = 0, formalParameterTypesSize = formalParameterTypes.size(); i < formalParameterTypesSize; i++) {
            String typeName = formalParameterTypes.get(i);
            parameterTypes[i] = StringUtil.isNotEmpty(typeName) ? PsiUtil.createSmartPointer(ResolveUtil.resolveTypeAliasChain(typeName, self, 0)) : null;
        }
        reducedParameterTypes = parameterTypes;
    }

    private boolean isReducedNameDirty() {
        return null == reducedName;
    }

    public String calcFunctionTypeStr() {
        ensureCacheActual();
        if (null == functionTypeStr) {
            if (getSelf().isConstructor()) {
                functionTypeStr = self instanceof PascalRoutineImpl ? resolveConstructorTypeImpl() : resolveConstructorTypeDecl();
            } else {
                PasTypeDecl type = findTypeDecl(getSelf());
                PasTypeID typeId = PsiTreeUtil.findChildOfType(type, PasTypeID.class);
                if (typeId != null) {
                    return typeId.getFullyQualifiedIdent().getName();
                }
                functionTypeStr = type != null ? RoutineUtil.calcCanonicalTypeName(type.getText()) : "";
            }
        }
        return functionTypeStr;
    }

    // Return struct type name
    private String resolveConstructorTypeDecl() {
        PasEntityScope scope = getSelf().getContainingScope();
        return scope != null ? RoutineUtil.calcCanonicalTypeName(scope.getName()) : "";
    }

    // Return namespace part of constructor implementation name as type name
    private String resolveConstructorTypeImpl() {
        String ns = self.getNamespace();
        ns = ns.substring(ns.lastIndexOf('.') + 1);
        List<String> typeParams = ((PascalRoutineImpl) self).getTypeParameters();
        if (!typeParams.isEmpty()) {
            StringBuilder sb = new StringBuilder();
            sb.append("<");
            for (String typeParam : typeParams) {
                if (sb.length() > 1) {
                    sb.append(", ");
                }
                sb.append(typeParam);
            }
            sb.append(">");
            ns = ns + sb.toString();
        }
        return ns;
    }

    @Nullable
    private static <T> T findTypeDecl(PascalRoutine routine) {
        for (PsiElement cur = routine.getFirstChild(); cur != null; cur = cur.getNextSibling()) {
            if (cur instanceof PasTypeDecl) return (T) cur;
        }
        return null;
    }

    private PascalRoutine getSelf() {
        return (PascalRoutine) self;
    }

}
