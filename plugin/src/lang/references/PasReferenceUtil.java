package com.siberika.idea.pascal.lang.references;

import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtilCore;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.search.FileTypeIndex;
import com.intellij.psi.search.FilenameIndex;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.SmartList;
import com.intellij.util.indexing.FileBasedIndex;
import com.siberika.idea.pascal.PPUFileType;
import com.siberika.idea.pascal.PascalFileType;
import com.siberika.idea.pascal.lang.parser.NamespaceRec;
import com.siberika.idea.pascal.lang.psi.PasEntityScope;
import com.siberika.idea.pascal.lang.psi.PasFullyQualifiedIdent;
import com.siberika.idea.pascal.lang.psi.PasModule;
import com.siberika.idea.pascal.lang.psi.PasNamespaceIdent;
import com.siberika.idea.pascal.lang.psi.PasTypeDecl;
import com.siberika.idea.pascal.lang.psi.PasTypeID;
import com.siberika.idea.pascal.lang.psi.PascalNamedElement;
import com.siberika.idea.pascal.lang.psi.PascalQualifiedIdent;
import com.siberika.idea.pascal.lang.psi.impl.PasField;
import com.siberika.idea.pascal.util.PsiUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

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
        PascalNamedElement module = findUsedModule(moduleName);
        if (module != null) {
            result.add(module);
        }
        for (PascalQualifiedIdent element : PsiUtil.findChildrenOfAnyType(moduleName.getContainingFile(), PascalQualifiedIdent.class)) {
            if (moduleName.getName().equalsIgnoreCase(element.getNamespace())) {
                result.add(element);
            }
        }
        return result;
    }

    @Nullable
    public static PascalNamedElement findUsedModule(@NotNull final PasNamespaceIdent moduleName) {
        Collection<VirtualFile> virtualFiles = FileBasedIndex.getInstance().getContainingFiles(FileTypeIndex.NAME, PascalFileType.INSTANCE,
                GlobalSearchScope.allScope(moduleName.getProject()));

        Module module = ModuleUtilCore.findModuleForPsiElement(moduleName);
        if (module != null) {
            virtualFiles.addAll(FileBasedIndex.getInstance().getContainingFiles(FilenameIndex.NAME, "baseunix.ppu",
                    GlobalSearchScope.moduleWithLibrariesScope(module)));
        }

        for (VirtualFile virtualFile : virtualFiles) {
            if (isUnitWithName(virtualFile, moduleName.getName())) {
                PsiFile pascalFile = PsiManager.getInstance(moduleName.getProject()).findFile(virtualFile);
                PasModule pasModule = PsiTreeUtil.findChildOfType(pascalFile, PasModule.class);
                if (pasModule != null) {
                    return pasModule;
                }
            }
        }
        return null;
    }

    private static boolean isUnitWithName(VirtualFile virtualFile, String name) {
        return isUnitExtension(virtualFile) &&
                virtualFile.getNameWithoutExtension().equalsIgnoreCase(name);
    }

    private static boolean isUnitExtension(VirtualFile virtualFile) {
        return PascalFileType.UNIT_EXTENSION.equalsIgnoreCase(virtualFile.getExtension())
            || PPUFileType.INSTANCE.getDefaultExtension().equalsIgnoreCase(virtualFile.getExtension());
    }

    public static Collection<LookupElement> getEntities(final PsiElement element, Set<PasField.Type> types) {
        Collection<PascalNamedElement> result = new TreeSet<PascalNamedElement>(); // TODO: add comparator
        PsiElement section = PsiUtil.getNearestAffectingDeclarationsRoot(element);
        doGetEntities(result, section, element.getTextRange().getStartOffset(), types, new HashSet<String>());

        Collection<LookupElement> res = new ArrayList<LookupElement>(result.size());
        for (PascalNamedElement namedElement : result) {
            res.add(LookupElementBuilder.createWithIcon(namedElement));
        }
        return res;
    }

    /**
     * Recursively goes up by entity declaration scopes and adds declared entities to result
     */
    private static void doGetEntities(Collection<PascalNamedElement> result, PsiElement section, int startOffset, Set<PasField.Type> types, Set<String> filter) {
        if (null == section) { return; }
        if (section instanceof PasEntityScope) {
            for (PasField field : ((PasEntityScope) section).getAllFields()) {
                if ((field.element != null) && (startOffset > field.element.getTextRange().getStartOffset()) &&  // TODO: pointer declaration exception
                    types.contains(field.type) && !filter.contains(field.name.toUpperCase())) {
                    result.add(field.element);
                    filter.add(field.name.toUpperCase());
                }
            }
        }
        doGetEntities(result, PsiUtil.getNearestAffectingDeclarationsRoot(section), startOffset, types, filter);
    }

    /**
     *  for each entry in FQN before target:
     *    find entity corresponding to NS in current scope
     *    if the entity represents a namespace - retrieve and make current
     *  for namespace of target entry add all its entities
     */
    public static Collection<LookupElement> getEntities(final NamespaceRec fqn, Set<PasField.Type> types) {
        // First entry in FQN
        PsiElement section = PsiUtil.getNearestAffectingDeclarationsRoot(fqn.getCurrent());
        List<PasEntityScope> namespaces = new SmartList<PasEntityScope>();
        while (section != null) {
            namespaces.add(PsiUtil.getDeclRootScope(section));
            section = fqn.isFirst() ? PsiUtil.getNearestAffectingDeclarationsRoot(namespaces.get(namespaces.size()-1)) : null;
        }

        if (namespaces.isEmpty() || (null == namespaces.get(0))) {
            return Collections.emptyList();
        }

        Collection<LookupElement> result = new ArrayList<LookupElement>();
        while (!fqn.isTarget() && (namespaces != null)) {
            PasField field = null;
            for (PasEntityScope namespace : namespaces) {
                field = namespace.getField(fqn.getCurrent().getName());
                if (field != null) { break; }
            }
            namespaces = null;
            if (field != null) {
                PasEntityScope newNS = retrieveNamespace(field, fqn.isFirst());
                namespaces = newNS != null ? new SmartList<PasEntityScope>(newNS) : null;
            }

            fqn.next();
        }

        if (fqn.isTarget() && (namespaces != null)) {
            for (PasEntityScope namespace : namespaces) {
                for (PasField pasField : namespace.getAllFields()) {
                    if ((pasField.element != null) && (types.contains(pasField.type)) && isVisibleFrom(pasField.element, fqn)) {
                        result.add(LookupElementBuilder.createWithIcon(pasField.element));
                    }
                }
            }
        }
        return result;
    }

    private static boolean isVisibleFrom(PascalNamedElement declaration, NamespaceRec fqn) {
        return (declaration.getTextRange().getStartOffset() <= fqn.getCurrent().getTextRange().getStartOffset()) ||
                PsiUtil.allowsForwardReference(fqn.getParentIdent());
    }

    /**
     * Recursively searches up over entity declaration scopes for an entity with the given name
     */
    private static PasField doGetEntity(PsiElement section, String name, Set<PasField.Type> types) {
        if (null == section) { return null; }
        if (section instanceof PasEntityScope) {
            PasField field = ((PasEntityScope) section).getField(name);
            if ((field != null) && (types.contains(field.type))) {
                return field;
            }
        }
        return doGetEntity(PsiUtil.getNearestAffectingDeclarationsRoot(section), name, types);
    }

    private static PasEntityScope retrieveNamespace(PasField field, boolean canBeUnit) {
        /*if (canBeUnit && (entityDecl instanceof PasNamespaceIdent)) {
            PasNamespaceIdent usedModuleName = getUsedModuleName(entityDecl);
            if (usedModuleName != null) {
                PascalNamedElement unit = PasReferenceUtil.findUsedModule(usedModuleName);
                if (unit != null) {
                    return unit;
                }
            }
        }*/
        if (null == field) { return null; }
        return getStructTypeByIdent(field);
    }

    @Nullable
    private static PasEntityScope getStructTypeByIdent(PasField ident) {
        if (null == ident) { return null; }
        PsiElement typeDecl = PsiTreeUtil.getNextSiblingOfType(ident.element, PasTypeDecl.class);
        if (null == typeDecl) {
            typeDecl = PsiTreeUtil.getChildOfType(ident.element, PasTypeID.class);
        }
        if (typeDecl != null) {
            PasEntityScope strucTypeDecl = PsiTreeUtil.getChildOfType(typeDecl, PasEntityScope.class);
            if (strucTypeDecl != null) {   // structured type
                return strucTypeDecl;
            } else {                       // regular type
                PasFullyQualifiedIdent typeId = PsiTreeUtil.findChildOfType(typeDecl, PasFullyQualifiedIdent.class, true);
                if (typeId != null) {
                    PsiElement section = PsiUtil.getNearestAffectingDeclarationsRoot(ident.element);
                    return getStructTypeByIdent(doGetEntity(section, typeId.getName(), PasField.TYPES_TYPE));    // factor unit-qualified names
                    /*Collection<PascalNamedElement> entities = new SmartList<PascalNamedElement>();
                    doGetEntities(entities, section, section.getTextRange().getEndOffset(), PasField.TYPES_TYPE, new HashSet<String>());
                    for (PascalNamedElement element : entities) if (typeId.getName().equalsIgnoreCase(element.getName())) {
                        return getStructTypeByIdent(element);
                    }*/
                } else {
                    return null;
                }
            }
        } else {
            return null;
        }
    }
}
