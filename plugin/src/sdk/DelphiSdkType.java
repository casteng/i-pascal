package com.siberika.idea.pascal.sdk;

import com.google.common.collect.ImmutableMap;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.projectRoots.AdditionalDataConfigurable;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.projectRoots.SdkModel;
import com.intellij.openapi.projectRoots.SdkModificator;
import com.intellij.openapi.projectRoots.SdkType;
import com.intellij.openapi.roots.OrderRootType;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.siberika.idea.pascal.PascalException;
import com.siberika.idea.pascal.PascalIcons;
import com.siberika.idea.pascal.jps.compiler.DelphiBackendCompiler;
import com.siberika.idea.pascal.jps.model.JpsPascalModelSerializerExtension;
import com.siberika.idea.pascal.jps.sdk.PascalCompilerFamily;
import com.siberika.idea.pascal.jps.sdk.PascalSdkData;
import com.siberika.idea.pascal.jps.sdk.PascalSdkUtil;
import com.siberika.idea.pascal.util.SysUtils;
import org.apache.commons.lang.text.StrBuilder;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Author: George Bakhtadze
 * Date: 10/01/2013
 */
public class DelphiSdkType extends BasePascalSdkType {

    private static final Logger LOG = Logger.getInstance(DelphiSdkType.class.getName());
    private static final String[] LIBRARY_DIRS_DCU = {"debug"};
    private static final String[] LIBRARY_DIRS_SOURCE = {"common", "posix", "sys", "win"};
    private static final Pattern DELPHI_VERSION_PATTERN = Pattern.compile("[\\w\\s]+[vV]ersion\\s(\\d+\\.\\d+)");
    private static final String DELPHI_STARTER_VERSION_PATTERN = DelphiBackendCompiler.DELPHI_STARTER_RESPONSE;
    private static final Pattern RTLPKG_PATTERN = Pattern.compile("RTL(\\d{3,4}).BPL");
    private static final String[] EMPTY_STRINGS = new String[0];
    private static final Map<Integer, String> rtlPkgVersionMap = new ImmutableMap.Builder<Integer, String>().
            put(240, "31").
            put(250, "32").
            put(260, "33").
            put(270, "34").
            put(280, "35").
            put(290, "36")
            .build();

    @NotNull
    public static DelphiSdkType getInstance() {
        return SdkType.findInstance(DelphiSdkType.class);
    }

    public DelphiSdkType() {
        super(JpsPascalModelSerializerExtension.DELPHI_SDK_TYPE_ID, PascalCompilerFamily.DELPHI);
        loadResources("delphi");
    }

    @Nullable
    @Override
    public String suggestHomePath() {
        List<String> dirs = Arrays.asList("", "program files", "embarcadero", "program files/embarcadero");
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
        List<String> ides = Arrays.asList("delphi", "studio", "rad studio");
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
        final String prefix = String.format("Delphi %sv. ", isStarter(getVersionLines(sdkHome)) ? "(Starter) " : "");
        if (version != null) {
            return prefix + version + " | " + getTargetString(sdkHome);
        } else {
            return prefix + "?? at " + sdkHome;
        }
    }

    private String getVersion(String[] lines) {
        for (String line : lines) {
            Matcher m = DELPHI_VERSION_PATTERN.matcher(line);
            if (m.matches()) {
                return m.group(1);
            }
        }
        return null;
    }

    private boolean isStarter(String[] lines) {
        LOG.info("Checking for starter edition");
        for (String line : lines) {
            LOG.info("=== Line: " + line);
            if ((line != null) && line.startsWith(DELPHI_STARTER_VERSION_PATTERN)) {
                return true;
            }
        }
        return false;
    }

    @NotNull
    private String[] getVersionLines(String sdkHome) {
        try {
            String out = SysUtils.runAndGetStdOut(sdkHome, PascalSdkUtil.getDCC32Executable(sdkHome).getAbsolutePath(), PascalSdkUtil.DELPHI_PARAMS_VERSION_GET);
            return out != null ? out.split("\n", 3) : EMPTY_STRINGS;
        } catch (PascalException e) {
            LOG.info("Error: " + e.getMessage(), e);
            return EMPTY_STRINGS;
        }
    }

    @Nullable
    @Override
    public String getVersionString(String sdkHome) {
        LOG.info("Getting version for SDK path: " + sdkHome);
        String[] lines = getVersionLines(sdkHome);
        if (isStarter(lines)) {
            return getVersionByRTL(sdkHome);
        } else {
            return getVersion(lines);
        }
    }

    private String getVersionByRTL(String sdkHome) {
        File binDir = new File(sdkHome, "bin");
        File[] rtl = binDir.listFiles();
        Integer version = null;
        if (rtl != null) {
            for (File file : rtl) {
                Matcher m = RTLPKG_PATTERN.matcher(file.getName().toUpperCase());
                if (m.matches()) {
                    int v = Integer.parseInt(m.group(1));
                    if ((null == version) || (v > version)) {
                        version = v;
                    }
                }
            }
        }
        return rtlPkgVersionMap.get(version);
    }

    @NotNull
    private static String getTargetString(String sdkHome) {
        LOG.info("Getting target for SDK path: " + sdkHome);
        return "Win32";
    }

    @NotNull
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
        data.setValue(PascalSdkData.Keys.COMPILER_OPTIONS.getKey(), sb.toString());
        if (isStarter(getVersionLines(sdk.getHomePath()))) {
            data.setValue(PascalSdkData.Keys.DELPHI_IS_STARTER.getKey(), PascalSdkData.SDK_DATA_TRUE);
        }
    }

    private static void configureSdkPaths(@NotNull final Sdk sdk) {
        LOG.info("Setting up SDK paths for SDK at " + sdk.getHomePath());
        final SdkModificator[] sdkModificatorHolder = new SdkModificator[]{null};
        final SdkModificator sdkModificator = sdk.getSdkModificator();
        for (String dir : LIBRARY_DIRS_SOURCE) {
            VirtualFile vdir = getSource(sdk, dir);
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

    private static VirtualFile getSource(Sdk sdk, String name) {
        File rtlDir = new File(sdk.getHomePath() + File.separatorChar + "source" + File.separatorChar + "rtl" + File.separatorChar + name);
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
