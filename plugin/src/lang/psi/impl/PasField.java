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

    public static final Set<FieldType> TYPES_ALL = new HashSet<FieldType>(Arrays.asList(FieldType.values()));
    public static final Set<FieldType> TYPES_LEFT_SIDE = new HashSet<FieldType>(Arrays.asList(FieldType.UNIT, FieldType.VARIABLE, FieldType.PROPERTY, FieldType.ROUTINE));
    public static final Set<FieldType> TYPES_TYPE = new HashSet<FieldType>(Collections.singletonList(FieldType.TYPE));
    public static final Set<FieldType> TYPES_TYPE_UNIT = new HashSet<FieldType>(Arrays.asList(FieldType.UNIT, FieldType.TYPE));

    public enum Visibility {INTERNAL, STRICT_PRIVATE, PRIVATE, STRICT_PROTECTED, PROTECTED, PUBLIC, PUBLISHED, AUTOMATED}

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

    public final PasField typeField;

    private final int cachedHash;

    public PasField(@Nullable PasEntityScope owner, @Nullable PascalNamedElement element, String name, FieldType fieldType, @NotNull Visibility visibility, @Nullable PsiElement target, PasField typeField) {
        this.owner = owner;
        this.element = element;
        this.name = name;
        this.fieldType = fieldType;
        this.visibility = visibility;
        this.offset = element != null ? element.getTextOffset() : 0;
        this.target = target;
        //System.out.println(this);
        this.cachedHash = name.hashCode() * 31 + (element != null ? element.hashCode() : 0);
        this.typeField = typeField;
    }

    public PasField(@Nullable PasEntityScope owner, @Nullable PascalNamedElement element, String name, FieldType fieldType, @NotNull Visibility visibility, PasField typeField) {
        this(owner, element, name, fieldType, visibility, null, typeField);
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
}
