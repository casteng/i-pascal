package com.siberika.idea.pascal.lang.references;

import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtilCore;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.search.FileTypeIndex;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.SmartList;
import com.intellij.util.indexing.FileBasedIndex;
import com.siberika.idea.pascal.PPUFileType;
import com.siberika.idea.pascal.PascalFileType;
import com.siberika.idea.pascal.PascalRTException;
import com.siberika.idea.pascal.lang.parser.NamespaceRec;
import com.siberika.idea.pascal.lang.parser.PascalParserUtil;
import com.siberika.idea.pascal.lang.psi.PasClassProperty;
import com.siberika.idea.pascal.lang.psi.PasEntityScope;
import com.siberika.idea.pascal.lang.psi.PasFullyQualifiedIdent;
import com.siberika.idea.pascal.lang.psi.PasInvalidScopeException;
import com.siberika.idea.pascal.lang.psi.PasModule;
import com.siberika.idea.pascal.lang.psi.PasNamespaceIdent;
import com.siberika.idea.pascal.lang.psi.PasTypeDecl;
import com.siberika.idea.pascal.lang.psi.PasTypeID;
import com.siberika.idea.pascal.lang.psi.PascalNamedElement;
import com.siberika.idea.pascal.lang.psi.PascalStructType;
import com.siberika.idea.pascal.lang.psi.impl.PasField;
import com.siberika.idea.pascal.lang.psi.impl.PascalModuleImpl;
import com.siberika.idea.pascal.sdk.BuiltinsParser;
import com.siberika.idea.pascal.util.PsiUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import static com.google.common.collect.Iterables.getFirst;

/**
 * Author: George Bakhtadze
 * Date: 25/04/2013
 */
public class PasReferenceUtil {
    private static final int MAX_RECURSION_COUNT = 1000;

    /**
     * Returns references of the given module. This can be module qualifier in identifiers and other modules.
     * @param moduleName - name element of module to find references for
     * @return array of references in nearest-first order
     */
    @SuppressWarnings({"unchecked", "ConstantConditions"})
    public static List<PascalNamedElement> findUsedModuleReferences(@NotNull final PasNamespaceIdent moduleName) {
        final List<PascalNamedElement> result = new ArrayList<PascalNamedElement>();
        PascalNamedElement unit = findUnit(moduleName.getProject(), ModuleUtilCore.findModuleForPsiElement(moduleName), moduleName.getName());
        if (unit != null) {
            result.add(unit);
        }

        /*for (PascalQualifiedIdent element : PsiUtil.findChildrenOfAnyType(moduleName.getContainingFile(), PascalQualifiedIdent.class)) {
            if (moduleName.getName().equalsIgnoreCase(element.getNamespace())) {
                result.add(element);
            }
        }*/
        return result;
    }

    /**
     * Finds and returns unit in path by name
     * @param module - IDEA module
     * @param moduleName - unit name
     * @return unit element
     */
    @Nullable
    public static PasEntityScope findUnit(@NotNull Project project, @Nullable final Module module, @NotNull final String moduleName) {
        VirtualFile file = findUnitFile(project, module, moduleName);
        if (file != null) {
            PsiFile pascalFile = PsiManager.getInstance(project).findFile(file);
            PasModule pasModule = PsiTreeUtil.findChildOfType(pascalFile, PasModule.class);
            if (pasModule != null) {
                return pasModule;
            }
        }
        return null;
    }

    /**
     * Finds and returns file of a module with the given name
     * @param module - IDEA module to include its compiled dependencies
     * @return list of PsiFiles
     */
    @Nullable
    public static VirtualFile findUnitFile(@NotNull Project project, @Nullable final Module module, @NotNull final String moduleName) {
        for (VirtualFile virtualFile : findUnitFiles(project, module)) {
            if (isFileOfModuleWithName(virtualFile, moduleName)) {
                return virtualFile;
            }
        }
        return null;
    }

    /**
     * Finds and returns module files in search path
     * @param module - IDEA module to include its compiled dependencies
     * @return list of PsiFiles
     */
    @NotNull
    public static List<VirtualFile> findUnitFiles(@NotNull Project project, @Nullable final Module module) {
        final List<VirtualFile> virtualFiles = new SmartList<VirtualFile>();
        virtualFiles.addAll(FileBasedIndex.getInstance().getContainingFiles(FileTypeIndex.NAME, PascalFileType.INSTANCE,
                GlobalSearchScope.allScope(project)));
        if (module != null) {
            virtualFiles.addAll(FileBasedIndex.getInstance().getContainingFiles(FileTypeIndex.NAME, PPUFileType.INSTANCE,
                    GlobalSearchScope.moduleWithDependenciesAndLibrariesScope(module)));
        }
        return virtualFiles;
    }

    // Returns True if the file is the file of a module with the given name
    public static boolean isFileOfModuleWithName(VirtualFile virtualFile, @NotNull String name) {
        return isUnitExtension(virtualFile) && isFileUnitName(virtualFile.getNameWithoutExtension(), name);
    }

    private static boolean isFileUnitName(@NotNull String fileNameWoExt, @NotNull String name) {
        return fileNameWoExt.equalsIgnoreCase(name) || ((name.length() > 8) && (name.substring(0, 8).equalsIgnoreCase(fileNameWoExt)));
    }

    private static boolean isUnitExtension(VirtualFile virtualFile) {
        return PascalFileType.UNIT_EXTENSION.equalsIgnoreCase(virtualFile.getExtension())
            || PPUFileType.INSTANCE.getDefaultExtension().equalsIgnoreCase(virtualFile.getExtension());
    }

    @Deprecated
    public static Collection<LookupElement> getEntities(final PsiElement element, Set<PasField.FieldType> fieldTypes) {
        Collection<PascalNamedElement> result = new TreeSet<PascalNamedElement>(); // TODO: add comparator
        PsiElement section = PsiUtil.getNearestAffectingDeclarationsRoot(element);
        doGetEntities(result, section, element.getTextRange().getStartOffset(), fieldTypes, new HashSet<String>());

        Collection<LookupElement> res = new ArrayList<LookupElement>(result.size());
        for (PascalNamedElement namedElement : result) {
            res.add(LookupElementBuilder.createWithIcon(namedElement));
        }
        return res;
    }

    /**
     * Recursively goes up by entity declaration scopes and adds declared entities to result
     */
    @Deprecated
    private static void doGetEntities(Collection<PascalNamedElement> result, PsiElement section, int startOffset, Set<PasField.FieldType> fieldTypes, Set<String> filter) {
        if (null == section) { return; }
        if (section instanceof PasEntityScope) {
            try {
                for (PasField field : ((PasEntityScope) section).getAllFields()) {
                    if ((field.element != null) && (startOffset > field.element.getTextRange().getStartOffset()) &&  // TODO: pointer declaration exception
                        fieldTypes.contains(field.fieldType) && !filter.contains(field.name.toUpperCase())) {
                        result.add(field.element);
                        filter.add(field.name.toUpperCase());
                    }
                }
            } catch (PasInvalidScopeException e) {
                e.printStackTrace();
            }
        }
        doGetEntities(result, PsiUtil.getNearestAffectingDeclarationsRoot(section), startOffset, fieldTypes, filter);
    }

    /**
     *  for each entry in FQN before target:
     *    find entity corresponding to NS in current scope
     *    if the entity represents a namespace - retrieve and make current
     *  for namespace of target entry add all its entities
     */
    public static Collection<LookupElement> getEntities(final NamespaceRec fqn, Set<PasField.FieldType> fieldTypes) {
        // First entry in FQN
        PsiElement section = PsiUtil.getNearestAffectingDeclarationsRoot(fqn.getParentIdent());
        List<PasEntityScope> namespaces = new SmartList<PasEntityScope>();
        // Retrieve all namespaces affecting first FQN level
        while (section != null) {
            namespaces.add(PsiUtil.getDeclRootScope(section));
            section = fqn.isFirst() ? PsiUtil.getNearestAffectingDeclarationsRoot(namespaces.get(namespaces.size()-1)) : null;
        }

        Collection<LookupElement> result = new ArrayList<LookupElement>();
        try {
            while (!fqn.isTarget() && (namespaces != null)) {
                PasField field = null;
                // Scan namespaces and get one matching field
                for (PasEntityScope namespace : namespaces) {
                    field = namespace.getField(fqn.getCurrentName());
                    if (field != null) { break; }
                }
                namespaces = null;
                if (field != null) {
                    PasEntityScope newNS = retrieveNamespace(field, fqn.isFirst());
                    namespaces = newNS != null ? new SmartList<PasEntityScope>(newNS) : null;
                    while (newNS != null) {              // Scan namespace's parent namespaces (class parents etc)
                        newNS = getFirst(newNS.getParentScope(), null);
                        if (newNS != null) {
                            namespaces.add(newNS);
                        }
                    }
                }

                fqn.next();
            }

            if (fqn.isTarget() && (namespaces != null)) {
                for (PasEntityScope namespace : namespaces) {
                    for (PasField pasField : namespace.getAllFields()) {
                        if ((pasField.element != null) && (fieldTypes.contains(pasField.fieldType)) && isVisibleWithinUnit(pasField, fqn)) {
                            result.add(LookupElementBuilder.createWithIcon(pasField.element));
                        }
                    }
                }
            }
        } catch (PasInvalidScopeException e) {
            e.printStackTrace();
        }
        return result;
    }

    private static boolean isVisibleWithinUnit(@NotNull PasField field, @NotNull NamespaceRec fqn) {
        if ((field.element != null) && (field.element.getContainingFile() == fqn.getParentIdent().getContainingFile())) {
            // check if declaration comes earlier then usage or declaration allows forward mode
            int offs;
            offs = fqn.getParentIdent().getTextRange().getStartOffset();
            return (field.offset <= offs)
                    || PsiUtil.allowsForwardReference(fqn.getParentIdent());
        } else {
            // check if field visibility allows usage from another unit
            return PasField.isAllowed(field.visibility, PasField.Visibility.PRIVATE);
        }
    }

    /**
     * Recursively searches up over entity declaration scopes for an entity with the given name
     */
    private static PasField doGetEntity(PsiElement section, String name, Set<PasField.FieldType> fieldTypes) throws PasInvalidScopeException {
        if (null == section) { return null; }
        if (section instanceof PasEntityScope) {
            PasField field = ((PasEntityScope) section).getField(name);
            if ((field != null) && (fieldTypes.contains(field.fieldType))) {
                return field;
            }
        }
        return doGetEntity(PsiUtil.getNearestAffectingDeclarationsRoot(section), name, fieldTypes);
    }

    private static PasEntityScope retrieveNamespace(PasField field, boolean canBeUnit) throws PasInvalidScopeException {
        /*if (canBeUnit && (entityDecl instanceof PasNamespaceIdent)) {
            PasNamespaceIdent usedModuleName = getUsedModuleName(entityDecl);
            if (usedModuleName != null) {
                PascalNamedElement unit = PasReferenceUtil.findUnit(usedModuleName);
                if (unit != null) {
                    return unit;
                }
            }
        }*/
        if (null == field) { return null; }
        return getStructTypeByIdent(field);
    }

    @Nullable
    private static PasEntityScope getStructTypeByIdent(PasField ident) throws PasInvalidScopeException {
        if (null == ident) { return null; }
        PsiElement typeDecl = PsiTreeUtil.getNextSiblingOfType(ident.element, PasTypeDecl.class);
        if (null == typeDecl) {
            typeDecl = PsiTreeUtil.getChildOfType(ident.element, PasTypeID.class);
        }
        if (typeDecl != null) {
            PasEntityScope strucTypeDecl = PsiTreeUtil.getChildOfType(typeDecl, PasEntityScope.class);
            if (strucTypeDecl != null) {            // Inline structured type declaration
                return strucTypeDecl;
            } else {                                // regular type
                PasFullyQualifiedIdent typeId = PsiTreeUtil.findChildOfType(typeDecl, PasFullyQualifiedIdent.class, true);
                if (typeId != null) {
                    PsiElement section = PsiUtil.getNearestAffectingDeclarationsRoot(ident.element);
                    return getStructTypeByIdent(doGetEntity(section, typeId.getName(), PasField.TYPES_TYPE));    // factor unit-qualified names
                    /*Collection<PascalNamedElement> entities = new SmartList<PascalNamedElement>();
                    doGetEntities(entities, section, section.getTextRange().getEndOffset(), PasField.TYPES_TYPE, new HashSet<String>());
                    for (PascalNamedElement element : entities) if (typeId.getName().equalsIgnoreCase(element.getName())) {
                        return getStructTypeByIdent(element);
                    }*/
                } else {
                    return null;
                }
            }
        } else {
            return null;
        }
    }

//-------------------------------------------------------------------

    private static PasField.ValueType resolveFieldType(PasField field, boolean includeLibrary, int recursionCount) {
        if (recursionCount > MAX_RECURSION_COUNT) {
            throw new PascalRTException("Too much recursion during resolving type for: " + field.element);
        }
        PasTypeID typeId;
        PasTypeDecl decl = null;
        PascalNamedElement element = null;
        if (field.element instanceof PasClassProperty) {
            typeId = PsiTreeUtil.getChildOfType(field.element, PasTypeID.class);
        } else {
            decl = PsiUtil.getTypeDeclaration(field.element);
            PsiElement child = decl != null ? decl.getFirstChild() : null;
            element = (child instanceof PascalNamedElement) ? (PascalNamedElement) child : null;
            typeId = PsiUtil.getDeclaredTypeName(decl);
        }
        PasField.Kind kind = null;
        PasField.ValueType valueType = null;
        if (typeId != null) {
            if (PsiUtil.isTypeDeclPointingToSelf(typeId.getFullyQualifiedIdent())) {
                return PasField.getValueType(field.name);
            }
            Collection<PasField> types = resolve(NamespaceRec.fromElement(typeId.getFullyQualifiedIdent()), PasField.TYPES_TYPE, includeLibrary, ++recursionCount);
            if (!types.isEmpty()) {
                PasField type = types.iterator().next();
                if (type == field) {                                                               //
                    return PasField.getValueType(field.name);
                }
                valueType = resolveFieldType(type, includeLibrary, ++recursionCount);          // resolve next type in chain
            }
        } else {                                                // anonimous type
            if (decl != null) {
                element = PsiUtil.findImmChildOfAnyType(decl, PascalNamedElement.class);
            }
            System.out.println("===*** anonymous type: " + field.element);
            kind = PasField.Kind.BOOLEAN;
        }
        return new PasField.ValueType(typeId != null ? typeId.getFullyQualifiedIdent().getName() : null, field, kind, valueType, element);
    }

    @Nullable
    private static PasEntityScope retrieveFieldUnitScope(PasField field, boolean includeLibrary) {
        return (field.element != null) && (includeLibrary || !PsiUtil.isFromLibrary(field.element)) ? (PasEntityScope) field.element : null;
    }

    @Nullable
    private static PasEntityScope retrieveFieldTypeScope(@NotNull PasField field, int recursionCount) throws PasInvalidScopeException {
        synchronized (field) {
            if (!field.isTypeResolved()) {
                field.setValueType(resolveFieldType(field, true, recursionCount));
            }
        }
        return field.getTypeScope();
    }

    @Nullable
    public static PasEntityScope resolveTypeScope(NamespaceRec fqn, boolean includeLibrary) {
        Collection<PasField> types = resolve(fqn, PasField.TYPES_TYPE, includeLibrary, 0);
        for (PasField field : types) {
            PasEntityScope struct = field.element != null ? PascalParserUtil.getStructTypeByIdent(field.element, 0) : null;
            if (struct != null) {
                return struct;
            }
        }
        return null;
    }

    /**
     *  for each entry in FQN before target:
     *    find entity corresponding to NS in current scope
     *    if the entity represents a namespace - retrieve and make current
     *  for namespace of target entry add all its entities
     */
    public static Collection<PasField> resolve(final NamespaceRec fqn, Set<PasField.FieldType> fieldTypesOrig, boolean includeLibrary, int recursionCount) {
        if (recursionCount > MAX_RECURSION_COUNT) {
            throw new PascalRTException("Too much recursion during resolving identifier: " + fqn.getParentIdent());
        }
        // First entry in FQN
        PasEntityScope scope = PsiUtil.getNearestAffectingScope(fqn.getParentIdent());
        List<PasEntityScope> namespaces = new SmartList<PasEntityScope>();
        Collection<PasField> result = new HashSet<PasField>();

        Set<PasField.FieldType> fieldTypes = new HashSet<PasField.FieldType>(fieldTypesOrig);

        try {
            // Retrieve all namespaces affecting first FQN level
            while (scope != null) {
                addFirstNamespaces(namespaces, scope, includeLibrary);
                scope = fqn.isFirst() ? PsiUtil.getNearestAffectingScope(scope) : null;
            }

            while (!fqn.isTarget() && (namespaces != null)) {
                PasField field = null;
                // Scan namespaces and get one matching field
                for (PasEntityScope namespace : namespaces) {
                    field = namespace.getField(fqn.getCurrentName());
                    if (field != null) {
                        break;
                    }
                }
                namespaces = null;
                if (field != null) {
                    PasEntityScope newNS;
                    if (field.fieldType == PasField.FieldType.UNIT) {
                        newNS = fqn.isFirst() ? retrieveFieldUnitScope(field, includeLibrary) : null;                    // First qualifier can be unit name
                    } else {
                        newNS = retrieveFieldTypeScope(field, recursionCount);
                    }

                    namespaces = newNS != null ? new SmartList<PasEntityScope>(newNS) : null;
                    addParentNamespaces(namespaces, newNS, false);
                }
                fqn.next();
                fieldTypes.remove(PasField.FieldType.UNIT);                                                              // Unit qualifier can be only first
            }

            if (fqn.isTarget() && (namespaces != null)) {
                for (PasEntityScope namespace : namespaces) {
                    if (!PsiUtil.isElementValid(namespace)) {
                        PsiUtil.rebuildPsi(namespace);
                        return result;
                    }
                    for (PasField pasField : namespace.getAllFields()) {
                        if ((pasField.element != null) && isFieldMatches(pasField, fqn, fieldTypes) &&
                                !result.contains(pasField) &&
                                isVisibleWithinUnit(pasField, fqn)) {
                            result.add(pasField);
                        }
                    }
                    if (!result.isEmpty() && !isCollectingAll(fqn)) {
                        break;
                    }
                }
                if (!fqn.isEmpty() && (fqn.isFirst())) {
                    addBuiltins(result, fqn, fieldTypes);
                }
            }
        } catch (PasInvalidScopeException e) {
            /*if (namespaces != null) {
                for (PasEntityScope namespace : namespaces) {
                    namespace.invalidateCache();
                }
            }*/
        }
        return result;
    }

    private static boolean isCollectingAll(NamespaceRec fqn) {
        return "".equals(fqn.getCurrentName());
    }

    private static void addBuiltins(Collection<PasField> result, NamespaceRec fqn, Set<PasField.FieldType> fieldTypes) {
        PasModule module = null;
        for (PasField field : BuiltinsParser.getBuiltins()) {
            if (isFieldMatches(field, fqn, fieldTypes)) {
                module = module != null ? module : PsiUtil.getElementPasModule(fqn.getParentIdent());
                result.add(new PasField(field.owner, field.element, field.name, field.fieldType, field.visibility, module != null ? module : fqn.getParentIdent(), field.getValueType()));
            }
        }
    }

    private static boolean isFieldMatches(PasField field, NamespaceRec fqn, Set<PasField.FieldType> fieldTypes) {
        return (!fqn.isTarget() || fieldTypes.contains(field.fieldType)) &&
                (isCollectingAll(fqn) || field.name.equalsIgnoreCase(fqn.getCurrentName()));
    }

    /*
      #. WITH blocks                                                 EntityScope (dep)
      . local declaration blocks  (need pos check)                  RoutineImpl (strict)
      . parameters section                                          RoutineImpl (private)
      . implementation block      (need pos check)                  ModuleImpl  (private)
      . interface block           (need pos check)                  ModuleImpl  (public)
      . interface of units used in implementation (need pos check)  ModuleImpl  (public)
      . interface of units used in interface                        ModuleImpl  (public)
      . builtins
      . SELF - in method context
      . RESULT - in routine context
*/
    private static void addFirstNamespaces(List<PasEntityScope> namespaces, PasEntityScope section, boolean includeLibrary) throws PasInvalidScopeException {
        namespaces.add(section);
        if (section instanceof PascalModuleImpl) {
            addUnitNamespaces(namespaces, ((PascalModuleImpl) section).getPrivateUnits(), includeLibrary);
            addUnitNamespaces(namespaces, ((PascalModuleImpl) section).getPublicUnits(), includeLibrary);
        }
        addParentNamespaces(namespaces, section, true);
    }

    private static void addUnitNamespaces(List<PasEntityScope> namespaces, List<PasEntityScope> units, boolean includeLibrary) {
        for (PasEntityScope scope : units) {
            if ((scope != null) && (includeLibrary || !PsiUtil.isFromLibrary(scope))) {
                namespaces.add(scope);
            }
        }
    }

    private static void addParentNamespaces(List<PasEntityScope> namespaces, @Nullable PasEntityScope section, boolean first) throws PasInvalidScopeException {
        if (null == section) {
            return;
        }
        PasEntityScope newNS = getFirst(section.getParentScope(), null);
        while (newNS != null) {              // Scan namespace's parent namespaces (class parents etc)
            if (first || (newNS instanceof PascalStructType)) {
                namespaces.add(newNS);
            }
            newNS = getFirst(newNS.getParentScope(), null);
        }
    }

}
