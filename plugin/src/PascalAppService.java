package com.siberika.idea.pascal;

import com.intellij.notification.NotificationDisplayType;
import com.intellij.notification.NotificationGroup;
import com.intellij.openapi.components.ApplicationComponent;
import org.jetbrains.annotations.NotNull;

/**
 * Author: George Bakhtadze
 * Date: 30/11/2015
 */
public class PascalAppService implements ApplicationComponent {
    public static final String PASCAL_NOTIFICATION_GROUP = "I-Pascal";

    @Override
    public void initComponent() {
        new NotificationGroup(PASCAL_NOTIFICATION_GROUP, NotificationDisplayType.BALLOON, true);
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
