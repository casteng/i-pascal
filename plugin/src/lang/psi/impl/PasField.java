package com.siberika.idea.pascal.lang.psi.impl;

import com.intellij.psi.PsiElement;
import com.siberika.idea.pascal.lang.psi.PasEntityScope;
import com.siberika.idea.pascal.lang.psi.PascalNamedElement;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
* Author: George Bakhtadze
* Date: 14/09/2013
*/
public class PasField {

    public static final String DUMMY_IDENTIFIER = "____";

    public enum FieldType {UNIT, TYPE, VARIABLE, CONSTANT, ROUTINE, PROPERTY}

    public enum Kind {BOOLEAN, POINTER, INTEGER, FLOAT, STRING}

    public static final Set<FieldType> TYPES_ALL = new HashSet<FieldType>(Arrays.asList(FieldType.values()));
    public static final Set<FieldType> TYPES_LEFT_SIDE = new HashSet<FieldType>(Arrays.asList(FieldType.UNIT, FieldType.VARIABLE, FieldType.PROPERTY, FieldType.ROUTINE));
    public static final Set<FieldType> TYPES_TYPE = new HashSet<FieldType>(Collections.singletonList(FieldType.TYPE));
    public static final Set<FieldType> TYPES_TYPE_UNIT = new HashSet<FieldType>(Arrays.asList(FieldType.UNIT, FieldType.TYPE));

    public enum Visibility {INTERNAL, STRICT_PRIVATE, PRIVATE, STRICT_PROTECTED, PROTECTED, PUBLIC, PUBLISHED, AUTOMATED}

    public static final ValueType INTEGER = new ValueType(null, Kind.INTEGER);
    public static final ValueType FLOAT = new ValueType(null, Kind.FLOAT);
    public static final ValueType STRING = new ValueType(null, Kind.STRING);
    public static final ValueType BOOLEAN = new ValueType(null, Kind.BOOLEAN);
    public static final ValueType POINTER = new ValueType(null, Kind.POINTER);

    private static final ValueType NOT_INITIALIZED = new ValueType(null, null);

    public static boolean isAllowed(Visibility check, Visibility minAllowed) {
        return check.compareTo(minAllowed) >= 0;
    }

    @Nullable
    public final PasEntityScope owner;
    @Nullable
    public final PascalNamedElement element;
    public final String name;
    public final FieldType fieldType;
    @NotNull
    public final Visibility visibility;
    public int offset;
    // Reference target
    @Nullable
    public final PsiElement target;

    private ValueType valueType;

    private final int cachedHash;

    public PasField(@Nullable PasEntityScope owner, @Nullable PascalNamedElement element, String name, FieldType fieldType,
                    @NotNull Visibility visibility, @Nullable PsiElement target, ValueType valueType) {
        this.owner = owner;
        this.element = element;
        this.name = name;
        this.fieldType = fieldType;
        this.visibility = visibility;
        this.offset = element != null ? element.getTextOffset() : 0;
        this.target = target;
        //System.out.println(this);
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

    @Override
    public String toString() {
        return visibility + " " + fieldType + ": " + (owner != null ? owner.getName() : "-") + "." + name + ", " + element;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        PasField field = (PasField) o;

        if (!name.equals(field.name)) return false;
        if (element != null ? !element.equals(field.element) : field.element != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return cachedHash;
    }

    public boolean isTypeResolved() {
        return valueType != NOT_INITIALIZED;
    }

    public boolean isInteger() {
        return (valueType != null) && (valueType.kind == Kind.INTEGER);
    }

    public boolean isFloat() {
        return (valueType != null) && (valueType.kind == Kind.FLOAT);
    }

    public boolean isNumeric() {
        return isInteger() || isFloat();
    }

    @Nullable
    public PasField getTypeField() {
        return valueType != null ? valueType.field : null;
    }

    public ValueType getValueType() {
        return valueType;
    }

    public void setValueType(ValueType valueType) {
        this.valueType = valueType;
    }

    public static class ValueType {
        private final PasField field;
        private final Kind kind;

        private ValueType(PasField field, Kind kind) {
            this.field = field;
            this.kind = kind;
        }
    }

}
