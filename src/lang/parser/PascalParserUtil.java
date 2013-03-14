package com.siberika.idea.pascal.lang.parser;

import com.intellij.lang.PsiBuilder;
import com.intellij.navigation.ItemPresentation;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.search.FileTypeIndex;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.indexing.FileBasedIndex;
import com.siberika.idea.pascal.PascalFileType;
import com.siberika.idea.pascal.PascalIcons;
import com.siberika.idea.pascal.lang.psi.PasGenericTypeIdent;
import com.siberika.idea.pascal.lang.psi.PascalNamedElement;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * Author: George Bakhtadze
 * Date: 12/9/12
 */
public class PascalParserUtil extends GeneratedParserUtilBase {
    public static boolean parsePascal(PsiBuilder builder_, int level, Parser parser) {
        builder_.setDebugMode(true);
        ErrorState state = ErrorState.get(builder_);
        return parseAsTree(state, builder_, level, DUMMY_BLOCK, true, parser, TRUE_CONDITION);
    }

    public static List<PascalNamedElement> findTypes(Project project, String key) {
        List<PascalNamedElement> result = null;
        Collection<VirtualFile> virtualFiles = FileBasedIndex.getInstance().getContainingFiles(FileTypeIndex.NAME, PascalFileType.INSTANCE,
                GlobalSearchScope.allScope(project));
        for (VirtualFile virtualFile : virtualFiles) {
            PascalFile pascalFile = (PascalFile) PsiManager.getInstance(project).findFile(virtualFile);
            if (pascalFile != null) {
                Collection<PasGenericTypeIdent> elements = PsiTreeUtil.findChildrenOfAnyType(pascalFile, PasGenericTypeIdent.class);
                if (elements != null) {
                    for (PascalNamedElement property : elements) {
                        if (key.equalsIgnoreCase(property.getName())) {
                            if (result == null) {
                                result = new ArrayList<PascalNamedElement>();
                            }
                            result.add(property);
                        }
                    }
                }
            }
        }
        return result != null ? result : Collections.<PascalNamedElement>emptyList();
    }

    public static List<PascalNamedElement> findTypes(Project project) {
        List<PascalNamedElement> result = null;
        Collection<VirtualFile> virtualFiles = FileBasedIndex.getInstance().getContainingFiles(FileTypeIndex.NAME, PascalFileType.INSTANCE,
                GlobalSearchScope.allScope(project));
        for (VirtualFile virtualFile : virtualFiles) {
            PascalFile pascalFile = (PascalFile) PsiManager.getInstance(project).findFile(virtualFile);
            if (pascalFile != null) {
                Collection<PasGenericTypeIdent> elements = PsiTreeUtil.findChildrenOfAnyType(pascalFile, PasGenericTypeIdent.class);
                if (elements != null) {
                    for (PascalNamedElement property : elements) {
                        if (result == null) {
                            result = new ArrayList<PascalNamedElement>();
                        }
                        result.add(property);
                    }
                }
            }
        }
        return result != null ? result : Collections.<PascalNamedElement>emptyList();
    }

    public static List<PascalNamedElement> findProperties(Project project, String key) {
        List<PascalNamedElement> result = null;
        Collection<VirtualFile> virtualFiles = FileBasedIndex.getInstance().getContainingFiles(FileTypeIndex.NAME, PascalFileType.INSTANCE,
                GlobalSearchScope.allScope(project));
        for (VirtualFile virtualFile : virtualFiles) {
            PascalFile pascalFile = (PascalFile) PsiManager.getInstance(project).findFile(virtualFile);
            if (pascalFile != null) {
                PascalNamedElement[] elements = PsiTreeUtil.getChildrenOfType(pascalFile, PascalNamedElement.class);
                if (elements != null) {
                    for (PascalNamedElement property : elements) {
                        if (key.equals(property.getName())) {
                            if (result == null) {
                                result = new ArrayList<PascalNamedElement>();
                            }
                            result.add(property);
                        }
                    }
                }
            }
        }
        return result != null ? result : Collections.<PascalNamedElement>emptyList();
    }

    public static List<PascalNamedElement> findProperties(Project project) {
        List<PascalNamedElement> result = null;
        Collection<VirtualFile> virtualFiles = FileBasedIndex.getInstance().getContainingFiles(FileTypeIndex.NAME, PascalFileType.INSTANCE,
                GlobalSearchScope.allScope(project));
        for (VirtualFile virtualFile : virtualFiles) {
            PascalFile pascalFile = (PascalFile) PsiManager.getInstance(project).findFile(virtualFile);
            if (pascalFile != null) {
                PascalNamedElement[] elements = PsiTreeUtil.getChildrenOfType(pascalFile, PascalNamedElement.class);
                if (elements != null) {
                    Collections.addAll(result, elements);
                }
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

}
