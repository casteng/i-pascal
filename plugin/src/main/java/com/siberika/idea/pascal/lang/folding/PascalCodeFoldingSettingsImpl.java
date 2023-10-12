package com.siberika.idea.pascal.lang.folding;

import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.util.xmlb.XmlSerializerUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@State(name = "PascalCodeFoldingSettings", storages = @Storage("editor.codeinsight.xml"))
public class PascalCodeFoldingSettingsImpl extends PascalCodeFoldingSettings implements PersistentStateComponent<PascalCodeFoldingSettingsImpl> {

    private boolean FOLD_WITH_BLOCKS = false;
    private boolean COLLAPSE_ENUMS = true;

    @Nullable
    @Override
    public PascalCodeFoldingSettingsImpl getState() {
        return this;
    }

    @Override
    public void loadState(@NotNull PascalCodeFoldingSettingsImpl state) {
        XmlSerializerUtil.copyBean(state, this);
    }


    @Override
    public boolean isFoldWithBlocks() {
        return FOLD_WITH_BLOCKS;
    }

    @Override
    public void setFoldWithBlocks(boolean value) {
        FOLD_WITH_BLOCKS = value;
    }

    @Override
    public boolean isCollapseEnums() {
        return COLLAPSE_ENUMS;
    }

    @Override
    public void setCollapseEnums(boolean value) {
        COLLAPSE_ENUMS = value;
    }
}
