package com.siberika.idea.pascal.editor;

import com.intellij.codeHighlighting.Pass;
import com.intellij.codeInsight.daemon.GutterIconNavigationHandler;
import com.intellij.codeInsight.daemon.LineMarkerInfo;
import com.intellij.codeInsight.daemon.LineMarkerProvider;
import com.intellij.codeInsight.daemon.impl.PsiElementListNavigator;
import com.intellij.icons.AllIcons;
import com.intellij.ide.util.DefaultPsiElementCellRenderer;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.markup.GutterIconRenderer;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.NavigatablePsiElement;
import com.intellij.psi.PsiElement;
import com.intellij.util.ConstantFunction;
import com.intellij.util.SmartList;
import com.siberika.idea.pascal.lang.psi.PasEntityScope;
import com.siberika.idea.pascal.lang.psi.PasExportedRoutine;
import com.siberika.idea.pascal.lang.psi.PasInvalidScopeException;
import com.siberika.idea.pascal.lang.psi.PasModule;
import com.siberika.idea.pascal.lang.psi.PasNamedIdent;
import com.siberika.idea.pascal.lang.psi.PascalNamedElement;
import com.siberika.idea.pascal.lang.psi.impl.PasEntityScopeImpl;
import com.siberika.idea.pascal.lang.psi.impl.PasField;
import com.siberika.idea.pascal.lang.psi.impl.PasImplDeclSectionImpl;
import com.siberika.idea.pascal.lang.psi.impl.PasMethodImplDeclImpl;
import com.siberika.idea.pascal.lang.psi.impl.PascalRoutineImpl;
import com.siberika.idea.pascal.util.PsiUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.event.MouseEvent;
import java.util.Collection;
import java.util.List;

/**
 * Author: George Bakhtadze
 * Date: 05/09/2013
 */
public class PascalLineMarkerProvider implements LineMarkerProvider {

    public static final Logger LOG = Logger.getInstance(PascalLineMarkerProvider.class.getName());

    private void collectNavigationMarkers(@NotNull PsiElement element, Collection<? super LineMarkerInfo> result, PsiElement implSection, PsiElement intfSection) throws PasInvalidScopeException {
        if (element instanceof PascalRoutineImpl) {
            PascalRoutineImpl routineDecl = (PascalRoutineImpl) element;
            Collection<PsiElement> targets;
            if (routineDecl instanceof PasExportedRoutine) {
                targets = getImplementationRoutinesTargets(routineDecl, implSection);
                if (!targets.isEmpty()) {
                    result.add(createLineMarkerInfo(element, AllIcons.Gutter.ImplementedMethod, "Go to implementation", getHandler(targets)));
                }
            } else if (routineDecl instanceof PasMethodImplDeclImpl) {
                if (!StringUtil.isEmpty(routineDecl.getNamespace())) {
                    targets = getInterfaceMethodTargets(routineDecl);
                } else {
                    targets = getInterfaceRoutinesTargets(routineDecl);
                }
                if (!targets.isEmpty()) {
                    result.add(createLineMarkerInfo(element, AllIcons.Gutter.ImplementingMethod, "Go to interface", getHandler(targets)));
                }
            }

        }
    }

    public LineMarkerInfo<PsiElement> createLineMarkerInfo(@NotNull PsiElement element, Icon icon, final String tooltip,
                                                           @NotNull GutterIconNavigationHandler<PsiElement> handler) {
        LineMarkerInfo<PsiElement> info = new LineMarkerInfo<PsiElement>(element, element.getTextRange(),
                icon, Pass.UPDATE_OVERRIDEN_MARKERS,
                new ConstantFunction<PsiElement, String>(tooltip), handler,
                GutterIconRenderer.Alignment.RIGHT);
        return info;
    }

    @Nullable
    @Override
    public LineMarkerInfo getLineMarkerInfo(@NotNull PsiElement element) {
        return null;
    }

    @Override
    public void collectSlowLineMarkers(@NotNull List<PsiElement> elements, @NotNull Collection<LineMarkerInfo> result) {
        System.out.println(String.format("Line markers: size: %d", elements.size()));
        long time = System.nanoTime();
        int i = 0;
        if (elements.isEmpty()) {
            return;
        }
        try {
            PsiElement implSection = PsiUtil.getModuleImplementationSection(elements.get(0).getContainingFile());
            PsiElement intfSection = PsiUtil.getModuleInterfaceSection(elements.get(0).getContainingFile());
            for (PsiElement psiElement : elements) {
                collectNavigationMarkers(psiElement, result, implSection, intfSection);
                i++;
                if ((i % 1000) == 0) {
                    System.out.println("Elements processed: " + i);
                }
            }
        } catch (PasInvalidScopeException e) {
            e.printStackTrace();
        }
        System.out.println(String.format("Line markers done in %.3f seconds", (System.nanoTime() - time) / 1000000000.0));
    }

    private GutterIconNavigationHandler<PsiElement> getHandler(@NotNull final Collection<PsiElement> targets) {
        return new GutterIconNavigationHandler<PsiElement>() {
            @Override
            public void navigate(MouseEvent e, PsiElement elt) {
                PsiElementListNavigator.openTargets(e,
                        targets.toArray(new NavigatablePsiElement[targets.size()]),
                        "Title", null, new DefaultPsiElementCellRenderer());
            }
        };
    }

    private Collection<PsiElement> getInterfaceRoutinesTargets(PascalRoutineImpl routineDecl) {
        Collection<PsiElement> result = new SmartList<PsiElement>();
        if (null == routineDecl.getContainingFile()) {
            LOG.warn(String.format("Containing file is null for class %s, name %s", routineDecl.getClass().getSimpleName(), routineDecl.getName()));
            return result;
        }
        PsiElement section = PsiUtil.getModuleInterfaceSection(routineDecl.getContainingFile());
        if (section == null) {
            return result;
        }
        //noinspection unchecked
        for (PsiElement element : PsiUtil.findImmChildrenOfAnyType(section, PasExportedRoutine.class)) {
            PasExportedRoutine routine = (PasExportedRoutine) element;
            PasNamedIdent nameIdent = routine.getNamedIdent();
            if ((nameIdent != null) && (nameIdent.getName().equalsIgnoreCase(routineDecl.getName()))) {
                result.add(routine);
            }
        }
        return result;
    }

    private Collection<PsiElement> getInterfaceMethodTargets(@NotNull PascalRoutineImpl routineDecl) throws PasInvalidScopeException {
        Collection<PsiElement> result = new SmartList<PsiElement>();
        PasModule module = PsiUtil.getElementPasModule(routineDecl);
        if (module != null) {
            PasField typeMember = module.getField(routineDecl.getNamespace());
            if ((typeMember != null) && (typeMember.element != null) && (typeMember.fieldType == PasField.FieldType.TYPE)) {
                PasEntityScope struct = PasEntityScopeImpl.getStructByNameElement(typeMember.element);
                if (struct != null) {
                    PasField field = struct.getField(routineDecl.getNamePart());
                    if ((field != null) && (field.fieldType == PasField.FieldType.ROUTINE)) {
                        result.add(field.element);
                    }
                }
            }
        }
        return result;
    }

    @SuppressWarnings("unchecked")
    private Collection<PsiElement> getImplementationRoutinesTargets(PascalRoutineImpl routineDecl, PsiElement section) {
        Collection<PsiElement> result = new SmartList<PsiElement>();
        String name = PsiUtil.getQualifiedMethodName(routineDecl);
        findImplTargets(result, section, name, PascalRoutineImpl.class);
        return result;
    }

    @SuppressWarnings("unchecked")
    private static <T extends PascalNamedElement> void findImplTargets(Collection<PsiElement> result, PsiElement implSection, String name, Class<T> clazz) {
        if ((implSection != null) && (implSection.getParent() != null)) {
            for (PsiElement child : implSection.getChildren()) {
                if (child.getClass() == PasImplDeclSectionImpl.class) {
                    for (PsiElement element : PsiUtil.findImmChildrenOfAnyType(child, clazz)) {
                        PascalNamedElement routine = (PascalNamedElement) element;
                        if ((routine.getName().equalsIgnoreCase(name))) {
                            result.add(routine);
                        }
                    }
                }
            }
        }
    }

}
