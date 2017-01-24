package com.siberika.idea.pascal.lang.psi.impl;

import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.SmartPointerManager;
import com.intellij.psi.SmartPsiElementPointer;
import com.siberika.idea.pascal.lang.psi.PasEntityScope;
import com.siberika.idea.pascal.lang.psi.PasTypeDecl;
import com.siberika.idea.pascal.lang.psi.PascalNamedElement;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.ReentrantLock;

/**
* Author: George Bakhtadze
* Date: 14/09/2013
*/
public class PasField {

    public static final String DUMMY_IDENTIFIER = "____;";

    public enum FieldType {UNIT, TYPE, VARIABLE, CONSTANT, ROUTINE, PROPERTY, PSEUDO_VARIABLE}

    public enum Kind {BOOLEAN, POINTER, INTEGER, FLOAT, CHAR, STRING, SET, STRUCT, CLASSREF, FILE, PROCEDURE, ENUM, SUBRANGE, ARRAY}

    public static final Set<FieldType> TYPES_ALL = new HashSet<FieldType>(Arrays.asList(FieldType.values()));
    public static final Set<FieldType> TYPES_LEFT_SIDE = new HashSet<FieldType>(Arrays.asList(FieldType.UNIT, FieldType.VARIABLE, FieldType.PSEUDO_VARIABLE, FieldType.PROPERTY, FieldType.ROUTINE));
    public static final Set<FieldType> TYPES_TYPE = new HashSet<FieldType>(Collections.singletonList(FieldType.TYPE));
    public static final Set<FieldType> TYPES_TYPE_UNIT = new HashSet<FieldType>(Arrays.asList(FieldType.UNIT, FieldType.TYPE));
    public static final Set<FieldType> TYPES_ROUTINE = new HashSet<FieldType>(Collections.singletonList(FieldType.ROUTINE));
    public static final Set<FieldType> TYPES_PROPERTY_SPECIFIER = new HashSet<FieldType>(Arrays.asList(FieldType.ROUTINE, FieldType.VARIABLE));
    public static final Set<FieldType> TYPES_STRUCTURE = new HashSet<FieldType>(Arrays.asList(FieldType.TYPE, FieldType.VARIABLE, FieldType.CONSTANT, FieldType.PROPERTY, FieldType.ROUTINE));
    public static final Set<FieldType> TYPES_STATIC = new HashSet<FieldType>(Arrays.asList(FieldType.UNIT, FieldType.TYPE, FieldType.CONSTANT, FieldType.ROUTINE));

    public enum Visibility {INTERNAL, STRICT_PRIVATE, PRIVATE, STRICT_PROTECTED, PROTECTED, PUBLIC, PUBLISHED, AUTOMATED}

    public static final List<String> VISIBILITY_STR = Arrays.asList("INTERNAL", "STRICT PRIVATE", "PRIVATE", "STRICT PROTECTED", "PROTECTED", "PUBLIC", "PUBLISHED", "AUTOMATED");
    public static final Map<String, Visibility> VISIBILITY_MAP = getVisibilityMap();

    public static final ValueType INTEGER = new ValueType(null, Kind.INTEGER, null, null);
    public static final ValueType FLOAT = new ValueType(null, Kind.FLOAT, null, null);
    public static final ValueType STRING = new ValueType(null, Kind.STRING, null, null);
    public static final ValueType BOOLEAN = new ValueType(null, Kind.BOOLEAN, null, null);
    public static final ValueType POINTER = new ValueType(null, Kind.POINTER, null, null);

    private static final ValueType NOT_INITIALIZED = new ValueType(null, null, null, null);

    private static Map<String, Visibility> getVisibilityMap() {
        Map<String, Visibility> res = new HashMap<String, Visibility>();
        res.put("INTERNAL", Visibility.INTERNAL);
        res.put("STRICT PRIVATE", Visibility.STRICT_PRIVATE);
        res.put("PRIVATE", Visibility.PRIVATE);
        res.put("STRICT PROTECTED", Visibility.STRICT_PROTECTED);
        res.put("PROTECTED", Visibility.PROTECTED);
        res.put("PUBLIC", Visibility.PUBLIC);
        res.put("PUBLISHED", Visibility.PUBLISHED);
        res.put("AUTOMATED", Visibility.AUTOMATED);
        return Collections.unmodifiableMap(res);
    }

    public static boolean isAllowed(Visibility check, Visibility minAllowed) {
        return check.compareTo(minAllowed) >= 0;
    }

    @Nullable
    public final PasEntityScope owner;
    @Nullable
    private final SmartPsiElementPointer<PascalNamedElement> element;
    public final String name;
    public final FieldType fieldType;
    @NotNull
    public final Visibility visibility;
    public int offset;
    // Reference target if different from element
    @Nullable
    public final PsiElement target;

    private ValueType valueType;

    private final int cachedHash;

    private ReentrantLock typeLock = new ReentrantLock();

    public PasField(@Nullable PasEntityScope owner, @Nullable PascalNamedElement element, String name, FieldType fieldType,
                    @NotNull Visibility visibility, @Nullable PsiElement target, ValueType valueType) {
        this.owner = owner;
        Project project = element != null ? element.getProject() : null;
        this.element = project != null ? SmartPointerManager.getInstance(project).createSmartPsiElementPointer(element) : null;
        this.name = name;
        this.fieldType = fieldType;
        this.visibility = visibility;
        this.offset = element != null ? element.getTextOffset() : 0;
        this.target = target;
        this.cachedHash = name.hashCode() * 31 + (element != null ? element.hashCode() : 0);
        this.valueType = valueType;
    }

    public PasField(@Nullable PasEntityScope owner, @Nullable PascalNamedElement element, String name, FieldType fieldType,
                    @NotNull Visibility visibility, ValueType valueType) {
        this(owner, element, name, fieldType, visibility, null, valueType);
    }

    public PasField(@Nullable PasEntityScope owner, @Nullable PascalNamedElement element, String name, FieldType fieldType,
                    @NotNull Visibility visibility) {
        this(owner, element, name, fieldType, visibility, null, NOT_INITIALIZED);
    }

    @Nullable
    public PascalNamedElement getElement() {
        return element != null ? element.getElement() : null;
    }

    @Nullable
    public SmartPsiElementPointer<PascalNamedElement> getElementPtr() {
        return element;
    }

    @Override
    public String toString() {
        return visibility + " " + fieldType + ": " + (owner != null ? owner.getName() : "-") + "." + name + ", " + getElement();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        PasField field = (PasField) o;

        if (!name.equals(field.name)) return false;
        if (getElement() != null ? !getElement().equals(field.getElement()) : field.getElement() != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return cachedHash;
    }

    public boolean isTypeResolved() {
        return (valueType != NOT_INITIALIZED);
        //&& ((null == valueType.field) || (null == valueType.field.element) || (valueType.field.element.isValid()));
    }

    public boolean isInteger() {
        return (getValueType() != null) && (valueType.kind == Kind.INTEGER);
    }

    public boolean isFloat() {
        return (getValueType() != null) && (valueType.kind == Kind.FLOAT);
    }

    public boolean isNumeric() {
        return isInteger() || isFloat();
    }

    public ValueType getValueType() {
        return valueType;
    }

    public void setValueType(ValueType valueType) {
        this.valueType = valueType;
    }

    public static ValueType getValueType(String name) {
        Kind kind = getKindByName(name);
        if (null == kind) {
            return null;
        }
        switch (kind) {
            case BOOLEAN:
                return BOOLEAN;
            case INTEGER:
                return INTEGER;
            case FLOAT:
                return FLOAT;
            case POINTER:
                return POINTER;
            case STRING:
                return STRING;
            default:
                return null;
        }
    }

    private static Kind getKindByName(String value) {
        for (Kind kind : Kind.values()) {
            if (kind.name().equalsIgnoreCase(value)) {
                return kind;
            }
        }
        return null;
    }

    public static class ValueType {
        // referenced type field (TRefType in type TValueType = TRefType)
        public PasField field;
        // additional information about type
        public Kind kind;
        // base type (TBaseType in TRefType = array of TBaseType)
        public ValueType baseType;
        // type declaration element
        public SmartPsiElementPointer<PasTypeDecl> declaration;

        public ValueType(PasField field, Kind kind, ValueType baseType, PasTypeDecl declaration) {
            this.field = field;
            this.kind = kind;
            this.baseType = baseType;
            this.declaration = declaration != null ? SmartPointerManager.getInstance(declaration.getProject()).createSmartPsiElementPointer(declaration) : null;
        }

        @Override
        public String toString() {
            return String.format("%s: %s (%s)", field != null ? field.name : "<anon>",
                    kind != null ? kind.name() : "-",
                    baseType != null ? (" of " + baseType.toString()) : "-");
        }

        // Searches all type chain for structured type
        @Nullable
        public PasEntityScope getTypeScope() {  //TODO: resolve unresolved types
            ValueType type = this;
            while (type.baseType != null) {
                type = type.baseType;
            }
            PasTypeDecl typeDecl = type.declaration != null ? type.declaration.getElement() : null;
            if ((typeDecl != null) && (typeDecl.getFirstChild() instanceof PasEntityScope)) {
                return (PasEntityScope) typeDecl.getFirstChild();
            }
            return null;
        }

    }

    public ReentrantLock getTypeLock() {
        return typeLock;
    }
}
