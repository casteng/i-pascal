package com.siberika.idea.pascal.editor.refactoring;

import com.intellij.psi.PsiElement;
import com.intellij.psi.codeStyle.NameUtil;
import com.intellij.psi.codeStyle.SuggestedNameInfo;
import com.intellij.refactoring.rename.NameSuggestionProvider;
import com.intellij.util.SmartList;
import com.intellij.util.containers.SmartHashSet;
import com.siberika.idea.pascal.PascalLanguage;
import com.siberika.idea.pascal.lang.psi.PasArrayType;
import com.siberika.idea.pascal.lang.psi.PasClassField;
import com.siberika.idea.pascal.lang.psi.PasClassProperty;
import com.siberika.idea.pascal.lang.psi.PasClassPropertySpecifier;
import com.siberika.idea.pascal.lang.psi.PasClassTypeTypeDecl;
import com.siberika.idea.pascal.lang.psi.PasConstDeclaration;
import com.siberika.idea.pascal.lang.psi.PasGenericTypeIdent;
import com.siberika.idea.pascal.lang.psi.PasPointerType;
import com.siberika.idea.pascal.lang.psi.PasSetType;
import com.siberika.idea.pascal.lang.psi.PasTypeDecl;
import com.siberika.idea.pascal.lang.psi.PasTypeDeclaration;
import com.siberika.idea.pascal.lang.psi.PasTypeID;
import com.siberika.idea.pascal.lang.psi.PascalVariableDeclaration;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;
import java.util.Set;

public class PascalNameSuggestionProvider implements NameSuggestionProvider {

    private enum TypeMod {POINTER, ARRAY, SET, METACLASS}

    public enum ElementType {VAR, CONST, TYPE, FIELD, PROPERTY, ACTUAL_PARAMETER}

    @Nullable
    @Override
    public SuggestedNameInfo getSuggestedNames(PsiElement element, @Nullable PsiElement nameSuggestionContext, Set<String> result) {
        if (!element.getLanguage().isKindOf(PascalLanguage.INSTANCE)) {
            return null;
        }
        suggestForElement(element.getParent(), result);
        return null;
    }

    public static Set<String> suggestForElement(PsiElement position) {
        Set<String> result = new SmartHashSet<>();
        suggestForElement(position, result);
        return result;
    }

    public static Set<String> suggestForElement(PsiElement position, Set<String> result) {
        while (position instanceof PasGenericTypeIdent) {
            position = position.getParent();
        }
        if (position instanceof PasTypeDeclaration) {
            List<TypeMod> mods = new SmartList<>();
            String name = retrieveTypeModsAndName(((PasTypeDeclaration) position).getTypeDecl(), mods);
            suggestNames(name, mods, ElementType.TYPE, result);
        } else if (position instanceof PascalVariableDeclaration) {
            List<TypeMod> mods = new SmartList<>();
            String name = retrieveTypeModsAndName(((PascalVariableDeclaration) position).getTypeDecl(), mods);
            suggestNames(name, mods, position instanceof PasClassField ? ElementType.FIELD : ElementType.VAR, result);
        } else if (position instanceof PasClassProperty) {
            PasTypeID typeId = ((PasClassProperty) position).getTypeID();
            String name = typeId != null ? typeId.getFullyQualifiedIdent().getNamePart() : null;
            suggestNames(name, Collections.emptyList(), ElementType.PROPERTY, result);
        } else if (position instanceof PasConstDeclaration) {
            List<TypeMod> mods = new SmartList<>();
            String name = retrieveTypeModsAndName(((PasConstDeclaration) position).getTypeDecl(), mods);
            suggestNames(name, mods, ElementType.CONST, result);
        } else if (position instanceof PasClassPropertySpecifier) {
            PsiElement prop = position.getParent();
            if (prop instanceof PasClassProperty) {
                result.add("Get" + ((PasClassProperty) prop).getName());
                result.add("Set" + ((PasClassProperty) prop).getName());
            }
        }
        return result;
    }

    private static final String[] POINTER_SUFFIXES = {"Ptr", "Pointer"};

    static void suggestNames(String typeName, List<TypeMod> mods, ElementType type, Set<String> result) {
        if (typeName != null) {
            for (String sName : extractWords(typeNameToVarName(typeName), type)) {
                for (String pointerSuffix : POINTER_SUFFIXES) {
                    String sn1 = calcSuggestedName(mods, type == ElementType.TYPE, pointerSuffix).replace("#", sName);
                    if (type == ElementType.FIELD) {
                        result.add("F" + sn1);
                    } else if ((type == ElementType.TYPE) && (sName.charAt(0) == '#')) {
                        result.add("T" + sn1);
                    }
                    result.add(sn1);
                }
            }
        }
    }

    private static String[] extractWords(String s, ElementType type) {
        String[] splitNameIntoWords = NameUtil.splitNameIntoWords(s);
        String[] result = new String[splitNameIntoWords.length];
        String lastWord = "";
        for (int i = splitNameIntoWords.length - 1; i >= 0; i--) {
            String curWord = splitNameIntoWords[i];
            if (ElementType.CONST == type) {
                curWord = curWord.toUpperCase() + (lastWord.length() == 0 ? "" : "_");
            }
            lastWord = curWord + lastWord;
            result[i] = lastWord;
        }
        return result;
    }

    private static String retrieveTypeModsAndName(PasTypeDecl decl, @NotNull List<TypeMod> mods) {
        while (decl != null) {
            PasTypeID typeId = decl.getTypeID();
            if (typeId != null) {
                return typeId.getFullyQualifiedIdent().getNamePart();
            } else {
                PasPointerType ptr = decl.getPointerType();
                if (ptr != null) {
                    mods.add(TypeMod.POINTER);
                    decl = ptr.getTypeDecl();
                } else {
                    PasArrayType arr = decl.getArrayType();
                    if (arr != null) {
                        mods.add(TypeMod.ARRAY);
                        decl = arr.getTypeDecl();
                    } else {
                        PasSetType set = decl.getSetType();
                        if (set != null) {
                            mods.add(TypeMod.SET);
                            decl = set.getTypeDecl();
                        } else {
                            PasClassTypeTypeDecl metaclass = decl.getClassTypeTypeDecl();
                            if (metaclass != null) {
                                mods.add(TypeMod.METACLASS);
                                return metaclass.getTypeID().getFullyQualifiedIdent().getNamePart();
                            } else {
                                decl = null;
                            }
                        }
                    }
                }
            }
        }
        return null;
    }

    @NotNull
    private static String calcSuggestedName(List<TypeMod> mods, boolean forType, String pointerSuffix) {
        StringBuilder sb = new StringBuilder("#");
        boolean prefixed = false;
        for (int i = mods.size() - 1; i >= 0; i--) {
            TypeMod mod = mods.get(i);
            switch (mod) {
                case POINTER: {
                    if (forType && (0 == i)) {
                        sb.insert(0, "P");
                        prefixed = true;
                    } else {
                        sb.append(pointerSuffix);
                    }
                    break;
                }
                case ARRAY: {
                    sb.append("Array");
                    break;
                }
                case SET: {
                    sb.append("Set");
                    break;
                }
                case METACLASS: {
                    if (forType && (0 == i)) {
                        sb.insert(0, "C");
                        prefixed = true;
                    } else {
                        sb.append("Class");
                    }
                }
            }
        }
        if (forType && !prefixed && (sb.charAt(0) != 'T')) {
            sb.insert(0, "T");
        }
        return sb.toString();
    }

    private static String typeNameToVarName(String typeName) {
        if (typeName.startsWith("T")) {
            return (typeName.length() > 1 && isPascalIdentifierStart(typeName.charAt(1)) ? "" : "_") + typeName.substring(1);
        } else {
            return typeName;
        }
    }

    private static boolean isPascalIdentifierStart(char c) {
        return Character.isLetter(c) || (c == '_');
    }

}
