package com.siberika.idea.pascal.lang.psi;

import com.intellij.psi.PsiElement;
import com.siberika.idea.pascal.lang.psi.field.ParamModifier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public interface PascalRoutineEntity extends PascalPsiElement {

    String getName();

    boolean isConstructor();

    boolean isFunction();

    boolean isProcedure();

    boolean hasParameters();

    @NotNull
    String getFunctionTypeStr();

    @NotNull
    List<String> getFormalParameterNames();

    @NotNull
    List<String> getFormalParameterTypes();

    @NotNull
    List<ParamModifier> getFormalParameterAccess();

    @NotNull
    List<String> getFormalParameterDefaultValues();

    @Nullable
    PasFormalParameterSection getFormalParameterSection();         // TODO: replace with getFormalParameters()
}
