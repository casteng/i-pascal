package com.siberika.idea.pascal.lang.parser;

import com.intellij.lang.PsiBuilder;
import com.intellij.navigation.ItemPresentation;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiManager;
import com.intellij.psi.search.FileTypeIndex;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.search.PsiElementProcessor;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.indexing.FileBasedIndex;
import com.siberika.idea.pascal.PascalFileType;
import com.siberika.idea.pascal.PascalIcons;
import com.siberika.idea.pascal.lang.psi.PasGenericTypeIdent;
import com.siberika.idea.pascal.lang.psi.PasNamedIdent;
import com.siberika.idea.pascal.lang.psi.PasNamespaceIdent;
import com.siberika.idea.pascal.lang.psi.PascalModuleHead;
import com.siberika.idea.pascal.lang.psi.PascalNamedElement;
import com.siberika.idea.pascal.lang.psi.PascalPsiElement;
import com.siberika.idea.pascal.lang.references.PasReferenceUtil;
import com.siberika.idea.pascal.util.PsiUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Author: George Bakhtadze
 * Date: 12/9/12
 */
public class PascalParserUtil extends GeneratedParserUtilBase {
    public static boolean parsePascal(PsiBuilder builder_, int level, Parser parser) {
        //builder_.setDebugMode(true);
        ErrorState state = ErrorState.get(builder_);
        return parseAsTree(state, builder_, level, DUMMY_BLOCK, true, parser, TRUE_CONDITION);
    }

    public static List<PascalNamedElement> findTypes(Project project) {
        final List<PascalNamedElement> result = new ArrayList<PascalNamedElement>();
        processProjectElements(project, new PsiElementProcessor<PasGenericTypeIdent>() {
            @Override
            public boolean execute(@NotNull PasGenericTypeIdent element) {
                result.add(element);
                return true;
            }
        }, PasGenericTypeIdent.class);
        return result;
    }

    public static List<PascalNamedElement> findTypes(PsiElement element, final String key) {
        return retrieveTypesFromSection(PsiUtil.getOuterScopeDecl(element), key, PasGenericTypeIdent.class);
    }

    private static <T extends PascalNamedElement> List<PascalNamedElement> retrieveTypesFromSection(PsiElement section, String key, Class<T>...classes) {
        final List<PascalNamedElement> result = new ArrayList<PascalNamedElement>();
        if (section != null) {
            for (PascalNamedElement namedElement : PsiUtil.findChildrenOfAnyType(section, classes)) {
                if ((null == key) || key.equalsIgnoreCase(namedElement.getName())) {
                    result.add(namedElement);
                }
            }
            result.addAll(retrieveTypesFromSection(PsiUtil.getOuterScopeDecl(section), key, classes));
        }
        return result;
    }

    public static List<PascalNamedElement> findConstants(PsiElement element, final String key) {
        return retrieveTypesFromSection(PsiUtil.getOuterScopeDecl(element), key, PasNamedIdent.class);
    }

    public static List<PascalNamedElement> findVariables(PsiElement element, final String key) {
        return retrieveTypesFromSection(PsiUtil.getOuterScopeDecl(element), key, PasNamedIdent.class);
    }

    public static List<PascalNamedElement> findModules(PsiElement element, final String key) {
        List<PascalNamedElement> result = new ArrayList<PascalNamedElement>();
        Collection<PascalModuleHead> head = PsiTreeUtil.findChildrenOfType(element.getContainingFile(), PascalModuleHead.class);
        for (PascalModuleHead el : head) {
            if ((null == key) || key.equalsIgnoreCase(el.getName())) {
                result.add(el);
            }
        }
        return result;
    }

    public static ItemPresentation getPresentation(final PascalNamedElement element) {
        return new ItemPresentation() {
            @Nullable
            @Override
            public String getPresentableText() {
                return element.getName();
            }

            @Nullable
            @Override
            public String getLocationString() {
                return element.getContainingFile().getName();
            }

            @Nullable
            @Override
            public Icon getIcon(boolean unused) {
                return PascalIcons.GENERAL;
            }
        };
    }

    /**
     * Handle all elements of the specified classes in project with the given processor
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

    @NotNull
    public static List<PascalNamedElement> findAllReferences(PsiElement element, String key) {
        PasNamespaceIdent usedModule = getUsedModuleName(element);
        if (usedModule != null) {
            return PasReferenceUtil.findUsedModuleReferences(usedModule);
        }
        List<PascalNamedElement> types = findTypes(element, key);
        List<PascalNamedElement> consts = findConstants(element, key);
        List<PascalNamedElement> vars = findVariables(element, key);
        List<PascalNamedElement> modules = findModules(element, key);
        List<PascalNamedElement> result = new ArrayList<PascalNamedElement>(types.size() + consts.size() + vars.size() + modules.size());
        result.addAll(types);
        result.addAll(consts);
        result.addAll(vars);
        result.addAll(modules);
        return result;
    }

    private static PasNamespaceIdent getUsedModuleName(PsiElement element) {
        if (element instanceof PasNamespaceIdent) {
            return (PasNamespaceIdent) element;
        } else if (element.getParent() instanceof PasNamespaceIdent) {
            return (PasNamespaceIdent) element.getParent();
        } else {
            return null;
        }
    }

    @NotNull
    public static List<PascalNamedElement> findNamespaceElements(PsiElement element, String key) {
        return null;
    }
}
