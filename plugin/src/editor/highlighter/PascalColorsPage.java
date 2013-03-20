//package com.siberika.idea.pascal.editor.highlighter;
//
//import com.intellij.openapi.editor.colors.TextAttributesKey;
//import com.intellij.openapi.fileTypes.SyntaxHighlighter;
//import com.intellij.openapi.options.colors.AttributesDescriptor;
//import com.intellij.openapi.options.colors.ColorDescriptor;
//import com.intellij.openapi.options.colors.ColorSettingsPage;
//import com.siberika.idea.pascal.PascalIcons;
//import org.jetbrains.annotations.NonNls;
//import org.jetbrains.annotations.NotNull;
//import org.jetbrains.annotations.Nullable;
//
//import javax.swing.*;
//import java.util.HashMap;
//import java.util.Map;
//
///**
//* Author: George Bakhtadze
//* Date: 12/5/12
//*/
//public class PascalColorsPage implements ColorSettingsPage {
//    final String DEMO_TEXT = "<global>a</global> = { <global>foo</global>.<field>bar</field>,  <global>foo</global>.<field>bar</field>(), <global>fx</global>(), <field>f</field> = <global>a</global>, 1,  <global>FOO</global> } -- url http://www.url.com \n" +
//            "local <local>x</local>,<local>y</local> = 20,nil\n" +
//            "for <local>i</local>=1,10 do\n" +
//            "  local <local>y</local> = 0\n" +
//            "  <global>a</global>[<local>i</local>] = function() <local><upval>y</upval></local>=<local><upval>y</upval></local>+1; return <local><upval>x</upval></local>+<local>y</local>; end\n" +
//            "end\n" +
//            "\n" +
//            "--[[ " +
//            "  Multiline\n" +
//            "  Comment\n" +
//            "]]\n" +
//            "\n" +
//            "<luadoc>--- External Documentation URL (shift-F1)</luadoc>\n" +
//            "<luadoc>-- This is called by shift-F1 on the symbol, or by the</luadoc>\n" +
//            "<luadoc>-- external documentation button on the quick help panel</luadoc>\n" +
//            "<luadoc>-- <luadoc-tag>@class</luadoc-tag> <luadoc-value>tag-name</luadoc-value> The name to get documentation for.</luadoc>\n" +
//            "<luadoc>-- <luadoc-tag>@param</luadoc-tag> <parameter>name</parameter> The name to get documentation for.</luadoc>\n" +
//            "<luadoc>-- <luadoc-tag>@return</luadoc-tag> the URL of the external documentation</luadoc>\n" +
//            "function <global>getDocumentationUrl</global>(<parameter>name</parameter>) \n" +
//            "  local <local>p1</local>, <local>p2</local> = <global>string</global>.<field>match</field>(<parameter>name</parameter>, \"(%a+)\\.?(%a*)\")\n" +
//            "  local <local>url</local> = <global>BASE_URL</global> .. \"/docs/api/\" .. <local>p1</local> .. [[long string]]\n" +
//            "\n" +
//            "  if <local>p2</local> and true then <local>url</local> = <local>url</local> .. <local>p2</local>; end\n" +
//            "\n" +
//            "  function() local <local>upval_parameter</local> = <parameter><upval>name</upval></parameter> end\n" +
//            "\n" +
//            "  <local><upval>x</upval></local>, <local><upval>y</upval></local> = <local>p1</local>, <local>p2</local>\n" +
//            "\n" +
//            "  return <local>url</local>\n" +
//            "end\n" +
//            "\n" +
//            "<global>a</global> = \"BAD\n";
//
//
//
//    private static final AttributesDescriptor[] ATTRS = new AttributesDescriptor[]{
//            new AttributesDescriptor(PascalBundle.message("color.settings.number"), LuaHighlightingData.NUMBER),
//            new AttributesDescriptor(PascalBundle.message("color.settings.string"), LuaHighlightingData.STRING),
//            new AttributesDescriptor(PascalBundle.message("color.settings.longstring"), LuaHighlightingData.LONGSTRING),
//            new AttributesDescriptor(PascalBundle.message("color.settings.keyword"), LuaHighlightingData.KEYWORD),
//            new AttributesDescriptor(PascalBundle.message("color.settings.constant.keywords"), LuaHighlightingData.DEFINED_CONSTANTS),
//            new AttributesDescriptor(PascalBundle.message("color.settings.globals"), LuaHighlightingData.GLOBAL_VAR),
//            new AttributesDescriptor(PascalBundle.message("color.settings.locals"), LuaHighlightingData.LOCAL_VAR),
//            new AttributesDescriptor(PascalBundle.message("color.settings.field"), LuaHighlightingData.FIELD),
//            new AttributesDescriptor(PascalBundle.message("color.settings.parameter"), LuaHighlightingData.PARAMETER),
//            new AttributesDescriptor(PascalBundle.message("color.settings.upvalue"), LuaHighlightingData.UPVAL),
//            new AttributesDescriptor(PascalBundle.message("color.settings.comment"), LuaHighlightingData.COMMENT),
//            new AttributesDescriptor(PascalBundle.message("color.settings.longcomment"), LuaHighlightingData.LONGCOMMENT),
//            new AttributesDescriptor(PascalBundle.message("color.settings.luadoc"), LuaHighlightingData.LUADOC),
//            new AttributesDescriptor(PascalBundle.message("color.settings.luadoc.tag"), LuaHighlightingData.LUADOC_TAG),
//            new AttributesDescriptor(PascalBundle.message("color.settings.luadoc.value"), LuaHighlightingData.LUADOC_VALUE),
//            new AttributesDescriptor(PascalBundle.message("color.settings.longstring.braces"), LuaHighlightingData.LONGSTRING_BRACES),
//            new AttributesDescriptor(PascalBundle.message("color.settings.longcomment.braces"), LuaHighlightingData.LONGCOMMENT_BRACES),
//            new AttributesDescriptor(PascalBundle.message("color.settings.operation"), LuaHighlightingData.OPERATORS),
//            new AttributesDescriptor(PascalBundle.message("color.settings.brackets"), LuaHighlightingData.BRACKETS),
//            new AttributesDescriptor(PascalBundle.message("color.settings.parenths"), LuaHighlightingData.PARENTHS),
//            new AttributesDescriptor(PascalBundle.message("color.settings.braces"), LuaHighlightingData.BRACES),
//            new AttributesDescriptor(PascalBundle.message("color.settings.comma"), LuaHighlightingData.COMMA),
//            new AttributesDescriptor(PascalBundle.message("color.settings.semi"), LuaHighlightingData.SEMI),
//            new AttributesDescriptor(PascalBundle.message("color.settings.bad_character"), LuaHighlightingData.BAD_CHARACTER),
//    };
//
//    private static final Map<String, TextAttributesKey> ATTR_MAP = new HashMap<String, TextAttributesKey> ();
//
//    static {
//        ATTR_MAP.put("local", LuaHighlightingData.LOCAL_VAR);
//        ATTR_MAP.put("global", LuaHighlightingData.GLOBAL_VAR);
//        ATTR_MAP.put("field", LuaHighlightingData.FIELD);
//        ATTR_MAP.put("upval", LuaHighlightingData.UPVAL);
//        ATTR_MAP.put("parameter", LuaHighlightingData.PARAMETER);
//        ATTR_MAP.put("luadoc", LuaHighlightingData.LUADOC);
//        ATTR_MAP.put("luadoc-tag", LuaHighlightingData.LUADOC_TAG);
//        ATTR_MAP.put("luadoc-value", LuaHighlightingData.LUADOC_VALUE);
//
//    }
//
//    @NotNull
//    public String getDisplayName() {
//        return PascalBundle.message("color.settings.name");
//    }
//
//    @Nullable
//    public Icon getIcon() {
//        return PascalIcons.GENERAL;
//    }
//
//    @NotNull
//    public AttributesDescriptor[] getAttributeDescriptors() {
//        return ATTRS;
//    }
//
//    @NotNull
//    public ColorDescriptor[] getColorDescriptors() {
//        return new ColorDescriptor[0];
//    }
//
//    @NotNull
//    public SyntaxHighlighter getHighlighter() {
//        return new PascalSyntaxHighlighter();
//    }
//
//    @NonNls
//    @NotNull
//    public String getDemoText() {
//        return DEMO_TEXT;
//    }
//
//    @Nullable
//    public Map<String, TextAttributesKey> getAdditionalHighlightingTagToDescriptorMap() {
//        return ATTR_MAP;
//    }
//
//}
