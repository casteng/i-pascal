package com.siberika.idea.pascal.module;

import com.intellij.ide.util.projectWizard.ModuleBuilder;
import com.intellij.ide.util.projectWizard.ModuleWizardStep;
import com.intellij.ide.util.projectWizard.SdkSettingsStep;
import com.intellij.ide.util.projectWizard.SettingsStep;
import com.intellij.openapi.module.ModuleType;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.projectRoots.SdkTypeId;
import com.intellij.openapi.roots.CompilerModuleExtension;
import com.intellij.openapi.roots.ContentEntry;
import com.intellij.openapi.roots.ModifiableRootModel;
import com.intellij.openapi.util.Condition;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.siberika.idea.pascal.sdk.BasePascalSdkType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;

/**
 * Author: George Bakhtadze
 * Date: 08/01/2013
 */
public class PascalModuleBuilder extends ModuleBuilder {
    private String myCompilerOutputPath;

    private void addSourceRoot(ContentEntry contentEntry, String path) {
        path = getContentEntryPath() + File.separator + path;
        new File(path).mkdirs();
        final VirtualFile sourceRoot = LocalFileSystem.getInstance().refreshAndFindFileByPath(FileUtil.toSystemIndependentName(path));
        if (sourceRoot != null) {
            contentEntry.addSourceFolder(sourceRoot, false, "");
        }
    }

    @Override
    public void setupRootModel(ModifiableRootModel rootModel) throws ConfigurationException {
        final CompilerModuleExtension compilerModuleExtension = rootModel.getModuleExtension(CompilerModuleExtension.class);
        compilerModuleExtension.setExcludeOutput(true);
        if (myJdk != null){
            rootModel.setSdk(myJdk);
        } else {
            rootModel.inheritSdk();
        }

        ContentEntry contentEntry = doAddContentEntry(rootModel);
        if (contentEntry != null) {
            addSourceRoot(contentEntry, "");//TODO add test source root
        }

        if (myCompilerOutputPath != null) {
            // should set only absolute paths
            String canonicalPath;
            try {
                canonicalPath = FileUtil.resolveShortWindowsName(myCompilerOutputPath);
            }
            catch (IOException e) {
                canonicalPath = myCompilerOutputPath;
            }
            compilerModuleExtension.setCompilerOutputPath(VfsUtil.pathToUrl(FileUtil.toSystemIndependentName(canonicalPath)));
        }
        else {
            compilerModuleExtension.inheritCompilerOutputPath(true);
        }

    }

    @Override
    public ModuleType getModuleType() {
        return PascalModuleType.getInstance();
    }

    @Override
    public boolean isSuitableSdkType(SdkTypeId sdkType) {
        return sdkType instanceof BasePascalSdkType;
    }

    public String getMyCompilerOutputPath() {
        return myCompilerOutputPath;
    }

    public void setMyCompilerOutputPath(String myCompilerOutputPath) {
        this.myCompilerOutputPath = myCompilerOutputPath;
    }

    @Nullable
    @Override
    public ModuleWizardStep modifySettingsStep(@NotNull SettingsStep settingsStep) {
        return new SdkSettingsStep(settingsStep, this, new Condition<SdkTypeId>() {
            @Override
            public boolean value(SdkTypeId sdkType) {
                return PascalModuleBuilder.this.isSuitableSdkType(sdkType);
            }
        });
    }

}
