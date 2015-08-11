package com.siberika.idea.pascal.lang;

import com.intellij.lang.annotation.Annotation;
import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.lang.annotation.Annotator;
import com.intellij.psi.PsiElement;
import com.siberika.idea.pascal.editor.PascalActionDeclare;
import com.siberika.idea.pascal.editor.PascalRoutineActions;
import com.siberika.idea.pascal.ide.actions.SectionToggle;
import com.siberika.idea.pascal.lang.parser.NamespaceRec;
import com.siberika.idea.pascal.lang.psi.PasClassQualifiedIdent;
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
import org.apache.commons.lang.StringUtils;
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

        if (PsiUtil.isEntityName(element) && !isLastPartOfMethodImplName((PascalNamedElement) element)) {
            PascalNamedElement namedElement = (PascalNamedElement) element;
            Collection<PasField> refs = PasReferenceUtil.resolveExpr(NamespaceRec.fromElement(element), PasField.TYPES_ALL, true, 0);
            if (refs.isEmpty()) {
                Annotation ann = holder.createErrorAnnotation(element, message("ann.error.undeclared.identifier"));
                String name = namedElement.getName();
                if (!StrUtil.hasLowerCaseChar(name)) {
                    ann.registerFix(new PascalActionDeclare(namedElement, message("action.createConst"), PascalActionDeclare.CREATE_CONST));
                } else {
                    ann.registerFix(new PascalActionDeclare(namedElement, message("action.createVar"), PascalActionDeclare.CREATE_VAR));
                }
                if (name.startsWith("T")) {
                    ann.registerFix(new PascalActionDeclare(namedElement, message("action.createType"), PascalActionDeclare.CREATE_TYPE));
                }
            }
        }
    }

    private boolean isLastPartOfMethodImplName(PascalNamedElement element) {
        PsiElement parent = element.getParent();
        if (parent instanceof PasClassQualifiedIdent) {
            PasClassQualifiedIdent name = (PasClassQualifiedIdent) parent;
            return (element == name.getSubIdentList().get(name.getSubIdentList().size() - 1))
                 && PsiUtil.isRoutineName((PascalNamedElement) parent) && !StringUtils.isEmpty(((PascalNamedElement) parent).getNamespace());
        }
        return false;
    }

    /**
     * # unimplemented routine error
     * # unimplemented method  error
     * # filter external/abstract routines/methods
     * # implement routine fix
     * # implement method fix
     * error on class if not all methods implemented
     * implement all methods fix
     */
    private void annotateRoutineInInterface(PasExportedRoutineImpl routine, AnnotationHolder holder) {
        if (routine.getContainingScope() instanceof PasInterfaceTypeDecl) {
            return;
        }
        if (null == SectionToggle.getRoutineTarget(routine)) {
            if (routine.getExternalDirective() != null) {
                return;
            }
            List<PasFunctionDirective> dirs = routine.getFunctionDirectiveList();
            for (PasFunctionDirective dir : dirs) {
                if (dir.getText().toUpperCase().startsWith("ABSTRACT")) {
                    return;
                }
            }
            Annotation ann = holder.createErrorAnnotation(routine, message("ann.error.missing.implemenation"));
            ann.registerFix(new PascalActionDeclare(routine, message("action.implement"), PascalRoutineActions.IMPLEMENT));
        }
    }

    /**
     * error on method in implementation only
     * add method to class declaration fix
     * add to interface section fix for routines in implementation section only
     */
    private void annotateRoutineInImplementation(PasRoutineImplDeclImpl routine, AnnotationHolder holder) {
        if (null == SectionToggle.getRoutineTarget(routine)) {
            if (routine.getContainingScope() instanceof PasModule) {
                if (((PasModule) routine.getContainingScope()).getUnitInterface() != null) {
                    Annotation ann = holder.createWeakWarningAnnotation(routine.getNameIdentifier() != null ? routine.getNameIdentifier() : routine, message("ann.error.missing.declaration"));
                    ann.registerFix(new PascalActionDeclare(routine, message("action.declare"), PascalRoutineActions.DECLARE));
                }
            } else {
                Annotation ann = holder.createErrorAnnotation(routine.getNameIdentifier() != null ? routine.getNameIdentifier() : routine, message("ann.error.missing.declaration"));
                ann.registerFix(new PascalActionDeclare(routine, message("action.declare"), PascalRoutineActions.DECLARE));
            }
        }
    }

}
