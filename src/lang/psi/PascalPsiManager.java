package com.siberika.idea.pascal.lang.psi;

import com.intellij.openapi.components.AbstractProjectComponent;
import com.intellij.openapi.components.ProjectComponent;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

/**
 * User: George Bakhtadze
 * Date: 12/06/12
 */
public class PascalPsiManager extends AbstractProjectComponent implements ProjectComponent {
    private static final Logger log = Logger.getInstance("pascal.PascalPsiManger");

    public PascalPsiManager(final Project project) {
        super(project);
        log.debug("*** CREATED ***");
    }

    public static PascalPsiManager getInstance(Project project) {
        return project.getComponent(PascalPsiManager.class);
    }

    @NotNull
    @Override
    public String getComponentName() {
        return "pascal.PsiManager";
    }

}
