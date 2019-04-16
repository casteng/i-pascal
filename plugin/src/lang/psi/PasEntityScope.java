package com.siberika.idea.pascal.lang.psi;

import com.intellij.psi.NavigatablePsiElement;
import com.intellij.psi.SmartPsiElementPointer;
import com.siberika.idea.pascal.lang.psi.impl.HasUniqueName;
import com.siberika.idea.pascal.lang.psi.impl.PasField;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.List;

/**
 * Author: George Bakhtadze
 * Date: 15/09/2013
 */
public interface PasEntityScope extends PascalNamedElement, NavigatablePsiElement, HasUniqueName {
    String BUILTIN_RESULT = "Result";
    String BUILTIN_RESULT_UPPER = BUILTIN_RESULT.toUpperCase();
    String BUILTIN_SELF = "Self";
    String BUILTIN_SELF_UPPER = BUILTIN_SELF.toUpperCase();

    @NotNull
    String getName();

    @Nullable
    PasField getField(final String name);

    @Nullable
    PascalRoutine getRoutine(final String reducedName);

    @NotNull
    Collection<PasField> getAllFields();

    // owning class for methods, parent classes/interfaces for structured types //TODO: use Set
    @NotNull
    List<SmartPsiElementPointer<PasEntityScope>> getParentScope();

    /**
     * 1. For methods and method implementations returns containing class
     * 2. For routines returns containing module
     * 3. For nested routines returns containing routine
     * 4. For structured types returns containing module
     * 5. For nested structured types returns containing type
     * 6. For modules returns null
     */
    @Nullable
    PasEntityScope getContainingScope();

    String getKey();

    @NotNull
    Collection<PasWithStatement> getWithStatements();

}
