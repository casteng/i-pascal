package com.siberika.idea.pascal.lang.parser;

import com.intellij.lang.ASTNode;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.stubs.PsiFileStub;
import com.intellij.psi.tree.IStubFileElementType;
import com.siberika.idea.pascal.PascalLanguage;
import com.siberika.idea.pascal.module.PascalProjectService;
import org.jetbrains.annotations.NotNull;

/**
 * Author: George Bakhtadze
 * Date: 27/10/2015
 */
public class PascalFileElementType extends IStubFileElementType<PsiFileStub<PascalFile>> {
    public PascalFileElementType(String debugName, PascalLanguage language) {
        super(debugName, language);
    }

    @Override
    public int getStubVersion() {
        return getStubIndexVersion();
    }

    public static int getStubIndexVersion() {
        return 111;
    }

    @Override
    protected ASTNode doParseContents(@NotNull ASTNode chameleon, @NotNull PsiElement psi) {
        Project project = psi.getProject();
        PascalProjectService service = project.getComponent(PascalProjectService.class);
        // store file being parsed to retrieve in lexer
        service.setData(PascalProjectService.KEY_PARSING, psi.getContainingFile().getVirtualFile());
        return super.doParseContents(chameleon, psi);
    }
}
