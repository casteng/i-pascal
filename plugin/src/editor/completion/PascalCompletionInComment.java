package com.siberika.idea.pascal.editor.completion;

import com.intellij.codeInsight.completion.CompletionParameters;
import com.intellij.codeInsight.completion.CompletionResultSet;
import com.intellij.codeInsight.completion.InsertHandler;
import com.intellij.codeInsight.completion.InsertionContext;
import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtilCore;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.siberika.idea.pascal.PascalBundle;
import com.siberika.idea.pascal.lang.lexer.PascalFlexLexerImpl;
import com.siberika.idea.pascal.sdk.BasePascalSdkType;
import com.siberika.idea.pascal.sdk.Define;
import com.siberika.idea.pascal.sdk.Directive;
import com.siberika.idea.pascal.util.DocUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Author: George Bakhtadze
 * Date: 04/09/2016
 */
class PascalCompletionInComment {
    private static final Pattern DIRECTIVE = Pattern.compile("\\{\\$?\\w*");
    private static final Pattern DIRECTIVE_PARAM = Pattern.compile("\\{(\\$\\w+)\\s+(\\w*)");
    private static final InsertHandler<LookupElement> INSERT_HANDLER_COMMENT = new InsertHandler<LookupElement>() {
        @Override
        public void handleInsert(final InsertionContext context, LookupElement item) {
            DocUtil.adjustDocument(context.getEditor(), context.getEditor().getCaretModel().getOffset(), DocUtil.PLACEHOLDER_CARET + "}");
        }
    };

    static void handleComments(CompletionResultSet result, CompletionParameters parameters) {
        PsiElement comment = parameters.getOriginalPosition();
        final boolean needClose = DocUtil.isSingleLine(parameters.getEditor().getDocument(), comment);
        int ofs = parameters.getOffset() - comment.getTextOffset();
        if (ofs <= 0) {
            return;
        }
        String text = comment.getText().substring(0, ofs);
        if (DIRECTIVE.matcher(text).matches()) {
            for (Map.Entry<String, Directive> entry : retrieveDirectives(comment).entrySet()) {
                result.addElement(LookupElementBuilder.create(entry.getKey() + (entry.getValue().hasParameters(entry.getKey()) ? " " : ""))
                        .withTypeText(entry.getValue().desc, true).withInsertHandler(INSERT_HANDLER_COMMENT));
            }
        } else {
            Matcher m = DIRECTIVE_PARAM.matcher(text);
            if (m.matches()) {
                String id = m.group(1);
                result = result.withPrefixMatcher(m.group(2));
                if (Directive.isDefine(id)) {
                    Map<String, Define> defines = retrieveDefines(comment.getContainingFile() != null ? comment.getContainingFile().getVirtualFile() : null, comment.getProject());
                    for (Define define : defines.values()) {
                        result.addElement(LookupElementBuilder.create(define.name).withCaseSensitivity(false).withTypeText(getDesc(define), true));
                    }
                } else {
                    Directive dir = retrieveDirectives(comment).get(id);
                    if ((dir != null) && (dir.values != null)) {
                        for (String value : dir.values) {
                            result.addElement(LookupElementBuilder.create(value + (needClose ? "}" : "")).withPresentableText(value));
                        }
                    }
                }
            }
        }
    }

    private static String getDesc(Define define) {
        if (define.virtualFile != null) {
            Document doc = FileDocumentManager.getInstance().getDocument(define.virtualFile);
            return define.virtualFile.getPath() + (doc != null ? ":" + doc.getLineNumber(define.offset) : "");
        }
        return define.offset < 0 ? PascalBundle.message("completion.defines.source.builtin") : PascalBundle.message("completion.defines.source.commandLine");
    }

    private static Map<String, Define> retrieveDefines(@Nullable VirtualFile file, @NotNull Project project) {
        PascalFlexLexerImpl lexer = PascalFlexLexerImpl.processFile(project, file);
        return lexer != null ? lexer.getAllDefines() : Collections.<String, Define>emptyMap();
    }

    private static Map<String, Directive> retrieveDirectives(PsiElement comment) {
        Module module = ModuleUtilCore.findModuleForPsiElement(comment);
        Sdk sdk = module != null ? ModuleRootManager.getInstance(module).getSdk() : null;
        return sdk != null ? BasePascalSdkType.getDirectives(sdk, sdk.getVersionString()) : Collections.<String, Directive>emptyMap();
    }

}
