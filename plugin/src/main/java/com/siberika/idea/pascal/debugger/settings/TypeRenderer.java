package com.siberika.idea.pascal.debugger.settings;

import org.jetbrains.annotations.NonNls;

import java.util.Objects;

public class TypeRenderer implements Cloneable {
    public String type;
    public String value;

    public TypeRenderer(@NonNls String name, @NonNls String value) {
        this.type = name;
        this.value = value;
    }

    public TypeRenderer() {
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    @Override
    public TypeRenderer clone() {
        try {
            return (TypeRenderer)super.clone();
        }
        catch (CloneNotSupportedException e) {
            return null;
        }
    }

    public boolean getNameIsWriteable() {
        return true;
    }

    public String getDescription() {
        return null;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TypeRenderer that = (TypeRenderer) o;
        return Objects.equals(type, that.type) &&
                Objects.equals(getValue(), that.getValue());
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, getValue());
    }
}