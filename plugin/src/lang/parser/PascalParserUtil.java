package com.siberika.idea.pascal.lang.parser;

import com.intellij.lang.PsiBuilder;
import com.intellij.navigation.ItemPresentation;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtilCore;
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
import com.siberika.idea.pascal.lang.psi.PasClassHelperDecl;
import com.siberika.idea.pascal.lang.psi.PasClassTypeDecl;
import com.siberika.idea.pascal.lang.psi.PasClosureExpression;
import com.siberika.idea.pascal.lang.psi.PasEntityScope;
import com.siberika.idea.pascal.lang.psi.PasFullyQualifiedIdent;
import com.siberika.idea.pascal.lang.psi.PasGenericTypeIdent;
import com.siberika.idea.pascal.lang.psi.PasInterfaceTypeDecl;
import com.siberika.idea.pascal.lang.psi.PasModule;
import com.siberika.idea.pascal.lang.psi.PasNamedIdent;
import com.siberika.idea.pascal.lang.psi.PasNamespaceIdent;
import com.siberika.idea.pascal.lang.psi.PasObjectDecl;
import com.siberika.idea.pascal.lang.psi.PasRecordDecl;
import com.siberika.idea.pascal.lang.psi.PasRecordHelperDecl;
import com.siberika.idea.pascal.lang.psi.PasRefNamedIdent;
import com.siberika.idea.pascal.lang.psi.PasSubIdent;
import com.siberika.idea.pascal.lang.psi.PasTypeDecl;
import com.siberika.idea.pascal.lang.psi.PasTypeDeclaration;
import com.siberika.idea.pascal.lang.psi.PasTypeID;
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
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Pattern;

/**
 * Author: George Bakhtadze
 * Date: 12/9/12
 */
@SuppressWarnings("unchecked")
public class PascalParserUtil extends GeneratedParserUtilBase {
    private static final Logger LOG = Logger.getInstance(PascalParserUtil.class);

    public static final Collection<String> EXPLICIT_UNITS = Arrays.asList("system");

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

    public static Collection<PascalNamedElement> findSymbols(Project project, final String pattern) {
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
        }, PascalNamedElement.class);
        return new ArrayList<PascalNamedElement>(result);
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

    private static void retrieveBuiltinReferences(Collection<PascalNamedElement> result, PascalNamedElement ident) {
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
        if (section instanceof PascalRoutineImpl) {
            result.add(((PascalRoutineImpl) section));
        }
    }

    private static void retrieveDefaultNamespaceEntities(Collection<PascalNamedElement> result, PascalNamedElement current) {
        PsiElement section = PsiUtil.getNearestAffectingDeclarationsRoot(current);
        if (section instanceof PascalRoutineImpl) {
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
        if (section instanceof PascalRoutineImpl) {
            PasModule module = PsiUtil.getModule(section);
            if (module != null) {
                PasField field = module.getField(((PascalRoutineImpl) section).getNamespace());
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
    @SuppressWarnings("ConstantConditions")
    private static void addUsedUnitDeclarations(Collection<PascalNamedElement> result, PascalNamedElement current) {
        for (String unitName : EXPLICIT_UNITS) {
            addUnitDeclarations(result, current.getProject(), ModuleUtilCore.findModuleForPsiElement(current), unitName);
        }
        for (PasNamespaceIdent usedUnitName : PsiUtil.getUsedUnits(current.getContainingFile())) {
            addUnitDeclarations(result, current.getProject(), ModuleUtilCore.findModuleForPsiElement(usedUnitName), usedUnitName.getName());
        }
    }

    private static void addUnitDeclarations(Collection<PascalNamedElement> result, Project project, Module module, String unitName) {
        PascalNamedElement usedUnit = PasReferenceUtil.findUnit(project, module, unitName);
        if (usedUnit != null) {
            addDeclarations(result, PsiUtil.getModuleInterfaceSection(usedUnit), unitName);
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
            PsiElement section = getExplicitUnitSection(namespaces);
            section = section != null ? section : retrieveNamespace(entityDecl, namespaces.isFirst());
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

    private static PsiElement getExplicitUnitSection(NamespaceRec namespaces) {
        if (namespaces.isFirst()) {
            for (String unitName : EXPLICIT_UNITS) {
                if (unitName.equalsIgnoreCase(namespaces.getCurrent().getName())) {
                    return PasReferenceUtil.findUnit(namespaces.getCurrent().getProject(), ModuleUtilCore.findModuleForPsiElement(namespaces.getCurrent()), namespaces.getCurrent().getName());
                }
            }
        }
        return null;
    }

    private final static int getEndOffset(PsiElement section) {
        return section.getTextRange().getEndOffset();
    }

    @SuppressWarnings("ConstantConditions")
    private static PsiElement retrieveNamespace(PascalNamedElement entityDecl, boolean canBeUnit) {
        if (canBeUnit && (entityDecl instanceof PasNamespaceIdent)) {                                         // unit reference case
            PasNamespaceIdent usedModuleName = getUsedModuleName(entityDecl);
            if (usedModuleName != null) {
                PascalNamedElement unit = PasReferenceUtil.findUnit(usedModuleName.getProject(), ModuleUtilCore.findModuleForPsiElement(usedModuleName), usedModuleName.getName());
                if (unit != null) {
                    return unit;
                }
            }
        }
        if (canBeUnit && (entityDecl instanceof PascalRoutineImpl)) {                                         // routine self-reference case
            PasFullyQualifiedIdent typeName = ((PascalRoutineImpl) entityDecl).getFunctionTypeIdent();
            if (typeName != null) {
                for (PascalNamedElement strucTypeIdent : findVariables(new NamespaceRec(typeName, null), PasGenericTypeIdent.class, PasNamespaceIdent.class)) {
                    return getStructTypeByIdent(strucTypeIdent);
                }
            }
        }
        if (PsiUtil.isVariableDecl(entityDecl) || PsiUtil.isFieldDecl(entityDecl) || PsiUtil.isPropertyDecl(entityDecl)) { // variable declaration case
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
        } else if (entityDecl.getParent() instanceof PascalRoutineImpl) {                                     // routine declaration case
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
    private static PasEntityScope getStructTypeByIdent(@NotNull PascalNamedElement typeIdent) {
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
        if (null == element.getContainingFile()) {
            return result;
        }
        int offset = element.getTextRange().getStartOffset();
        if (PsiUtil.allowsForwardReference(element)) {
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
                return element.getContainingFile() != null ? element.getContainingFile().getName() : "-";
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
