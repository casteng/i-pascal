package com.siberika.idea.pascal.editor.linemarker;

import com.intellij.codeInsight.daemon.GutterIconNavigationHandler;
import com.intellij.codeInsight.daemon.impl.PsiElementListNavigator;
import com.intellij.codeInsight.navigation.BackgroundUpdaterTask;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.project.DumbService;
import com.intellij.psi.NavigatablePsiElement;
import com.intellij.psi.PsiElement;
import com.intellij.util.Processor;
import com.intellij.util.Query;
import com.siberika.idea.pascal.PascalBundle;
import com.siberika.idea.pascal.util.EditorUtil;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.event.MouseEvent;

public abstract class CollectionNavigationHandler<T extends PsiElement> implements GutterIconNavigationHandler<PsiElement> {

    private final String titleMsg;
    private final String searchTitleMsg;
    private final String noItemsMsg;
    private final String impossibleReindexMsg;

    CollectionNavigationHandler(String titleMsg, String searchTitleMsg, String noItemsMsg, String impossibleReindexMsg) {
        this.titleMsg = titleMsg;
        this.searchTitleMsg = searchTitleMsg;
        this.noItemsMsg = noItemsMsg;
        this.impossibleReindexMsg = impossibleReindexMsg;
    }

    abstract Query<T> createQuery(PsiElement element);

    @Override
    public void navigate(MouseEvent e, PsiElement elt) {
        if (DumbService.isDumb(elt.getProject())) {
            DumbService.getInstance(elt.getProject()).showDumbModeNotification(impossibleReindexMsg);
            return;
        }
        NavigatablePsiElement[] targetsNav = new NavigatablePsiElement[1];
        if (!ProgressManager.getInstance().runProcessWithProgressSynchronously(() -> {
            createQuery(elt).forEach(new Processor<T>() {
                        @Override
                        public boolean process(T element) {
                            targetsNav[0] = (NavigatablePsiElement) element;
                            return false;
                        }
                    });
        }, searchTitleMsg, true, elt.getProject(), (JComponent) e.getComponent())) {
            return;
        }
        if (targetsNav[0] == null) {
            return;
        }

        BackgroundUpdaterTask updater = new TargetsUpdater(elt);

        PsiElementListNavigator.openTargets(e, targetsNav, searchTitleMsg,searchTitleMsg, new EditorUtil.MyPsiElementCellRenderer(), updater);
    }

    private class TargetsUpdater extends BackgroundUpdaterTask {
        @NotNull
        private final PsiElement element;

        public TargetsUpdater(@NotNull PsiElement element) {
            super(element.getProject(), searchTitleMsg, null);
            this.element = element;
        }

        @Override
        public String getCaption(int size) {
            return String.format("%s (%d %s)", titleMsg, size, PascalBundle.message("navigate.status.found"));
        }

        @Override
        public void onSuccess() {
            super.onSuccess();
            PsiElement oneElement = getTheOnlyOneElement();
            if (oneElement instanceof NavigatablePsiElement) {
                ((NavigatablePsiElement)oneElement).navigate(true);
                myPopup.cancel();
            }
        }

        @Override
        public void run(@NotNull ProgressIndicator indicator) {
            super.run(indicator);
            createQuery(element).forEach(new Processor<T>() {
                @Override
                public boolean process(T element) {
                    if (!updateComponent(element)) {
                        indicator.cancel();
                    }
                    ProgressManager.checkCanceled();
                    return true;
                }
            });
        }
    }
}
