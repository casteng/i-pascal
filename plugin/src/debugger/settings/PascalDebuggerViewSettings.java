package com.siberika.idea.pascal.debugger.settings;

import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.util.xmlb.XmlSerializerUtil;
import org.jetbrains.annotations.NotNull;

@State(
        name = "PascalDebuggerViewsSettings",
        storages = @Storage("pascal.debugger.xml")
)
public class PascalDebuggerViewSettings implements PersistentStateComponent<PascalDebuggerViewSettings> {

    public boolean showNonPrintable = true;
    public boolean refineStrings = true;
    public boolean refineDynamicArrays = true;
    public boolean refineOpenArrays = true;
    public boolean refineStructured = true;
    public int limitChars = 1000;
    public int limitElements = 1000;
    public int limitChilds = 100;
    public int limitValueSize = 2 * 1024 * 1024;

    public static PascalDebuggerViewSettings getInstance() {
        return ServiceManager.getService(PascalDebuggerViewSettings.class);
    }

    @Override
    public void loadState(@NotNull PascalDebuggerViewSettings state) {
        XmlSerializerUtil.copyBean(state, this);
    }

    @Override
    public PascalDebuggerViewSettings getState() {
        return this;
    }

    public boolean equals(Object object) {
        if (!(object instanceof PascalDebuggerViewSettings)) return false;
        PascalDebuggerViewSettings generalSettings = ((PascalDebuggerViewSettings)object);
        return showNonPrintable == generalSettings.showNonPrintable;
    }
}
