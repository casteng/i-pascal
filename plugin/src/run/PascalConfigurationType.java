package com.siberika.idea.pascal.run;

import com.intellij.execution.configurations.ConfigurationFactory;
import com.intellij.execution.configurations.ConfigurationType;
import com.intellij.execution.configurations.RunConfiguration;
import com.intellij.execution.configurations.RunConfigurationModule;
import com.intellij.openapi.extensions.Extensions;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.intellij.util.containers.ContainerUtil;
import com.siberika.idea.pascal.PascalIcons;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

/**
 * Author: George Bakhtadze
 * Date: 12/5/12
 */
public class PascalConfigurationType implements ConfigurationType, DumbAware {
    private final ConfigurationFactory myFactory;

    public PascalConfigurationType() {
        myFactory = new ConfigurationFactory(this) {
            @NotNull
            @Override
            public RunConfiguration createTemplateConfiguration(@NotNull Project project) {
                return new PascalRunConfiguration("", new RunConfigurationModule(project), this);
            }
        };
    }
    public String getDisplayName() {
        return "Pascal executable";
    }

    public String getConfigurationTypeDescription() {
        return "Pascal run configuration";
    }

    public Icon getIcon() {
        return PascalIcons.GENERAL;
    }

    @NotNull
    public String getId() {
        return "#com.siberika.idea.pascal.run.PascalConfigurationType";
    }

    public static PascalConfigurationType getInstance() {
        return ContainerUtil.findInstance(Extensions.getExtensions(CONFIGURATION_TYPE_EP), PascalConfigurationType.class);
    }

    public ConfigurationFactory[] getConfigurationFactories() {
        return new ConfigurationFactory[]{myFactory};
    }

    public boolean isDumbAware() {
        return true;
    }
}
