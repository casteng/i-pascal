package com.siberika.idea.pascal.jps.model;

import org.jetbrains.jps.model.JpsDummyElement;
import org.jetbrains.jps.model.ex.JpsElementTypeWithDummyProperties;
import org.jetbrains.jps.model.module.JpsModuleType;

public class JpsPascalModuleType extends JpsElementTypeWithDummyProperties implements JpsModuleType<JpsDummyElement> {
    public static final JpsPascalModuleType INSTANCE = new JpsPascalModuleType();

    private JpsPascalModuleType() {
    }
}