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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class JpsPascalModelSerializerExtension extends JpsModelSerializerExtension {
    public static final String FPC_SDK_TYPE_ID = "FPCSdkType";
    public static final String DELPHI_SDK_TYPE_ID = "DelphiSdkType";

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
        List<JpsSdkPropertiesSerializer<JpsSimpleElement<ParamMap>>> result = new ArrayList<JpsSdkPropertiesSerializer<JpsSimpleElement<ParamMap>>>(2);
        result.add(new JpsPascalSdkPropertiesSerializer<JpsSimpleElement<ParamMap>>(FPC_SDK_TYPE_ID, JpsPascalSdkType.INSTANCE));
        result.add(new JpsPascalSdkPropertiesSerializer<JpsSimpleElement<ParamMap>>(DELPHI_SDK_TYPE_ID, JpsPascalSdkType.INSTANCE));
        return result;
    }

    @Override
    public void loadModuleOptions(@NotNull JpsModule module, @NotNull Element rootElement) {
        super.loadModuleOptions(module, rootElement);
        for (Attribute attribute : rootElement.getAttributes()) {
            ParamMap.addJpsParam(module.getProperties(), attribute.getName(), attribute.getValue());
        }
    }

    private static class JpsPascalSdkPropertiesSerializer<T> extends JpsSdkPropertiesSerializer<JpsSimpleElement<ParamMap>> {
        public JpsPascalSdkPropertiesSerializer(String typeId, JpsPascalSdkType type) {
            super(typeId, type);
        }

        @NotNull
        @Override
        public JpsSimpleElement<ParamMap> loadProperties(@Nullable Element propertiesElement) {
            return JpsElementFactory.getInstance().createSimpleElement(new ParamMap()
                    .addPair(PascalSdkData.Keys.COMPILER_COMMAND.getKey(),
                            propertiesElement != null ? propertiesElement.getAttributeValue(PascalSdkData.Keys.COMPILER_COMMAND.getKey()) : "")
                    .addPair(PascalSdkData.Keys.COMPILER_NAMESPACES.getKey(),
                            propertiesElement != null ? propertiesElement.getAttributeValue(PascalSdkData.Keys.COMPILER_NAMESPACES.getKey()) : "")
                    .addPair(PascalSdkData.Keys.COMPILER_OPTIONS.getKey(),
                            propertiesElement != null ? propertiesElement.getAttributeValue(PascalSdkData.Keys.COMPILER_OPTIONS.getKey()) : "")
                    .addPair(PascalSdkData.Keys.COMPILER_OPTIONS_DEBUG.getKey(),
                            propertiesElement != null ? propertiesElement.getAttributeValue(PascalSdkData.Keys.COMPILER_OPTIONS_DEBUG.getKey()) : "")
                    .addPair(PascalSdkData.Keys.COMPILER_FAMILY.getKey(),
                            propertiesElement != null ? propertiesElement.getAttributeValue(PascalSdkData.Keys.COMPILER_FAMILY.getKey()) : "")
            );
        }

        @Override
        public void saveProperties(@NotNull JpsSimpleElement<ParamMap> properties, @NotNull Element element) {
        }
    }
}
