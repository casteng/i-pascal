package com.siberika.idea.pascal.lang.references.resolve;

import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiErrorElement;
import com.intellij.psi.PsiWhiteSpace;
import com.intellij.psi.util.PsiTreeUtil;
import com.siberika.idea.pascal.lang.parser.NamespaceRec;
import com.siberika.idea.pascal.lang.psi.PasCallExpr;
import com.siberika.idea.pascal.lang.psi.PasEntityScope;
import com.siberika.idea.pascal.lang.psi.PasFullyQualifiedIdent;
import com.siberika.idea.pascal.lang.psi.PasGenericTypeIdent;
import com.siberika.idea.pascal.lang.psi.PasIndexExpr;
import com.siberika.idea.pascal.lang.psi.PasNamedIdent;
import com.siberika.idea.pascal.lang.psi.PasNamedIdentDecl;
import com.siberika.idea.pascal.lang.psi.PasRefNamedIdent;
import com.siberika.idea.pascal.lang.psi.PasReferenceExpr;
import com.siberika.idea.pascal.lang.psi.PasSubIdent;
import com.siberika.idea.pascal.lang.psi.PascalIdentDecl;
import com.siberika.idea.pascal.lang.psi.PascalNamedElement;
import com.siberika.idea.pascal.lang.psi.impl.PasField;
import com.siberika.idea.pascal.lang.psi.impl.PascalExpression;
import com.siberika.idea.pascal.lang.references.ResolveContext;
import com.siberika.idea.pascal.lang.references.ResolveUtil;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.atomic.AtomicReference;

@SuppressWarnings("Convert2Lambda")
public class Resolve {

    // Returns True if no candidates found or all processor invocations returned True
    public static boolean resolveExpr(NamespaceRec fqn, ResolveContext context, ResolveProcessor processor) {
        PsiElement expr = ResolveUtil.isStubPowered(fqn.getParentIdent()) ? null : PsiTreeUtil.skipParentsOfType(fqn.getParentIdent(),
                PasFullyQualifiedIdent.class, PasSubIdent.class, PasRefNamedIdent.class, PasNamedIdent.class, PasNamedIdentDecl.class, PasGenericTypeIdent.class,
                PsiWhiteSpace.class, PsiErrorElement.class);
        if (expr instanceof PasReferenceExpr) {
            if ((fqn.isTargetingEnd() && (expr.getParent() instanceof PasCallExpr)) || (expr.getParent() instanceof PasIndexExpr)) {
                expr = expr.getParent();
            }
            ExpressionProcessor expressionProcessor = new ExpressionProcessor(fqn, context, processor);
            return expressionProcessor.resolveExprTypeScope((PascalExpression) expr, true);
        } else {                          // not within expression
            final FQNResolver fqnResolver = new FQNResolver(null, fqn, context) {
                @Override
                boolean processField(final PasEntityScope scope, final PasField field) {
                    return processor.process(scope, scope, field, field.fieldType);
                }
            };
            context.options.add(ResolveOptions.LAST_PART);
            return fqnResolver.resolve(true);
        }
    }

    @Nullable
    public static PascalNamedElement getDefaultProperty(PasEntityScope typeScope) {
        for (PasField field : typeScope.getAllFields()) {
            if (field.fieldType == PasField.FieldType.PROPERTY) {
                PascalNamedElement el = field.getElement();
                if ((el instanceof PascalIdentDecl) && ((PascalIdentDecl) el).isDefaultProperty()) {
                    return el;
                }
            }
        }
        return null;
    }

    public static boolean resolveFQN(NamespaceRec fqn, ResolveContext context, ResolveProcessor processor) {
        final FQNResolver resolver = new FQNResolver(null, fqn, context) {
            @Override
            boolean processField(PasEntityScope scope, PasField field) {
                return processor.process(scope, scope, field, field.fieldType);
            }
        };
        context.options.add(ResolveOptions.LAST_PART);
        return resolver.resolve(true);
    }

    public static PasField resolveFQN(String fqn, PsiElement context) {
        AtomicReference<PasField> result = new AtomicReference<>();
        resolveExpr(NamespaceRec.fromFQN(context, fqn), new ResolveContext(PasField.TYPES_ALL, true), new ResolveProcessor() {
            @Override
            public boolean process(PasEntityScope originalScope, PasEntityScope scope, PasField field, PasField.FieldType type) {
                result.set(field);
                return false;
            }
        });
        return result.get();
    }
}
