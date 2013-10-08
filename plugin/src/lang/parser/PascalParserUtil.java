package com.siberika.idea.pascal.lang.parser;

import com.intellij.lang.PsiBuilder;
import com.intellij.navigation.ItemPresentation;
import com.intellij.openapi.diagnostic.Logger;
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
import com.siberika.idea.pascal.lang.psi.PasClassField;
import com.siberika.idea.pascal.lang.psi.PasClassHelperDecl;
import com.siberika.idea.pascal.lang.psi.PasClassProperty;
import com.siberika.idea.pascal.lang.psi.PasClassTypeDecl;
import com.siberika.idea.pascal.lang.psi.PasClosureExpression;
import com.siberika.idea.pascal.lang.psi.PasEntityScope;
import com.siberika.idea.pascal.lang.psi.PasFormalParameter;
import com.siberika.idea.pascal.lang.psi.PasFullyQualifiedIdent;
import com.siberika.idea.pascal.lang.psi.PasGenericTypeIdent;
import com.siberika.idea.pascal.lang.psi.PasInterfaceTypeDecl;
import com.siberika.idea.pascal.lang.psi.PasMethodImplDecl;
import com.siberika.idea.pascal.lang.psi.PasModule;
import com.siberika.idea.pascal.lang.psi.PasNamedIdent;
import com.siberika.idea.pascal.lang.psi.PasNamespaceIdent;
import com.siberika.idea.pascal.lang.psi.PasObjectDecl;
import com.siberika.idea.pascal.lang.psi.PasRecordDecl;
import com.siberika.idea.pascal.lang.psi.PasRecordField;
import com.siberika.idea.pascal.lang.psi.PasRecordHelperDecl;
import com.siberika.idea.pascal.lang.psi.PasRefNamedIdent;
import com.siberika.idea.pascal.lang.psi.PasRoutineImplDecl;
import com.siberika.idea.pascal.lang.psi.PasSubIdent;
import com.siberika.idea.pascal.lang.psi.PasTypeDecl;
import com.siberika.idea.pascal.lang.psi.PasTypeDeclaration;
import com.siberika.idea.pascal.lang.psi.PasTypeID;
import com.siberika.idea.pascal.lang.psi.PasVarSection;
import com.siberika.idea.pascal.lang.psi.PascalNamedElement;
import com.siberika.idea.pascal.lang.psi.PascalPsiElement;
import com.siberika.idea.pascal.lang.psi.PascalQualifiedIdent;
import com.siberika.idea.pascal.lang.psi.impl.PasField;
import com.siberika.idea.pascal.lang.psi.impl.PascalRoutineImpl;
import com.siberika.idea.pascal.lang.references.PasReferenceUtil;
import com.siberika.idea.pascal.sdk.BuiltinsParser;
import com.siberika.idea.pascal.util.PsiUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

/**
 * Author: George Bakhtadze
 * Date: 12/9/12
 */
@SuppressWarnings("unchecked")
public class PascalParserUtil extends GeneratedParserUtilBase {
    private static final Logger LOG = Logger.getInstance(PascalParserUtil.class);

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
    private static Collection<PascalNamedElement> findTypes(PsiElement element, final String key) {
        Collection<PascalNamedElement> result = retrieveSortedVisibleEntitiesDecl(element, key, PasGenericTypeIdent.class);
        return result;
    }

    private static boolean isSameAffectingScope(PsiElement innerSection, PsiElement outerSection) {
        for (int i = 0; i < 4; i++) {
            if (innerSection == outerSection) {
                return true;
            }
            if ((null == innerSection) || PsiUtil.isInstanceOfAny(innerSection,
                    PasClassTypeDecl.class, PasClassHelperDecl.class, PasInterfaceTypeDecl.class, PasObjectDecl.class, PasRecordDecl.class, PasRecordHelperDecl.class,
                    PasClosureExpression.class, PascalRoutineImpl.class)) {
                return false;
            }
            innerSection = PsiUtil.getNearestAffectingDeclarationsRoot(innerSection);
        }
        return false;
    }

    private static Collection<PascalNamedElement> findVariables(NamespaceRec namespaces, Class<? extends PascalNamedElement>...classes) {
        Collection<PascalNamedElement> result = new LinkedHashSet<PascalNamedElement>();
        if (!namespaces.isEmpty()) {
            Collection<PascalNamedElement> entitiesDecl = retrieveSortedVisibleEntitiesDecl(namespaces.getCurrent(), namespaces.getCurrent().getName(), classes);
            if ("result".equalsIgnoreCase(namespaces.getCurrent().getName()) && entitiesDecl.isEmpty()) {
                retrieveFunctionResultReference(entitiesDecl, namespaces.getCurrent());
            }
            if (entitiesDecl.isEmpty()) {
                retrieveDefaultNamespaceEntities(entitiesDecl, namespaces.getCurrent());
            }
            for (PascalNamedElement entity : entitiesDecl) {
                doFindVariables(result, entity, namespaces);
            }
            if (result.isEmpty()) {
                retrieveBuiltinReferences(result, namespaces.getParentIdent());
            }
        }
        return result;
    }

    private static void retrieveBuiltinReferences(Collection<PascalNamedElement> result, PascalQualifiedIdent ident) {
        if (null == ident) { return; }
        for (PasField field : BuiltinsParser.getBuiltins()) {
            if (field.name.equalsIgnoreCase(ident.getName())) {
                PasModule module = PsiUtil.getModule(ident);
                result.add(module != null ? module : ident);
                return;
            }
        }
    }

    private static void retrieveFunctionResultReference(Collection<PascalNamedElement> result, PascalNamedElement current) {
        PsiElement section = PsiUtil.getNearestAffectingDeclarationsRoot(current);
        if ((section instanceof PasRoutineImplDecl) || (section instanceof PasMethodImplDecl)) {
            result.add(((PascalRoutineImpl) section));
        }
    }

    private static void retrieveDefaultNamespaceEntities(Collection<PascalNamedElement> result, PascalNamedElement current) {
        PsiElement section = PsiUtil.getNearestAffectingDeclarationsRoot(current);
        if (section instanceof PasMethodImplDecl) {
            // add class declarations
            for (PsiElement element : section.getChildren()) {
                if (element instanceof PascalQualifiedIdent) {
                    Collection<PascalNamedElement> entities = retrieveEntitiesFromSection(section, ((PascalQualifiedIdent) element).getNamespace(),
                            getEndOffset(section), PasGenericTypeIdent.class);
                    for (PascalNamedElement namedElement : entities) {
                        addDeclarations(result, getStructTypeByIdent(namedElement), current.getName());
                    }
                }
            }
            if ("self".equalsIgnoreCase(current.getName())) {
                retrieveMethodSelfReference(result, current);
            }
        }
        if (result.isEmpty()) {
            addUsedUnitDeclarations(result, current);
        }
    }

    private static void retrieveMethodSelfReference(Collection<PascalNamedElement> result, PascalNamedElement self) {
        PsiElement section = PsiUtil.getNearestAffectingDeclarationsRoot(self);
        if (section instanceof PasMethodImplDecl) {
            PasModule module = PsiUtil.getModule(section);
            if (module != null) {
                PasField field = module.getField(((PasMethodImplDecl) section).getNamespace());
                if ((field != null) && (field.type == PasField.Type.TYPE)) {
                    result.add(field.element);
                    return;
                }
            }
        }
    }

    /**
     * add used unit interface declarations to result
     * @param result list of declarations to add unit declarations to
     * @param current element which should be affected by a unit declaration in order to be added to result
     */
    private static void addUsedUnitDeclarations(Collection<PascalNamedElement> result, PascalNamedElement current) {
        for (PasNamespaceIdent usedUnitName : PsiUtil.getUsedUnits(current.getContainingFile())) {
            PascalNamedElement usedUnit = PasReferenceUtil.findUsedModule(usedUnitName);
            if (usedUnit != null) {
                addDeclarations(result, PsiUtil.getModuleInterfaceSection(usedUnit), current.getName());
            }
        }
    }

    /**
     * Add all declarations of entities with matching names from the specified section to result
     * @param result list of declarations to add declarations to
     * @param section section containing declarations
     * @param name name which a declaration should match
     */
    private static void addDeclarations(Collection<PascalNamedElement> result, PsiElement section, String name) {
        if (section != null) {
            result.addAll(retrieveEntitiesFromSection(section, name, getEndOffset(section),
                    PasNamedIdent.class, PasGenericTypeIdent.class, PasNamespaceIdent.class));
        }
    }

    private static void doFindVariables(Collection<PascalNamedElement> result, PascalNamedElement entityDecl, NamespaceRec namespaces) {
        if (namespaces.isTarget()) {
            result.add(entityDecl);
        } else {
            assert namespaces.getCurrent() != null;
            PsiElement section = retrieveNamespace(entityDecl, namespaces.isFirst());
            // Check if the new section is another unit and use its interface section in this case
            if ((section instanceof PasModule) && (section.getContainingFile() != entityDecl.getContainingFile())) {
                section = PsiUtil.getModuleInterfaceSection(section);
            }

            if (section != null) {
                namespaces.next();
                Collection<PascalNamedElement> entities = retrieveEntitiesFromSection(section, namespaces.getCurrent().getName(),
                        getEndOffset(section), PasNamedIdent.class, PasGenericTypeIdent.class);
                for (PascalNamedElement element : entities) {
                    doFindVariables(result, element, namespaces);
                }
                namespaces.prev();
            }
        }
    }

    private final static int getEndOffset(PsiElement section) {
        return section.getTextRange().getEndOffset();
    }

    private static PsiElement retrieveNamespace(PascalNamedElement entityDecl, boolean canBeUnit) {
        if (canBeUnit && (entityDecl instanceof PasNamespaceIdent)) {
            PasNamespaceIdent usedModuleName = getUsedModuleName(entityDecl);
            if (usedModuleName != null) {
                PascalNamedElement unit = PasReferenceUtil.findUsedModule(usedModuleName);
                if (unit != null) {
                    return unit;
                }
            }
        }
        if (canBeUnit && (entityDecl instanceof PascalRoutineImpl)) {
            PasFullyQualifiedIdent typeName = ((PascalRoutineImpl) entityDecl).getFunctionTypeIdent();
            if (typeName != null) {
                for (PascalNamedElement strucTypeIdent : findVariables(new NamespaceRec(typeName, null), PasGenericTypeIdent.class, PasNamespaceIdent.class)) {
                    return getStructTypeByIdent(strucTypeIdent);
                }
            }
        }
        if (isVariableDecl(entityDecl) || isFieldDecl(entityDecl) || isPropertyDecl(entityDecl)) { // variable declaration case
            PascalPsiElement varDecl = PsiTreeUtil.getNextSiblingOfType(entityDecl, PasTypeDecl.class);
            if (null == varDecl) {
                varDecl = PsiTreeUtil.getNextSiblingOfType(entityDecl, PasTypeID.class);
            }
            if (varDecl != null) {
                PascalNamedElement typeIdent = PsiTreeUtil.findChildOfType(varDecl, PascalQualifiedIdent.class, true);
                if (typeIdent != null) {
                    for (PascalNamedElement strucTypeIdent : findVariables(new NamespaceRec((PascalQualifiedIdent) typeIdent, null), PasGenericTypeIdent.class, PasNamespaceIdent.class)) {
                        return getStructTypeByIdent(strucTypeIdent);
                    }
                }
            }
        } else if (entityDecl.getParent() instanceof PasTypeDeclaration) {                                    // type declaration case
            return getStructTypeByIdent(entityDecl);
        } else if (entityDecl.getParent() instanceof PascalRoutineImpl) {                                    // type declaration case
            PasFullyQualifiedIdent typeIdent = ((PascalRoutineImpl) entityDecl.getParent()).getFunctionTypeIdent();
            if (typeIdent != null) {
                for (PascalNamedElement strucTypeIdent : findVariables(new NamespaceRec(typeIdent, null), PasGenericTypeIdent.class, PasNamespaceIdent.class)) {
                    return getStructTypeByIdent(strucTypeIdent);
                }
            }
        }

        return null;
    }

    @Nullable
    private static PascalNamedElement getStructTypeByIdent(@NotNull PascalNamedElement typeIdent) {
        PasTypeDecl typeDecl = PsiTreeUtil.getNextSiblingOfType(typeIdent, PasTypeDecl.class);
        if (typeDecl != null) {
            PasEntityScope strucTypeDecl = PsiTreeUtil.findChildOfType(typeDecl, PasEntityScope.class, true);
            if (strucTypeDecl != null) {   // structured type
                return strucTypeDecl;
            } else {                       // regular type
                PasFullyQualifiedIdent typeId = PsiTreeUtil.findChildOfType(typeDecl, PasFullyQualifiedIdent.class, true);
                if (typeId != null) {
                    PsiElement section = PsiUtil.getNearestAffectingDeclarationsRoot(typeIdent);
                    Collection<PascalNamedElement> entities = retrieveEntitiesFromSection(section, typeId.getName(),
                            getEndOffset(section), PasGenericTypeIdent.class);
                    for (PascalNamedElement element : entities) {
                        return getStructTypeByIdent(element);
                    }
                    return null;
                } else {
                    return null;
                }
            }
        } else {
            return null;
        }
    }

    private static boolean isFieldDecl(PascalNamedElement entityDecl) {
        return (entityDecl.getParent() instanceof PasRecordField) || (entityDecl.getParent() instanceof PasClassField);
    }

    private static boolean isPropertyDecl(PascalNamedElement entityDecl) {
        return (entityDecl.getParent() instanceof PasClassProperty);
    }

    /**
     * Checks if the entityDecl is a declaration of variable or formal parameter
     * @param entityDecl entity declaration to check
     * @return true if the entityDecl is a declaration of variable or formal parameter
     */
    private static boolean isVariableDecl(PascalNamedElement entityDecl) {
        return (entityDecl.getParent() instanceof PasVarSection) || (entityDecl.getParent() instanceof PasFormalParameter);
    }

    /**
     * Returns list of entities matching the specified key and classes which may be visible from the element
     * @param element - element which should be affected by returned named entities
     * @param key - key which should match entities names
     * @param classes - classes of entities to retrieve
     * @return list of entities sorted in such a way that entity nearest to element comes first
     */
    private static <T extends PascalNamedElement> Collection<PascalNamedElement> retrieveSortedVisibleEntitiesDecl(PsiElement element, String key, Class<? extends T>... classes) {
        Collection<PascalNamedElement> result = new TreeSet<PascalNamedElement>(new Comparator<PascalNamedElement>() {
            @Override
            public int compare(PascalNamedElement o1, PascalNamedElement o2) {
                return o2.getTextRange().getStartOffset() - o1.getTextRange().getStartOffset();
            }
        });
        int offset = element.getTextRange().getStartOffset();
        if ((element instanceof PascalNamedElement) && PsiUtil.isPointerTypeDeclaration((PascalNamedElement) element)) {
            offset = element.getContainingFile().getTextLength();
        }
        result.addAll(retrieveEntitiesFromSection(PsiUtil.getNearestAffectingDeclarationsRoot(element), key, offset, classes));
        return result;
    }

    private static <T extends PascalNamedElement> Collection<PascalNamedElement> retrieveEntitiesFromSection(PsiElement section, String key, int maxOffset, Class<? extends T>...classes) {
        //System.out.println("get \"" + key + "\" in " + section);
        final Set<PascalNamedElement> result = new LinkedHashSet<PascalNamedElement>();
        if (section != null) {
            for (PascalNamedElement namedElement : PsiUtil.findChildrenOfAnyType(section, classes)) {
                if (((null == key) || key.equalsIgnoreCase(namedElement.getName()))) {
                    if ((namedElement.getTextRange().getStartOffset() < maxOffset) && isSameAffectingScope(PsiUtil.getNearestAffectingDeclarationsRoot(namedElement), section)) {
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

    public static ItemPresentation getPresentation(final PascalNamedElement element) {
        return new ItemPresentation() {
            @Nullable
            @Override
            public String getPresentableText() {
                if (element instanceof PascalRoutineImpl) {
                    return element.getText();
                }
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
    public static Collection<PascalNamedElement> findAllReferences(PsiElement element, String key) {
        LOG.debug("*** refs(" + key + ")" + PsiUtil.getElDebugContext(element));
        Collection<PascalNamedElement> result = new LinkedHashSet<PascalNamedElement>();
        PasNamespaceIdent usedModule = getUsedModuleName(element);
        if (usedModule != null) {
            return PasReferenceUtil.findUsedModuleReferences(usedModule);
        } else if (PsiUtil.isTypeName(element)) {
            result.addAll(findTypes(element, key));
        } else if (PsiUtil.isEntityName(element)) {
            //result.addAll(findTypes(element, key));
            NamespaceRec namespaceRec;
            if (element instanceof PasSubIdent) {
                namespaceRec = new NamespaceRec((PasSubIdent) element);
            } else {
                namespaceRec = new NamespaceRec((PasRefNamedIdent) element);
            }
            result.addAll(findVariables(namespaceRec, PasNamedIdent.class, PasGenericTypeIdent.class, PasNamespaceIdent.class));
            //result.addAll(findConstants(element, key));
            //List<PascalNamedElement> modules = findModules(element, key);
        }
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

}
