package com.siberika.idea.pascal.util;

import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.testFramework.fixtures.LightPlatformCodeInsightFixtureTestCase;
import com.siberika.idea.pascal.lang.context.ContextUtil;
import com.siberika.idea.pascal.lang.psi.PascalNamedElement;
import com.siberika.idea.pascal.lang.psi.impl.PascalModuleImpl;
import com.siberika.idea.pascal.lang.references.PasReferenceUtil;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class PsiUtilTest extends LightPlatformCodeInsightFixtureTestCase {
    @Override
    protected String getTestDataPath() {
        return "testData/util";
    }

    public void testIsAssignLeftPart() {
        myFixture.configureByFiles("psiUtilTest.pas");
        Map<String, PascalNamedElement> named = collectNamed("psiUtilTest");
        assertTrue(ContextUtil.isAssignLeftPart(named.get("AssignLeftPart")));
        assertFalse(ContextUtil.isAssignLeftPart(named.get("AssignRightPart")));
        assertTrue(ContextUtil.isAssignLeftPart(named.get("LeftPart1")));
        assertTrue(ContextUtil.isAssignLeftPart(named.get("LeftPart2")));
        assertTrue(ContextUtil.isAssignLeftPart(named.get("LeftPart3")));
        assertTrue(ContextUtil.isAssignLeftPart(named.get("LeftPart4")));
    }

    private Map<String, PascalNamedElement> collectNamed(String unitName) {
        PascalModuleImpl mod = (PascalModuleImpl) PasReferenceUtil.findUnit(myFixture.getProject(),
                PasReferenceUtil.findUnitFiles(myFixture.getProject(), myModule), unitName);
        Collection<PascalNamedElement> named = PsiTreeUtil.findChildrenOfType(mod, PascalNamedElement.class);
        Map<String, PascalNamedElement> res = new HashMap<String, PascalNamedElement>();
        for (PascalNamedElement element : named) {
            res.put(element.getName(), element);
        }
        return res;
    }

}
