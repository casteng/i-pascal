package com.siberika.idea.pascal.lang.references.resolve;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.psi.PsiFile;
import com.intellij.psi.SmartPsiElementPointer;
import com.intellij.psi.stubs.StubElement;
import com.intellij.util.Processor;
import com.siberika.idea.pascal.lang.parser.NamespaceRec;
import com.siberika.idea.pascal.lang.parser.PascalFile;
import com.siberika.idea.pascal.lang.psi.PasEntityScope;
import com.siberika.idea.pascal.lang.psi.PasModule;
import com.siberika.idea.pascal.lang.psi.PascalClassDecl;
import com.siberika.idea.pascal.lang.psi.PascalHelperDecl;
import com.siberika.idea.pascal.lang.psi.PascalNamedElement;
import com.siberika.idea.pascal.lang.psi.PascalRecordDecl;
import com.siberika.idea.pascal.lang.psi.PascalStructType;
import com.siberika.idea.pascal.lang.psi.PascalStubElement;
import com.siberika.idea.pascal.lang.psi.impl.PasField;
import com.siberika.idea.pascal.lang.psi.impl.PascalModuleImpl;
import com.siberika.idea.pascal.lang.references.PasReferenceUtil;
import com.siberika.idea.pascal.lang.references.ResolveContext;
import com.siberika.idea.pascal.lang.references.ResolveUtil;
import com.siberika.idea.pascal.lang.search.Helper;
import com.siberika.idea.pascal.util.PsiUtil;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

@SuppressWarnings("Convert2Lambda")
public class Resolve {

    private static final Logger LOG = Logger.getInstance(Resolve.class);

    public static boolean resolveExpr(NamespaceRec fqn, ResolveContext context, ResolveProcessor processor) {
        return false;
    }

    public static boolean resolve(final NamespaceRec fqn, ResolveContext context, ResolveProcessor processor) {
        ResolveContext ctx = new ResolveContext(context);
        if (fqn.isFirst()) {                               // TODO: remove
            ctx.options.add(ResolveOptions.FIRST_PART);
        }
        if (ctx.isFirstPart()) {
            resolveFirst(fqn, ctx, new ResolveProcessor() {
                        @Override
                        public boolean process(PasEntityScope originalScope, PasEntityScope scope, PasField field, PasField.FieldType type) {
                            if (fqn.isTarget()) {
                                return processor.process(originalScope, scope, field, type);
                            } else {
                                fqn.next();
                                if (fqn.isBeforeTarget()) {
                                    ctx.scope = getScope(originalScope, scope, field, type);
                                    return (ctx.scope != null) && resolveNext(fqn, ctx, this);
                                }
                            }
                            return true;
                        }
                    }
            );
        } else {
            return resolveNext(fqn, ctx, processor);
        }
        return true;
    }

    private static boolean resolveNext(NamespaceRec fqn, ResolveContext context, ResolveProcessor processor) {
        Processor<PasEntityScope> scopeProcessor = new Processor<PasEntityScope>() {
            @Override
            public boolean process(PasEntityScope scope) {
                PasField field = scope.getField(fqn.getCurrentName());
                if ((field != null) && !processor.process(context.scope, scope, field, field.fieldType)) {       // found current name in the scope
                    fqn.next();
                    return false;
                }
                return processParentScopes(context.scope, false, this);
            }
        };
        return scopeProcessor.process(context.scope);
    }

    private static PasEntityScope getScope(PasEntityScope originalScope, PasEntityScope scope, PasField field, PasField.FieldType type) {
        if (type == PasField.FieldType.UNIT) {
            return (PasEntityScope) field.getElement();
        }
        ResolveContext ctx = new ResolveContext(scope, PasField.TYPES_ALL, true, null, null);
        return PasReferenceUtil.retrieveFieldTypeScope(field, ctx);
    }

    private static void resolveFirst(NamespaceRec fqn, ResolveContext context, ResolveProcessor processor) {
        Set<PasField.FieldType> fieldTypes = EnumSet.copyOf(context.fieldTypes);
        PsiFile file = fqn.getParentIdent().getContainingFile();
        boolean implAffects = !ResolveUtil.isStubPowered(context.scope) && file instanceof PascalFile
                && PsiUtil.isBefore(((PascalFile) file).getImplementationSection(), fqn.getParentIdent());
        calcScope(fqn, context);

        PasEntityScope originalScope = context.scope;

        List<PasEntityScope> sortedUnits = new ArrayList<>();
        // TODO: handle WITH
        if (processFirstPartScopes(fqn, context, implAffects, new Processor<PasEntityScope>() {
            @Override
            public boolean process(PasEntityScope scope) {
                PasField field = scope.getField(fqn.getCurrentName());
                if ((field != null) && !processor.process(originalScope, scope, field, field.fieldType)) {       // found current name in the scope
                    return false;
                }
                if ((scope instanceof PasModule) && !StringUtils.isEmpty(scope.getName())) {
                    sortedUnits.add(scope);
                }
                return true;
            }
        })) {                                                                                     // current name not found
            // sort namespaces by name length in reverse order to check longer named namespaces first
            sortedUnits.sort(new UnitNameLengthComparator());
            ResolveUnit.checkForDottedUnitName(fqn, context, processor, sortedUnits);
        }
    }

    private static boolean processFirstPartScopes(NamespaceRec fqn, ResolveContext context, boolean implAffects, Processor<PasEntityScope> processor) {
        // Retrieve all namespaces affecting first FQN level
        while (context.scope != null) {
            if (!processFirstPartScopes(context, implAffects, processor)) {
                return false;
            }
            context.scope = PsiUtil.getNearestAffectingScope(context.scope);
        }
        return true;
    }

    private static boolean processFirstPartScopes(ResolveContext context, boolean implAffects, Processor<PasEntityScope> processor) {
        processHelperScopes(context.scope, processor);
        if (!processor.process(context.scope)) {
            return false;
        }
        if (context.scope instanceof PascalModuleImpl) {
            if (implAffects) {
                if (!processUnitScopes(((PascalModuleImpl) context.scope).getPrivateUnits(), context.includeLibrary, processor)) {
                    return false;
                }
            }
            if (!processUnitScopes(((PascalModuleImpl) context.scope).getPublicUnits(), context.includeLibrary, processor)) {
                return false;
            }
        }
        return processParentScopes(context.scope, true, processor);
    }

    private static boolean processUnitScopes(List<SmartPsiElementPointer<PasEntityScope>> units, boolean includeLibrary, Processor<PasEntityScope> processor) {
        for (SmartPsiElementPointer<PasEntityScope> unitPtr : units) {
            PasEntityScope unit = unitPtr.getElement();
            if ((unit != null) && (includeLibrary || !PsiUtil.isFromLibrary(unit))) {
                if (!processor.process(unit)) {
                    return false;
                }
            }
        }
        return true;
    }

    private static boolean processParentScopes(@Nullable PasEntityScope scope, boolean first, Processor<PasEntityScope> processor) {
        if (null == scope) {
            return true;
        }
        if (scope instanceof PascalHelperDecl) {                                              // Helper's target to helper's code scope
            PascalNamedElement target = Helper.resolveTarget((PascalHelperDecl) scope);
            if (target instanceof PascalStructType) {
                if (processor.process((PasEntityScope) target)) {
                    return false;
                }
                processParentScopes((PasEntityScope) target, first, processor);
            }
        }
        for (SmartPsiElementPointer<PasEntityScope> scopePtr : scope.getParentScope()) {
            PasEntityScope entityScope = scopePtr.getElement();
            if (first || (entityScope instanceof PascalStructType)) {                  // Search for parents for first namespace (method) or any for structured types
                if (null != entityScope) {
                    processHelperScopes(scope, processor);
                    if (!processor.process(entityScope)) {
                        return false;
                    }
                    processParentScopes(entityScope, first, processor);
                }
            }
        }
        return true;
    }

    private static void processHelperScopes(PasEntityScope scope, Processor<PasEntityScope> processor) {
        if (scope instanceof PascalRecordDecl || scope instanceof PascalClassDecl) {
            Helper.getQuery((PascalStructType) scope).forEach(helper -> {
                return processor.process(helper);
            });
        }
    }

    private static void calcScope(NamespaceRec fqn, ResolveContext context) {
        if (context.scope == null) {
            if (fqn.getParentIdent() instanceof PascalStubElement) {
                LOG.info("!!!*** FQN parent is a stub element: " + ((PascalStubElement) fqn.getParentIdent()).getName());
                StubElement stub = ((PascalStubElement) fqn.getParentIdent()).retrieveStub();
                stub = stub != null ? stub.getParentStub() : null;
                context.scope = stub != null ? (PasEntityScope) stub.getPsi() : null;
            } else {
                context.scope = PsiUtil.getNearestAffectingScope(fqn.getParentIdent());
            }
        }
    }

}
