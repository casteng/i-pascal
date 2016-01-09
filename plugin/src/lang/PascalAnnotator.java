package com.siberika.idea.pascal.lang;

import com.intellij.lang.annotation.Annotation;
import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.lang.annotation.Annotator;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.SmartList;
import com.siberika.idea.pascal.editor.PascalActionDeclare;
import com.siberika.idea.pascal.editor.PascalRoutineActions;
import com.siberika.idea.pascal.ide.actions.AddFixType;
import com.siberika.idea.pascal.ide.actions.SectionToggle;
import com.siberika.idea.pascal.ide.actions.UsesActions;
import com.siberika.idea.pascal.lang.parser.NamespaceRec;
import com.siberika.idea.pascal.lang.psi.PasConstExpression;
import com.siberika.idea.pascal.lang.psi.PasEntityScope;
import com.siberika.idea.pascal.lang.psi.PasEnumType;
import com.siberika.idea.pascal.lang.psi.PasModule;
import com.siberika.idea.pascal.lang.psi.PasNamespaceIdent;
import com.siberika.idea.pascal.lang.psi.PascalNamedElement;
import com.siberika.idea.pascal.lang.psi.PascalStructType;
import com.siberika.idea.pascal.lang.psi.impl.PasExportedRoutineImpl;
import com.siberika.idea.pascal.lang.psi.impl.PasField;
import com.siberika.idea.pascal.lang.psi.impl.PasRoutineImplDeclImpl;
import com.siberika.idea.pascal.lang.psi.impl.PascalRoutineImpl;
import com.siberika.idea.pascal.lang.references.PasReferenceUtil;
import com.siberika.idea.pascal.util.PsiContext;
import com.siberika.idea.pascal.util.PsiUtil;
import com.siberika.idea.pascal.util.StrUtil;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

import static com.siberika.idea.pascal.PascalBundle.message;

/**
 * Author: George Bakhtadze
 * Date: 12/14/12
 */
public class PascalAnnotator implements Annotator {

    public void annotate(@NotNull PsiElement element, @NotNull AnnotationHolder holder) {
        if ((element instanceof PascalNamedElement) && (PsiUtil.isRoutineName((PascalNamedElement) element))) {
            PsiElement parent = element.getParent();
            if (parent.getClass() == PasExportedRoutineImpl.class) {
                annotateRoutineInInterface((PasExportedRoutineImpl) parent, holder);
            } else if (parent.getClass() == PasRoutineImplDeclImpl.class) {
                annotateRoutineInImplementation((PasRoutineImplDeclImpl) parent, holder);
            }
        }

        //noinspection ConstantConditions
        if (PsiUtil.isEntityName(element) && !PsiUtil.isLastPartOfMethodImplName((PascalNamedElement) element)) {
            //noinspection ConstantConditions
            PascalNamedElement namedElement = (PascalNamedElement) element;
            List<PsiElement> scopes = new SmartList<PsiElement>();
            Collection<PasField> refs = PasReferenceUtil.resolveExpr(scopes, NamespaceRec.fromElement(element), PasField.TYPES_ALL, true, 0);

            if (refs.isEmpty()) {
                Annotation ann = holder.createErrorAnnotation(element, message("ann.error.undeclared.identifier"));
                Set<AddFixType> fixes = EnumSet.of(AddFixType.VAR, AddFixType.TYPE, AddFixType.CONST, AddFixType.ROUTINE); // [*] => var type const routine
                PsiElement scope = scopes.isEmpty() ? null : scopes.get(0);
                if (scope instanceof PasEnumType) {                                                         // TEnum.* => -* +enum
                    fixes = EnumSet.of(AddFixType.ENUM);
                } else if (scope instanceof PascalRoutineImpl) {                                            // [inRoutine] => +parameter
                    fixes.add(AddFixType.PARAMETER);
                }
                PsiContext context = PsiUtil.getContext(namedElement);
                if (context == PsiContext.TYPE_ID) {                                                         // [TypeIdent] => -* +type
                    fixes = EnumSet.of(AddFixType.TYPE);
                } else if (PsiTreeUtil.getParentOfType(namedElement, PasConstExpression.class) != null) {   // [part of const expr] => -* +const +enum
                    fixes = EnumSet.of(AddFixType.CONST);
                } else if ((context == PsiContext.EXPORT) || (context == PsiContext.CALL)) {
                    fixes = EnumSet.of(AddFixType.ROUTINE);
                } else if (context == PsiContext.PROPERTY_SPEC) {
                    fixes = EnumSet.of(AddFixType.VAR, AddFixType.ROUTINE);
                } else if (context == PsiContext.FOR) {
                    fixes = EnumSet.of(AddFixType.VAR);
                }

                String name = namedElement.getName();
                for (AddFixType fix : fixes) {
                    switch (fix) {
                        case VAR: {
                            if (!(scope instanceof PascalStructType)) {
                                ann.registerFix(new PascalActionDeclare.ActionCreateVarHP(message("action.create.var"), namedElement, null, context != PsiContext.FOR ? "T" : "Integer"));
                            }
                            PsiElement adjustedScope = adjustScope(scope);
                            if (adjustedScope instanceof PascalStructType) {
                                if (StrUtil.PATTERN_FIELD.matcher(name).matches()) {
                                    ann.registerFix(new PascalActionDeclare.ActionCreateVarHP(message("action.create.field"), namedElement, adjustedScope, "T"));
                                    if (context != PsiContext.PROPERTY_SPEC) {
                                        ann.registerFix(new PascalActionDeclare.ActionCreateProperty(message("action.create.property"), namedElement, adjustedScope));
                                    }
                                } else {
                                    ann.registerFix(new PascalActionDeclare.ActionCreateVar(message("action.create.field"), namedElement, adjustedScope, "T"));
                                    if (context != PsiContext.PROPERTY_SPEC) {
                                        ann.registerFix(new PascalActionDeclare.ActionCreatePropertyHP(message("action.create.property"), namedElement, adjustedScope));
                                    }
                                }
                            }
                            break;
                        }
                        case TYPE: {
                            boolean priority = name.startsWith("T");
                            if (!(scope instanceof PascalStructType)) {
                                ann.registerFix(PascalActionDeclare.newActionCreateType(namedElement, scope, priority));
                            }
                            ann.registerFix(PascalActionDeclare.newActionCreateType(namedElement, adjustScope(scope), priority));
                            break;
                        }
                        case CONST: {
                            boolean priority = !StrUtil.hasLowerCaseChar(name);
                            if (!(scope instanceof PascalStructType)) {
                                ann.registerFix(PascalActionDeclare.newActionCreateConst(namedElement, scope, priority));
                            }
                            ann.registerFix(PascalActionDeclare.newActionCreateConst(namedElement, adjustScope(scope), priority));
                            break;
                        }
                        case ROUTINE: {
                            if (scope instanceof PascalStructType) {
                                ann.registerFix(new PascalActionDeclare.ActionCreateRoutine(message("action.create.method"), namedElement, scope, null, null));
                            } else {
                                ann.registerFix(new PascalActionDeclare.ActionCreateRoutine(message("action.create.routine"), namedElement, scope, null, null));
                                PsiElement adjustedScope = adjustScope(scope);
                                if (adjustedScope instanceof PascalStructType) {
                                    ann.registerFix(new PascalActionDeclare.ActionCreateRoutine(message("action.create.method"), namedElement, adjustedScope, scope, null));
                                }
                            }
                            break;
                        }
                        case ENUM: {
                            ann.registerFix(new PascalActionDeclare.ActionCreateEnum(message("action.create.enumConst"), namedElement, scope));
                            break;
                        }
                        case PARAMETER: {
                            ann.registerFix(new PascalActionDeclare.ActionCreateParameter(message("action.create.parameter"), namedElement, scope));
                            break;
                        }
                    }
                }
            }
        }

        if ((element instanceof PascalNamedElement) && PsiUtil.isUsedUnitName(element.getParent())) {
            annotateUnit(holder, (PasNamespaceIdent) element.getParent());
        }
    }

    private PsiElement adjustScope(PsiElement scope) {
        if (scope instanceof PascalRoutineImpl) {
            PasEntityScope struct = ((PascalRoutineImpl) scope).getContainingScope();
            if (struct instanceof PascalStructType) {
                return struct;
            }
        }
        return scope;
    }

    private void annotateUnit(AnnotationHolder holder, PasNamespaceIdent usedUnitName) {
        if (PascalImportOptimizer.isExcludedFromCheck(usedUnitName)) {
            return;
        }
        Annotation ann = null;
        switch (PascalImportOptimizer.getUsedUnitStatus(usedUnitName)) {
            case UNUSED: {
                ann = holder.createWarningAnnotation(usedUnitName, message("ann.warn.unused.unit"));
                break;
            }
            case USED_IN_IMPL: {
                ann = holder.createWarningAnnotation(usedUnitName, message("ann.warn.unused.unit.interface"));
                ann.registerFix(new UsesActions.MoveUnitAction(message("action.uses.move"), usedUnitName));
                break;
            }
        }
        if (ann != null) {
            ann.registerFix(new UsesActions.RemoveUnitAction(message("action.uses.remove"), usedUnitName));
            ann.registerFix(new UsesActions.ExcludeUnitAction(message("action.uses.exclude"), usedUnitName));
            ann.registerFix(new UsesActions.OptimizeUsesAction(message("action.uses.optimize")));
        }
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
        if (!PsiUtil.isFromBuiltinsUnit(routine) && PsiUtil.needImplementation(routine) && (null == SectionToggle.getRoutineTarget(routine))) {
            Annotation ann = holder.createErrorAnnotation(routine, message("ann.error.missing.implementation"));
            ann.registerFix(new PascalRoutineActions.ActionImplement(message("action.implement"), routine));
            ann.registerFix(new PascalRoutineActions.ActionImplementAll(message("action.implement.all"), routine));
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
                    Annotation ann = holder.createInfoAnnotation(routine.getNameIdentifier() != null ? routine.getNameIdentifier() : routine, message("ann.error.missing.routine.declaration"));
                    ann.registerFix(new PascalRoutineActions.ActionDeclare(message("action.declare.routine"), routine));
                    ann.registerFix(new PascalRoutineActions.ActionDeclareAll(message("action.declare.routine.all"), routine));
                }
            } else {
                Annotation ann = holder.createErrorAnnotation(routine.getNameIdentifier() != null ? routine.getNameIdentifier() : routine, message("ann.error.missing.method.declaration"));
                ann.registerFix(new PascalRoutineActions.ActionDeclare(message("action.declare.method"), routine));
                ann.registerFix(new PascalRoutineActions.ActionDeclareAll(message("action.declare.method.all"), routine));
            }
        }
    }

}
