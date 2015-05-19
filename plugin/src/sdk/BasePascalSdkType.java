package com.siberika.idea.pascal.sdk;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.projectRoots.SdkAdditionalData;
import com.intellij.openapi.projectRoots.SdkModificator;
import com.intellij.openapi.projectRoots.SdkType;
import com.siberika.idea.pascal.jps.sdk.PascalSdkData;
import com.siberika.idea.pascal.jps.sdk.PascalSdkUtil;
import org.apache.commons.lang.StringUtils;
import org.jdom.Element;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;

/**
 * Author: George Bakhtadze
 * Date: 10/01/2013
 */
public abstract class BasePascalSdkType extends SdkType {

    public static final Logger LOG = Logger.getInstance(BasePascalSdkType.class.getName());

    public BasePascalSdkType(@NonNls String name) {
        super(name);
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

    public static File getDecompilerCommand(@NotNull Sdk sdk) {
        String command = (String) getAdditionalData(sdk).getValue(PascalSdkData.DATA_KEY_DECOMPILER_COMMAND);
        File res;
        if (StringUtils.isEmpty(command)) {
            res = PascalSdkUtil.getPPUDumpExecutable(sdk.getHomePath() != null ? sdk.getHomePath() : "");
        } else {
            res = new File(command);
        }
        if (!res.canExecute()) {
            LOG.info("ERROR: Invalid decompiler command: " + command);
        }
        return res;
    }

    protected void configureOptions(@NotNull Sdk sdk, PascalSdkData data, String target) {
        File file = PascalSdkUtil.getPPUDumpExecutable(sdk.getHomePath() != null ? sdk.getHomePath() : "");
        data.setValue(PascalSdkData.DATA_KEY_DECOMPILER_COMMAND, file.getAbsolutePath());
    }

    protected void setParam(PascalSdkData additionalData, Element additional, String name) {
        Object val = additionalData.getValue(name);
        additional.setAttribute(name, val != null ? (String) val : "");
    }

    @Override
    public void saveAdditionalData(@NotNull final SdkAdditionalData additionalData, @NotNull final Element additional) {
        if (additionalData instanceof PascalSdkData) {
            setParam((PascalSdkData) additionalData, additional, PascalSdkData.DATA_KEY_COMPILER_OPTIONS);
            setParam((PascalSdkData) additionalData, additional, PascalSdkData.DATA_KEY_DECOMPILER_COMMAND);
            setParam((PascalSdkData) additionalData, additional, PascalSdkData.DATA_KEY_COMPILER_FAMILY);
        }
    }

    @Nullable
    @Override
    public SdkAdditionalData loadAdditionalData(Element additional) {
        PascalSdkData result = new PascalSdkData();
        if (additional != null) {
            result.setValue(PascalSdkData.DATA_KEY_COMPILER_OPTIONS, additional.getAttributeValue(PascalSdkData.DATA_KEY_COMPILER_OPTIONS));
            result.setValue(PascalSdkData.DATA_KEY_DECOMPILER_COMMAND, additional.getAttributeValue(PascalSdkData.DATA_KEY_DECOMPILER_COMMAND));
            result.setValue(PascalSdkData.DATA_KEY_COMPILER_FAMILY, additional.getAttributeValue(PascalSdkData.DATA_KEY_COMPILER_FAMILY));
        }
        return result;
    }

}
