package com.siberika.idea.pascal.lang.references.resolve;

import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiErrorElement;
import com.intellij.psi.PsiWhiteSpace;
import com.intellij.psi.util.PsiTreeUtil;
import com.siberika.idea.pascal.lang.parser.NamespaceRec;
import com.siberika.idea.pascal.lang.psi.PasCallExpr;
import com.siberika.idea.pascal.lang.psi.PasClassPropertySpecifier;
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
import org.jetbrains.annotations.Nullable;

@SuppressWarnings("Convert2Lambda")
public class Resolve {

    // Returns True if no candidates found or all processor invocations returned True
    public static boolean resolveExpr(NamespaceRec fqn, ResolveContext context, ResolveProcessor processor) {
        PsiElement expr = PsiTreeUtil.skipParentsOfType(fqn.getParentIdent(),
                PasFullyQualifiedIdent.class, PasSubIdent.class, PasRefNamedIdent.class, PasNamedIdent.class, PasNamedIdentDecl.class, PasGenericTypeIdent.class,
                PsiWhiteSpace.class, PsiErrorElement.class);
        if (expr instanceof PasReferenceExpr) {
            if (fqn.isTargetingEnd() && expr.getParent() instanceof PasCallExpr || expr.getParent() instanceof PasIndexExpr) {
                expr = expr.getParent();
            }
            ExpressionProcessor expressionProcessor = new ExpressionProcessor(fqn, context, processor);
            return expressionProcessor.resolveExprTypeScope((PascalExpression) expr, true);
        } else {
            if (fqn.getParentIdent() instanceof PasClassPropertySpecifier) {
                context.options.add(ResolveOptions.PROPERTY_SPECIFIER);
            }
            final FQNResolver fqnResolver = new FQNResolver(null, fqn, context) {
                @Override
                boolean processField(final PasEntityScope scope, final PasField field) {
                    return processor.process(scope, scope, field, field.fieldType);
                }
            };
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
}
