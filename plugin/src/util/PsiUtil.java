package com.siberika.idea.pascal.util;

import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiWhiteSpace;
import com.intellij.psi.search.PsiElementProcessor;
import com.intellij.psi.text.BlockSupport;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.SmartList;
import com.intellij.util.containers.ContainerUtil;
import com.siberika.idea.pascal.lang.psi.PasBlockGlobal;
import com.siberika.idea.pascal.lang.psi.PasBlockLocal;
import com.siberika.idea.pascal.lang.psi.PasClassField;
import com.siberika.idea.pascal.lang.psi.PasClassHelperDecl;
import com.siberika.idea.pascal.lang.psi.PasClassProperty;
import com.siberika.idea.pascal.lang.psi.PasClassTypeDecl;
import com.siberika.idea.pascal.lang.psi.PasClassTypeTypeDecl;
import com.siberika.idea.pascal.lang.psi.PasClosureExpression;
import com.siberika.idea.pascal.lang.psi.PasEntityID;
import com.siberika.idea.pascal.lang.psi.PasEntityScope;
import com.siberika.idea.pascal.lang.psi.PasExportedRoutine;
import com.siberika.idea.pascal.lang.psi.PasFormalParameter;
import com.siberika.idea.pascal.lang.psi.PasFormalParameterList;
import com.siberika.idea.pascal.lang.psi.PasFormalParameterSection;
import com.siberika.idea.pascal.lang.psi.PasFullyQualifiedIdent;
import com.siberika.idea.pascal.lang.psi.PasInterfaceTypeDecl;
import com.siberika.idea.pascal.lang.psi.PasMethodImplDecl;
import com.siberika.idea.pascal.lang.psi.PasModule;
import com.siberika.idea.pascal.lang.psi.PasModuleHead;
import com.siberika.idea.pascal.lang.psi.PasModuleProgram;
import com.siberika.idea.pascal.lang.psi.PasNamedIdent;
import com.siberika.idea.pascal.lang.psi.PasNamespaceIdent;
import com.siberika.idea.pascal.lang.psi.PasObjectDecl;
import com.siberika.idea.pascal.lang.psi.PasPointerType;
import com.siberika.idea.pascal.lang.psi.PasRecordDecl;
import com.siberika.idea.pascal.lang.psi.PasRecordField;
import com.siberika.idea.pascal.lang.psi.PasRecordHelperDecl;
import com.siberika.idea.pascal.lang.psi.PasRoutineImplDecl;
import com.siberika.idea.pascal.lang.psi.PasSubIdent;
import com.siberika.idea.pascal.lang.psi.PasTypeDecl;
import com.siberika.idea.pascal.lang.psi.PasTypeDeclaration;
import com.siberika.idea.pascal.lang.psi.PasTypeID;
import com.siberika.idea.pascal.lang.psi.PasUnitImplementation;
import com.siberika.idea.pascal.lang.psi.PasUnitInterface;
import com.siberika.idea.pascal.lang.psi.PasUsesClause;
import com.siberika.idea.pascal.lang.psi.PasUsesFileClause;
import com.siberika.idea.pascal.lang.psi.PasVarSection;
import com.siberika.idea.pascal.lang.psi.PascalNamedElement;
import com.siberika.idea.pascal.lang.psi.PascalPsiElement;
import com.siberika.idea.pascal.lang.psi.PascalQualifiedIdent;
import com.siberika.idea.pascal.lang.psi.impl.PasField;
import com.siberika.idea.pascal.lang.psi.impl.PasGenericTypeIdentImpl;
import com.siberika.idea.pascal.lang.psi.impl.PasNamespaceIdentImpl;
import com.siberika.idea.pascal.lang.psi.impl.PasRefNamedIdentImpl;
import com.siberika.idea.pascal.lang.psi.impl.PasSubIdentImpl;
import com.siberika.idea.pascal.lang.psi.impl.PasTypeIDImpl;
import com.siberika.idea.pascal.lang.psi.impl.PascalRoutineImpl;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * Author: George Bakhtadze
 * Date: 24/03/2013
 */
public class PsiUtil {

    public static final int MAX_NON_BREAKING_NAMESPACES = 2;

    @NotNull
    public static <T extends PsiElement> Collection<T> findChildrenOfAnyType(@Nullable final PsiElement element,
                                                                             @NotNull final Class<? extends T>... classes) {
        if (element == null) {
            return ContainerUtil.emptyList();
        }

        PsiElementProcessor.CollectElements<T> processor = new PsiElementProcessor.CollectElements<T>() {
            @Override
            public boolean execute(@NotNull T each) {
                if (each == element) return true;
                if (PsiTreeUtil.instanceOf(each, classes)) {
                    return super.execute(each);
                }
                return true;
            }
        };
        PsiTreeUtil.processElements(element, processor);
        return processor.getCollection();
    }

    @NotNull
    @SuppressWarnings("unchecked")
    public static <T extends PsiElement> Collection<T> findImmChildrenOfAnyType(@Nullable final PsiElement element,
                                                                                @NotNull final Class<? extends T>... classes) {
        if (element == null) {
            return ContainerUtil.emptyList();
        }

        Collection<T> result = new SmartList<T>();
        for (PsiElement each : element.getChildren()) {
                if (PsiTreeUtil.instanceOf(each, classes)) {
                    result.add((T) each);
                }
        };
        return result;
    }

    @Nullable
    @SuppressWarnings("unchecked")
    public static <T extends PsiElement> T findImmChildOfAnyType(@Nullable final PsiElement element, @NotNull final Class<? extends T>... classes) {
        if (element != null) {
            for (PsiElement each : element.getChildren()) {
                if (PsiTreeUtil.instanceOf(each, classes)) {
                    return (T) each;
                }
            };
        }
        return null;
    }

    /**
     * Searches for a next sibling element ignoring whitespace elements
     * @param element - element from where to search
     * @return next sibling element or null if not found
     */
    @Nullable
    public static PsiElement getNextSibling(PsiElement element) {
        PsiElement result = element.getNextSibling();
        while (result instanceof PsiWhiteSpace) {
            result = result.getNextSibling();
        }
        return result;
    }

    /**
     * Returns nearest declarations root element which affects the given element, including formal parameters clause,
     * declaration section, structured type declaration, module declaration
     * @param element - element which should be affected by declarations found
     * @return nearest declarations root element which affects the given element
     */
    @SuppressWarnings("unchecked")
    public static PsiElement getNearestAffectingDeclarationsRoot(PsiElement element) {
        if (element instanceof PasUnitImplementation) {
            PasUnitInterface unitInterface = PsiTreeUtil.getPrevSiblingOfType(element, PasUnitInterface.class);
            if (unitInterface != null) {
                return unitInterface;
            }
        }
        PascalPsiElement parent = getParentDeclRoot(element);
        if ((element instanceof PasNamedIdent) && (parent instanceof PascalRoutineImpl) && (element.getParent() == parent)) {
            // Make routine itself belong to parent root
            return getNearestAffectingDeclarationsRoot(parent);
        } else if (PsiUtil.isInstanceOfAny(parent, PasFormalParameterSection.class, PasBlockLocal.class)) {
            // Make routine local variable or parameters belong to the routine
            return getParentDeclRoot(parent);
        }
        return parent;
    }

    @SuppressWarnings("unchecked")
    private static PascalPsiElement getParentDeclRoot(PsiElement element) {   // TODO: remove blocks?
        return PsiTreeUtil.getParentOfType(element,
                PascalRoutineImpl.class, PasFormalParameterSection.class,
                PasClosureExpression.class,
                PasModuleProgram.class, PasUnitImplementation.class,
                PasBlockGlobal.class, PasBlockLocal.class,
                PasModule.class,
                PasUnitInterface.class,
                PasEntityScope.class
        );
    }

    /**
     * Returns nearest scope which affecting the element
     */
    @Nullable
    public static PasEntityScope getDeclRootScope(PsiElement element) {
        if (null == element) {
            return null;
        } else if (element instanceof PasEntityScope) {
            return (PasEntityScope) element;
        } else {
            return getDeclRootScope(getParentDeclRoot(element));
        }
    }

    public static String getElDebugContext(PsiElement current) {
        return current != null ? "\"" + (current instanceof PascalNamedElement ? ((PascalNamedElement)current).getName() : "")
                + "\" [" + current.getClass().getSimpleName() + "]" + getParentStr(current.getParent()) : "-";
    }

    private static String getParentStr(@NotNull PsiElement parent) {
        return parent.getText() + " [" + parent.getClass().getSimpleName() + "]";
    }

    public static boolean isEntityName(@NotNull PsiElement element) {
        return (element.getClass() == PasSubIdentImpl.class) || (element.getClass() == PasRefNamedIdentImpl.class);
    }

    public static boolean isTypeName(@NotNull PsiElement element) {
        return (element.getClass() == PasGenericTypeIdentImpl.class) || (element.getParent().getClass() == PasTypeIDImpl.class);
    }

    public static boolean isRoutineName(@NotNull PascalNamedElement element) {
        return (element.getParent() instanceof PasExportedRoutine) || (element.getParent() instanceof PasRoutineImplDecl);
    }

    public static boolean isUsedUnitName(@NotNull PascalNamedElement element) {
        return element.getParent() instanceof PasUsesClause;
    }

    public static boolean isModuleName(@NotNull PascalNamedElement element) {
        return element.getParent() instanceof PasModuleHead;
    }

    /**
     * @param element element
     * @return true if the element is a pointer type declaration. I.e. "TType = ^element".
     */
    public static boolean isPointerTypeDeclaration(@NotNull PascalNamedElement element) {
         return PsiTreeUtil.skipParentsOfType(element, PasSubIdent.class, PasFullyQualifiedIdent.class, PasEntityID.class, PasTypeIDImpl.class, PasTypeDecl.class) instanceof PasPointerType;
    }

    /**
     * @param element element
     * @return true if the element is a class reference type declaration. I.e. "CClass = class of TSomeClass".
     */
    public static boolean isClassRefDeclaration(@NotNull PascalNamedElement element) {
        return PsiTreeUtil.skipParentsOfType(element, PasSubIdent.class, PasFullyQualifiedIdent.class, PasEntityID.class, PasTypeIDImpl.class, PasTypeDecl.class) instanceof PasClassTypeTypeDecl;
    }

    public static boolean isFormalParameterName(@NotNull PascalNamedElement element) {
        return element.getParent() instanceof PasFormalParameter;
    }

    public static <T extends PsiElement> boolean isInstanceOfAny(PsiElement object, Class<? extends T>... classes) {
        int i = classes.length - 1;
        while ((i >= 0) && (!classes[i].isInstance(object))) {
            i--;
        }
        return i >= 0;
    }

    /**
     * Returns module containing the element
     * @param element - element
     * @return module containing the element
     */
    @Nullable
    public static PasModule getModule(@NotNull PsiElement element) {
        return PsiTreeUtil.findChildOfType(element.getContainingFile(), PasModule.class);
    }

    /**
     * Returns interface section of module specified by section
     * @param section - can be PasModule or PsiFile
     * @return interface section of module
     */
    @Nullable
    public static PsiElement getModuleInterfaceSection(@NotNull PsiElement section) {
        assert (section instanceof PasModule) || (section instanceof PsiFile);
        return PsiTreeUtil.findChildOfType(section, PasUnitInterface.class);
    }

    /**
     * Returns implementation section of module specified by section
     * @param section - can be PasModule or PsiFile
     * @return unit implementation section or module itself if the module is not a unit
     */
    @Nullable
    public static PsiElement getModuleImplementationSection(@NotNull PsiElement section) {
        assert (section instanceof PasModule) || (section instanceof PsiFile);
        PsiElement result = PsiTreeUtil.findChildOfType(section, PasUnitImplementation.class);
        if (result == null) {
            result = section;
        }
        return result;
    }

    @NotNull
    @SuppressWarnings("unchecked")
    public static List<PasNamespaceIdent> getUsedUnits(PsiFile file) {
        List<PasNamespaceIdent> result = new ArrayList<PasNamespaceIdent>();
        Collection<PascalPsiElement> usesClauses = findChildrenOfAnyType(file, PasUsesClause.class, PasUsesFileClause.class);
        for (PascalPsiElement usesClause : usesClauses) {
            for (PsiElement usedUnitName : usesClause.getChildren()) {
                if (usedUnitName.getClass() == PasNamespaceIdentImpl.class) {
                    result.add((PasNamespaceIdent) usedUnitName);
                }
            }
        }
        return result;
    }

    public static int getElementLevel(PsiElement element) {
        int result = 0;
        while ((element != null) && (!(element instanceof PsiFile))) {
            result++;
            element = element.getParent();
        }
        return result;
    }

    public static <T extends PascalNamedElement> void retrieveEntitiesFromSection(PasEntityScope owner, PsiElement section,
                                                                                  PasField.Visibility visibility, FieldCollector fieldCollector, Class<? extends T>... classes) {
        if (section != null) {
            for (PascalNamedElement namedElement : PsiUtil.findChildrenOfAnyType(section, classes)) {
                if (isSameAffectingScope(PsiUtil.getNearestAffectingDeclarationsRoot(namedElement), owner)) {
                    if (!PsiUtil.isModuleName(namedElement) && !PsiUtil.isFormalParameterName(namedElement)) {
                        String name = namedElement.getName();
                        if (!fieldCollector.fieldExists(namedElement)) {
                            PasField.Type type = PasField.Type.VARIABLE;
                            if (PsiUtil.isTypeName(namedElement)) {
                                type = PasField.Type.TYPE;
                            } else if (PsiUtil.isRoutineName(namedElement)) {
                                type = PasField.Type.ROUTINE;
                            } else if (PsiUtil.isUsedUnitName(namedElement)) {
                                type = PasField.Type.UNIT;
                            }
                            fieldCollector.addField(name, new PasField(owner, namedElement, name, type, visibility));
                        }

                    }
                }
            }
            retrieveEntitiesFromSection(owner, PsiUtil.getNearestAffectingDeclarationsRoot(section), visibility, fieldCollector, classes);
        }
    }

    private static boolean isSameAffectingScope(PsiElement innerSection, PsiElement outerSection) {
        for (int i = 0; i < MAX_NON_BREAKING_NAMESPACES; i++) {
            if ((innerSection == outerSection) || sameModuleSections(innerSection, outerSection)) {
                return true;
            }
            //noinspection unchecked
            if ((null == innerSection) || PsiUtil.isInstanceOfAny(innerSection,
                    PasClassTypeDecl.class, PasClassHelperDecl.class, PasInterfaceTypeDecl.class, PasObjectDecl.class, PasRecordDecl.class, PasRecordHelperDecl.class,
                    PasRoutineImplDecl.class, PasMethodImplDecl.class, PasClosureExpression.class)) {
                return false;
            }
            innerSection = PsiUtil.getNearestAffectingDeclarationsRoot(innerSection);
        }
        return false;
    }

    private static boolean sameModuleSections(PsiElement innerSection, PsiElement outerSection) {
        return (outerSection instanceof PasModule) && (innerSection instanceof PasUnitInterface) && (((PasModule) outerSection).getUnitInterface() == innerSection);
    }

    @NotNull
    public static List<PasFormalParameter> getFormalParameters(PasFormalParameterSection paramsSection) {
        if (paramsSection != null) {
            PasFormalParameterList paramList = paramsSection.getFormalParameterList();
            if (paramList != null) {
                return paramList.getFormalParameterList();
            }
        }
        return Collections.emptyList();
    }

    @Nullable
    public static <T extends PsiElement> T findInSameSection(PsiElement section, Class<? extends T>...classes) {
        T element = PsiTreeUtil.findChildOfAnyType(section, classes);
        if ((element != null) && (isSameAffectingScope(getNearestAffectingDeclarationsRoot(element), section))) {
            return element;
        }
        return null;
    }

    public static void rebuildPsi(@NotNull PsiElement block) {
        BlockSupport.getInstance(block.getProject()).reparseRange(block.getContainingFile(), block.getTextRange().getStartOffset(), block.getTextRange().getEndOffset(), block.getText());
    }

    public static boolean isFieldDecl(PascalNamedElement entityDecl) {
        return (entityDecl.getParent() instanceof PasRecordField) || (entityDecl.getParent() instanceof PasClassField);
    }

    public static boolean isPropertyDecl(PascalNamedElement entityDecl) {
        return (entityDecl.getParent() instanceof PasClassProperty);
    }

    /**
     * Checks if the entityDecl is a declaration of variable or formal parameter
     * @param entityDecl entity declaration to check
     * @return true if the entityDecl is a declaration of variable or formal parameter
     */
    public static boolean isVariableDecl(PascalNamedElement entityDecl) {
        return (entityDecl.getParent() instanceof PasVarSection) || (entityDecl.getParent() instanceof PasFormalParameter);
    }

    /**
     * Returns type identifier element by entity declaration
     */
    @Nullable
    public static PascalNamedElement getEntityType(PascalNamedElement entityDecl) {
        if (PsiUtil.isVariableDecl(entityDecl) || PsiUtil.isFieldDecl(entityDecl) || PsiUtil.isPropertyDecl(entityDecl)) { // variable declaration case
            PascalPsiElement varDecl = PsiTreeUtil.getNextSiblingOfType(entityDecl, PasTypeDecl.class);
            if (null == varDecl) {
                varDecl = PsiTreeUtil.getNextSiblingOfType(entityDecl, PasTypeID.class);
            }
            if (varDecl != null) {
                return PsiTreeUtil.findChildOfType(varDecl, PascalQualifiedIdent.class, true);
            }
        } else if (entityDecl.getParent() instanceof PasTypeDeclaration) {                                    // type declaration case
            return entityDecl;
        } else if (entityDecl.getParent() instanceof PascalRoutineImpl) {                                     // routine declaration case
            return  ((PascalRoutineImpl) entityDecl.getParent()).getFunctionTypeIdent();
        }
        return null;
    }

    public static boolean allowsForwardReference(PsiElement element) {
        return (element instanceof PascalNamedElement) &&
               (PsiUtil.isPointerTypeDeclaration((PascalNamedElement) element) || PsiUtil.isClassRefDeclaration((PascalNamedElement) element));
    }
}
