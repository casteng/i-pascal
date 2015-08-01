package com.siberika.idea.pascal.editor;

import com.intellij.codeInsight.daemon.LineMarkerInfo;
import com.intellij.codeInsight.daemon.LineMarkerProvider;
import com.intellij.icons.AllIcons;
import com.intellij.psi.PsiElement;
import com.siberika.idea.pascal.PascalBundle;
import com.siberika.idea.pascal.ide.actions.PascalDefinitionsSearch;
import com.siberika.idea.pascal.lang.psi.PasEntityScope;
import com.siberika.idea.pascal.lang.psi.PasInvalidScopeException;
import com.siberika.idea.pascal.lang.psi.PascalNamedElement;
import com.siberika.idea.pascal.lang.psi.PascalStructType;
import com.siberika.idea.pascal.lang.psi.impl.PascalRoutineImpl;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.List;

/**
 * Author: George Bakhtadze
 * Date: 01/08/2015
 */
public class PascalHeavyLineMarkerProvider implements LineMarkerProvider {
    @Nullable
    @Override
    public LineMarkerInfo getLineMarkerInfo(@NotNull PsiElement element) {
        return null;
    }

    @Override
    public void collectSlowLineMarkers(@NotNull List<PsiElement> elements, @NotNull Collection<LineMarkerInfo> result) {
        try {
            for (PsiElement element : elements) {
                if ((element instanceof PascalRoutineImpl) || (element instanceof PascalStructType)) {
                    // Goto implementations
                    Collection<PasEntityScope> impls = PascalDefinitionsSearch.findImplementations(((PascalNamedElement) element).getNameIdentifier(), 0);
                    if (!impls.isEmpty()) {
                        result.add(PascalLineMarkerProvider.createLineMarkerInfo((PasEntityScope) element, AllIcons.Gutter.OverridenMethod,
                                        PascalBundle.message("navigate.title.goto.overridden"),
                                        PascalLineMarkerProvider.getHandler(PascalBundle.message("navigate.title.goto.overridden"), impls))
                        );
                    }

                }
            }
        } catch (PasInvalidScopeException e) {
            e.printStackTrace();
        }
    }
}
