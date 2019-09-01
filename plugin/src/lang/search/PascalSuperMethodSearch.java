package com.siberika.idea.pascal.lang.search;

import com.intellij.openapi.application.QueryExecutorBase;
import com.intellij.util.Processor;
import com.siberika.idea.pascal.lang.psi.PasEntityScope;
import org.jetbrains.annotations.NotNull;

public class PascalSuperMethodSearch extends QueryExecutorBase<PasEntityScope, Object> {

    @Override
    public void processQuery(@NotNull Object queryParameters, @NotNull Processor<? super PasEntityScope> consumer) {
        if (queryParameters instanceof GotoSuper.OptionsRoutine) {            // Workaround for issue https://bitbucket.org/argb32/i-pascal/issues/96/plugin-throws-casscastexception
            GotoSuper.searchForRoutine(((GotoSuper.OptionsRoutine) queryParameters).getElement()).forEach(consumer);
        }
    }

}
