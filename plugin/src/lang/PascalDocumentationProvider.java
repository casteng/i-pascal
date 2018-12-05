package com.siberika.idea.pascal.lang;

import com.intellij.lang.documentation.DocumentationMarkup;
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
import com.siberika.idea.pascal.ide.actions.SectionToggle;
import com.siberika.idea.pascal.lang.compiled.CompiledFileImpl;
import com.siberika.idea.pascal.lang.psi.PasClassProperty;
import com.siberika.idea.pascal.lang.psi.PasConstExpression;
import com.siberika.idea.pascal.lang.psi.PasFormalParameter;
import com.siberika.idea.pascal.lang.psi.PasGenericTypeIdent;
import com.siberika.idea.pascal.lang.psi.PasNamedIdentDecl;
import com.siberika.idea.pascal.lang.psi.PasParamType;
import com.siberika.idea.pascal.lang.psi.PasTypeDecl;
import com.siberika.idea.pascal.lang.psi.PascalExportedRoutine;
import com.siberika.idea.pascal.lang.psi.PascalIdentDecl;
import com.siberika.idea.pascal.lang.psi.PascalNamedElement;
import com.siberika.idea.pascal.lang.psi.PascalRoutine;
import com.siberika.idea.pascal.lang.psi.impl.HasUniqueName;
import com.siberika.idea.pascal.lang.psi.impl.PasField;
import com.siberika.idea.pascal.util.PsiUtil;
import kotlin.reflect.jvm.internal.impl.utils.SmartList;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
        StringBuilder sb = new StringBuilder();
        String comment = findElementComment(file, element);
        if (comment != null) {
            sb.append(comment);
            if (element instanceof PascalExportedRoutine) {
                if (comment.endsWith(DOC_LF)) {
                    sb.delete(sb.length() - DOC_LF.length(), sb.length());
                }
                PsiElement impl = SectionToggle.retrieveImplementation((PascalRoutine) element, true);
                String commentImpl = impl != null ? findElementComment(file, impl) : null;
                if (commentImpl != null) {
                    sb.append(DOC_LF).append(commentImpl);
                }
            }
        }
        return comment != null ? formatDocumentation(file, getQuickNavigateInfo(element, originalElement), sb.toString()) : null;
    }

    private String formatDocumentation(PsiFile file, String quickNavigateInfo, String text) {
        return
                DocumentationMarkup.DEFINITION_START +
                        quickNavigateInfo +
                        DocumentationMarkup.DEFINITION_END
                        + DocumentationMarkup.CONTENT_START
                        + text
                        + DocumentationMarkup.CONTENT_END
                ;
    }

    /* Search for comments above element, empty line breaks the search
       If not found search for comment at the end of line where the element starts */
    private static String findElementComment(PsiFile file, PsiElement element) {
        if (file instanceof CompiledFileImpl) {
            return null;
        }
        TextRange range = findElementCommentRange(file, element);
        return range != null ? formatComment(file.getText().substring(range.getStartOffset(), range.getEndOffset())) : null;
    }

    public static TextRange findElementCommentRange(PsiFile file, PsiElement element) {
        List<PsiElement> elements = findElementCommentElements(file, element);
        return elements.isEmpty() ? TextRange.EMPTY_RANGE : TextRange.create(elements.get(0).getTextRange().getStartOffset(), elements.get(elements.size()-1).getTextRange().getEndOffset());
    }

    @NotNull
    public static List<PsiElement> findElementCommentElements(PsiFile file, PsiElement element) {
        List<PsiElement> res = new SmartList<>();
        Document doc = PsiDocumentManager.getInstance(element.getProject()).getDocument(file);
        if (null == doc) {
            return res;
        }
        PsiElement el = PsiTreeUtil.prevVisibleLeaf(element);
        int line = doc.getLineNumber(element.getTextRange().getStartOffset());
        while (el instanceof PsiComment) {
            TextRange range = el.getTextRange();
            if ((line - doc.getLineNumber(range.getEndOffset())) < 2) {
                line = doc.getLineNumber(range.getStartOffset());
                res.add(el);
                el = PsiTreeUtil.prevVisibleLeaf(el);
            } else {
                break;
            }
        }
        if (res.isEmpty()) {
            int offs = doc.getLineEndOffset(line);
            el = file.findElementAt(offs - 1);
            if (el instanceof PsiComment) {
                res.add(el);
            }
        }
        return res;
    }

    private static final String[] COMMENT_STARTS = {"{", "(*"};
    private static final String[] COMMENT_ENDS = {"}", "*)"};
    private static final Pattern LINE_START = Pattern.compile("\\s*(//)|(\\*)");

    private static String formatComment(String comment) {
        String[] lines = handleStarts(handleEnds(comment)).split("\\s*\\n");
        StringBuilder sb = new StringBuilder();
        for (String line : lines) {
            if (sb.length() != 0) {
                sb.append(DOC_LF);
            }
            sb.append(handleLineStarts(line));
        }
        return sb.toString();
    }

    private static String handleLineStarts(String line) {
        Matcher m = LINE_START.matcher(line);
        if (m.find()) {
            return line.substring(m.group(0).length());
        }
        return line;
    }

    private static String handleStarts(String comment) {
        for (String commentStart : COMMENT_STARTS) {
            if (comment.startsWith(commentStart)) {
                return comment.substring(commentStart.length());
            }
        }
        return comment;
    }

    private static String handleEnds(String comment) {
        for (String commentEnd : COMMENT_ENDS) {
            if (comment.endsWith(commentEnd)) {
                return comment.substring(0, comment.length() - commentEnd.length());
            }
        }
        return comment;
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
