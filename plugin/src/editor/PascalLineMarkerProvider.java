package com.siberika.idea.pascal.editor;

import com.intellij.codeHighlighting.Pass;
import com.intellij.codeInsight.daemon.GutterIconNavigationHandler;
import com.intellij.codeInsight.daemon.LineMarkerInfo;
import com.intellij.codeInsight.daemon.LineMarkerProvider;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.markup.GutterIconRenderer;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.ConstantFunction;
import com.siberika.idea.pascal.PascalBundle;
import com.siberika.idea.pascal.ide.actions.GotoSuper;
import com.siberika.idea.pascal.ide.actions.SectionToggle;
import com.siberika.idea.pascal.lang.psi.PasEntityScope;
import com.siberika.idea.pascal.lang.psi.PasInvalidScopeException;
import com.siberika.idea.pascal.lang.psi.PasUnitInterface;
import com.siberika.idea.pascal.lang.psi.PasUsesClause;
import com.siberika.idea.pascal.lang.psi.PascalNamedElement;
import com.siberika.idea.pascal.lang.psi.PascalStructType;
import com.siberika.idea.pascal.lang.psi.impl.PasExportedRoutineImpl;
import com.siberika.idea.pascal.lang.psi.impl.PasRoutineImplDeclImpl;
import com.siberika.idea.pascal.lang.psi.impl.PascalRoutineImpl;
import com.siberika.idea.pascal.util.EditorUtil;
import com.siberika.idea.pascal.util.PsiUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.event.MouseEvent;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * Author: George Bakhtadze
 * Date: 05/09/2013
 */
public class PascalLineMarkerProvider implements LineMarkerProvider {

    public static final Logger LOG = Logger.getInstance(PascalLineMarkerProvider.class.getName());

    private void collectNavigationMarkers(@NotNull PsiElement element, Collection<? super LineMarkerInfo> result) throws PasInvalidScopeException {
        boolean impl = true;
        PsiElement target = null;
        if (element instanceof PasExportedRoutineImpl) {
            target = SectionToggle.getRoutineTarget((PasExportedRoutineImpl) element);
        } else if (element instanceof PasRoutineImplDeclImpl) {
            target = SectionToggle.getRoutineTarget((PasRoutineImplDeclImpl) element);
            impl = false;
        } else if (element instanceof PasUsesClause) {
            target = SectionToggle.getUsesTarget((PasUsesClause) element);
            impl = PsiTreeUtil.getParentOfType(element, PasUnitInterface.class) != null;
        }
        if (PsiUtil.isElementUsable(target)) {
            result.add(createLineMarkerInfo(element, impl ? AllIcons.Gutter.ImplementedMethod : AllIcons.Gutter.ImplementingMethod,
                    msg("navigate.title.toggle.section"), getHandler(msg("navigate.title.toggle.section"), Collections.singletonList(target))));
        }
        // Goto super
        if (element instanceof PascalNamedElement) {
            PascalNamedElement namedElement = (PascalNamedElement) element;
            Collection<PasEntityScope> supers = GotoSuper.retrieveGotoSuperTargets(namedElement.getNameIdentifier());
            if (!supers.isEmpty()) {
                result.add(createLineMarkerInfo((PasEntityScope) element, AllIcons.Gutter.OverridingMethod, msg("navigate.title.goto.super"),
                        getHandler(msg("navigate.title.goto.super"), supers)));
            }
        }
    }

    private String msg(String key) {
        return PascalBundle.message(key);
    }


    static  <T extends PsiElement> LineMarkerInfo<T> createLineMarkerInfo(@NotNull T element, Icon icon, final String tooltip,
                                                           @NotNull GutterIconNavigationHandler<T> handler) {
        return new LineMarkerInfo<T>(element, element.getTextRange(),
                icon, Pass.UPDATE_OVERRIDEN_MARKERS,
                new ConstantFunction<T, String>(tooltip), handler,
                GutterIconRenderer.Alignment.RIGHT);
    }

    @Nullable
    @Override
    public LineMarkerInfo getLineMarkerInfo(@NotNull PsiElement element) {
        return null;
    }

    @Override
    public void collectSlowLineMarkers(@NotNull List<PsiElement> elements, @NotNull Collection<LineMarkerInfo> result) {
        try {
            for (PsiElement element : elements) {
                if ((element instanceof PascalRoutineImpl) || (element instanceof PascalStructType) || (element instanceof PasUsesClause)) {
                    collectNavigationMarkers(element, result);
                }
            }
        } catch (PasInvalidScopeException e) {
            e.printStackTrace();
        }
    }

    static  <T extends PsiElement> GutterIconNavigationHandler<T> getHandler(final String title, @NotNull final Collection<T> targets) {
        return new GutterIconNavigationHandler<T>() {
            @Override
            public void navigate(MouseEvent e, PsiElement elt) {
                EditorUtil.navigateTo(e, title, targets);
            }
        };
    }

}
