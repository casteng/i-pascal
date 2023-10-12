package com.siberika.idea.pascal.lang.references.impl;

import com.intellij.openapi.util.io.FileUtil;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.refactoring.rename.RenamePsiElementProcessor;
import com.siberika.idea.pascal.PascalFileType;
import com.siberika.idea.pascal.ide.actions.SectionToggle;
import com.siberika.idea.pascal.lang.psi.PasFormalParameter;
import com.siberika.idea.pascal.lang.psi.PasFormalParameterSection;
import com.siberika.idea.pascal.lang.psi.PasModule;
import com.siberika.idea.pascal.lang.psi.PascalNamedElement;
import com.siberika.idea.pascal.lang.psi.PascalRoutine;
import com.siberika.idea.pascal.util.PsiUtil;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

/**
 * Author: George Bakhtadze
 * Date: 20/03/2015
 */
public class PascalRenameProcessor extends RenamePsiElementProcessor {
    @Override
    public boolean canProcessElement(@NotNull PsiElement element) {
        return (element instanceof PsiFile) || (element instanceof PasModule)
            || (element instanceof PascalNamedElement) && (element.getParent() instanceof PasFormalParameter);
    }

    @Override
    public boolean isInplaceRenameSupported() {
        return true;
    }

    @Override
    public void prepareRenaming(PsiElement element, String newName, Map<PsiElement, String> allRenames) {
        if (element instanceof PsiFile) {
            PasModule module = PsiUtil.getElementPasModule(element);
            if (module != null && module.isValid()) {
                final String moduleName = FileUtil.getNameWithoutExtension(newName);
                allRenames.put(module, moduleName);
            }
        } else if (element instanceof PasModule) {
            PsiFile file = element.getContainingFile();
            if (file != null && file.isValid()) {
                final String fileName = FileUtil.getNameWithoutExtension(newName) + "." + PascalFileType.INSTANCE.getDefaultExtension();
                allRenames.put(file, fileName);
            }
        } else if (element.getParent() instanceof PasFormalParameter) {
            addRoutineRenames(element, allRenames, newName);
        }
    }

    private void addRoutineRenames(PsiElement element, Map<PsiElement, String> allRenames, String newName) {
        String name = ((PascalNamedElement) element).getName();
        if (element.getParent() instanceof PasFormalParameter) {
            PsiElement r = element.getParent().getParent().getParent();
            if (r instanceof PascalRoutine) {
                PsiElement routine = SectionToggle.getImplementationOrDeclaration((PascalRoutine) r);
                if (routine instanceof PascalRoutine) {
                    PasFormalParameterSection pars = ((PascalRoutine) routine).getFormalParameterSection();
                    if (pars != null) for (PasFormalParameter parameter : pars.getFormalParameterList()) {
                        for (PascalNamedElement ident : parameter.getNamedIdentDeclList()) {
                            if (name.equalsIgnoreCase(ident.getName())) {
                                allRenames.put(ident, newName);
                            }
                        }
                    }
                }
            }
        }
    }

}
