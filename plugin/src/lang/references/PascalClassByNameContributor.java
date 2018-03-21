package com.siberika.idea.pascal.lang.references;

import com.intellij.navigation.ChooseByNameContributor;
import com.intellij.navigation.NavigationItem;
import com.intellij.openapi.project.Project;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.stubs.StubIndex;
import com.intellij.util.Processor;
import com.intellij.util.containers.SmartHashSet;
import com.siberika.idea.pascal.lang.psi.PascalNamedElement;
import com.siberika.idea.pascal.lang.psi.PascalStructType;
import com.siberika.idea.pascal.lang.stub.PascalStructIndex;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Date: 3/14/13
 * Author: George Bakhtadze
 */
public class PascalClassByNameContributor implements ChooseByNameContributor {
    @NotNull
    @Override
    public String[] getNames(Project project, boolean includeNonProjectItems) {
        List<String> names = new ArrayList<String>();
        StubIndex.getInstance().processAllKeys(PascalStructIndex.KEY, new Processor<String>() {
            @Override
            public boolean process(String key) {
                names.add(keyToName(key));
                return true;
            }
        }, getScope(project, includeNonProjectItems), null);
        return names.toArray(new String[names.size()]);
    }

    static GlobalSearchScope getScope(Project project, boolean includeNonProjectItems) {
        return includeNonProjectItems ? GlobalSearchScope.allScope(project) : GlobalSearchScope.projectScope(project);
    }

    static String keyToName(String key) {
        int ind = key.indexOf('.');
        return key.substring(ind + 1).replaceAll("#", "");
    }

    @NotNull
    @Override
    public NavigationItem[] getItemsByName(String name, String pattern, Project project, boolean includeNonProjectItems) {
        Collection<PascalNamedElement> items = new SmartHashSet<>();
        final Pattern p = Pattern.compile("\\w*" + pattern + "\\w*");

        StubIndex.getInstance().processAllKeys(PascalStructIndex.KEY, new Processor<String>() {
            @Override
            public boolean process(final String key) {
                if (p.matcher(keyToName(key)).matches()) {
                    StubIndex.getInstance().processElements(PascalStructIndex.KEY, key, project, getScope(project, includeNonProjectItems),
                            PascalStructType.class, new Processor<PascalStructType>() {
                                @Override
                                public boolean process(PascalStructType structType) {
                                    items.add(structType);
                                    return true;
                                }
                            });
                }
                return true;
            }
        }, getScope(project, includeNonProjectItems), null);

        return items.toArray(new NavigationItem[items.size()]);
    }

}
