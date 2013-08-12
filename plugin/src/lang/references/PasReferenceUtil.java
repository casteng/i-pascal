package com.siberika.idea.pascal.lang.references;

import com.intellij.psi.search.PsiElementProcessor;
import com.siberika.idea.pascal.lang.parser.PascalParserUtil;
import com.siberika.idea.pascal.lang.psi.PasModule;
import com.siberika.idea.pascal.lang.psi.PasNamespaceIdent;
import com.siberika.idea.pascal.lang.psi.PasQualifiedIdent;
import com.siberika.idea.pascal.lang.psi.PascalNamedElement;
import com.siberika.idea.pascal.util.PsiUtil;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * Author: George Bakhtadze
 * Date: 25/04/2013
 */
public class PasReferenceUtil {
    /**
     * Returns references of the given module. This can be module qualifier in identifiers and other modules.
     * @param moduleName - name element of module to find references for
     * @return array of references in nearest-first order
     */
    @SuppressWarnings("unchecked")
    public static List<PascalNamedElement> findUsedModuleReferences(@NotNull final PasNamespaceIdent moduleName) {
        final List<PascalNamedElement> result = new ArrayList<PascalNamedElement>();
        PascalParserUtil.processProjectElements(moduleName.getProject(), new PsiElementProcessor<PasModule>() {
            @Override
            public boolean execute(@NotNull PasModule element) {
                if (element.getName().equalsIgnoreCase(moduleName.getName())) {
                    result.add(element);
                    return false;
                }
                return true;
            }
        }, PasModule.class);

        for (PasQualifiedIdent element : PsiUtil.findChildrenOfAnyType(moduleName.getContainingFile(), PasQualifiedIdent.class)) {
            if (moduleName.getName().equalsIgnoreCase(element.getNamespace())) {
                result.add(element);
            }
        }

        return result;
    }
}
