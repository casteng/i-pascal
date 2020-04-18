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
import com.intellij.util.SmartList;
import com.intellij.util.containers.SmartHashSet;
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

    private static final Logger LOG = Logger.getInstance(PascalDefinitionsSearch.class.getName());

    private static final int MAX_RECURSION = 10;

    PascalDefinitionsSearch() {
        super(true);
    }

    @Override
    public void processQuery(@NotNull DefinitionsScopedSearch.SearchParameters queryParameters, @NotNull Processor<? super PasEntityScope> consumer) {
        findImplementations(queryParameters.getElement(), consumer);
    }

    /**
     * Find all descending implementations of a method or structured type and call processor for each
     * @param element     - element within a method or a structured type
     * @param processor   - processor to call for each child found entity - should not use indexes!
     * @return True if there were no processor.process() calls or all of them returned True
     */
    public static boolean findImplementations(PsiElement element, @NotNull Processor<? super PasEntityScope> processor) {
        PascalRoutine routine = element instanceof PascalRoutine ? (PascalRoutine) element : PsiTreeUtil.getParentOfType(element, PascalRoutine.class);
        if (routine != null) {
            return findImplementingMethods(routine, processor);
        } else {
            return processDescendingStructs(PsiUtil.getStructByElement(element), true, processor);
        }
    }

    /**
     * Find all descending implementations of a method and call processor for each
     * @param routine     - parent method
     * @param processor   - processor to call for each child found method - should not use indexes!
     * @return True if there were no processor.process() calls or all of them returned True
     */
    public static boolean findImplementingMethods(PascalRoutine routine, Processor<? super PasEntityScope> processor) {
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
        return processDescendingStructs(struct, true, new Processor<PasEntityScope>() {
                    @Override
                    public boolean process(PasEntityScope scope) {
                        return GotoSuper.extractMethodsByName(scope, finalRoutine, false, 0, new Processor<PasEntityScope>() {
                                    @Override
                                    public boolean process(PasEntityScope scope) {
                                        return processor.process(scope);
                                    }
                                }
                        );
                    }
                }
        );
    }

    /**
     * Find immediate child types of the given parent structured type and calls processor for each
     * @param parent    - parent to find childs of
     * @param recursive - if True the search will be ran for each child recursively
     * @param processor - processor to call for each child type - should not use indexes!
     * @return True if there were no processor.process() calls or all of them returned True
     **/
    public static boolean processDescendingStructs(PascalStructType parent, boolean recursive, Processor<? super PasEntityScope> processor) {
        return processDescendingStructs(null, parent, recursive, processor, 0);
    }

    private static boolean processDescendingStructs(@Nullable Set<String> processed, PascalStructType parent, boolean recursive,
                                                   @NotNull Processor<? super PasEntityScope> processor, int rCnt) {
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
        final List<PascalStructType> toProcessRecursive = recursive ? new SmartList<>() : null;
        StubIndex index = StubIndex.getInstance();
        final boolean includeNonProjectItems = PsiUtil.isFromLibrary(parent);

        final GlobalSearchScope scope = PascalClassByNameContributor.getScope(project, includeNonProjectItems);
        boolean result = index.processAllKeys(PascalStructIndex.KEY, new Processor<String>() {
                    @Override
                    public boolean process(String key) {
                        for (PascalStructType type : StubIndex.getElements(PascalStructIndex.KEY, key, project, scope, PascalStructType.class)) {
                            String uname = type.getUniqueName();
                            List<String> parents = type.getParentNames();
                            for (String parentToCheck : parents) {
                                if (parentToCheck.toUpperCase().endsWith(name)) {
                                    PasEntityScope resolved = resolveParent(parent, type, parentToCheck);
                                    if (elementsEqual(project, parent, resolved)) {
                                        if (!processor.process(type)) {
                                            return false;
                                        }
                                        if (recursive && !processedParents.contains(uname)) {
                                            processedParents.add(uname);
                                            toProcessRecursive.add(type);
                                        }
                                    }
                                }
                            }
                        }
                        return true;
                    }
                },
                scope, null);
        if (recursive) {
            for (PascalStructType type : toProcessRecursive) {
                if (!processDescendingStructs(processedParents, type, true, processor, rCnt + 1)) {
                    return false;
                }
            }
        }
        return result;
    }

    private static boolean elementsEqual(Project project, PascalStructType struct, PasEntityScope resolved) {
        return (resolved != null) &&
                (PsiManager.getInstance(project).areElementsEquivalent(struct, resolved)
                        || struct.getUniqueName().equalsIgnoreCase(ResolveUtil.cleanupName(resolved.getUniqueName())));
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
