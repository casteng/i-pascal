package com.siberika.idea.pascal.lang.psi.impl;

import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiErrorElement;
import com.intellij.psi.PsiWhiteSpace;
import com.intellij.psi.tree.TokenSet;
import com.intellij.psi.util.PsiTreeUtil;
import com.siberika.idea.pascal.lang.psi.PasArgumentList;
import com.siberika.idea.pascal.lang.psi.PasBlockBody;
import com.siberika.idea.pascal.lang.psi.PasBlockLocal;
import com.siberika.idea.pascal.lang.psi.PasCallExpr;
import com.siberika.idea.pascal.lang.psi.PasCompoundStatement;
import com.siberika.idea.pascal.lang.psi.PasDereferenceExpr;
import com.siberika.idea.pascal.lang.psi.PasEntityScope;
import com.siberika.idea.pascal.lang.psi.PasExportedRoutine;
import com.siberika.idea.pascal.lang.psi.PasFormalParameter;
import com.siberika.idea.pascal.lang.psi.PasFormalParameterSection;
import com.siberika.idea.pascal.lang.psi.PasFunctionDirective;
import com.siberika.idea.pascal.lang.psi.PasParamType;
import com.siberika.idea.pascal.lang.psi.PasProcBodyBlock;
import com.siberika.idea.pascal.lang.psi.PasReferenceExpr;
import com.siberika.idea.pascal.lang.psi.PasRoutineImplDecl;
import com.siberika.idea.pascal.lang.psi.PasTypeDecl;
import com.siberika.idea.pascal.lang.psi.PasTypes;
import com.siberika.idea.pascal.lang.psi.PascalNamedElement;
import com.siberika.idea.pascal.lang.psi.PascalRoutine;
import com.siberika.idea.pascal.lang.psi.PascalRoutineEntity;
import com.siberika.idea.pascal.lang.psi.field.ParamModifier;
import com.siberika.idea.pascal.util.PsiUtil;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

public class RoutineUtil {

    static final TokenSet FUNCTION_KEYWORDS = TokenSet.create(PasTypes.FUNCTION, PasTypes.OPERATOR);

    private static final Pattern PATTERN_CONSTRAINT = Pattern.compile(":\\s*\\w+");

    static String calcKey(PascalRoutine routine) {
        StringBuilder sb = new StringBuilder(PsiUtil.getFieldName(routine));
        sb.append(PsiUtil.isForwardProc(routine) ? "-fwd" : "");
        if (routine instanceof PasExportedRoutine) {
            sb.append("^intf");
        } else {
            sb.append("^impl");
        }

        PasEntityScope scope = routine.getContainingScope();
        sb.append(scope != null ? "." + scope.getKey() : "");

//        System.out.println(String.format("%s:%d - %s", PsiUtil.getFieldName(this), this.getTextOffset(), sb.toString()));
        return sb.toString();
    }

    static boolean isConstructor(PsiElement routine) {
        PsiElement first = routine.getFirstChild();
        if (first.getNode().getElementType() == PasTypes.CONSTRUCTOR) {
            return true;
        } else if (first.getNode().getElementType() == PasTypes.CLASS) {
            PsiElement second = PsiUtil.getNextSibling(first);
            return (second != null) && (second.getNode().getElementType() == PasTypes.CONSTRUCTOR);
        } else {
            return false;
        }
    }

    public static boolean isDestructor(PascalRoutine routine) {
        PsiElement first = routine.getFirstChild();
        if (first.getNode().getElementType() == PasTypes.DESTRUCTOR) {
            return true;
        } else if (first.getNode().getElementType() == PasTypes.CLASS) {
            PsiElement second = PsiUtil.getNextSibling(first);
            return (second != null) && (second.getNode().getElementType() == PasTypes.DESTRUCTOR);
        } else {
            return false;
        }
    }

    static void calcFormalParameterNames(PasFormalParameterSection formalParameterSection, List<String> formalParameterNames, List<String> formalParameterTypes, List<ParamModifier> formalParameterAccess, List<String> formalParameterValues) {
        if (formalParameterSection != null) {
            for (PasFormalParameter parameter : formalParameterSection.getFormalParameterList()) {
                PasTypeDecl td = parameter.getTypeDecl();
                String typeStr = td != null ? td.getText() : null;
                ParamModifier modifier = calcModifier(parameter.getParamType());
                for (PascalNamedElement pasNamedIdent : parameter.getNamedIdentDeclList()) {
                    formalParameterNames.add(pasNamedIdent.getName());
                    formalParameterTypes.add(typeStr != null ? typeStr : "");
                    formalParameterAccess.add(modifier);
                }
                if ((formalParameterValues != null) && (parameter.getConstExpression() != null)) {
                    formalParameterValues.add(parameter.getConstExpression().getText());
                }
            }
        }
    }

    private static ParamModifier calcModifier(PasParamType paramType) {
        if (paramType != null) {
            String text = paramType.getText().toUpperCase();
            if ("CONST".equals(text)) {
                return ParamModifier.CONST;
            } else if ("VAR".equals(text)) {
                return ParamModifier.VAR;
            } else if ("OUT".equals(text)) {
                return ParamModifier.OUT;
            } else if ("CONSTREF".equals(text)) {
                return ParamModifier.CONSTREF;
            }
        }
        return ParamModifier.NONE;
    }

    public static PasCallExpr retrieveCallExpr(PsiElement element) {
        PsiElement expr = PsiTreeUtil.skipParentsOfType(element, PascalNamedElement.class, PasReferenceExpr.class, PasDereferenceExpr.class,
                PsiWhiteSpace.class, PsiErrorElement.class);
        if (expr instanceof PasArgumentList) {
            expr = expr.getParent();
            return expr instanceof PasCallExpr ? (PasCallExpr) expr : null;
        }
        return null;
    }

    public static boolean isSuitable(PasCallExpr expression, PascalRoutineEntity routine) {
        List<String> params = routine.getFormalParameterNames();
        List<String> defValues = routine.getFormalParameterDefaultValues();
        int actCount = expression.getArgumentList().getExprList().size();
        // TODO: make type check and handle overload
        if (((params.size() - defValues.size()) <= actCount) && ((params.size() + defValues.size()) >= actCount)) {
            return true;
        }
        return false;
    }

    public static String calcCanonicalName(String name, List<String> formalParameterNames, List<String> formalParameterTypes, List<ParamModifier> formalParameterAccess, String typeStr, List<String> formalParameterValues) {
        StringBuilder res = new StringBuilder(name);
        res.append("(");
        for (int i = 0; i < formalParameterTypes.size(); i++) {
            res.append(i > 0 ? "," : "");
            String typeName = formalParameterTypes.get(i);
            typeName = StringUtil.isNotEmpty(typeName) ? typeName : PsiUtil.TYPE_UNTYPED_NAME;
            ParamModifier modifier = formalParameterAccess.get(i);
            if (modifier != ParamModifier.NONE) {
                res.append(modifier.name().toLowerCase()).append(" ");
            }
            res.append(formalParameterNames.get(i)).append(":");
            res.append(typeName);
            if (i >= formalParameterNames.size() - formalParameterValues.size()) {
                int idx = i - (formalParameterNames.size() - formalParameterValues.size());
                res.append(" = ").append(formalParameterValues.get(idx));
            }

        }
        res.append(")");
        if (StringUtil.isNotEmpty(typeStr)) {
            res.append(":").append(typeStr);
        }
        return res.toString();
    }

    public static String calcReducedName(String name, String[] reducedParameterTypes) {
        StringBuilder res = new StringBuilder(name);
        res.append("(");
        for (int i = 0; i < reducedParameterTypes.length; i++) {
            res.append(i > 0 ? "," : "");
            res.append(reducedParameterTypes[i]);
        }
        res.append(")");
        return res.toString();
    }

    static List<String> parseTypeParametersStr(String typeParamText) {
        List<String> result;
        if (typeParamText != null && typeParamText.length() > 1) {
            result = Arrays.asList(typeParamText.substring(1, typeParamText.length() - 1).replaceAll("\\s*:\\s*\\w+", "").split("[,;]\\s*", 100));
        } else {
            result = Collections.emptyList();
        }
        return result;
    }

    public static String calcCanonicalTypeName(String name) {
        return PATTERN_CONSTRAINT.matcher(name).replaceAll("");
    }

    public static boolean isOverloaded(PasExportedRoutine routine) {
        for (PasFunctionDirective directive : routine.getFunctionDirectiveList()) {
            if (directive.getText().toUpperCase().startsWith("OVERLOAD")) {
                return true;
            }
        }
        return false;
    }

    public static boolean isOverridden(PasExportedRoutine routine) {
        for (PasFunctionDirective directive : routine.getFunctionDirectiveList()) {
            if (directive.getText().toUpperCase().startsWith("OVERRIDE")) {
                return true;
            }
        }
        return false;
    }

    public static boolean isExternal(PasExportedRoutine routine) {
        return routine.getExternalDirective() != null;
    }

    public static int calcMethodPos(PasEntityScope scope, PsiElement prevMethod) {
        if (prevMethod != null) {
            return prevMethod.getTextRange().getEndOffset();
        } else {
            PsiElement pos = PsiUtil.findEndSibling(scope.getFirstChild());
            return pos != null ? pos.getTextRange().getStartOffset() : -1;
        }
    }

    public static CharSequence prepareRoutineHeaderText(String text, String virtualReplacement, String abstractReplacement) {
        String res = StringUtil.replace(text, "virtual", virtualReplacement, true);
        res = StringUtil.replace(res, "abstract", abstractReplacement, true);
        res = res.replaceAll(";\\s*;", ";");
        return res;
    }

    public static PascalRoutine findRoutine(Collection<PasField> allFields, String reducedName) {
        if (null == reducedName) {
            return null;
        }
        for (PasField field : allFields) {
            if (field.fieldType == PasField.FieldType.ROUTINE) {
                PascalNamedElement el = field.getElement();
                if (el instanceof PascalRoutine) {
                    PascalRoutine routine = (PascalRoutine) el;
                    if (reducedName.equalsIgnoreCase(routine.getReducedName())) {
                        return routine;
                    }
                }
            }
        }
        return null;
    }

    public static boolean isFunctionResultReference(PascalNamedElement element, String name) {
        return PasEntityScope.BUILTIN_RESULT.equalsIgnoreCase(element.getName()) || name.equalsIgnoreCase(element.getName());
    }

    public static PasCompoundStatement retrieveRoutineCodeBlock(PascalRoutine routine) {
        PasProcBodyBlock block = ((PasRoutineImplDecl) routine).getProcBodyBlock();
        PasBlockLocal blockLocal = block != null ? block.getBlockLocal() : null;
        PasBlockBody blockBody = blockLocal != null ? blockLocal.getBlockBody() : null;
        return blockBody != null ? blockBody.getCompoundStatement() : null;
    }

    public static String getCanonicalNameWoScope(PascalRoutine routine) {
        String result = routine.getCanonicalName();
        if (result != null) {
            int nameEnd = result.indexOf('(');
            int lastDot = result.substring(0, nameEnd).indexOf('.');
            result = lastDot > 0 ? result.substring(lastDot + 1) : result;
        }
        return result;
    }
}
