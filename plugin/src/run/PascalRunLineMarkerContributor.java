package com.siberika.idea.pascal.run;

import com.intellij.execution.lineMarker.ExecutorAction;
import com.intellij.execution.lineMarker.RunLineMarkerContributor;
import com.intellij.icons.AllIcons;
import com.intellij.psi.PsiElement;
import com.intellij.util.Function;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Author: George Bakhtadze
 * Date: 09/07/2016
 */
public class PascalRunLineMarkerContributor extends RunLineMarkerContributor {

    private static final Function<PsiElement, String> TOOLTIP_PROVIDER = new Function<PsiElement, String>() {
        public String fun(PsiElement element) {
            return "Run Program";
        }
    };

    @Nullable
    @Override
    public Info getInfo(@NotNull PsiElement element) {
        if (PascalRunContextConfigurationProducer.isProgramLeafElement(element)) {
            return new Info(AllIcons.General.Run, TOOLTIP_PROVIDER, ExecutorAction.getActions(0));
        }
        return null;
    }

}
