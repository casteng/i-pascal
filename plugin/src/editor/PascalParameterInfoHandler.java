package com.siberika.idea.pascal.editor;

import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.lang.parameterInfo.CreateParameterInfoContext;
import com.intellij.lang.parameterInfo.ParameterInfoContext;
import com.intellij.lang.parameterInfo.ParameterInfoHandler;
import com.intellij.lang.parameterInfo.ParameterInfoUIContext;
import com.intellij.lang.parameterInfo.UpdateParameterInfoContext;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import com.siberika.idea.pascal.lang.parser.NamespaceRec;
import com.siberika.idea.pascal.lang.psi.PasCallExpr;
import com.siberika.idea.pascal.lang.psi.PasFullyQualifiedIdent;
import com.siberika.idea.pascal.lang.psi.impl.PasField;
import com.siberika.idea.pascal.lang.references.PasReferenceUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;

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
        System.out.println("=== getParametersForLookup");
        return new String[] {"Hello", "World"};
    }

    @Nullable
    @Override
    public Object[] getParametersForDocumentation(Object p, ParameterInfoContext context) {
        System.out.println("=== getParametersForDocumentation");
        return new String[] {"Hello", "World"};
    }

    @Nullable
    @Override
    public PsiElement findElementForParameterInfo(@NotNull CreateParameterInfoContext context) {
        System.out.println("=== findElementForParameterInfo");
        return context.getFile().findElementAt(context.getOffset());
    }

    @Override
    public void showParameterInfo(@NotNull PsiElement element, @NotNull CreateParameterInfoContext context) {
        if (element instanceof PasCallExpr) {
            PasFullyQualifiedIdent ident = PsiTreeUtil.findChildOfType(((PasCallExpr) element).getExpr(), PasFullyQualifiedIdent.class);
            if (ident != null) {
                Collection<PasField> routines = PasReferenceUtil.resolveExpr(NamespaceRec.fromElement(ident), PasField.TYPES_ROUTINE, true, 0);
            }
        }
        context.setItemsToShow(getParametersForLookup(null, context));
        context.showHint(element, element.getTextRange().getStartOffset(), this);
    }

    @Nullable
    @Override
    public PsiElement findElementForUpdatingParameterInfo(@NotNull UpdateParameterInfoContext context) {
        System.out.println("=== findElementForUpdatingParameterInfo");
        PsiElement uc = context.getFile().findElementAt(context.getOffset());
        PasCallExpr call = PsiTreeUtil.getParentOfType(uc, PasCallExpr.class);
        if (call != null) {
            System.out.println("=== call: " + call);
        }
        return call;
    }

    @Override
    public void updateParameterInfo(@NotNull PsiElement element, @NotNull UpdateParameterInfoContext context) {
        System.out.println("=== updateParameterInfo: " + element);
    }

    @Nullable
    @Override
    public String getParameterCloseChars() {
        return ")";
    }

    @Override
    public boolean tracksParameterIndex() {
        return true;
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
