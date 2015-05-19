package com.siberika.idea.pascal;

import com.intellij.openapi.fileTypes.LanguageFileType;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * User: George Bakhtadze
 * Date: 09.12.2012
 */
public class PascalFileType extends LanguageFileType {
    public static final PascalFileType INSTANCE = new PascalFileType();

    public static final Set<String> UNIT_EXTENSIONS = new HashSet<String>(Arrays.asList("pas", "pp"));
    public static final Set<String> PROGRAM_EXTENSIONS = new HashSet<String>(Arrays.asList("dpr", "lpr"));

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
