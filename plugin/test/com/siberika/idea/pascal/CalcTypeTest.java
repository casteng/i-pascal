package com.siberika.idea.pascal;

import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.testFramework.fixtures.LightPlatformCodeInsightFixtureTestCase;
import com.siberika.idea.pascal.lang.psi.PasEntityScope;
import com.siberika.idea.pascal.lang.psi.PasStatement;
import com.siberika.idea.pascal.lang.psi.impl.PasField;
import com.siberika.idea.pascal.lang.psi.impl.PascalExpression;
import com.siberika.idea.pascal.lang.psi.impl.PascalModuleImpl;
import com.siberika.idea.pascal.lang.references.PasReferenceUtil;
import com.siberika.idea.pascal.util.PsiUtil;
import com.siberika.idea.pascal.util.TestUtil;

import java.util.List;

public class CalcTypeTest extends LightPlatformCodeInsightFixtureTestCase {
    @Override
    protected String getTestDataPath() {
        return "testData/annotator";
    }

    public void testGetDefaultProperty() throws Exception {
        myFixture.configureByFiles("structTypes.pas");
        PasEntityScope obj = TestUtil.findClass(PsiUtil.getElementPasModule(myFixture.getFile()), "TObserverMapping");
        assertEquals("DefProp", PsiUtil.getDefaultProperty(obj).getName());
    }

    public void testExprType() throws Exception {
        myFixture.configureByFiles("structTypes.pas", "calcTypesTest.pas");
        PascalModuleImpl mod = (PascalModuleImpl) PasReferenceUtil.findUnit(myFixture.getProject(),
                PasReferenceUtil.findUnitFiles(myFixture.getProject(), myModule), "calcTypesTest");
        PasStatement stmt = PsiTreeUtil.findChildOfType(mod, PasStatement.class);
        PascalExpression expr = PsiTreeUtil.findChildOfType(stmt, PascalExpression.class);
        PsiElement par = expr.getParent();
        par = par != null ? par.getFirstChild() : null;
        if (par instanceof PascalExpression) {
            List<PasField.ValueType> types = PascalExpression.getTypes((PascalExpression) par);
            for (PasField.ValueType type : types) {
                System.out.println(String.format("%s: %s", type.field != null ? type.field.name : "<anon>",
                        type.kind != null ? type.kind.name() : "-"));
            }
        }
    }
}
