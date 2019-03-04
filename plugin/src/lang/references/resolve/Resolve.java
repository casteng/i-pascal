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
import com.siberika.idea.pascal.lang.psi.impl.PasField;
import com.siberika.idea.pascal.lang.psi.impl.PascalExpression;
import com.siberika.idea.pascal.lang.references.ResolveContext;

@SuppressWarnings("Convert2Lambda")
public class Resolve {

    public static boolean resolveExpr(NamespaceRec fqn, ResolveContext context, ResolveProcessor processor) {
        PsiElement expr = PsiTreeUtil.skipParentsOfType(fqn.getParentIdent(),
                PasFullyQualifiedIdent.class, PasSubIdent.class, PasRefNamedIdent.class, PasNamedIdent.class, PasNamedIdentDecl.class, PasGenericTypeIdent.class,
                PsiWhiteSpace.class, PsiErrorElement.class);
        if (expr instanceof PasReferenceExpr) {
            if (expr.getParent() instanceof PasCallExpr || expr.getParent() instanceof PasIndexExpr) {
                expr = expr.getParent();
            }
            ExpressionProcessor expressionProcessor = new ExpressionProcessor(fqn, context, processor);
            expressionProcessor.resolveExprTypeScope((PascalExpression) expr, true);
        } else {
            final FQNResolver fqnResolver = new FQNResolver(null, fqn, context) {
                @Override
                boolean processField(final PasEntityScope scope, final PasField field) {
                    return processor.process(scope, scope, field, field.fieldType);
                }
            };
            fqnResolver.resolve(true);
        }
        return true;
    }

}
