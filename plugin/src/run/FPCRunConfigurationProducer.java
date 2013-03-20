package com.siberika.idea.pascal.run;

import com.intellij.execution.Location;
import com.intellij.execution.RunManagerEx;
import com.intellij.execution.RunnerAndConfigurationSettings;
import com.intellij.execution.actions.ConfigurationContext;
import com.intellij.execution.impl.RunnerAndConfigurationSettingsImpl;
import com.intellij.execution.junit.RuntimeConfigurationProducer;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.siberika.idea.pascal.PascalBundle;
import com.siberika.idea.pascal.PascalFileType;

/**
 * Author: George Bakhtadze
 * Date: 12/5/12
 */
public class FPCRunConfigurationProducer extends RuntimeConfigurationProducer implements Cloneable {
    private PsiFile sourceFile = null;

    public FPCRunConfigurationProducer() {
        super(FPCConfigurationType.getInstance());
    }

    @Override
    public PsiElement getSourceElement() {
        return sourceFile;
    }

    @Override
    protected RunnerAndConfigurationSettings createConfigurationByElement(Location location, ConfigurationContext configurationContext) {
        sourceFile = location.getPsiElement().getContainingFile();

        Project project = location.getProject();
        Module module = location.getModule();

        if ((sourceFile != null) && (module != null) &&
                sourceFile.getFileType().equals(PascalFileType.INSTANCE)) {
            RunnerAndConfigurationSettings settings = cloneTemplateConfiguration(project, configurationContext);
            settings.setName(PascalBundle.message("common.module") + " " + module.getName());
            PascalRunConfiguration configuration = (PascalRunConfiguration)settings.getConfiguration();
            configuration.getConfigurationModule().setModule(module);
            //settings.getConfiguration().set
            //RunnerAndConfigurationSettings settings = RunManagerEx.getInstanceEx(project).createConfiguration("", getConfigurationFactory());
            return settings;
        }

        return null;
    }

    public int compareTo(Object o) {
        return 0;
    }
}