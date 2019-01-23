package com.siberika.idea.pascal.lang.search;

import com.intellij.openapi.application.QueryExecutorBase;
import com.intellij.openapi.application.ReadAction;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.stubs.StubIndex;
import com.intellij.psi.util.PsiUtilCore;
import com.intellij.util.ExecutorsQuery;
import com.intellij.util.Processor;
import com.intellij.util.Query;
import com.siberika.idea.pascal.lang.parser.NamespaceRec;
import com.siberika.idea.pascal.lang.psi.PascalHelperDecl;
import com.siberika.idea.pascal.lang.psi.PascalNamedElement;
import com.siberika.idea.pascal.lang.psi.PascalStructType;
import com.siberika.idea.pascal.lang.references.PasReferenceUtil;
import com.siberika.idea.pascal.lang.stub.PascalHelperIndex;
import com.siberika.idea.pascal.util.PsiUtil;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;

public class Helper {
    public static Query<PascalStructType> getQuery(PascalStructType entity) {
        return new ExecutorsQuery<>(new Options(entity), Collections.singletonList(new QueryExecutor()));
    }

    public static boolean isHelperFor(PascalStructType helper, PascalStructType target) {
        PascalNamedElement resolved = resolveTarget(helper);
        return (resolved == target) || PsiUtil.hasSameUniqueName(resolved, target) || (target.getManager().areElementsEquivalent(resolved, target));
    }

    public static PascalNamedElement resolveTarget(PascalStructType helper) {
        String targetFqn = null;
        if (helper instanceof PascalHelperDecl) {
            targetFqn = ((PascalHelperDecl) helper).getTarget();
        }
        if (targetFqn != null) {
            NamespaceRec fqn = NamespaceRec.fromFQN(helper, targetFqn);
            return PasReferenceUtil.resolveTypeScope(fqn, null, true);
        }
        return null;
    }

    public static boolean hasHelpers(PascalStructType element) {
        return getQuery(element).findFirst() != null;
    }

    private static class Options {
        @NotNull private final PascalStructType element;
        @NotNull private final GlobalSearchScope scope;

        private Options(@NotNull PascalStructType element) {
            this.element = element;
            this.scope = GlobalSearchScope.allScope(PsiUtilCore.getProjectInReadAction(element));
        }
    }

    private static class QueryExecutor extends QueryExecutorBase<PascalStructType, Options> {

        QueryExecutor() {
            super(true);
        }

        @Override
        public void processQuery(@NotNull Options queryParameters, @NotNull Processor<? super PascalStructType> consumer) {
            String name = queryParameters.element.getName().toUpperCase();
            ReadAction.run(() -> StubIndex.getInstance().processElements(
                    PascalHelperIndex.KEY, name, queryParameters.element.getProject(), queryParameters.scope, PascalStructType.class, new Processor<PascalStructType>() {
                        @Override
                        public boolean process(PascalStructType structType) {
                            if (isHelperFor(structType, queryParameters.element)) {
                                return consumer.process(structType);
                            }
                            return true;
                        }
                    }));
        }
    }
}
