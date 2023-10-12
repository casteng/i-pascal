package com.siberika.idea.pascal.sdk;

import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiManager;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.testFramework.LightVirtualFile;
import com.siberika.idea.pascal.PascalFileType;
import com.siberika.idea.pascal.lang.psi.PascalModule;
import org.apache.xmlbeans.impl.common.IOUtil;

import java.io.IOException;
import java.io.InputStream;

/**
* Author: George Bakhtadze
* Date: 03/10/2013
*/
public class BuiltinsParser {

    public static final String UNIT_NAME_BUILTINS = "$builtins.pas";
    private static LightVirtualFile BUILTINS = prepareBuiltins();

    private static LightVirtualFile prepareBuiltins() {
        LightVirtualFile res = new LightVirtualFile(UNIT_NAME_BUILTINS, PascalFileType.INSTANCE, "Error occured while preparing builtins");
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

    public static PascalModule getBuiltinsModule(Project project) {
        return PsiTreeUtil.getChildOfType(PsiManager.getInstance(project).findFile(BUILTINS), PascalModule.class);
    }
}
