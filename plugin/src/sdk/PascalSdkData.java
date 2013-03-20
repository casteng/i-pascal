package com.siberika.idea.pascal.sdk;

import com.intellij.openapi.projectRoots.SdkAdditionalData;

import java.util.HashMap;
import java.util.Map;

/**
 * Author: George Bakhtadze
 * Date: 18/01/2013
 */
public class PascalSdkData implements SdkAdditionalData {

    private final Map<String, String> data;

    public PascalSdkData() {
        this.data = new HashMap<String, String>();
    }

    public PascalSdkData(Map<String, String> data) {
        this.data = data;
    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        return new PascalSdkData(data);
    };

    public String getValue(final String key) {
        return data.get(key);
    }

    public void setValue(final String key, final String value) {
        data.put(key, value);
    }
}
