package com.siberika.idea.pascal.lang.references;

import com.intellij.navigation.ChooseByNameContributor;
import com.intellij.navigation.NavigationItem;
import com.intellij.openapi.project.Project;
import com.intellij.psi.codeStyle.MinusculeMatcher;
import com.intellij.psi.codeStyle.NameUtil;
import com.intellij.psi.stubs.StubIndex;
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
        MinusculeMatcher matcher = NameUtil.buildMatcher(pattern).build();

        StubIndex.getInstance().processAllKeys(PascalSymbolIndex.KEY, new Processor<String>() {
            @Override
            public boolean process(final String key) {
                if (matcher.matches(key)) {
                    StubIndex.getInstance().processElements(PascalSymbolIndex.KEY, key, project, PascalClassByNameContributor.getScope(project, includeNonProjectItems),
                            PascalNamedElement.class, new Processor<PascalNamedElement>() {
                                @Override
                                public boolean process(PascalNamedElement namedElement) {
                                    items.add(namedElement);
                                    return true;
                                }
                            });
                }
                return true;
            }
        }, PascalClassByNameContributor.getScope(project, includeNonProjectItems), null);

        return items.toArray(new NavigationItem[0]);
    }
}
