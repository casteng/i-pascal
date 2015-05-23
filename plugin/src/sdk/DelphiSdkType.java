package com.siberika.idea.pascal.sdk;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.projectRoots.AdditionalDataConfigurable;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.projectRoots.SdkAdditionalData;
import com.intellij.openapi.projectRoots.SdkModel;
import com.intellij.openapi.projectRoots.SdkModificator;
import com.intellij.openapi.projectRoots.SdkType;
import com.intellij.openapi.roots.OrderRootType;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.siberika.idea.pascal.PascalException;
import com.siberika.idea.pascal.PascalIcons;
import com.siberika.idea.pascal.jps.model.JpsPascalModelSerializerExtension;
import com.siberika.idea.pascal.jps.sdk.PascalCompilerFamily;
import com.siberika.idea.pascal.jps.sdk.PascalSdkData;
import com.siberika.idea.pascal.jps.sdk.PascalSdkUtil;
import com.siberika.idea.pascal.util.SysUtils;
import org.apache.commons.lang.text.StrBuilder;
import org.jdom.Element;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.io.File;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Author: George Bakhtadze
 * Date: 10/01/2013
 */
public class DelphiSdkType extends BasePascalSdkType {

    public static final Logger LOG = Logger.getInstance(DelphiSdkType.class.getName());
    private static final String[] LIBRARY_DIRS = {"debug"};
    private static final Pattern DELPHI_VERSION_PATTERN = Pattern.compile("[\\w\\s]+[vV]ersion\\s(\\d+\\.\\d+)");

    @NotNull
    public static DelphiSdkType getInstance() {
        return SdkType.findInstance(DelphiSdkType.class);
    }

    public DelphiSdkType() {
        super(JpsPascalModelSerializerExtension.DELPHI_SDK_TYPE_ID);
        InputStream definesStream = getClass().getClassLoader().getResourceAsStream("/defines.xml");
        if (definesStream != null) {
            DefinesParser.parse(definesStream);
        }
    }

    @Nullable
    @Override
    public String suggestHomePath() {
        List<String> dirs = Arrays.asList("", "program files");
        for (File drive : File.listRoots()) {
            if (drive.isDirectory()) {
                for (String dir : dirs) {
                    String s = checkDir(new File(drive, dir));
                    if (s != null) return s;
                }
            }
        }
        return null;
    }

    private String checkDir(@NotNull File file) {
        List<String> ides = Arrays.asList("delphi", "rad studio");
        for (String ide : ides) {
            File f = new File(file, ide);
            LOG.info("=== checking directory " + f.getAbsolutePath());
            if (f.isDirectory()) {
                return f.getAbsolutePath();
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
        final File dcc32Exe = PascalSdkUtil.getDCC32Executable(path);
        return dcc32Exe.isFile() && dcc32Exe.canExecute();
    }

    @NotNull
    public String suggestSdkName(@Nullable final String currentSdkName, @NotNull final String sdkHome) {
        String version = getVersionString(sdkHome);
        if (version == null) return "Delphi v. ?? at " + sdkHome;
        return "Delphi v. " + version + " | " + getTargetString(sdkHome);
    }

    @Nullable
    public String getVersionString(String sdkHome) {
        LOG.info("Getting version for SDK path: " + sdkHome);
        try {
            String out = SysUtils.runAndGetStdOut(sdkHome, PascalSdkUtil.getDCC32Executable(sdkHome).getAbsolutePath(), PascalSdkUtil.DELPHI_PARAMS_VERSION_GET);
            String[] lines = out != null ? out.split("\n", 3) : null;
            if ((lines != null) && (lines.length > 1)) {
                LOG.info("=== lines: " + lines.length + ", " + lines[1]);
                Matcher m = DELPHI_VERSION_PATTERN.matcher(lines[1]);
                if (m.matches()) {
                    return m.group(1);
                }
            } else {
                LOG.info("=== wrong lines: " + (lines != null ? lines.length : "null"));
            }
        } catch (PascalException e) {
            LOG.info("Error: " + e.getMessage(), e);
        }
        return null;
    }

    @Nullable
    public static String getTargetString(String sdkHome) {
        LOG.info("Getting target for SDK path: " + sdkHome);
        return "Win32";
    }

    @Override
    public void saveAdditionalData(@NotNull final SdkAdditionalData additionalData, @NotNull final Element additional) {
        if (additionalData instanceof PascalSdkData) {
            Object val = ((PascalSdkData) additionalData).getValue(PascalSdkData.DATA_KEY_COMPILER_OPTIONS);
            additional.setAttribute(PascalSdkData.DATA_KEY_COMPILER_OPTIONS, val != null ? (String) val : "");
            additional.setAttribute(PascalSdkData.DATA_KEY_COMPILER_FAMILY, PascalCompilerFamily.DELPHI.toString());
        }
    }

    @Nullable
    @Override
    public SdkAdditionalData loadAdditionalData(Element additional) {
        PascalSdkData result = new PascalSdkData();
        if (additional != null) {
            result.setValue(PascalSdkData.DATA_KEY_COMPILER_OPTIONS, additional.getAttributeValue(PascalSdkData.DATA_KEY_COMPILER_OPTIONS));
            result.setValue(PascalSdkData.DATA_KEY_COMPILER_FAMILY, PascalCompilerFamily.DELPHI.toString());
        }
        return result;
    }

    @NonNls
    @Override
    public String getPresentableName() {
        return "Delphi SDK";
    }

    @Override
    public void setupSdkPaths(@NotNull final Sdk sdk) {
        configureSdkPaths(sdk);
        configureOptions(sdk, getAdditionalData(sdk), "");
    }

    @Override
    protected void configureOptions(@NotNull Sdk sdk, PascalSdkData data, String target) {
        super.configureOptions(sdk, data, target);
        StrBuilder sb = new StrBuilder();
        sb.append("-dWINDOWS ");
        sb.append("-dWIN32 ");
        data.setValue(PascalSdkData.DATA_KEY_COMPILER_OPTIONS, sb.toString());
    }

    private static void configureSdkPaths(@NotNull final Sdk sdk) {
        LOG.info("Setting up SDK paths for SDK at " + sdk.getHomePath());
        final SdkModificator[] sdkModificatorHolder = new SdkModificator[]{null};
        final SdkModificator sdkModificator = sdk.getSdkModificator();
        for (String dir : LIBRARY_DIRS) {
            VirtualFile vdir = getLibrary(sdk, dir);
            if (vdir != null) {
                sdkModificator.addRoot(vdir, OrderRootType.CLASSES);
            }
        }
        sdkModificatorHolder[0] = sdkModificator;
        sdkModificatorHolder[0].commitChanges();
    }

    private static VirtualFile getLibrary(Sdk sdk, String name) {
        String target = "win32";
        File rtlDir = new File(sdk.getHomePath() + File.separatorChar + "lib" + File.separatorChar + target + File.separatorChar + name);
        if (!rtlDir.exists()) {
            rtlDir = new File(sdk.getHomePath() + File.separatorChar + "lib" + File.separatorChar + name);
        }
        return LocalFileSystem.getInstance().findFileByIoFile(rtlDir);
    }

    @Override
    public boolean isRootTypeApplicable(OrderRootType type) {
        return type.equals(OrderRootType.SOURCES) || type.equals(OrderRootType.CLASSES);
    }

    @Nullable
    @Override
    public AdditionalDataConfigurable createAdditionalDataConfigurable(@NotNull final SdkModel sdkModel, @NotNull final SdkModificator sdkModificator) {
        return new PascalSdkConfigUI();
    }

}
