package com.siberika.idea.pascal.lang.references;

import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiErrorElement;
import com.intellij.psi.PsiWhiteSpace;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.usages.impl.rules.UsageType;
import com.intellij.usages.impl.rules.UsageTypeProvider;
import com.siberika.idea.pascal.PascalBundle;
import com.siberika.idea.pascal.editor.highlighter.PascalReadWriteAccessDetector;
import com.siberika.idea.pascal.lang.context.CodePlace;
import com.siberika.idea.pascal.lang.context.Context;
import com.siberika.idea.pascal.lang.parser.NamespaceRec;
import com.siberika.idea.pascal.lang.psi.PasCallExpr;
import com.siberika.idea.pascal.lang.psi.PasFullyQualifiedIdent;
import com.siberika.idea.pascal.lang.psi.PasGenericTypeIdent;
import com.siberika.idea.pascal.lang.psi.PasNamedIdent;
import com.siberika.idea.pascal.lang.psi.PasNamedIdentDecl;
import com.siberika.idea.pascal.lang.psi.PasRefNamedIdent;
import com.siberika.idea.pascal.lang.psi.PasReferenceExpr;
import com.siberika.idea.pascal.lang.psi.PasSubIdent;
import com.siberika.idea.pascal.lang.psi.PasTypeDecl;
import com.siberika.idea.pascal.lang.psi.PasTypeID;
import com.siberika.idea.pascal.lang.psi.PascalRoutine;
import com.siberika.idea.pascal.lang.psi.impl.PasField;
import com.siberika.idea.pascal.lang.references.resolve.Resolve;
import com.siberika.idea.pascal.util.PsiUtil;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.atomic.AtomicReference;

public class PascalUsageTypeProvider implements UsageTypeProvider {

    private static final UsageType USAGE_PARENT = new UsageType(PascalBundle.message("usage.type.parent"));
    private static final UsageType USAGE_VAR_DECL = new UsageType(PascalBundle.message("usage.type.declaration.var"));
    private static final UsageType USAGE_PARAMETER_DECL = new UsageType(PascalBundle.message("usage.type.declaration.parameter"));
    private static final UsageType USAGE_FIELD_DECL = new UsageType(PascalBundle.message("usage.type.declaration.field"));
    private static final UsageType USAGE_PROPERTY_DECL = new UsageType(PascalBundle.message("usage.type.declaration.property"));
    private static final UsageType USAGE_TYPE_DECL = new UsageType(PascalBundle.message("usage.type.declaration.type"));
    private static final UsageType USAGE_CONST_DECL = new UsageType(PascalBundle.message("usage.type.declaration.const"));
    private static final UsageType USAGE_EXCEPT = new UsageType(PascalBundle.message("usage.type.except"));
    private static final UsageType USAGE_ROUTINE_CALL = new UsageType(PascalBundle.message("usage.type.call"));

    @Nullable
    @Override
    public UsageType getUsageType(PsiElement element) {
        PsiElement parent = PsiTreeUtil.skipParentsOfType(element,
                PasFullyQualifiedIdent.class, PasSubIdent.class, PasRefNamedIdent.class, PasNamedIdent.class, PasNamedIdentDecl.class, PasGenericTypeIdent.class,
                PsiWhiteSpace.class, PsiErrorElement.class);
        Context context = new Context(element, null, null);
        if (PascalReadWriteAccessDetector.isWriteAccess(element)) {
            return UsageType.WRITE;
        } else if (context.getPrimary() == CodePlace.TYPE_ID) {
            if (context.contains(CodePlace.STRUCT_PARENT)) {
                return USAGE_PARENT;
            } else if (context.contains(CodePlace.DECL_VAR)) {
                return USAGE_VAR_DECL;
            } else if (context.contains(CodePlace.FORMAL_PARAMETER)) {
                return USAGE_PARAMETER_DECL;
            } else if (context.contains(CodePlace.DECL_FIELD)) {
                return USAGE_FIELD_DECL;
            } else if (context.contains(CodePlace.DECL_PROPERTY)) {
                return USAGE_PROPERTY_DECL;
            } else if (context.contains(CodePlace.DECL_TYPE)) {
                return USAGE_TYPE_DECL;
            } else if (context.contains(CodePlace.DECL_CONST)) {
                return USAGE_CONST_DECL;
            } else if (context.contains(CodePlace.STMT_EXCEPT)) {
                return USAGE_EXCEPT;
            } else if ((parent instanceof PasTypeID) && (parent.getParent() instanceof PasTypeDecl) && (parent.getParent().getParent() instanceof PascalRoutine)) {
                return UsageType.CLASS_METHOD_RETURN_TYPE;
            }
        } else if (context.contains(CodePlace.USES)) {
            return UsageType.CLASS_IMPORT;
        } else if (parent instanceof PasReferenceExpr) {
            AtomicReference<UsageType> result = new AtomicReference<>();
            Resolve.resolveExpr(NamespaceRec.fromElement(((PasReferenceExpr) parent).getFullyQualifiedIdent()), new ResolveContext(PasField.TYPES_ROUTINE, PsiUtil.isFromLibrary(element)),
                    (originalScope, scope, field, type) -> {
                        if (field.isConstructor()) {
                            result.set(UsageType.CLASS_NEW_OPERATOR);
                            return false;
                        }
                        return true;
                    }
            );
            if (result.get() != null) {
                return result.get();
            } else if (parent.getParent() instanceof PasCallExpr) {
                return USAGE_ROUTINE_CALL;
            } else {
                return UsageType.READ;
            }
        } else if (context.getPrimary() == CodePlace.EXPR) {
            return UsageType.READ;
        }
        return null;
    }
}
