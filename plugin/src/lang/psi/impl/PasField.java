package com.siberika.idea.pascal.lang.psi.impl;

import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.SmartPointerManager;
import com.intellij.psi.SmartPsiElementPointer;
import com.intellij.psi.stubs.StubElement;
import com.siberika.idea.pascal.lang.psi.PasEntityScope;
import com.siberika.idea.pascal.lang.psi.PasTypeDecl;
import com.siberika.idea.pascal.lang.psi.PascalNamedElement;
import com.siberika.idea.pascal.lang.psi.PascalRoutine;
import com.siberika.idea.pascal.lang.psi.PascalStubElement;
import com.siberika.idea.pascal.lang.references.PasReferenceUtil;
import com.siberika.idea.pascal.lang.references.ResolveContext;
import com.siberika.idea.pascal.lang.references.ResolveUtil;
import com.siberika.idea.pascal.lang.stub.PasExportedRoutineStub;
import com.siberika.idea.pascal.lang.stub.PasNamedStub;
import com.siberika.idea.pascal.util.ModuleUtil;
import com.siberika.idea.pascal.util.SyncUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;
import java.util.concurrent.locks.ReentrantLock;

/**
* Author: George Bakhtadze
* Date: 14/09/2013
*/
public class PasField {

    public static final String DUMMY_IDENTIFIER = "____";
    private static final long PERSISTENT_VALUE = -1;

    public enum FieldType {UNIT, TYPE, VARIABLE, CONSTANT, ROUTINE, PROPERTY, PSEUDO_VARIABLE}

    public enum Kind {BOOLEAN, POINTER, INTEGER, FLOAT, CHAR, STRING, SET, STRUCT, CLASSREF, FILE, PROCEDURE, ENUM, SUBRANGE, ARRAY, VARIANT, TYPEALIAS, TYPEREF}

    public enum Access {READONLY, WRITEONLY, READWRITE}

    public static final Set<FieldType> TYPES_ALL = Collections.unmodifiableSet(EnumSet.allOf(FieldType.class));
    public static final Set<FieldType> TYPES_LEFT_SIDE = Collections.unmodifiableSet(EnumSet.of(FieldType.UNIT, FieldType.VARIABLE, FieldType.PSEUDO_VARIABLE, FieldType.PROPERTY, FieldType.ROUTINE));
    public static final Set<FieldType> TYPES_TYPE = Collections.unmodifiableSet(EnumSet.of(FieldType.TYPE));
    public static final Set<FieldType> TYPES_TYPE_UNIT = Collections.unmodifiableSet(EnumSet.of(FieldType.UNIT, FieldType.TYPE));
    public static final Set<FieldType> TYPES_ROUTINE = Collections.unmodifiableSet(EnumSet.of(FieldType.ROUTINE));
    public static final Set<FieldType> TYPES_PROPERTY_SPECIFIER = Collections.unmodifiableSet(EnumSet.of(FieldType.ROUTINE, FieldType.VARIABLE));
    public static final Set<FieldType> TYPES_STRUCTURE = Collections.unmodifiableSet(EnumSet.of(FieldType.TYPE, FieldType.VARIABLE, FieldType.CONSTANT, FieldType.PROPERTY, FieldType.ROUTINE));
    public static final Set<FieldType> TYPES_STATIC = Collections.unmodifiableSet(EnumSet.of(FieldType.UNIT, FieldType.TYPE, FieldType.CONSTANT));
    public static final Set<FieldType> TYPES_LOCAL = Collections.unmodifiableSet(EnumSet.of(FieldType.VARIABLE, FieldType.PROPERTY, FieldType.ROUTINE, FieldType.PSEUDO_VARIABLE));

    public enum Visibility {
        INTERNAL("INTERNAL"), STRICT_PRIVATE("STRICT PRIVATE"), PRIVATE("PRIVATE"),
        STRICT_PROTECTED("STRICT PROTECTED"), PROTECTED("PROTECTED"), PUBLIC("PUBLIC"), PUBLISHED("PUBLISHED"), AUTOMATED("AUTOMATED");

        private final String key;

        Visibility(String key) {
            this.key = key;
        }

        public String getKey() {
            return key;
        }

        public static Visibility byKey(String key) {
            for (Visibility visibility : values()) {
                if (visibility.getKey().equals(key)) {
                    return visibility;
                }
            }
            throw new IllegalArgumentException("Invalid visibility key: " + key);
        }

        public boolean moreStrictThan(Visibility visibility) {
            return this.compareTo(visibility) < 0;
        }
    }

    public static final ValueType INTEGER = new ValueType(null, Kind.INTEGER, null, null);
    public static final ValueType FLOAT = new ValueType(null, Kind.FLOAT, null, null);
    public static final ValueType STRING = new ValueType(null, Kind.STRING, null, null);
    public static final ValueType BOOLEAN = new ValueType(null, Kind.BOOLEAN, null, null);
    public static final ValueType POINTER = new ValueType(null, Kind.POINTER, null, null);
    public static final ValueType VARIANT = new ValueType(null, Kind.VARIANT, null, null);

    private static final ValueType NOT_INITIALIZED = new ValueType(null, null, null, null);
    private static final ValueType CONSTRUCTOR = new ValueType(null, null, null, null);

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

    private long typeTimestamp;

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
        this.offset = (element != null) && !ResolveUtil.isStubPowered(element) ? element.getTextRange().getStartOffset() : 0;
        this.target = target;
        this.valueType = valueType;
        this.cachedHash = updateHashCode();
    }

    public PasField(@Nullable PasEntityScope owner, @Nullable PascalNamedElement element, String name, FieldType fieldType,
                    @NotNull Visibility visibility, ValueType valueType) {
        this(owner, element, name, fieldType, visibility, null, valueType);
    }

    public PasField(@Nullable PasEntityScope owner, @Nullable PascalNamedElement element, String name, FieldType fieldType,
                    @NotNull Visibility visibility) {
        this(owner, element, name, fieldType, visibility, null, NOT_INITIALIZED);
    }

    public PasField(PasNamedStub stub, @Nullable String alias) {
        this(getScope(stub), stub.getPsi() instanceof PascalNamedElement ? (PascalNamedElement) stub.getPsi() : null,
                calcName(alias, stub), stub.getType(), Visibility.PUBLIC, stub.getPsi(), NOT_INITIALIZED);
    }

    private static String calcName(String alias, PasNamedStub stub) {
        if (alias != null) {
            return alias;
        }
        if (stub instanceof PasExportedRoutineStub) {
            PasExportedRoutineStub exportedRoutineStub = (PasExportedRoutineStub) stub;
            return RoutineUtil.calcCanonicalName(exportedRoutineStub.getName(), exportedRoutineStub.getFormalParameterNames(),
                    exportedRoutineStub.getFormalParameterTypes(), exportedRoutineStub.getFormalParameterAccess(),
                    exportedRoutineStub.getFunctionTypeStr(), exportedRoutineStub.getFormalParameterValues());
        } else {
            return stub.getName();
        }
    }

    private static PasEntityScope getScope(PasNamedStub stub) {
        StubElement parent = stub.getParentStub();
        PsiElement psi = parent != null ? parent.getPsi() : null;
        if (psi instanceof PasEntityScope) {
            return (PasEntityScope) psi;
        } else {
            return null;
        }
    }

    @Nullable
    public PascalNamedElement getElement() {
        return element != null ? element.getElement() : null;
    }

    @Nullable
    public SmartPsiElementPointer<PascalNamedElement> getElementPtr() {
        return element;
    }

    public boolean isConstructor() {
        PascalNamedElement el = getElement();
        if ((el instanceof PascalRoutine) && ((PascalRoutine) el).isConstructor()) {
            valueType = CONSTRUCTOR;            // TODO: move constructor handling to main resolve routine
            return true;
        }
        return false;
    }

    @Override
    public String toString() {
        return visibility + " " + fieldType + ": " + (owner != null ? owner.getName() : "-") + "." + name + ", " + getElement();
    }

    @Override
    public int hashCode() {
        return cachedHash;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        PasField pasField = (PasField) o;

        if (owner != null ? !owner.equals(pasField.owner) : pasField.owner != null) return false;
        if (name != null ? !name.equals(pasField.name) : pasField.name != null) return false;
        if (fieldType != pasField.fieldType) return false;
        if (visibility != pasField.visibility) return false;
        if (getElement() != null ? !getElement().equals(pasField.getElement()) : pasField.getElement() != null) return false;
        return true;
    }

    private int updateHashCode() {
        int result = owner != null ? owner.hashCode() : 0;
        result = 31 * result + (name != null ? name.hashCode() : 0);
        result = 31 * result + (fieldType != null ? fieldType.hashCode() : 0);
        result = 31 * result + visibility.hashCode();
        PascalNamedElement el = getElement();
        result = 31 * result + (el != null ? el.hashCode() : 0);
        return result;
    }

    @Nullable
    public ValueType getValueType(int recursionCount) {
        calcValueType(recursionCount);
        return valueType;
    }

    private void calcValueType(int recursionCount) {
        PascalNamedElement el = getElement();
        long timestamp = el != null ? el.getContainingFile().getModificationStamp() : 0;
        if (SyncUtil.tryLockQuiet(typeLock, SyncUtil.LOCK_TIMEOUT_MS)) {
            try {
                if (isValueTypeCacheDirty(timestamp)) {
                    if (ResolveUtil.isStubPowered(el)) {
                        ResolveContext context = new ResolveContext(owner, PasField.TYPES_TYPE, true, null, ModuleUtil.retrieveUnitNamespaces(el));
                        valueType = ResolveUtil.resolveTypeWithStub((PascalStubElement) el, context, recursionCount);
                    } else {
                        valueType = PasReferenceUtil.resolveFieldType(this, true, recursionCount);
                    }
                    if (valueType != null) {
                        if (el != null) {
                            typeTimestamp = el.getContainingFile().getModificationStamp();
                        }
                        valueType.field = this;
                    }
                }
            } finally {
                typeLock.unlock();
            }
        }
    }

    private boolean isValueTypeCacheDirty(long timestamp) {
        return (typeTimestamp != PERSISTENT_VALUE) && ((valueType == NOT_INITIALIZED) || (timestamp > typeTimestamp));
    }

    void setValueType(@NotNull ValueType valueType) {
        if (SyncUtil.tryLockQuiet(typeLock, SyncUtil.LOCK_TIMEOUT_MS)) {
            try {
                this.valueType = valueType;
                this.typeTimestamp = PERSISTENT_VALUE;
            } finally {
                typeLock.unlock();
            }
        }
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
            case VARIANT:
                return VARIANT;
            default:
                return null;
        }
    }

    private static Kind getKindByName(String value) {
        String valueUpper = value != null ? value.toUpperCase() : null;
        for (Kind kind : Kind.values()) {
            if (kind.name().equals(valueUpper)) {
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
        public SmartPsiElementPointer<PsiElement> declaration;

        public ValueType(PasField field, Kind kind, ValueType baseType, PsiElement declaration) {
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
            PsiElement el = declaration != null ? declaration.getElement() : null;
            if (el instanceof PasEntityScope) {
                return (PasEntityScope) el;
            }
            ValueType type = this;
            while (type.baseType != null) {
                type = type.baseType;
            }
            PsiElement declEl = type.declaration != null ? type.declaration.getElement() : null;
            if (declEl instanceof PasEntityScope) {
                return (PasEntityScope) declEl;
            }
            PasTypeDecl typeDecl = declEl instanceof PasTypeDecl ? (PasTypeDecl) declEl : null;
            if ((typeDecl != null) && (typeDecl.getFirstChild() instanceof PasEntityScope)) {
                return (PasEntityScope) typeDecl.getFirstChild();
            }
            return null;
        }

        // Searches all type chain for structured type using only stub tree //TODO: check
        @Nullable
        public PasEntityScope getTypeScopeStub() {
            ValueType type = this;
            while (type.baseType != null) {
                type = type.baseType;
            }
            PsiElement el = type.declaration != null ? type.declaration.getElement() : null;
            return el instanceof PasEntityScope ? (PasEntityScope) el : null;
        }

    }

}
