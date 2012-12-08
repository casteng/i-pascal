/*
 * Copyright 2010 Jon S Akhtar (Sylvanaar)
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */

package com.siberika.idea.pascal.editor.highlighter;

import com.intellij.openapi.editor.HighlighterColors;
import com.intellij.openapi.editor.SyntaxHighlighterColors;
import com.intellij.openapi.editor.colors.TextAttributesKey;
import com.intellij.openapi.editor.markup.TextAttributes;
import com.intellij.ui.SimpleTextAttributes;
import org.jetbrains.annotations.NonNls;

import java.awt.*;


/**
 * Created by IntelliJ IDEA.
 * User: jon
 * Date: Apr 3, 2010
 * Time: 1:55:00 AM
 */
public class LuaHighlightingData {
    @NonNls
    static final String KEYWORD_ID = "LUA_KEYWORD";
    @NonNls
    static final String COMMENT_ID = "LUA_COMMENT";
    @NonNls
    static final String LONGCOMMENT_ID = "LUA_LONGCOMMENT";
    @NonNls
    static final String NUMBER_ID = "LUA_NUMBER";
    @NonNls
    static final String STRING_ID = "LUA_STRING";
    @NonNls
    static final String LONGSTRING_ID = "LUA_LONGSTRING";   
    @NonNls
    static final String LONGSTRING_BRACES_ID = "LUA_LONGSTRING_BRACES";
    @NonNls
    static final String LONGCOMMENT_BRACES_ID = "LUA_LONGCOMMENT_BRACES";
    @NonNls
    static final String BRACES_ID = "LUA_BRACES";
    @NonNls
    static final String PARENTHS_ID = "LUA_PARENTHS";
    @NonNls
    static final String BRACKETS_ID = "LUA_BRACKETS";
    @NonNls
    static final String BAD_CHARACTER_ID = "LUA_BAD_CHARACTER";
    @NonNls
    static final String COMMA_ID = "LUA_COMMA";
    @NonNls
    static final String SEMICOLON_ID = "LUA_SEMICOLON";
    @NonNls
    static final String DEFINED_CONSTANTS_ID = "LUA_DEFINED_CONSTANTS";
    @NonNls
    static final String LOCAL_VAR_ID = "LUA_LOCAL_VAR";
    @NonNls
    static final String GLOBAL_VAR_ID = "LUA_GLOBAL_VAR";
    @NonNls
    static final String FIELD_ID = "LUA_FIELD";
    @NonNls
    static final String TAIL_CALL_ID = "LUA_TAIL_CALL";
    @NonNls
    static final String PARAMETER_ID = "LUA_PARAMETER";
    @NonNls
    static final String UPVAL_ID = "LUA_UPVAL";

    @NonNls
    static final String OPERATORS_ID = "LUA_OPERATORS";
    @NonNls
    static final String LUADOC_ID = "LUA_LUADOC";
    @NonNls
    static final String LUADOC_TAG_ID = "LUA_LUADOC_TAG";
    @NonNls
    static final String LUADOC_VALUE_ID = "LUA_LUADOC_VALUE";

    private static final TextAttributes    LUADOC_ATTR =
            SyntaxHighlighterColors.DOC_COMMENT.getDefaultAttributes().clone();
    public static final  TextAttributesKey LUADOC      =
            TextAttributesKey.createTextAttributesKey(LUADOC_ID, LUADOC_ATTR);

    private static TextAttributes    LUADOC_TAG_ATTR =
            SyntaxHighlighterColors.DOC_COMMENT_TAG.getDefaultAttributes().clone();
    public static  TextAttributesKey LUADOC_TAG      =
            TextAttributesKey.createTextAttributesKey(LUADOC_TAG_ID, LUADOC_TAG_ATTR);

    private static TextAttributes    LUADOC_VALUE_ATTR =
            SyntaxHighlighterColors.DOC_COMMENT.getDefaultAttributes().clone();
    public static  TextAttributesKey LUADOC_VALUE      =
            TextAttributesKey.createTextAttributesKey(LUADOC_VALUE_ID, LUADOC_VALUE_ATTR);

    private static final TextAttributes LOCAL_VAR_ATTR = HighlighterColors.TEXT.getDefaultAttributes().clone();
    public static final TextAttributesKey LOCAL_VAR =
        TextAttributesKey.createTextAttributesKey(LOCAL_VAR_ID, LOCAL_VAR_ATTR);

    private static final TextAttributes    UPVAL_ATTR = HighlighterColors.TEXT.getDefaultAttributes().clone();
    public static final  TextAttributesKey UPVAL      = TextAttributesKey.createTextAttributesKey(UPVAL_ID, UPVAL_ATTR);

    private static final TextAttributes PARAMETER_ATTR = HighlighterColors.TEXT.getDefaultAttributes().clone();
    public static final TextAttributesKey PARAMETER =
        TextAttributesKey.createTextAttributesKey(PARAMETER_ID, PARAMETER_ATTR);


    private static final TextAttributes GLOBAL_VAR_ATTR = HighlighterColors.TEXT.getDefaultAttributes().clone();
    public static final TextAttributesKey GLOBAL_VAR =
        TextAttributesKey.createTextAttributesKey(GLOBAL_VAR_ID, GLOBAL_VAR_ATTR);

    
    public static final TextAttributesKey FIELD =
        TextAttributesKey.createTextAttributesKey(FIELD_ID, HighlighterColors.TEXT.getDefaultAttributes().clone());

    static final TextAttributes TAIL_CALL_ATTR = HighlighterColors.TEXT.getDefaultAttributes().clone();
    
    public static final TextAttributesKey TAIL_CALL =
        TextAttributesKey.createTextAttributesKey(TAIL_CALL_ID, TAIL_CALL_ATTR);

    public static final TextAttributesKey KEYWORD =
        TextAttributesKey.createTextAttributesKey(KEYWORD_ID, SyntaxHighlighterColors.KEYWORD.getDefaultAttributes().clone());

    public static final TextAttributesKey COMMENT =
        TextAttributesKey.createTextAttributesKey(COMMENT_ID, SyntaxHighlighterColors.LINE_COMMENT.getDefaultAttributes().clone());
    public static final TextAttributesKey LONGCOMMENT =
        TextAttributesKey.createTextAttributesKey(LONGCOMMENT_ID, SyntaxHighlighterColors.JAVA_BLOCK_COMMENT.getDefaultAttributes().clone());

    static final TextAttributes LONGCOMMENT_BRACES_ATTR = SyntaxHighlighterColors.JAVA_BLOCK_COMMENT.getDefaultAttributes().clone();

    public static final TextAttributesKey LONGCOMMENT_BRACES =
        TextAttributesKey.createTextAttributesKey(LONGCOMMENT_BRACES_ID, LONGCOMMENT_BRACES_ATTR);
    
    public static final TextAttributesKey NUMBER =
        TextAttributesKey.createTextAttributesKey(NUMBER_ID, SyntaxHighlighterColors.NUMBER.getDefaultAttributes().clone());
    public static final TextAttributesKey STRING =
        TextAttributesKey.createTextAttributesKey(STRING_ID, SyntaxHighlighterColors.STRING.getDefaultAttributes().clone());

    private final static TextAttributes LONGSTRING_ATTR = SyntaxHighlighterColors.STRING.getDefaultAttributes().clone();
    private final static TextAttributes LONGSTRING_BRACES_ATTR = SyntaxHighlighterColors.STRING.getDefaultAttributes().clone();
    public static final TextAttributesKey LONGSTRING =
        TextAttributesKey.createTextAttributesKey(LONGSTRING_ID, LONGSTRING_ATTR);
    public static final TextAttributesKey LONGSTRING_BRACES =
        TextAttributesKey.createTextAttributesKey(LONGSTRING_BRACES_ID, LONGSTRING_BRACES_ATTR);
    public static final TextAttributesKey BRACKETS =
        TextAttributesKey.createTextAttributesKey(BRACKETS_ID, SyntaxHighlighterColors.BRACKETS.getDefaultAttributes().clone());
    public static final TextAttributesKey BRACES =
        TextAttributesKey.createTextAttributesKey(BRACES_ID, SyntaxHighlighterColors.BRACES.getDefaultAttributes().clone());
        public static final TextAttributesKey PARENTHS =
        TextAttributesKey.createTextAttributesKey(PARENTHS_ID, SyntaxHighlighterColors.PARENTHS.getDefaultAttributes().clone());
    public static final TextAttributesKey BAD_CHARACTER =
        TextAttributesKey.createTextAttributesKey(BAD_CHARACTER_ID, HighlighterColors.BAD_CHARACTER.getDefaultAttributes().clone());

    public static final TextAttributesKey OPERATORS =
        TextAttributesKey.createTextAttributesKey(OPERATORS_ID, SyntaxHighlighterColors.OPERATION_SIGN.getDefaultAttributes().clone());
    public static final TextAttributesKey COMMA =
        TextAttributesKey.createTextAttributesKey(COMMA_ID, SyntaxHighlighterColors.COMMA.getDefaultAttributes().clone());

    public static final TextAttributesKey SEMI =
        TextAttributesKey.createTextAttributesKey(SEMICOLON_ID, SyntaxHighlighterColors.JAVA_SEMICOLON.getDefaultAttributes().clone());

    private final static TextAttributes DEFINED_CONSTANTS_ATTR = SyntaxHighlighterColors.KEYWORD.getDefaultAttributes().clone();

    public static final TextAttributesKey DEFINED_CONSTANTS =
         TextAttributesKey.createTextAttributesKey(DEFINED_CONSTANTS_ID, DEFINED_CONSTANTS_ATTR);

    static {
        DEFINED_CONSTANTS_ATTR.setForegroundColor(Color.MAGENTA);
        UPVAL_ATTR.setFontType(SimpleTextAttributes.STYLE_ITALIC);
        LONGSTRING_ATTR.setBackgroundColor(new Color(0xD0, 0xD0, 0xD0));
        GLOBAL_VAR_ATTR.setForegroundColor(new Color(128, 0, 0));
        LOCAL_VAR_ATTR.setForegroundColor(new Color(0, 153, 153));
        PARAMETER_ATTR.setForegroundColor(new Color(153, 102, 255));
        LUADOC_ATTR.setForegroundColor(new Color(64, 95, 189));
        LUADOC_TAG_ATTR.setForegroundColor(new Color(64, 95, 189));
        LUADOC_VALUE_ATTR.setForegroundColor(new Color(64, 95, 189));
    }


}
