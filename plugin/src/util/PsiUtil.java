package com.siberika.idea.pascal.util;

import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiWhiteSpace;
import com.intellij.psi.search.PsiElementProcessor;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.SmartList;
import com.intellij.util.containers.ContainerUtil;
import com.siberika.idea.pascal.lang.psi.PasBlockGlobal;
import com.siberika.idea.pascal.lang.psi.PasClassMethod;
import com.siberika.idea.pascal.lang.psi.PasClosureExpression;
import com.siberika.idea.pascal.lang.psi.PasDeclSection;
import com.siberika.idea.pascal.lang.psi.PasDeclSectionLocal;
import com.siberika.idea.pascal.lang.psi.PasMethodDecl;
import com.siberika.idea.pascal.lang.psi.PasModule;
import com.siberika.idea.pascal.lang.psi.PasModuleProgram;
import com.siberika.idea.pascal.lang.psi.PasNamespaceIdent;
import com.siberika.idea.pascal.lang.psi.PasProcedureType;
import com.siberika.idea.pascal.lang.psi.PasRoutineDecl;
import com.siberika.idea.pascal.lang.psi.PasStrucType;
import com.siberika.idea.pascal.lang.psi.PasUnitImplementation;
import com.siberika.idea.pascal.lang.psi.PasUnitInterface;
import com.siberika.idea.pascal.lang.psi.PasUsesClause;
import com.siberika.idea.pascal.lang.psi.PasUsesFileClause;
import com.siberika.idea.pascal.lang.psi.PascalNamedElement;
import com.siberika.idea.pascal.lang.psi.PascalPsiElement;
import com.siberika.idea.pascal.lang.psi.impl.PasGenericTypeIdentImpl;
import com.siberika.idea.pascal.lang.psi.impl.PasNamespaceIdentImpl;
import com.siberika.idea.pascal.lang.psi.impl.PasRefNamedIdentImpl;
import com.siberika.idea.pascal.lang.psi.impl.PasSubIdentImpl;
import com.siberika.idea.pascal.lang.psi.impl.PasTypeIDImpl;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Author: George Bakhtadze
 * Date: 24/03/2013
 */
public class PsiUtil {
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
    public static <T extends PsiElement> T findImmChildOfAnyType(@Nullable final PsiElement element,
                                                                             @NotNull final Class<? extends T>... classes) {
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
        PascalPsiElement parent = PsiTreeUtil.getParentOfType(element,
                PasRoutineDecl.class, PasMethodDecl.class, PasClassMethod.class, PasProcedureType.class, PasClosureExpression.class,
                PasModuleProgram.class, PasUnitImplementation.class, PasBlockGlobal.class,
                PasModule.class,
                PasDeclSection.class, PasDeclSectionLocal.class,
                PasUnitInterface.class,
                PasStrucType.class);
        if (isInstanceOfAny(parent, PasRoutineDecl.class, PasMethodDecl.class, PasProcedureType.class) && element.getParent() == parent) {
            return getNearestAffectingDeclarationsRoot(parent);
        }
        return parent;
    }


    public static String getElDebugContext(PsiElement current) {
        return current != null ? "\"" + (current instanceof PascalNamedElement ? ((PascalNamedElement)current).getName() : "")
                + "\" [" + current.getClass().getSimpleName() + "]" + getParentStr(current.getParent()) : "-";
    }

    private static String getParentStr(PsiElement parent) {
        return parent != null ? parent.getText() + " [" + parent.getClass().getSimpleName() + "]" : "";
    }

    public static boolean isEntity(PsiElement element) {
        return (element.getClass() == PasSubIdentImpl.class) || (element.getClass() == PasRefNamedIdentImpl.class);
    }

    public static boolean isType(PsiElement element) {
        return (element.getClass() == PasGenericTypeIdentImpl.class) || (element.getParent().getClass() == PasTypeIDImpl.class);
    }

    public static <T extends PsiElement> boolean isInstanceOfAny(PsiElement object, Class<? extends T>... classes) {
        int i = classes.length - 1;
        while ((i >= 0) && (!classes[i].isInstance(object))) {
            i--;
        }
        return i >= 0;
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
}
