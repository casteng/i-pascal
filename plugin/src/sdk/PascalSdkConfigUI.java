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
import com.intellij.openapi.ui.ComboBox;
import com.intellij.openapi.ui.TextBrowseFolderListener;
import com.intellij.openapi.ui.TextFieldWithBrowseButton;
import com.intellij.openapi.util.Disposer;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileEvent;
import com.intellij.openapi.vfs.VirtualFileListener;
import com.intellij.psi.search.FileTypeIndex;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.ui.JBColor;
import com.intellij.ui.TabbedPaneWrapper;
import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import com.intellij.util.FileContentUtil;
import com.intellij.util.indexing.FileBasedIndex;
import com.intellij.util.ui.JBUI;
import com.siberika.idea.pascal.DCUFileType;
import com.siberika.idea.pascal.PPUFileType;
import com.siberika.idea.pascal.PascalBundle;
import com.siberika.idea.pascal.jps.sdk.PascalSdkData;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
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
    private JTextField namespacesEdit;
    private JTextField compilerOptionsEdit;
    private JTextField compilerOptionsDebugEdit;
    private TextFieldWithBrowseButton decompilerCommandEdit;
    private TextFieldWithBrowseButton gdbCommandEdit;
    private ComboBox debugBackendCBox;
    private JTextField gdbOptionsEdit;
    private JCheckBox gdbResolveNames;
    private JCheckBox gdbRedirectConsole;
    private JCheckBox debugBreakFullNames;
    private JCheckBox gdbRetrieveChilds;
    private JCheckBox gdbUseGdbInit;
    private final Map<String, JComponent> keyComponentMap = new HashMap<String, JComponent>();

    @Override
    public JComponent createComponent() {
        TabbedPaneWrapper myTabbedPane = new TabbedPaneWrapper(myDisposable);
        myTabbedPane.addTab(PascalBundle.message("ui.sdkSettings.tab.general"), createGeneralOptionsPanel());
        myTabbedPane.addTab(PascalBundle.message("ui.sdkSettings.tab.debugger"), createDebuggerOptionsPanel());
        if (!(sdk.getSdkType() instanceof FPCSdkType)) {
            myTabbedPane.getTabComponentAt(1).setVisible(false);
        }

        keyComponentMap.clear();
        keyComponentMap.put(PascalSdkData.Keys.COMPILER_COMMAND.getKey(), compilerCommandEdit);
        keyComponentMap.put(PascalSdkData.Keys.COMPILER_NAMESPACES.getKey(), namespacesEdit);
        keyComponentMap.put(PascalSdkData.Keys.COMPILER_OPTIONS.getKey(), compilerOptionsEdit);
        keyComponentMap.put(PascalSdkData.Keys.COMPILER_OPTIONS_DEBUG.getKey(), compilerOptionsDebugEdit);
        keyComponentMap.put(PascalSdkData.Keys.DECOMPILER_COMMAND.getKey(), decompilerCommandEdit);

        keyComponentMap.put(PascalSdkData.Keys.DEBUGGER_BACKEND.getKey(), debugBackendCBox);
        keyComponentMap.put(PascalSdkData.Keys.DEBUGGER_COMMAND.getKey(), gdbCommandEdit);
        keyComponentMap.put(PascalSdkData.Keys.DEBUGGER_OPTIONS.getKey(), gdbOptionsEdit);
        keyComponentMap.put(PascalSdkData.Keys.DEBUGGER_REDIRECT_CONSOLE.getKey(), gdbRedirectConsole);
        keyComponentMap.put(PascalSdkData.Keys.DEBUGGER_BREAK_FULL_NAME.getKey(), debugBreakFullNames);
        keyComponentMap.put(PascalSdkData.Keys.DEBUGGER_RETRIEVE_CHILDS.getKey(), gdbRetrieveChilds);
        keyComponentMap.put(PascalSdkData.Keys.DEBUGGER_RESOLVE_NAMES.getKey(), gdbResolveNames);
        keyComponentMap.put(PascalSdkData.Keys.DEBUGGER_USE_GDBINIT.getKey(), gdbUseGdbInit);

        return myTabbedPane.getComponent();
    }

    private JPanel createGeneralOptionsPanel() {
        JPanel panel = new JPanel();
        panel.setBorder(new LineBorder(JBColor.border()));
        panel.setLayout(new GridLayoutManager(6, 2, JBUI.emptyInsets(), -1, -1));

        int row = 0;
        addLabel(panel, PascalBundle.message("ui.sdkSettings.compiler.command"), row);
        compilerCommandEdit = addFileFieldWithBrowse(panel, row++);

        addLabel(panel, PascalBundle.message("ui.sdkSettings.compiler.namespaces"), row);
        namespacesEdit = new JTextField();
        panel.add(namespacesEdit, new GridConstraints(row++, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL,
                GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                null, null, null, 0, false));

        addLabel(panel, PascalBundle.message("ui.sdkSettings.compiler.options"), row);
        compilerOptionsEdit = new JTextField();
        panel.add(compilerOptionsEdit, new GridConstraints(row++, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));

        addLabel(panel, PascalBundle.message("ui.sdkSettings.compiler.options.debug"), row);
        compilerOptionsDebugEdit = new JTextField();
        panel.add(compilerOptionsDebugEdit, new GridConstraints(row++, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));

        addLabel(panel, PascalBundle.message("ui.sdkSettings.decompiler.command"), row);
        decompilerCommandEdit = addFileFieldWithBrowse(panel, row++);

        JLabel statusLabel = new JLabel();
        panel.add(statusLabel, new GridConstraints(row, 0, 1, 2, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL,
                GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 1, false));
        if (BasePascalSdkType.getAdditionalData(sdk).getBoolean(PascalSdkData.Keys.DELPHI_IS_STARTER)) {
            statusLabel.setText(PascalBundle.message("ui.sdkSettings.delphi.starter.warning"));
        }
        return panel;
    }

    private JPanel createDebuggerOptionsPanel() {
        JPanel panel = new JPanel();
        panel.setBorder(new LineBorder(JBColor.border()));
        panel.setLayout(new GridLayoutManager(9, 2, JBUI.emptyInsets(), -1, -1));

        int row = 0;
        addLabel(panel, PascalBundle.message("ui.sdkSettings.debug.backend"), row);
        debugBackendCBox = new ComboBox(PascalSdkData.DEBUGGER_BACKENDS);
        panel.add(debugBackendCBox, new GridConstraints(row++, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));

        addLabel(panel, PascalBundle.message("ui.sdkSettings.gdb.command"), row);
        gdbCommandEdit = addFileFieldWithBrowse(panel, row);
        panel.add(gdbCommandEdit, new GridConstraints(row++, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));

        addLabel(panel, PascalBundle.message("ui.sdkSettings.gdb.options"), row);
        gdbOptionsEdit = new JTextField();
        panel.add(gdbOptionsEdit, new GridConstraints(row++, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));

        addLabel(panel, PascalBundle.message("ui.sdkSettings.gdb.redirect.console"), row);
        gdbRedirectConsole = new JCheckBox();
        panel.add(gdbRedirectConsole, new GridConstraints(row++, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));

        addLabel(panel, PascalBundle.message("ui.sdkSettings.debug.break.fullnames"), row);
        debugBreakFullNames = new JCheckBox();
        panel.add(debugBreakFullNames, new GridConstraints(row++, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));

        addLabel(panel, PascalBundle.message("ui.sdkSettings.gdb.retrieve.childs"), row);
        gdbRetrieveChilds = new JCheckBox();
        panel.add(gdbRetrieveChilds, new GridConstraints(row++, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));

        addLabel(panel, PascalBundle.message("ui.sdkSettings.gdb.resolve.names"), row);
        gdbResolveNames = new JCheckBox();
        panel.add(gdbResolveNames, new GridConstraints(row++, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));

        addLabel(panel, PascalBundle.message("ui.sdkSettings.gdb.use.gdbinit"), row);
        gdbUseGdbInit = new JCheckBox();
        panel.add(gdbUseGdbInit, new GridConstraints(row++, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));

        final JTextArea statusLabel = new JTextArea();
        statusLabel.setLineWrap(true);
        statusLabel.setMinimumSize(new Dimension(100, 20));
        panel.add(statusLabel, new GridConstraints(row++, 0, 1, 2, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL,
                GridConstraints.SIZEPOLICY_CAN_SHRINK, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 1, false));
        statusLabel.setText(PascalBundle.message("ui.sdkSettings.lldb.variables.warning"));

        debugBackendCBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                statusLabel.setVisible(PascalSdkData.DEBUGGER_BACKENDS[1].equals(debugBackendCBox.getSelectedItem()));
            }
        });

        return panel;
    }

    private void addLabel(JPanel panel, String caption, int row) {
        final JLabel label1 = new JLabel(caption);
        panel.add(label1, new GridConstraints(row, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE,
                GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 1, false));
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
        if ((decompilerCommandEdit != null) &&
                !getValue(keyComponentMap.get(PascalSdkData.Keys.DECOMPILER_COMMAND.getKey())).equals(
                        BasePascalSdkType.getAdditionalData(sdk).getValue(PascalSdkData.Keys.DECOMPILER_COMMAND.getKey()))
                ) {
            BasePascalSdkType.getAdditionalData(sdk).setValue(PascalSdkData.Keys.DECOMPILER_CACHE.getKey(), null);
            invalidateCompiledCache();
        }
        for (Map.Entry<String, JComponent> entry : keyComponentMap.entrySet()) {
            BasePascalSdkType.getAdditionalData(sdk).setValue(entry.getKey(), getValue(keyComponentMap.get(entry.getKey())));
        }
        BasePascalSdkType.invalidateSdkCaches();
    }

    private Object getValue(JComponent control) {
        if (control instanceof TextFieldWithBrowseButton) {
            return ((TextFieldWithBrowseButton) control).getText();
        } else if (control instanceof JTextField) {
            return ((JTextField) control).getText();
        } else if (control instanceof JCheckBox) {
            return ((JCheckBox) control).isSelected() ? PascalSdkData.SDK_DATA_TRUE : "0";
        } else if (control instanceof ComboBox) {
            return ((ComboBox) control).getSelectedItem();
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
            ((JCheckBox) control).setSelected(PascalSdkData.SDK_DATA_TRUE.equals(value));
        } else if (control instanceof ComboBox) {
            ((ComboBox) control).setSelectedItem(value);
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

