package com.siberika.idea.pascal.lang.psi.impl;

import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.SmartList;
import com.siberika.idea.pascal.lang.psi.PasEntityScope;
import com.siberika.idea.pascal.lang.psi.PasTypeDecl;
import com.siberika.idea.pascal.lang.psi.PasTypeID;
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
    volatile private String[] reducedParameterTypes;
    volatile List<String> formalParameterNames;
    volatile List<String> formalParameterTypes;
    volatile List<ParamModifier> formalParameterAccess;
    volatile List<String> formalParameterDefaultValues;
    volatile private String functionTypeStr;
    private ReentrantLock calcParamLock = new ReentrantLock();

    PascalHelperRoutine(PascalRoutine self) {
        super(self);
    }

    @Override
    void invalidateCache(boolean subtreeChanged) {
        SyncUtil.doWithLock(calcParamLock, () -> {
            formalParameterNames = null;
            formalParameterTypes = null;
            formalParameterAccess = null;
            formalParameterDefaultValues = null;
            canonicalName = null;
            reducedName = null;
            functionTypeStr = null;
            reducedParameterTypes = null;
        });
        super.invalidateCache(subtreeChanged);
    }

    String getCanonicalName() {
        ensureCacheActual();
        if (null == canonicalName) {
            canonicalName = RoutineUtil.calcCanonicalName(self.getName(), getSelf().getFormalParameterNames(), getSelf().getFormalParameterTypes(),
                    getSelf().getFormalParameterAccess(), getSelf().getFunctionTypeStr() ,getSelf().getFormalParameterDefaultValues());
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
        SyncUtil.doWithLock(calcParamLock, () -> {
            ensureCacheActual();
            if (null == formalParameterNames) {
                List<String> parameterNames = new SmartList<>();
                List<String> parameterTypes = new SmartList<>();
                List<ParamModifier> parameterAccess = new SmartList<>();
                List<String> parameterValues = new SmartList<>();
                RoutineUtil.calcFormalParameterNames(getSelf().getFormalParameterSection(), parameterNames, parameterTypes, parameterAccess, parameterValues);
                formalParameterNames = parameterNames;
                formalParameterTypes = parameterTypes;
                formalParameterAccess = parameterAccess;
                formalParameterDefaultValues = parameterValues;
            }
        });
    }

    private void resolveParameterTypes(List<String> formalParameterTypes) {
        String[] parameterTypes = new String[formalParameterTypes.size()];
        for (int i = 0, formalParameterTypesSize = formalParameterTypes.size(); i < formalParameterTypesSize; i++) {
            String typeName = formalParameterTypes.get(i);
            parameterTypes[i] = StringUtil.isNotEmpty(typeName) ? ResolveUtil.resolveTypeAliasChain(typeName, self, 0) : PsiUtil.TYPE_UNTYPED_NAME;
        }
        reducedParameterTypes = parameterTypes;
    }

    private boolean isReducedNameDirty() {
        return null == reducedName;
    }

    String calcFunctionTypeStr() {
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
