package com.siberika.idea.pascal.lang.stub;

import com.intellij.psi.stubs.StubBase;
import com.intellij.psi.stubs.StubElement;
import com.siberika.idea.pascal.lang.psi.PascalModule;
import com.siberika.idea.pascal.lang.psi.impl.PasField;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

/**
 * Author: George Bakhtadze
 * Date: 13/10/2015
 */
public class PasModuleStubImpl extends StubBase<PascalModule> implements PasModuleStub {

    private String name;
    private String uniqueName;
    private PascalModule.ModuleType moduleType;
    private Set<String> usedUnitsPublic;
    private Set<String> usedUnitsPrivate;

    public PasModuleStubImpl(StubElement parent, String name, PascalModule.ModuleType moduleType,
                             Set<String> usedUnitsPublic, Set<String> usedUnitsPrivate) {
        super(parent, PasModuleStubElementType.INSTANCE);
        this.name = name;
        this.uniqueName = (parent instanceof PasNamedStub ? ((PasNamedStub) parent).getUniqueName() + "." : "") + name;
        this.moduleType = moduleType;
        this.usedUnitsPublic = usedUnitsPublic;
        this.usedUnitsPrivate = usedUnitsPrivate;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public PasField.FieldType getType() {
        return PasField.FieldType.UNIT;
    }

    @Override
    public String getUniqueName() {
        return uniqueName;
    }

    @Override
    public PascalModule.ModuleType getModuleType() {
        return moduleType;
    }

    @NotNull
    @Override
    public Set<String> getUsedUnitsPublic() {
        return usedUnitsPublic;
    }

    @NotNull
    @Override
    public Set<String> getUsedUnitsPrivate() {
        return usedUnitsPrivate;
    }
}
