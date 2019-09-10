package com.siberika.idea.pascal.debugger.settings;

import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.util.xmlb.XmlSerializerUtil;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@State(
        name = "PascalTypeRenderersSettings",
        storages = @Storage("pascal.debugger.xml")
)
public class PascalTypeRenderers implements PersistentStateComponent<PascalTypeRenderers> {

    public List<TypeRenderer> typeRenderers = new ArrayList<>();

    public static PascalTypeRenderers getInstance() {
        return ServiceManager.getService(PascalTypeRenderers.class);
    }

    @Override
    public void loadState(@NotNull PascalTypeRenderers state) {
        XmlSerializerUtil.copyBean(state, this);
    }

    @Override
    public PascalTypeRenderers getState() {
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PascalTypeRenderers that = (PascalTypeRenderers) o;
        return Objects.equals(typeRenderers, that.typeRenderers);
    }

    @Override
    public int hashCode() {
        return Objects.hash(typeRenderers);
    }
}
