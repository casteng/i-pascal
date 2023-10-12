package com.siberika.idea.pascal;

import com.intellij.notification.Notification;
import com.intellij.notification.NotificationListener;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.intellij.openapi.actionSystem.KeyboardShortcut;
import com.intellij.openapi.actionSystem.Shortcut;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.keymap.Keymap;
import com.intellij.openapi.keymap.impl.KeymapManagerImpl;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.startup.StartupActivity;
import com.siberika.idea.pascal.util.ModuleUtil;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Author: George Bakhtadze
 * Date: 13/01/2016
 */
public class PostStartupActivity implements StartupActivity {

    private static final Logger LOG = Logger.getInstance(PostStartupActivity.class);

    private static final List<String> ACTION__SHORTCUT_CONFLICT_STOPLIST = Arrays.asList("IncrementWindowHeight", "DecrementWindowHeight", "ResizeToolWindowUp", "ResizeToolWindowDown"); // TODO: determine which actions are for editor

    @Override
    public void runActivity(@NotNull Project project) {
        if (ModuleUtil.hasPascalModules(project)) {
            Notifications.Bus.notify(new Notification(PascalAppService.PASCAL_NOTIFICATION_GROUP, PascalBundle.message("app.welcome.title"),
                    PascalBundle.message("app.welcome.text"), NotificationType.INFORMATION,
                    new NotificationListener.UrlOpeningListener(true)));
            setupShortcuts();
        }
    }

    private void setupShortcuts() {
        LOG.info("Checking shortcuts for duplicates...");
        Keymap keymap = KeymapManagerImpl.getInstance().getActiveKeymap().getParent();
        if (keymap == null) {
            keymap = KeymapManagerImpl.getInstance().getActiveKeymap();
        }
        if (keymap != null) {
            Shortcut[] shortcuts = keymap.getShortcuts("Pascal.ToggleSection");
            for (Shortcut shortcut : shortcuts) {
                if (shortcut instanceof KeyboardShortcut) {
                    Map<String, List<KeyboardShortcut>> conflicts = keymap.getConflicts("Pascal.ToggleSection", (KeyboardShortcut) shortcut);
                    for (Map.Entry<String, List<KeyboardShortcut>> entry : conflicts.entrySet()) {
                        if (!ACTION__SHORTCUT_CONFLICT_STOPLIST.contains(entry.getKey()) && !entry.getValue().isEmpty()) {
                            LOG.warn(String.format("Removed shortcut conflicting with Pascal.ToggleSection action from action %s", entry.getKey()));
                            for (KeyboardShortcut keyboardShortcut : entry.getValue()) {
                                keymap.removeShortcut(entry.getKey(), keyboardShortcut);
                            }
                        }
                    }
                }
            }
        } else {
            LOG.error("No keymap found");
        }
    }

}
