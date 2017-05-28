package com.siberika.idea.pascal.util;

import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.testFramework.fixtures.LightPlatformCodeInsightFixtureTestCase;
import com.siberika.idea.pascal.lang.psi.PascalStructType;
import com.siberika.idea.pascal.lang.psi.impl.PasField;
import com.siberika.idea.pascal.lang.psi.impl.PascalModuleImpl;
import com.siberika.idea.pascal.lang.references.PasReferenceUtil;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Author: George Bakhtadze
 * Date: 27/05/2017
 */
public class PosUtilTest extends LightPlatformCodeInsightFixtureTestCase {

    private Map<String, PascalStructType> structs;

    @Override
    protected String getTestDataPath() {
        return "testData/util";
    }

    @Override
    public void setUp() throws Exception {
        super.setUp();
        myFixture.configureByFiles("posUtilTest.pas");
        PascalModuleImpl mod = (PascalModuleImpl) PasReferenceUtil.findUnit(myFixture.getProject(),
                PasReferenceUtil.findUnitFiles(myFixture.getProject(), myModule), "posUtilTest");
        Collection<PascalStructType> named = PsiTreeUtil.findChildrenOfType(mod, PascalStructType.class);
        structs = new HashMap<String, PascalStructType>();
        for (PascalStructType element : named) {
            structs.put(element.getName(), element);
        }
    }

    public PascalStructType findPosInStruct(String name) throws Exception {
        return structs.get(name);
    }

    public void testEmptyClass() throws Exception {
        PascalStructType res = findPosInStruct("TClass1");
        assertEquals(Integer.valueOf(54), PosUtil.findPosInStruct(res, PasField.FieldType.TYPE, PasField.Visibility.PUBLIC).first);
        assertEquals(Integer.valueOf(54), PosUtil.findPosInStruct(res, PasField.FieldType.CONSTANT, PasField.Visibility.PUBLIC).first);
        assertEquals(Integer.valueOf(57), PosUtil.findPosInStruct(res, PasField.FieldType.VARIABLE, PasField.Visibility.PUBLIC).first);
        assertEquals(Integer.valueOf(57), PosUtil.findPosInStruct(res, PasField.FieldType.PROPERTY, PasField.Visibility.PUBLIC).first);
        assertEquals(Integer.valueOf(57), PosUtil.findPosInStruct(res, PasField.FieldType.ROUTINE, PasField.Visibility.PUBLIC).first);
    }

    public void testClassWith1Section() throws Exception {
        PascalStructType res = findPosInStruct("TClass2");
        assertEquals(Integer.valueOf(79), PosUtil.findPosInStruct(res, PasField.FieldType.TYPE, PasField.Visibility.PUBLIC).first);
        assertEquals(Integer.valueOf(91), PosUtil.findPosInStruct(res, PasField.FieldType.CONSTANT, PasField.Visibility.PRIVATE).first);
        assertEquals(Integer.valueOf(82), PosUtil.findPosInStruct(res, PasField.FieldType.VARIABLE, PasField.Visibility.PUBLIC).first);
        assertEquals(Integer.valueOf(94), PosUtil.findPosInStruct(res, PasField.FieldType.PROPERTY, PasField.Visibility.PRIVATE).first);
        assertEquals(Integer.valueOf(94), PosUtil.findPosInStruct(res, PasField.FieldType.ROUTINE, PasField.Visibility.PROTECTED).first);
    }

    public void testClassWith2Sections() throws Exception {
        PascalStructType res = findPosInStruct("TClass3");
        assertEquals(Integer.valueOf(141), PosUtil.findPosInStruct(res, PasField.FieldType.TYPE, PasField.Visibility.PUBLIC).first);
        assertEquals(Integer.valueOf(153), PosUtil.findPosInStruct(res, PasField.FieldType.CONSTANT, PasField.Visibility.PRIVATE).first);
        assertEquals(Integer.valueOf(146), PosUtil.findPosInStruct(res, PasField.FieldType.VARIABLE, PasField.Visibility.PUBLIC).first);
        assertEquals(Integer.valueOf(156), PosUtil.findPosInStruct(res, PasField.FieldType.PROPERTY, PasField.Visibility.PRIVATE).first);
        assertEquals(Integer.valueOf(146), PosUtil.findPosInStruct(res, PasField.FieldType.ROUTINE, PasField.Visibility.PROTECTED).first);
    }

    public void testClassWithField() throws Exception {
        PascalStructType res = findPosInStruct("TClass4");
        assertEquals(Integer.valueOf(180), PosUtil.findPosInStruct(res, PasField.FieldType.TYPE, PasField.Visibility.PUBLIC).first);
        assertEquals(Integer.valueOf(180), PosUtil.findPosInStruct(res, PasField.FieldType.CONSTANT, PasField.Visibility.PRIVATE).first);
        assertEquals(Integer.valueOf(203), PosUtil.findPosInStruct(res, PasField.FieldType.VARIABLE, PasField.Visibility.PUBLIC).first);
        assertEquals(Integer.valueOf(203), PosUtil.findPosInStruct(res, PasField.FieldType.PROPERTY, PasField.Visibility.PRIVATE).first);
        assertEquals(Integer.valueOf(203), PosUtil.findPosInStruct(res, PasField.FieldType.ROUTINE, PasField.Visibility.PROTECTED).first);
    }

    public void testClassWithFields() throws Exception {
        PascalStructType res = findPosInStruct("TClass5");
        assertEquals(Integer.valueOf(225), PosUtil.findPosInStruct(res, PasField.FieldType.TYPE, PasField.Visibility.PUBLIC).first);
        assertEquals(Integer.valueOf(225), PosUtil.findPosInStruct(res, PasField.FieldType.CONSTANT, PasField.Visibility.PRIVATE).first);
        assertEquals(Integer.valueOf(250), PosUtil.findPosInStruct(res, PasField.FieldType.VARIABLE, PasField.Visibility.PUBLIC).first);
        assertEquals(Integer.valueOf(305), PosUtil.findPosInStruct(res, PasField.FieldType.PROPERTY, PasField.Visibility.PRIVATE).first);
        assertEquals(Integer.valueOf(266), PosUtil.findPosInStruct(res, PasField.FieldType.ROUTINE, PasField.Visibility.PROTECTED).first);
    }

    public void testClassWithSectionsAndFields() throws Exception {
        PascalStructType res = findPosInStruct("TClass6");
        assertEquals(Integer.valueOf(392), PosUtil.findPosInStruct(res, PasField.FieldType.TYPE, PasField.Visibility.PUBLIC).first);
        assertEquals(Integer.valueOf(342), PosUtil.findPosInStruct(res, PasField.FieldType.CONSTANT, PasField.Visibility.PRIVATE).first);
        assertEquals(Integer.valueOf(397), PosUtil.findPosInStruct(res, PasField.FieldType.VARIABLE, PasField.Visibility.PUBLIC).first);
        assertEquals(Integer.valueOf(386), PosUtil.findPosInStruct(res, PasField.FieldType.PROPERTY, PasField.Visibility.PRIVATE).first);
        assertEquals(Integer.valueOf(418), PosUtil.findPosInStruct(res, PasField.FieldType.ROUTINE, PasField.Visibility.PROTECTED).first);
    }

    public void testClassWithSectionsAndFields2() throws Exception {
        PascalStructType res = findPosInStruct("TClass7");
        assertEquals(Integer.valueOf(482), PosUtil.findPosInStruct(res, PasField.FieldType.VARIABLE, PasField.Visibility.PRIVATE).first);
        assertEquals(Integer.valueOf(530), PosUtil.findPosInStruct(res, PasField.FieldType.VARIABLE, PasField.Visibility.PUBLIC).first);
        assertEquals(Integer.valueOf(516), PosUtil.findPosInStruct(res, PasField.FieldType.PROPERTY, PasField.Visibility.PRIVATE).first);
        assertEquals(Integer.valueOf(565), PosUtil.findPosInStruct(res, PasField.FieldType.PROPERTY, PasField.Visibility.PUBLIC).first);
        assertEquals(Integer.valueOf(482), PosUtil.findPosInStruct(res, PasField.FieldType.ROUTINE, PasField.Visibility.PROTECTED).first);
        assertEquals(Integer.valueOf(530), PosUtil.findPosInStruct(res, PasField.FieldType.ROUTINE, PasField.Visibility.PUBLIC).first);
    }

    public void testClassWithMethodAndProperty() throws Exception {
        PascalStructType res = findPosInStruct("TClass8");
        assertEquals(Integer.valueOf(609), PosUtil.findPosInStruct(res, PasField.FieldType.VARIABLE, PasField.Visibility.PRIVATE).first);
        assertEquals(Integer.valueOf(680), PosUtil.findPosInStruct(res, PasField.FieldType.PROPERTY, PasField.Visibility.PUBLIC).first);
        assertEquals(Integer.valueOf(640), PosUtil.findPosInStruct(res, PasField.FieldType.ROUTINE, PasField.Visibility.PROTECTED).first);
    }
}