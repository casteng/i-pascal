package com.siberika.idea.pascal;

import com.intellij.notification.Notification;
import com.intellij.notification.NotificationListener;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.startup.StartupActivity;
import com.siberika.idea.pascal.module.PascalModuleType;
import org.jetbrains.annotations.NotNull;

/**
 * Author: George Bakhtadze
 * Date: 13/01/2016
 */
public class PostStartupActivity implements StartupActivity {
    @Override
    public void runActivity(@NotNull Project project) {
        Module[] modules = ModuleManager.getInstance(project).getModules();
        boolean hasPascalModules = false;
        for (Module module : modules) {
            hasPascalModules |= PascalModuleType.isPascalModule(module);
        }
        if (hasPascalModules || (modules.length == 0)) {
            Notifications.Bus.notify(new Notification(PascalAppService.PASCAL_NOTIFICATION_GROUP, PascalBundle.message("app.welcome.title"),
                    PascalBundle.message("app.welcome.text"), NotificationType.INFORMATION,
                    new NotificationListener.UrlOpeningListener(true)));
        }
    }
}
