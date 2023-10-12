package com.siberika.idea.pascal.lang.folding;

import com.intellij.application.options.editor.CodeFoldingOptionsProvider;
import com.intellij.openapi.options.BeanConfigurable;
import com.siberika.idea.pascal.PascalBundle;

public class PascalFoldingOptionsProvider extends BeanConfigurable<PascalCodeFoldingSettings> implements CodeFoldingOptionsProvider {

    protected PascalFoldingOptionsProvider() {
        super(PascalCodeFoldingSettings.getInstance());
        PascalCodeFoldingSettings settings = getInstance();

        checkBox(PascalBundle.message("ui.settings.folding.collapse.enums"), settings::isCollapseEnums, settings::setCollapseEnums);
        checkBox(PascalBundle.message("ui.settings.folding.with"), settings::isFoldWithBlocks, settings::setFoldWithBlocks);
    }
}
