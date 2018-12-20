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
    private String canonicalName;
    volatile private String reducedName;
    volatile private SmartPsiElementPointer<PascalNamedElement>[] reducedParameterTypes;
    List<String> formalParameterNames;
    List<String> formalParameterTypes;
    List<ParamModifier> formalParameterAccess;

    private ReentrantLock parametersLock = new ReentrantLock();

    PascalRoutineHelper(PascalRoutine routine) {
        this.self = routine;
    }

    void invalidateCaches() {
        if (SyncUtil.lockOrCancel(parametersLock)) {
            formalParameterNames = null;
            formalParameterTypes = null;
            formalParameterAccess = null;
            canonicalName = null;
            reducedName = null;
            parametersLock.unlock();
        }
    }

    String getCanonicalName() {
        SyncUtil.doWithLock(parametersLock, () -> {
            if (null == canonicalName) {
                canonicalName = RoutineUtil.calcCanonicalName(self.getName(), self.getFormalParameterTypes(), self.getFormalParameterAccess(), self.getFunctionTypeStr());
            }
        });
        return canonicalName;
    }

    String getReducedName() {
        boolean reducedNameDirty = isReducedNameDirty();
        while (reducedNameDirty) {
            SyncUtil.doWithLock(parametersLock, () -> {
                resolveParameterTypes(self.getFormalParameterTypes());
                reducedName = RoutineUtil.calcReducedName(self.getName(), reducedParameterTypes);
            });
            reducedNameDirty = (reducedName == null);
        }
        return reducedName;
    }

    void calcFormalParameters() {
        SyncUtil.doWithLock(parametersLock, () -> {
            if (null == formalParameterNames) {
                formalParameterNames = new SmartList<>();
                formalParameterTypes = new SmartList<>();
                formalParameterAccess = new SmartList<>();
                RoutineUtil.calcFormalParameterNames(self.getFormalParameterSection(), formalParameterNames, formalParameterTypes, formalParameterAccess);
            }
        });
    }

    private void resolveParameterTypes(List<String> formalParameterTypes) {
        SmartPsiElementPointer[] parameterTypes = new SmartPsiElementPointer[formalParameterTypes.size()];
        for (int i = 0, formalParameterTypesSize = formalParameterTypes.size(); i < formalParameterTypesSize; i++) {
            String typeName = formalParameterTypes.get(i);
            parameterTypes[i] = StringUtil.isNotEmpty(typeName) ? PsiUtil.createSmartPointer(ResolveUtil.resolveTypeAliasChain(typeName, self, 0)) : null;
        }
        reducedParameterTypes = parameterTypes;
    }

    private boolean isReducedNameDirty() {
        return null == reducedName;
    }
}
