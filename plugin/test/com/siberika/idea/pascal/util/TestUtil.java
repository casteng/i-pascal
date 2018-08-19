package com.siberika.idea.pascal.util;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.search.FileTypeIndex;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.search.PsiElementProcessor;
import com.intellij.util.indexing.FileBasedIndex;
import com.siberika.idea.pascal.PascalFileType;
import com.siberika.idea.pascal.lang.parser.PascalFile;
import com.siberika.idea.pascal.lang.psi.PasConstDeclaration;
import com.siberika.idea.pascal.lang.psi.PasEntityScope;
import com.siberika.idea.pascal.lang.psi.PasGenericTypeIdent;
import com.siberika.idea.pascal.lang.psi.PasModule;
import com.siberika.idea.pascal.lang.psi.PasNamedIdent;
import com.siberika.idea.pascal.lang.psi.PasNamedIdentDecl;
import com.siberika.idea.pascal.lang.psi.PascalNamedElement;
import com.siberika.idea.pascal.lang.psi.PascalPsiElement;
import com.siberika.idea.pascal.lang.psi.PascalRoutine;
import com.siberika.idea.pascal.lang.psi.PascalStructType;
import com.siberika.idea.pascal.lang.psi.impl.PasField;
import com.siberika.idea.pascal.lang.references.PasReferenceUtil;
import com.siberika.idea.pascal.lang.references.ResolveContext;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Author: George Bakhtadze
 * Date: 03/12/2015
 */
public class TestUtil {
    public static PasEntityScope findClass(PasModule module, String name) {
        PasField parentField = module.getField(name);
        return PasReferenceUtil.retrieveFieldTypeScope(parentField, new ResolveContext(module, PasField.TYPES_TYPE, true, null, null));
    }

    @NotNull
    public static Collection<PascalNamedElement> findSymbols(final Project project, final String pattern) {
        final Set<PascalNamedElement> result = new HashSet<PascalNamedElement>();
        final Pattern p = Pattern.compile("\\w*" + pattern + "\\w*");
        processProjectElements(project, new PsiElementProcessor<PascalNamedElement>() {
            @Override
            public boolean execute(@NotNull PascalNamedElement element) {
                if (p.matcher(element.getName()).matches()) {
                    result.add(element);
                }
                return true;
            }
        }, PascalStructType.class, PasConstDeclaration.class, PascalRoutine.class, PasNamedIdent.class, PasNamedIdentDecl.class, PasGenericTypeIdent.class);
        return new ArrayList<PascalNamedElement>(result);
    }

    @NotNull
    public static <T extends PascalNamedElement> Collection<T> findSymbols(final Project project, final String pattern, Class<T> clazz) {
        final Set<T> result = new HashSet<T>();
        final Pattern p = Pattern.compile("\\w*" + pattern + "\\w*");
        processProjectElements(project, new PsiElementProcessor<T>() {
            @Override
            public boolean execute(@NotNull T element) {
                if (p.matcher(element.getName()).matches()) {
                    result.add(element);
                }
                return true;
            }
        }, clazz);
        return new ArrayList<T>(result);
    }

    /**
     * Handle all elements of the specified classes in project source (not in PPU) with the given processor
     */
    public static <T extends PascalPsiElement> void processProjectElements(Project project, PsiElementProcessor<T> processor, Class<? extends T>... clazz) {
        Collection<VirtualFile> virtualFiles = FileBasedIndex.getInstance().getContainingFiles(FileTypeIndex.NAME, PascalFileType.INSTANCE,
                GlobalSearchScope.allScope(project));
        for (VirtualFile virtualFile : virtualFiles) {
            PascalFile pascalFile = (PascalFile) PsiManager.getInstance(project).findFile(virtualFile);
            if (pascalFile != null) {
                for (T element : PsiUtil.findChildrenOfAnyType(pascalFile, clazz)) {
                    processor.execute(element);
                }
            }
        }
    }
}
