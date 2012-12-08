/*
 * Copyright 2010 Jon S Akhtar (Sylvanaar)
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */

package com.siberika.idea.pascal;

import com.intellij.lang.*;
import com.intellij.openapi.fileTypes.*;
import com.siberika.idea.pascal.lang.PascalLanguage;
import org.jetbrains.annotations.*;

import javax.swing.*;

/**
 * Author: George Bakhtadze
 * Date: 12.05.2012
 */
public class PascalFileType extends LanguageFileType {
    public static final PascalFileType PASCAL_FILE_TYPE = new PascalFileType();

    public static final Language PASCAL_LANGUAGE = PASCAL_FILE_TYPE.getLanguage();
    @NonNls
    public static final String DEFAULT_EXTENSION = "pas";
    public static final String PASCAL = "Pascal";
    public static final String PASCAL_PLUGIN_ID = PASCAL;

    public static final ExtensionFileNameMatcher[] EXTENSION_FILE_NAME_MATCHERS = {
            new ExtensionFileNameMatcher(PascalFileType.DEFAULT_EXTENSION), new ExtensionFileNameMatcher("pp"),
            new ExtensionFileNameMatcher("lpr"), new ExtensionFileNameMatcher("dpr"),
    };

    private PascalFileType() {
        super(new PascalLanguage());
    }

    /**
     * Creates a language file type for the specified language.
     *
     * @param language The language used in the files of the type.
     */
    protected PascalFileType(@NotNull Language language) {
        super(language);
    }

    @NotNull
    public String getName() {
        return PASCAL;
    }

    @NotNull
    public String getDescription() {
        return PascalBundle.message("pascal.filetype");
    }

    @NotNull
    public String getDefaultExtension() {
        return DEFAULT_EXTENSION;
    }

    public Icon getIcon() {
        return LuaIcons.LUA_ICON;
    }

}



