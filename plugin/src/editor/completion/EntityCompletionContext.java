package com.siberika.idea.pascal.editor.completion;

import com.intellij.codeInsight.completion.CompletionParameters;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.codeStyle.MinusculeMatcher;
import com.intellij.psi.codeStyle.NameUtil;
import com.intellij.util.SmartList;
import com.siberika.idea.pascal.lang.context.Context;
import com.siberika.idea.pascal.lang.psi.impl.PasField;
import com.siberika.idea.pascal.util.StrUtil;

import java.util.Collection;
import java.util.EnumSet;

class EntityCompletionContext {
    private static final int PRIORITY_NAME_MATCH = 60;
    private static final int PRIORITY_OTHER_FILE = -50;
    private static final int PRIORITY_UNDERSCORED = -100;
    private static final int PRIORITY_TYPE_MATCH = 55;
    private static final int PRIORITY_TYPE_NOT_MATCH = -80;
    private static final int PRIORITY_NAME_DENIED = -100;

    final Collection<String> boostNames = new SmartList<>();
    final Context context;
    final CompletionParameters completionParameters;
    
    EnumSet<PasField.FieldType> likelyTypes;
    EnumSet<PasField.FieldType> deniedTypes;
    String deniedName;
    final private VirtualFile virtualFile;

    EntityCompletionContext(Context context, CompletionParameters completionParameters) {
        this.context = context;
        this.completionParameters = completionParameters;
        likelyTypes = EnumSet.noneOf(PasField.FieldType.class);
        deniedTypes = EnumSet.noneOf(PasField.FieldType.class);
        virtualFile = completionParameters.getOriginalFile().getVirtualFile();
    }

    boolean isUnrelatedUnitsEnabled() {
        return completionParameters.getInvocationCount() > 1;
    }

    boolean isFromOtherFile(PasField field) {
        return (field.getElementPtr() != null) && (virtualFile != null) && !virtualFile.equals(field.getElementPtr().getVirtualFile());
    }

    int calcPriority(String lookupString, String fieldName, PasField.FieldType fieldType, boolean fromOtherFile) {
        int priority = 0;
        if (likelyTypes.contains(fieldType)) {
            priority += PRIORITY_TYPE_MATCH;
        } else if (deniedTypes.contains(fieldType)) {
            priority += PRIORITY_TYPE_NOT_MATCH;
        }
        if (lookupString.startsWith("_")) {
            priority += PRIORITY_UNDERSCORED;
        }
        if (fromOtherFile) {
            priority += PRIORITY_OTHER_FILE;
        }
        if (isNameAllowed(fieldName)) {
            for (String boostName : boostNames) {
                MinusculeMatcher matcher = NameUtil.buildMatcher(boostName).build();
                if (matcher.matches(StrUtil.removePrefixes(fieldName, new String[]{"F", "A", "T", "I", "C"}))) {
                    priority += PRIORITY_NAME_MATCH;
                }
            }
        } else {
            priority = PRIORITY_NAME_DENIED;
        }
        return priority;
    }

    private boolean isNameAllowed(String name) {
        return (null == deniedName) || !deniedName.equalsIgnoreCase(name);
    }
}
