package com.siberika.idea.pascal.debugger;

public class TranslatedExpression {
    private String expression;
    private String error;
    private String arrayLow;
    private String arrayHigh;
    private String arrayType;

    public TranslatedExpression() {
    }

    public TranslatedExpression(String error) {
        this.error = error;
    }

    public String getExpression() {
        return expression;
    }

    public void setExpression(String expression) {
        this.expression = expression;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public String getArrayLow() {
        return arrayLow;
    }

    public void setArrayLow(String arrayLow) {
        this.arrayLow = arrayLow;
    }

    public String getArrayHigh() {
        return arrayHigh;
    }

    public void setArrayHigh(String arrayHigh) {
        this.arrayHigh = arrayHigh;
    }

    public String getArrayType() {
        return arrayType;
    }

    public void setArrayType(String arrayType) {
        this.arrayType = arrayType;
    }

    public boolean isError() {
        return error != null;
    }

    public boolean isArray() {
        return arrayHigh != null;
    }
    
}
