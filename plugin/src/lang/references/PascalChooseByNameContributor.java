package com.siberika.idea.pascal.lang.references;

import com.intellij.navigation.ChooseByNameContributor;
import com.intellij.navigation.NavigationItem;
import com.intellij.openapi.project.Project;
import com.intellij.psi.stubs.StubIndex;
import com.intellij.util.Processor;
import com.intellij.util.containers.SmartHashSet;
import com.siberika.idea.pascal.lang.psi.PascalNamedElement;
import com.siberika.idea.pascal.lang.stub.PascalSymbolIndex;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.regex.Pattern;

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
        return names.toArray(new String[names.size()]);
    }

    @NotNull
    @Override
    public NavigationItem[] getItemsByName(String name, String pattern, Project project, boolean includeNonProjectItems) {
        Collection<PascalNamedElement> items = new SmartHashSet<>();
        final Pattern p = Pattern.compile("\\w*" + pattern + "\\w*");

        StubIndex.getInstance().processAllKeys(PascalSymbolIndex.KEY, new Processor<String>() {
            @Override
            public boolean process(final String key) {
                if (p.matcher(key).matches()) {
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

        return items.toArray(new NavigationItem[items.size()]);
    }
}
