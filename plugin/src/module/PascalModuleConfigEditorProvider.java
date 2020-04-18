package com.siberika.idea.pascal.module;

import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleConfigurationEditor;
import com.intellij.openapi.roots.ui.configuration.DefaultModuleConfigurationEditorFactory;
import com.intellij.openapi.roots.ui.configuration.ModuleConfigurationEditorProvider;
import com.intellij.openapi.roots.ui.configuration.ModuleConfigurationState;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * Author: George Bakhtadze
 * Date: 13/01/2013
 */
public class PascalModuleConfigEditorProvider implements ModuleConfigurationEditorProvider {
    public ModuleConfigurationEditor[] createEditors(@NotNull final ModuleConfigurationState state) {
        final Module module = state.getRootModel().getModule();
        if (!PascalModuleType.isPascalModule(module)) return ModuleConfigurationEditor.EMPTY;
        final DefaultModuleConfigurationEditorFactory editorFactory = DefaultModuleConfigurationEditorFactory.getInstance();
        final List<ModuleConfigurationEditor> editors = new ArrayList<ModuleConfigurationEditor>();

        editors.add(editorFactory.createModuleContentRootsEditor(state));
        editors.add(editorFactory.createOutputEditor(state));
        editors.add(editorFactory.createClasspathEditor(state));
        editors.add(new PascalModuleOptionsEditor(state, module));

        return editors.toArray(new ModuleConfigurationEditor[editors.size()]);
    }
}