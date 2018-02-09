package com.siberika.idea.pascal.editor;

import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.lang.parameterInfo.CreateParameterInfoContext;
import com.intellij.lang.parameterInfo.ParameterInfoContext;
import com.intellij.lang.parameterInfo.ParameterInfoHandler;
import com.intellij.lang.parameterInfo.ParameterInfoUIContext;
import com.intellij.lang.parameterInfo.ParameterInfoUtils;
import com.intellij.lang.parameterInfo.UpdateParameterInfoContext;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.SmartList;
import com.siberika.idea.pascal.lang.psi.PasCallExpr;
import com.siberika.idea.pascal.lang.psi.PasFormalParameter;
import com.siberika.idea.pascal.lang.psi.PasFormalParameterSection;
import com.siberika.idea.pascal.lang.psi.PasTypes;
import com.siberika.idea.pascal.lang.psi.PascalNamedElement;
import com.siberika.idea.pascal.lang.psi.impl.PasField;
import com.siberika.idea.pascal.lang.psi.impl.PascalRoutineImpl;
import com.siberika.idea.pascal.lang.references.PasReferenceUtil;
import com.siberika.idea.pascal.util.PsiUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Author: George Bakhtadze
 * Date: 25/03/2015
 */
public class PascalParameterInfoHandler implements ParameterInfoHandler<PasCallExpr, PasFormalParameterSection> {
    @Override
    public boolean couldShowInLookup() {
        return true;
    }

    @Nullable
    @Override
    public Object[] getParametersForLookup(LookupElement item, ParameterInfoContext context) {
        return null;
    }

    @Nullable
    @Override
    public Object[] getParametersForDocumentation(PasFormalParameterSection p, ParameterInfoContext context) {
        return null;
    }

    @Nullable
    @Override
    public PasCallExpr findElementForParameterInfo(@NotNull CreateParameterInfoContext context) {
        PasCallExpr res = getCallExpr(context.getFile().findElementAt(context.getOffset()));
        context.setItemsToShow(getParameters(res));
        return res;
    }

    @Nullable
    @Override
    public PasCallExpr findElementForUpdatingParameterInfo(@NotNull UpdateParameterInfoContext context) {
        PasCallExpr res = getCallExpr(context.getFile().findElementAt(context.getOffset()));
        if (res != null) {
            int index = ParameterInfoUtils.getCurrentParameterIndex(res.getArgumentList().getNode(), context.getOffset(), PasTypes.COMMA);
            context.setCurrentParameter(index);
        }
        return res;
    }

    private Object[] getParameters(PasCallExpr callExpr) {
        Map<String, PasFormalParameterSection> res = new TreeMap<String, PasFormalParameterSection>();
        for (PasField field : PasReferenceUtil.resolveRoutines(callExpr)) {
            if (field.getElement() instanceof PascalRoutineImpl) {
                PasFormalParameterSection parameters = ((PascalRoutineImpl) field.getElement()).getFormalParameterSection();
                if (parameters != null) {
                    res.put(PsiUtil.getFieldName(field.getElement()), parameters);
                }
            }
        }
        return res.values().toArray();
    }

    @Override
    public void showParameterInfo(@NotNull PasCallExpr element, @NotNull CreateParameterInfoContext context) {
        context.showHint(element, element.getTextRange().getStartOffset(), this);
    }

    private PasCallExpr getCallExpr(PsiElement element) {
        PasCallExpr call = PsiTreeUtil.getParentOfType(element, PasCallExpr.class);
        if ((null == call) && (element != null)) {
            PsiElement prev = PsiTreeUtil.prevLeaf(element, true);
            if ((prev != null) && (prev.getText().equals("("))) {
                call = PsiTreeUtil.getParentOfType(prev, PasCallExpr.class);
            }
        }
        return call;
    }

    @Override
    public void updateParameterInfo(@NotNull PasCallExpr element, @NotNull UpdateParameterInfoContext context) {
    }

    @Nullable
    @Override
    public String getParameterCloseChars() {
        return ")";
    }

    @Override
    public boolean tracksParameterIndex() {
        return false;
    }

    @Override
    public void updateUI(PasFormalParameterSection p, @NotNull ParameterInfoUIContext context) {
        List<PsiElement> idents = getIdentList(p);
        PsiElement hlParam = null;
        boolean isDisabled = false;
        if (context.getCurrentParameterIndex() < idents.size()) {
            hlParam = (context.getCurrentParameterIndex() >= 0) ? idents.get(context.getCurrentParameterIndex()) : null;
        } else {
            isDisabled = true;
        }
        int hlStart = -1;
        int hlEnd = -1;
        if (hlParam != null) {
            hlStart = hlParam.getTextRange().getStartOffset() - p.getTextOffset() - 1;
            hlEnd = hlParam.getTextRange().getEndOffset() - p.getTextOffset() - 1;
        }
        context.setupUIComponentPresentation(getHintText(p), hlStart, hlEnd,
                isDisabled, false, false, context.getDefaultParameterColor()
        );
    }

    @NotNull
    private List<PsiElement> getIdentList(PasFormalParameterSection p) {
        SmartList<PsiElement> res = new SmartList<PsiElement>();
        for (PasFormalParameter paramSec : p.getFormalParameterList()) {
            for (PascalNamedElement pasNamedIdent : paramSec.getNamedIdentDeclList()) {
                res.add(pasNamedIdent);
            }
        }
        return res;
    }

    private String getHintText(PasFormalParameterSection p) {
        String s = p.getText();
        return s.length() > 2 ? s.substring(1, s.length() - 1) : "()";
    }
}
