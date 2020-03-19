package com.siberika.idea.pascal;

import com.intellij.openapi.module.Module;
import com.intellij.testFramework.fixtures.LightPlatformCodeInsightFixtureTestCase;
import org.jetbrains.annotations.NotNull;

public class PascalLightCodeInsightFixtureTestCase
        extends LightPlatformCodeInsightFixtureTestCase {
    @NotNull
    public Module getModule() {
        return myFixture.getModule();
    }
}
