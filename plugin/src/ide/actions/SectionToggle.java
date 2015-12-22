package com.siberika.idea.pascal.ide.actions;

import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.SmartList;
import com.intellij.util.containers.SmartHashSet;
import com.siberika.idea.pascal.lang.psi.PasBlockGlobal;
import com.siberika.idea.pascal.lang.psi.PasEntityScope;
import com.siberika.idea.pascal.lang.psi.PasExportedRoutine;
import com.siberika.idea.pascal.lang.psi.PasRoutineImplDecl;
import com.siberika.idea.pascal.lang.psi.PasUsesClause;
import com.siberika.idea.pascal.lang.psi.PascalNamedElement;
import com.siberika.idea.pascal.lang.psi.PascalStructType;
import com.siberika.idea.pascal.lang.psi.impl.PasField;
import com.siberika.idea.pascal.lang.psi.impl.PasModuleImpl;
import com.siberika.idea.pascal.lang.psi.impl.PascalModule;
import com.siberika.idea.pascal.lang.psi.impl.PascalModuleImpl;
import com.siberika.idea.pascal.lang.psi.impl.PascalRoutineImpl;
import com.siberika.idea.pascal.util.Filter;
import com.siberika.idea.pascal.util.PosUtil;
import com.siberika.idea.pascal.util.PsiUtil;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Collections;
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
    public static PsiElement retrieveImplementation(PascalRoutineImpl routine) {
        return retrieveImplementation(calcPrefix(new Container(routine)));
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
        return field != null ? field.getElement() : null;
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
                    if ((null == o1.getElement()) || (null == o2.getElement())) {
                        return 0;
                    }
                    return o1.getElement().getTextOffset() - o2.getElement().getTextOffset();
                }
            });
            for (PasField field : container.scope.getAllFields()) {
                if ((field.fieldType == PasField.FieldType.ROUTINE) && (field.name.toUpperCase().startsWith(prefix))) {
                    res.add(field);
                }
            }
            if (!res.isEmpty()) {
                targets.add(res.iterator().next().getElement());
            }
        }
    }

    @Nullable
    public static PsiElement retrieveDeclaration(PascalRoutineImpl routine) {
        return retrieveDeclaration(calcPrefix(new Container(routine)));
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
        return field != null ? field.getElement() : null;
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

    public static int getModuleMainDeclSectionOffset(PsiFile section) {
        PasBlockGlobal block = PsiTreeUtil.findChildOfType(section, PasBlockGlobal.class);
        if (block != null) {
            List<PasRoutineImplDecl> impls = block.getRoutineImplDeclList();
            if (!impls.isEmpty()) {
                return impls.get(impls.size() - 1).getTextRange().getEndOffset();
            }
            return block.getBlockBody().getTextOffset();
        }
        return -1;
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

    public static int findImplPos(final PascalRoutineImpl routine) {
        // collect all routine/method declarations in module/structure
        // remember index of the given routine declaration
        int res = -1;
        int ind = -1;
        PasEntityScope scope = routine.getContainingScope();
        if (scope != null) {
            List<PascalRoutineImpl> decls = collectFields(getDeclFields(scope), PasField.FieldType.ROUTINE, null);
            for (int i = 0; i < decls.size(); i++) {
                if (decls.get(i).isEquivalentTo(routine)) {
                    ind = i;
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

    @SuppressWarnings("unchecked")
    public static <T extends PsiElement> List<T> collectFields(@NotNull Collection<PasField> fields, PasField.FieldType type, Filter<PasField> filter) {
        List<T> result = new SmartList<T>();
        Set<T> resultSet = new SmartHashSet<T>();
        for (PasField field : fields) {
            PascalNamedElement el = field.getElement();
            //noinspection SuspiciousMethodCalls
            if (((null == type) || (field.fieldType == type)) && !resultSet.contains(el) && ((null == filter) || filter.allow(field))) {
                result.add((T) el);
                resultSet.add((T) el);
            }
        }
        return result;
    }

    @NotNull
    public static Collection<PasField> getDeclFields(PasEntityScope scope) {
        if (scope instanceof PascalModule) {
            return ((PascalModule) scope).getPubicFields();
        } else if (scope != null) {
            return scope.getAllFields();
        } else {
            return Collections.emptyList();
        }
    }

    // Returns suggested position of declaration in interface/structure of the specified implementation of routine/method
    public static int findIntfPos(final PascalRoutineImpl routine) {
        // collect all routine implementations in module with the same prefix
        // remember index of the given routine implementation
        int res = -1;
        int ind = -1;
        int member = -1;                        // To be used if right place will not be found

        final PasEntityScope scope = routine.getContainingScope();
        Container cont = calcPrefix(new Container(routine));
        Collection<PasField> fields;
        if (cont.scope instanceof PascalModule) {
            fields = ((PascalModule) cont.scope).getPrivateFields();
        } else {
            fields = cont.scope.getAllFields();
        }
        List<PascalRoutineImpl> impls = collectFields(fields, PasField.FieldType.ROUTINE, new Filter<PasField>() {
            @Override
            public boolean allow(PasField value) {
                return (value.getElement() instanceof PascalRoutineImpl) && (((PascalRoutineImpl) value.getElement()).getContainingScope() == scope);
            }
        });
        for (int i = 0; i < impls.size(); i++) {
            if (impls.get(i).isEquivalentTo(routine)) {
                ind = i;
            }
        }
        for (PasField field : getDeclFields(scope)) {
            if (field.getElement() != null) {
                if (field.fieldType == PasField.FieldType.PROPERTY) {
                    member = field.getElement().getTextRange().getStartOffset();
                }
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
        res = res < 0 ? member : res;
        if (res < 0) {                             // other declarations not found
            if (scope instanceof PascalStructType) {
                res = PosUtil.findPosInStruct((PascalStructType) scope, PasField.FieldType.ROUTINE, PasField.Visibility.PRIVATE.ordinal());
            } else {
                PsiElement pos = PsiUtil.getModuleInterfaceSection(routine.getContainingFile());
                if (null != pos) {                                                                  // to the end of interface section
                    res = pos.getTextRange().getEndOffset();
                } else {                                            // program or library. Should not go here.
                    res = getModuleMainDeclSectionOffset(routine.getContainingFile());
                }
            }
        }
        return res;
    }

}
