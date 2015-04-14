package com.siberika.idea.pascal.lang.references;

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
import com.siberika.idea.pascal.lang.psi.PasInvalidScopeException;
import com.siberika.idea.pascal.lang.psi.PasModule;
import com.siberika.idea.pascal.lang.psi.PasNamespaceIdent;
import com.siberika.idea.pascal.lang.psi.PasTypeDecl;
import com.siberika.idea.pascal.lang.psi.PasTypeID;
import com.siberika.idea.pascal.lang.psi.PascalNamedElement;
import com.siberika.idea.pascal.lang.psi.PascalStructType;
import com.siberika.idea.pascal.lang.psi.impl.PasArrayTypeImpl;
import com.siberika.idea.pascal.lang.psi.impl.PasClassTypeTypeDeclImpl;
import com.siberika.idea.pascal.lang.psi.impl.PasEnumTypeImpl;
import com.siberika.idea.pascal.lang.psi.impl.PasField;
import com.siberika.idea.pascal.lang.psi.impl.PasFileTypeImpl;
import com.siberika.idea.pascal.lang.psi.impl.PasPointerTypeImpl;
import com.siberika.idea.pascal.lang.psi.impl.PasProcedureTypeImpl;
import com.siberika.idea.pascal.lang.psi.impl.PasSetTypeImpl;
import com.siberika.idea.pascal.lang.psi.impl.PasStringTypeImpl;
import com.siberika.idea.pascal.lang.psi.impl.PasStructTypeImpl;
import com.siberika.idea.pascal.lang.psi.impl.PasSubRangeTypeImpl;
import com.siberika.idea.pascal.lang.psi.impl.PasTypeIDImpl;
import com.siberika.idea.pascal.lang.psi.impl.PascalExpression;
import com.siberika.idea.pascal.lang.psi.impl.PascalModuleImpl;
import com.siberika.idea.pascal.lang.psi.impl.PascalRoutineImpl;
import com.siberika.idea.pascal.sdk.BuiltinsParser;
import com.siberika.idea.pascal.util.PsiUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
     * If more than one file matches the one with longest name is returned
     * @param module - IDEA module to include its compiled dependencies
     * @return list of PsiFiles
     */
    @Nullable
    public static VirtualFile findUnitFile(@NotNull Project project, @Nullable final Module module, @NotNull final String moduleName) {
        List<VirtualFile> candidates = new ArrayList<VirtualFile>();
        for (VirtualFile virtualFile : findUnitFiles(project, module)) {
            if (isFileOfModuleWithName(virtualFile, moduleName)) {
                candidates.add(virtualFile);
            }
        }
        Collections.sort(candidates, new Comparator<VirtualFile>() {
            @Override
            public int compare(VirtualFile o1, VirtualFile o2) {
                return o2.getName().length() - o1.getName().length();
            }
        });
        return !candidates.isEmpty() ? candidates.get(0) : null;
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

//-------------------------------------------------------------------

    private static PasField.ValueType resolveFieldType(PasField field, boolean includeLibrary, int recursionCount) {
        if (recursionCount > PascalParserUtil.MAX_STRUCT_TYPE_RESOLVE_RECURSION) {
            throw new PascalRTException("Too much recursion during resolving type for: " + field.element);
        }
        PasTypeID typeId = null;
        PasField.ValueType res = null;
        if (field.element instanceof PasClassProperty) {
            typeId = PsiTreeUtil.getChildOfType(field.element, PasTypeID.class);
        } else if (field.element instanceof PascalRoutineImpl) {                                     // routine declaration case
            typeId = ((PascalRoutineImpl) field.element).getFunctionTypeIdent();
        } else {
            if ((field.element != null) && PsiUtil.isTypeDeclPointingToSelf(field.element)) {
                res = PasField.getValueType(field.element.getName());
            } else {
                res = retrieveAnonymousType(PsiUtil.getTypeDeclaration(field.element), includeLibrary, recursionCount);
            }
        }
        if (typeId != null) {
            res = resolveTypeId(typeId, includeLibrary, recursionCount);
        }
        if (res != null) {
            res.field = field;
        }
        return res;
    }

    private static PasField.ValueType resolveTypeId(@NotNull PasTypeID typeId, boolean includeLibrary, int recursionCount) {
        Collection<PasField> types = resolve(NamespaceRec.fromElement(typeId.getFullyQualifiedIdent()), PasField.TYPES_TYPE, includeLibrary, ++recursionCount);
        if (!types.isEmpty()) {
            return resolveFieldType(types.iterator().next(), includeLibrary, ++recursionCount);          // resolve next type in chain
        }
        return null;
    }

    private static PasField.ValueType retrieveAnonymousType(PasTypeDecl decl, boolean includeLibrary, int recursionCount) {
        PasField.Kind kind = null;
        PasField.ValueType baseType = null;
        if (decl != null) {
            PsiElement type = decl.getFirstChild();
            if (type.getClass() == PasTypeIDImpl.class) {
                return resolveTypeId((PasTypeID) type, includeLibrary, recursionCount);
            } else if (type.getClass() == PasClassTypeTypeDeclImpl.class) {
                kind = PasField.Kind.CLASSREF;
                baseType = resolveTypeId(((PasClassTypeTypeDeclImpl) type).getTypeID(), includeLibrary, recursionCount);
            } else if (type instanceof PasStructTypeImpl) {
                kind = PasField.Kind.STRUCT;
            } else if (type.getClass() == PasArrayTypeImpl.class) {
                kind = PasField.Kind.ARRAY;
                baseType = retrieveAnonymousType(((PasArrayTypeImpl) type).getTypeDecl(), includeLibrary, ++recursionCount);
            } else if (type.getClass() == PasSetTypeImpl.class) {
                kind = PasField.Kind.SET;
                baseType = retrieveAnonymousType(((PasSetTypeImpl) type).getTypeDecl(), includeLibrary, ++recursionCount);
            } else if (type.getClass() == PasFileTypeImpl.class) {
                kind = PasField.Kind.FILE;
            } else if (type.getClass() == PasPointerTypeImpl.class) {
                kind = PasField.Kind.POINTER;
                baseType = retrieveAnonymousType(((PasPointerTypeImpl) type).getTypeDecl(), includeLibrary, ++recursionCount);
            } else if (type.getClass() == PasProcedureTypeImpl.class) {
                kind = PasField.Kind.PROCEDURE;
            } else if (type.getClass() == PasStringTypeImpl.class) {
                kind = PasField.Kind.STRING;
            } else if (type.getClass() == PasEnumTypeImpl.class) {
                kind = PasField.Kind.ENUM;
            } else if (type.getClass() == PasSubRangeTypeImpl.class) {
                kind = PasField.Kind.SUBRANGE;
            }
        }
        return new PasField.ValueType(null, kind, baseType, decl);
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
        return field.getValueType() != null ? field.getValueType().getTypeScope() : null;
    }

    @Nullable
    public static PasEntityScope retrieveFieldTypeScope(@NotNull PasField field) throws PasInvalidScopeException {
        return retrieveFieldTypeScope(field, 0);
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

    public static Collection<PasField> resolveExpr(final NamespaceRec fqn, Set<PasField.FieldType> fieldTypesOrig, boolean includeLibrary, int recursionCount) {
        PsiElement expr = fqn.getParentIdent().getParent();
        expr = expr != null ? expr.getFirstChild() : null;
        List<PasField.ValueType> types = Collections.emptyList();
        if (expr instanceof PascalExpression) {
            try {
                types = PascalExpression.getType((PascalExpression) expr);
            } catch (PasInvalidScopeException e) {
            }
        }
        if (!types.isEmpty()) {
            return resolve(PascalExpression.retrieveScope(types), fqn, fieldTypesOrig, includeLibrary, recursionCount);
        } else {
            return resolve(fqn, fieldTypesOrig, includeLibrary, recursionCount);
        }

    }

    public static Collection<PasField> resolve(final NamespaceRec fqn, Set<PasField.FieldType> fieldTypesOrig, boolean includeLibrary, int recursionCount) {
        return resolve(PsiUtil.getNearestAffectingScope(fqn.getParentIdent()), fqn, fieldTypesOrig, includeLibrary, recursionCount);
    }

    /**
     *  for each entry in FQN before target:
     *    find entity corresponding to NS in current scope
     *    if the entity represents a namespace - retrieve and make current
     *  for namespace of target entry add all its entities
     */
    public static Collection<PasField> resolve(PasEntityScope scope, final NamespaceRec fqn, Set<PasField.FieldType> fieldTypesOrig, boolean includeLibrary, int recursionCount) {
        if (recursionCount > MAX_RECURSION_COUNT) {
            throw new PascalRTException("Too much recursion during resolving identifier: " + fqn.getParentIdent());
        }
        if (null == scope) {
            scope = PsiUtil.getNearestAffectingScope(fqn.getParentIdent());
        }
        // First entry in FQN
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
