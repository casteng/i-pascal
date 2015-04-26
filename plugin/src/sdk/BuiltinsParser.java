package com.siberika.idea.pascal.sdk;

import com.intellij.testFramework.LightVirtualFile;
import com.siberika.idea.pascal.PascalFileType;
import org.apache.xmlbeans.impl.common.IOUtil;

import java.io.IOException;
import java.io.InputStream;

/**
* Author: George Bakhtadze
* Date: 03/10/2013
*/
public class BuiltinsParser {

    private static LightVirtualFile BUILTINS = prepareBuiltins();

    private static LightVirtualFile prepareBuiltins() {
        LightVirtualFile res = new LightVirtualFile("$builtins.pas", PascalFileType.INSTANCE, "Error occured while preparing builtins");
        InputStream data = BuiltinsParser.class.getResourceAsStream("/builtins.pas");
        try {
            IOUtil.copyCompletely(data, res.getOutputStream(null));
        } catch (IOException e) {
        }
        return res;
    }

    public static LightVirtualFile getBuiltinsSource() {
        return BUILTINS;
    }
}
