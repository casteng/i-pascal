package com.siberika.idea.pascal.editor;

import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.lang.parameterInfo.CreateParameterInfoContext;
import com.intellij.lang.parameterInfo.ParameterInfoContext;
import com.intellij.lang.parameterInfo.ParameterInfoHandler;
import com.intellij.lang.parameterInfo.ParameterInfoUIContext;
import com.intellij.lang.parameterInfo.UpdateParameterInfoContext;
import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Author: George Bakhtadze
 * Date: 25/03/2015
 */
public class PascalParameterInfoHandler implements ParameterInfoHandler<PsiElement, Object> {
    @Override
    public boolean couldShowInLookup() {
        return true;
    }

    @Nullable
    @Override
    public Object[] getParametersForLookup(LookupElement item, ParameterInfoContext context) {
        return new String[] {"Hello", "World"};
    }

    @Nullable
    @Override
    public Object[] getParametersForDocumentation(Object p, ParameterInfoContext context) {
        return new String[] {"Hello", "World"};
    }

    @Nullable
    @Override
    public PsiElement findElementForParameterInfo(@NotNull CreateParameterInfoContext context) {
        return context.getFile().findElementAt(context.getOffset());
    }

    @Override
    public void showParameterInfo(@NotNull PsiElement element, @NotNull CreateParameterInfoContext context) {
        context.setItemsToShow(getParametersForLookup(null, context));
        context.showHint(element, element.getTextRange().getStartOffset(), this);
    }

    @Nullable
    @Override
    public PsiElement findElementForUpdatingParameterInfo(@NotNull UpdateParameterInfoContext context) {
        return context.getFile().findElementAt(context.getOffset());
    }

    @Override
    public void updateParameterInfo(@NotNull PsiElement element, @NotNull UpdateParameterInfoContext context) {
        System.out.println("=== updateParameterInfo");
    }

    @Nullable
    @Override
    public String getParameterCloseChars() {
        return null;
    }

    @Override
    public boolean tracksParameterIndex() {
        return false;
    }

    @Override
    public void updateUI(Object p, @NotNull ParameterInfoUIContext context) {
        context.setupUIComponentPresentation("Test: " + p,
                0,
                1,
                !context.isUIComponentEnabled(),
                false,
                false,
                context.getDefaultParameterColor()
        );
    }
}
