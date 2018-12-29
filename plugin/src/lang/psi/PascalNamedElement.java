package com.siberika.idea.pascal.lang.psi;

import com.intellij.psi.PsiNameIdentifierOwner;
import com.intellij.psi.tree.TokenSet;
import com.siberika.idea.pascal.lang.psi.impl.PasField;
import org.jetbrains.annotations.NotNull;

/**
 * Author: George Bakhtadze
 * Date: 1/4/13
 */
public interface PascalNamedElement extends PascalPsiElement, PsiNameIdentifierOwner {
    TokenSet NAME_TYPE_SET = TokenSet.create(PasTypes.NAME, PasTypes.KEYWORD_IDENT, PasTypes.ESCAPED_IDENT);
    @NotNull
    String getName();
    String getNamespace();
    String getNamePart();
    @NotNull
    PasField.FieldType getType();
    // directly visible from other units. The value is stored in stub index.
    boolean isExported();
    // not exported and not an implementation/override of another entity. The value can't be stored in stub index.
    boolean isLocal();
    // invalidate cached values depending on any modification of a file or the element's subtree modification. Super must be called.
    default void invalidateCache(boolean subtreeChanged) {};
}
