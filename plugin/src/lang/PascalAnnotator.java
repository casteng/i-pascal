package com.siberika.idea.pascal.lang;

import com.intellij.codeInsight.CodeInsightUtilBase;
import com.intellij.codeInsight.intention.IntentionAction;
import com.intellij.lang.ASTNode;
import com.intellij.lang.annotation.Annotation;
import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.lang.annotation.Annotator;
import com.intellij.lang.annotation.HighlightSeverity;
import com.intellij.lexer.Lexer;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.HighlighterColors;
import com.intellij.openapi.editor.colors.EditorColorsManager;
import com.intellij.openapi.editor.colors.TextAttributesKey;
import com.intellij.openapi.editor.markup.TextAttributes;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.tree.IElementType;
import com.siberika.idea.pascal.editor.highlighter.PascalSyntaxHighlighter;
import com.siberika.idea.pascal.lang.parser.PascalParserUtil;
import com.siberika.idea.pascal.lang.psi.PascalNamedElement;
import com.siberika.idea.pascal.lang.psi.PascalPsiElement;
import com.siberika.idea.pascal.util.PsiUtil;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

/**
 * Author: George Bakhtadze
 * Date: 12/14/12
 */
public class PascalAnnotator implements Annotator {

    public void annotate(@NotNull PsiElement element, @NotNull AnnotationHolder holder) {
        if (PsiUtil.isEntity(element)) {
            PascalNamedElement namedElement = (PascalNamedElement) element;
            Collection<PascalNamedElement> refs = PascalParserUtil.findAllReferences(element, namedElement.getName());
            if (refs.isEmpty()) {
                holder.createErrorAnnotation(element, "Undeclared identifier");
            }
        }
    }

    private static void highlightTokens(final PascalPsiElement property, final ASTNode node, final AnnotationHolder holder, PascalSyntaxHighlighter highlighter) {
        Lexer lexer = highlighter.getHighlightingLexer();
        final String s = node.getText();
        lexer.start(s);

        while (lexer.getTokenType() != null) {
            IElementType elementType = lexer.getTokenType();
            TextAttributesKey[] keys = highlighter.getTokenHighlights(elementType);
            for (TextAttributesKey key : keys) {
                //Pair<String, HighlightSeverity> pair = PascalSyntaxHighlighter.DISPLAY_NAMES.get(key);
                String displayName = "disp name";//===*** pair.getFirst();
                HighlightSeverity severity = HighlightSeverity.ERROR;//pair.getSecond();
                if (severity != null) {
                    int start = lexer.getTokenStart() + node.getTextRange().getStartOffset();
                    int end = lexer.getTokenEnd() + node.getTextRange().getStartOffset();
                    TextRange textRange = new TextRange(start, end);
                    final Annotation annotation;
                    if (severity == HighlightSeverity.WARNING) {
                        annotation = holder.createWarningAnnotation(textRange, displayName);
                    } else if (severity == HighlightSeverity.ERROR) {
                        annotation = holder.createErrorAnnotation(textRange, displayName);
                    } else {
                        annotation = holder.createInfoAnnotation(textRange, displayName);
                    }
                    TextAttributes attributes = EditorColorsManager.getInstance().getGlobalScheme().getAttributes(key);
                    annotation.setEnforcedTextAttributes(attributes);
                    if (key == HighlighterColors.BAD_CHARACTER) {
                        annotation.registerFix(new IntentionAction() {
                            @NotNull
                            public String getText() {
                                return "unescape";
                            }

                            @NotNull
                            public String getFamilyName() {
                                return getText();
                            }

                            public boolean isAvailable(@NotNull Project project, Editor editor, PsiFile file) {
                                if (!property.isValid() || !property.getManager().isInProject(property)) return false;

                                String text = property.getContainingFile().getContainingFile().getText();
                                int startOffset = annotation.getStartOffset();
                                return text.length() > startOffset && text.charAt(startOffset) == '\\';
                            }

                            public void invoke(@NotNull Project project, Editor editor, PsiFile file) {
                                if (!CodeInsightUtilBase.prepareFileForWrite(file)) return;
                                int offset = annotation.getStartOffset();
                                if (property.getContainingFile().getText().charAt(offset) == '\\') {
                                    editor.getDocument().deleteString(offset, offset + 1);
                                }
                            }

                            public boolean startInWriteAction() {
                                return true;
                            }
                        });
                    }
                }
            }
            lexer.advance();
        }
    }
}
