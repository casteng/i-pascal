package com.siberika.idea.pascal;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.fileTypes.LanguageFileType;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

/**
 * User: George Bakhtadze
 * Date: 09.12.2012
 */
public class PascalFileType extends LanguageFileType {
    public static final PascalFileType INSTANCE = new PascalFileType();

    protected PascalFileType() {
        super(PascalLanguage.INSTANCE);
    }

    @NotNull
    @Override
    public String getName() {
        return "Pascal";
    }

    @NotNull
    @Override
    public String getDescription() {
        return "Pascal Source";
    }

    @NotNull
    @Override
    public String getDefaultExtension() {
        return "pas";
    }

    @Override
    public Icon getIcon() {
        return PascalIcons.UNIT;
    }

}
