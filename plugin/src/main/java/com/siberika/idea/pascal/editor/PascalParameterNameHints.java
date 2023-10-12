package com.siberika.idea.pascal.editor;

import com.intellij.codeInsight.hints.HintInfo;
import com.intellij.codeInsight.hints.InlayInfo;
import com.intellij.codeInsight.hints.InlayParameterHintsProvider;
import com.intellij.codeInsight.hints.Option;
import com.intellij.psi.PsiElement;
import com.intellij.util.SmartList;
import com.siberika.idea.pascal.PascalBundle;
import com.siberika.idea.pascal.lang.psi.PasCallExpr;
import com.siberika.idea.pascal.lang.psi.PasExpr;
import com.siberika.idea.pascal.lang.psi.PasLiteralExpr;
import com.siberika.idea.pascal.lang.psi.PascalRoutineEntity;
import com.siberika.idea.pascal.lang.references.PasReferenceUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * Author: George Bakhtadze
 * Date: 27/04/2017
 */
public class PascalParameterNameHints implements InlayParameterHintsProvider {

    private static final String OPTION_ID_PARAM_HINTS_NON_LITERALS = "pascal.paramHints.nonLiterals";
    private static final Option OPTION_PARAM_HINTS_NON_LITERALS = new Option(OPTION_ID_PARAM_HINTS_NON_LITERALS, PascalBundle.message(OPTION_ID_PARAM_HINTS_NON_LITERALS), false);

    @NotNull
    @Override
    public List<InlayInfo> getParameterHints(PsiElement psiElement) {
        if (psiElement instanceof PasCallExpr) {
            return getParameters((PasCallExpr) psiElement);
        } else {
            return Collections.emptyList();
        }
    }

    @Nullable
    @Override
    public HintInfo getHintInfo(PsiElement psiElement) {
        return null;
    }

    @NotNull
    @Override
    public Set<String> getDefaultBlackList() {
        return Collections.emptySet();
    }

    @NotNull
    @Override
    public List<Option> getSupportedOptions() {
        return Collections.singletonList(PascalParameterNameHints.OPTION_PARAM_HINTS_NON_LITERALS);
    }

    @Override
    public boolean isBlackListSupported() {
        return false;
    }

    private List<InlayInfo> getParameters(PasCallExpr callExpr) {
        int count = callExpr.getArgumentList().getExprList().size();
        if (count > 0) {
            for (PascalRoutineEntity el : PasReferenceUtil.resolveRoutines(callExpr)) {
                List<String> params = el.getFormalParameterNames();
                if (count == params.size()) {
                    return retrieveInlayInfo(callExpr, params);
                }
            }
        }
        return Collections.emptyList();
    }

    private List<InlayInfo> retrieveInlayInfo(PasCallExpr callExpr, List<String> parameters) {
        List<InlayInfo> res = new SmartList<InlayInfo>();
        List<PasExpr> exprList = callExpr.getArgumentList().getExprList();
        for (int i = 0; i < exprList.size(); i++) {
            PsiElement arg = exprList.get(i);
            if (OPTION_PARAM_HINTS_NON_LITERALS.get() || (arg instanceof PasLiteralExpr)) {
                res.add(new InlayInfo(parameters.get(i), arg.getTextRange().getStartOffset()));
            }
        }
        return res;
    }
}
