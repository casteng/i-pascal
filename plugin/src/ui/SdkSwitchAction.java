package com.siberika.idea.pascal.ui;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.roots.ui.configuration.projectRoot.ProjectSdksModel;
import com.intellij.openapi.ui.popup.JBPopup;
import com.intellij.openapi.ui.popup.PopupStep;
import com.intellij.openapi.ui.popup.util.BaseListPopupStep;
import com.intellij.ui.popup.list.ListPopupImpl;
import com.siberika.idea.pascal.PascalBundle;
import com.siberika.idea.pascal.sdk.BasePascalSdkType;
import org.jetbrains.annotations.NotNull;

import java.awt.event.InputEvent;
import java.util.ArrayList;
import java.util.List;

public class SdkSwitchAction extends AnAction implements DumbAware {

    private JBPopup popup;

    public SdkSwitchAction() {
        this.setEnabledInModalContext(true);
    }

    public void update(@NotNull AnActionEvent e) {
        e.getPresentation().setEnabled(e.getProject() != null);
    }

    public void actionPerformed(@NotNull AnActionEvent e) {
        final Project project = e.getProject();
        if (project != null) {
            if (popup != null && popup.isVisible()) {
                popup.cancel();
            } else {
                ProjectSdksModel model = new ProjectSdksModel();
                model.reset(project);
                Sdk currentSdk = model.getProjectSdk();
                int selectedIndex = -1;
                List<Sdk> pascalSdks = new ArrayList<Sdk>();
                for (Sdk sdk : model.getSdks()) {
                    if (sdk.getSdkType() instanceof BasePascalSdkType) {
                        if (currentSdk == sdk) {
                            selectedIndex = pascalSdks.size();
                        }
                        pascalSdks.add(sdk);
                    }
                }
                BaseListPopupStep step = new BaseListPopupStepSdkChoice(project, PascalBundle.message("ui.sdkSwitch.title"), pascalSdks);
                step.setDefaultOptionIndex(selectedIndex);
                popup = new ListPopupImpl(step);
                InputEvent event = e.getInputEvent();
                if (event != null) {
                    popup.showUnderneathOf(e.getInputEvent().getComponent());
                } else {
                    popup.showCenteredInCurrentWindow(project);
                }
            }
        }
    }

    private class BaseListPopupStepSdkChoice extends BaseListPopupStep<Sdk> {
        private final Project project;

        BaseListPopupStepSdkChoice(Project project, String sdk_popup, List<Sdk> sdks) {
            super(sdk_popup, sdks);
            this.project = project;
        }

        @Override
        public PopupStep onChosen(Sdk selectedValue, boolean finalChoice) {
            ApplicationManager.getApplication().runWriteAction(new Runnable() {
                @Override
                public void run() {
                    ProjectRootManager.getInstance(project).setProjectSdk(selectedValue);
                }
            });
            return super.onChosen(selectedValue, finalChoice);
        }

    }

}
