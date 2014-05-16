package com.siberika.idea.pascal.jps.util;

import com.intellij.openapi.diagnostic.Logger;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.jps.model.JpsElement;
import org.jetbrains.jps.model.JpsSimpleElement;

import java.util.HashMap;

/**
 * Author: George Bakhtadze
 * Date: 11/05/2014
 */
public class ParamMap extends HashMap<String, String> {
    private static final Logger LOG = Logger.getInstance(ParamMap.class.getName());
    public ParamMap() {
    }

    public ParamMap(ParamMap msg) {
        this.putAll(msg);
    }

    public ParamMap addPair(String key, String value) {
        this.put(key, value);
        return this;
    }

    public static void addJpsParam(JpsElement jpsElement, String name, String value) {
        Object data = null;
        if (jpsElement instanceof JpsSimpleElement) {
            data = ((JpsSimpleElement) jpsElement).getData();
        }
        if (data instanceof ParamMap) {
            ((ParamMap) data).addPair(name, value);
        } else {
            LOG.warn("Data is not ParamMap: " + data);
        }
    }

    public static String getJpsParam(JpsElement jpsElement, String name) {
        Object data = null;
        if (jpsElement instanceof JpsSimpleElement) {
            data = ((JpsSimpleElement) jpsElement).getData();
        }
        if (data instanceof ParamMap) {
            return ((ParamMap) data).get(name);
        } else {
            LOG.warn("Data is not ParamMap: " + data);
            return null;
        }
    }

    @Nullable
    public static ParamMap getJpsParams(JpsElement jpsElement) {
        if (jpsElement instanceof JpsSimpleElement) {
            Object data = ((JpsSimpleElement) jpsElement).getData();
            if (data instanceof ParamMap) {
                return (ParamMap) data;
            } else {
                LOG.warn("Data is not ParamMap: " + data);
                return null;
            }
        }
        return null;
    }
}

