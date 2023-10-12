package com.siberika.idea.pascal.jps.model;

import com.siberika.idea.pascal.jps.util.ParamMap;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.jps.model.JpsElementFactory;
import org.jetbrains.jps.model.JpsElementTypeWithDefaultProperties;
import org.jetbrains.jps.model.JpsSimpleElement;
import org.jetbrains.jps.model.library.sdk.JpsSdkType;

public class JpsPascalSdkType extends JpsSdkType<JpsSimpleElement<ParamMap>> implements JpsElementTypeWithDefaultProperties<JpsSimpleElement<ParamMap>> {
    public static final JpsPascalSdkType INSTANCE = new JpsPascalSdkType();

    @NotNull
    @Override
    public JpsSimpleElement<ParamMap> createDefaultProperties() {
        return JpsElementFactory.getInstance().createSimpleElement(new ParamMap());
    }
}