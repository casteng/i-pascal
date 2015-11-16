package com.siberika.idea.pascal.module;

import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleConfigurationEditor;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.roots.CollectingContentIterator;
import com.intellij.openapi.roots.ModuleFileIndex;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.roots.ui.configuration.ModuleConfigurationState;
import com.intellij.openapi.ui.ComboBox;
import com.intellij.openapi.ui.TextBrowseFolderListener;
import com.intellij.openapi.ui.TextFieldWithBrowseButton;
import com.intellij.openapi.vfs.VirtualFile;
import com.siberika.idea.pascal.PascalBundle;
import com.siberika.idea.pascal.PascalFileType;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Author: George Bakhtadze
 * Date: 14/01/2013
 */
public class PascalModuleOptionsEditor implements ModuleConfigurationEditor {
    final ModuleConfigurationState state;
    final Module module;

    private JComponent myComponent;
    private TextFieldWithBrowseButton exePathEdit;
    private JComboBox mainFileCBox;

    public PascalModuleOptionsEditor(ModuleConfigurationState state, Module module) {
        assert PascalModuleType.isPascalModule(module);
        this.state = state;
        this.module = module;
    }

    @Override
    public void saveData() {
    }

    @Override
    public void moduleStateChanged() {
    }

    @Nls
    @Override
    public String getDisplayName() {
        return PascalBundle.message("ui.module.options.editor.name");
    }

    @Nullable
    @Override
    public String getHelpTopic() {
        return null;
    }

    @Nullable
    @Override
    public JComponent createComponent() {
        if (myComponent == null) {
            myComponent = createComponentImpl();
        }
        return myComponent;
    }

    private JComponent createComponentImpl() {
        final JPanel panel = new JPanel(new GridBagLayout());

        panel.add(new JLabel(PascalBundle.message("ui.module.options.editor.mainFile.label")),
                new GridBagConstraints(0,0,1,1,0.0, 0.0, GridBagConstraints.NORTHWEST, GridBagConstraints.NONE, new Insets(10, 6, 6, 0), 0, 0));
        panel.add(new JLabel(PascalBundle.message("ui.module.options.editor.exePath.label")),
                new GridBagConstraints(0,1,1,1,0.0, 0.0, GridBagConstraints.NORTHWEST, GridBagConstraints.NONE, new Insets(10, 6, 6, 0), 0, 0));

        final ModuleFileIndex index = ModuleRootManager.getInstance(module).getFileIndex();
        final List<VirtualFile> pascalFiles = new ArrayList<VirtualFile>();
        index.iterateContent(new CollectingContentIterator() {
            @NotNull
            @Override
            public List<VirtualFile> getFiles() {
                return pascalFiles;
            }

            @Override
            public boolean processFile(VirtualFile fileOrDir) {
                if (fileOrDir.isValid() && !fileOrDir.isDirectory()
                    && PascalFileType.INSTANCE.equals(fileOrDir.getFileType())) {
                    pascalFiles.add(fileOrDir);
                }
                return true;
            }
        });

        mainFileCBox = new ComboBox(pascalFiles.toArray());
        panel.add(mainFileCBox,
                new GridBagConstraints(1, 0, 1, 1, 1.0, 0.0, GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL, new Insets(6, 6, 6, 6), 0, 0));

        FileChooserDescriptor fileChooserDescriptor = FileChooserDescriptorFactory.createSingleFolderDescriptor();
        //noinspection DialogTitleCapitalization
        fileChooserDescriptor.setTitle(PascalBundle.message("title.choose.directory"));
        exePathEdit = new TextFieldWithBrowseButton();
        exePathEdit.addBrowseFolderListener(new TextBrowseFolderListener(fileChooserDescriptor));

        panel.add(exePathEdit, new GridBagConstraints(1, 1, 1, 1, 1.0, 0.0, GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL, new Insets(6, 6, 6, 6), 0, 0));

        return panel;
    }

    @Override
    public boolean isModified() {
        return true;
    }

    @Override
    public void apply() throws ConfigurationException {
        PascalModuleType.setMainFile(module, (VirtualFile) mainFileCBox.getSelectedItem());
        PascalModuleType.setExeOutputPath(module, exePathEdit.getText());
    }

    @Override
    public void reset() {
        mainFileCBox.setSelectedItem(PascalModuleType.getMainFile(module));
        exePathEdit.setText(PascalModuleType.getExeOutputPath(module));
    }

    @Override
    public void disposeUIResources() {
    }
}
