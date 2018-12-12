package com.siberika.idea.pascal.util;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiComment;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiErrorElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.PsiNamedElement;
import com.intellij.psi.PsiWhiteSpace;
import com.intellij.psi.SmartPointerManager;
import com.intellij.psi.SmartPsiElementPointer;
import com.intellij.psi.StubBasedPsiElement;
import com.intellij.psi.impl.source.tree.LeafPsiElement;
import com.intellij.psi.impl.source.tree.TreeUtil;
import com.intellij.psi.search.PsiElementProcessor;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.FileContentUtil;
import com.intellij.util.SmartList;
import com.intellij.util.containers.ContainerUtil;
import com.siberika.idea.pascal.lang.context.ContextUtil;
import com.siberika.idea.pascal.lang.psi.*;
import com.siberika.idea.pascal.lang.psi.impl.PasClassParentImpl;
import com.siberika.idea.pascal.lang.psi.impl.PasExportedRoutineImpl;
import com.siberika.idea.pascal.lang.psi.impl.PasField;
import com.siberika.idea.pascal.lang.psi.impl.PasGenericTypeIdentImpl;
import com.siberika.idea.pascal.lang.psi.impl.PasNamespaceIdentImpl;
import com.siberika.idea.pascal.lang.psi.impl.PasRefNamedIdentImpl;
import com.siberika.idea.pascal.lang.psi.impl.PasRoutineImplDeclImpl;
import com.siberika.idea.pascal.lang.psi.impl.PasStubStructTypeImpl;
import com.siberika.idea.pascal.lang.psi.impl.PasSubIdentImpl;
import com.siberika.idea.pascal.lang.psi.impl.PasTypeIDImpl;
import com.siberika.idea.pascal.lang.psi.impl.PascalExpression;
import com.siberika.idea.pascal.lang.references.ResolveUtil;
import com.siberika.idea.pascal.sdk.BuiltinsParser;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * Author: George Bakhtadze
 * Date: 24/03/2013
 */
public class PsiUtil {

    private static final Logger LOG = Logger.getInstance(PsiUtil.class.getName());

    public static final Class[] ELEMENT_WS_COMMENTS = {PsiWhiteSpace.class, PsiComment.class};

    private static final int MAX_NON_BREAKING_NAMESPACES = 2;
    private static final long MIN_REPARSE_INTERVAL = 2000;
    public static final String TYPE_UNTYPED_NAME = "<untyped>";
    private static long lastReparseRequestTime = System.currentTimeMillis() - MIN_REPARSE_INTERVAL;

    public static PsiElement findChildByElementType(PsiElement element, IElementType elementType) {
        if (element == null) {
            return null;
        }
        PsiElementProcessor.FindElement<PsiElement> processor = new PsiElementProcessor.FindElement<PsiElement>() {
            @Override
            public boolean execute(@NotNull PsiElement each) {
                if (each.getNode().getElementType() == elementType) {
                    setFound(each);
                }
                return true;
            }
        };
        PsiTreeUtil.processElements(element, processor);
        return processor.getFoundElement();
    }

    @NotNull
    public static <T extends PsiElement> Collection<T> findChildrenOfAnyType(@Nullable final PsiElement element,
                                                                             @NotNull final Class<? extends T>... classes) {
        if (element == null) {
            return ContainerUtil.emptyList();
        }

        PsiElementProcessor.CollectElements<T> processor = new PsiElementProcessor.CollectElements<T>() {
            @Override
            public boolean execute(@NotNull T each) {
                if (each == element) return true;
                if (PsiTreeUtil.instanceOf(each, classes)) {
                    return super.execute(each);
                }
                return true;
            }
        };
        PsiTreeUtil.processElements(element, processor);
        return processor.getCollection();
    }

    @NotNull
    @SuppressWarnings("unchecked")
    public static <T extends PsiElement> Collection<T> findImmChildrenOfAnyType(@Nullable final PsiElement element,
                                                                                @NotNull final Class<? extends T>... classes) {
        if (element == null) {
            return ContainerUtil.emptyList();
        }

        Collection<T> result = new SmartList<T>();
        for (PsiElement each : element.getChildren()) {
            if (PsiTreeUtil.instanceOf(each, classes)) {
                result.add((T) each);
            }
        }

        return result;
    }

    @Nullable
    @SuppressWarnings("unchecked")
    public static <T extends PsiElement> T findImmChildOfAnyType(@Nullable final PsiElement element, @NotNull final Class<? extends T>... classes) {
        if (element != null) {
            for (PsiElement each : element.getChildren()) {
                if (PsiTreeUtil.instanceOf(each, classes)) {
                    return (T) each;
                }
            }
        }
        return null;
    }

    /**
     * Searches for a previous sibling element ignoring whitespace elements
     *
     * @param element - element from where to search
     * @return previous sibling element or null if not found
     */
    @Nullable
    public static PsiElement getPrevSibling(@NotNull PsiElement element) {
        PsiElement result = element.getPrevSibling();
        while (result instanceof PsiWhiteSpace) {
            result = result.getPrevSibling();
        }
        return result;
    }

    /**
     * Searches for a next sibling element ignoring whitespace elements
     * @param element - element from where to search
     * @return next sibling element or null if not found
     */
    @Nullable
    public static PsiElement getNextSibling(@NotNull PsiElement element) {
        PsiElement result = element.getNextSibling();
        while (result instanceof PsiWhiteSpace) {
            result = result.getNextSibling();
        }
        return result;
    }

    /**
     * Searches for element within parent by the given offset skipping whitespaces and comments
     * @param parent    parent element
     * @param offset    offset within parent
     * @return element or null if not found
     */
    @Nullable
    public static PsiElement findElementAt(@NotNull PsiElement parent, int offset) {
        PsiElement result = parent.findElementAt(offset);
        if (PsiTreeUtil.instanceOf(result, ELEMENT_WS_COMMENTS)) {
            result = PsiTreeUtil.skipSiblingsForward(result, ELEMENT_WS_COMMENTS);
        }
        return result;
    }

    /**
     * Searches for sibling element which contains "end" text
     * @param element - element from where to search
     * @return next sibling element or null if not found
     */
    @Nullable
    public static PsiElement findEndSibling(PsiElement element) {
        PsiElement result = element.getNextSibling();
        while ((result != null) && !((result instanceof LeafPsiElement) && ("end".equalsIgnoreCase(result.getText())))) {
            result = result.getNextSibling();
        }
        return result;
    }

    /**
     * Returns nearest declarations root element which affects the given element, including formal parameters clause,
     * declaration section, structured type declaration, module declaration
     *
     * @param element - element which should be affected by declarations found
     * @return nearest declarations root element which affects the given element
     */
    @SuppressWarnings("unchecked")
    public static PsiElement getNearestAffectingDeclarationsRoot(PsiElement element) {
        if (element instanceof PasUnitImplementation) {
            PasUnitInterface unitInterface = PsiTreeUtil.getPrevSiblingOfType(element, PasUnitInterface.class);
            if (unitInterface != null) {
                return unitInterface;
            }
        }
        PascalPsiElement parent = getParentDeclRoot(element);
        if (isNamedIdent(element) &&
                // Make routine itself belong to parent root
                ((parent instanceof PascalRoutine) && (element.getParent() == parent))
                // Make class parent belong to parent root
                || ((parent instanceof PasEntityScope) && (element.getParent().getParent().getClass() == PasClassParentImpl.class))) {
            return getNearestAffectingDeclarationsRoot(parent);
        } else if (PsiUtil.isInstanceOfAny(parent, PasFormalParameterSection.class, PasBlockGlobal.class)) {
            // Make routine local variable or parameters belong to the routine
            return getParentDeclRoot(parent);
        }
        return parent;
    }

    private static boolean isNamedIdent(PsiElement element) {
        return (element instanceof PasNamedIdent) || (element instanceof PasClassQualifiedIdent);
    }

    @SuppressWarnings("unchecked")
    private static PascalPsiElement getParentDeclRoot(PsiElement element) {   // TODO: remove blocks?
        return PsiTreeUtil.getParentOfType(element,
                PascalRoutine.class, PasFormalParameterSection.class,
                PasClosureExpr.class,
                PasUnitImplementation.class,
                PasBlockGlobal.class,
                PasModule.class,
                PasUnitInterface.class,
                PasEntityScope.class
        );
    }

    /**
     * Returns nearest scope which affecting the element
     */
    @Nullable
    public static PasEntityScope getDeclRootScope(PsiElement element) {
        if (null == element) {
            return null;
        } else if (element instanceof PasEntityScope) {
            return (PasEntityScope) element;
        } else {
            return getDeclRootScope(getParentDeclRoot(element));
        }
    }

    public static String getElDebugContext(PsiElement current) {
        return current != null ? "\"" + (current instanceof PascalNamedElement ? ((PascalNamedElement) current).getName() : "")
                + "\" [" + current.getClass().getSimpleName() + "]" + getParentStr(current.getParent()) : "-";
    }

    private static String getParentStr(@NotNull PsiElement parent) {
        return parent.getText() + " [" + parent.getClass().getSimpleName() + "]";
    }

    public static boolean isEntityName(@NotNull PsiElement element) {
        return (element.getClass() == PasSubIdentImpl.class) || (element.getClass() == PasRefNamedIdentImpl.class);
    }

    public static boolean isTypeName(@NotNull PsiElement element) {
        if (checkClass(element, PasGenericTypeIdentImpl.class)) {
            return true;
        }
        PsiElement el = PsiTreeUtil.skipParentsOfType(element, PasSubIdent.class, PasFullyQualifiedIdent.class, PsiWhiteSpace.class, PsiErrorElement.class);
        return checkClass(el, PasGenericTypeIdentImpl.class) || checkClass(el, PasTypeIDImpl.class);
    }

    private static boolean checkClass(PsiElement element, Class clazz) {
        return (element != null) && (element.getClass() == clazz);
    }

    public static boolean isRoutineName(@NotNull PascalNamedElement element) {
        return element.getParent() instanceof PascalRoutine;
    }

    public static boolean isUsedUnitName(@NotNull PsiElement element) {
        return (element instanceof PasNamespaceIdent) && (element.getParent() instanceof PasUsesClause);
    }

    /**
     * @param element element
     * @return true if the element is a pointer type declaration. I.e. "TType = ^element".
     */
    public static boolean isPointerTypeDeclaration(@NotNull PascalNamedElement element) {
        return PsiTreeUtil.skipParentsOfType(element, PasSubIdent.class, PasFullyQualifiedIdent.class, PasTypeIDImpl.class, PasTypeDecl.class) instanceof PasPointerType;
    }

    /**
     * @param element element
     * @return true if the element is a class reference type declaration. I.e. "CClass = class of TSomeClass".
     */
    public static boolean isClassRefDeclaration(@NotNull PascalNamedElement element) {
        return PsiTreeUtil.skipParentsOfType(element, PasSubIdent.class, PasFullyQualifiedIdent.class, PasTypeIDImpl.class, PasTypeDecl.class) instanceof PasClassTypeTypeDecl;
    }

    public static boolean isFormalParameterName(@NotNull PascalNamedElement element) {
        return element.getParent() instanceof PasFormalParameter;
    }

    public static <T extends PsiElement> boolean isInstanceOfAny(Object object, Class... classes) {
        int i = classes.length - 1;
        while ((i >= 0) && (!classes[i].isInstance(object))) {
            i--;
        }
        return i >= 0;
    }

    /**
     * Returns module containing the element
     *
     * @param element - element
     * @return module containing the element
     */
    @Nullable
    public static PasModule getElementPasModule(@NotNull PsiElement element) {
        return PsiTreeUtil.findChildOfType(element.getContainingFile(), PasModule.class);
    }

    /**
     * Returns interface section of module specified by section
     *
     * @param section - can be PasModule or PsiFile
     * @return interface section of module
     */
    @Nullable
    public static PsiElement getModuleInterfaceSection(@NotNull PsiElement section) {
        assert (section instanceof PascalModule) || (section instanceof PsiFile);
        return PsiTreeUtil.findChildOfType(section, PasUnitInterface.class);
    }

    /**
     * Returns implementation section of module specified by section
     *
     * @param section - can be PasModule or PsiFile
     * @return unit implementation section or module itself if the module is not a unit
     */
    @NotNull
    public static PsiElement getModuleImplementationSection(@NotNull PsiElement section) {
        assert (section instanceof PascalModule) || (section instanceof PsiFile);
        PsiElement result = PsiTreeUtil.findChildOfType(section, PasUnitImplementation.class);
        if (result == null) {
            result = section;
        }
        return result;
    }

    @NotNull
    @SuppressWarnings("unchecked")
    public static List<PascalQualifiedIdent> getUsedUnits(PsiElement parent) {
        List<PascalQualifiedIdent> result = new SmartList<PascalQualifiedIdent>();
        Collection<PasUsesClause> usesClauses = findChildrenOfAnyType(parent, PasUsesClause.class);
        for (PasUsesClause usesClause : usesClauses) {
            for (PsiElement usedUnitName : usesClause.getChildren()) {
                if (usedUnitName.getClass() == PasNamespaceIdentImpl.class) {
                    result.add((PasNamespaceIdent) usedUnitName);
                }
            }
        }
        return result;
    }

    public static int getElementLevel(PsiElement element) {
        int result = 0;
        while ((element != null) && (!(element instanceof PsiFile))) {
            result++;
            element = element.getParent();
        }
        return result;
    }

    public static boolean isSameAffectingScope(PsiElement innerSection, PsiElement outerSection) {
        for (int i = 0; i < MAX_NON_BREAKING_NAMESPACES; i++) {
            if ((innerSection == outerSection) || sameModuleSections(innerSection, outerSection)) {
                return true;
            }
            //noinspection unchecked
            if ((null == innerSection) || PsiUtil.isInstanceOfAny(innerSection, PasEntityScope.class, PasClosureExpr.class)) {
                return false;
            }
            innerSection = PsiUtil.getNearestAffectingDeclarationsRoot(innerSection);
        }
        return false;
    }

    private static boolean sameModuleSections(PsiElement innerSection, PsiElement outerSection) {
        return (outerSection instanceof PasModule) && (innerSection instanceof PasUnitInterface) && (((PasModule) outerSection).getUnitInterface() == innerSection);
    }

    @NotNull
    public static List<PascalNamedElement> getFormalParameters(PasFormalParameterSection paramsSection) {
        if (paramsSection != null) {
            List<PascalNamedElement> result = new SmartList<PascalNamedElement>();
            for (PasFormalParameter parameter : paramsSection.getFormalParameterList()) {
                for (PascalNamedElement ident : parameter.getNamedIdentDeclList()) {
                    result.add(ident);
                }
            }
            return result;
        }
        return Collections.emptyList();
    }

    @Nullable
    public static <T extends PsiElement> T findInSameSection(PsiElement section, Class<? extends T>... classes) {
        T element = PsiTreeUtil.findChildOfAnyType(section, classes);
        if ((element != null) && (isSameAffectingScope(getNearestAffectingDeclarationsRoot(element), section))) {
            return element;
        }
        return null;
    }

    public static void rebuildPsi(PsiElement block) {
        LOG.info("WARNING: requesting reparse: " + block);
        if (System.currentTimeMillis() - lastReparseRequestTime >= MIN_REPARSE_INTERVAL) {
            lastReparseRequestTime = System.currentTimeMillis();
            //BlockSupport.getInstance(block.getProject()).reparseRange(block.getContainingFile(), block.getTextRange().getStartOffset(), block.getTextRange().getEndOffset(), block.getText());
            ApplicationManager.getApplication().invokeLater(
                    new Runnable() {
                        @Override
                        public void run() {
                            FileContentUtil.reparseOpenedFiles();
                        }
                    }
            );
        }
    }

    public static boolean isForwardProc(PascalRoutine decl) {
        return PsiTreeUtil.findChildOfType(decl, PasProcForwardDecl.class) != null;
    }

    public static boolean allowsForwardReference(PsiElement element) {
        return (element instanceof PascalNamedElement) &&
                (PsiUtil.isPointerTypeDeclaration((PascalNamedElement) element) || PsiUtil.isClassRefDeclaration((PascalNamedElement) element));
    }

    public static boolean isStructureMember(PsiElement element) {
        return (element.getParent() != null) && (element.getParent() instanceof PascalStructType);
    }

    public static PascalStructType getStructTypeByName(PsiElement name) {
        PasTypeDecl typeDecl = PsiTreeUtil.getNextSiblingOfType(name, PasTypeDecl.class);
        return typeDecl != null ? PsiTreeUtil.findChildOfType(typeDecl, PascalStructType.class) : null;
    }

    public static String getQualifiedMethodName(PsiNamedElement element) {
        if (PsiUtil.isStructureMember(element)) {
            PascalStructType owner = PasStubStructTypeImpl.findOwnerStruct(element);
            if (null != owner) {
                return getQualifiedMethodName(owner) + "." + element.getName();
            }
        }
        return element != null ? element.getName() : "";
    }

    public static boolean isIdent(PsiElement element) {
        return element instanceof PasSubIdent;
    }

    public static boolean isElementValid(PsiElement element) {
        return element.isValid();
    }

    public static boolean isElementUsable(PsiElement element) {
        return (element != null) && isElementValid(element);
    }

//--------------------------------------------------------------------------------------------------------------

    public static boolean isFromBuiltinsUnit(PsiElement element) {
        return (element.getContainingFile() != null) && BuiltinsParser.UNIT_NAME_BUILTINS.equalsIgnoreCase(element.getContainingFile().getName());
    }

    public static boolean isForwardClassDecl(PascalNamedElement element) {
        PsiElement el = element instanceof PasNamedIdentDecl ? element.getParent() : element;
        LeafPsiElement leaf1 = getLeafSiblingOfType(el, LeafPsiElement.class);
        LeafPsiElement leaf2 = leaf1 != null ? getLeafSiblingOfType(leaf1, LeafPsiElement.class) : null;
        return ((leaf2 != null) && ((leaf2.getElementType() == PasTypes.CLASS) || (leaf2.getElementType() == PasTypes.INTERFACE)));
    }

    public static <T extends PsiElement> T getLeafSiblingOfType(@Nullable PsiElement sibling, @NotNull Class<T> aClass) {
        T result = PsiTreeUtil.getNextSiblingOfType(sibling, aClass);
        while ((result != null) && (TreeUtil.isWhitespaceOrComment(result.getNode()))) {
            result = PsiTreeUtil.getNextSiblingOfType(result, aClass);
        }
        return result;
    }

    public static boolean isTypeDeclPointingToSelf(@NotNull PascalNamedElement typeIdent) {
        if (typeIdent instanceof StubBasedPsiElement) {
            return ResolveUtil.isTypeDeclPointingToSelf((StubBasedPsiElement)typeIdent);
        }
        PsiElement parent = typeIdent.getParent();
        if (parent instanceof PasTypeDeclaration) {
            PasTypeDecl typeDecl = ((PasTypeDeclaration) parent).getTypeDecl();
            PasTypeID typeId = typeDecl != null ? typeDecl.getTypeID() : null;
            if (typeId != null) {
                return typeIdent.getName().equalsIgnoreCase(typeId.getFullyQualifiedIdent().getName());
            }
        }
        return false;
    }

    public static boolean isFromLibrary(@NotNull PsiElement element) {
        return !PsiManager.getInstance(element.getProject()).isInProject(element);
    }

    public static PasEntityScope getNearestAffectingScope(PsiElement element) {
        if (PsiTreeUtil.getParentOfType(element, PasClassParent.class) != null) {                       // Don't search for a struct parent IDs in this struct
            PascalStructType struct = PsiTreeUtil.getParentOfType(element, PascalStructType.class);
            element = struct != null ? struct : element;
        }
        return PsiTreeUtil.getParentOfType(element, PasEntityScope.class);
    }

    public static PsiElement getNearestSection(PsiElement element) {
        return PsiTreeUtil.getParentOfType(element, PasEntityScope.class, PasUnitInterface.class, PasUnitImplementation.class, PasBlockGlobal.class);
    }

    // returns type declaration for the specified field or routine element
    @Nullable
    public static PasTypeDecl getTypeDeclaration(PascalNamedElement element) {
        PasTypeDecl typeDecl;
        if ((element instanceof PascalRoutine) && (element.getFirstChild() != null)) {                      // resolve function type
            typeDecl = PsiTreeUtil.getNextSiblingOfType(element.getFirstChild(), PasTypeDecl.class);
        } else if ((element instanceof PascalIdentDecl) && (element.getParent() instanceof PasGenericTypeIdent)) {
            typeDecl = PsiTreeUtil.getNextSiblingOfType(element.getParent(), PasTypeDecl.class);
        } else {
            typeDecl = PsiTreeUtil.getNextSiblingOfType(element, PasTypeDecl.class);
        }
        return typeDecl;
    }

    // returns type name which referenced in type declaration or null if type is anonymous
    @Nullable
    public static PasTypeID getDeclaredTypeName(@Nullable PasTypeDecl typeDecl) {
        PasTypeID typeId = typeDecl != null ? typeDecl.getTypeID() : null;
        if (null == typeId) {                                                                                    // immediate complex non-structured type
            typeId = PsiTreeUtil.getChildOfType(typeDecl, PasTypeID.class);
        }
        return typeId;
    }

    public static boolean checkeElement(PsiElement element) {
        if (!isElementValid(element)) {
            //rebuildPsi(element);
            return false;
            //throw new PasInvalidScopeException(this);
        }
        return true;
    }

    public static String getFieldName(PascalNamedElement element) {
        if (element instanceof PascalRoutine) {
            return ((PascalRoutine) element).getCanonicalName();
        } else {
            return element.getName();
        }
    }

    @NotNull
    public static String getContainingFilePath(@Nullable PsiElement element) {
        PsiFile file = element != null ? element.getContainingFile() : null;
        VirtualFile vFile = file != null ? file.getVirtualFile() : null;
        return vFile != null ? vFile.getPath() : "";
    }

    public static String normalizeRoutineName(PascalRoutine routine) {
        StringBuilder res = new StringBuilder(routine.getName());
        PasFormalParameterSection params = routine.getFormalParameterSection();
        if (params != null) {
            res.append("(");
            boolean nonFirst = false;
            for (PasFormalParameter param : params.getFormalParameterList()) {
                PasTypeDecl td = param.getTypeDecl();
                String typeStr = td != null ? td.getText() : null;
                typeStr = StringUtils.isNotBlank(typeStr) ? typeStr : TYPE_UNTYPED_NAME;
                for (int i = 0; i < param.getNamedIdentDeclList().size(); i++) {
                    res.append(nonFirst ? "," : "").append(typeStr);
                    nonFirst = true;
                }
            }
            res.append(")");
            //name = name + (params != null ? params.getText() : "");
            //name = name.replaceAll("\\s+", " ");
        } else {
            res.append("()");
        }
        String typeStr = routine.getFunctionTypeStr();
        if (typeStr.length() > 0) {
            res.append(":").append(typeStr);
        }
        return res.toString();
    }

    public static boolean isParentOf(PsiElement element, PsiElement parent) {
        while ((element != null) && (element != parent)) {
            element = element.getParent();
        }
        return element != null;
    }

    public static boolean isClassParent(PsiElement element) {
        PasClassParent parent = PsiTreeUtil.getParentOfType(element, PasClassParent.class);
        PasGenericPostfix gen = PsiTreeUtil.getParentOfType(element, PasGenericPostfix.class);
        return (parent != null) && !isParentOf(gen, parent);
    }

    // Returns structured type declaration by any of its contained element or name element
    public static PascalStructType getStructByElement(PsiElement element) {
        PascalNamedElement named;
        if (element instanceof PascalNamedElement) {
            named = (PascalNamedElement) element;
        } else {
            named = PsiTreeUtil.getParentOfType(element, PascalNamedElement.class);
        }
        PascalStructType struct = getStructTypeByName(named);
        return struct != null ? struct : PsiTreeUtil.getParentOfType(element, PascalStructType.class);
    }

    public static boolean needImplementation(PasExportedRoutineImpl routine) {
        if (routine.getExternalDirective() != null) {
            return false;
        }
        if (routine.getContainingScope() instanceof PasInterfaceTypeDecl) {
            return false;
        }
        for (PasFunctionDirective dir : routine.getFunctionDirectiveList()) {
            if (dir.getText().toUpperCase().startsWith("ABSTRACT")) {
                return false;
            }
        }
        return true;
    }

    public static String cleanGenericDef(String name) {
        int ind = name.indexOf('<');
        return ind < 0 ? name : name.substring(0, ind);
    }

    @Deprecated  // remove after finish with stubs
    public static <T extends PsiElement> Collection<T> extractSmartPointers(List<SmartPsiElementPointer<T>> smartPtrs) {
        Collection<T> res = new ArrayList<T>(smartPtrs.size());
        for (SmartPsiElementPointer<T> smartPtr : smartPtrs) {
            res.add(smartPtr.getElement());
        }
        return res;
    }

    @Deprecated  // remove after finish with stubs
    public static <T extends PsiElement> List<SmartPsiElementPointer<T>> packSmartPointers(List<? extends T> elements) {
        List<SmartPsiElementPointer<T>> res = new ArrayList<SmartPsiElementPointer<T>>(elements.size());
        for (T element : elements) {
            res.add(createSmartPointer(element));
        }
        return res;
    }

    @Nullable
    public static PascalNamedElement getDefaultProperty(PasEntityScope typeScope) {
        for (PasField field : typeScope.getAllFields()) {
            if (field.fieldType == PasField.FieldType.PROPERTY) {
                PascalNamedElement el = field.getElement();
                if ((el != null) && (el.getNode().findChildByType(PasTypes.DEFAULT) != null)) {
                    return el;
                }
            }
        }
        return null;
    }

    public static PsiElement skipToExpressionParent(PsiElement element) {
        return PsiTreeUtil.skipParentsOfType(element,
                PasSubIdent.class, PasFullyQualifiedIdent.class, PasRefNamedIdent.class, PasNamedIdent.class, PasNamedIdentDecl.class, PasNamespaceIdent.class, PasGenericTypeIdent.class,
                PasExpression.class, PascalExpression.class, PasExpressionOrd.class, PasConstExpression.class,
                PsiWhiteSpace.class, PsiErrorElement.class,
                PasUnitModuleHead.class);
    }

    public static PsiElement skipToExpression(PsiElement element) {
        return PsiTreeUtil.skipParentsOfType(element,
                PasSubIdent.class, PasFullyQualifiedIdent.class, PasRefNamedIdent.class, PasNamedIdent.class, PasNamedIdentDecl.class, PasGenericTypeIdent.class,
                PsiWhiteSpace.class, PsiErrorElement.class,
                PasUnitModuleHead.class);
    }

    public static boolean isLastPartOfMethodImplName(PascalNamedElement element) {
        PsiElement parent = element.getParent();
        if (parent instanceof PasClassQualifiedIdent) {
            PasClassQualifiedIdent name = (PasClassQualifiedIdent) parent;
            return (element == name.getSubIdentList().get(name.getSubIdentList().size() - 1))
                 && isRoutineName((PascalNamedElement) parent) && !StringUtils.isEmpty(((PascalNamedElement) parent).getNamespace());
        }
        return false;
    }

    // returns unique name of ident qualifying it with scopes
    public static String getUniqueName(PascalNamedElement ident) {
        StringBuilder sb = new StringBuilder(ident.getName());
        PasEntityScope scope = getNearestAffectingScope(ident);
        while (scope != null) {
            sb.append(".").append(PsiUtil.getFieldName(scope));
            scope = scope.getContainingScope();
        }
        return sb.toString();
    }

    // Returns pair sorted by starting offset. Nulls come last.
    public static Pair<PsiElement, PsiElement> sortByStart(PsiElement o1, PsiElement o2, boolean ascending) {
        if (null == o2) {
            return Pair.create(o1, null);
        }
        if (null == o1) {
            return Pair.create(o2, null);
        }
        boolean less = o1.getTextRange().getStartOffset() <= o2.getTextRange().getStartOffset();
        less = ascending ? less : !less;
        if (less) {
            return Pair.create(o1, o2);
        } else {
            return Pair.create(o2, o1);
        }
    }

    @NotNull
    public static PsiContext getContext(@NotNull PascalNamedElement element) {
        PascalNamedElement fqn = element;
        PascalQualifiedIdent fqi = getFQI(element);
        int count = 1;
        int index = 0;
        if (fqi != null) {
            fqn = fqi;
            if (element instanceof PasSubIdent) {
                index = fqi.getSubIdentList().indexOf(element);
                count = fqi.getSubIdentList().size();
            }
        }
        if (index == count - 1) {                    // Last part of FQN
            if ((element instanceof PasGenericTypeIdent) && (element.getParent() instanceof PasTypeDeclaration)) {
                return PsiContext.TYPE_DECL;
            } else if (isTypeName(element)) {
                return PsiContext.TYPE_ID;
            } else if (fqn.getParent() instanceof PasClassPropertySpecifier) {
                return PsiContext.PROPERTY_SPEC;                                    // False positives possible
            } else if (fqn.getParent() instanceof PasGenericDefinition) {
                return PsiContext.GENERIC_PARAM;
            } else if (fqn.getParent() instanceof PasForStatement) {
                return PsiContext.FOR;
            } else if ((fqn.getParent() instanceof PasUsesClause) || (fqn.getParent() instanceof PasRequiresClause)) {
                return PsiContext.USES;
            } else if (fqn.getParent() instanceof PasExportsSection) {
                return PsiContext.EXPORT;
            } else {
                PasExpr expr = PsiTreeUtil.getParentOfType(element, PasExpr.class);
                if ((expr != null) && (expr.getNextSibling() instanceof PasArgumentList)) {
                    return PsiContext.CALL;
                }
            }
        }
        if (fqi != null) {
            if (1 == count) {
                return PsiContext.FQN_SINGLE;
            } else if (0 == index) {
                return PsiContext.FQN_FIRST;
            } else {
                return PsiContext.FQN_NEXT;
            }
        }
        return PsiContext.UNKNOWN;
    }

    // Returns FQI which element is a part of
    public static PascalQualifiedIdent getFQI(PsiElement element) {
        if (element instanceof PascalQualifiedIdent) {
            return (PascalQualifiedIdent) element;
        }
        PsiElement sub = (element instanceof PasSubIdent) ? element : element.getParent();
        if (sub != null) {
            return (sub.getParent() instanceof PascalQualifiedIdent) ? (PascalQualifiedIdent) sub.getParent() : null;
        }
        return null;
    }

    private static String normalizeGenericName(PasGenericTypeIdent ident) {
        StringBuilder res = new StringBuilder();
        PasGenericDefinition gen = ident.getGenericDefinition();
        if (gen != null) {
            for (PasNamedIdent type : gen.getNamedIdentList()) {
                if (res.length() > 0) {
                    res.append(",");
                }
                res.append(type.getName());
            }
        }
        if (res.length() > 0) {
            res.insert(0, "<").insert(0, ident.getNamedIdentDecl().getName()).append(">");
        } else {
            return ident.getNamedIdentDecl().getName();
        }
        return res.toString();
    }

    public static boolean hasParameters(PascalRoutine routine) {
        PasFormalParameterSection params = routine.getFormalParameterSection();
        return (params != null) && !params.getFormalParameterList().isEmpty();
    }

    public static boolean isBefore(@NotNull PsiElement el1, @NotNull PsiElement el2) {
        return el1.getTextRange().getStartOffset() < el2.getTextRange().getStartOffset();
    }

    public static boolean isNotNestedRoutine(PascalRoutine routine) {
        return routine.getClass() == PasRoutineImplDeclImpl.class;
    }

    public static boolean isFormalParameterOfExportedRoutineOrProcType(@NotNull PascalNamedElement element) {
        return ((element.getParent() instanceof PasFormalParameter) && (element.getParent().getParent() instanceof PasFormalParameterSection)
                && PsiUtil.isInstanceOfAny(element.getParent().getParent().getParent(), PasExportedRoutine.class, PasProcedureType.class));
    }

    public static PasField.FieldType getFieldType(PascalNamedElement namedElement) {
        PasField.FieldType type = PasField.FieldType.VARIABLE;
        if (isTypeName(namedElement)) {
            type = PasField.FieldType.TYPE;
        } else if (namedElement instanceof PascalRoutine) {
            type = PasField.FieldType.ROUTINE;
        } else if (ContextUtil.isConstDecl(namedElement) || ContextUtil.isEnumDecl(namedElement)) {
            type = PasField.FieldType.CONSTANT;
        } else if (ContextUtil.isPropertyDecl(namedElement)) {
            type = PasField.FieldType.PROPERTY;
        } else if (namedElement instanceof PascalModule) {
            type = PasField.FieldType.UNIT;
        }

        return type;
    }

    public static boolean isSmartPointerValid(SmartPsiElementPointer pointer) {
        return (pointer != null) && (pointer.getElement() != null);
    }

    public static <T extends PsiElement> SmartPsiElementPointer<T> createSmartPointer(T element) {
        try {
            return SmartPointerManager.getInstance(element.getProject()).createSmartPsiElementPointer(element);
        } catch (Throwable e) {
            LOG.info(e.getMessage(), e);
            return null;
        }
    }

    // element can be PasNamedIdentDecl or PasGenericTypeIdent
    public static boolean isStructDecl(PsiElement element) {
        if (element instanceof PasNamedIdentDecl) {
            element = element.getParent();
        }
        if (element instanceof PasGenericTypeIdent) {
            PasTypeDecl typeDecl = PsiTreeUtil.getNextSiblingOfType(element, PasTypeDecl.class);
            return (typeDecl != null) &&
                    ((typeDecl.getClassTypeDecl() != null) || (typeDecl.getInterfaceTypeDecl() != null)
                            || (typeDecl.getObjectDecl() != null) || (typeDecl.getRecordDecl() != null)
                            || (typeDecl.getClassHelperDecl() != null) || (typeDecl.getRecordHelperDecl() != null)
                    );
        }
        return false;
    }

    public static boolean isPropertyIndexIdent(PascalNamedElement element) {
        return (element.getParent() instanceof PasFormalParameter) && (element.getParent().getParent() instanceof PasClassPropertyArray);
    }

    public static boolean isImplementationScope(PsiElement scope) {
        return scope instanceof PasImplDeclSection || scope instanceof PasBlockGlobal || scope instanceof PasBlockLocal;
    }

    public static long getModificationStamp(PsiElement element) {
        PsiFile file = element != null ? element.getContainingFile() : null;
        return file != null ? file.getModificationStamp() : 0;
    }

    public static boolean isComma(PsiElement element) {
        return (element instanceof LeafPsiElement) && ",".equals(element.getText());
    }

}
