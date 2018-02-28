package com.siberika.idea.pascal.lang.references;

import com.intellij.extapi.psi.StubBasedPsiElementBase;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.StubBasedPsiElement;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.search.ProjectScope;
import com.intellij.psi.stubs.StubElement;
import com.intellij.psi.stubs.StubIndex;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.Processor;
import com.intellij.util.SmartList;
import com.intellij.util.containers.SmartHashSet;
import com.siberika.idea.pascal.PascalRTException;
import com.siberika.idea.pascal.lang.parser.NamespaceRec;
import com.siberika.idea.pascal.lang.psi.PasEntityScope;
import com.siberika.idea.pascal.lang.psi.PasGenericTypeIdent;
import com.siberika.idea.pascal.lang.psi.PasInvalidScopeException;
import com.siberika.idea.pascal.lang.psi.PasTypeDecl;
import com.siberika.idea.pascal.lang.psi.PasTypeID;
import com.siberika.idea.pascal.lang.psi.PascalIdentDecl;
import com.siberika.idea.pascal.lang.psi.PascalModule;
import com.siberika.idea.pascal.lang.psi.PascalNamedElement;
import com.siberika.idea.pascal.lang.psi.PascalPsiElement;
import com.siberika.idea.pascal.lang.psi.PascalQualifiedIdent;
import com.siberika.idea.pascal.lang.psi.PascalRoutine;
import com.siberika.idea.pascal.lang.psi.PascalStructType;
import com.siberika.idea.pascal.lang.psi.impl.PasField;
import com.siberika.idea.pascal.lang.psi.impl.PasVariantScope;
import com.siberika.idea.pascal.lang.stub.PasModuleStub;
import com.siberika.idea.pascal.lang.stub.PasNamedStub;
import com.siberika.idea.pascal.lang.stub.PascalModuleIndex;
import com.siberika.idea.pascal.sdk.BuiltinsParser;
import com.siberika.idea.pascal.util.PsiUtil;
import com.siberika.idea.pascal.util.SyncUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ResolveUtil {

    private static final Logger LOG = Logger.getInstance(ResolveUtil.class);

    // Find Pascal modules by key using stub index. If key is not specified return all modules.
    @NotNull
    public static Collection<PascalModule> findUnitsWithStub(@NotNull Project project, @Nullable final Module module, @Nullable String key) {
        final Collection<PascalModule> modules = new SmartHashSet<>();
        final GlobalSearchScope scope = module != null ? GlobalSearchScope.moduleWithDependenciesAndLibrariesScope(module, false) : ProjectScope.getAllScope(project);
        if (key != null) {
            modules.addAll(StubIndex.getElements(PascalModuleIndex.KEY, key, project, scope, PascalModule.class));
        } else {
            Processor<String> processor = new Processor<String>() {
                @Override
                public boolean process(String key) {
                    modules.addAll(StubIndex.getElements(PascalModuleIndex.KEY, key, project, scope, PascalModule.class));
                    return true;
                }
            };
            StubIndex.getInstance().processAllKeys(PascalModuleIndex.KEY, processor, scope, null);
        }
        if ((null == key) || BuiltinsParser.UNIT_NAME_BUILTINS.equalsIgnoreCase(key)) {
            modules.add(BuiltinsParser.getBuiltinsModule(project));
        }
        return modules;
    }

    @Nullable
    public static String getDeclarationTypeString(@NotNull PascalNamedElement el) {
        PascalQualifiedIdent ident = null;
        if (PsiUtil.isVariableDecl(el) || PsiUtil.isFieldDecl(el) || PsiUtil.isPropertyDecl(el) || PsiUtil.isConstDecl(el)) {   // variable declaration case
            PascalPsiElement varDecl = PsiTreeUtil.getNextSiblingOfType(el, PasTypeDecl.class);
            if (null == varDecl) {
                varDecl = PsiTreeUtil.getNextSiblingOfType(el, PasTypeID.class);
            }
            if (varDecl != null) {
                ident = PsiTreeUtil.findChildOfType(varDecl, PascalQualifiedIdent.class, true);
            }
        } else if (el.getParent() instanceof PasGenericTypeIdent) {                                                             // type declaration case
            el = (PascalNamedElement) el.getParent();
            PasTypeDecl res = PsiUtil.getTypeDeclaration(el);
            if (res != null) {
                PasTypeID typeId = PsiTreeUtil.findChildOfType(res, PasTypeID.class);
                ident = typeId != null ? typeId.getFullyQualifiedIdent() : null;
            }
        } else if (el.getParent() instanceof PascalRoutine) {                                     // routine declaration case
            PasTypeID type = ((PascalRoutine) el.getParent()).getFunctionTypeIdent();
            ident = type != null ? type.getFullyQualifiedIdent() : null;
        }
        return ident != null ? ident.getName() : null;
    }

    @Nullable
    public static PasEntityScope retrieveFieldTypeScope(@NotNull PasField field, ResolveContext context, int recursionCount) {
        if (SyncUtil.tryLockQuiet(field.getTypeLock(), SyncUtil.LOCK_TIMEOUT_MS)) {
            try {
                if (!field.isTypeResolved()) {
                    PascalNamedElement el = field.getElement();
                    if ((el instanceof StubBasedPsiElementBase) && (((StubBasedPsiElementBase) el).getStub() != null)) {
                        PasField.ValueType valueType = resolveTypeWithStub((StubBasedPsiElementBase) el, context, recursionCount);
                        valueType.field = field;
                        field.setValueType(valueType);
                    }
                }
                if (field.getValueType() == PasField.VARIANT) {
                    return new PasVariantScope(field.getElement());
                }
                return field.getValueType() != null ? field.getValueType().getTypeScopeStub() : null;
            } finally {
                field.getTypeLock().unlock();
            }
        } else {
            return null;
        }
    }

    private static PasField.ValueType resolveTypeWithStub(StubBasedPsiElementBase element, ResolveContext context, int recursionCount) {
        if (element instanceof PascalIdentDecl) {
            return resolveIdentDeclType((PascalIdentDecl) element, context, recursionCount);
        }
        return null;
    }

    private static PasField.ValueType resolveIdentDeclType(PascalIdentDecl element, ResolveContext context, int recursionCount) {
        if (element.getStub().getType() == PasField.FieldType.TYPE) {
            String type = getDeclarationTypeString(element);
            if (type != null) {
                ResolveContext typeResolveContext = new ResolveContext(PasField.TYPES_TYPE, context.includeLibrary);
                Collection<PasField> types = resolveWithStubs(NamespaceRec.fromFQN(element, type), typeResolveContext, ++recursionCount);
                if (!types.isEmpty()) {
                    for (PasField pasField : types) {
                        PascalNamedElement el = pasField.getElement();
                        if (el instanceof PascalStructType) {
                            return new PasField.ValueType(pasField, PasField.Kind.STRUCT, null, null);
                        }
                    }
                }
            }
            return null;
        }
        return null;
    }

    /**
     *  for each entry in FQN before target:
     *    find entity corresponding to NS in current scope
     *    if the entity represents a namespace - retrieve and make current
     *  for namespace of target entry add all its entities
     */
    public static Collection<PasField> resolveWithStubs(final NamespaceRec fqn, ResolveContext context, final int recursionCount) {
        assert(context.scope instanceof StubBasedPsiElement);
        StubElement stub = ((StubBasedPsiElement) context.scope).getStub();
        assert(stub instanceof PasNamedStub);
        PasEntityScope scope = context.scope;
        ProgressManager.checkCanceled();
        if (recursionCount > PasReferenceUtil.MAX_RECURSION_COUNT) {
            throw new PascalRTException("Too much recursion during resolving identifier: " + fqn.getParentIdent());
        }

        // First entry in FQN
        List<PasEntityScope> namespaces = new SmartList<PasEntityScope>();
        Collection<PasField> result = new HashSet<PasField>();

        Set<PasField.FieldType> fieldTypes = EnumSet.copyOf(context.fieldTypes);

        try {
            // Retrieve all namespaces affecting first FQN level
            while (scope != null) {
                addFirstNamespaces(namespaces, scope, context.includeLibrary);
                stub = stub.getParentStub();
                PsiElement parentScope = stub.getPsi();
                scope = fqn.isFirst() && (parentScope instanceof PasEntityScope) ? (PasEntityScope) parentScope : null;
            }

            List<PasEntityScope> newNs = PasReferenceUtil.checkUnitScope(result, namespaces, fqn);
            if (newNs != null) {
                namespaces = newNs;
                fieldTypes.remove(PasField.FieldType.UNIT);                                                              // Unit qualifier can be only first
            }

            while (fqn.isBeforeTarget() && (namespaces != null)) {
                PasField field = null;
                // Scan namespaces and get one matching field
                for (PasEntityScope namespace : namespaces) {
                    field = namespace.getField(fqn.getCurrentName());        // TODO: optimize?
                    if (field != null) {
                        break;
                    }
                }
                namespaces = null;
                if (field != null) {
                    PasEntityScope newNS;
                        newNS = retrieveFieldTypeScope(field, context, recursionCount);
                        boolean isDefault = "DEFAULT".equals(fqn.getLastName().toUpperCase());
                        if ((fqn.getRestLevels() == 1) && ((null == newNs) || isDefault)         // "default" type pseudo value
                                && (field.fieldType == PasField.FieldType.TYPE)) {                      // Enumerated type member
                            if (isDefault) {
                                fqn.next();
                                PasField defaultField = new PasField(field.owner, field.getElement(), "default", PasField.FieldType.CONSTANT, field.visibility);
                                if (PasReferenceUtil.isFieldMatches(defaultField, fqn, fieldTypes)) {
                                    result.add(defaultField);
                                }
//                                saveScope(context.resultScope, newNS, true);
                                return result;
                            }
                            /*if (field.getValueType() != null) {
                                SmartPsiElementPointer<PasTypeDecl> typePtr = field.getValueType().declaration;
                                PasTypeDecl enumType = typePtr != null ? typePtr.getElement() : null;
                                PasEnumType enumDecl = enumType != null ? _PsiTreeUtil.findChildOfType(enumType, PasEnumType.class) : null;
                                if (enumDecl != null) {
                                    fqn.next();
//                                    saveScope(context.resultScope, enumDecl, true);
                                    return collectEnumFields(result, field, enumDecl, fqn, fieldTypes);
                                }
                            }*/
                    }

                    namespaces = newNS != null ? new SmartList<PasEntityScope>(newNS) : null;
                    if (newNS instanceof PascalStructType) {
                        addParentNamespaces(namespaces, (PascalStructType) newNS);
                    }
                }
                fqn.next();
                fieldTypes.remove(PasField.FieldType.UNIT);                                                              // Unit qualifier can be only first in FQN
                fieldTypes.remove(PasField.FieldType.PSEUDO_VARIABLE);                                                   // Pseudo variables can be only first in FQN
            }

            if (!fqn.isComplete() && (namespaces != null)) {
                for (PasEntityScope namespace : namespaces) {
                    if (null == namespace) {
                        LOG.info(String.format("===*** null namespace! %s", fqn));
                        continue;
                    }
                    for (PasField pasField : namespace.getAllFields()) {
                        if (PasReferenceUtil.isFieldMatches(pasField, fqn, fieldTypes) &&
                                !result.contains(pasField) &&
                                isVisibleWithinUnit(pasField, fqn)) {
//                            saveScope(context.resultScope, namespace, false);
                            result.add(pasField);
                            if (!PasReferenceUtil.isCollectingAll(fqn)) {
                                break;
                            }
                        }
                    }
                    if (!result.isEmpty() && !PasReferenceUtil.isCollectingAll(fqn)) {
                        break;
                    }
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

    private static boolean isVisibleWithinUnit(PasField field, NamespaceRec fqn) {
        return fqn.isIgnoreVisibility() || (field.fieldType == PasField.FieldType.ROUTINE) || PasField.isAllowed(field.visibility, PasField.Visibility.STRICT_PROTECTED);
    }

    private static void addFirstNamespaces(List<PasEntityScope> namespaces, PasEntityScope scope, boolean includeLibrary) {
        namespaces.add(scope);
        if (scope instanceof PascalModule) {
            addUnitNamespaces(namespaces, (PascalModule) scope);
        }
        if (scope instanceof PascalStructType) {
            addParentNamespaces(namespaces, (PascalStructType) scope);
        }
    }

    private static void addUnitNamespaces(List<PasEntityScope> namespaces, PascalModule scope) {
        PasModuleStub stub = scope.getStub();
        if (null == stub) {
            LOG.info("Stub is null" + scope);
            return;
        }
        for (String unitName : stub.getUsedUnitsPublic()) {
            namespaces.addAll(findUnitsWithStub(scope.getProject(), null, unitName));
        }
    }

    private static void addParentNamespaces(@Nullable List<PasEntityScope> namespaces, @Nullable PascalStructType scope) {
        if ((null == namespaces) || (namespaces.size() > PasReferenceUtil.MAX_NAMESPACES)) {
            return;
        }
        // TODO: add all parents and implemented interfaces scopes
    }

    public static boolean isStubPowered(PascalNamedElement element) {
        return (element instanceof StubBasedPsiElementBase) && (((StubBasedPsiElementBase) element).getStub() != null);
    }
}
