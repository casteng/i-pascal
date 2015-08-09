package com.siberika.idea.pascal.ide.actions;

import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.SmartList;
import com.intellij.util.containers.SmartHashSet;
import com.siberika.idea.pascal.lang.psi.PasEntityScope;
import com.siberika.idea.pascal.lang.psi.PasExportedRoutine;
import com.siberika.idea.pascal.lang.psi.PasRoutineImplDecl;
import com.siberika.idea.pascal.lang.psi.PasUsesClause;
import com.siberika.idea.pascal.lang.psi.PascalStructType;
import com.siberika.idea.pascal.lang.psi.impl.PasField;
import com.siberika.idea.pascal.lang.psi.impl.PasModuleImpl;
import com.siberika.idea.pascal.lang.psi.impl.PascalModuleImpl;
import com.siberika.idea.pascal.lang.psi.impl.PascalRoutineImpl;
import com.siberika.idea.pascal.util.PsiUtil;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

/**
 * Author: George Bakhtadze
 * Date: 31/07/2015
 */
public class SectionToggle {

    public static void getStructTarget(Collection<PsiElement> targets, PsiElement element) {
        PascalStructType struct = PsiUtil.getStructByElement(element);
        if (struct != null) {
            Container cont = calcPrefix(new Container(struct));
            retrieveFirstImplementations(targets, cont);
        }
    }

    public static PsiElement getUsesTarget(PasUsesClause usesClause) {
        if (usesClause != null) {
            PsiElement impl = PsiUtil.getModuleImplementationSection(usesClause.getContainingFile());
            PsiElement intf = PsiUtil.getModuleInterfaceSection(usesClause.getContainingFile());
            if ((impl != null) && (intf != null)) {
                if (PsiUtil.isParentOf(usesClause, impl)) {
                    return PsiTreeUtil.findChildOfType(intf, PasUsesClause.class);
                } else if (PsiUtil.isParentOf(usesClause, intf)) {
                    return PsiTreeUtil.findChildOfType(impl, PasUsesClause.class);
                }
            }
        }
        return null;
    }

    public static PsiElement getRoutineTarget(PascalRoutineImpl routine) {
        Container cont = calcPrefix(new Container(routine));
        if (routine instanceof PasExportedRoutine) {
            return retrieveImplementation(cont);
        } else if (routine instanceof PasRoutineImplDecl) {
            return retrieveDeclaration(cont);
        }
        return null;
    }

    @Nullable
    private static PsiElement retrieveImplementation(Container container) {
        if (null == container) {
            return null;
        }
        PasField field = null;
        if (container.scope instanceof PasModuleImpl) {
            field = ((PasModuleImpl) container.scope).getPrivateField(container.prefix + PsiUtil.getFieldName(container.element));
        }
        return field != null ? field.element : null;
    }

    private static void retrieveFirstImplementations(Collection<PsiElement> targets, Container container) {
        if (null == container) {
            return;
        }
        if (container.scope instanceof PasModuleImpl) {
            String prefix = (container.prefix + container.element.getName()).toUpperCase();
            Set<PasField> res = new TreeSet<PasField>(new Comparator<PasField>() {
                @Override
                public int compare(PasField o1, PasField o2) {
                    if ((null == o1.element) || (null == o2.element)) {
                        return 0;
                    }
                    return o1.element.getTextOffset() - o2.element.getTextOffset();
                }
            });
            for (PasField field : container.scope.getAllFields()) {
                if ((field.fieldType == PasField.FieldType.ROUTINE) && (field.name.toUpperCase().startsWith(prefix))) {
                    res.add(field);
                }
            }
            if (!res.isEmpty()) {
                targets.add(res.iterator().next().element);
            }
        }
    }

    @Nullable
    private static PsiElement retrieveDeclaration(Container container) {
        if (null == container) {
            return null;
        }
        PasField field = null;
        PasEntityScope scope = container.element.getContainingScope();
        if (scope != null) {
            String ns = container.element.getNamespace();
            String name = PsiUtil.getFieldName(container.element).substring(StringUtils.isEmpty(ns) ? 0 : ns.length() + 1);
            if (scope instanceof PasModuleImpl) {
                field = ((PasModuleImpl) scope).getPublicField(name);
            } else {
                field = scope.getField(name);
            }
        }
        return field != null ? field.element : null;
    }

    public static String getPrefix(PasEntityScope scope) {
        return calcPrefix(new Container(scope)).prefix;
    }

    private static Container calcPrefix(Container current) {
        while ((current.scope != null) && !(current.scope instanceof PascalModuleImpl)) {
            current.scope = findOwner(current.scope);
            if (current.scope instanceof PascalStructType) {
                current.prefix = current.scope.getName() + "." + current.prefix;
            } else if (current.scope instanceof PascalRoutineImpl) {
                current.element = current.scope;
            }
        }
        return current;
    }

    private static PasEntityScope findOwner(PasEntityScope scope) {
        return scope.getContainingScope();
    }

    private static class Container {
        String prefix = "";
        PasEntityScope element;
        PasEntityScope scope;

        public Container(PasEntityScope element) {
            this.element = element;
            this.scope = element;
        }
    }

    public static int findImplPos(PascalRoutineImpl routine) {
        // collect all routine/method declarations in module/structure
        // remember index of the given routine declaration
        int res = -1;
        int ind = -1;
        List<PascalRoutineImpl> decls = new SmartList<PascalRoutineImpl>();
        Set<PascalRoutineImpl> declSet = new SmartHashSet<PascalRoutineImpl>();
        PasEntityScope scope = routine.getContainingScope();
        if (scope != null) {
            Collection<PasField> fields;
            if (scope instanceof PascalModuleImpl) {
                fields = ((PascalModuleImpl) scope).getPubicFields();
            } else {
                fields = scope.getAllFields();
            }
            for (PasField field : fields) {
                if ((field.fieldType == PasField.FieldType.ROUTINE) && !declSet.contains(field.element)) {
                    if (routine.isEquivalentTo(field.element)) {
                        ind = decls.size();
                    }
                    decls.add((PascalRoutineImpl) field.element);
                    declSet.add((PascalRoutineImpl) field.element);
                }
            }
            // starting from the index search for implementations
            for (int i = ind - 1; (i >= 0) && (res < 0); i--) {
                PsiElement impl = retrieveImplementation(calcPrefix(new Container(decls.get(i))));
                res = impl != null ? impl.getTextRange().getEndOffset() : -1;
            }
            for (int i = ind + 1; (i < decls.size()) && (res < 0); i++) {
                PsiElement impl = retrieveImplementation(calcPrefix(new Container(decls.get(i))));
                if (impl != null) {
                    res = impl.getTextRange().getStartOffset();
                }
            }
        }
        return res;
    }

    // Returns suggested position of declaration in interface/structure of the specified implementation of routine/method
    public static int findIntfPos(PascalRoutineImpl routine) {
        // collect all routine implementations in module with the same prefix
        // remember index of the given routine implementation
        int res = -1;
        int ind = -1;
        List<PascalRoutineImpl> impls = new SmartList<PascalRoutineImpl>();
        Set<PascalRoutineImpl> implSet = new SmartHashSet<PascalRoutineImpl>();
        Container cont = calcPrefix(new Container(routine));
        Collection<PasField> fields;
        if (cont.scope instanceof PascalModuleImpl) {
            fields = ((PascalModuleImpl) cont.scope).getPrivateFields();
        } else {
            fields = cont.scope.getAllFields();
        }
        for (PasField field : fields) if (field.element instanceof PascalRoutineImpl) {
            PascalRoutineImpl impl = (PascalRoutineImpl) field.element;
            if ((field.fieldType == PasField.FieldType.ROUTINE) && (impl.getContainingScope() == routine.getContainingScope()) && !implSet.contains(impl)) {
                if (routine.isEquivalentTo(impl)) {
                    ind = impls.size();
                }
                impls.add(impl);
                implSet.add(impl);
            }
        }
        // starting from the index search for declarations
        for (int i = ind - 1; (i >= 0) && (res < 0); i--) {
            PsiElement decl = retrieveDeclaration(calcPrefix(new Container(impls.get(i))));
            res = decl != null ? decl.getTextRange().getEndOffset() : -1;
        }
        for (int i = ind + 1; (i < impls.size()) && (res < 0); i++) {
            PsiElement decl = retrieveDeclaration(calcPrefix(new Container(impls.get(i))));
            if (decl != null) {
                res = decl.getTextRange().getStartOffset();
            }
        }
        return res;
    }

}
