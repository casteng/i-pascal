package com.siberika.idea.pascal.run;

import com.intellij.execution.actions.ConfigurationContext;
import com.intellij.execution.actions.ConfigurationFromContext;
import com.intellij.execution.actions.RunConfigurationProducer;
import com.intellij.execution.configurations.RunConfiguration;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.util.Ref;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.siberika.idea.pascal.lang.psi.PasBlockBody;
import com.siberika.idea.pascal.lang.psi.PasBlockGlobal;
import com.siberika.idea.pascal.lang.psi.PasModule;
import com.siberika.idea.pascal.lang.psi.PasProgramModuleHead;
import com.siberika.idea.pascal.lang.psi.impl.PascalModule;
import com.siberika.idea.pascal.module.PascalModuleType;
import com.siberika.idea.pascal.util.PsiUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Author: George Bakhtadze
 * Date: 12/5/12
 */
public class PascalRunContextConfigurationProducer extends RunConfigurationProducer<PascalRunConfiguration> implements Cloneable {
    public PascalRunContextConfigurationProducer() {
        super(PascalConfigurationType.getInstance());
    }

    @Override
    protected boolean setupConfigurationFromContext(PascalRunConfiguration configuration, ConfigurationContext context, Ref<PsiElement> sourceElement) {
        if (isProgramElement(sourceElement.get())) {
            setupConf(context, configuration, true);
            return true;
        }
        return false;
    }

    @Override
    public boolean isConfigurationFromContext(PascalRunConfiguration configuration, ConfigurationContext context) {
        return (configuration.getConfigurationModule().getModule() == context.getModule()) &&
                (configuration.getProgramFileName().equals(getMainFile(context.getPsiLocation()).getPath()));
    }

    static boolean isProgramElement(PsiElement element) {
        if (element instanceof PasProgramModuleHead || element instanceof PasBlockBody && element.getParent() instanceof PasBlockGlobal) {
            PasModule module = PsiUtil.getElementPasModule(element);
            return module != null && module.getModuleType() == PascalModule.ModuleType.PROGRAM;
        }
        return false;
    }

    private void setupConf(ConfigurationContext context, RunConfiguration conf, boolean setupModule) {
        if (conf instanceof PascalRunConfiguration) {
            conf.setName(context.getProject().getName());
            Module module = context.getModule();
            if (PascalModuleType.isPascalModule(module) && context.getPsiLocation() != null) {
                conf.setName(module.getName());
                ((PascalRunConfiguration) conf).setModule(module);
                VirtualFile mainFile = getMainFile(context.getPsiLocation());
                if (mainFile != null) {
                    conf.setName(String.format("[%s] %s", module.getName(), mainFile.getNameWithoutExtension()));
                    ((PascalRunConfiguration) conf).setProgramFileName(mainFile.getNameWithoutExtension());
                    if (setupModule) {
                        PascalModuleType.setMainFile(module, mainFile);
                    }
                }
            }
        }
    }

    @Nullable
    @Override
    public ConfigurationFromContext createConfigurationFromContext(ConfigurationContext context) {
        ConfigurationFromContext conf = super.createConfigurationFromContext(context);
        setupConf(context, conf != null ? conf.getConfiguration() : null, true);
        return conf;
    }

    private static VirtualFile getMainFile(@NotNull PsiElement element) {
        PsiFile mainFile = element.getContainingFile();
        return mainFile != null ? mainFile.getVirtualFile() : null;
    }

}