package com.siberika.idea.pascal.lang.psi.impl;

import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.SmartPsiElementPointer;
import com.intellij.util.SmartList;
import com.siberika.idea.pascal.lang.psi.PascalNamedElement;
import com.siberika.idea.pascal.lang.psi.PascalRoutine;
import com.siberika.idea.pascal.lang.psi.field.ParamModifier;
import com.siberika.idea.pascal.lang.references.ResolveUtil;
import com.siberika.idea.pascal.util.PsiUtil;
import com.siberika.idea.pascal.util.SyncUtil;

import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

class PascalRoutineHelper {
    private final PascalRoutine self;
    volatile private String canonicalName;
    volatile private String reducedName;
    volatile private SmartPsiElementPointer<PascalNamedElement>[] reducedParameterTypes;
    volatile List<String> formalParameterNames;
    volatile List<String> formalParameterTypes;
    volatile List<ParamModifier> formalParameterAccess;
    volatile private long modified;
    private ReentrantLock calcParamLock = new ReentrantLock();

    PascalRoutineHelper(PascalRoutine routine) {
        this.self = routine;
    }

    void invalidateCaches() {
        SyncUtil.doWithLock(calcParamLock, () -> {
            formalParameterNames = null;
            formalParameterTypes = null;
            formalParameterAccess = null;
            canonicalName = null;
            reducedName = null;
            modified = self.getContainingFile().getModificationStamp();
        });
    }

    String getCanonicalName() {
        ensureCacheActual();
        if (null == canonicalName) {
            canonicalName = RoutineUtil.calcCanonicalName(self.getName(), self.getFormalParameterTypes(), self.getFormalParameterAccess(), self.getFunctionTypeStr());
        }
        return canonicalName;
    }

    String getReducedName() {
        ensureCacheActual();
        if (isReducedNameDirty()) {
            String name = null;
            while (name == null) {
                resolveParameterTypes(self.getFormalParameterTypes());
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
                RoutineUtil.calcFormalParameterNames(self.getFormalParameterSection(), parameterNames, parameterTypes, parameterAccess);
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

    private void ensureCacheActual() {
        if (modified != self.getContainingFile().getModificationStamp()) {
            invalidateCaches();
        }
    }
}
