package com.siberika.idea.pascal;

import com.intellij.notification.Notification;
import com.intellij.notification.NotificationListener;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.intellij.openapi.components.ProjectComponent;
import org.jetbrains.annotations.NotNull;

/**
 * Author: George Bakhtadze
 * Date: 03/01/2016
 */
public class PascalProjectService implements ProjectComponent {

    public PascalProjectService() {
    }

    @Override
    public void projectOpened() {
        Notifications.Bus.notify(new Notification(PascalAppService.PASCAL_NOTIFICATION_GROUP, PascalBundle.message("app.welcome.title"),
                PascalBundle.message("app.welcome.text"), NotificationType.INFORMATION,
                new NotificationListener.UrlOpeningListener(true)));
    }

    @Override
    public void projectClosed() {

    }

    @Override
    public void initComponent() {
    }

    @Override
    public void disposeComponent() {
    }

    @NotNull
    @Override
    public String getComponentName() {
        return PascalBundle.message("app.title");
    }
}
