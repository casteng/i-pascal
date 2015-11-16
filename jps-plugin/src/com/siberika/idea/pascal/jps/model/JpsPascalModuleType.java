package com.siberika.idea.pascal.jps.model;

import com.intellij.openapi.util.Key;
import com.siberika.idea.pascal.jps.util.ParamMap;
import org.jetbrains.jps.model.JpsSimpleElement;
import org.jetbrains.jps.model.ex.JpsElementTypeBase;
import org.jetbrains.jps.model.module.JpsModuleType;

public class JpsPascalModuleType extends JpsElementTypeBase<JpsSimpleElement<ParamMap>> implements JpsModuleType<JpsSimpleElement<ParamMap>> {
    public static final JpsPascalModuleType INSTANCE = new JpsPascalModuleType();

    public static final String MODULE_TYPE_ID = "PASCAL_MODULE";
    public static final Key<Object> USERDATA_KEY_MAIN_FILE = new Key<Object>("mainFile");
    public static final Key<Object> USERDATA_KEY_EXE_OUTPUT_PATH = new Key<Object>("exeOutputPath");

    private JpsPascalModuleType() {
    }
}