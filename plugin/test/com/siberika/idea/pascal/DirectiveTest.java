package com.siberika.idea.pascal;

import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.testFramework.LightProjectDescriptor;
import com.intellij.testFramework.fixtures.LightPlatformCodeInsightFixtureTestCase;
import com.siberika.idea.pascal.sdk.FPCSdkType;
import org.jetbrains.annotations.Nullable;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class DirectiveTest extends LightPlatformCodeInsightFixtureTestCase {
    @Override
    protected String getTestDataPath() {
        return "testData/directive";
    }

    @Override
    protected LightProjectDescriptor getProjectDescriptor() {
        return new PascalProjectDescriptor();
    }

    public void testDirective() {
        myFixture.configureByFiles("compilerDirective.pas");
        CompletionTest.checkCompletionContains(myFixture,
                "$A ", "$A8", "$DEFINE", "$ELSE", "$IFDEF ", "$IFOPT ", "$INCLUDE ", "$INLINE ", "$Q+", "$R-", "$WARN ");
    }

    public void testDefine() {
        myFixture.configureByFiles("define.pas");
        CompletionTest.checkCompletionContains(myFixture, "test1", "test2", "test3");
    }

    private static Sdk mockPascalSdk = createSdk();

    private static Sdk createSdk() {
        Sdk sdk = mock(Sdk.class);
        FPCSdkType sdkType = new FPCSdkType();
        when(sdk.getSdkType()).thenReturn(sdkType);
        when(sdk.getVersionString()).thenReturn("2.2.0");
        return sdk;
    }

    private static class PascalProjectDescriptor extends LightProjectDescriptor {
        @Nullable
        @Override
        public Sdk getSdk() {
            return mockPascalSdk;
        }
    }

}
