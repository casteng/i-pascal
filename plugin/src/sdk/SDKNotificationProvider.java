package com.siberika.idea.pascal.sdk;

import com.intellij.ProjectTopics;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.fileEditor.FileEditor;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtilCore;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectBundle;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.roots.ModuleRootEvent;
import com.intellij.openapi.roots.ModuleRootListener;
import com.intellij.openapi.roots.ModuleRootModificationUtil;
import com.intellij.openapi.roots.ui.configuration.ProjectSettingsService;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.ui.EditorNotificationPanel;
import com.intellij.ui.EditorNotifications;
import com.siberika.idea.pascal.PascalFileType;
import com.siberika.idea.pascal.util.ModuleUtil;
import org.jetbrains.annotations.NotNull;

/**
 * Author: George Bakhtadze
 * Date: 18/11/2018
 */
public class SDKNotificationProvider extends EditorNotifications.Provider<EditorNotificationPanel> implements DumbAware {
    private static final Key<EditorNotificationPanel> KEY = Key.create("Setup Pascal SDK");

    private final Project project;

    public SDKNotificationProvider(Project project, final EditorNotifications notifications) {
        this.project = project;
        this.project.getMessageBus().connect(project).subscribe(ProjectTopics.PROJECT_ROOTS, new ModuleRootListener() {
            @Override
            public void rootsChanged(ModuleRootEvent event) {
                notifications.updateAllNotifications();
            }
        });
    }

    @NotNull
    @Override
    public Key<EditorNotificationPanel> getKey() {
        return KEY;
    }

    @Override
    public EditorNotificationPanel createNotificationPanel(@NotNull VirtualFile file, @NotNull FileEditor fileEditor) {
        if (!(file.getFileType() instanceof PascalFileType)) {
            return null;
        }
        Sdk sdk = ModuleUtil.getSdk(project, file);
        if (sdk != null) {
            return null;
        }
        return createPanel(project, file);
    }

    @NotNull
    private static EditorNotificationPanel createPanel(@NotNull final Project project, @NotNull final VirtualFile file) {
        EditorNotificationPanel panel = new EditorNotificationPanel();
        panel.setText(ProjectBundle.message("project.sdk.not.defined"));
        panel.createActionLabel(ProjectBundle.message("project.sdk.setup"), () -> {
            Sdk projectSdk = ProjectSettingsService.getInstance(project).chooseAndSetSdk();
            if (projectSdk == null) {
                return;
            }
            ApplicationManager.getApplication().runWriteAction(() -> {
                Module module = ModuleUtilCore.findModuleForFile(file, project);
                if (module != null) {
                    ModuleRootModificationUtil.setSdkInherited(module);
                }
            });
        });
        return panel;
    }
}
