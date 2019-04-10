package com.siberika.idea.pascal.lang.references.resolve;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.SmartPsiElementPointer;
import com.intellij.psi.stubs.StubElement;
import com.siberika.idea.pascal.lang.parser.NamespaceRec;
import com.siberika.idea.pascal.lang.parser.PascalFile;
import com.siberika.idea.pascal.lang.psi.PasEntityScope;
import com.siberika.idea.pascal.lang.psi.PasExpr;
import com.siberika.idea.pascal.lang.psi.PasExpression;
import com.siberika.idea.pascal.lang.psi.PasModule;
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

abstract class FQNResolver {

    private static final Logger LOG = Logger.getInstance(FQNResolver.class);

    private final PasEntityScope scope;
    final NamespaceRec fqn;
    private final ResolveContext context;
    private final List<PasEntityScope> sortedUnits;
    private boolean wasType;
    public PasField result;

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
            return (scope != null) && resolveNext(scope);
        }
    }

    public boolean isWasType() {
        return wasType;
    }

    private boolean resolveFirst() {
        PsiFile file = fqn.getParentIdent().getContainingFile();
        boolean implAffects = !ResolveUtil.isStubPowered(context.scope) && file instanceof PascalFile
                && PsiUtil.isBefore(((PascalFile) file).getImplementationSection(), fqn.getParentIdent());
        calcScope(fqn, context);

        if (processFirstPartScopes(fqn, context, implAffects)) {         // current name not found, search for units
            fqn.reset();
            // sort namespaces by name length in reverse order to check longer named namespaces first
            sortedUnits.sort(new UnitNameLengthComparator());
            return checkForDottedUnitName(implAffects);
        } else {
            return false;
        }
    }

    boolean processDefault(PasEntityScope scope, String fieldName) {
        PasField field = scope.getField(fieldName);
        if (field != null) {
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
                PasEntityScope fieldScope = getScope(scope, field, field.fieldType);
                return (fieldScope != null) && resolveNext(fieldScope);
            } else {
                return processField(scope, field);
//                return !((null == context.matcher) || context.matcher.process(Collections.singleton(field))) || processField(scope, field);
            }
        }
        return true;
    }

    private boolean processScope(final PasEntityScope scope, boolean first) {
        if (!processScope(scope, fqn.getCurrentName())) {       // found current name in the scope
            return false;
        }
        if (first && (scope instanceof PasModule) && !StringUtils.isEmpty(scope.getName())) {
            sortedUnits.add(scope);
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
            context.scope = PsiUtil.getNearestAffectingScope(context.scope);
        }
        return true;
    }

    private boolean processFirstPartScopes(ResolveContext context, boolean implAffects) {
        processHelperScopes(context.scope);
        if (!processScope(context.scope, true)) {
            return false;
        }
        if (context.scope instanceof PascalModuleImpl) {
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
        processHelperScopes(scope);
        if (!processScope(scope, fqn.getCurrentName())) {       // found current name in the scope
            return false;
        }
        return processParentScopes(scope, false);
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
                                processHelperScopes(scope);           // TODO: ===*** is it needed?
                                if (!processScope(scope, false)) {
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
                if (!processScope(unit, true)) {        // Add unit scopes to sorted units list
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
                    processHelperScopes(entityScope);
                    if (!processScope(entityScope, first) || !processParentScopes(entityScope, first)) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    private void processHelperScopes(PasEntityScope scope) {
        if (scope instanceof PascalRecordDecl || scope instanceof PascalClassDecl) {
            Helper.getQuery((PascalStructType) scope).forEach(helper -> {
                return processScope(helper, false);
            });
        }
    }

    private static void calcScope(NamespaceRec fqn, ResolveContext context) {
        if (context.scope == null) {
            if (ResolveUtil.isStubPowered(fqn.getParentIdent())) {
                LOG.info("!!!*** FQN parent is stub-powered: " + ((PascalStubElement) fqn.getParentIdent()).getName());
                StubElement stub = ((PascalStubElement) fqn.getParentIdent()).retrieveStub();
                stub = stub != null ? stub.getParentStub() : null;
                context.scope = stub != null ? (PasEntityScope) stub.getPsi() : null;
            } else {
                context.scope = PsiUtil.getNearestAffectingScope(fqn.getParentIdent());
            }
        }
    }

    private boolean checkForDottedUnitName(final boolean implAffects) {
        PasEntityScope res;
        res = tryUnit(fqn, sortedUnits);
        if (res != null) {
            return processScope(res, res.getName());
        } else if (fqn.isTarget()) {            // don't check with prefixes if fqn has more than one level
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
