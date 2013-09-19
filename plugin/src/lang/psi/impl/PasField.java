package com.siberika.idea.pascal.lang.psi.impl;

import com.siberika.idea.pascal.lang.psi.PasStruct;
import com.siberika.idea.pascal.lang.psi.PascalNamedElement;

/**
* Author: George Bakhtadze
* Date: 14/09/2013
*/
public class PasField {
    public enum Type {UNIT, TYPE, VARIABLE, CONSTANT, ROUTINE, PROPERTY}
    public enum Visibility {STRICT_PRIVATE, PRIVATE, STRICT_PROTECTED, PROTECTED, PUBLIC, PUBLISHED, AUTOMATED}

    public final PasStruct owner;
    public final PascalNamedElement element;
    public final String name;
    public final Type type;
    public final Visibility visibility;

    PasField(PasStruct owner, PascalNamedElement element, String name, Type type, Visibility visibility) {
        this.owner = owner;
        this.element = element;
        this.name = name;
        this.type = type;
        this.visibility = visibility;
        System.out.println(this);
    }

    @Override
    public String toString() {
        return visibility + " " + type + ": " + owner.getName() + "." + name + ", " + element;
    }
}
