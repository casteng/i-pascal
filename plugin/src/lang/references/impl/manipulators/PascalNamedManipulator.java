package com.siberika.idea.pascal.lang.references.impl.manipulators;

import com.intellij.openapi.editor.Document;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.AbstractElementManipulator;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.util.IncorrectOperationException;
import com.siberika.idea.pascal.lang.psi.PascalNamedElement;
import org.jetbrains.annotations.NotNull;

/**
 * Author: George Bakhtadze
 * Date: 23/08/2013
 */
public class PascalNamedManipulator extends AbstractElementManipulator<PascalNamedElement> {
    @Override
    public PascalNamedElement handleContentChange(@NotNull PascalNamedElement element, @NotNull TextRange range, String newContent) throws IncorrectOperationException {
        if ((element.getContainingFile() != null) && (element.getContainingFile().getVirtualFile() != null)) {
            @SuppressWarnings("ConstantConditions")
            final Document document = FileDocumentManager.getInstance().getDocument(element.getContainingFile().getVirtualFile());
            if (document != null) {
                document.replaceString(element.getTextRange().getStartOffset() + range.getStartOffset(), element.getTextRange().getStartOffset() + range.getEndOffset(), newContent);
                PsiDocumentManager.getInstance(element.getProject()).commitDocument(document);
            }
        }
        return element;
    }


}
