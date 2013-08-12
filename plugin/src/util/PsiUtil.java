package com.siberika.idea.pascal.util;

import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiWhiteSpace;
import com.intellij.psi.search.PsiElementProcessor;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.containers.ContainerUtil;
import com.siberika.idea.pascal.lang.psi.PasBlockGlobal;
import com.siberika.idea.pascal.lang.psi.PasClassMethod;
import com.siberika.idea.pascal.lang.psi.PasClosureExpression;
import com.siberika.idea.pascal.lang.psi.PasDeclSection;
import com.siberika.idea.pascal.lang.psi.PasDeclSectionLocal;
import com.siberika.idea.pascal.lang.psi.PasFormalParameterSection;
import com.siberika.idea.pascal.lang.psi.PasImplDeclSection;
import com.siberika.idea.pascal.lang.psi.PasMethodDecl;
import com.siberika.idea.pascal.lang.psi.PasModule;
import com.siberika.idea.pascal.lang.psi.PasModuleProgram;
import com.siberika.idea.pascal.lang.psi.PasProcedureType;
import com.siberika.idea.pascal.lang.psi.PasRoutineDecl;
import com.siberika.idea.pascal.lang.psi.PasStrucType;
import com.siberika.idea.pascal.lang.psi.PasUnitImplementation;
import com.siberika.idea.pascal.lang.psi.PasUnitInterface;
import com.siberika.idea.pascal.lang.psi.PascalPsiElement;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;

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
     * Returns nearest declaration clause, affecting the specified element, including formal parameters clause,
     * declaration section, structured type declaration, module declaration
     * @param element - element from where to search
     * @return one of the above elements or null if not found
     */
    @Deprecated
    public static PsiElement getOuterScopeDecl(PsiElement element) {
        if (element instanceof PasUnitImplementation) {
            PasUnitInterface unitInterface = PsiTreeUtil.getPrevSiblingOfType(element, PasUnitInterface.class);
            if (unitInterface != null) {
                return unitInterface;
            }
        }
        @SuppressWarnings("unchecked")
        PascalPsiElement parent = PsiTreeUtil.getParentOfType(element, PasFormalParameterSection.class, PasDeclSectionLocal.class, PasDeclSection.class, PasModule.class,
                PasMethodDecl.class, PasImplDeclSection.class, PasUnitImplementation.class, PasUnitInterface.class);
        // TODO: Used units handle
        return parent;
    }

    /**
     * Returns nearest declarations root element which affects the given element
     * @param element - element which should be affected by declarations found
     * @return nearest declarations root element which affects the given element
     */
    public static PsiElement getNearestAffectingDeclarationsRoot(PsiElement element) {
        if (element instanceof PasUnitImplementation) {
            PasUnitInterface unitInterface = PsiTreeUtil.getPrevSiblingOfType(element, PasUnitInterface.class);
            if (unitInterface != null) {
                return unitInterface;
            }
        }
        @SuppressWarnings("unchecked")
        PascalPsiElement parent = PsiTreeUtil.getParentOfType(element,
                PasRoutineDecl.class, PasMethodDecl.class, PasClassMethod.class, PasProcedureType.class, PasClosureExpression.class,
                PasModuleProgram.class, PasUnitImplementation.class, PasBlockGlobal.class,
                PasModule.class,
                PasDeclSection.class, PasDeclSectionLocal.class,
                PasUnitInterface.class,
                PasStrucType.class);
        // TODO: Used units handle
        return parent;
    }


}
