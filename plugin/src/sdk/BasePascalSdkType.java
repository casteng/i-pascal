package com.siberika.idea.pascal.sdk;

import com.intellij.codeInspection.SmartHashMap;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.projectRoots.SdkAdditionalData;
import com.intellij.openapi.projectRoots.SdkModificator;
import com.intellij.openapi.projectRoots.SdkType;
import com.intellij.openapi.projectRoots.SdkTypeId;
import com.siberika.idea.pascal.jps.sdk.PascalCompilerFamily;
import com.siberika.idea.pascal.jps.sdk.PascalSdkData;
import com.siberika.idea.pascal.util.StrUtil;
import com.siberika.idea.pascal.util.SysUtils;
import org.apache.commons.lang.StringUtils;
import org.jdom.Element;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * Author: George Bakhtadze
 * Date: 10/01/2013
 */
public abstract class BasePascalSdkType extends SdkType {

    private static final Logger LOG = Logger.getInstance(BasePascalSdkType.class.getName());
    private static final String[] EMPTY_ARGS = new String[0];
    private final String compilerFamily;

    private Map<String, Map<String, Directive>> directives;
    private Map<String, Map<String, Define>> defines;

    protected BasePascalSdkType(@NonNls String name, @NonNls PascalCompilerFamily compilerFamily) {
        super(name);
        this.compilerFamily = compilerFamily.name();
    }

    public static PascalSdkData getAdditionalData(@NotNull Sdk sdk) {
        SdkAdditionalData params = sdk.getSdkAdditionalData();
        if (!(params instanceof PascalSdkData)) {
            params = new PascalSdkData();
            SdkModificator sdkModificator = sdk.getSdkModificator();
            sdkModificator.setSdkAdditionalData(params);
            sdkModificator.commitChanges();
        }
        return (PascalSdkData) params;
    }

    public static File getDecompilerCommand(@NotNull Sdk sdk, File defaultDecompilerCommand) {
        File res;
        String command = (String) getAdditionalData(sdk).getValue(PascalSdkData.keys.DECOMPILER_COMMAND.getKey());
        if (StringUtils.isEmpty(command)) {
            res = defaultDecompilerCommand;
        } else {
            res = new File(command);
        }
        if (!res.canExecute()) {
            LOG.info("ERROR: Invalid decompiler command: " + command);
        }
        return res;
    }

    public static String getDebuggerCommand(@Nullable Sdk sdk, String defaultDebuggerCommand) {
        String command = sdk != null ? (String) getAdditionalData(sdk).getValue(PascalSdkData.keys.DEBUGGER_COMMAND.getKey()) : null;
        if (StringUtils.isEmpty(command)) {
            return defaultDebuggerCommand;
        } else {
            return command;
        }
    }

    public static String[] getDecompilerArgs(Sdk sdk) {
        return EMPTY_ARGS;
        /*String command = (String) getAdditionalData(sdk).getValue(PascalSdkData.DATA_KEY_DECOMPILER_COMMAND);
        if (StringUtils.isEmpty(command) || !command.contains(" ")) {
            return EMPTY_ARGS;
        }
        return command.substring(command.indexOf(" ")+1).split(" ");*/
    }

    /**
     * Retrieves compiler defines from compiler command line parameters specified in SDK options
     * @param defines    collection of defines to append defines to
     * @param options    command line specified in SDK
     */
    public static void getDefinesFromCmdLine(Map<String, Define> defines, @Nullable String options) {
        if (null == options) {
            return;
        }
        String[] compilerOptions = options.split("\\s+");
        for (String opt : compilerOptions) {
            if (opt.startsWith("-d")) {
                defines.put(opt.substring(2).toUpperCase(), new Define(opt.substring(2), null, 0));
            }
        }
    }

    public static Map<String, Define> getDefaultDefines(@NotNull Sdk sdk, String version) {
        SdkTypeId id = sdk.getSdkType();
        if (id instanceof BasePascalSdkType) {
            SdkAdditionalData data = sdk.getSdkAdditionalData();
            Map<String, Define> result = new HashMap<String, Define>();
            if (data instanceof PascalSdkData) {
                String options = (String) ((PascalSdkData) data).getValue(PascalSdkData.keys.COMPILER_OPTIONS.getKey());
                getDefinesFromCmdLine(result, options);
            }
            Map<String, Map<String, Define>> compilerDefines = ((BasePascalSdkType) id).defines;
            for (Map.Entry<String, Map<String, Define>> entry : compilerDefines.entrySet()) {
                if (StrUtil.isVersionLessOrEqual(entry.getKey(), version)) {
                    result.putAll(entry.getValue());
                }
            }
            return result;
        }
        return new SmartHashMap<String, Define>();
    }

    public static Map<String, Directive> getDirectives(@NotNull Sdk sdk, String version) {
        SdkTypeId id = sdk.getSdkType();
        if (id instanceof BasePascalSdkType) {
            Map<String, Map<String, Directive>> directives = ((BasePascalSdkType) id).directives;
            Map<String, Directive> result = new HashMap<String, Directive>();
            for (Map.Entry<String, Map<String, Directive>> entry : directives.entrySet()) {
                if (StrUtil.isVersionLessOrEqual(entry.getKey(), version)) {
                    result.putAll(entry.getValue());
                }
            }
            return result;
        }
        return new SmartHashMap<String, Directive>();
    }

    protected void configureOptions(@NotNull Sdk sdk, PascalSdkData data, String target) {
    }

    @Override
    public void saveAdditionalData(@NotNull final SdkAdditionalData additionalData, @NotNull final Element additional) {
        if (additionalData instanceof PascalSdkData) {
            for (PascalSdkData.keys key : PascalSdkData.keys.values()) {
                Object val = ((PascalSdkData)additionalData).getValue(key.getKey());
                if (val instanceof String) {
                    additional.setAttribute(key.getKey(), val != null ? (String) val : "");
                }
            }
            additional.setAttribute(PascalSdkData.keys.COMPILER_FAMILY.getKey(), compilerFamily);
        }
    }

    @Nullable
    @Override
    public SdkAdditionalData loadAdditionalData(Element additional) {
        PascalSdkData result = new PascalSdkData();
        if (additional != null) {
            for (PascalSdkData.keys key : PascalSdkData.keys.values()) {
                result.setValue(key.getKey(), additional.getAttributeValue(key.getKey()));
            }
            result.setValue(PascalSdkData.keys.COMPILER_FAMILY.getKey(), compilerFamily);
        }
        return result;
    }

    void loadResources(String resourcePrefix) {
        InputStream definesStream = null;
        InputStream directivesStream = null;
        try {
            definesStream = getClass().getResourceAsStream("/" + resourcePrefix + "Defines.xml");
            if (definesStream != null) {
                defines = DefinesParser.parse(definesStream);
            }
            directivesStream = getClass().getResourceAsStream("/" + resourcePrefix + "Directives.xml");
            if (directivesStream != null) {
                directives = DirectivesParser.parse(directivesStream);
            }
        } finally {
            SysUtils.close(definesStream);
            SysUtils.close(directivesStream);
        }
    }

}
