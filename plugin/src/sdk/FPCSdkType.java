package com.siberika.idea.pascal.sdk;

import com.intellij.execution.ExecutionException;
import com.intellij.execution.process.ProcessOutput;
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

/**
 * Author: George Bakhtadze
 * Date: 10/01/2013
 */
public class FPCSdkType extends BasePascalSdkType {

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
        DefinesParser.parse(getClass().getResource("/defines.xml"));
        BuiltinsParser.parse(getClass().getResource("/builtins.xml"));
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
        File binDir = new File(sdkHome, "bin");
        for (File targetDir : FileUtil.listDirs(binDir)) {
            File executable = getExecutable(targetDir.getAbsolutePath(), "fpc");
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
    public String getVersionString(String sdkHome){
        return getExecutableVersionOutput(sdkHome);
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

    @Nullable
    private static String getExecutableVersionOutput(String sdkHome) {
        final String exePath = getCompilerExecutable(sdkHome).getAbsolutePath();
        final ProcessOutput processOutput;
        try {
            processOutput = SysUtils.getProcessOutput(sdkHome, exePath, "-iV");
        } catch (final ExecutionException e) {
            return null;
        }
        if (processOutput.getExitCode() != 0) {
            return null;
        }
        final String stdout = processOutput.getStdout().trim();
        if (stdout.isEmpty()) return null;

        return stdout;
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
