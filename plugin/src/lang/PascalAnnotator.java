package com.siberika.idea.pascal.lang;

import com.intellij.lang.annotation.Annotation;
import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.lang.annotation.Annotator;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.util.io.FileUtilRt;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.SmartList;
import com.siberika.idea.pascal.PascalBundle;
import com.siberika.idea.pascal.editor.PascalActionDeclare;
import com.siberika.idea.pascal.editor.PascalRoutineActions;
import com.siberika.idea.pascal.editor.refactoring.PascalRenameAction;
import com.siberika.idea.pascal.ide.actions.AddFixType;
import com.siberika.idea.pascal.ide.actions.SectionToggle;
import com.siberika.idea.pascal.ide.actions.UsesActions;
import com.siberika.idea.pascal.lang.context.ContextUtil;
import com.siberika.idea.pascal.lang.parser.NamespaceRec;
import com.siberika.idea.pascal.lang.psi.PasClassPropertySpecifier;
import com.siberika.idea.pascal.lang.psi.PasConstExpression;
import com.siberika.idea.pascal.lang.psi.PasEntityScope;
import com.siberika.idea.pascal.lang.psi.PasEnumType;
import com.siberika.idea.pascal.lang.psi.PasLibraryModuleHead;
import com.siberika.idea.pascal.lang.psi.PasModule;
import com.siberika.idea.pascal.lang.psi.PasNamespaceIdent;
import com.siberika.idea.pascal.lang.psi.PasPackageModuleHead;
import com.siberika.idea.pascal.lang.psi.PasProgramModuleHead;
import com.siberika.idea.pascal.lang.psi.PasUnitModuleHead;
import com.siberika.idea.pascal.lang.psi.PascalNamedElement;
import com.siberika.idea.pascal.lang.psi.PascalRoutine;
import com.siberika.idea.pascal.lang.psi.PascalStructType;
import com.siberika.idea.pascal.lang.psi.impl.PasExportedRoutineImpl;
import com.siberika.idea.pascal.lang.psi.impl.PasField;
import com.siberika.idea.pascal.lang.psi.impl.PasRoutineImplDeclImpl;
import com.siberika.idea.pascal.lang.psi.impl.PasVariantScope;
import com.siberika.idea.pascal.lang.references.PasReferenceUtil;
import com.siberika.idea.pascal.lang.references.ResolveContext;
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

        annotateModuleHead(element, holder);

        //noinspection ConstantConditions
        if (PsiUtil.isEntityName(element) && !PsiUtil.isLastPartOfMethodImplName((PascalNamedElement) element)) {
            //noinspection ConstantConditions
            PascalNamedElement namedElement = (PascalNamedElement) element;
            List<PsiElement> scopes = new SmartList<PsiElement>();
            ResolveContext resolveContext = new ResolveContext(null, PasField.TYPES_ALL, true, scopes, null);
            Collection<PasField> refs = PasReferenceUtil.resolveExpr(NamespaceRec.fromElement(element), resolveContext, 0);

            if (refs.isEmpty() && !isVariantField(scopes)) {
                Annotation ann = holder.createErrorAnnotation(element, message("ann.error.undeclared.identifier"));
                PsiContext context = PsiUtil.getContext(namedElement);
                Set<AddFixType> fixes = EnumSet.of(AddFixType.VAR, AddFixType.TYPE, AddFixType.CONST, AddFixType.ROUTINE, AddFixType.UNIT_FIND); // [*] => var type const routine
                if (context == PsiContext.FQN_FIRST) {
                    fixes.add(AddFixType.UNIT);
                }
                PsiElement scope = scopes.isEmpty() ? null : scopes.get(0);
                if (scope instanceof PasEnumType) {                                                          // TEnum.* => -* +enum
                    fixes = EnumSet.of(AddFixType.ENUM);
                    fixes.remove(AddFixType.UNIT_FIND);
                } else if (scope instanceof PascalRoutine) {                                                 // [inRoutine] => +parameter
                    fixes.add(AddFixType.PARAMETER);
                }
                if (context == PsiContext.TYPE_ID) {                                                         // [TypeIdent] => -* +type
                    fixes = EnumSet.of(AddFixType.TYPE, AddFixType.UNIT_FIND);
                } else if (PsiTreeUtil.getParentOfType(namedElement, PasConstExpression.class) != null) {    // [part of const expr] => -* +const +enum
                    fixes = EnumSet.of(AddFixType.CONST, AddFixType.UNIT_FIND);
                } else if (context == PsiContext.EXPORT) {
                    fixes = EnumSet.of(AddFixType.ROUTINE);
                } else if (context == PsiContext.CALL) {
                    fixes = EnumSet.of(AddFixType.ROUTINE, AddFixType.VAR, AddFixType.UNIT_FIND);
                } else if (context == PsiContext.PROPERTY_SPEC) {
                    fixes = EnumSet.of(AddFixType.VAR, AddFixType.ROUTINE);
                } else if (context == PsiContext.FOR) {
                    fixes = EnumSet.of(AddFixType.VAR, AddFixType.UNIT_FIND);
                }
                if (context == PsiContext.USES) {
                    fixes = EnumSet.of(AddFixType.NEW_UNIT);
                }

                String name = namedElement.getName();
                for (AddFixType fix : fixes) {
                    switch (fix) {
                        case VAR: {
                            boolean priority = context != PsiContext.CALL;
                            if (!(scope instanceof PascalStructType)) {
                                ann.registerFix(PascalActionDeclare.newActionCreateVar(message("action.create.var"), namedElement, null, priority, context != PsiContext.FOR ? null : "Integer"));
                            }
                            PsiElement adjustedScope = adjustScope(scope);
                            if (adjustedScope instanceof PascalStructType) {
                                if (StrUtil.PATTERN_FIELD.matcher(name).matches()) {
                                    ann.registerFix(PascalActionDeclare.newActionCreateVar(message("action.create.field"), namedElement, adjustedScope, priority, null));
                                    if (context != PsiContext.PROPERTY_SPEC) {
                                        ann.registerFix(PascalActionDeclare.newActionCreateProperty(message("action.create.property"), namedElement, adjustedScope, false));
                                    }
                                } else {
                                    ann.registerFix(PascalActionDeclare.newActionCreateVar(message("action.create.field"), namedElement, adjustedScope, false, null));
                                    if (context != PsiContext.PROPERTY_SPEC) {
                                        ann.registerFix(PascalActionDeclare.newActionCreateProperty(message("action.create.property"), namedElement, adjustedScope, priority));
                                    }
                                }
                            }
                            break;
                        }
                        case TYPE: {
                            boolean priority = name.startsWith("T");
                            if (!(scope instanceof PascalStructType) || (context != PsiContext.FQN_NEXT)) {
                                ann.registerFix(PascalActionDeclare.newActionCreateType(namedElement, null, priority));
                                priority = false;             // lower priority for nested
                            }
                            ann.registerFix(PascalActionDeclare.newActionCreateType(namedElement, adjustScope(scope), priority));
                            break;
                        }
                        case CONST: {
                            boolean priority = !StrUtil.hasLowerCaseChar(name);
                            if ((scope instanceof PascalStructType)) {
                                ann.registerFix(PascalActionDeclare.newActionCreateConst(namedElement, null, priority));
                                priority = false;             // lower priority for nested
                            } else {
                                ann.registerFix(PascalActionDeclare.newActionCreateConst(namedElement, scope, priority));
                            }
                            ann.registerFix(PascalActionDeclare.newActionCreateConst(namedElement, adjustScope(scope), priority));
                            break;
                        }
                        case ROUTINE: {
                            boolean priority = context == PsiContext.CALL;
                            if (scope instanceof PascalStructType) {
                                if (context == PsiContext.PROPERTY_SPEC) {
                                    PasClassPropertySpecifier spec = PsiTreeUtil.getParentOfType(namedElement, PasClassPropertySpecifier.class);
                                    ann.registerFix(PascalActionDeclare.newActionCreateRoutine(message("action.create." + (ContextUtil.isPropertyGetter(spec) ? "getter" : "setter")),
                                            namedElement, scope, null, priority, spec));
                                } else {
                                    ann.registerFix(PascalActionDeclare.newActionCreateRoutine(message("action.create.method"), namedElement, scope, null, priority, null));
                                }
                            } else {
                                ann.registerFix(PascalActionDeclare.newActionCreateRoutine(message("action.create.routine"), namedElement, scope, null, priority, null));
                                PsiElement adjustedScope = adjustScope(scope);
                                if (adjustedScope instanceof PascalStructType) {
                                    ann.registerFix(PascalActionDeclare.newActionCreateRoutine(message("action.create.method"), namedElement, adjustedScope, scope, priority, null));
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
                        case UNIT: {
                            ann.registerFix(new UsesActions.AddUnitAction(message("action.add.uses", namedElement.getName()), namedElement.getName(), ContextUtil.belongsToInterface(namedElement)));
                            break;
                        }
                        case NEW_UNIT: {
                            ann.registerFix(new UsesActions.NewUnitAction(message("action.create.unit"), namedElement.getName()));
                            break;
                        }
                        case UNIT_FIND: {
                            ann.registerFix(new UsesActions.SearchUnitAction(namedElement, ContextUtil.belongsToInterface(namedElement)));
                            break;
                        }
                    }
                }
            }
        }
    }

    private void annotateModuleHead(PsiElement element, AnnotationHolder holder) {
        PasNamespaceIdent nameIdent = null;
        if (element instanceof PasUnitModuleHead) {
            nameIdent = ((PasUnitModuleHead) element).getNamespaceIdent();
        } else if (element instanceof PasLibraryModuleHead) {
            nameIdent = ((PasLibraryModuleHead) element).getNamespaceIdent();
        } else if (element instanceof PasProgramModuleHead) {
            nameIdent = ((PasProgramModuleHead) element).getNamespaceIdent();
        } else if (element instanceof PasPackageModuleHead) {
            nameIdent = ((PasPackageModuleHead) element).getNamespaceIdent();
        }
        if (nameIdent != null) {
            String fn = element.getContainingFile().getName();
            String fileName = FileUtil.getNameWithoutExtension(fn);
            if (!nameIdent.getName().equalsIgnoreCase(fileName)) {
                Annotation ann = holder.createErrorAnnotation(element, PascalBundle.message("ann.error.unit.name.notmatch"));
                ann.registerFix(new PascalRenameAction(element, fileName, PascalBundle.message("action.module.rename")));
                ann.registerFix(new PascalRenameAction(element.getContainingFile(),
                        nameIdent.getName() + "." + FileUtilRt.getExtension(fn),
                        PascalBundle.message("action.file.rename")));
            }
        }
    }

    private boolean isVariantField(List<PsiElement> scopes) {
        return !scopes.isEmpty() && scopes.get(0) instanceof PasVariantScope;
    }

    private PsiElement adjustScope(PsiElement scope) {
        if (scope instanceof PascalRoutine) {
            PasEntityScope struct = ((PascalRoutine) scope).getContainingScope();
            if (struct instanceof PascalStructType) {
                return struct;
            }
        }
        return scope;
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
        if (!PsiUtil.isFromBuiltinsUnit(routine) && PsiUtil.needImplementation(routine) && (null == SectionToggle.retrieveImplementation(routine, true))) {
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
        if (null == SectionToggle.retrieveDeclaration(routine, true)) {
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
