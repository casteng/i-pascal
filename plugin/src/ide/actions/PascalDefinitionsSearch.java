package com.siberika.idea.pascal.ide.actions;

import com.intellij.openapi.application.QueryExecutorBase;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiManager;
import com.intellij.psi.search.searches.DefinitionsScopedSearch;
import com.intellij.psi.stubs.StubIndex;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.Processor;
import com.siberika.idea.pascal.lang.parser.NamespaceRec;
import com.siberika.idea.pascal.lang.psi.PasEntityScope;
import com.siberika.idea.pascal.lang.psi.PasExportedRoutine;
import com.siberika.idea.pascal.lang.psi.PasGenericTypeIdent;
import com.siberika.idea.pascal.lang.psi.PasRoutineImplDecl;
import com.siberika.idea.pascal.lang.psi.PascalNamedElement;
import com.siberika.idea.pascal.lang.psi.PascalRoutine;
import com.siberika.idea.pascal.lang.psi.PascalStructType;
import com.siberika.idea.pascal.lang.psi.PascalStubElement;
import com.siberika.idea.pascal.lang.psi.impl.PasField;
import com.siberika.idea.pascal.lang.references.PasReferenceUtil;
import com.siberika.idea.pascal.lang.references.PascalClassByNameContributor;
import com.siberika.idea.pascal.lang.references.ResolveContext;
import com.siberika.idea.pascal.lang.references.ResolveUtil;
import com.siberika.idea.pascal.lang.stub.PascalStructIndex;
import com.siberika.idea.pascal.lang.stub.StubUtil;
import com.siberika.idea.pascal.util.PsiUtil;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;

/**
 * Author: George Bakhtadze
 * Date: 02/07/2015
 */
public class PascalDefinitionsSearch extends QueryExecutorBase<PsiElement, DefinitionsScopedSearch.SearchParameters> {

    private static final Logger LOG = Logger.getInstance(IntfImplNavAction.class.getName());

    private static final int MAX_RECURSION = 10;

    public PascalDefinitionsSearch() {
        super(true);
    }

    @Override
    public void processQuery(@NotNull DefinitionsScopedSearch.SearchParameters queryParameters, @NotNull Processor<? super PsiElement> consumer) {
        Collection<PasEntityScope> targets = findImplementations(queryParameters.getElement(), GotoSuper.LIMIT_NONE, 0);
        for (PsiElement target : targets) {
            consumer.process(target);
        }
    }

    public static Collection<PasEntityScope> findImplementations(PsiElement element, Integer limit, int rCnt) {
        Collection<PasEntityScope> targets = new LinkedHashSet<PasEntityScope>();
        PascalRoutine routine = element instanceof PascalRoutine ? (PascalRoutine) element : PsiTreeUtil.getParentOfType(element, PascalRoutine.class);
        if (routine != null) {
            findImplementingMethods(targets, routine, limit, 0);
        } else {
            findDescendingStructs(targets, PsiUtil.getStructByElement(element), limit, 0);
        }
        return targets;
    }

    public static void findImplementingMethods(Collection<PasEntityScope> targets, PascalRoutine routine, Integer limit, int rCnt) {
        Collection<PasEntityScope> found = new LinkedHashSet<PasEntityScope>();
        Collection<PasEntityScope> scopes = new LinkedHashSet<PasEntityScope>();
        if (routine instanceof PasRoutineImplDecl) {
            PsiElement el = SectionToggle.retrieveDeclaration(routine, false);
            if (el instanceof PasExportedRoutine) {
                routine = (PascalRoutine) el;
            } else {
                return;
            }
        }
        PascalStructType struct = PsiUtil.getStructByElement(routine);
        findDescendingStructs(scopes, struct, limit != null ? GotoSuper.LIMIT_FIRST_ATTEMPT : GotoSuper.LIMIT_NONE, rCnt);
        GotoSuper.extractMethodsByName(found, scopes, routine, false, GotoSuper.calcRemainingLimit(targets, limit), 0);
        if ((limit != null) && (scopes.size() == GotoSuper.LIMIT_FIRST_ATTEMPT) && (found.size() < limit)) {        // second attempt if the first one not found all results
            scopes = new LinkedHashSet<PasEntityScope>();
            findDescendingStructs(scopes, PsiUtil.getStructByElement(routine), GotoSuper.LIMIT_NONE, rCnt);
            GotoSuper.extractMethodsByName(targets, scopes, routine, false, GotoSuper.calcRemainingLimit(targets, limit), 0);
        } else {
            targets.addAll(found);
        }
    }

    public static void findDescendingStructs(Collection<PasEntityScope> targets, PascalStructType struct, Integer limit, int rCnt) {
        if ((limit != null) && (limit <= 0)) {
            return;
        }
        if (rCnt > MAX_RECURSION) {
            LOG.info("ERROR: Max recursion reached");
            return;
        }
        if (null == struct) {
            return;
        }

        final String name = ResolveUtil.cleanupName(struct.getName()).toUpperCase();
        final Project project = struct.getProject();

        StubIndex index = StubIndex.getInstance();

        final boolean includeNonProjectItems = PsiUtil.isFromLibrary(struct);

        index.processAllKeys(PascalStructIndex.KEY, new Processor<String>() {
                    @Override
                    public boolean process(String key) {
                        index.processElements(PascalStructIndex.KEY, key, project, PascalClassByNameContributor.getScope(project, includeNonProjectItems),
                                PascalStructType.class, new Processor<PascalStructType>() {
                                    @Override
                                    public boolean process(PascalStructType type) {
                                        List<String> parents = type.getParentNames();
                                        for (String parent : parents) {
                                            if (parent.toUpperCase().endsWith(name)) {
                                                PasEntityScope resolved = resolveParent(struct, type, parent);
                                                if (elementsEqual(struct, resolved)) {
                                                    targets.add(type);
                                                    findDescendingStructs(targets, type, GotoSuper.calcRemainingLimit(targets, limit), rCnt + 1);
                                                }
                                            }
                                        }
                                        return ((null == limit) || (limit > targets.size()));
                                    }

                                    private boolean elementsEqual(PascalStructType struct, PasEntityScope resolved) {
                                        return (resolved != null) &&
                                                (PsiManager.getInstance(project).areElementsEquivalent(struct, resolved)
                                                        || struct.getUniqueName().equalsIgnoreCase(ResolveUtil.cleanupName(resolved.getUniqueName())));
                                    }
                                });
                        return ((null == limit) || (limit > targets.size()));
                    }
                },
                PascalClassByNameContributor.getScope(project, includeNonProjectItems), null);
    }

    private static PasEntityScope resolveParent(PascalStructType parent, PascalStructType descendant, String name) {
        ResolveContext ctx = new ResolveContext(descendant, PasField.TYPES_TYPE, PsiUtil.isFromLibrary(parent), null, null);
        NamespaceRec rec = NamespaceRec.fromFQN(descendant, name);
        Collection<PasField> fields = PasReferenceUtil.resolve(rec, ctx, 0);
        for (PasField field : fields) {
            PascalNamedElement el = field.getElement();
            if (el instanceof PasGenericTypeIdent) {
                return PasReferenceUtil.resolveTypeScope(NamespaceRec.fromFQN(el, name), null, PsiUtil.isFromLibrary(parent));
            } else if (ResolveUtil.isStubPowered(el)) {          // not tested
                ctx = new ResolveContext(StubUtil.retrieveScope((PascalStubElement) el), PasField.TYPES_TYPE, PsiUtil.isFromLibrary(parent), null, ctx.unitNamespaces);
                PasField.ValueType types = ResolveUtil.resolveTypeWithStub((PascalStubElement) el, ctx, 0);
                if (types != null) {
                    return types.getTypeScopeStub();
                }
            }
        }
        return null;
    }

}
