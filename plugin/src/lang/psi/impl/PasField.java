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

    public enum Type {UNIT, TYPE, VARIABLE, CONSTANT, ROUTINE, PROPERTY}

    public static final Set<Type> TYPES_ALL = new HashSet<Type>(Arrays.asList(Type.values()));
    public static final Set<Type> TYPES_LEFT_SIDE = new HashSet<Type>(Arrays.asList(Type.UNIT, Type.VARIABLE, Type.PROPERTY, Type.ROUTINE));
    public static final Set<Type> TYPES_TYPE = new HashSet<Type>(Collections.singletonList(Type.TYPE));

    public enum Visibility {INTERNAL, STRICT_PRIVATE, PRIVATE, STRICT_PROTECTED, PROTECTED, PUBLIC, PUBLISHED, AUTOMATED}

    public static boolean isAllowed(Visibility check, Visibility minAllowed) {
        return check.compareTo(minAllowed) >= 0;
    }

    @Nullable
    public final PasEntityScope owner;
    @Nullable
    public final PascalNamedElement element;
    public final String name;
    public final Type type;
    @NotNull
    public final Visibility visibility;
    public int offset;
    // Reference target
    @Nullable
    public final PsiElement target;

    public PasField(@Nullable PasEntityScope owner, @Nullable PascalNamedElement element, String name, Type type, @NotNull Visibility visibility, PsiElement target) {
        this.owner = owner;
        this.element = element;
        this.name = name;
        this.type = type;
        this.visibility = visibility;
        this.offset = element != null ? element.getTextOffset() : 0;
        this.target = target;
        //System.out.println(this);
    }

    public PasField(@Nullable PasEntityScope owner, @Nullable PascalNamedElement element, String name, Type type, @NotNull Visibility visibility) {
        this(owner, element, name, type, visibility, null);
    }

    @Override
    public String toString() {
        return visibility + " " + type + ": " + (owner != null ? owner.getName() : "-") + "." + name + ", " + element;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        PasField field = (PasField) o;

        if (element != null ? !element.equals(field.element) : field.element != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return element != null ? element.hashCode() : 0;
    }
}
