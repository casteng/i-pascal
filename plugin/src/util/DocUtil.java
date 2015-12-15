package com.siberika.idea.pascal.util;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.command.CommandProcessor;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.ScrollType;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.codeStyle.CodeStyleManager;
import com.siberika.idea.pascal.PascalBundle;
import org.apache.commons.lang.StringUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * Author: George Bakhtadze
 * Date: 08/12/2015
 */
public class DocUtil {
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

    public static void adjustDocument(Editor editor, int offset, String content) {
        final Document document = editor.getDocument();
        int caretOffset = content.indexOf(PLACEHOLDER_CARET);
        content = content.replaceAll(PLACEHOLDER_CARET, "");
        document.insertString(offset, adjustContent(editor, offset, content));
        if (caretOffset >= 0) {
            editor.getCaretModel().moveToOffset(offset + caretOffset);
            editor.getScrollingModel().scrollToCaret(ScrollType.MAKE_VISIBLE);
        }
    }

    private static String adjustContent(Editor editor, int offset, String content) {
        final Document document = editor.getDocument();
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
        ApplicationManager.getApplication().invokeLater(new Runnable() {
            @Override
            public void run() {
                ApplicationManager.getApplication().runWriteAction(new Runnable() {
                    @Override
                    public void run() {
                        CommandProcessor.getInstance().executeCommand(project, new Runnable() {
                            @Override
                            public void run() {
                                PsiElement el = file.findElementAt(editor.getCaretModel().getOffset());
                                el = PsiUtil.skipToExpressionParent(el);
                                PsiManager manager = el != null ? el.getManager() : null;
                                if ((el != null) && (manager != null)) {
                                    CodeStyleManager.getInstance(manager).reformat(el, true);
                                }
                            }
                        }, PascalBundle.message("action.reformat"), null);
                    }
                });
            }
        });
    }

    public static void reformat(final PsiElement block) {
        ApplicationManager.getApplication().invokeLater(new Runnable() {
            @Override
            public void run() {
                ApplicationManager.getApplication().runWriteAction(new Runnable() {
                    @Override
                    public void run() {
                        CommandProcessor.getInstance().executeCommand(block.getProject(), new Runnable() {
                            @Override
                            public void run() {
                                PsiManager manager = block.getManager();
                                if (manager != null) {
                                    CodeStyleManager.getInstance(manager).reformat(block, true);
                                }
                            }
                        }, PascalBundle.message("action.reformat"), null);
                    }
                });
            }
        });
    }


}
