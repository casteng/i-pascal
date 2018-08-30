package com.siberika.idea.pascal.run;

import com.intellij.execution.actions.ConfigurationContext;
import com.intellij.execution.actions.ConfigurationFromContext;
import com.intellij.execution.actions.RunConfigurationProducer;
import com.intellij.execution.configurations.RunConfiguration;
import com.intellij.lang.ASTNode;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.util.Ref;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.siberika.idea.pascal.lang.psi.PasBlockBody;
import com.siberika.idea.pascal.lang.psi.PasBlockGlobal;
import com.siberika.idea.pascal.lang.psi.PasModule;
import com.siberika.idea.pascal.lang.psi.PasProgramModuleHead;
import com.siberika.idea.pascal.lang.psi.PasTypes;
import com.siberika.idea.pascal.lang.psi.PascalModule;
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
        if (isProgramLeafElement(sourceElement.get())) {
            setupConf(context, configuration, true);
            return true;
        }
        return false;
    }

    @Override
    public boolean isConfigurationFromContext(PascalRunConfiguration configuration, ConfigurationContext context) {
        return (configuration.getConfigurationModule().getModule() == context.getModule()) &&
                (context.getPsiLocation() != null) &&
                (configuration.getProgramFileName().equals(getProgramFileName(context)));
    }

    private String getProgramFileName(@NotNull ConfigurationContext context) {
        VirtualFile mainFile = context.getPsiLocation() != null ? getMainFile(context.getPsiLocation()) : null;
        return mainFile != null ? mainFile.getNameWithoutExtension() : null;
    }

    private void setupConf(ConfigurationContext context, RunConfiguration conf, boolean setupModule) {
        if (conf instanceof PascalRunConfiguration) {
            conf.setName(context.getProject().getName());
            Module module = context.getModule();
            if (PascalModuleType.isPascalModule(module) && context.getPsiLocation() != null) {
                conf.setName(module.getName());
                PascalRunConfiguration pasConf = (PascalRunConfiguration) conf;
                pasConf.setModule(module);
                pasConf.setProgramFileName(getProgramFileName(context));
                VirtualFile mainFile = getMainFile(context.getPsiLocation());
                if (mainFile != null) {
                    conf.setName(String.format("[%s] %s", module.getName(), mainFile.getNameWithoutExtension()));
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
        return isProgramLeafElement(context.getPsiLocation()) ? super.createConfigurationFromContext(context) : null;
    }

    private static VirtualFile getMainFile(@NotNull PsiElement element) {
        PsiFile mainFile = element.getContainingFile();
        return mainFile != null ? mainFile.getVirtualFile() : null;
    }

    public static boolean isProgramLeafElement(PsiElement element) {
        ASTNode node = element.getNode();
        if ((null == node) || ((node.getElementType() != PasTypes.BEGIN) && (node.getElementType() != PasTypes.PROGRAM))) {
            return false;
        }
        if ((element.getFirstChild() == null) && (element.getParent().getFirstChild() == element) &&
                (element.getParent() instanceof PasProgramModuleHead
                        || element.getParent().getParent() instanceof PasBlockBody && element.getParent().getParent().getParent() instanceof PasBlockGlobal)
                ) {
            PasModule module = PsiUtil.getElementPasModule(element);
            return module != null && module.getModuleType() == PascalModule.ModuleType.PROGRAM;
        }
        return false;
    }
}