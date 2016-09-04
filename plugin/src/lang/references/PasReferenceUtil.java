package com.siberika.idea.pascal.lang.references;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.SmartPsiElementPointer;
import com.intellij.psi.search.FileTypeIndex;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.search.ProjectScope;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.SmartList;
import com.intellij.util.StringLenComparator;
import com.intellij.util.indexing.FileBasedIndex;
import com.siberika.idea.pascal.DCUFileType;
import com.siberika.idea.pascal.PPUFileType;
import com.siberika.idea.pascal.PascalFileType;
import com.siberika.idea.pascal.PascalRTException;
import com.siberika.idea.pascal.lang.parser.NamespaceRec;
import com.siberika.idea.pascal.lang.parser.PascalParserUtil;
import com.siberika.idea.pascal.lang.psi.PasClassProperty;
import com.siberika.idea.pascal.lang.psi.PasEntityScope;
import com.siberika.idea.pascal.lang.psi.PasEnumType;
import com.siberika.idea.pascal.lang.psi.PasExpression;
import com.siberika.idea.pascal.lang.psi.PasHandler;
import com.siberika.idea.pascal.lang.psi.PasInvalidScopeException;
import com.siberika.idea.pascal.lang.psi.PasModule;
import com.siberika.idea.pascal.lang.psi.PasNamedIdent;
import com.siberika.idea.pascal.lang.psi.PasTypeDecl;
import com.siberika.idea.pascal.lang.psi.PasTypeID;
import com.siberika.idea.pascal.lang.psi.PasWithStatement;
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
import com.siberika.idea.pascal.util.SyncUtil;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Author: George Bakhtadze
 * Date: 25/04/2013
 */
public class PasReferenceUtil {

    private static final Logger LOG = Logger.getInstance(PasReferenceUtil.class.getName());

    private static final int MAX_RECURSION_COUNT = 1000;
    private static final int MAX_NAMESPACES = 300;

    /**
     * Finds and returns unit in path by name
     * @param moduleName - unit name
     * @return unit element
     */
    @Nullable
    public static PasEntityScope findUnit(@NotNull Project project, @NotNull List<VirtualFile> unitFiles, @NotNull final String moduleName) {
        VirtualFile file = findUnitFile(unitFiles, moduleName);
        if (file != null) {
            PsiFile pascalFile = PsiManager.getInstance(project).findFile(file);
            PasModule pasModule = PsiTreeUtil.findChildOfType(pascalFile, PasModule.class);
            if (pasModule != null) {
                return pasModule;
            } else {
                LOG.info(String.format("ERROR: No module found in file %s", file.getName()));
            }
        } else {
            LOG.info(String.format("ERROR: No file found for unit %s", moduleName));
        }
        return null;
    }

    /**
     * Finds and returns file of a module with the given name
     * If more than one file matches the one with longest name is returned
     * @return list of PsiFiles
     */
    @Nullable
    public static VirtualFile findUnitFile(@NotNull List<VirtualFile> unitFiles, @NotNull final String moduleName) {
        List<VirtualFile> candidates = new ArrayList<VirtualFile>();
        for (VirtualFile virtualFile : unitFiles) {
            if (isFileOfModuleWithName(virtualFile, moduleName)) {
                candidates.add(virtualFile);
            }
        }
        Collections.sort(candidates, new Comparator<VirtualFile>() {
            @Override
            public int compare(VirtualFile o1, VirtualFile o2) {
                return o2.getNameWithoutExtension().length() - o1.getNameWithoutExtension().length();
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
        if (module != null) {
            virtualFiles.addAll(FileBasedIndex.getInstance().getContainingFiles(FileTypeIndex.NAME, PascalFileType.INSTANCE,
                    GlobalSearchScope.allScope(project)));
            virtualFiles.addAll(FileBasedIndex.getInstance().getContainingFiles(FileTypeIndex.NAME, PPUFileType.INSTANCE,
                    GlobalSearchScope.moduleWithDependenciesAndLibrariesScope(module)));
            virtualFiles.addAll(FileBasedIndex.getInstance().getContainingFiles(FileTypeIndex.NAME, DCUFileType.INSTANCE,
                    GlobalSearchScope.moduleWithDependenciesAndLibrariesScope(module)));
        } else {
            virtualFiles.addAll(FileBasedIndex.getInstance().getContainingFiles(FileTypeIndex.NAME, PascalFileType.INSTANCE, ProjectScope.getLibrariesScope(project)));
            virtualFiles.addAll(FileBasedIndex.getInstance().getContainingFiles(FileTypeIndex.NAME, PPUFileType.INSTANCE, ProjectScope.getLibrariesScope(project)));
            virtualFiles.addAll(FileBasedIndex.getInstance().getContainingFiles(FileTypeIndex.NAME, DCUFileType.INSTANCE, ProjectScope.getLibrariesScope(project)));
        }
        virtualFiles.add(BuiltinsParser.getBuiltinsSource());
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
        String ext = virtualFile.getExtension();
        return (ext != null) && PascalFileType.UNIT_EXTENSIONS.contains(ext.toLowerCase())
                || PPUFileType.INSTANCE.getDefaultExtension().equalsIgnoreCase(ext)
                || DCUFileType.INSTANCE.getDefaultExtension().equalsIgnoreCase(ext);
    }

    private static boolean isVisibleWithinUnit(@NotNull PasField field, @NotNull NamespaceRec fqn) {
        if ((field.getElement() != null) && (field.getElement().getContainingFile() == fqn.getParentIdent().getContainingFile())) {
            // check if declaration comes earlier then usage or declaration allows forward mode
            int offs;
            offs = fqn.getParentIdent().getTextRange().getStartOffset();
            return (field.offset <= offs)
                    || PsiUtil.allowsForwardReference(fqn.getParentIdent());
        } else {
            // check if field visibility allows usage from another unit
            return (field.fieldType == PasField.FieldType.ROUTINE) || PasField.isAllowed(field.visibility, PasField.Visibility.STRICT_PROTECTED);
        }
    }

//-------------------------------------------------------------------

    private static PasField.ValueType resolveFieldType(PasField field, boolean includeLibrary, int recursionCount) {
        final PascalNamedElement element = field.getElement();
        if (recursionCount > PascalParserUtil.MAX_STRUCT_TYPE_RESOLVE_RECURSION) {
            throw new PascalRTException("Too much recursion during resolving type for: " + element);
        }
        PasTypeID typeId = null;
        PasField.ValueType res = null;
        if (element instanceof PasClassProperty) {
            typeId = PsiTreeUtil.getChildOfType(element, PasTypeID.class);
        } else if (element instanceof PascalRoutineImpl) {                                     // routine declaration case
            typeId = ((PascalRoutineImpl) element).getFunctionTypeIdent();
        } else if ((element != null) && (element.getParent() instanceof PasHandler)) {                                     // exception handler case
            typeId = ((PasHandler) element.getParent()).getTypeID();
        } else {
            if ((element != null) && PsiUtil.isTypeDeclPointingToSelf(element)) {
                res = PasField.getValueType(element.getName());
            } else {
                res = retrieveAnonymousType(PsiUtil.getTypeDeclaration(element), includeLibrary, recursionCount);
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

    @Nullable
    private static PasField.ValueType resolveTypeId(@NotNull PasTypeID typeId, boolean includeLibrary, int recursionCount) {
        Collection<PasField> types = resolve(null, NamespaceRec.fromElement(typeId.getFullyQualifiedIdent()), PasField.TYPES_TYPE, includeLibrary, ++recursionCount);
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
        return (field.getElement() != null) && (includeLibrary || !PsiUtil.isFromLibrary(field.getElement())) ? (PasEntityScope) field.getElement() : null;
    }

    @Nullable
    private static PasEntityScope retrieveFieldTypeScope(@NotNull PasField field, int recursionCount) {
        if (SyncUtil.tryLockQuiet(field.getTypeLock(), SyncUtil.LOCK_TIMEOUT_MS)) {
            try {
                if (!field.isTypeResolved()) {
                    field.setValueType(resolveFieldType(field, true, recursionCount));
                }
                return field.getValueType() != null ? field.getValueType().getTypeScope() : null;
            } finally {
                field.getTypeLock().unlock();
            }
        } else {
            return null;
        }
    }

    @Nullable
    public static PasEntityScope retrieveFieldTypeScope(@NotNull PasField field) {
        return retrieveFieldTypeScope(field, 0);
    }

    @Nullable
    public static PasEntityScope resolveTypeScope(NamespaceRec fqn, boolean includeLibrary) {
        Collection<PasField> types = resolve(null, fqn, PasField.TYPES_TYPE, includeLibrary, 0);
        for (PasField field : types) {
            PasEntityScope struct = field.getElement() != null ? PascalParserUtil.getStructTypeByIdent(field.getElement(), 0) : null;
            if (struct != null) {
                return struct;
            }
        }
        return null;
    }

    public static Collection<PasField> resolveExpr(final List<PsiElement> resultScope, final NamespaceRec fqn, Set<PasField.FieldType> fieldTypesOrig, boolean includeLibrary, int recursionCount) {
        PsiElement expr = fqn.getParentIdent().getParent();
        expr = expr != null ? expr.getFirstChild() : null;
        if (expr instanceof PascalExpression) {
            List<PasField.ValueType> types = PascalExpression.getType((PascalExpression) expr);
            if (!types.isEmpty()) {
                fqn.setNested(true);
                Set<PasField.FieldType> fieldTypes = new HashSet<PasField.FieldType>(fieldTypesOrig);
                fieldTypes.remove(PasField.FieldType.PSEUDO_VARIABLE);
                return resolve(resultScope, PascalExpression.retrieveScope(types), fqn, fieldTypes, includeLibrary, recursionCount);
            }
        }
        return resolve(resultScope, fqn, fieldTypesOrig, includeLibrary, recursionCount);
    }

    public static Collection<PasField> resolve(final List<PsiElement> resultScope, final NamespaceRec fqn, Set<PasField.FieldType> fieldTypesOrig, boolean includeLibrary, int recursionCount) {
        return resolve(resultScope, PsiUtil.getNearestAffectingScope(fqn.getParentIdent()), fqn, fieldTypesOrig, includeLibrary, recursionCount);
    }

    /**
     *  for each entry in FQN before target:
     *    find entity corresponding to NS in current scope
     *    if the entity represents a namespace - retrieve and make current
     *  for namespace of target entry add all its entities
     */
    public static Collection<PasField> resolve(final List<PsiElement> resultScope, PasEntityScope scope, final NamespaceRec fqn, final Set<PasField.FieldType> fieldTypesOrig, final boolean includeLibrary, final int recursionCount) {
        ProgressManager.checkCanceled();
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
            handleWith(namespaces, scope, fqn.getParentIdent());
            // Retrieve all namespaces affecting first FQN level
            while (scope != null) {
                addFirstNamespaces(namespaces, scope, includeLibrary);
                scope = fqn.isFirst() ? PsiUtil.getNearestAffectingScope(scope) : null;
            }

            List<PasEntityScope> newNs = checkUnitScope(result, namespaces, fqn);
            if (newNs != null) {
                namespaces = newNs;
                fieldTypes.remove(PasField.FieldType.UNIT);                                                              // Unit qualifier can be only first
            }

            while (fqn.isBeforeTarget() && (namespaces != null)) {
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
                        boolean isDefault = "DEFAULT".equals(fqn.getLastName().toUpperCase());
                        if ((fqn.getRestLevels() == 1) && ((null == newNs) || isDefault)         // "default" type pseudo value
                         && (field.fieldType == PasField.FieldType.TYPE)) {                      // Enumerated type member
                            if (isDefault) {
                                fqn.next();
                                PasField defaultField = new PasField(field.owner, field.getElement(), "default", PasField.FieldType.CONSTANT, field.visibility);
                                if (isFieldMatches(defaultField, fqn, fieldTypes)) {
                                    result.add(defaultField);
                                }
                                saveScope(resultScope, newNS, true);
                                return result;
                            }
                            if (field.getValueType() != null) {
                                SmartPsiElementPointer<PasTypeDecl> typePtr = field.getValueType().declaration;
                                PasTypeDecl enumType = typePtr != null ? typePtr.getElement() : null;
                                PasEnumType enumDecl = enumType != null ? PsiTreeUtil.findChildOfType(enumType, PasEnumType.class) : null;
                                if (enumDecl != null) {
                                    fqn.next();
                                    saveScope(resultScope, enumDecl, true);
                                    return collectEnumFields(result, field, enumDecl, fqn, fieldTypes);
                                }
                            }
                        }
                    }

                    namespaces = newNS != null ? new SmartList<PasEntityScope>(newNS) : null;
                    addParentNamespaces(namespaces, newNS, false);
                }
                fqn.next();
                fieldTypes.remove(PasField.FieldType.UNIT);                                                              // Unit qualifier can be only first in FQN
                fieldTypes.remove(PasField.FieldType.PSEUDO_VARIABLE);                                                   // Pseudo variables can be only first in FQN
            }

            if (!fqn.isComplete() && (namespaces != null)) {
                for (PasEntityScope namespace : namespaces) {
                    if (null == namespace) {
                        LOG.warn(String.format("===*** null namespace! %s", fqn));
                        continue;
                    }
                    for (PasField pasField : namespace.getAllFields()) {
                        if ((pasField.getElementPtr() != null) && isFieldMatches(pasField, fqn, fieldTypes) &&
                                !result.contains(pasField) &&
                                isVisibleWithinUnit(pasField, fqn)) {
                            saveScope(resultScope, namespace, false);
                            result.add(pasField);
                        }
                    }
                    if (!result.isEmpty() && !isCollectingAll(fqn)) {
                        break;
                    }
                }
                if (result.isEmpty() && (resultScope != null)) {
                    resultScope.clear();
                    for (PasEntityScope namespace : namespaces) {
                        resultScope.add(namespace);
                    }
                }
            }
        } catch (PasInvalidScopeException e) {
            /*if (namespaces != null) {
                for (PasEntityScope namespace : namespaces) {
                    namespace.invalidateCache();
                }
            }*/
        /*} catch (Throwable e) {
            //LOG.error(String.format("Error parsing scope %s, file %s", scope, scope != null ? scope.getContainingFile().getName() : ""), e);
            throw e;*/
        }
        return result;
    }

    private static void saveScope(List<PsiElement> resultScope, PsiElement namespace, boolean clear) {
        if (resultScope != null) {
            if (clear) {
                resultScope.clear();
            }
            resultScope.add(namespace);
        }
    }

    // Advances fqn
    private static Collection<PasField> collectEnumFields(Collection<PasField> result, PasField field, PasEnumType enumDecl, NamespaceRec fqn, Set<PasField.FieldType> fieldTypes) {
        for (PasNamedIdent ident : enumDecl.getNamedIdentList()) {
            PasField enumField = new PasField(field.owner, ident, ident.getName(), PasField.FieldType.CONSTANT, field.visibility);
            if (isFieldMatches(enumField, fqn, fieldTypes)) {
                result.add(enumField);
            }
        }
        return result;
    }

    private static List<PasEntityScope> checkUnitScope(Collection<PasField> result, List<PasEntityScope> namespaces, NamespaceRec fqn) {
        ArrayList<PasEntityScope> sorted = new ArrayList<PasEntityScope>(namespaces.size());
        for (PasEntityScope namespace : namespaces) {
            if (namespace instanceof PasModule) {
                if (!StringUtils.isEmpty(namespace.getName())) {
                    sorted.add(namespace);
                }
            }
        }
        // sort namespaces by name length in reverse order to check longer named namespaces first
        Collections.sort(sorted, new Comparator<PasEntityScope>() {
            @Override
            public int compare(PasEntityScope o1, PasEntityScope o2) {
                return StringLenComparator.getInstance().compare(o2.getName(), o1.getName());
            }
        });
        for (PasEntityScope namespace : sorted) {
            if (fqn.advance(namespace.getName())) {
                if (fqn.isComplete()) {
                    result.add(namespace.getField(namespace.getName()));
                }
                return new SmartList<PasEntityScope>(namespace);
            }
        }
        return null;
    }

    private static void handleWith(List<PasEntityScope> namespaces, PasEntityScope scope, PsiElement ident) {
        if (null == scope) {
            return;
        }
        Collection<PasWithStatement> statements = PsiTreeUtil.findChildrenOfType(scope, PasWithStatement.class);
        for (PasWithStatement ws : statements) {
            if (PsiUtil.isParentOf(ident, ws.getStatement()) && PsiUtil.isParentOf(ws, scope)) {
                for (PasExpression expr : ws.getExpressionList()) {
                    if ((expr != null) && (expr.getExpr() instanceof PascalExpression)) {
                        List<PasField.ValueType> types = PascalExpression.getType((PascalExpression) expr.getExpr());
                        if (!types.isEmpty()) {
                            PasEntityScope ns = PascalExpression.retrieveScope(types);
                            if (ns != null) {
                                namespaces.add(ns);
                            }
                        }
                    }
                }
            }
        }
    }

    private static boolean isCollectingAll(NamespaceRec fqn) {
        return "".equals(fqn.getCurrentName());
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
    private static void addFirstNamespaces(List<PasEntityScope> namespaces, PasEntityScope section, boolean includeLibrary) {
        namespaces.add(section);
        if (section instanceof PascalModuleImpl) {
            addUnitNamespaces(namespaces, ((PascalModuleImpl) section).getPrivateUnits(), includeLibrary);
            addUnitNamespaces(namespaces, ((PascalModuleImpl) section).getPublicUnits(), includeLibrary);
        }
        addParentNamespaces(namespaces, section, true);
    }

    private static void addUnitNamespaces(List<PasEntityScope> namespaces, List<SmartPsiElementPointer<PasEntityScope>> units, boolean includeLibrary) {
        for (SmartPsiElementPointer<PasEntityScope> unitPtr : units) {
            PasEntityScope unit = unitPtr.getElement();
            if ((unit != null) && (includeLibrary || !PsiUtil.isFromLibrary(unit))) {
                namespaces.add(unit);
            }
        }
    }

    private static void addParentNamespaces(@Nullable List<PasEntityScope> namespaces, @Nullable PasEntityScope section, boolean first) {
        if ((null == namespaces) || (namespaces.size() > MAX_NAMESPACES)) {
            return;
        }
        if (null == section) {
            return;
        }
        for (SmartPsiElementPointer<PasEntityScope> scopePtr : section.getParentScope()) {
            PasEntityScope scope = scopePtr.getElement();
            if (first || (scope instanceof PascalStructType)) {                  // Search for parents for first namespace (method) or any for structured types
                if (null != scope) {
                    namespaces.add(scope);
                    addParentNamespaces(namespaces, scope, first);
                }
            }
        }
    }

}
