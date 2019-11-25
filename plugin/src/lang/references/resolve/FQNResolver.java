package com.siberika.idea.pascal.lang.references.resolve;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.SmartPsiElementPointer;
import com.intellij.psi.stubs.StubElement;
import com.intellij.util.Processor;
import com.siberika.idea.pascal.lang.parser.NamespaceRec;
import com.siberika.idea.pascal.lang.parser.PascalFile;
import com.siberika.idea.pascal.lang.psi.PasEntityScope;
import com.siberika.idea.pascal.lang.psi.PasExpr;
import com.siberika.idea.pascal.lang.psi.PasExpression;
import com.siberika.idea.pascal.lang.psi.PasWithStatement;
import com.siberika.idea.pascal.lang.psi.PascalClassDecl;
import com.siberika.idea.pascal.lang.psi.PascalHelperDecl;
import com.siberika.idea.pascal.lang.psi.PascalNamedElement;
import com.siberika.idea.pascal.lang.psi.PascalRecordDecl;
import com.siberika.idea.pascal.lang.psi.PascalStructType;
import com.siberika.idea.pascal.lang.psi.PascalStubElement;
import com.siberika.idea.pascal.lang.psi.impl.PasField;
import com.siberika.idea.pascal.lang.psi.impl.PascalExpression;
import com.siberika.idea.pascal.lang.psi.impl.PascalModuleImpl;
import com.siberika.idea.pascal.lang.references.ResolveContext;
import com.siberika.idea.pascal.lang.references.ResolveUtil;
import com.siberika.idea.pascal.lang.search.Helper;
import com.siberika.idea.pascal.util.ModuleUtil;
import com.siberika.idea.pascal.util.PsiUtil;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

abstract class FQNResolver {

    private static final Logger LOG = Logger.getInstance(FQNResolver.class);

    private final PasEntityScope scope;
    final NamespaceRec fqn;
    private final ResolveContext context;
    private final List<PasEntityScope> sortedUnits;
    private boolean wasType;
    public PasField result;
    PasEntityScope lastPartScope;

    FQNResolver(final PasEntityScope scope, final NamespaceRec fqn, final ResolveContext context) {
        this.scope = scope;
        this.fqn = fqn;
        this.context = new ResolveContext(context);
        if (null == this.context.unitNamespaces) {
            this.context.unitNamespaces = ModuleUtil.retrieveUnitNamespaces(fqn.getParentIdent());
        }
        this.sortedUnits = new ArrayList<>();
        this.wasType = false;
    }

    // return True to continue processing or False to stop
    abstract boolean processField(PasEntityScope scope, PasField field);

    boolean processScope(final PasEntityScope scope, final String fieldName) {
        return processDefault(scope, fieldName);
    }

    boolean resolve(boolean firstPart) {
        if (firstPart) {
            return resolveFirst();
        } else {
            return (scope == null) || resolveNext(scope);
        }
    }

    boolean isWasType() {
        return wasType;
    }

    private boolean resolveFirst() {
        PsiFile file = fqn.getParentIdent().getContainingFile();
        boolean implAffects = !ResolveUtil.isStubPowered(context.scope) && file instanceof PascalFile
                && PsiUtil.isBefore(((PascalFile) file).getImplementationSection(), fqn.getParentIdent());
        calcScope(fqn, context);
        PasEntityScope moduleScope = context.scope;
        if (!processFirstPartScopes(fqn, context, implAffects)) {
            while (!fqn.isComplete() && (lastPartScope != null)) {
                if (resolveNext(lastPartScope)) {
                    return resolveUnits(moduleScope, implAffects);         // Nothing matches, try unit names
                }
            }
            return !fqn.isComplete();
        } else {
            return resolveUnits(moduleScope, implAffects);
        }
    }

    private boolean resolveUnits(PasEntityScope moduleScope, boolean implAffects) {
        // current name not found, search for units
        fqn.reset();

        if (moduleScope instanceof PascalModuleImpl) {
            if (implAffects) {
                addSortedUnits(((PascalModuleImpl) moduleScope).getPrivateUnits());
            }
            addSortedUnits(((PascalModuleImpl) moduleScope).getPublicUnits());
        }
        // sort namespaces by name length in reverse order to check longer named namespaces first
        sortedUnits.sort(new UnitNameLengthComparator());
        boolean res = checkForDottedUnitName(implAffects);
        if (!res) {                     // Found unit
            return !fqn.isComplete() && resolveNext(lastPartScope);
        } else {
            return true;
        }
    }

    private void addSortedUnits(List<SmartPsiElementPointer<PasEntityScope>> units) {
        for (SmartPsiElementPointer<PasEntityScope> unitPtr : units) {
            PasEntityScope unit = unitPtr.getElement();
            if ((unit != null) && (context.includeLibrary || !PsiUtil.isFromLibrary(unit))) {
                // Add unit scopes to sorted units list
                if (!StringUtils.isEmpty(unit.getName())) {
                    sortedUnits.add(unit);
                }
            }
        }
    }

    boolean processDefault(PasEntityScope scope, String fieldName) {
        if (context.ignoreNames() && fqn.isTarget()) {
            for (PasField field : scope.getAllFields()) {
                if ((isFieldSuitable(field) && !processField(scope, field))) {
                    break;                        // No need to return false for ignoreNames mode
                }
            }
            return true;
        }
        PasField field = scope.getField(fieldName);
        if (isFieldSuitable(field)) {
            /*if (field.fieldType == PasField.FieldType.ROUTINE) {
                PascalNamedElement el = field.getElement();
                if (el instanceof PascalRoutine) {
                    if (!(((PascalRoutine) el).getFormalParameterNames().isEmpty()
                            || context.options.contains(ResolveOptions.PROPERTY_SPECIFIER) && fqn.isTarget()
                    )) {   // routines with parameters allowed only when resolving a property specifier
                        return true;
                    }
                }
            }*/
            fqn.next();
            wasType = field.fieldType == PasField.FieldType.TYPE;
            if (!fqn.isComplete()) {
                lastPartScope = getScope(scope, field, field.fieldType);
                return false;
            } else {
                return processField(scope, field);
            }
        }
        return true;
    }

    boolean isFieldSuitable(PasField field) {
        return (field != null) && context.fieldTypes.contains(field.fieldType);
    }

    private boolean processScope(final PasEntityScope scope, boolean first) {
        if (context.resultScope != null) {
            context.resultScope.add(scope);
        }
        if (!processScope(scope, fqn.getCurrentName())) {       // found current name in the scope
            return false;
        }
        return true;
    }

    private boolean processFirstPartScopes(NamespaceRec fqn, ResolveContext context, boolean implAffects) {
        if (!processWithScopes(context.scope, fqn.getParentIdent())) {
            return false;
        }
        // Retrieve all namespaces affecting first FQN level
        while (context.scope != null) {
            if (!processFirstPartScopes(context, implAffects)) {
                return false;
            }
            context.scope = context.scope.getContainingScope();
        }
        return true;
    }

    private boolean processFirstPartScopes(ResolveContext context, boolean implAffects) {
        if (!processHelperScopes(context.scope) || !processScope(context.scope, true)) {
            return false;
        }
        if (context.scope instanceof PascalModuleImpl) {
            if (!StringUtils.isEmpty(context.scope.getName())) {
                sortedUnits.add(context.scope);
            }
            if (implAffects) {
                if (!processUnitScopes(((PascalModuleImpl) context.scope).getPrivateUnits(), context.includeLibrary)) {
                    return false;
                }
            }
            if (!processUnitScopes(((PascalModuleImpl) context.scope).getPublicUnits(), context.includeLibrary)) {
                return false;
            }
        }
        return processParentScopes(context.scope, true);
    }

    /*
    .prepare scopes for search:
      .add parent/helper scopes if any
    .search in scopes for name from FQN
    */
    boolean resolveNext(PasEntityScope scope) {
        if (!processHelperScopes(scope) || !processScope(scope, fqn.getCurrentName())) {       // found current name in the scope
            return false;
        }
        boolean res = processParentScopes(scope, false)
                && !(context.ignoreNames() && fqn.isTarget());
        if (context.ignoreNames() && fqn.isTarget()) {
            fqn.next();
        }
        return res;
    }

    PasEntityScope getScope(PasEntityScope scope, PasField field, PasField.FieldType type) {
        if (type == PasField.FieldType.UNIT) {
            return (PasEntityScope) field.getElement();
        }
        ResolveContext ctx = new ResolveContext(scope, PasField.TYPES_ALL, true, null, context.unitNamespaces);

        return Types.retrieveFieldTypeScope(field, ctx);
    }

    private boolean processWithScopes(PasEntityScope scope, PsiElement ident) {
        if (null == scope) {
            return true;
        }
        Collection<PasWithStatement> statements = scope.getWithStatements();
        for (PasWithStatement ws : statements) {
            if (PsiUtil.isParentOf(ident, ws.getStatement()) && PsiUtil.isParentOf(ws, scope)) {
                if (!getWithStatementScopes(ws)) {
                    return false;
                }
            }
        }
        return true;
    }

    private boolean getWithStatementScopes(PasWithStatement withElement) {
        for (PasExpression expr : withElement.getExpressionList()) {
            PasExpr expression = expr != null ? expr.getExpr() : null;
            if (expression instanceof PascalExpression) {
                List<PasField.ValueType> types = Types.getTypes((PascalExpression) expr.getExpr());
                if (!types.isEmpty()) {
                    PasEntityScope ns = Types.retrieveScope(types);
                    if (ns != null) {
                        if (!processScope(ns, false)) {
                            return false;
                        }
                        if (ns instanceof PascalStructType) {
                            for (SmartPsiElementPointer<PasEntityScope> scopePtr : ns.getParentScope()) {
                                PasEntityScope scope = scopePtr.getElement();
                                if (!processHelperScopes(scope) || !processScope(scope, false)) {
                                    return false;
                                }
                            }
                        }
                    }
                }
            }
        }
        return true;
    }

    private boolean processUnitScopes(List<SmartPsiElementPointer<PasEntityScope>> units, boolean includeLibrary) {
        for (SmartPsiElementPointer<PasEntityScope> unitPtr : units) {
            PasEntityScope unit = unitPtr.getElement();
            if ((unit != null) && (includeLibrary || !PsiUtil.isFromLibrary(unit))) {
                if (!processScope(unit, true)) {
                    return false;
                }
            }
        }
        return true;
    }

    private boolean processParentScopes(@Nullable PasEntityScope scope, boolean first) {
        if (null == scope) {
            return true;
        }
        if (scope instanceof PascalHelperDecl) {                                              // Helper's target to helper's code scope
            PascalNamedElement target = Helper.resolveTarget((PascalHelperDecl) scope);
            if (target instanceof PascalStructType) {
                if (!processScope((PasEntityScope) target, first)) {
                    return false;
                }
                processParentScopes((PasEntityScope) target, first);
            }
        }
        for (SmartPsiElementPointer<PasEntityScope> scopePtr : scope.getParentScope()) {
            PasEntityScope entityScope = scopePtr.getElement();
            if (first || (entityScope instanceof PascalStructType)) {                  // Search for parents for first namespace (method) or any for structured types
                if (null != entityScope) {
                    if (!processHelperScopes(entityScope) || !processScope(entityScope, first) || !processParentScopes(entityScope, first)) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    private boolean processHelperScopes(PasEntityScope scope) {
        AtomicBoolean result = new AtomicBoolean(true);
        if (scope instanceof PascalRecordDecl || scope instanceof PascalClassDecl) {
            Helper.getQuery((PascalStructType) scope).forEach(new Processor<PascalStructType>() {
                @Override
                public boolean process(PascalStructType helper) {
                    if (!processScope(helper, false)) {
                        result.set(false);
                        return false;
                    } else {
                        return true;
                    }
                }
            });
        }
        return result.get();
    }

    private static void calcScope(NamespaceRec fqn, ResolveContext context) {
        if (context.scope == null) {
            if (ResolveUtil.isStubPowered(fqn.getParentIdent())) {
//                LOG.info("!!!*** FQN parent is stub-powered: " + ((PascalStubElement) fqn.getParentIdent()).getName());
                StubElement stub = ((PascalStubElement) fqn.getParentIdent()).retrieveStub();
                stub = stub != null ? stub.getParentStub() : null;
                context.scope = stub != null ? (PasEntityScope) stub.getPsi() : null;
            } else {
                PsiElement fqnIdent = fqn.getParentIdent();
                context.scope = PsiUtil.getNearestAffectingScope(fqnIdent);
                if (PsiUtil.isTypeSpecialization(fqnIdent)) {      // Search for type parameters in structured type being declared if any
                    PsiElement scopeCandidate = PsiUtil.getNearestAffectingDeclarationsRoot(fqn.getParentIdent());
                    context.scope = scopeCandidate instanceof PasEntityScope ? (PasEntityScope) scopeCandidate : context.scope;
                }
            }
        }
    }

    private boolean checkForDottedUnitName(final boolean implAffects) {
        PasEntityScope res;
        res = tryUnit(fqn, sortedUnits);
        if (res != null) {
            return processScope(res, res.getName());
        } else {
            NamespaceRec oldFqn = new NamespaceRec(fqn);
            for (String prefix : context.unitNamespaces) {
                fqn.addPrefix(oldFqn, prefix);
                res = tryUnit(fqn, sortedUnits);
                if (res != null) {
                    return processScope(res, res.getName());
                }
            }
        }
        return true;
    }

    private static PasEntityScope tryUnit(NamespaceRec fqn, List<PasEntityScope> sortedUnits) {
        for (PasEntityScope namespace : sortedUnits) {
            if (fqn.advance(namespace.getName())) {
                return namespace;
            }
        }
        return null;
    }
}
