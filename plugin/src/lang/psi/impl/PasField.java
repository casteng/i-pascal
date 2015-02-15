package com.siberika.idea.pascal.lang.psi.impl;

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

    public PasField(@Nullable PasEntityScope owner, @Nullable PascalNamedElement element, String name, Type type, @NotNull Visibility visibility) {
        this.owner = owner;
        this.element = element;
        this.name = name;
        this.type = type;
        this.visibility = visibility;
        //System.out.println(this);
    }

    @Override
    public String toString() {
        return visibility + " " + type + ": " + (owner != null ? owner.getName() : "-") + "." + name + ", " + element;
    }
}
