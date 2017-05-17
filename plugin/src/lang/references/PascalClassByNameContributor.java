package com.siberika.idea.pascal.lang.references;

import com.intellij.navigation.ChooseByNameContributor;
import com.intellij.navigation.NavigationItem;
import com.intellij.openapi.project.Project;
import com.siberika.idea.pascal.lang.parser.PascalParserUtil;
import com.siberika.idea.pascal.lang.psi.PascalNamedElement;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

/**
 * Date: 3/14/13
 * Author: George Bakhtadze
 */
public class PascalClassByNameContributor implements ChooseByNameContributor {
    @NotNull
    @Override
    public String[] getNames(Project project, boolean includeNonProjectItems) {
        return new String[0];
        /*Collection<PascalNamedElement> properties = PascalParserUtil.findClasses(project, "");
        List<String> names = new ArrayList<String>(properties.size());
        for (PascalNamedElement property : properties) {
            if (property.getName().length() > 0) {
                names.add(property.getName());
            }
        }
        return names.toArray(new String[names.size()]);*/
    }

    @NotNull
    @Override
    public NavigationItem[] getItemsByName(String name, String pattern, Project project, boolean includeNonProjectItems) {
        // todo include non project items
        Collection<PascalNamedElement> items = PascalParserUtil.findClasses(project, pattern);
        return items.toArray(new NavigationItem[items.size()]);
    }

}
