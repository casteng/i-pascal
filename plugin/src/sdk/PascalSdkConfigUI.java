package com.siberika.idea.pascal.sdk;

import com.intellij.openapi.Disposable;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.Result;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import com.intellij.openapi.projectRoots.AdditionalDataConfigurable;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.ui.TextBrowseFolderListener;
import com.intellij.openapi.ui.TextFieldWithBrowseButton;
import com.intellij.openapi.util.Disposer;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileEvent;
import com.intellij.openapi.vfs.VirtualFileListener;
import com.intellij.psi.search.FileTypeIndex;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.ui.TabbedPaneWrapper;
import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import com.intellij.util.FileContentUtil;
import com.intellij.util.indexing.FileBasedIndex;
import com.siberika.idea.pascal.DCUFileType;
import com.siberika.idea.pascal.PPUFileType;
import com.siberika.idea.pascal.PascalBundle;
import com.siberika.idea.pascal.jps.sdk.PascalSdkData;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Author: George Bakhtadze
 * Date: 18/01/2013
 */
public class PascalSdkConfigUI implements AdditionalDataConfigurable {
    private final Disposable myDisposable = Disposer.newDisposable();

    private TextFieldWithBrowseButton compilerCommandEdit;

    private Sdk sdk;
    private JTextField compilerOptionsEdit;
    private TextFieldWithBrowseButton decompilerCommandEdit;
    private TextFieldWithBrowseButton gdbCommandEdit;
    private JTextField gdbOptionsEdit;
    private JCheckBox gdbResolveNames;
    private JCheckBox gdbRedirectConsole;
    private JCheckBox gdbRetrieveChilds;
    private JCheckBox gdbUseGdbInit;
    private final Map<String, JComponent> keyComponentMap = new HashMap<String, JComponent>();

    public JComponent createComponent() {
        TabbedPaneWrapper myTabbedPane = new TabbedPaneWrapper(myDisposable);
        myTabbedPane.addTab(PascalBundle.message("ui.sdkSettings.tab.general"), createGeneralOptionsPanel());
        myTabbedPane.addTab(PascalBundle.message("ui.sdkSettings.tab.debugger"), createDebuggerOptionsPanel());

        keyComponentMap.clear();
        keyComponentMap.put(PascalSdkData.keys.COMPILER_COMMAND.getKey(), compilerCommandEdit);
        keyComponentMap.put(PascalSdkData.keys.COMPILER_OPTIONS.getKey(), compilerOptionsEdit);
        keyComponentMap.put(PascalSdkData.keys.DECOMPILER_COMMAND.getKey(), decompilerCommandEdit);

        keyComponentMap.put(PascalSdkData.keys.DEBUGGER_COMMAND.getKey(), gdbCommandEdit);
        keyComponentMap.put(PascalSdkData.keys.DEBUGGER_OPTIONS.getKey(), gdbOptionsEdit);
        keyComponentMap.put(PascalSdkData.keys.DEBUGGER_REDIRECT_CONSOLE.getKey(), gdbRedirectConsole);
        keyComponentMap.put(PascalSdkData.keys.DEBUGGER_RETRIEVE_CHILDS.getKey(), gdbRetrieveChilds);
        keyComponentMap.put(PascalSdkData.keys.DEBUGGER_RESOLVE_NAMES.getKey(), gdbResolveNames);
        keyComponentMap.put(PascalSdkData.keys.DEBUGGER_USE_GDBINIT.getKey(), gdbUseGdbInit);

        return myTabbedPane.getComponent();
    }

    private JPanel createGeneralOptionsPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new GridLayoutManager(3, 2, new Insets(0, 0, 0, 0), -1, -1));

        addLabel(panel, PascalBundle.message("ui.sdkSettings.compiler.command"), 0);
        compilerCommandEdit = addFileFieldWithBrowse(panel, 0);

        addLabel(panel, PascalBundle.message("ui.sdkSettings.compiler.options"), 1);
        compilerOptionsEdit = new JTextField();
        panel.add(compilerOptionsEdit, new GridConstraints(1, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));

        addLabel(panel, PascalBundle.message("ui.sdkSettings.decompiler.command"), 2);
        decompilerCommandEdit = addFileFieldWithBrowse(panel, 2);

        return panel;
    }

    private JPanel createDebuggerOptionsPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new GridLayoutManager(6, 2, new Insets(0, 0, 0, 0), -1, -1));

        addLabel(panel, PascalBundle.message("ui.sdkSettings.gdb.command"), 0);
        gdbCommandEdit = addFileFieldWithBrowse(panel, 0);
        panel.add(gdbCommandEdit, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));

        addLabel(panel, PascalBundle.message("ui.sdkSettings.gdb.options"), 1);
        gdbOptionsEdit = new JTextField();
        panel.add(gdbOptionsEdit, new GridConstraints(1, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));

        addLabel(panel, PascalBundle.message("ui.sdkSettings.gdb.redirect.console"), 2);
        gdbRedirectConsole = new JCheckBox();
        panel.add(gdbRedirectConsole, new GridConstraints(2, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));

        addLabel(panel, PascalBundle.message("ui.sdkSettings.gdb.retrieve.childs"), 3);
        gdbRetrieveChilds = new JCheckBox();
        panel.add(gdbRetrieveChilds, new GridConstraints(3, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));

        addLabel(panel, PascalBundle.message("ui.sdkSettings.gdb.resolve.names"), 4);
        gdbResolveNames = new JCheckBox();
        panel.add(gdbResolveNames, new GridConstraints(4, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));

        addLabel(panel, PascalBundle.message("ui.sdkSettings.gdb.use.gdbinit"), 5);
        gdbUseGdbInit = new JCheckBox();
        panel.add(gdbUseGdbInit, new GridConstraints(5, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));

        return panel;
    }

    private void addLabel(JPanel panel, String caption, int row) {
        final JLabel label1 = new JLabel();
        label1.setText(caption);
        panel.add(label1, new GridConstraints(row, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE,
                GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
    }

    private TextFieldWithBrowseButton addFileFieldWithBrowse(JPanel panel, int row) {
        FileChooserDescriptor fileChooserDescriptor = FileChooserDescriptorFactory.createSingleFileDescriptor();
        fileChooserDescriptor.setTitle(PascalBundle.message("title.choose.file"));
        TextFieldWithBrowseButton field = new TextFieldWithBrowseButton();
        field.addBrowseFolderListener(new TextBrowseFolderListener(fileChooserDescriptor));
        panel.add(field, new GridConstraints(row, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL,
                GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                null, null, null, 0, false));
        return field;
    }

    public boolean isModified() {
        for (Map.Entry<String, JComponent> entry : keyComponentMap.entrySet()) {
            if (!getValue(entry.getValue()).equals(BasePascalSdkType.getAdditionalData(sdk).getValue(entry.getKey()))) {
                return true;
            }
        }
        return false;
    }

    public void apply() throws ConfigurationException {
        for (Map.Entry<String, JComponent> entry : keyComponentMap.entrySet()) {
            BasePascalSdkType.getAdditionalData(sdk).setValue(entry.getKey(), getValue(keyComponentMap.get(entry.getKey())));
        }
        if ((decompilerCommandEdit != null) &&
                !getValue(keyComponentMap.get(PascalSdkData.keys.DECOMPILER_COMMAND.getKey())).equals(
                        BasePascalSdkType.getAdditionalData(sdk).getValue(PascalSdkData.keys.DECOMPILER_COMMAND.getKey()))
                ) {
            BasePascalSdkType.getAdditionalData(sdk).setValue(PascalSdkData.keys.DECOMPILER_CACHE.getKey(), null);
            invalidateCompiledCache();
        }
    }

    private Object getValue(JComponent control) {
        if (control instanceof TextFieldWithBrowseButton) {
            return ((TextFieldWithBrowseButton) control).getText();
        } else if (control instanceof JTextField) {
            return ((JTextField) control).getText();
        } else if (control instanceof JCheckBox) {
            return ((JCheckBox) control).isSelected() ? "1" : "0";
        } else {
            throw new IllegalStateException("getValue: Invalid control: " + ((control != null) ? control.getClass() : "null"));
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
                                files.addAll(FileBasedIndex.getInstance().getContainingFiles(FileTypeIndex.NAME, DCUFileType.INSTANCE, GlobalSearchScope.moduleWithDependenciesAndLibrariesScope(module)));
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
        for (Map.Entry<String, JComponent> entry : keyComponentMap.entrySet()) {
            setValue(keyComponentMap.get(entry.getKey()), BasePascalSdkType.getAdditionalData(sdk).getValue(entry.getKey()));
        }
    }

    private void setValue(JComponent control, Object value) {
        if (control instanceof TextFieldWithBrowseButton) {
            ((TextFieldWithBrowseButton) control).setText((String) value);
        } else if (control instanceof JTextField) {
            ((JTextField) control).setText((String) value);
        } else if (control instanceof JCheckBox) {
            ((JCheckBox) control).setSelected("1".equals(value));
        } else {
            throw new IllegalStateException("setValue: Invalid control: " + ((control != null) ? control.getClass() : "null"));
        }
    }

    public void disposeUIResources() {
    }

    @Override
    public void setSdk(Sdk sdk) {
        this.sdk = sdk;
    }
}

