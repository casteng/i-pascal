package com.siberika.idea.pascal.lang.references;

import com.intellij.navigation.ChooseByNameContributor;
import com.intellij.navigation.NavigationItem;
import com.intellij.openapi.project.Project;
import com.intellij.psi.codeStyle.MinusculeMatcher;
import com.intellij.psi.codeStyle.NameUtil;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.stubs.StubIndex;
import com.intellij.util.CommonProcessors;
import com.intellij.util.containers.SmartHashSet;
import com.siberika.idea.pascal.lang.psi.PascalNamedElement;
import com.siberika.idea.pascal.lang.psi.PascalStructType;
import com.siberika.idea.pascal.lang.stub.PascalStructIndex;
import org.jetbrains.annotations.NotNull;

/**
 * Date: 3/14/13
 * Author: George Bakhtadze
 */
public class PascalClassByNameContributor implements ChooseByNameContributor {
    @NotNull
    @Override
    public String[] getNames(Project project, boolean includeNonProjectItems) {
        CommonProcessors.CollectProcessor<String> processor = new CommonProcessors.CollectProcessor<>();
        StubIndex.getInstance().processAllKeys(PascalStructIndex.KEY, processor, getScope(project, includeNonProjectItems), null);
        return processor.getResults().toArray(new String[0]);
    }

    public static GlobalSearchScope getScope(Project project, boolean includeNonProjectItems) {
        return includeNonProjectItems ? GlobalSearchScope.allScope(project) : GlobalSearchScope.projectScope(project);
    }

    private static String keyToName(String key) {
        int ind = key.indexOf('.');
        return ResolveUtil.cleanupName(key.substring(ind + 1));
    }

    @NotNull
    @Override
    public NavigationItem[] getItemsByName(String name, String pattern, Project project, boolean includeNonProjectItems) {
        CommonProcessors.CollectProcessor<PascalNamedElement> processor = new CommonProcessors.CollectProcessor<>(new SmartHashSet<>());
        MinusculeMatcher matcher = NameUtil.buildMatcher(pattern).build();

        StubIndex.getInstance().processAllKeys(PascalStructIndex.KEY, key -> {
            if (matcher.matches(keyToName(key))) {
                StubIndex.getInstance().processElements(PascalStructIndex.KEY, key, project, getScope(project, includeNonProjectItems),
                        PascalStructType.class, processor);
            }
            return true;
        }, getScope(project, includeNonProjectItems), null);

        return processor.getResults().toArray(new NavigationItem[0]);
    }

}
