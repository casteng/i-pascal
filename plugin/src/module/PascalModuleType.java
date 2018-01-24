package com.siberika.idea.pascal.module;

import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleType;
import com.intellij.openapi.module.ModuleTypeManager;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileManager;
import com.siberika.idea.pascal.PascalIcons;
import com.siberika.idea.pascal.jps.model.JpsPascalModuleType;
import com.siberika.idea.pascal.sdk.BasePascalSdkType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

/**
 * Author: George Bakhtadze
 * Date: 08/01/2013
 */
public class PascalModuleType extends ModuleType<PascalModuleBuilder> {

    public PascalModuleType() {
        super(JpsPascalModuleType.MODULE_TYPE_ID);
    }

    public static PascalModuleType getInstance() {
        return (PascalModuleType) ModuleTypeManager.getInstance().findByID(JpsPascalModuleType.MODULE_TYPE_ID);
    }

    public static boolean isPascalModule(Module module) {
        return module != null && JpsPascalModuleType.MODULE_TYPE_ID.equals(ModuleType.get(module).getId());
    }

    @NotNull
    @Override
    public PascalModuleBuilder createModuleBuilder() {
        return new PascalModuleBuilder();
    }

    @NotNull
    @Override
    public String getName() {
        return "Pascal Module";
    }

    @NotNull
    @Override
    public String getDescription() {
        return getName();
    }

    public Icon getBigIcon() {
        return PascalIcons.GENERAL;
    }

    @Override
    public Icon getNodeIcon(@Deprecated boolean isOpened) {
        return PascalIcons.MODULE;
    }

    @Override
    public boolean isValidSdk(@NotNull Module module, @Nullable Sdk projectSdk) {
        return (projectSdk != null) && (projectSdk.getSdkType() instanceof BasePascalSdkType);
    }

    @Nullable
    public static VirtualFile getMainFile(Module module) {
        //Object obj = module.getUserData(USERDATA_KEY_MAIN_FILE);
        String fileUrl = module.getOptionValue(JpsPascalModuleType.USERDATA_KEY_MAIN_FILE.toString());
        Object obj = fileUrl != null ? VirtualFileManager.getInstance().findFileByUrl(VirtualFileManager.constructUrl("file", fileUrl)) : null;
        return obj != null ? (VirtualFile) obj : null;
    }

    public static void setMainFile(Module module, VirtualFile file) {
        //module.putUserData(USERDATA_KEY_MAIN_FILE, file);
        if (file != null) {
            module.setOption(JpsPascalModuleType.USERDATA_KEY_MAIN_FILE.toString(), file.getPath());
        }
    }

    @Nullable
    public static String getExeOutputPath(Module module) {
        return module.getOptionValue(JpsPascalModuleType.USERDATA_KEY_EXE_OUTPUT_PATH.toString());
    }

    public static void setExeOutputPath(Module module, String path) {
        module.setOption(JpsPascalModuleType.USERDATA_KEY_EXE_OUTPUT_PATH.toString(), path);
    }

}
