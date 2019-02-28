package com.siberika.idea.pascal.lang.references.resolve;

import com.intellij.psi.PsiElement;
import com.intellij.psi.impl.source.tree.LeafPsiElement;
import com.intellij.psi.search.PsiElementProcessor;
import com.siberika.idea.pascal.lang.parser.NamespaceRec;
import com.siberika.idea.pascal.lang.psi.PasArgumentList;
import com.siberika.idea.pascal.lang.psi.PasCallExpr;
import com.siberika.idea.pascal.lang.psi.PasDereferenceExpr;
import com.siberika.idea.pascal.lang.psi.PasEntityScope;
import com.siberika.idea.pascal.lang.psi.PasExpr;
import com.siberika.idea.pascal.lang.psi.PasFullyQualifiedIdent;
import com.siberika.idea.pascal.lang.psi.PasIndexExpr;
import com.siberika.idea.pascal.lang.psi.PasProductExpr;
import com.siberika.idea.pascal.lang.psi.PasReferenceExpr;
import com.siberika.idea.pascal.lang.psi.PascalNamedElement;
import com.siberika.idea.pascal.lang.psi.PascalRoutine;
import com.siberika.idea.pascal.lang.psi.impl.PasField;
import com.siberika.idea.pascal.lang.psi.impl.PascalExpression;
import com.siberika.idea.pascal.lang.references.PasReferenceUtil;
import com.siberika.idea.pascal.lang.references.ResolveContext;
import org.jetbrains.annotations.NotNull;

import java.util.List;

class ExpressionProcessor implements PsiElementProcessor<PasReferenceExpr> {

    private final NamespaceRec fqn;
    private final ResolveContext context;
    private final ResolveProcessor processor;
    private PasEntityScope currentScope;

    ExpressionProcessor(final NamespaceRec fqn, final ResolveContext context, final ResolveProcessor processor) {
        this.fqn = fqn;
        this.context = context;
        this.processor = processor;
        currentScope = null;
    }

    @Override
    public boolean execute(@NotNull final PasReferenceExpr refExpr) {
        if (refExpr.getFullyQualifiedIdent() != fqn.getParentIdent()) {              // Not the FQN which originally requested
            final FQNResolver fqnResolver = new FQNResolver(currentScope, NamespaceRec.fromElement(refExpr.getFullyQualifiedIdent()), context) {
                @Override
                boolean process(final PasEntityScope scope, final String fieldName) {
                    return processDefault(scope, fieldName);
                }

                @Override
                boolean processField(final PasEntityScope scope, final PasField field) {
                    currentScope = retrieveScope(scope, field);
                    return false;
                }
            };
            fqnResolver.resolve(refExpr.getExpr() == null);
        } else {
            final FQNResolver fqnResolver = new FQNResolver(currentScope, fqn, context) {
                @Override
                boolean process(final PasEntityScope scope, final String fieldName) {
                    return processDefault(scope, fieldName);
                }

                @Override
                boolean processField(final PasEntityScope scope, final PasField field) {
                    return processor.process(scope, scope, field, field.fieldType);
                }
            };
            fqnResolver.resolve(refExpr.getExpr() == null);
        }
        return false;
    }

    boolean resolveExprTypeScope(PascalExpression expression, boolean lastPart) {
        if (expression instanceof PasReferenceExpr) {
            PasExpr scopeExpr = ((PasReferenceExpr) expression).getExpr();
            // Resolve FQN in scope of Expr
            if (scopeExpr != null) {
                resolveExprTypeScope((PascalExpression) scopeExpr, false);
            }
            execute((PasReferenceExpr) expression);
        } else if (expression instanceof PasDereferenceExpr) {
            return resolveExprTypeScope((PascalExpression) ((PasDereferenceExpr) expression).getExpr(), false);
        } else if (expression instanceof PasIndexExpr) {
            return resolveExprTypeScope((PascalExpression) ((PasIndexExpr) expression).getExpr(), false);
            // send to special method of processor
            /*PasEntityScope typeScope = field != null ? retrieveScope(null, null, field) : null;
            PascalNamedElement defProp = typeScope != null ? PsiUtil.getDefaultProperty(typeScope) : null;    // Replace scope if indexing default array property
            if (defProp instanceof PasClassProperty) {
                PasTypeID typeId = ((PasClassProperty) defProp).getTypeID();
                return typeId != null ? resolveType(typeScope, typeId.getFullyQualifiedIdent()) : null;
            }*/
        } else if (expression instanceof PasProductExpr) {                                      // AS operator case
            Operation op = Operation.forId(((PasProductExpr) expression).getMulOp().getText());
            if (op == Operation.AS) {
                List<PasExpr> exprs = ((PasProductExpr) expression).getExprList();
                return exprs.size() < 2 || resolveExprTypeScope((PascalExpression) exprs.get(1), false);
            }
        } else if (expression instanceof PasCallExpr) {
            return handleCall((PasCallExpr) expression, lastPart);
        }
        PsiElement child = getFirstChild(expression);
        if (child instanceof PascalExpression) {
            return resolveExprTypeScope((PascalExpression) child, false);
        }
        return true;
    }

    private PasField callTarget;
    private PasEntityScope callTargetScope;

    private boolean handleCall(final PasCallExpr callExpr, final boolean lastPart) {
        final PasExpr expr = callExpr.getExpr();
        if (expr instanceof PasReferenceExpr) {             // call of a routine specified explicitly with its name
            callTarget = null;
            PasExpr scopeExpr = ((PasReferenceExpr) expr).getExpr();
            if (scopeExpr != null) {
                resolveExprTypeScope((PascalExpression) scopeExpr, false);
            }
            // Resolve FQN in current scope
            PasFullyQualifiedIdent fullyQualifiedIdent = ((PasReferenceExpr) expr).getFullyQualifiedIdent();
            final FQNResolver fqnResolver = new FQNResolver(currentScope, NamespaceRec.fromElement(fullyQualifiedIdent), context) {
                @Override
                boolean process(final PasEntityScope scope, final String fieldName) {
                    final PasArgumentList args = callExpr.getArgumentList();
                    final int argsCount = args.getExprList().size();
                    for (PasField field1 : scope.getAllFields()) {
                        if ((field1.fieldType == PasField.FieldType.ROUTINE) && fullyQualifiedIdent.getNamePart().equalsIgnoreCase(field1.name)) {
                            PascalNamedElement el = field1.getElement();
                            if (el instanceof PascalRoutine) {
                                PascalRoutine routine = (PascalRoutine) el;
                                if (routine.getFormalParameterNames().size() == argsCount) {
                                    if (lastPart) {                  // Return resolved field
                                        return ExpressionProcessor.this.processor.process(scope, scope, field1, field1.fieldType);
                                    } else {                         // Resolve next scope
                                        currentScope = retrieveScope(scope, field1);
                                        return false;
                                    }
                                }
                            }
                        }
                    }
                    // Get call target field to use if exact call target will be not found
                    if (null == callTarget) {
                        callTarget = scope.getField(fieldName);
                        callTargetScope = scope;
                    }
                    return true;
                }

                @Override
                boolean processField(final PasEntityScope scope, final PasField field) {
                    return true;
                }
            };
            if (fqnResolver.resolve(scopeExpr == null)) {               // No call candidates found
                if (callTarget != null) {
                    if (lastPart) {                  // Return resolved field
                        return ExpressionProcessor.this.processor.process(currentScope, callTargetScope, callTarget, callTarget.fieldType);
                    } else {                         // Resolve next scope
                        currentScope = retrieveScope(callTargetScope, callTarget);
                        return false;
                    }
                }
            }
        }
        return true;
    }

    private static PsiElement getFirstChild(PascalExpression expr) {
        PsiElement res = expr.getFirstChild();
        while ((res != null) && (res.getClass() == LeafPsiElement.class)) {
            res = res.getNextSibling();
        }
        return res;
    }

    // TODO: replace with PasNamedIdent.getScope() or getType()
    private PasEntityScope retrieveScope(PasEntityScope scope, PasField field) {
        if (field.fieldType == PasField.FieldType.UNIT) {
            return (PasEntityScope) field.getElement();
        }
        return PasReferenceUtil.retrieveFieldTypeScope(field, context);
    }

}
