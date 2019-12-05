package com.siberika.idea.pascal.lang;

import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.lang.annotation.ExternalAnnotator;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtilCore;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.roots.OrderRootType;
import com.intellij.openapi.util.TextRange;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.tree.TokenSet;
import com.siberika.idea.pascal.jps.compiler.CompilerMessager;
import com.siberika.idea.pascal.jps.compiler.PascalBackendCompiler;
import com.siberika.idea.pascal.jps.sdk.PascalCompilerFamily;
import com.siberika.idea.pascal.jps.sdk.PascalSdkData;
import com.siberika.idea.pascal.lang.psi.PasTypes;
import com.siberika.idea.pascal.module.ModuleService;
import com.siberika.idea.pascal.sdk.BasePascalSdkType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class PascalExternalAnnotator extends ExternalAnnotator<PascalAnnotatorInfo, PascalSyntaxCheckResult> implements DumbAware {

    public static final Logger LOG = Logger.getInstance(PascalExternalAnnotator.class.getName());

    @Nullable
    @Override
    public PascalAnnotatorInfo collectInformation(@NotNull PsiFile file, @NotNull Editor editor, boolean hasErrors) {
        Module module = ModuleUtilCore.findModuleForFile(file);
        Sdk sdk = module != null ? ModuleRootManager.getInstance(module).getSdk() : null;
        final PascalSdkData sdkData = sdk != null ? BasePascalSdkType.getAdditionalData(sdk) : null;
        if (null == sdkData) {
            return null;
        }
        return sdkData.isSyntaxCheckEnabled(hasErrors) ? new PascalAnnotatorInfo(file, editor.getDocument().getLineCount()) : null;
    }

    @Nullable
    @Override
    public PascalSyntaxCheckResult doAnnotate(PascalAnnotatorInfo collectedInfo) {
        Module module = ModuleUtilCore.findModuleForFile(collectedInfo.getFile());
        if (null == module) {
            return null;
        }
        Sdk sdk = ModuleRootManager.getInstance(module).getSdk();
        final PascalSdkData sdkData = sdk != null ? BasePascalSdkType.getAdditionalData(sdk) : null;
        String family = sdkData != null ? sdkData.getString(PascalSdkData.Keys.COMPILER_FAMILY) : null;
        PascalBackendCompiler compiler = family != null ? PascalBackendCompiler.getCompiler(PascalCompilerFamily.of(family), CompilerMessager.NO_OP_MESSAGER) : null;
        File tempDir = module.getComponent(ModuleService.class).getSyntaxCheckTempDir();
        final ArrayList<String> commandLine = new ArrayList<>();
        final boolean perform = (compiler != null) && compiler.createSyntaxCheckCommandImpl(sdk.getHomePath(), collectedInfo.getFile().getVirtualFile().getPath(), sdkData,
                collectSourcePaths(sdk, module), commandLine, tempDir.getAbsolutePath());
        if (!perform) {
            return null;
        }
        try {
            Document doc = PsiDocumentManager.getInstance(collectedInfo.getFile().getProject()).getDocument(collectedInfo.getFile());
            if (doc != null) {
                ApplicationManager.getApplication().invokeAndWait(() -> FileDocumentManager.getInstance().saveDocumentAsIs(doc));
                PascalSyntaxCheckResult messager = new PascalSyntaxCheckResult(collectedInfo);
                compiler.launch(messager, commandLine.toArray(new String[0]), tempDir);
                return messager;
            } else {
                return null;
            }
        } catch (IOException e) {
            LOG.error("Error launching external annotator", e);
            return null;
        }
    }

    private VirtualFile[] collectSourcePaths(Sdk sdk, Module module) {
        List<VirtualFile[]> urlLists = new ArrayList<>();
        urlLists.add(sdk.getRootProvider().getFiles(OrderRootType.CLASSES));
        urlLists.add(sdk.getRootProvider().getFiles(OrderRootType.SOURCES));
        urlLists.add(ModuleRootManager.getInstance(module).getSourceRoots());
        for (Module dependency : ModuleRootManager.getInstance(module).getDependencies()) {
            urlLists.add(ModuleRootManager.getInstance(dependency).getSourceRoots());
        }
        int count = 0;
        for (VirtualFile[] urlList : urlLists) {
            count += urlList.length;
        }
        VirtualFile[] result = new VirtualFile[count];
        int copied = 0;
        for (VirtualFile[] urlList : urlLists) {
            System.arraycopy(urlList, 0, result, copied, urlList.length);
            copied += urlList.length;
        }
        return result;
    }

    @Override
    public void apply(@NotNull PsiFile file, PascalSyntaxCheckResult annotationResult, @NotNull AnnotationHolder holder) {
        Document doc = PsiDocumentManager.getInstance(file.getProject()).getDocument(file);
        if ((null == doc) || (annotationResult.getResults() == null)) {
            return;
        }
        for (PascalSyntaxCheckResult.AnnotationItem result : annotationResult.getResults()) {
            final TextRange range = getRange(file, doc, result);
            if (result.severity == PascalSyntaxCheckResult.SEVERITY.ERROR) {
                holder.createErrorAnnotation(range, result.msg);
            } else if (result.severity == PascalSyntaxCheckResult.SEVERITY.WARNING) {
                holder.createWarningAnnotation(range, result.msg);
            } else if (result.severity == PascalSyntaxCheckResult.SEVERITY.HINT) {
                holder.createWeakWarningAnnotation(range, result.msg);
            } else {
                holder.createInfoAnnotation(range, result.msg);
            }
        }
    }

    private TextRange getRange(PsiFile file, Document doc, PascalSyntaxCheckResult.AnnotationItem result) {
        TokenSet SYMBOLS = TokenSet.create(PasTypes.LBRACK, PasTypes.LPAREN, PasTypes.RPAREN, PasTypes.RBRACK,
                PasTypes.SEMI, PasTypes.COMMA, PasTypes.COLON);
        int offset = (int) (doc.getLineStartOffset((int) result.line) + result.column);
        int end = offset + 1;
        PsiElement el = file.findElementAt(offset);
        if (el != null) {
            IElementType type = el.getNode().getElementType();
            if (SYMBOLS.contains(type) && offset > 0) {
                el = file.findElementAt(offset-1);
            }
        }
        return el != null ? el.getTextRange() : TextRange.create(offset, end);
    }

}
