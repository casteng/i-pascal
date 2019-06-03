package com.siberika.idea.pascal.lang.search;

import com.intellij.openapi.application.QueryExecutorBase;
import com.intellij.util.Processor;
import com.siberika.idea.pascal.lang.psi.PasEntityScope;
import org.jetbrains.annotations.NotNull;

public class PascalSuperMethodSearch extends QueryExecutorBase<PasEntityScope, GotoSuper.OptionsRoutine> {

    @Override
    public void processQuery(@NotNull GotoSuper.OptionsRoutine queryParameters, @NotNull Processor<? super PasEntityScope> consumer) {
        GotoSuper.searchForRoutine(queryParameters.getElement()).forEach(consumer);
    }

}
