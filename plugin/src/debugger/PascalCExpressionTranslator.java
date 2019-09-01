package com.siberika.idea.pascal.debugger;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.testFramework.LightVirtualFile;
import com.siberika.idea.pascal.PascalBundle;
import com.siberika.idea.pascal.PascalFileType;
import com.siberika.idea.pascal.lang.psi.PasArgumentList;
import com.siberika.idea.pascal.lang.psi.PasCallExpr;
import com.siberika.idea.pascal.lang.psi.PasConstExpression;
import com.siberika.idea.pascal.lang.psi.PasDereferenceExpr;
import com.siberika.idea.pascal.lang.psi.PasExpr;
import com.siberika.idea.pascal.lang.psi.PasIndexExpr;
import com.siberika.idea.pascal.lang.psi.PasIndexList;
import com.siberika.idea.pascal.lang.psi.PasLiteralExpr;
import com.siberika.idea.pascal.lang.psi.PasParenExpr;
import com.siberika.idea.pascal.lang.psi.PasProductExpr;
import com.siberika.idea.pascal.lang.psi.PasReferenceExpr;
import com.siberika.idea.pascal.lang.psi.PasRelationalExpr;
import com.siberika.idea.pascal.lang.psi.PasSumExpr;
import com.siberika.idea.pascal.lang.psi.PasUnaryExpr;
import com.siberika.idea.pascal.lang.psi.impl.PasExpressionImpl;

import java.util.HashMap;
import java.util.Map;

public class PascalCExpressionTranslator {

    private static final Logger LOG = Logger.getInstance(PascalCExpressionTranslator.class);

    private static final Map<String, String> OP_SUM_MAP = getSumOpMap();
    private static final Map<String, String> OP_REL_MAP = getRelOpMap();
    private static final Map<String, String> OP_MUL_MAP = getMulOpMap();

    private static Map<String, String> getSumOpMap() {
        Map<String, String> res = new HashMap<>();
        res.put("+", "+");
        res.put("-", "-");
        res.put("OR", "|");
        res.put("XOR", "^");
        return res;
    }

    private static Map<String, String> getRelOpMap() {
        Map<String, String> res = new HashMap<>();
        res.put("<>", "!=");
        res.put("=", "==");
        res.put(">=", ">=");
        res.put("<=", "<=");
        res.put(">", ">");
        res.put("<", "<");
        return res;
    }

    private static Map<String, String> getMulOpMap() {
        Map<String, String> res = new HashMap<>();
        res.put("*", "*");
        res.put("/", "/");
        res.put("DIV", "/");
        res.put("MOD", "%");
        res.put("AND", "&");
        res.put("SHL", "<<");
        res.put("SHR", ">>");
        res.put("<<", "<<");
        res.put(">>", ">>");
        return res;
    }

    public String reverse(String value) {
        return value;
    }

    public TranslatedExpression translate(String str, Project project) {
        int rangeInd = str.indexOf("..");
        str = rangeInd > 0 ? fixupRange(str, rangeInd) : str;
        VirtualFile vFile = new LightVirtualFile("test.pas", PascalFileType.INSTANCE, "begin " + str + " ;end.");
        PsiFile file = PsiManager.getInstance(project).findFile(vFile);
        PasExpressionImpl expr = PsiTreeUtil.findChildOfType(file, PasExpressionImpl.class);
        if (expr != null) {
            TranslatedExpression res = new TranslatedExpression();
            try {
                res.setExpression(doTranslate(res, expr.getExpr()));
                return res;
            } catch (RuntimeException e) {
                LOG.info("Debug error: ", e);
                return new TranslatedExpression(e.getMessage());
            }
        } else {
            return new TranslatedExpression(PascalBundle.message("debug.expression.parsing.error"));
        }
    }

    private String fixupRange(String str, int rangeInd) {
        int bracket1Index = str.indexOf('[');
        int bracket2Index = str.indexOf(']');
        if ((bracket1Index >= rangeInd) || (bracket2Index <= rangeInd)) {
            unsupported(PascalBundle.message("debug.expression.range"), PascalBundle.message("debug.expression.range.invalid"));
        }
        StringBuilder sb = new StringBuilder(str);
        sb.insert(bracket1Index + 1, '(');
        sb.insert(bracket2Index + 1, ')');
        sb.replace(rangeInd + 1, rangeInd + 3, ")--(");
        return sb.toString();
    }

    private String doTranslate(TranslatedExpression res, PasExpr expression) {
        if (expression instanceof PasLiteralExpr) {
            String text = expression.getText();
            if (((PasLiteralExpr) expression).getStringFactor() != null) {
                return "\"" + text.substring(1, text.length() - 1).replace("''", "'") + "\"";
            } else {
                if (text.startsWith("$")) {
                    return "0x" + text.substring(1);
                } else {
                    return text;
                }
            }
        } else if (expression instanceof PasConstExpression) {
            return expression.getText().replace('\'', '"');
        } else if (expression instanceof PasParenExpr) {
            return "(" + doTranslate(res, ((PasParenExpr) expression).getExprList().get(0)) + ")";
        } else if (expression instanceof PasUnaryExpr) {
            return translateUnaryExpr(res, (PasUnaryExpr) expression);
        } else if (expression instanceof PasSumExpr) {
            PasSumExpr sumExpr = (PasSumExpr) expression;
            String op = OP_SUM_MAP.get(sumExpr.getAddOp().getText().toUpperCase());
            return doTranslate(res, sumExpr.getExprList().get(0)) + " " + op + " " + doTranslate(res, sumExpr.getExprList().get(1));
        } else if (expression instanceof PasRelationalExpr) {
            PasRelationalExpr relExpr = (PasRelationalExpr) expression;
            String op = OP_REL_MAP.get(relExpr.getRelOp().getText().toUpperCase());
            if (op != null) {
                return doTranslate(res, relExpr.getExprList().get(0)) + " " + op + " " + doTranslate(res, relExpr.getExprList().get(1));
            } else {
                return unsupported(PascalBundle.message("debug.expression.operation"), relExpr.getRelOp().getText());
            }
        } else if (expression instanceof PasProductExpr) {
            PasProductExpr mulExpr = (PasProductExpr) expression;
            String op = mulExpr.getMulOp().getText().toUpperCase();
            if ("AS".equals(op)) {
                return "((" + mulExpr.getExprList().get(1).getText().toUpperCase() + ")" + doTranslate(res, mulExpr.getExprList().get(0)) + ")";
            } else {
                //TODO: decide when to use || and &&
                op = OP_MUL_MAP.get(op);
                return doTranslate(res, mulExpr.getExprList().get(0)) + " " + op + " " + doTranslate(res, mulExpr.getExprList().get(1));
            }
        } else if (expression instanceof PasDereferenceExpr) {
            return "(*" + doTranslate(res, ((PasDereferenceExpr) expression).getExpr()) + ")";
        } else if (expression instanceof PasReferenceExpr) {
            PasReferenceExpr refExpr = (PasReferenceExpr) expression;
            String fqi = refExpr.getFullyQualifiedIdent().getText().toUpperCase();
            fqi = fqi.replace("SELF", "this");
            return (refExpr.getExpr() != null ? doTranslate(res, refExpr.getExpr()) + "." : "" ) + fqi;
        } else if (expression instanceof PasIndexExpr) {
            PasIndexExpr indExpr = (PasIndexExpr) expression;
            return doTranslate(res, indExpr.getExpr()) + translateIndexes(res, indExpr.getIndexList());
        } else if (expression instanceof PasCallExpr) {
            PasCallExpr callExpr = (PasCallExpr) expression;
            return doTranslate(res, callExpr.getExpr()) + translateArgs(res, callExpr.getArgumentList());
        } else {
            return unsupported("expression", expression.getText() + " (" + expression.getClass().getSimpleName() + ")");
        }
    }

    private String translateUnaryExpr(TranslatedExpression res, PasUnaryExpr expression) {
        String op = expression.getUnaryOp().getText();
        if ("@".equals(op)) {
            return "&(" + doTranslate(res, expression.getExpr()) + ")";
        } else if ("not".equals(op)) {
            return "!(" + doTranslate(res, expression.getExpr()) + ")";
        } else {
            return op + doTranslate(res, expression.getExpr());
        }
    }

    private String translateArgs(TranslatedExpression res, PasArgumentList argumentList) {
        StringBuilder sb = new StringBuilder("(");
        for (PasExpr expr : argumentList.getExprList()) {
            if (sb.length() > 1) {
                sb.append(", ");
            }
            sb.append(doTranslate(res, expr));
        }
        sb.append(")");
        return sb.toString();
    }

    private String translateIndexes(TranslatedExpression res, PasIndexList indexList) {
        final String text = indexList.getText();
        int rangeInd = text.indexOf("--");
        if (rangeInd > 0) {
            PsiElement el = indexList.findElementAt(rangeInd - 1);
            PasParenExpr leftExpr = PsiTreeUtil.getParentOfType(el, PasParenExpr.class);
            el = indexList.findElementAt(rangeInd + 2);
            PasParenExpr rightExpr = PsiTreeUtil.getParentOfType(el, PasParenExpr.class);
            if ((leftExpr != null) && (rightExpr != null)) {
                if (res.isArray()) {
                    return unsupported(PascalBundle.message("debug.expression.range"), PascalBundle.message("debug.expression.range.multiple"));
                }
                res.setArrayLow(doTranslate(res, leftExpr));
                res.setArrayHigh(doTranslate(res, rightExpr));
                return "";
            } else {
                return unsupported(PascalBundle.message("debug.expression.range"), indexList.getText());
            }
        } else {
            StringBuilder sb = new StringBuilder("[");
            for (PasExpr expr : indexList.getExprList()) {
                if (sb.length() > 1) {
                    sb.append(", ");
                }
                sb.append(doTranslate(res, expr));
            }
            sb.append("]");
            return sb.toString();
        }
    }

    private String unsupported(String type, String id) {
        throw new RuntimeException(PascalBundle.message("debug.expression.unsupported", type, id));
    }

}
