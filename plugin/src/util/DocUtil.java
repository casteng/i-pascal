package com.siberika.idea.pascal.util;

import com.intellij.codeInsight.template.Template;
import com.intellij.codeInsight.template.impl.TemplateImpl;
import com.intellij.codeInsight.template.impl.TextExpression;
import com.intellij.codeInsight.template.impl.Variable;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.Result;
import com.intellij.openapi.command.CommandProcessor;
import com.intellij.openapi.command.UndoConfirmationPolicy;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.ScrollType;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.TextRange;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.PsiWhiteSpace;
import com.intellij.psi.codeStyle.CodeStyleManager;
import com.intellij.util.FileContentUtil;
import com.intellij.util.SmartList;
import com.siberika.idea.pascal.PascalBundle;
import com.siberika.idea.pascal.lang.psi.PasModule;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Author: George Bakhtadze
 * Date: 08/12/2015
 */
public class DocUtil {
    public static final Pattern RE_LF = Pattern.compile("\n");
    public static final Pattern RE_WHITESPACE = Pattern.compile("\\s");
    public static final Pattern RE_SEMICOLON = Pattern.compile(";");
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

    // for each entry in DUP_MAP if content ends with key and document starts with value truncate content by key length
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

    public static void reformatInSeparateCommand(@NotNull final Project project, @NotNull final PsiFile file, @NotNull final Editor editor) {
        runCommandLaterInWriteAction(project, PascalBundle.message("action.reformat"), new Runnable() {
            @Override
            public void run() {
                PsiElement el = file.findElementAt(editor.getCaretModel().getOffset());
                el = PsiUtil.skipToExpressionParent(el);
                PsiManager manager = el != null ? el.getManager() : null;
                if ((el != null) && (manager != null)) {
                    CodeStyleManager.getInstance(manager).reformatRange(file, el.getTextRange().getStartOffset(), el.getTextRange().getEndOffset(), true);
                }
            }
        });
    }

    public static void reformat(@NotNull final PsiElement block, boolean inSeparateCommand) {
        Runnable r = getReformatCode(block);
        if (inSeparateCommand) {
            runCommandLaterInWriteAction(block.getProject(), PascalBundle.message("action.reformat"), r);
        } else {
            r.run();
        }
    }

    private static Runnable getReformatCode(final PsiElement element) {
        return new Runnable() {
            @Override
            public void run() {
                PsiManager manager = element.getManager();
                PsiElement parent = element.getParent();
                if ((manager != null) && (parent != null)) {
                    CodeStyleManager.getInstance(manager).reformatNewlyAddedElement(parent.getNode(), element.getNode());
                }
            }
        };
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

    public static Template createTemplate(String template, Map<String, String> defaults, boolean inline) {
        TemplateImpl tpl = new TemplateImpl("", template, "");
        tpl.setToIndent(false);
        tpl.setToReformat(false);
        tpl.setToShortenLongNames(true);
        List<Variable> vars = new SmartList<Variable>();
        for (int i = 0; i < tpl.getSegmentsCount(); i++) {
            String varName = tpl.getSegmentName(i);
            String def = defaults != null ? defaults.get(varName) : null;
            TextExpression expr = new TextExpression(def != null ? def : "");
            Variable var = new Variable(varName, expr, expr, true, false);
            vars.add(var);
        }
        tpl.removeAllParsed();
        for (Variable var : vars) {
            if (!tpl.getVariables().contains(var)) {
                tpl.addVariable(var.getName(), var.getExpression(), var.getDefaultValueExpression(), var.isAlwaysStopAt(), var.skipOnStart());
            }
        }

        tpl.parseSegments();
        tpl.setInline(inline);
        return tpl;
    }

    public static void reparsePsi(final Project project, final VirtualFile file) {
        ApplicationManager.getApplication().invokeLater(
                new Runnable() {
                    @Override
                    public void run() {
                        new WriteCommandAction(project) {
                            @Override
                            protected void run(@NotNull Result result) throws Throwable {
                                //final FileDocumentManager documentManager = FileDocumentManager.getInstance();
                                //((VirtualFileListener) documentManager).contentsChanged(new VirtualFileEvent(null, file, file.getName(), file.getParent()));
                                FileContentUtil.reparseFiles(file);
                            }
                        }.execute();
                    }
                }
        );
    }

    private static final Pattern PATTERN_TEMPLATE_VARIABLE = Pattern.compile("\\$\\w+\\$");
    // Removes all template variables from the given range of the document
    public static void removeTemplateVariables(Document document, TextRange textRange) {
        String text = document.getText(textRange);
        Matcher m = PATTERN_TEMPLATE_VARIABLE.matcher(text);
        int offs = textRange.getStartOffset();
        while (m.find()) {
            //ranges.add(TextRange.create(m.start(), m.end()));
            int len = m.end() - m.start();
            document.deleteString(offs + m.start(), offs + m.end());
            offs -= len;
        }
    }

    private static final Pattern EMPTY_TEXT = Pattern.compile("(\\s*(\\{.*}|\\(\\*.*\\*\\)))*\\s*");
    public static boolean isFirstOnLine(Editor editor, PsiElement element) {
        Document d = editor.getDocument();
        int offs = element.getTextRange().getStartOffset();
        int line = d.getLineNumber(offs);
        int lineStart = d.getLineStartOffset(line);
        String text = d.getText(TextRange.create(lineStart, offs));
        return EMPTY_TEXT.matcher(text).matches();
    }

    public static boolean isSingleLine(@NotNull Document document, @NotNull PsiElement element) {
        return isSingleLine(document, element.getTextRange());
    }

    public static boolean isSingleLine(@NotNull Document document, @NotNull TextRange range) {
        return document.getLineNumber(range.getStartOffset()) == document.getLineNumber(range.getEndOffset());
    }

    public static boolean isLineEmpty(Document document, PsiFile file, int line) {
        int start = document.getLineStartOffset(line);
        int end = document.getLineEndOffset(line);
        PsiElement startEl = file.findElementAt(start);
        return (startEl instanceof PsiWhiteSpace) && (startEl == file.findElementAt(end));
    }
}
