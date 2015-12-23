package com.siberika.idea.pascal.util;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.command.CommandProcessor;
import com.intellij.openapi.command.UndoConfirmationPolicy;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.ScrollType;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.codeStyle.CodeStyleManager;
import com.siberika.idea.pascal.PascalBundle;
import com.siberika.idea.pascal.lang.psi.PasModule;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Author: George Bakhtadze
 * Date: 08/12/2015
 */
public class DocUtil {
    public static final Pattern RE_LF = Pattern.compile("\n");
    public static final Pattern RE_WHITESPACE = Pattern.compile("\\s");
    private static final Map<String, String> DUP_MAP = getDupMap();
    public static final String PLACEHOLDER_CARET = "__CARET__";
    private static final int SPACES = 3;

    private static Map<String, String> getDupMap() {
        HashMap<String, String> res = new HashMap<String, String>();
        res.put("()", "(");                                         // don't add "()" if there is already "("
        res.put(";", ";");
        res.put("end", "end");
        res.put("end;", "end");
        return res;
    }

    // Adjusts content and inserts it into document placing cursor to placeholder position
    public static void adjustDocument(Editor editor, int offset, String content) {
        final Document document = editor.getDocument();
        int caretOffset = content.indexOf(PLACEHOLDER_CARET);
        content = content.replaceAll(PLACEHOLDER_CARET, "");
        adjustDocument(document, offset, content);
        if (caretOffset >= 0) {
            editor.getCaretModel().moveToOffset(offset + caretOffset);
            editor.getScrollingModel().scrollToCaret(ScrollType.MAKE_VISIBLE);
        }
    }

    // Adjusts content and inserts it into document
    public static void adjustDocument(Document document, int offset, String content) {
        document.insertString(offset, adjustContent(document, offset, content));
    }

    private static String adjustContent(Document document, int offset, String content) {
        for (Map.Entry<String, String> entry : DUP_MAP.entrySet()) {
            String trimmedContent = StringUtils.stripEnd(content, null);
            if (trimmedContent.endsWith(entry.getKey())) {
                TextRange r = TextRange.from(offset, entry.getValue().length() + SPACES);
                if (r.getEndOffset() > document.getTextLength()) {
                    r = TextRange.create(r.getStartOffset(), document.getTextLength());
                }
                String trimmedDoc = StringUtils.stripStart(document.getText(r), " ");
                if (trimmedDoc.startsWith(entry.getValue())) {
                    return content.substring(0, trimmedContent.length() - entry.getKey().length());
                }
            }
        }
        return content;
    }

    public static void reformatInSeparateCommand(final Project project, final PsiFile file, final Editor editor) {
        runCommandLaterInWriteAction(project, PascalBundle.message("action.reformat"), new Runnable() {
            @Override
            public void run() {
                PsiElement el = file.findElementAt(editor.getCaretModel().getOffset());
                el = PsiUtil.skipToExpressionParent(el);
                PsiManager manager = el != null ? el.getManager() : null;
                if ((el != null) && (manager != null)) {
                    CodeStyleManager.getInstance(manager).reformat(el, true);
                }
            }
        });
    }

    public static void reformat(final PsiElement block) {
        runCommandLaterInWriteAction(block.getProject(), PascalBundle.message("action.reformat"), new Runnable() {
            @Override
            public void run() {
                PsiManager manager = block.getManager();
                if (manager != null) {
                    CodeStyleManager.getInstance(manager).reformat(block, true);
                }
            }
        });
    }

    public static int expandRangeEnd(Document doc, int endOffset, Pattern pattern) {
        while ((endOffset < doc.getTextLength()) && (pattern.matcher(doc.getText(TextRange.create(endOffset, endOffset+1)))).matches()) {
            endOffset++;
        }
        return endOffset;
    }

    // Expands range's start for symbols matching pattern
    public static int expandRangeStart(Document doc, int start, Pattern pattern) {
        while ((start > 0) && (pattern.matcher(doc.getText(TextRange.create(start-1, start)))).matches()) {
            start--;
        }
        return start;
    }

    public static void reformatRange(final PasModule module, final int start, final int end) {
        runCommandLaterInWriteAction(module.getProject(), PascalBundle.message("action.reformat"), new Runnable() {
            @Override
            public void run() {
                PsiManager manager = module.getManager();
                if (manager != null) {
                    CodeStyleManager.getInstance(manager).reformatRange(module, start, end, true);
                }
            }
        });
    }

    public static void runCommandLaterInWriteAction(@NotNull final Project project, @NotNull final String name, final Runnable runnable) {
        ApplicationManager.getApplication().invokeLater(new Runnable() {
            @Override
            public void run() {
                ApplicationManager.getApplication().runWriteAction(new Runnable() {
                    @Override
                    public void run() {
                        CommandProcessor.getInstance().executeCommand(project, runnable, name, null, UndoConfirmationPolicy.DO_NOT_REQUEST_CONFIRMATION);
                    }
                });
            }
        });
    }

    public static Document getDocument(PsiElement parent) {
        PsiFile file = parent != null ? parent.getContainingFile() : null;
        return file != null ? PsiDocumentManager.getInstance(parent.getProject()).getDocument(file) : null;
    }

}
