package com.siberika.idea.pascal.editor;

import com.intellij.codeHighlighting.Pass;
import com.intellij.codeInsight.daemon.DaemonCodeAnalyzerSettings;
import com.intellij.codeInsight.daemon.GutterIconNavigationHandler;
import com.intellij.codeInsight.daemon.LineMarkerInfo;
import com.intellij.codeInsight.daemon.LineMarkerProvider;
import com.intellij.codeInsight.daemon.impl.LineMarkersPass;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.colors.EditorColorsManager;
import com.intellij.openapi.editor.markup.GutterIconRenderer;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.ConstantFunction;
import com.intellij.util.SmartList;
import com.siberika.idea.pascal.PascalBundle;
import com.siberika.idea.pascal.ide.actions.GotoSuper;
import com.siberika.idea.pascal.ide.actions.SectionToggle;
import com.siberika.idea.pascal.lang.psi.PasEntityScope;
import com.siberika.idea.pascal.lang.psi.PasInvalidScopeException;
import com.siberika.idea.pascal.lang.psi.PasNamespaceIdent;
import com.siberika.idea.pascal.lang.psi.PasUnitInterface;
import com.siberika.idea.pascal.lang.psi.PasUnitModuleHead;
import com.siberika.idea.pascal.lang.psi.PasUsesClause;
import com.siberika.idea.pascal.lang.psi.PascalModule;
import com.siberika.idea.pascal.lang.psi.PascalNamedElement;
import com.siberika.idea.pascal.lang.psi.PascalRoutine;
import com.siberika.idea.pascal.lang.psi.PascalStructType;
import com.siberika.idea.pascal.lang.psi.impl.PasExportedRoutineImpl;
import com.siberika.idea.pascal.lang.psi.impl.PasRoutineImplDeclImpl;
import com.siberika.idea.pascal.lang.references.ResolveUtil;
import com.siberika.idea.pascal.util.EditorUtil;
import com.siberika.idea.pascal.util.ModuleUtil;
import com.siberika.idea.pascal.util.PsiUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.event.MouseEvent;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;

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
            result.add(createLineMarkerInfo(element, impl ? AllIcons.Gutter.ImplementedMethod : AllIcons.Gutter.ImplementingMethod,
                    msg("navigate.title.toggle.section"), getHandler(msg("navigate.title.toggle.section"), Collections.singletonList(target))));
        }
        // Goto super
        if (element instanceof PascalNamedElement) {
            PascalNamedElement namedElement = (PascalNamedElement) element;
            Collection<PasEntityScope> supers = GotoSuper.retrieveGotoSuperTargets(namedElement.getNameIdentifier());
            if (!supers.isEmpty()) {
                result.add(createLineMarkerInfo(element, AllIcons.Gutter.OverridingMethod, msg("navigate.title.goto.super"),
                        getHandler(msg("navigate.title.goto.super"), supers)));
            }
            if (element instanceof PasUnitModuleHead) {
                Collection<PascalModule> modules = retrieveUsingModules(((PasUnitModuleHead) element).getNamespaceIdent());
                if (!modules.isEmpty()) {
                    result.add(createLineMarkerInfo(element, AllIcons.Hierarchy.Caller, msg("navigate.title.used.by"),
                            getHandler(msg("navigate.title.used.by"), modules)));
                }
            }
        }
    }

    private Collection<PascalModule> retrieveUsingModules(@Nullable PasNamespaceIdent namespaceIdent) {
        if (null == namespaceIdent) {
            return Collections.emptyList();
        }
        String name = namespaceIdent.getName();
        String namespace = namespaceIdent.getNamespace();
        String namespaceless = namespaceIdent.getNamePart();
        boolean checkNamespaceless = false;
        if (StringUtil.isNotEmpty(namespace)) {
            for (String prefix : ModuleUtil.retrieveUnitNamespaces(namespaceIdent)) {
                if (namespace.equalsIgnoreCase(prefix)) {
                    checkNamespaceless = true;
                    break;
                }
            }
        }
        Collection<PascalModule> res = new SmartList<>();
        for (PascalModule module : ResolveUtil.findUnitsWithStub(namespaceIdent.getProject(), null, null)) {
            collectUnits(res, name, module, module.getUsedUnitsPublic());
            collectUnits(res, name, module, module.getUsedUnitsPrivate());
            if (checkNamespaceless) {
                collectUnits(res, namespaceless, module, module.getUsedUnitsPublic());
                collectUnits(res, namespaceless, module, module.getUsedUnitsPrivate());
            }
        }
        return res;
    }

    private void collectUnits(Collection<PascalModule> res, String name, PascalModule module, Set<String> usedUnitsList) {
        for (String unitName : usedUnitsList) {
            if (unitName.equalsIgnoreCase(name)) {
                res.add(module);
            }
        }
    }

    private String msg(String key) {
        return PascalBundle.message(key);
    }

    static LineMarkerInfo<PsiElement> createLineMarkerInfo(@NotNull PsiElement element, Icon icon, final String tooltip,
                                                           @NotNull GutterIconNavigationHandler<PsiElement> handler) {
        PsiElement el = getLeaf(element);
        return new LineMarkerInfo<PsiElement>(el, el.getTextRange(),
                icon, Pass.LINE_MARKERS,
                new ConstantFunction<PsiElement, String>(tooltip), handler,
                GutterIconRenderer.Alignment.RIGHT);
    }

    private static PsiElement getLeaf(PsiElement element) {
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
        }
        return null;
    }

    @Override
    public void collectSlowLineMarkers(@NotNull List<PsiElement> elements, @NotNull Collection<LineMarkerInfo> result) {
        try {
            for (PsiElement element : elements) {
                if ((element instanceof PascalRoutine) || (element instanceof PascalStructType) || (element instanceof PasUsesClause) || (element instanceof PasUnitModuleHead)) {
                    collectNavigationMarkers(element, result);
                }
            }
        } catch (PasInvalidScopeException e) {
            e.printStackTrace();
        }
    }

    static GutterIconNavigationHandler<PsiElement> getHandler(final String title, @NotNull final Collection<? extends PsiElement> targets) {
        return new GutterIconNavigationHandler<PsiElement>() {
            @Override
            public void navigate(MouseEvent e, PsiElement elt) {
                EditorUtil.navigateTo(e, title, null, targets);
            }
        };
    }

}
