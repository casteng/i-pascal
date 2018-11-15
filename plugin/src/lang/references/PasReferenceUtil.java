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
import com.intellij.psi.stubs.StubElement;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.SmartList;
import com.intellij.util.StringLenComparator;
import com.intellij.util.indexing.FileBasedIndex;
import com.siberika.idea.pascal.DCUFileType;
import com.siberika.idea.pascal.PPUFileType;
import com.siberika.idea.pascal.PascalFileType;
import com.siberika.idea.pascal.PascalRTException;
import com.siberika.idea.pascal.lang.parser.NamespaceRec;
import com.siberika.idea.pascal.lang.parser.PascalFile;
import com.siberika.idea.pascal.lang.parser.PascalParserUtil;
import com.siberika.idea.pascal.lang.psi.PasCallExpr;
import com.siberika.idea.pascal.lang.psi.PasClassProperty;
import com.siberika.idea.pascal.lang.psi.PasEntityScope;
import com.siberika.idea.pascal.lang.psi.PasFullyQualifiedIdent;
import com.siberika.idea.pascal.lang.psi.PasHandler;
import com.siberika.idea.pascal.lang.psi.PasInvalidScopeException;
import com.siberika.idea.pascal.lang.psi.PasModule;
import com.siberika.idea.pascal.lang.psi.PasProcedureType;
import com.siberika.idea.pascal.lang.psi.PasTypeDecl;
import com.siberika.idea.pascal.lang.psi.PasTypeDeclaration;
import com.siberika.idea.pascal.lang.psi.PasTypeID;
import com.siberika.idea.pascal.lang.psi.PasWithStatement;
import com.siberika.idea.pascal.lang.psi.PascalModule;
import com.siberika.idea.pascal.lang.psi.PascalNamedElement;
import com.siberika.idea.pascal.lang.psi.PascalRoutine;
import com.siberika.idea.pascal.lang.psi.PascalRoutineEntity;
import com.siberika.idea.pascal.lang.psi.PascalStructType;
import com.siberika.idea.pascal.lang.psi.PascalStubElement;
import com.siberika.idea.pascal.lang.psi.impl.PasArrayTypeImpl;
import com.siberika.idea.pascal.lang.psi.impl.PasClassTypeTypeDeclImpl;
import com.siberika.idea.pascal.lang.psi.impl.PasEnumTypeImpl;
import com.siberika.idea.pascal.lang.psi.impl.PasField;
import com.siberika.idea.pascal.lang.psi.impl.PasFileTypeImpl;
import com.siberika.idea.pascal.lang.psi.impl.PasPointerTypeImpl;
import com.siberika.idea.pascal.lang.psi.impl.PasProcedureTypeImpl;
import com.siberika.idea.pascal.lang.psi.impl.PasSetTypeImpl;
import com.siberika.idea.pascal.lang.psi.impl.PasStringTypeImpl;
import com.siberika.idea.pascal.lang.psi.impl.PasSubRangeTypeImpl;
import com.siberika.idea.pascal.lang.psi.impl.PasTypeIDImpl;
import com.siberika.idea.pascal.lang.psi.impl.PasVariantScope;
import com.siberika.idea.pascal.lang.psi.impl.PascalExpression;
import com.siberika.idea.pascal.lang.psi.impl.PascalModuleImpl;
import com.siberika.idea.pascal.sdk.BuiltinsParser;
import com.siberika.idea.pascal.util.ModuleUtil;
import com.siberika.idea.pascal.util.PsiUtil;
import com.siberika.idea.pascal.util.SyncUtil;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Author: George Bakhtadze
 * Date: 25/04/2013
 */
public class PasReferenceUtil {

    private static final Logger LOG = Logger.getInstance(PasReferenceUtil.class.getName());

    public static final int MAX_RECURSION_COUNT = 1000;
    static final int MAX_NAMESPACES = 300;

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
            PascalModule pasModule = PsiTreeUtil.findChildOfType(pascalFile, PascalModule.class);
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
            return fqn.isIgnoreVisibility() || (field.fieldType == PasField.FieldType.ROUTINE) || PasField.isAllowed(field.visibility, PasField.Visibility.STRICT_PROTECTED);
        }
    }

//-------------------------------------------------------------------

    static PasField.ValueType resolveFieldType(PasField field, boolean includeLibrary, int recursionCount) {
        final PascalNamedElement element = field.getElement();
        if (recursionCount > PascalParserUtil.MAX_STRUCT_TYPE_RESOLVE_RECURSION) {
            throw new PascalRTException("Too much recursion during resolving type for: " + element);
        }
        PasTypeID typeId = null;
        PasField.ValueType res = null;
        if (element instanceof PasClassProperty) {
            typeId = resolvePropertyType(field, (PasClassProperty) element);
        } else if (element instanceof PascalRoutine) {                                          // routine declaration case
            typeId = ((PascalRoutine) element).getFunctionTypeIdent();
        } else if ((element != null) && (element.getParent() instanceof PasHandler)) {          // exception handler case
            typeId = ((PasHandler) element.getParent()).getTypeID();
        } else {
            if ((element != null) && PsiUtil.isTypeDeclPointingToSelf(element)) {
                res = PasField.getValueType(element.getName());
                if (null == res) {
                    res = new PasField.ValueType(null, PasField.Kind.VARIANT, null, ((PasTypeDeclaration) element.getParent()).getTypeDecl());
                }
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

    public static PasTypeID resolvePropertyType(PasField field, PasClassProperty element) {
        PasTypeID typeId = element.getTypeID();
        if ((null == typeId) && (field.owner instanceof PascalStructType)) {
            for (SmartPsiElementPointer<PasEntityScope> parentPtr : field.owner.getParentScope()) {
                PasEntityScope parent = parentPtr.getElement();
                PasField propField = parent != null ? parent.getField(field.name) : null;
                if (propField != null && propField.fieldType == PasField.FieldType.PROPERTY) {
                    PascalNamedElement propEl = propField.getElement();
                    if (propEl instanceof PasClassProperty) {
                        typeId = resolvePropertyType(propField, (PasClassProperty) propEl);
                        if (typeId != null) {
                            break;
                        }
                    }
                }
            }
        }
        return typeId;
    }

    @Nullable
    private static PasField.ValueType resolveTypeId(@NotNull PasTypeID typeId, boolean includeLibrary, int recursionCount) {
        ResolveContext context = new ResolveContext(PasField.TYPES_TYPE, includeLibrary);
        Collection<PasField> types = resolve(NamespaceRec.fromElement(typeId.getFullyQualifiedIdent()), context, ++recursionCount);
        if (!types.isEmpty()) {
            PasField type = types.iterator().next();
            PascalNamedElement el = type.getElement();
            if (ResolveUtil.isStubPowered(el)) {
                return ResolveUtil.resolveTypeWithStub((PascalStubElement) el, new ResolveContext(type.owner, PasField.TYPES_TYPE, context.includeLibrary, null, context.unitNamespaces), recursionCount);
            }
            return resolveFieldType(type, includeLibrary, ++recursionCount);          // resolve next type in chain
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
            } else if (type instanceof PascalStructType) {
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
    static PasEntityScope retrieveFieldTypeScope(@NotNull PasField field, ResolveContext context, int recursionCount) {
        if (ResolveUtil.isStubPowered(field.owner)) {
            return ResolveUtil.retrieveFieldTypeScope(field, context, recursionCount);
        }
        if (SyncUtil.tryLockQuiet(field.getTypeLock(), SyncUtil.LOCK_TIMEOUT_MS)) {
            try {
                if (!field.isTypeResolved()) {
                    field.setValueType(resolveFieldType(field, true, recursionCount));
                }
                if (field.getValueType() == PasField.VARIANT) {
                    return new PasVariantScope(field.getElement());
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
    public static PasEntityScope retrieveFieldTypeScope(@NotNull PasField field, ResolveContext context) {
        return retrieveFieldTypeScope(field, context, 0);
    }

    @Nullable
    public static PasEntityScope resolveTypeScope(NamespaceRec fqn, @Nullable PasEntityScope scope, boolean includeLibrary) {
        ResolveContext context = new ResolveContext(scope, PasField.TYPES_TYPE, includeLibrary, null, null);
        Collection<PasField> types = resolve(fqn, context, 0);
        for (PasField field : types) {
            PascalNamedElement el = field.getElement();
            if (ResolveUtil.isStubPowered(el)) {
                return ResolveUtil.retrieveFieldTypeScope(field, context, 0);
            }
            PasEntityScope struct = field.getElement() != null ? PascalParserUtil.getStructTypeByIdent(field.getElement(), 0) : null;
            if (struct != null) {
                return struct;
            }
        }
        return null;
    }

    public static Collection<PasField> resolveExpr(final NamespaceRec fqn, ResolveContext context, int recursionCount) {
        PsiElement expr = fqn.getParentIdent().getParent();
        expr = expr != null ? expr.getFirstChild() : null;
        if (expr instanceof PascalExpression) {
            List<PasField.ValueType> types = PascalExpression.getTypes((PascalExpression) expr);
            if (!types.isEmpty()) {
                fqn.setNested(true);
                Set<PasField.FieldType> savedFieldTypes = context.fieldTypes;
                context.fieldTypes = new HashSet<PasField.FieldType>(context.fieldTypes);
                context.fieldTypes.remove(PasField.FieldType.PSEUDO_VARIABLE);
                context.scope = PascalExpression.retrieveScope(types);
                Collection<PasField> result = resolve(fqn, context, recursionCount);
                context.fieldTypes = savedFieldTypes;
                return result;
            }
        }
        context.scope = null;
        return resolve(fqn, context, recursionCount);
    }

    /**
     *  for each entry in FQN before target:
     *    find entity corresponding to NS in current scope
     *    if the entity represents a namespace - retrieve and make current
     *  for namespace of target entry add all its entities
     */
    public static Collection<PasField> resolve(final NamespaceRec fqn, ResolveContext context, final int recursionCount) {
        ProgressManager.checkCanceled();
        if (recursionCount > MAX_RECURSION_COUNT) {
            throw new PascalRTException("Too much recursion during resolving identifier: " + fqn.getParentIdent());
        }
        if (null == context.unitNamespaces) {
            context.unitNamespaces = ModuleUtil.retrieveUnitNamespaces(fqn.getParentIdent());
        }
        PsiFile file = fqn.getParentIdent().getContainingFile();
        boolean implAffects = fqn.isFirst()
                && !ResolveUtil.isStubPowered(context.scope)
                && file instanceof PascalFile && PsiUtil.isBefore(((PascalFile) file).getImplementationSection(), fqn.getParentIdent());
        if (context.scope != null) {
            if (context.scope instanceof PascalStubElement) {
                StubElement stub = ((PascalStubElement) context.scope).retrieveStub();
                if (stub != null) {
                    return ResolveUtil.resolveWithStubs(fqn, context, recursionCount);
                }
            }
        } else {
            context.scope = PsiUtil.getNearestAffectingScope(fqn.getParentIdent());
        }

        // First entry in FQN
        List<PasEntityScope> namespaces = new SmartList<PasEntityScope>();
        Collection<PasField> result = new HashSet<PasField>();

        Set<PasField.FieldType> fieldTypes = EnumSet.copyOf(context.fieldTypes);

        try {
            handleWith(namespaces, context.scope, fqn.getParentIdent());
            // Retrieve all namespaces affecting first FQN level
            while (context.scope != null) {
                addFirstNamespaces(namespaces, context.scope, context.includeLibrary, implAffects);
                context.scope = fqn.isFirst() ? PsiUtil.getNearestAffectingScope(context.scope) : null;
            }

            while (fqn.isBeforeTarget() && (namespaces != null)) {
                PasField field = null;
                // Scan namespaces and get one matching field
                for (PasEntityScope namespace : namespaces) {
                    Collection<PasField> fields = resolveFromStub(fqn, namespace, context, recursionCount);
                    if ((fields != null) && (!fields.isEmpty())) {
                        result.addAll(fields);
                        return result;
                    }
                    field = namespace.getField(fqn.getCurrentName());
                    if (field != null) {
                        break;
                    }
                }

                PasEntityScope unitNamespace = null;
                if (((null == field) && fqn.isFirst()) || ((field != null ? field.fieldType : null) == PasField.FieldType.UNIT)) {
                    unitNamespace = handleUnitScope(result, namespaces, fqn, fieldTypes, context.unitNamespaces);
                    field = unitNamespace != null ? null : field;
                }
                namespaces = unitNamespace != null ? namespaces : null;

                if (field != null) {
                    PasEntityScope newNS;
                    if (field.fieldType == PasField.FieldType.UNIT) {
                        newNS = fqn.isFirst() && implAffects && PasField.isAllowed(field.visibility, PasField.Visibility.PRIVATE) ?
                                retrieveFieldUnitScope(field, context.includeLibrary) : null;                    // First qualifier can be unit name
                    } else {
                        newNS = retrieveFieldTypeScope(field, context, recursionCount);
                        boolean isDefault = "DEFAULT".equals(fqn.getLastName().toUpperCase());
                        if ((fqn.getRestLevels() == 1) && ((null == newNS) || isDefault)         // "default" type pseudo value
                         && (field.fieldType == PasField.FieldType.TYPE)) {                      // Enumerated type member
                            if (isDefault) {
                                fqn.next();
                                PasField defaultField = new PasField(field.owner, field.getElement(), "default", PasField.FieldType.CONSTANT, field.visibility);
                                if (isFieldMatches(defaultField, fqn, fieldTypes)) {
                                    result.add(defaultField);
                                }
                                saveScope(context.resultScope, newNS, true);
                                return result;
                            }
                            if (ResolveUtil.resolveEnumMember(result, field, fqn, fieldTypes)) {
                                return result;
                            }
                        }
                    }

                    namespaces = newNS != null ? new SmartList<PasEntityScope>(newNS) : null;
                    addParentNamespaces(namespaces, newNS, false);
                }
                if (null == unitNamespace) {
                    fqn.next();
                    removeFirstOnlyTypes(fieldTypes);
                }
            }

            if (!fqn.isComplete() && (namespaces != null)) {
                for (PasEntityScope namespace : namespaces) {
                    if (null == namespace) {
                        LOG.info(String.format("===*** null namespace! %s", fqn));
                        continue;
                    }
                    Collection<PasField> fields = resolveFromStub(fqn, namespace, context, recursionCount);
                    if ((fields != null) && (!fields.isEmpty())) {
                        result.addAll(fields);
                    } else {
                        ResolveUtil.findLastPart(result, fqn, namespace, fieldTypes, context, PasReferenceUtil::isVisibleWithinUnit);
                    }
                    if (!result.isEmpty() && !isCollectingAll(fqn)) {
                        break;
                    }
                }
                if (result.isEmpty() && fqn.isFirst()) {
                    handleUnitScope(result, namespaces, fqn, fieldTypes, context.unitNamespaces);
                }
                if (result.isEmpty() && (context.resultScope != null)) {
                    context.resultScope.clear();
                    context.resultScope.addAll(namespaces);
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

    static PasEntityScope handleUnitScope(Collection<PasField> result, List<PasEntityScope> namespaces, NamespaceRec fqn, Set<PasField.FieldType> fieldTypes, List<String> unitNamespaces) {
        PasEntityScope unitNamespace = null;
        if (fqn.isFirst()) {
            unitNamespace = checkUnitScope(result, namespaces, fqn, unitNamespaces);
        }
        if (unitNamespace != null) {
            namespaces.clear();
            namespaces.add(unitNamespace);
            if (PascalParserUtil.UNIT_NAME_SYSTEM.equalsIgnoreCase(unitNamespace.getName())) {
                namespaces.add(BuiltinsParser.getBuiltinsModule(unitNamespace.getProject()));
            }
            removeFirstOnlyTypes(fieldTypes);
        }
        return unitNamespace;
    }

    static void removeFirstOnlyTypes(Set<PasField.FieldType> fieldTypes) {
        fieldTypes.remove(PasField.FieldType.UNIT);                                                              // Unit qualifier can be only first in FQN
        fieldTypes.remove(PasField.FieldType.PSEUDO_VARIABLE);                                                   // Pseudo variables can be only first in FQN
    }

    private static Collection<PasField> resolveFromStub(NamespaceRec fqn, PasEntityScope namespace, ResolveContext context, int recursionCount) {
        Collection<PasField> result = null;
        if (namespace instanceof PascalStubElement) {
            StubElement stub = ((PascalStubElement) namespace).retrieveStub();
            if (stub != null) {
                ResolveContext ctx = new ResolveContext(namespace, context.fieldTypes, context.includeLibrary, context.resultScope, context.unitNamespaces);
                ctx.disableParentNamespaces = true;
                result = ResolveUtil.resolveWithStubs(new NamespaceRec(fqn), ctx, ++recursionCount);
            }
        }
        return result;
    }

    static void saveScope(List<PsiElement> resultScope, PsiElement namespace, boolean clear) {
        if (resultScope != null) {
            if (clear) {
                resultScope.clear();
            }
            resultScope.add(namespace);
        }
    }

    static PasEntityScope checkUnitScope(Collection<PasField> result, List<PasEntityScope> namespaces, NamespaceRec fqn, List<String> unitPrefixes) {
        List<PasEntityScope> sorted = new ArrayList<PasEntityScope>(namespaces.size());
        for (PasEntityScope namespace : namespaces) {
            if ((namespace instanceof PasModule) && !StringUtils.isEmpty(namespace.getName())) {
                sorted.add(namespace);
            }
        }
        // sort namespaces by name length in reverse order to check longer named namespaces first
        Collections.sort(sorted, new Comparator<PasEntityScope>() {
            @Override
            public int compare(PasEntityScope o1, PasEntityScope o2) {
                return StringLenComparator.getInstance().compare(o2.getName(), o1.getName());
            }
        });
        PasEntityScope res;
        res = tryUnit(fqn, sorted, "");
        if ((null == res) && (fqn.isTarget())) {        // don't check with prefixes if fqn has more than one level
            NamespaceRec oldFqn = new NamespaceRec(fqn);
            for (String prefix : unitPrefixes) {
                fqn.addPrefix(oldFqn, prefix);
                res = tryUnit(fqn, sorted, prefix);
                if (res != null) {
                    break;
                }
            }
        }
        if (res != null) {
            if (fqn.isComplete()) {
                PasField field = res.getField(res.getName());
                if (field != null) {
                    result.add(field);
                } else {
                    LOG.info("ERROR: field is null. FQN: " + fqn.toString());
                }
            }
            return res;
        }
        return null;
    }

    private static PasEntityScope tryUnit(NamespaceRec fqn, List<PasEntityScope> sortedScopes, String prefix) {
        for (PasEntityScope namespace : sortedScopes) {
            if (fqn.advance(namespace.getName())) {
                return namespace;
            }
        }
        return null;
    }

    private static void handleWith(List<PasEntityScope> namespaces, PasEntityScope scope, PsiElement ident) {
        if (null == scope) {
            return;
        }
        Collection<PasWithStatement> statements = scope.getWithStatements();
        for (PasWithStatement ws : statements) {
            if (PsiUtil.isParentOf(ident, ws.getStatement()) && PsiUtil.isParentOf(ws, scope)) {
                ResolveUtil.getScopes(namespaces, ws);
            }
        }
    }

    static boolean isCollectingAll(NamespaceRec fqn) {
        return "".equals(fqn.getCurrentName());
    }

    static boolean isFieldTypeMatches(PasField field, NamespaceRec fqn, Set<PasField.FieldType> fieldTypes) {
        return !fqn.isTarget() || fieldTypes.contains(field.fieldType);
    }

    static boolean isFieldMatches(PasField field, NamespaceRec fqn, Set<PasField.FieldType> fieldTypes) {
        return isFieldTypeMatches(field, fqn, fieldTypes) &&
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
    private static void addFirstNamespaces(List<PasEntityScope> namespaces, PasEntityScope scope, boolean includeLibrary, boolean implAffects) {
        namespaces.add(scope);
        if (scope instanceof PascalModuleImpl) {
            if (implAffects) {
                addUnitNamespaces(namespaces, ((PascalModuleImpl) scope).getPrivateUnits(), includeLibrary);
            }
            addUnitNamespaces(namespaces, ((PascalModuleImpl) scope).getPublicUnits(), includeLibrary);
        }
        addParentNamespaces(namespaces, scope, true);
    }

    private static void addUnitNamespaces(List<PasEntityScope> namespaces, List<SmartPsiElementPointer<PasEntityScope>> units, boolean includeLibrary) {
        for (SmartPsiElementPointer<PasEntityScope> unitPtr : units) {
            PasEntityScope unit = unitPtr.getElement();
            if ((unit != null) && (includeLibrary || !PsiUtil.isFromLibrary(unit))) {
                namespaces.add(unit);
            }
        }
    }

    private static void addParentNamespaces(@Nullable List<PasEntityScope> namespaces, @Nullable PasEntityScope scope, boolean first) {
        if ((null == namespaces) || (namespaces.size() > MAX_NAMESPACES)) {
            return;
        }
        if (null == scope) {
            return;
        }
        for (SmartPsiElementPointer<PasEntityScope> scopePtr : scope.getParentScope()) {
            PasEntityScope entityScope = scopePtr.getElement();
            if (first || (entityScope instanceof PascalStructType)) {                  // Search for parents for first namespace (method) or any for structured types
                if (null != entityScope) {
                    namespaces.add(entityScope);
                    addParentNamespaces(namespaces, entityScope, first);
                }
            }
        }
    }

    @NotNull
    public static Collection<PascalRoutineEntity> resolveRoutines(PasCallExpr callExpr) {
        PasFullyQualifiedIdent ident = callExpr != null ? PsiTreeUtil.findChildOfType(callExpr.getExpr(), PasFullyQualifiedIdent.class) : null;
        if (null == ident) {
            return Collections.emptyList();
        }
        Collection<PascalRoutineEntity> result = new SmartList<>();
        for (PasField field : resolveExpr(NamespaceRec.fromElement(ident), new ResolveContext(PasField.TYPES_PROPERTY_SPECIFIER, true), 0)) {
            if (field.fieldType == PasField.FieldType.ROUTINE) {
                PascalNamedElement el = field.getElement();
                if ((el instanceof PascalRoutineEntity) && el.isValid()) {
                    result.add((PascalRoutineEntity) el);
                }
            } else {
                PasField.ValueType type = resolveFieldType(field, true, 0);
                if (type != null) {
                    PsiElement decl = type.declaration != null ? type.declaration.getElement() : null;
                    PsiElement procType = decl != null ? decl.getFirstChild() : null;
                    if (procType instanceof PasProcedureType) {
                        result.add((PascalRoutineEntity) procType);
                    }
                }
            }
        }
        return result;
    }
}
