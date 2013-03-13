package com.siberika.idea.pascal.run;

import com.intellij.execution.configurations.ConfigurationFactory;
import com.intellij.execution.configurations.ConfigurationType;
import com.intellij.execution.configurations.RunConfiguration;
import com.intellij.execution.configurations.RunConfigurationModule;
import com.intellij.openapi.extensions.Extensions;
import com.intellij.openapi.project.Project;
import com.intellij.util.containers.ContainerUtil;
import com.siberika.idea.pascal.PascalIcons;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

/**
 * Author: George Bakhtadze
 * Date: 12/5/12
 */
public class FPCConfigurationType implements ConfigurationType {
    private final ConfigurationFactory myFactory;

    public FPCConfigurationType() {
        myFactory = new ConfigurationFactory(this) {
            @Override
            public RunConfiguration createTemplateConfiguration(Project project) {
                return new PascalRunConfiguration("", new RunConfigurationModule(project), this);
            }
        };
    }
    public String getDisplayName() {
        return "FPC executable";
    }

    public String getConfigurationTypeDescription() {
        return "FPC run configuration";
    }

    public Icon getIcon() {
        return PascalIcons.GENERAL;
    }

    @NotNull
    public String getId() {
        return "#com.siberika.idea.pascal.run.FPCConfigurationType";
    }

    public static FPCConfigurationType getInstance() {
        return ContainerUtil.findInstance(Extensions.getExtensions(CONFIGURATION_TYPE_EP), FPCConfigurationType.class);
    }

    public ConfigurationFactory[] getConfigurationFactories() {
        return new ConfigurationFactory[]{myFactory};
    }
}
