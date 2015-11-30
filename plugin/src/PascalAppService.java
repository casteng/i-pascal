package com.siberika.idea.pascal;

import com.intellij.ide.BrowserUtil;
import com.intellij.notification.Notification;
import com.intellij.notification.NotificationDisplayType;
import com.intellij.notification.NotificationGroup;
import com.intellij.notification.NotificationListener;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.intellij.openapi.components.ApplicationComponent;
import org.jetbrains.annotations.NotNull;

import javax.swing.event.HyperlinkEvent;

/**
 * Author: George Bakhtadze
 * Date: 30/11/2015
 */
public class PascalAppService implements ApplicationComponent {
    private static final String PASCAL_NOTIFICATION_GROUP = "I-Pascal";

    @Override
    public void initComponent() {
        new NotificationGroup(PASCAL_NOTIFICATION_GROUP, NotificationDisplayType.STICKY_BALLOON, true);
        String content = PascalBundle.message("app.welcome.text");
        Notifications.Bus.notify(new Notification(PASCAL_NOTIFICATION_GROUP, "Welcome to I-Pascal!", content, NotificationType.INFORMATION,
                new NotificationListener() {
                    public void hyperlinkUpdate(@NotNull Notification notification,
                                                @NotNull HyperlinkEvent event) {
                        BrowserUtil.browse(event.getURL());
                        notification.expire();
                    }
                }));
    }

    @Override
    public void disposeComponent() {

    }

    @NotNull
    @Override
    public String getComponentName() {
        return "I-Pascal";
    }
}
