package com.siberika.idea.pascal.editor;

import com.intellij.codeInsight.hints.HintInfo;
import com.intellij.codeInsight.hints.InlayInfo;
import com.intellij.codeInsight.hints.InlayParameterHintsProvider;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import com.siberika.idea.pascal.lang.psi.PasArgumentList;
import com.siberika.idea.pascal.lang.psi.PasCallExpr;
import com.siberika.idea.pascal.lang.psi.PasClosureExpr;
import com.siberika.idea.pascal.lang.psi.PasExpr;
import com.siberika.idea.pascal.lang.psi.PasFormalParameter;
import com.siberika.idea.pascal.lang.psi.PasFormalParameterSection;
import com.siberika.idea.pascal.lang.psi.PasIndexList;
import com.siberika.idea.pascal.lang.psi.PasLiteralExpr;
import com.siberika.idea.pascal.lang.psi.PasNamedIdent;
import com.siberika.idea.pascal.lang.psi.PascalPsiElement;
import com.siberika.idea.pascal.lang.psi.impl.PasField;
import com.siberika.idea.pascal.lang.psi.impl.PascalRoutineImpl;
import com.siberika.idea.pascal.lang.references.PasReferenceUtil;
import kotlin.reflect.jvm.internal.impl.utils.SmartList;
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

    private List<InlayInfo> getParameters(PasCallExpr callExpr) {
        int count = callExpr.getArgumentList().getExprList().size();
        if (count > 0) for (PasField field : PasReferenceUtil.resolveRoutines(callExpr)) {
            if (field.getElement() instanceof PascalRoutineImpl) {
                PasFormalParameterSection parameters = ((PascalRoutineImpl) field.getElement()).getFormalParameterSection();
                if (parameters != null) {
                    List<String> params = new SmartList<String>();
                    for (PasFormalParameter parameter : parameters.getFormalParameterList()) {
                        for (PasNamedIdent pasNamedIdent : parameter.getNamedIdentList()) {
                            params.add(pasNamedIdent.getName());
                        }
                    }
                    if (count == params.size()) {
                        return retrieveInlayInfo(callExpr, params);
                    }
                }
            }
        }
        return Collections.emptyList();
    }

    private List<InlayInfo> retrieveInlayInfo(PasCallExpr callExpr, List<String> parameters) {
        List<InlayInfo> res = new SmartList<InlayInfo>();
        List<PasExpr> exprList = callExpr.getArgumentList().getExprList();
        for (int i = 0; i < exprList.size(); i++) {
            PasExpr arg = exprList.get(i);
            PasLiteralExpr literal = PsiTreeUtil.findChildOfType(arg, PasLiteralExpr.class, false);
            PascalPsiElement root = PsiTreeUtil.getParentOfType(literal, PasArgumentList.class, PasIndexList.class, PasClosureExpr.class);
            if (root == callExpr.getArgumentList()) {
                res.add(new InlayInfo(parameters.get(i), arg.getTextRange().getStartOffset()));
            }
        }
        return res;
    }
}
