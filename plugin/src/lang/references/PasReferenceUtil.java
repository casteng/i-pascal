package com.siberika.idea.pascal.lang.references;

import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiManager;
import com.intellij.psi.search.FileTypeIndex;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.indexing.FileBasedIndex;
import com.siberika.idea.pascal.PascalFileType;
import com.siberika.idea.pascal.lang.parser.PascalFile;
import com.siberika.idea.pascal.lang.psi.PasEntityScope;
import com.siberika.idea.pascal.lang.psi.PasModule;
import com.siberika.idea.pascal.lang.psi.PasNamespaceIdent;
import com.siberika.idea.pascal.lang.psi.PascalNamedElement;
import com.siberika.idea.pascal.lang.psi.PascalQualifiedIdent;
import com.siberika.idea.pascal.lang.psi.impl.PasField;
import com.siberika.idea.pascal.util.PsiUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Author: George Bakhtadze
 * Date: 25/04/2013
 */
public class PasReferenceUtil {
    /**
     * Returns references of the given module. This can be module qualifier in identifiers and other modules.
     * @param moduleName - name element of module to find references for
     * @return array of references in nearest-first order
     */
    @SuppressWarnings("unchecked")
    public static List<PascalNamedElement> findUsedModuleReferences(@NotNull final PasNamespaceIdent moduleName) {
        final List<PascalNamedElement> result = new ArrayList<PascalNamedElement>();
        PascalNamedElement module = findUsedModule(moduleName);
        if (module != null) {
            result.add(module);
        }
        for (PascalQualifiedIdent element : PsiUtil.findChildrenOfAnyType(moduleName.getContainingFile(), PascalQualifiedIdent.class)) {
            if (moduleName.getName().equalsIgnoreCase(element.getNamespace())) {
                result.add(element);
            }
        }
        return result;
    }

    @Nullable
    public static PascalNamedElement findUsedModule(@NotNull final PasNamespaceIdent moduleName) {
        Collection<VirtualFile> virtualFiles = FileBasedIndex.getInstance().getContainingFiles(FileTypeIndex.NAME, PascalFileType.INSTANCE,
                GlobalSearchScope.allScope(moduleName.getProject()));
        for (VirtualFile virtualFile : virtualFiles) {
            if (isUnitWithName(virtualFile, moduleName.getName())) {
                PascalFile pascalFile = (PascalFile) PsiManager.getInstance(moduleName.getProject()).findFile(virtualFile);
                PasModule module = PsiTreeUtil.findChildOfType(pascalFile, PasModule.class);
                if (module != null) {
                    return module;
                }
            }
        }
        return null;
    }

    private static boolean isUnitWithName(VirtualFile virtualFile, String name) {
        return PascalFileType.UNIT_EXTENSION.equalsIgnoreCase(virtualFile.getExtension()) &&
                virtualFile.getNameWithoutExtension().equalsIgnoreCase(name);
    }

    public static Collection<LookupElement> getEntities(final PsiElement element, Set<PasField.Type> types) {
        Collection<LookupElement> result = new ArrayList<LookupElement>();
        PsiElement section = PsiUtil.getNearestAffectingDeclarationsRoot(element);
        doGetEntities(result, new HashSet<String>(), element.getTextRange().getStartOffset(), section, types);
        return result;
    }

    private static void doGetEntities(Collection<LookupElement> result, Set<String> filter, int startOffset, PsiElement section, Set<PasField.Type> types) {
        if (null == section) { return; }
        if (section instanceof PasEntityScope) {
            for (PasField field : ((PasEntityScope) section).getAllFields()) {
                if ((startOffset > field.element.getTextRange().getStartOffset()) &&
                    types.contains(field.type) && !filter.contains(field.name.toUpperCase())) {
                    result.add(LookupElementBuilder.createWithIcon(field.element));
                    filter.add(field.name.toUpperCase());
                }
            }
        }
        doGetEntities(result, filter, startOffset, PsiUtil.getNearestAffectingDeclarationsRoot(section), types);
    }
}
