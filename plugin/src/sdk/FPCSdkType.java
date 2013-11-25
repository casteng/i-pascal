package com.siberika.idea.pascal.sdk;

import com.intellij.openapi.module.Module;
import com.intellij.openapi.projectRoots.AdditionalDataConfigurable;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.projectRoots.SdkAdditionalData;
import com.intellij.openapi.projectRoots.SdkModel;
import com.intellij.openapi.projectRoots.SdkModificator;
import com.intellij.openapi.projectRoots.SdkType;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.roots.OrderRootType;
import com.intellij.openapi.util.SystemInfo;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.siberika.idea.pascal.PascalIcons;
import com.siberika.idea.pascal.util.FileUtil;
import com.siberika.idea.pascal.util.SysUtils;
import org.jdom.Element;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.io.File;
import java.io.InputStream;

/**
 * Author: George Bakhtadze
 * Date: 10/01/2013
 */
public class FPCSdkType extends BasePascalSdkType {

    public static final String FPC_PARAMS_VERSION_GET = "-iV";
    private static String target;

    @NotNull
    public static FPCSdkType getInstance() {
        return SdkType.findInstance(FPCSdkType.class);
    }

    public static Sdk findSdk(Module module) {
        if (module == null) {
            return null;
        }

        Sdk sdk = ModuleRootManager.getInstance(module).getSdk();
        if (sdk != null && (sdk.getSdkType().equals(FPCSdkType.getInstance()))) {
            return sdk;
        }

        return null;
    }

    public FPCSdkType() {
        super(FPCSdkType.class.getSimpleName());
        InputStream definesStream = getClass().getClassLoader().getResourceAsStream("/defines.xml");
        if (definesStream != null) {
            DefinesParser.parse(definesStream);
        }
        InputStream builtinsUrl = getClass().getClassLoader().getResourceAsStream("/builtins.xml");
        if (builtinsUrl != null) {
            BuiltinsParser.parse(builtinsUrl);
        }
    }

    @NotNull
    @Override
    public Icon getIcon() {
        return PascalIcons.GENERAL;
    }

    @NotNull
    @Override
    public Icon getIconForAddAction() {
        return getIcon();
    }

    @Nullable
    @Override
    public String suggestHomePath() {
        if (SystemInfo.isWindows) {
            return "C:\\fpc\\bin";
        } else if (SystemInfo.isLinux) {
            if (FileUtil.exists("/usr/share/fpc")) {
                return "/usr/share/fpc";
            } else {
                return "/usr/lib/codetyphon/fpc";
            }
        }
        return null;
    }

    @Override
    public boolean isValidSdkHome(@NotNull final String path) {
        final File fpcExe = getCompilerExecutable(path);
        return fpcExe.isFile() && fpcExe.canExecute();
    }

    @NotNull
    public static File getCompilerExecutable(@NotNull final String sdkHome) {
        return getUtilExecutable(sdkHome, "bin", "fpc");
    }

    @NotNull
    public static File getPPUDumpExecutable(@NotNull final String sdkHome) {
        return getUtilExecutable(sdkHome, "bin", "ppudump");
    }

    //TODO: take target directory from compiler target
    @NotNull
    static File getUtilExecutable(@NotNull final String sdkHome, @NotNull final String dir, @NotNull final String exe) {
        File binDir = new File(sdkHome, dir);
        for (File targetDir : FileUtil.listDirs(binDir)) {
            File executable = getExecutable(targetDir.getAbsolutePath(), exe);
            if (executable.canExecute()) {
                target = targetDir.getName();
                return executable;
            }
        }
        return binDir;
    }

    @NotNull
    public static File getByteCodeCompilerExecutable(@NotNull final String sdkHome) {
        return getExecutable(new File(sdkHome, "bin").getAbsolutePath(), "ppcjvm");
    }

    @NotNull
    public String suggestSdkName(@Nullable final String currentSdkName, @NotNull final String sdkHome) {
        String version = getVersionString(sdkHome);
        if (version == null) return "Free Pascal v. ?? at " + sdkHome;
        return "Free Pascal v. " + version;
    }

    @Nullable
    public String getVersionString(String sdkHome) {
        return SysUtils.runAndGetStdOut(sdkHome, getCompilerExecutable(sdkHome).getAbsolutePath(), FPC_PARAMS_VERSION_GET);
    }

    @NotNull
    private static File getExecutable(@NotNull final String path, @NotNull final String command) {
        return new File(path, SystemInfo.isWindows ? command + ".exe" : command);
    }

    @Override
    public void saveAdditionalData(@NotNull final SdkAdditionalData additionalData, @NotNull final Element additional) {
    }

    @NonNls
    @Override
    public String getPresentableName() {
        return "Free Pascal SDK";
    }

    @Override
    public void setupSdkPaths(@NotNull final Sdk sdk) {
        configureSdkPaths(sdk);
    }

    private static void configureSdkPaths(@NotNull final Sdk sdk) {
        final SdkModificator[] sdkModificatorHolder = new SdkModificator[]{null};
        final SdkModificator sdkModificator = sdk.getSdkModificator();
        File rtlDir = new File(sdk.getHomePath() + File.separatorChar + "units" + File.separatorChar + target + File.separatorChar + "rtl");
        final VirtualFile dir = LocalFileSystem.getInstance().findFileByIoFile(rtlDir);
        sdkModificator.addRoot(dir, OrderRootType.SOURCES);

        sdkModificatorHolder[0] = sdkModificator;

        if (sdkModificatorHolder[0] != null) {
            sdkModificatorHolder[0].commitChanges();
        }
    }

    @Override
    public boolean isRootTypeApplicable(OrderRootType type) {
        return type.equals(OrderRootType.SOURCES);
    }

    @Nullable
    @Override
    public AdditionalDataConfigurable createAdditionalDataConfigurable(@NotNull final SdkModel sdkModel, @NotNull final SdkModificator sdkModificator) {
        return new PascalSdkConfigUI();
    }

}
