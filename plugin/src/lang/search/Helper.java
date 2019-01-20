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
import com.siberika.idea.pascal.lang.psi.PasClassHelperDecl;
import com.siberika.idea.pascal.lang.psi.PasRecordHelperDecl;
import com.siberika.idea.pascal.lang.psi.PascalNamedElement;
import com.siberika.idea.pascal.lang.psi.PascalStructType;
import com.siberika.idea.pascal.lang.psi.impl.PasField;
import com.siberika.idea.pascal.lang.references.PasReferenceUtil;
import com.siberika.idea.pascal.lang.references.ResolveContext;
import com.siberika.idea.pascal.lang.stub.PascalHelperIndex;
import com.siberika.idea.pascal.util.PsiUtil;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Collections;

public class Helper {
    public static Query<PascalStructType> getQuery(PascalStructType entity) {
        return new ExecutorsQuery<>(new Options(entity), Collections.singletonList(new QueryExecutor()));
    }

    public static boolean isHelperFor(PascalStructType helper, PascalStructType target) {
        String targetFqn = null;
        if (helper instanceof PasClassHelperDecl) {
            targetFqn = ((PasClassHelperDecl) helper).getTarget();
        } else if (helper instanceof PasRecordHelperDecl) {
            targetFqn = ((PasRecordHelperDecl) helper).getTarget();
        }
        if (targetFqn != null) {
            NamespaceRec fqn = NamespaceRec.fromFQN(helper, targetFqn);
            ResolveContext ctx = new ResolveContext(PasField.TYPES_TYPE, true);
            Collection<PasField> targets = PasReferenceUtil.resolve(fqn, ctx, 0);
            for (PasField field : targets) {
                PascalNamedElement resolved = field.getElement();
                if ((resolved == target) || PsiUtil.hasSameUniqueName(resolved, target) || (target.getManager().areElementsEquivalent(resolved, target))) {
                    return true;
                }
            }
        }
        return false;
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
