package com.siberika.idea.pascal.editor;

import com.intellij.codeHighlighting.Pass;
import com.intellij.codeInsight.daemon.DaemonCodeAnalyzerSettings;
import com.intellij.codeInsight.daemon.LineMarkerInfo;
import com.intellij.codeInsight.daemon.LineMarkerProvider;
import com.intellij.codeInsight.daemon.impl.LineMarkersPass;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.colors.EditorColorsManager;
import com.intellij.openapi.editor.markup.GutterIconRenderer;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.util.PsiTreeUtil;
import com.siberika.idea.pascal.PascalIcons;
import com.siberika.idea.pascal.editor.linemarker.PascalMarker;
import com.siberika.idea.pascal.ide.actions.GotoSuper;
import com.siberika.idea.pascal.ide.actions.SectionToggle;
import com.siberika.idea.pascal.lang.psi.PasClassTypeDecl;
import com.siberika.idea.pascal.lang.psi.PasRecordDecl;
import com.siberika.idea.pascal.lang.psi.PasUnitInterface;
import com.siberika.idea.pascal.lang.psi.PasUnitModuleHead;
import com.siberika.idea.pascal.lang.psi.PasUsesClause;
import com.siberika.idea.pascal.lang.psi.PascalNamedElement;
import com.siberika.idea.pascal.lang.psi.PascalRoutine;
import com.siberika.idea.pascal.lang.psi.PascalStructType;
import com.siberika.idea.pascal.lang.psi.impl.PasExportedRoutineImpl;
import com.siberika.idea.pascal.lang.psi.impl.PasRoutineImplDeclImpl;
import com.siberika.idea.pascal.lang.search.UsedBy;
import com.siberika.idea.pascal.lang.stub.PascalHelperIndex;
import com.siberika.idea.pascal.lang.stub.StubUtil;
import com.siberika.idea.pascal.util.PsiUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.Collection;
import java.util.List;

/**
 * Author: George Bakhtadze
 * Date: 05/09/2013
 */
public class PascalLineMarkerProvider implements LineMarkerProvider {

    public static final Logger LOG = Logger.getInstance(PascalLineMarkerProvider.class.getName());

    private final DaemonCodeAnalyzerSettings myDaemonSettings;
    private final EditorColorsManager myColorsManager;

    public PascalLineMarkerProvider(DaemonCodeAnalyzerSettings daemonSettings, EditorColorsManager colorsManager) {
        myDaemonSettings = daemonSettings;
        myColorsManager = colorsManager;
    }

    private void collectNavigationMarkers(@NotNull PsiElement element, Collection<? super LineMarkerInfo> result) {
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
            result.add(createLineMarkerInfo(element, impl ? AllIcons.Gutter.ImplementedMethod : AllIcons.Gutter.ImplementingMethod, PascalMarker.SECTION_TOGGLE));
        }
        // Goto super
        if (element instanceof PascalNamedElement) {
            PascalNamedElement namedElement = (PascalNamedElement) element;
            if (GotoSuper.hasSuperTargets(namedElement.getNameIdentifier())) {
                result.add(createLineMarkerInfo(element, AllIcons.Gutter.OverridingMethod, PascalMarker.GOTO_SUPER));
            }
            if (element instanceof PasUnitModuleHead) {
                if (UsedBy.hasDependentModules((PasUnitModuleHead) element)) {
                    result.add(createLineMarkerInfo(element, AllIcons.Hierarchy.Caller, PascalMarker.USED_BY_UNIT));
                }
            }
        }
    }

    static LineMarkerInfo<PsiElement> createLineMarkerInfo(@NotNull PsiElement element, Icon icon, @NotNull PascalMarker marker) {
        PsiElement el = getLeaf(element);
        return new LineMarkerInfo<PsiElement>(el, el.getTextRange(),
                icon, Pass.LINE_MARKERS,
                marker.getTooltip(), marker.getHandler(),
                GutterIconRenderer.Alignment.RIGHT);
    }

    static PsiElement getLeaf(PsiElement element) {
        while (element.getFirstChild() != null) {
            element = element.getFirstChild();
        }
        return element;
    }

    @Nullable
    @Override
    public LineMarkerInfo getLineMarkerInfo(@NotNull PsiElement element) {
        if (element instanceof PasRoutineImplDeclImpl) {
            if (myDaemonSettings.SHOW_METHOD_SEPARATORS) {
                return LineMarkersPass.createMethodSeparatorLineMarker(getLeaf(element), myColorsManager);
            }
        } else if ((element instanceof PasClassTypeDecl) || (element instanceof PasRecordDecl)) {
            String name = ((PascalStructType) element).getName().toUpperCase();
            Project project = element.getProject();
            if (StubUtil.keyExists(PascalHelperIndex.KEY, name, project, GlobalSearchScope.projectScope(project), PascalStructType.class)) {
                return collectHelpers((PascalStructType) element);
            }
        }
        return null;
    }

    @Override
    public void collectSlowLineMarkers(@NotNull List<PsiElement> elements, @NotNull Collection<LineMarkerInfo> result) {
        for (PsiElement element : elements) {
            if ((element instanceof PascalRoutine) || (element instanceof PascalStructType) || (element instanceof PasUsesClause) || (element instanceof PasUnitModuleHead)) {
                collectNavigationMarkers(element, result);
            }
        }
    }

    private LineMarkerInfo collectHelpers(PascalStructType structType) {
        PsiElement leaf = PascalLineMarkerProvider.getLeaf(structType);
        return new LineMarkerInfo<PsiElement>(leaf, leaf.getTextRange(),
                PascalIcons.HELPER, Pass.LINE_MARKERS, PascalMarker.HELPERS.getTooltip(), PascalMarker.HELPERS.getHandler(), GutterIconRenderer.Alignment.LEFT) {
        };
    }

}
