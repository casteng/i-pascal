package com.siberika.idea.pascal.lang.references.resolve;

public enum Operation {
    ADD("+"), SUB("-"), OR("OR"), XOR("XOR"),
    AS("AS"),
    MUL("*"), DIV("/"), IDIV("DIV"), MOD("MOD"), AND("AND"), SHL("SHL"), SHR("SHR");

    private final String id;

    Operation(String id) {
        this.id = id;
    }

    // TODO: cache in element?
    public static Operation forId(final String id) {
        for (Operation value : values()) {
            if (id.equalsIgnoreCase(value.id)) {
                return value;
            }
        }
        return null;
    }
}
