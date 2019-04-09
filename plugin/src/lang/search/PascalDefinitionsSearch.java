package com.siberika.idea.pascal.lang.search;

import com.intellij.openapi.application.QueryExecutorBase;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiManager;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.search.searches.DefinitionsScopedSearch;
import com.intellij.psi.stubs.StubIndex;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.Processor;
import com.intellij.util.containers.SmartHashSet;
import com.siberika.idea.pascal.ide.actions.IntfImplNavAction;
import com.siberika.idea.pascal.ide.actions.SectionToggle;
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
import com.siberika.idea.pascal.lang.references.resolve.Resolve;
import com.siberika.idea.pascal.lang.references.resolve.ResolveProcessor;
import com.siberika.idea.pascal.lang.stub.PascalStructIndex;
import com.siberika.idea.pascal.lang.stub.StubUtil;
import com.siberika.idea.pascal.util.ModuleUtil;
import com.siberika.idea.pascal.util.PsiUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Author: George Bakhtadze
 * Date: 02/07/2015
 */
public class PascalDefinitionsSearch extends QueryExecutorBase<PasEntityScope, DefinitionsScopedSearch.SearchParameters> {

    private static final Logger LOG = Logger.getInstance(IntfImplNavAction.class.getName());

    private static final int MAX_RECURSION = 10;

    public PascalDefinitionsSearch() {
        super(true);
    }

    @Override
    public void processQuery(@NotNull DefinitionsScopedSearch.SearchParameters queryParameters, @NotNull Processor<? super PasEntityScope> consumer) {
        findImplementations(queryParameters.getElement(), consumer);
    }

    public static boolean findImplementations(PsiElement element, @NotNull Processor<? super PasEntityScope> consumer) {
        PascalRoutine routine = element instanceof PascalRoutine ? (PascalRoutine) element : PsiTreeUtil.getParentOfType(element, PascalRoutine.class);
        if (routine != null) {
            return findImplementingMethods(routine, consumer);
        } else {
            return findDescendingStructs(PsiUtil.getStructByElement(element), consumer);
        }
    }

    // Returns True if there were no consumer.process() calls
    public static boolean findImplementingMethods(PascalRoutine routine, Processor<? super PasEntityScope> consumer) {
        if (routine instanceof PasRoutineImplDecl) {
            PsiElement el = SectionToggle.retrieveDeclaration(routine, false);
            if (el instanceof PasExportedRoutine) {
                routine = (PascalRoutine) el;
            } else {
                return true;
            }
        }
        PascalRoutine finalRoutine = routine;
        PascalStructType struct = PsiUtil.getStructByElement(routine);
        return findDescendingStructs(struct, new Processor<PasEntityScope>() {
                    @Override
                    public boolean process(PasEntityScope scope) {
                        return GotoSuper.extractMethodsByName(scope, finalRoutine, false, 0, new Processor<PasEntityScope>() {
                                    @Override
                                    public boolean process(PasEntityScope scope) {
                                        return consumer.process(scope);
                                    }
                                }
                        );
                    }
                }
        );
    }

    // Returns True if there were no consumer.process() calls
    public static boolean processDescendingStructs(@Nullable Set<String> processed, PascalStructType parent, boolean recursive,
                                                   @NotNull Processor<? super PasEntityScope> consumer, int rCnt) {
        if (rCnt > MAX_RECURSION) {
            LOG.info("ERROR: Max recursion reached");
            return true;
        }
        if (null == parent) {
            return true;
        }
        final String name = ResolveUtil.cleanupName(parent.getName()).toUpperCase();
        final Project project = parent.getProject();
        final Set<String> processedParents = processed != null ? processed : new SmartHashSet<>();
        StubIndex index = StubIndex.getInstance();
        final boolean includeNonProjectItems = PsiUtil.isFromLibrary(parent);

        final GlobalSearchScope scope = PascalClassByNameContributor.getScope(project, includeNonProjectItems);
        return index.processAllKeys(PascalStructIndex.KEY, new Processor<String>() {            // ===*** TODO: try to remove this step
                    @Override
                    public boolean process(String key) {
                        return index.processElements(PascalStructIndex.KEY, key, project, scope,
                                PascalStructType.class, new Processor<PascalStructType>() {
                                    @Override
                                    public boolean process(PascalStructType type) {
                                        String uname = type.getUniqueName();
                                        List<String> parents = type.getParentNames();
                                        for (String parentToCheck : parents) {
                                            if (parentToCheck.toUpperCase().endsWith(name)) {
                                                PasEntityScope resolved = resolveParent(parent, type, parentToCheck);
                                                if (elementsEqual(parent, resolved)) {
                                                    boolean result = consumer.process(type);
                                                    if (result && recursive && !processedParents.contains(uname)) {
                                                        processedParents.add(uname);
                                                        result = processDescendingStructs(processedParents, type, true, consumer, rCnt + 1);
                                                    }
                                                    return result;
                                                }
                                            }
                                        }
                                        return true;
                                    }

                                    private boolean elementsEqual(PascalStructType struct, PasEntityScope resolved) {
                                        return (resolved != null) &&
                                                (PsiManager.getInstance(project).areElementsEquivalent(struct, resolved)
                                                        || struct.getUniqueName().equalsIgnoreCase(ResolveUtil.cleanupName(resolved.getUniqueName())));
                                    }
                                });
                    }
                },
                scope, null);
    }

    public static boolean findDescendingStructs(PascalStructType struct, Processor<? super PasEntityScope> consumer) {
        return processDescendingStructs(null, struct, true, consumer, 0);
    }

    private static PasEntityScope resolveParent(PascalStructType parent, PascalStructType descendant, String name) {
        ResolveContext ctx = new ResolveContext(descendant, PasField.TYPES_TYPE, PsiUtil.isFromLibrary(parent), null, ModuleUtil.retrieveUnitNamespaces(descendant));
        NamespaceRec rec = NamespaceRec.fromFQN(descendant, name);
        AtomicReference<PasEntityScope> result = new AtomicReference<>();
        Resolve.resolveExpr(rec, ctx, new ResolveProcessor() {
            @Override
            public boolean process(final PasEntityScope originalScope, final PasEntityScope scope, final PasField field, final PasField.FieldType type) {
                PascalNamedElement el = field.getElement();
                if (el instanceof PasGenericTypeIdent) {
                    result.set(PasReferenceUtil.resolveTypeScope(NamespaceRec.fromFQN(el, name), null, PsiUtil.isFromLibrary(parent)));
                    return false;
                } else if (ResolveUtil.isStubPowered(el)) {          // not tested
                    final ResolveContext ctx2 = new ResolveContext(StubUtil.retrieveScope((PascalStubElement) el), PasField.TYPES_TYPE, PsiUtil.isFromLibrary(parent), null, ctx.unitNamespaces);
                    PasField.ValueType types = ResolveUtil.resolveTypeWithStub((PascalStubElement) el, ctx2, 0);
                    if (types != null) {
                        result.set(types.getTypeScopeStub());
                        return false;
                    }
                }
                return true;
            }
        });
        return result.get();
    }

}
