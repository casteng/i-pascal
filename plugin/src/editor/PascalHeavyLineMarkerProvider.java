package com.siberika.idea.pascal.editor;

import com.intellij.codeInsight.daemon.LineMarkerInfo;
import com.intellij.codeInsight.daemon.LineMarkerProvider;
import com.intellij.concurrency.JobLauncher;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressIndicatorProvider;
import com.intellij.openapi.util.Computable;
import com.intellij.psi.PsiElement;
import com.intellij.util.Processor;
import com.siberika.idea.pascal.editor.linemarker.PascalMarker;
import com.siberika.idea.pascal.lang.psi.PasEntityScope;
import com.siberika.idea.pascal.lang.psi.PasExportedRoutine;
import com.siberika.idea.pascal.lang.psi.PasRoutineImplDecl;
import com.siberika.idea.pascal.lang.psi.PascalNamedElement;
import com.siberika.idea.pascal.lang.psi.PascalRoutine;
import com.siberika.idea.pascal.lang.psi.PascalStructType;
import com.siberika.idea.pascal.lang.search.PascalDefinitionsSearch;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
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
        ApplicationManager.getApplication().assertReadAccessAllowed();
        List<Computable<List<LineMarkerInfo>>> tasks = new ArrayList<>();
        Processor<? super PasEntityScope> consumer = (Processor<PasEntityScope>) descending -> false;
        for (PsiElement element : elements) {
            tasks.add(new Computable<List<LineMarkerInfo>>() {
                @Override
                public List<LineMarkerInfo> compute() {
                    boolean noMarker = true;
                    if (element instanceof PascalStructType) {
                        noMarker = PascalDefinitionsSearch.findImplementations(((PascalNamedElement) element).getNameIdentifier(), consumer);
                    } else if ((element instanceof PasExportedRoutine) || (element instanceof PasRoutineImplDecl)) {
                        PasEntityScope scope = ((PasEntityScope) element).getContainingScope();
                        if (scope instanceof PascalStructType) {
                            noMarker = PascalDefinitionsSearch.findImplementingMethods((PascalRoutine) element, consumer);
                        }
                    }
                    if (noMarker) {
                        return Collections.emptyList();
                    } else {
                        return Collections.singletonList(PascalLineMarkerProvider.createLineMarkerInfo(element, AllIcons.Gutter.OverridenMethod, PascalMarker.DESCENDING_ENTITIES));
                    }
                }
            });
        }
        Object lock = new Object();
        ProgressIndicator indicator = ProgressIndicatorProvider.getGlobalProgressIndicator();
        JobLauncher.getInstance().invokeConcurrentlyUnderProgress(tasks, indicator, computable -> {
            List<LineMarkerInfo> infos = computable.compute();
            synchronized (lock) {
                result.addAll(infos);
            }
            return true;
        });
    }

}
