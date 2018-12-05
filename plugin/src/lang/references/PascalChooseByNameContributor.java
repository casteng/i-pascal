package com.siberika.idea.pascal.lang.references;

import com.intellij.navigation.ChooseByNameContributor;
import com.intellij.navigation.NavigationItem;
import com.intellij.openapi.project.Project;
import com.intellij.psi.codeStyle.MinusculeMatcher;
import com.intellij.psi.codeStyle.NameUtil;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.stubs.StubIndex;
import com.intellij.psi.stubs.StubIndexKey;
import com.intellij.util.Processor;
import com.intellij.util.containers.SmartHashSet;
import com.siberika.idea.pascal.lang.psi.PascalNamedElement;
import com.siberika.idea.pascal.lang.stub.PascalSymbolIndex;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Date: 3/14/13
 * Author: George Bakhtadze
 */
public class PascalChooseByNameContributor implements ChooseByNameContributor {
    @NotNull
    @Override
    public String[] getNames(Project project, boolean includeNonProjectItems) {
        List<String> names = new ArrayList<String>();
        StubIndex.getInstance().processAllKeys(PascalSymbolIndex.KEY, new Processor<String>() {
            @Override
            public boolean process(String key) {
                names.add(key);
                return true;
            }
        }, PascalClassByNameContributor.getScope(project, includeNonProjectItems), null);
        return names.toArray(new String[0]);
    }

    @NotNull
    @Override
    public NavigationItem[] getItemsByName(String name, String pattern, Project project, boolean includeNonProjectItems) {
        Collection<PascalNamedElement> items = new SmartHashSet<>();
        processByName(PascalSymbolIndex.KEY, pattern, project, includeNonProjectItems, new Processor<PascalNamedElement>() {
            @Override
            public boolean process(PascalNamedElement namedElement) {
                items.add(namedElement);
                return true;
            }
        });
        return items.toArray(new NavigationItem[0]);
    }

    public static void processByName(StubIndexKey<String, PascalNamedElement> indexKey, String pattern, Project project, boolean includeNonProjectItems, Processor<PascalNamedElement> processor) {
        MinusculeMatcher matcher = NameUtil.buildMatcher(pattern).build();

        final GlobalSearchScope scope = PascalClassByNameContributor.getScope(project, includeNonProjectItems);
        StubIndex.getInstance().processAllKeys(indexKey, new Processor<String>() {
            @Override
            public boolean process(final String key) {
                if (matcher.matches(key)) {
                    StubIndex.getInstance().processElements(indexKey, key, project, scope, PascalNamedElement.class, processor);
                }
                return true;
            }
        }, scope, null);
    }

}
