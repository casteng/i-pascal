package com.siberika.idea.pascal.lang.references.resolve;

import com.intellij.psi.PsiElement;
import com.intellij.psi.impl.source.tree.LeafPsiElement;
import com.intellij.psi.search.PsiElementProcessor;
import com.siberika.idea.pascal.lang.parser.NamespaceRec;
import com.siberika.idea.pascal.lang.psi.PasArgumentList;
import com.siberika.idea.pascal.lang.psi.PasCallExpr;
import com.siberika.idea.pascal.lang.psi.PasClassProperty;
import com.siberika.idea.pascal.lang.psi.PasDereferenceExpr;
import com.siberika.idea.pascal.lang.psi.PasEntityScope;
import com.siberika.idea.pascal.lang.psi.PasExpr;
import com.siberika.idea.pascal.lang.psi.PasFullyQualifiedIdent;
import com.siberika.idea.pascal.lang.psi.PasIndexExpr;
import com.siberika.idea.pascal.lang.psi.PasProductExpr;
import com.siberika.idea.pascal.lang.psi.PasReferenceExpr;
import com.siberika.idea.pascal.lang.psi.PasTypeID;
import com.siberika.idea.pascal.lang.psi.PascalNamedElement;
import com.siberika.idea.pascal.lang.psi.PascalRoutine;
import com.siberika.idea.pascal.lang.psi.impl.PasField;
import com.siberika.idea.pascal.lang.psi.impl.PascalExpression;
import com.siberika.idea.pascal.lang.references.ResolveContext;
import com.siberika.idea.pascal.lang.search.routine.FieldMatcher;
import com.siberika.idea.pascal.lang.search.routine.ParamCountFieldMatcher;
import com.siberika.idea.pascal.util.ModuleUtil;
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
        if (null == this.context.unitNamespaces) {
            this.context.unitNamespaces = ModuleUtil.retrieveUnitNamespaces(fqn.getParentIdent());
        }
        this.processor = processor;
        this.currentScope = null;
    }

    @Override
    public boolean execute(@NotNull final PasReferenceExpr refExpr) {
        if (refExpr.getFullyQualifiedIdent() != fqn.getParentIdent()) {              // Not the FQN which originally requested
            final FQNResolver fqnResolver = new FQNResolver(currentScope, NamespaceRec.fromElement(refExpr.getFullyQualifiedIdent()), context) {
                @Override
                boolean processField(final PasEntityScope scope, final PasField field) {
                    currentScope = retrieveScope(scope, field);
                    return false;
                }
            };
            return fqnResolver.resolve(refExpr.getExpr() == null);
        } else {
            final FQNResolver fqnResolver = new FQNResolver(currentScope, fqn, context) {
                @Override
                boolean processScope(final PasEntityScope scope, final String fieldName) {
                    if (fieldName == null) {
                        return true;
                    }
                    boolean isDefault = "DEFAULT".equals(fieldName.toUpperCase());
                    if ((fqn.isTarget() || isDefault) && isWasType()) {         // "default" type pseudo value
                        PasField field = scope.getField(fieldName);
                        if (field != null) {
                            return processField(scope, field);
                        }
                        if (isDefault) {
                            fqn.next();
                            PasField defaultField = new PasField(scope, scope, "default", PasField.FieldType.CONSTANT, PasField.Visibility.PUBLIC);
                            return processField(scope, defaultField);
                        }
                    } else {
                        return processDefault(scope, fieldName);
                    }
                    return true;
                }

                @Override
                boolean processField(final PasEntityScope scope, final PasField field) {
                    return processor.process(scope, scope, field, field.fieldType);
                }
            };
            return fqnResolver.resolve(refExpr.getExpr() == null);
        }
    }

    boolean resolveExprTypeScope(PascalExpression expression, boolean lastPart) {
        if (expression instanceof PasReferenceExpr) {
            PasExpr scopeExpr = ((PasReferenceExpr) expression).getExpr();
            // Resolve FQN in scope of Expr
            if (scopeExpr != null) {
                resolveExprTypeScope((PascalExpression) scopeExpr, false);
            }
            return execute((PasReferenceExpr) expression);
        } else if (expression instanceof PasDereferenceExpr) {
            return resolveExprTypeScope((PascalExpression) ((PasDereferenceExpr) expression).getExpr(), false);
        } else if (expression instanceof PasIndexExpr) {
            return handleArray((PasIndexExpr) expression, lastPart);
        } else if (expression instanceof PasProductExpr) {                                      // AS operator case
            Operation op = Operation.forId(((PasProductExpr) expression).getMulOp().getText());
            if (op == Operation.AS) {
                List<PasExpr> exprs = ((PasProductExpr) expression).getExprList();
                return (exprs.size() < 2) || resolveExprTypeScope((PascalExpression) exprs.get(1), false);
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

    private boolean handleArray(final PasIndexExpr indexExpr, final boolean lastPart) {
        final boolean result = resolveExprTypeScope((PascalExpression) indexExpr.getExpr(), lastPart);
        PascalNamedElement defProp = currentScope != null ? Resolve.getDefaultProperty(currentScope) : null;    // Replace scope if indexing default array property
        if (defProp != null && defProp.getParent() instanceof PasClassProperty) {
            PasTypeID typeId = ((PasClassProperty) defProp.getParent()).getTypeID();
            final PasField field = typeId != null ? Types.resolveType(currentScope, typeId.getFullyQualifiedIdent()) : null;
            if (field != null) {
                currentScope = retrieveScope(currentScope, field);
            } else {
                currentScope = null;
            }
        }
        return result;
    }

    private boolean handleCall(final PasCallExpr callExpr, final boolean lastPart) {
        final PasExpr expr = callExpr.getExpr();
        if (expr instanceof PasReferenceExpr) {             // call of a routine specified explicitly with its name
            PasExpr scopeExpr = ((PasReferenceExpr) expr).getExpr();
            if (scopeExpr != null) {
                resolveExprTypeScope((PascalExpression) scopeExpr, false);
            }
            // Resolve FQN in current scope
            PasFullyQualifiedIdent fullyQualifiedIdent = ((PasReferenceExpr) expr).getFullyQualifiedIdent();
            final FQNResolver fqnResolver = new FQNResolver(currentScope, NamespaceRec.fromElement(fullyQualifiedIdent), context) {

                private PasEntityScope lastPartScope;

                @Override
                boolean processScope(final PasEntityScope scope, final String fieldName) {
                    final PasArgumentList args = callExpr.getArgumentList();
                    if (this.fqn.isTarget()) {
                        FieldMatcher matcher = new ParamCountFieldMatcher(fullyQualifiedIdent.getNamePart(), args.getExprList().size()) {
                            @Override
                            protected boolean onMatch(final PasField field, final PascalNamedElement element) {
                                if (lastPart) {                  // Return resolved field
                                    return ExpressionProcessor.this.processor.process(scope, scope, field, field.fieldType);
                                } else {                         // Resolve next scope
                                    currentScope = ((element instanceof PascalRoutine) && ((PascalRoutine) element).isConstructor()) ? ((lastPartScope != null) ? lastPartScope : scope) : retrieveScope(scope, field);
                                    return false;
                                }
                            }
                        };
                        if (!matcher.process(scope.getAllFields())) {
                            return false;
                        }
                    } else {                                     // No need to resolve intermediate names of FQN as routines
                        PasField field = scope.getField(fieldName);
                        if (field != null) {
                            this.fqn.next();
                            lastPartScope = getScope(scope, field, field.fieldType);
                            return (lastPartScope == null) || resolveNext(lastPartScope);
                        }
                    }
                    return true;
                }

                @Override
                boolean processField(final PasEntityScope scope, final PasField field) {
                    return true;
                }
            };
            return fqnResolver.resolve(scopeExpr == null);
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
        return Types.retrieveFieldTypeScope(field, context);
    }

}
