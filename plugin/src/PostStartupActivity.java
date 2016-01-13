package com.siberika.idea.pascal;

import com.intellij.notification.Notification;
import com.intellij.notification.NotificationListener;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.startup.StartupActivity;
import org.jetbrains.annotations.NotNull;

/**
 * Author: George Bakhtadze
 * Date: 13/01/2016
 */
public class PostStartupActivity implements StartupActivity {
    @Override
    public void runActivity(@NotNull Project project) {
        Notifications.Bus.notify(new Notification(PascalAppService.PASCAL_NOTIFICATION_GROUP, PascalBundle.message("app.welcome.title"),
                PascalBundle.message("app.welcome.text"), NotificationType.INFORMATION,
                new NotificationListener.UrlOpeningListener(true)));
    }
}
