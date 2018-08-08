package com.siberika.idea.pascal.editor.highlighter;

import com.intellij.codeInsight.highlighting.ReadWriteAccessDetector;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import com.intellij.psi.util.PsiTreeUtil;
import com.siberika.idea.pascal.lang.context.ContextUtil;
import com.siberika.idea.pascal.lang.psi.PasTypes;
import com.siberika.idea.pascal.lang.psi.PascalIdentDecl;
import com.siberika.idea.pascal.lang.psi.PascalNamedElement;
import com.siberika.idea.pascal.lang.psi.impl.PasField;
import com.siberika.idea.pascal.util.PsiUtil;
import org.jetbrains.annotations.NotNull;

public class PascalReadWriteAccessDetector extends ReadWriteAccessDetector {

    public static final Logger LOG = Logger.getInstance(PascalReadWriteAccessDetector.class);

    @Override
    public boolean isReadWriteAccessible(@NotNull PsiElement element) {
        if (element instanceof PascalIdentDecl) {
            return ((PascalIdentDecl) element).getAccess() == PasField.Access.READWRITE;
        } else {
            return false;
        }
    }

    @Override
    public boolean isDeclarationWriteAccess(@NotNull PsiElement element) {
        if (element instanceof PascalIdentDecl) {
            return ((PascalIdentDecl) element).getValue() != null;
        } else {
            return false;
        }
    }

    @NotNull
    @Override
    public Access getReferenceAccess(@NotNull PsiElement referencedElement, @NotNull PsiReference reference) {
        return isWriteAccess(reference.getElement()) ? Access.Write : Access.Read;
    }

    static boolean isWriteAccess(PsiElement element) {
        if (element instanceof PascalNamedElement) {
            if (ContextUtil.isAssignLeftPart((PascalNamedElement) element)) {
                return true;
            } else {
                PsiElement next = PsiTreeUtil.skipSiblingsForward(element.getParent(), PsiUtil.ELEMENT_WS_COMMENTS);
                return (next != null) && (next.getNode().getElementType() == PasTypes.ASSIGN);
            }
        } else {
            return false;
        }
    }

    @NotNull
    @Override
    public Access getExpressionAccess(@NotNull PsiElement expression) {
        return Access.Read;
    }
}
