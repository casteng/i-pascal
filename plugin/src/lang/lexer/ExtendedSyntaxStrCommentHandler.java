package com.siberika.idea.pascal.lang.lexer;

/**
 * Created by IntelliJ IDEA.
 * User: jon
 * Date: Apr 3, 2010
 * Time: 2:15:34 AM
 */
public class ExtendedSyntaxStrCommentHandler {
    /* Code to handle extended quote/comment syntax
    *
    * There is a basic assumption that inside a longstring or longcomment
    * you cannot begin another longstring or comment, thus there is only
    * ever 1 closing bracket to track, and once found no more closing brackets are valid
    * until another opening bracket.
    * */
    int longQLevel = 0;

    boolean isCurrentExtQuoteStart(CharSequence endQuote) {
        int level = getLevel(endQuote);
        return longQLevel == level;
    }

    void resetCurrentExtQuoteStart() {
        longQLevel=0;
    }

    void setCurrentExtQuoteStart(CharSequence cs) {
        int level = getLevel(cs);

        longQLevel = level;
    }

    private static int getLevel(CharSequence cs) {
        int level = 0;
        int comment = 0;
        while (cs.charAt(comment) == '-') comment++;
        while (cs.length() > comment+level && cs.charAt(comment+1+level) == '=') level++;
        return level;
    }

}
