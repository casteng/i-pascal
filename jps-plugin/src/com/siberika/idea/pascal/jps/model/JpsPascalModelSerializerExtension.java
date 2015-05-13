package com.siberika.idea.pascal.jps.model;

import com.siberika.idea.pascal.jps.sdk.PascalSdkData;
import com.siberika.idea.pascal.jps.util.ParamMap;
import org.jdom.Attribute;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.jps.model.JpsElementFactory;
import org.jetbrains.jps.model.JpsSimpleElement;
import org.jetbrains.jps.model.module.JpsModule;
import org.jetbrains.jps.model.serialization.JpsModelSerializerExtension;
import org.jetbrains.jps.model.serialization.library.JpsSdkPropertiesSerializer;
import org.jetbrains.jps.model.serialization.module.JpsModulePropertiesSerializer;

import java.util.Collections;
import java.util.List;

public class JpsPascalModelSerializerExtension extends JpsModelSerializerExtension {
    public static final String PASCAL_SDK_TYPE_ID = "PascalSdkType";

    @NotNull
    @Override
    public List<? extends JpsModulePropertiesSerializer<?>> getModulePropertiesSerializers() {
        return Collections.singletonList(new JpsModulePropertiesSerializer<JpsSimpleElement<ParamMap>>(JpsPascalModuleType.INSTANCE, "PASCAL_MODULE", null) {
            @Override
            public JpsSimpleElement<ParamMap> loadProperties(@Nullable Element componentElement) {
                return JpsElementFactory.getInstance().createSimpleElement(new ParamMap());
            }

            @Override
            public void saveProperties(@NotNull JpsSimpleElement<ParamMap> properties, @NotNull Element componentElement) {
            }
        });
    }

    @NotNull
    @Override
    public List<? extends JpsSdkPropertiesSerializer<?>> getSdkPropertiesSerializers() {
        return Collections.singletonList(new JpsSdkPropertiesSerializer<JpsSimpleElement<ParamMap>>(PASCAL_SDK_TYPE_ID, JpsPascalSdkType.INSTANCE) {
            @NotNull
            @Override
            public JpsSimpleElement<ParamMap> loadProperties(@Nullable Element propertiesElement) {
                return JpsElementFactory.getInstance().createSimpleElement(new ParamMap()
                            .addPair(PascalSdkData.DATA_KEY_COMPILER_OPTIONS,
                                    propertiesElement != null ? propertiesElement.getAttributeValue(PascalSdkData.DATA_KEY_COMPILER_OPTIONS) : ""));
            }

            @Override
            public void saveProperties(@NotNull JpsSimpleElement<ParamMap> properties, @NotNull Element element) {
            }
        });
    }

    @Override
    public void loadModuleOptions(@NotNull JpsModule module, @NotNull Element rootElement) {
        super.loadModuleOptions(module, rootElement);
        for (Attribute attribute : rootElement.getAttributes()) {
            ParamMap.addJpsParam(module.getProperties(), attribute.getName(), attribute.getValue());
        }
    }
}
