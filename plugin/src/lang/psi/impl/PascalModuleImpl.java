package com.siberika.idea.pascal.lang.psi.impl;

import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.siberika.idea.pascal.lang.psi.PasClosureExpression;
import com.siberika.idea.pascal.lang.psi.PasGenericTypeIdent;
import com.siberika.idea.pascal.lang.psi.PasMethodDecl;
import com.siberika.idea.pascal.lang.psi.PasNamedIdent;
import com.siberika.idea.pascal.lang.psi.PasNamespaceIdent;
import com.siberika.idea.pascal.lang.psi.PasProcedureType;
import com.siberika.idea.pascal.lang.psi.PasRoutineDecl;
import com.siberika.idea.pascal.lang.psi.PasStruct;
import com.siberika.idea.pascal.lang.psi.PascalNamedElement;
import com.siberika.idea.pascal.util.PsiUtil;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

/**
 * Author: George Bakhtadze
 * Date: 14/09/2013
 */
public class PascalModuleImpl extends PascalNamedElementImpl implements PasStruct {

    public static final int MAX_NON_BREAKING_NAMESPACES = 1;
    private Map<String, PasField> privateMembers = null;
    private Map<String, PasField> publicMembers = null;
    private Set<PascalNamedElement> redeclaredPublicMembers = null;
    private Set<PascalNamedElement> redeclaredPrivateMembers = null;

    public PascalModuleImpl(ASTNode node) {
        super(node);
    }

    @Override
    @Nullable
    public final PasField getField(final String name) {
        if (publicMembers == null) {
            buildPublicMembers();
        }
        PasField result = publicMembers.get(name);
        if (null == result) {
            if (privateMembers == null) {
                buildPrivateMembers();
            }
            result = privateMembers.get(name);
        }
        return result;
    }

    synchronized private void buildPrivateMembers() {
        if (privateMembers != null) { return; }  // TODO: check correctness
        privateMembers = new LinkedHashMap<String, PasField>();
        redeclaredPrivateMembers = new LinkedHashSet<PascalNamedElement>();
        System.out.println("buildPrivateMembers: " + getName());
        //noinspection unchecked
        retrieveEntitiesFromSection(PsiUtil.getModuleImplementationSection(this), PasField.Visibility.PRIVATE, privateMembers, redeclaredPrivateMembers,
                PasNamedIdent.class, PasGenericTypeIdent.class, PasNamespaceIdent.class);
    }

    synchronized private void buildPublicMembers() {
        if (publicMembers != null) { return; }  // TODO: check correctness
        publicMembers = new LinkedHashMap<String, PasField>();
        redeclaredPublicMembers = new LinkedHashSet<PascalNamedElement>();
        System.out.println("buildPublicMembers: " + getName());
        //noinspection unchecked
        retrieveEntitiesFromSection(PsiUtil.getModuleInterfaceSection(this), PasField.Visibility.PUBLIC, publicMembers, redeclaredPublicMembers,
                PasNamedIdent.class, PasGenericTypeIdent.class, PasNamespaceIdent.class);
    }

    private <T extends PascalNamedElement> void retrieveEntitiesFromSection(PsiElement section, PasField.Visibility visibility, Map<String, PasField> members, Set<PascalNamedElement> redeclaredMembers, Class<? extends T>... classes) {
        if (section != null) {
            for (PascalNamedElement namedElement : PsiUtil.findChildrenOfAnyType(section, classes)) {
                if (isSameAffectingScope(PsiUtil.getNearestAffectingDeclarationsRoot(namedElement), section)) {
                    if (!PsiUtil.isModuleName(namedElement) && !PsiUtil.isFormalParameterName(namedElement)) {
                        String name = namedElement.getName();
                        if (!members.containsKey(name)) {
                            PasField.Type type = PasField.Type.VARIABLE;
                            if (PsiUtil.isTypeName(namedElement)) {
                                type = PasField.Type.TYPE;
                            } else if (PsiUtil.isRoutineName(namedElement)) {
                                type = PasField.Type.ROUTINE;
                            } else if (PsiUtil.isUsedUnitName(namedElement)) {
                                type = PasField.Type.UNIT;
                            }
                            members.put(name, new PasField(this, namedElement, name, type, visibility));
                        } else {
                            redeclaredMembers.add(namedElement);
                        }

                    }
                }
            }
            retrieveEntitiesFromSection(PsiUtil.getNearestAffectingDeclarationsRoot(section), null, members, redeclaredMembers, classes);
        }
    }

    private static boolean isSameAffectingScope(PsiElement innerSection, PsiElement outerSection) {
        for (int i = 0; i < MAX_NON_BREAKING_NAMESPACES; i++) {
            if (innerSection == outerSection) {
                return true;
            }
            //noinspection unchecked
            if ((null == innerSection) || PsiUtil.isInstanceOfAny(innerSection, PasStruct.class, PasRoutineDecl.class, PasMethodDecl.class,
                    PasProcedureType.class, PasClosureExpression.class)) {
                return false;
            }
            innerSection = PsiUtil.getNearestAffectingDeclarationsRoot(innerSection);
        }
        return false;
    }

}
