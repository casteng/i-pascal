package com.siberika.idea.pascal.lang;

import com.intellij.lang.documentation.DocumentationProvider;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiComment;
import com.intellij.psi.PsiCompiledFile;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.util.PsiTreeUtil;
import com.siberika.idea.pascal.lang.psi.PasClassProperty;
import com.siberika.idea.pascal.lang.psi.PasConstExpression;
import com.siberika.idea.pascal.lang.psi.PasFormalParameter;
import com.siberika.idea.pascal.lang.psi.PasGenericTypeIdent;
import com.siberika.idea.pascal.lang.psi.PasNamedIdentDecl;
import com.siberika.idea.pascal.lang.psi.PasParamType;
import com.siberika.idea.pascal.lang.psi.PasTypeDecl;
import com.siberika.idea.pascal.lang.psi.PascalIdentDecl;
import com.siberika.idea.pascal.lang.psi.PascalNamedElement;
import com.siberika.idea.pascal.lang.psi.PascalRoutine;
import com.siberika.idea.pascal.lang.psi.impl.HasUniqueName;
import com.siberika.idea.pascal.lang.psi.impl.PasField;
import com.siberika.idea.pascal.util.PsiUtil;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class PascalDocumentationProvider implements DocumentationProvider {

    private static final String DOC_LF = "<br/>";

    @Nullable
    @Override
    public String getQuickNavigateInfo(PsiElement element, PsiElement originalElement) {
        if (element instanceof PasGenericTypeIdent) {
            element = element.getFirstChild();
        }
        StringBuilder sb = new StringBuilder();
        String kind = "Identifier";
        String name = "?";
        String type = null;
        String value = null;
        if (element instanceof PasClassProperty) {
            element = ((PasClassProperty) element).getNamedIdentDecl();
        }
        if (element instanceof PascalNamedElement) {
            PasField.FieldType fieldType = ((PascalNamedElement) element).getType();
            if ((fieldType == PasField.FieldType.ROUTINE) && (element instanceof PascalRoutine)) {
                if (StringUtils.isEmpty(((PascalRoutine) element).getFunctionTypeStr())) {
                    kind = "procedure";
                } else {
                    kind = "function";
                }
            } else {
                kind = fieldType.name().toLowerCase();
            }
            if (element instanceof PascalIdentDecl) {
                type = ((PascalIdentDecl) element).getTypeString();
            }
        }
        if (element instanceof PasNamedIdentDecl) {
            value = ((PasNamedIdentDecl) element).getValue();
        }
        if (element instanceof HasUniqueName) {
            name = ((HasUniqueName) element).getUniqueName();
        } else if (element instanceof PascalNamedElement) {
            name = ((PascalNamedElement) element).getName();
            if (PsiUtil.isFormalParameterName((PascalNamedElement) element)) {
                PasFormalParameter formalParameter = (PasFormalParameter) element.getParent();
                PasTypeDecl typeDecl = formalParameter.getTypeDecl();
                type = typeDecl != null ? typeDecl.getText() : PsiUtil.TYPE_UNTYPED_NAME;
                PasParamType mod = formalParameter.getParamType();
                name = (mod != null ? mod.getText() + " " : "") + name;
                PasConstExpression valueConst = formalParameter.getConstExpression();
                value = valueConst != null ? valueConst.getText() : null;
            }
        }
        sb.append(kind).append(" ").append(name.replace("<", "&lt;"));
        if (!StringUtils.isEmpty(type)) {
            sb.append(": ").append(type.replace("<", "&lt;"));
        }
        if (!StringUtils.isEmpty(value)) {
            sb.append(" = ").append(value);
        }
        return sb.toString();
    }

    @Nullable
    @Override
    public List<String> getUrlFor(PsiElement element, PsiElement originalElement) {
        return null;
    }

    @Nullable
    @Override
    public String generateDoc(PsiElement element, @Nullable PsiElement originalElement) {
        PsiFile file = element.getContainingFile();
        if (element instanceof PsiCompiledFile) {
            return null;
        }
        TextRange commentRange = findElementComment(file, element);
        return commentRange.isEmpty() ? null : formatDocumentation(file, getQuickNavigateInfo(element, originalElement), file.getText().substring(commentRange.getStartOffset(), commentRange.getEndOffset()));
    }

    private String formatDocumentation(PsiFile file, String quickNavigateInfo, String text) {
        return quickNavigateInfo + DOC_LF + text.replace("\n", DOC_LF);
    }

    /* Search for comments above element, empty line breaks the search
       If not found search for comment at the end of line where the element starts */
    private static TextRange findElementComment(PsiFile file, PsiElement element) {
        int start = element.getTextRange().getStartOffset();
        int end = start;
        Document doc = PsiDocumentManager.getInstance(element.getProject()).getDocument(file);
        if (null == doc) {
            return TextRange.EMPTY_RANGE;
        }
        PsiElement el = PsiTreeUtil.prevVisibleLeaf(element);
        int line = doc.getLineNumber(start);
        while (el instanceof PsiComment) {
            if ((line - doc.getLineNumber(el.getTextRange().getEndOffset())) < 2) {
                start = el.getTextRange().getStartOffset();
                line = doc.getLineNumber(start);
                el = PsiTreeUtil.prevVisibleLeaf(el);
            } else {
                break;
            }
        }
        if (start >= end) {
            int offs = doc.getLineEndOffset(line);
            el = file.findElementAt(offs - 1);
            if (el instanceof PsiComment) {
                start = el.getTextRange().getStartOffset();
                end = el.getTextRange().getEndOffset();
            }
        }
        return TextRange.create(start, end);
    }

    @Nullable
    @Override
    public PsiElement getDocumentationElementForLookupItem(PsiManager psiManager, Object object, PsiElement element) {
        return null;
    }

    @Nullable
    @Override
    public PsiElement getDocumentationElementForLink(PsiManager psiManager, String link, PsiElement context) {
        return null;
    }
}
