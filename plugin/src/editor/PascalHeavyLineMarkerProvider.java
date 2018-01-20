package com.siberika.idea.pascal.editor;

import com.intellij.codeInsight.daemon.GutterIconNavigationHandler;
import com.intellij.codeInsight.daemon.LineMarkerInfo;
import com.intellij.codeInsight.daemon.LineMarkerProvider;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.project.DumbService;
import com.intellij.psi.PsiElement;
import com.intellij.psi.search.PsiElementProcessor;
import com.intellij.util.SmartList;
import com.siberika.idea.pascal.PascalBundle;
import com.siberika.idea.pascal.ide.actions.PascalDefinitionsSearch;
import com.siberika.idea.pascal.lang.psi.PasEntityScope;
import com.siberika.idea.pascal.lang.psi.PasExportedRoutine;
import com.siberika.idea.pascal.lang.psi.PasInvalidScopeException;
import com.siberika.idea.pascal.lang.psi.PasRoutineImplDecl;
import com.siberika.idea.pascal.lang.psi.PascalNamedElement;
import com.siberika.idea.pascal.lang.psi.PascalStructType;
import com.siberika.idea.pascal.util.EditorUtil;
import com.siberika.idea.pascal.util.PsiUtil;
import gnu.trove.THashSet;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.event.MouseEvent;
import java.util.Arrays;
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
                if (element instanceof PascalStructType) {
                    // Goto implementations
                    Collection<PasEntityScope> impls = PascalDefinitionsSearch.findImplementations(((PascalNamedElement) element).getNameIdentifier(), 1, 0);
                    if (!impls.isEmpty()) {
                        result.add(PascalLineMarkerProvider.createLineMarkerInfo((PasEntityScope) element, AllIcons.Gutter.OverridenMethod,
                                        PascalBundle.message("navigate.title.goto.subclassed"), getHandler(PascalBundle.message("navigate.title.goto.subclassed"))));
                    }
                } else if ((element instanceof PasExportedRoutine) || (element instanceof PasRoutineImplDecl)) {
                    PasEntityScope scope = ((PasEntityScope) element).getContainingScope();
                    if (scope instanceof PascalStructType) {
                        Collection<PasEntityScope> inheritedScopes = new SmartList<PasEntityScope>();
                        PascalDefinitionsSearch.findDescendingStructs(inheritedScopes, (PascalStructType) scope, 1, 0);
                        if (!inheritedScopes.isEmpty()) {
                            result.add(PascalLineMarkerProvider.createLineMarkerInfo((PasEntityScope) element, AllIcons.Gutter.OverridenMethod,
                                    PascalBundle.message("navigate.title.goto.subclassed"), getHandler(PascalBundle.message("navigate.title.goto.subclassed"))));
                        }
                    }
                }
            }
        } catch (PasInvalidScopeException e) {
            e.printStackTrace();
        }
    }

    static GutterIconNavigationHandler<PsiElement> getHandler(final String title) {
        return new GutterIconNavigationHandler<PsiElement>() {
            @Override
            public void navigate(MouseEvent e, final PsiElement elt) {
                if (DumbService.isDumb(elt.getProject())) {
                    DumbService.getInstance(elt.getProject()).showDumbModeNotification(PascalBundle.message("navigate.subclassed.impossible.reindex"));
                    return;
                }

                final PasEntityScope scope = PsiUtil.getNearestAffectingScope(elt);

                final PsiElementProcessor.CollectElementsWithLimit<PasEntityScope> collectProcessor = new PsiElementProcessor.CollectElementsWithLimit<PasEntityScope>(100, new THashSet<PasEntityScope>());
                if (!ProgressManager.getInstance().runProcessWithProgressSynchronously(new Runnable() {
                    @Override
                    public void run() {
                        ApplicationManager.getApplication().runReadAction(new Runnable() {
                            @Override
                            public void run() {
                                Collection<PasEntityScope> impls = PascalDefinitionsSearch.findImplementations((scope).getNameIdentifier(), 100, 0);
                                for (PasEntityScope impl : impls) {
                                    collectProcessor.execute(impl);
                                }
                            }
                        });
                    }
                }, PascalBundle.message("navigate.title.goto.subclassed.search"), true, elt.getProject(), (JComponent)e.getComponent())) {
                    return;
                }

                List<PasEntityScope> inheritors = Arrays.asList(collectProcessor.toArray(new PasEntityScope[0]));
                EditorUtil.navigateTo(e, title, PascalBundle.message("navigate.info.subclassed.noitems"), inheritors);
            }
        };
    }

}
