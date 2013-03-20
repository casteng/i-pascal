package com.siberika.idea.pascal.sdk;

import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.projectRoots.SdkAdditionalData;
import com.intellij.openapi.projectRoots.SdkModificator;
import com.intellij.openapi.projectRoots.SdkType;
import org.jetbrains.annotations.NonNls;

/**
 * Author: George Bakhtadze
 * Date: 10/01/2013
 */
public abstract class BasePascalSdkType extends SdkType {
    public static final String DATA_KEY_COMPILER_OPTIONS = "compilerOptions";

    public BasePascalSdkType(@NonNls String name) {
        super(name);
    }

    public static PascalSdkData getAdditionalData(Sdk sdk) {
        SdkAdditionalData params = sdk.getSdkAdditionalData();
        if (!(params instanceof PascalSdkData)) {
            params = new PascalSdkData();
            SdkModificator sdkModificator = (SdkModificator) sdk.getSdkModificator();
            sdkModificator.setSdkAdditionalData(params);
            sdkModificator.commitChanges();
        }
        return (PascalSdkData) params;
    }

}
