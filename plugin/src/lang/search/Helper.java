package com.siberika.idea.pascal.lang.search;

import com.intellij.openapi.application.QueryExecutorBase;
import com.intellij.openapi.application.ReadAction;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.stubs.StubIndex;
import com.intellij.util.ExecutorsQuery;
import com.intellij.util.Processor;
import com.intellij.util.Query;
import com.siberika.idea.pascal.lang.psi.PascalStructType;
import com.siberika.idea.pascal.lang.stub.PascalHelperIndex;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;

public class Helper {
    public static Query<PascalStructType> getQuery(PascalStructType entity, GlobalSearchScope scope) {
        return new ExecutorsQuery<>(new Options(entity, scope), Collections.singletonList(new QueryExecutor()));
    }

    private static class Options {
        @NotNull private final PascalStructType element;
        @NotNull private final GlobalSearchScope scope;

        private Options(@NotNull PascalStructType element, @NotNull GlobalSearchScope scope) {
            this.element = element;
            this.scope = scope;
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
                    PascalHelperIndex.KEY, name, queryParameters.element.getProject(), queryParameters.scope, PascalStructType.class, consumer));
        }
    }
}
