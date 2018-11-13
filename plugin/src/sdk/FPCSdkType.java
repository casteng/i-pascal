package com.siberika.idea.pascal.sdk;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.projectRoots.AdditionalDataConfigurable;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.projectRoots.SdkModel;
import com.intellij.openapi.projectRoots.SdkModificator;
import com.intellij.openapi.projectRoots.SdkType;
import com.intellij.openapi.roots.OrderRootType;
import com.intellij.openapi.util.SystemInfo;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.siberika.idea.pascal.PascalException;
import com.siberika.idea.pascal.PascalIcons;
import com.siberika.idea.pascal.jps.model.JpsPascalModelSerializerExtension;
import com.siberika.idea.pascal.jps.sdk.PascalCompilerFamily;
import com.siberika.idea.pascal.jps.sdk.PascalSdkData;
import com.siberika.idea.pascal.jps.sdk.PascalSdkUtil;
import com.siberika.idea.pascal.util.SysUtils;
import org.apache.commons.lang.SystemUtils;
import org.apache.commons.lang.text.StrBuilder;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.io.File;
import java.util.Arrays;
import java.util.List;

/**
 * Author: George Bakhtadze
 * Date: 10/01/2013
 */
public class FPCSdkType extends BasePascalSdkType {

    private static final Logger LOG = Logger.getInstance(FPCSdkType.class.getName());
    private static final String[] LIBRARY_DIRS = {"rtl", "rtl-objpas", "pthreads", "regexpr", "x11", "windows"};

    @NotNull
    public static FPCSdkType getInstance() {
        return SdkType.findInstance(FPCSdkType.class);
    }

    public FPCSdkType() {
        super(JpsPascalModelSerializerExtension.FPC_SDK_TYPE_ID, PascalCompilerFamily.FPC);
        loadResources("fpc");
    }

    @Nullable
    @Override
    public String suggestHomePath() {
        List<String> paths = Arrays.asList("/usr/lib/codetyphon/fpc/fpc32", "/usr/lib/codetyphon/fpc",
                "/usr/lib/fpc", "/usr/share/fpc", "/usr/local/lib/fpc");
        if (SystemInfo.isWindows) {
            paths = Arrays.asList("c:\\codetyphon\\fpc\\fpc32", "c:\\codetyphon\\fpc", "c:\\fpc");
        }
        for (String path : paths) {
            if (new File(path).isDirectory()) {
                return path;
            }
        }

        return null;
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

    @Override
    public boolean isValidSdkHome(@NotNull final String path) {
        LOG.info("Checking SDK path: " + path);
        final File fpcExe = PascalSdkUtil.getFPCExecutable(path);
        return fpcExe.isFile() && fpcExe.canExecute();
    }

    @NotNull
    public String suggestSdkName(@Nullable final String currentSdkName, @NotNull final String sdkHome) {
        String version = getVersionString(sdkHome);
        if (version == null) return "Free Pascal v. ?? at " + sdkHome;
        return "Free Pascal v. " + version + " | " + getTargetString(sdkHome);
    }

    @Nullable
    public String getVersionString(String sdkHome) {
        LOG.info("Getting version for SDK path: " + sdkHome);
        try {
            return SysUtils.runAndGetStdOut(sdkHome, PascalSdkUtil.getFPCExecutable(sdkHome).getAbsolutePath(), PascalSdkUtil.FPC_PARAMS_VERSION_GET);
        } catch (PascalException e) {
            LOG.info("Error: " + e.getMessage(), e);
        } catch (RuntimeException e) {
            LOG.info("Error: " + e.getMessage(), e);
        }
        return null;
    }

    @Nullable
    private static String getTargetString(String sdkHome) {
        LOG.info("Getting target for SDK path: " + sdkHome);
        try {
            return SysUtils.runAndGetStdOut(sdkHome, PascalSdkUtil.getFPCExecutable(sdkHome).getAbsolutePath(), PascalSdkUtil.FPC_PARAMS_TARGET_GET);
        } catch (PascalException e) {
            LOG.info("Error: " + e.getMessage(), e);
        }
        return null;
    }

    @NotNull
    @NonNls
    @Override
    public String getPresentableName() {
        return "Free Pascal SDK";
    }

    @Override
    public void setupSdkPaths(@NotNull final Sdk sdk) {
        String target = getTargetString(sdk.getHomePath());
        configureSdkPaths(sdk, target);
        configureOptions(sdk, getAdditionalData(sdk), target);
    }

    @Override
    protected void configureOptions(@NotNull Sdk sdk, PascalSdkData data, String target) {
        super.configureOptions(sdk, data, target);
        File file = PascalSdkUtil.getPPUDumpExecutable(sdk.getHomePath() != null ? sdk.getHomePath() : "");
        data.setValue(PascalSdkData.Keys.DECOMPILER_COMMAND.getKey(), file.getAbsolutePath());
        StrBuilder sb = new StrBuilder("-Mdelphi ");
        if (SystemUtils.IS_OS_WINDOWS) {
            sb.append("-dMSWINDOWS ");
        } else {
            sb.append("-dPOSIX ");
            if (SystemUtils.IS_OS_MAC_OSX) {
                sb.append("-dMACOS ");
            } else {
                sb.append("-dLINUX ");
            }
        }
        if (target.contains("_64")) {
            sb.append("-dCPUX64 ");
        } else {
            sb.append("-dCPUX86 ");
        }
        data.setValue(PascalSdkData.Keys.COMPILER_OPTIONS.getKey(), sb.toString());
        data.setValue(PascalSdkData.Keys.COMPILER_OPTIONS_DEBUG.getKey(), "-Ddebug -glh -CroiO -godwarfsets");
    }

    private static void configureSdkPaths(@NotNull final Sdk sdk, String target) {
        LOG.info("Setting up SDK paths for SDK at " + sdk.getHomePath());
        final SdkModificator[] sdkModificatorHolder = new SdkModificator[]{null};
        final SdkModificator sdkModificator = sdk.getSdkModificator();
        if (target != null) {
            target = target.replace(' ', '-');
            for (String dir : LIBRARY_DIRS) {
                VirtualFile vdir = getLibrary(sdk, target, dir);
                if (vdir != null) {
                    sdkModificator.addRoot(vdir, OrderRootType.CLASSES);
                }
            }
            sdkModificatorHolder[0] = sdkModificator;
            sdkModificatorHolder[0].commitChanges();
        }
    }

    private static VirtualFile getLibrary(Sdk sdk, String target, String name) {
        File rtlDir = new File(sdk.getHomePath() + File.separatorChar + "units" + File.separatorChar + target + File.separatorChar + name);
        if (!rtlDir.exists()) {
            rtlDir = new File(sdk.getHomePath() + File.separatorChar + sdk.getVersionString() + File.separatorChar + "units" + File.separatorChar + target + File.separatorChar + name);
        }
        return LocalFileSystem.getInstance().findFileByIoFile(rtlDir);
    }

    @Override
    public boolean isRootTypeApplicable(@NotNull OrderRootType type) {
        return type.equals(OrderRootType.SOURCES) || type.equals(OrderRootType.CLASSES);
    }

    @Nullable
    @Override
    public AdditionalDataConfigurable createAdditionalDataConfigurable(@NotNull final SdkModel sdkModel, @NotNull final SdkModificator sdkModificator) {
        return new PascalSdkConfigUI();
    }

}
