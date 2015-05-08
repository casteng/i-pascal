package com.siberika.idea.pascal.sdk;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.Result;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import com.intellij.openapi.projectRoots.AdditionalDataConfigurable;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileEvent;
import com.intellij.openapi.vfs.VirtualFileListener;
import com.intellij.psi.search.FileTypeIndex;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import com.intellij.util.FileContentUtil;
import com.intellij.util.indexing.FileBasedIndex;
import com.siberika.idea.pascal.PPUFileType;
import com.siberika.idea.pascal.PascalBundle;
import com.siberika.idea.pascal.jps.sdk.PascalSdkData;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.util.Collection;

/**
 * Author: George Bakhtadze
 * Date: 18/01/2013
 */
public class PascalSdkConfigUI implements AdditionalDataConfigurable {
    private Sdk sdk;
    JTextField optionsEdit;
    JTextField commandEdit;

    public JComponent createComponent() {
        JPanel panel = new JPanel();
        panel.setLayout(new GridLayoutManager(2, 2, new Insets(0, 0, 0, 0), -1, -1));
        final JLabel label1 = new JLabel();
        label1.setText(PascalBundle.message("ui.sdkSettings.compiler.options"));
        panel.add(label1, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));

        optionsEdit = new JTextField();
        panel.add(optionsEdit, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));

        final JLabel label2 = new JLabel();
        label2.setText(PascalBundle.message("ui.sdkSettings.decompiler.command"));
        panel.add(label2, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));

        commandEdit = new JTextField();
        panel.add(commandEdit, new GridConstraints(1, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));

        return panel;
    }

    public boolean isModified() {
        return (optionsEdit != null) && !optionsEdit.getText().equals(BasePascalSdkType.getAdditionalData(sdk).getValue(PascalSdkData.DATA_KEY_COMPILER_OPTIONS)) ||
               (commandEdit != null) && !commandEdit.getText().equals(BasePascalSdkType.getAdditionalData(sdk).getValue(PascalSdkData.DATA_KEY_DECOMPILER_COMMAND));
    }

    public void apply() throws ConfigurationException {
        if (optionsEdit != null) {
            BasePascalSdkType.getAdditionalData(sdk).setValue(PascalSdkData.DATA_KEY_COMPILER_OPTIONS, optionsEdit.getText());
        }
        if (commandEdit != null) {
            BasePascalSdkType.getAdditionalData(sdk).setValue(PascalSdkData.DATA_KEY_DECOMPILER_COMMAND, commandEdit.getText());
            BasePascalSdkType.getAdditionalData(sdk).setValue(PascalSdkData.DATA_KEY_DECOMPILER_CACHE, null);

            invalidateCompiledCache();
        }
    }

    private void invalidateCompiledCache() {
        ApplicationManager.getApplication().invokeLater(new Runnable() {
            @Override
            public void run() {
                Project[] projects = ProjectManager.getInstance().getOpenProjects();
                final FileDocumentManager documentManager = FileDocumentManager.getInstance();
                for (final Project project : projects) {
                    final Module[] modules = ModuleManager.getInstance(project).getModules();
                    new WriteCommandAction(project) {
                        @Override
                        protected void run(@NotNull Result result) throws Throwable {
                            for (Module module : modules) {
                                Collection<VirtualFile> files = FileBasedIndex.getInstance().getContainingFiles(FileTypeIndex.NAME, PPUFileType.INSTANCE, GlobalSearchScope.moduleWithDependenciesAndLibrariesScope(module));
                                for (VirtualFile virtualFile : files) {
                                    ((VirtualFileListener) documentManager).contentsChanged(new VirtualFileEvent(null, virtualFile, virtualFile.getName(), virtualFile.getParent()));
                                }
                                FileContentUtil.reparseFiles(files);
                            }
                        }
                    }.execute();
                }
            }
        });
    }

    public void reset() {
        if (optionsEdit != null) {
            optionsEdit.setText((String) BasePascalSdkType.getAdditionalData(sdk).getValue(PascalSdkData.DATA_KEY_COMPILER_OPTIONS));
        }
        if (commandEdit != null) {
            commandEdit.setText((String) BasePascalSdkType.getAdditionalData(sdk).getValue(PascalSdkData.DATA_KEY_DECOMPILER_COMMAND));
        }
    }

    public void disposeUIResources() {
    }

    @Override
    public void setSdk(Sdk sdk) {
        this.sdk = sdk;
    }
}

