package com.siberika.idea.pascal.ide.intention;

import com.intellij.codeInsight.intention.IntentionAction;
import com.intellij.testFramework.UsefulTestCase;
import com.intellij.testFramework.builders.JavaModuleFixtureBuilder;
import com.intellij.testFramework.fixtures.CodeInsightTestFixture;
import com.intellij.testFramework.fixtures.IdeaProjectTestFixture;
import com.intellij.testFramework.fixtures.IdeaTestFixtureFactory;
import com.intellij.testFramework.fixtures.JavaTestFixtureFactory;
import com.intellij.testFramework.fixtures.TestFixtureBuilder;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class IntentionTest extends UsefulTestCase {
    protected CodeInsightTestFixture myFixture;

    @Before
    public void setUp() throws Exception {
        final IdeaTestFixtureFactory fixtureFactory = IdeaTestFixtureFactory.getFixtureFactory();
        final TestFixtureBuilder<IdeaProjectTestFixture> testFixtureBuilder = fixtureFactory.createFixtureBuilder(getName());
        myFixture = JavaTestFixtureFactory.getFixtureFactory().createCodeInsightFixture(testFixtureBuilder.getFixture());
        myFixture.setTestDataPath("testData/intention");
        final JavaModuleFixtureBuilder builder = testFixtureBuilder.addModule(JavaModuleFixtureBuilder.class);
        builder.addContentRoot(myFixture.getTempDirPath()).addSourceRoot("");
        myFixture.setUp();
    }

    @After
    public void tearDown() throws Exception {
        myFixture.tearDown();
    }

    private void doTest(String testName, String hint) {
        myFixture.configureByFile(testName + ".pas");
        final IntentionAction action = myFixture.findSingleIntention(hint);
        Assert.assertNotNull(action);
        myFixture.launchAction(action);
        myFixture.checkResultByFile(testName + ".after.pas");
    }

    @Test
    public void testAddCompound() {
        doTest("addCompound", "Add compound");
    }

    @Test
    public void testRemoveCompound() {
        doTest("removeCompound", "Remove compound");
    }

    @Test
    public void testCreatePropertyFromParam() {
        doTest("createPropertyForParam", "Create property");
    }

}