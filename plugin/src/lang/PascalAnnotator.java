package com.siberika.idea.pascal.lang;

import com.intellij.lang.annotation.Annotation;
import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.lang.annotation.Annotator;
import com.intellij.psi.PsiElement;
import com.siberika.idea.pascal.editor.PascalActionDeclare;
import com.siberika.idea.pascal.ide.actions.SectionToggle;
import com.siberika.idea.pascal.lang.parser.NamespaceRec;
import com.siberika.idea.pascal.lang.psi.PasFunctionDirective;
import com.siberika.idea.pascal.lang.psi.PasInterfaceTypeDecl;
import com.siberika.idea.pascal.lang.psi.PasModule;
import com.siberika.idea.pascal.lang.psi.PascalNamedElement;
import com.siberika.idea.pascal.lang.psi.impl.PasExportedRoutineImpl;
import com.siberika.idea.pascal.lang.psi.impl.PasField;
import com.siberika.idea.pascal.lang.psi.impl.PasRoutineImplDeclImpl;
import com.siberika.idea.pascal.lang.references.PasReferenceUtil;
import com.siberika.idea.pascal.util.PsiUtil;
import com.siberika.idea.pascal.util.StrUtil;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.List;

import static com.siberika.idea.pascal.PascalBundle.message;

/**
 * Author: George Bakhtadze
 * Date: 12/14/12
 */
public class PascalAnnotator implements Annotator {

    public void annotate(@NotNull PsiElement element, @NotNull AnnotationHolder holder) {
        if (element instanceof PasExportedRoutineImpl) {
            annotateRoutineInInterface((PasExportedRoutineImpl) element, holder);
        } else if (element instanceof PasRoutineImplDeclImpl) {
            annotateRoutineInImplementation((PasRoutineImplDeclImpl) element, holder);
        }

        if (PsiUtil.isEntityName(element)) {
            PascalNamedElement namedElement = (PascalNamedElement) element;
            Collection<PasField> refs = PasReferenceUtil.resolveExpr(NamespaceRec.fromElement(element), PasField.TYPES_ALL, true, 0);
            if (refs.isEmpty()) {
                Annotation ann = holder.createErrorAnnotation(element, message("ann.error.undeclared.identifier"));
                String name = namedElement.getName();
                if (!StrUtil.hasLowerCaseChar(name)) {
                    ann.registerFix(new PascalActionDeclare(namedElement, PascalActionDeclare.CREATE_CONST));
                } else {
                    ann.registerFix(new PascalActionDeclare(namedElement, PascalActionDeclare.CREATE_VAR));
                }
                if (name.startsWith("T")) {
                    ann.registerFix(new PascalActionDeclare(namedElement, PascalActionDeclare.CREATE_TYPE));
                }
            }
        }
    }

    /**
     * E: [routine in interface only]     +Fix: implement routine
     * E: [method in declaration only]    +Fix: implement method
     * E: [class if not all methods implemented] +Fix: implement all methods
     */
    private void annotateRoutineInInterface(PasExportedRoutineImpl element, AnnotationHolder holder) {
        if (element.getContainingScope() instanceof PasInterfaceTypeDecl) {
            return;
        }
        if (null == SectionToggle.getRoutineTarget(element)) {
            if (element.getExternalDirective() != null) {
                return;
            }
            List<PasFunctionDirective> dirs = element.getFunctionDirectiveList();
            for (PasFunctionDirective dir : dirs) {
                if (dir.getText().toUpperCase().startsWith("ABSTRACT")) {
                    return;
                }
            }
            Annotation ann = holder.createErrorAnnotation(element, message("ann.error.missing.implemenation"));
        }
    }

    /**
     * E: [method in implementation only] +Fix: add to class declaration
     * F: [routines in implementation section only] - add to interface section
     */
    private void annotateRoutineInImplementation(PasRoutineImplDeclImpl element, AnnotationHolder holder) {
        if (null == SectionToggle.getRoutineTarget(element)) {
            if (element.getContainingScope() instanceof PasModule) {
                Annotation ann = holder.createInfoAnnotation(element.getNamedIdent() != null ? element.getNamedIdent() : element, message("ann.error.missing.declaration"));
            } else {
                Annotation ann = holder.createErrorAnnotation(element.getNamedIdent() != null ? element.getNamedIdent() : element, message("ann.error.missing.declaration"));
            }
        }
    }

}
