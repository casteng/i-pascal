package com.siberika.idea.pascal.lang.parser;

import com.intellij.lang.PsiBuilder;
import com.intellij.navigation.ItemPresentation;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.impl.source.resolve.FileContextUtil;
import com.intellij.psi.search.FileTypeIndex;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.search.PsiElementProcessor;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.indexing.FileBasedIndex;
import com.siberika.idea.pascal.PascalFileType;
import com.siberika.idea.pascal.PascalIcons;
import com.siberika.idea.pascal.lang.psi.PasClassMethod;
import com.siberika.idea.pascal.lang.psi.PasClosureExpression;
import com.siberika.idea.pascal.lang.psi.PasGenericTypeIdent;
import com.siberika.idea.pascal.lang.psi.PasMethodDecl;
import com.siberika.idea.pascal.lang.psi.PasNamedIdent;
import com.siberika.idea.pascal.lang.psi.PasNamespaceIdent;
import com.siberika.idea.pascal.lang.psi.PasProcedureType;
import com.siberika.idea.pascal.lang.psi.PasQualifiedIdent;
import com.siberika.idea.pascal.lang.psi.PasRoutineDecl;
import com.siberika.idea.pascal.lang.psi.PasStrucType;
import com.siberika.idea.pascal.lang.psi.PasSubIdent;
import com.siberika.idea.pascal.lang.psi.PasTypeDecl;
import com.siberika.idea.pascal.lang.psi.PascalModuleHead;
import com.siberika.idea.pascal.lang.psi.PascalNamedElement;
import com.siberika.idea.pascal.lang.psi.PascalPsiElement;
import com.siberika.idea.pascal.lang.psi.impl.PasGenericTypeIdentImpl;
import com.siberika.idea.pascal.lang.psi.impl.PasSubIdentImpl;
import com.siberika.idea.pascal.lang.psi.impl.PasTypeIDImpl;
import com.siberika.idea.pascal.lang.references.PasReferenceUtil;
import com.siberika.idea.pascal.util.PsiUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Author: George Bakhtadze
 * Date: 12/9/12
 */
@SuppressWarnings("unchecked")
public class PascalParserUtil extends GeneratedParserUtilBase {
    public static boolean parsePascal(PsiBuilder builder_, int level, Parser parser) {
        PsiFile file = builder_.getUserDataUnprotected(FileContextUtil.CONTAINING_FILE_KEY);
        if ((file != null) && (file.getVirtualFile() != null)) {
            //noinspection ConstantConditions
            System.out.println("Parse: " + file.getVirtualFile().getName());
        }
        //builder_.setDebugMode(true);
        ErrorState state = ErrorState.get(builder_);
        return parseAsTree(state, builder_, level, DUMMY_BLOCK, true, parser, TRUE_CONDITION);
    }

    @SuppressWarnings("unchecked")
    @Deprecated
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

    @SuppressWarnings("unchecked")
    private static List<PascalNamedElement> findTypes(PsiElement element, final String key) {
        List<PascalNamedElement> result = retrieveSortedAffectingEntitiesDecl(element, key, PasGenericTypeIdent.class);
        return result;
    }

    private static boolean isSameAffectingScope(PsiElement innerSection, PsiElement outerSection) {
        for (int i = 0; i < 4; i++) {
            if (innerSection == outerSection) {
                return true;
            }
            if ((null == innerSection) || isInstanceOfAny(innerSection, PasStrucType.class, PasRoutineDecl.class, PasMethodDecl.class,
                    PasClassMethod.class, PasProcedureType.class, PasClosureExpression.class)) {
                return false;
            }
            innerSection = PsiUtil.getNearestAffectingDeclarationsRoot(innerSection);
        }
        return false;
    }

    private static <T extends PsiElement> boolean isInstanceOfAny(PsiElement object, Class<? extends T>...classes) {
        int i = classes.length - 1;
        while ((i >= 0) && (!classes[i].isInstance(object))) {
            i--;
        }
        return i >= 0;
    }

    private static List<PascalNamedElement> findVariables(NamespaceRec namespaces) {
        //System.out.println("*** findvars: " + key + ", ns: " + namespaces.levels.size());
        List<PascalNamedElement> result = new ArrayList<PascalNamedElement>();
        if (!namespaces.isEmpty()) {
            List<PascalNamedElement> entitiesDecl = retrieveSortedAffectingEntitiesDecl(namespaces.getCurrent(), namespaces.getCurrent().getName(), PasNamedIdent.class);
            for (PascalNamedElement entity : entitiesDecl) {
                doFindVariables(result, entity, namespaces);
            }
        }
        return result;
    }

    private static void doFindVariables(List<PascalNamedElement> result, PascalNamedElement entityDecl, NamespaceRec namespaces) {
        //System.out.println("*** doFindVars: " + entityDecl.getName() + ", ns: " + namespaces.getCurrent());
        if (namespaces.isTarget()) {
            result.add(entityDecl);
        } else {
            assert namespaces.getCurrent() != null;
            namespaces.next();
            PsiElement section = retrieveNamespace(entityDecl);
            if (section != null) {
                List<PascalNamedElement> entities = retrieveEntitiesFromSection(section, namespaces.getCurrent().getName(),
                        section.getTextOffset() + section.getTextLength(), PasNamedIdent.class);
                if (!entities.isEmpty()) {
                    doFindVariables(result, entities.get(0), namespaces); //TODO: all entities
                }
            }
        }
    }

    private static PsiElement retrieveNamespace(PascalNamedElement entityDecl) {
        PasTypeDecl typeDecl = PsiTreeUtil.getNextSiblingOfType(entityDecl, PasTypeDecl.class);
        if (typeDecl != null) {          // type declaration case
            //System.out.println("*** structured type case: " + typeDecl);
            PascalNamedElement typeName = PsiTreeUtil.findChildOfType(typeDecl, PasQualifiedIdent.class, true);
            if (typeName != null) {
                List<PascalNamedElement> strucTypes = retrieveSortedAffectingEntitiesDecl(typeName, typeName.getName(), PasGenericTypeIdent.class);
                if (!strucTypes.isEmpty()) {
                    PasTypeDecl strucTypeDecl = PsiTreeUtil.getNextSiblingOfType(strucTypes.get(0), PasTypeDecl.class);
                    if (strucTypeDecl != null) {
                        //System.out.println("*** found type: " + strucTypeDecl);
                        return PsiTreeUtil.findChildOfType(strucTypeDecl, PasStrucType.class, true);
                    }
                }
            }
        }
        return null;
    }

    /**
     * Returns list of entities matching the specified key and classes which may affect the element
     * @param element - element which should be affected by returned named entities
     * @param key - key which should match entities names
     * @param classes - classes of entities to retrieve
     * @return list of entities sorted in such a way that entity nearest to element comes first
     */
    private static <T extends PascalNamedElement> List<PascalNamedElement> retrieveSortedAffectingEntitiesDecl(PsiElement element, String key, Class<T>...classes) {
        List<PascalNamedElement> result = retrieveEntitiesFromSection(PsiUtil.getNearestAffectingDeclarationsRoot(element), key, element.getTextOffset(), classes);
        // nearest to element should be first
        Collections.sort(result, new Comparator<PascalNamedElement>() {
            @Override
            public int compare(PascalNamedElement o1, PascalNamedElement o2) {
                return o2.getTextOffset() - o1.getTextOffset();
            }
        });
        return result;
    }

    // returns list of
    private static <T extends PascalNamedElement> List<PascalNamedElement> retrieveEntitiesFromSection(PsiElement section, String key, int maxOffset, Class<T>...classes) {
        //System.out.println("get \"" + key + "\" in " + section);
        final List<PascalNamedElement> result = new ArrayList<PascalNamedElement>();
        if (section != null) {
            for (PascalNamedElement namedElement : PsiUtil.findChildrenOfAnyType(section, classes)) {
                if (((null == key) || key.equalsIgnoreCase(namedElement.getName()))) {
                    if ((namedElement.getTextOffset() < maxOffset) && isSameAffectingScope(PsiUtil.getNearestAffectingDeclarationsRoot(namedElement), section)) {
                        result.add(namedElement);
                    } else {
                        //System.out.println("not match in: " + PsiUtil.getNearestAffectingDeclarationsRoot(namedElement));
                    }
                }
            }
            result.addAll(retrieveEntitiesFromSection(PsiUtil.getNearestAffectingDeclarationsRoot(section), key, maxOffset, classes));
        }
        return result;
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
        System.out.println("*** el: " + element + ", key: " + key);
        List<PascalNamedElement> result = new ArrayList<PascalNamedElement>();
        PasNamespaceIdent usedModule = getUsedModuleName(element);
        if (usedModule != null) {
            return PasReferenceUtil.findUsedModuleReferences(usedModule);
        } else if (isType(element)) {
            result.addAll(findTypes(element, key));
        } else if (isEntity(element)) {
            result.addAll(findTypes(element, key));
            if (element instanceof PasSubIdent) {
                result.addAll(findVariables(new NamespaceRec((PasSubIdent) element)));
            }
            //result.addAll(findConstants(element, key));
            //List<PascalNamedElement> modules = findModules(element, key);
        }
        return result;
    }

    private static boolean isEntity(PsiElement element) {
        return element.getClass() == PasSubIdentImpl.class;
    }

    private static boolean isType(PsiElement element) {
        return (element.getClass() == PasGenericTypeIdentImpl.class) || (element.getParent().getClass() == PasTypeIDImpl.class);
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

}
