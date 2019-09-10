package com.siberika.idea.pascal.debugger.settings;

import com.intellij.openapi.options.SearchableConfigurable;
import com.siberika.idea.pascal.PascalBundle;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class TypeRenderersConfigurable implements SearchableConfigurable {

    private TypeRenderersTable table;

    @Override
    public String getDisplayName() {
        return PascalBundle.message("debug.settings.type.renderers");
    }

    @Override
    public JComponent createComponent() {
        table = new TypeRenderersTable();
        return table.getComponent();
    }

    @Override
    public void apply() {
        PascalTypeRenderers instance = PascalTypeRenderers.getInstance();
        instance.typeRenderers = new ArrayList<>(table.getTypeRenderers().size());
        doCopy(table.getTypeRenderers(), instance.typeRenderers);
    }

    @Override
    public void reset() {
        table.getTypeRenderers().clear();
        table.setValues(PascalTypeRenderers.getInstance().typeRenderers);
        table.refreshValues();
    }

    @Override
    public boolean isModified() {
        return !Objects.equals(PascalTypeRenderers.getInstance().typeRenderers, table.getTypeRenderers());
    }

    private void doCopy(List<TypeRenderer> src, List<TypeRenderer> dest) {
        for (TypeRenderer typeRenderer : src) {
            dest.add(typeRenderer.clone());
        }
    }

    @SuppressWarnings("SpellCheckingInspection")
    @Override
    @NotNull
    public String getHelpTopic() {
        return "Debugger_Pascal_Type_Renderers";
    }

    @Override
    @NotNull
    public String getId() {
        return getHelpTopic();
    }

}
