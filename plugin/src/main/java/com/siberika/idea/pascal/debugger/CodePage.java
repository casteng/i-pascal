package com.siberika.idea.pascal.debugger;

public enum CodePage {
    ACP(0L, null),
    OEMCP(1L, "IBM437"),
    ASCII(20127L, "US-ASCII"),    // US-ASCII (7-BIT)
    UTF8(65001L, "UTF-8"),        // UNICODE (UTF-8)
    UTF7(65000L, "UTF-7"),        // UNICODE (UTF-7)
    UTF16(1200L, "UTF-16LE"),     // UNICODE UTF-16, LITTLE ENDIAN BYTE ORDER (BMP OF ISO 10646); AVAILABLE ONLY TO MANAGED APPLICATIONS
    UTF16BE(1201L, "UTF-16BE"),   // UNICODE UTF-16, BIG ENDIAN BYTE ORDER; AVAILABLE ONLY TO MANAGED APPLICATIONS
    UTF32(12000L, "UTF-32LE"),      // UNICODE UTF-32, LITTLE ENDIAN BYTE ORDER; AVAILABLE ONLY TO MANAGED APPLICATIONS
    UTF32BE(12001L, "UTF-32BE"),  // UNICODE UTF-32, BIG ENDIAN BYTE ORDER; AVAILABLE ONLY TO MANAGED APPLICATIONS
    NONE(65535L, null),
    BIG5(950L, "Big5"),        // ANSI/OEM TRADITIONAL CHINESE (TAIWAN; HONG KONG SAR, PRC); CHINESE TRADITIONAL (BIG5)
    EUC_JP(51932L, "EUC-JP"),        // EUC JAPANESE
    EUC_KR(51949L, "EUC-KR"),        // EUC KOREAN
    GB18030(54936L, "GB18030"),        // WINDOWS XP AND LATER: GB18030 SIMPLIFIED CHINESE (4 BYTE); CHINESE SIMPLIFIED (GB18030)
    GB2312(936L, "GB2312"),        // ANSI/OEM SIMPLIFIED CHINESE (PRC, SINGAPORE); CHINESE SIMPLIFIED (GB2312)
    IBM_THAI(20838L, "IBM-THAI"),        // IBM EBCDIC THAI
    IBM00858(858L, "IBM00858"),        // OEM Multilingual Latin 1 + Euro symbol
    IBM01140(1140L, "IBM01140"),        // IBM EBCDIC US-CANADA (037 + EURO SYMBOL); IBM EBCDIC (US-CANADA-EURO)
    IBM01141(1141L, "IBM01141"),        // IBM EBCDIC GERMANY (20273 + EURO SYMBOL); IBM EBCDIC (GERMANY-EURO)
    IBM01142(1142L, "IBM01142"),        // IBM EBCDIC DENMARK-NORWAY (20277 + EURO SYMBOL); IBM EBCDIC (DENMARK-NORWAY-EURO)
    IBM01143(1143L, "IBM01143"),        // IBM EBCDIC FINLAND-SWEDEN (20278 + EURO SYMBOL); IBM EBCDIC (FINLAND-SWEDEN-EURO)
    IBM01144(1144L, "IBM01144"),        // IBM EBCDIC ITALY (20280 + EURO SYMBOL); IBM EBCDIC (ITALY-EURO)
    IBM01145(1145L, "IBM01145"),        // IBM EBCDIC LATIN AMERICA-SPAIN (20284 + EURO SYMBOL); IBM EBCDIC (SPAIN-EURO)
    IBM01146(1146L, "IBM01146"),        // IBM EBCDIC UNITED KINGDOM (20285 + EURO SYMBOL); IBM EBCDIC (UK-EURO)
    IBM01147(1147L, "IBM01147"),        // IBM EBCDIC FRANCE (20297 + EURO SYMBOL); IBM EBCDIC (FRANCE-EURO)
    IBM01148(1148L, "IBM01148"),        // IBM EBCDIC INTERNATIONAL (500 + EURO SYMBOL); IBM EBCDIC (INTERNATIONAL-EURO)
    IBM01149(1149L, "IBM01149"),        // IBM EBCDIC ICELANDIC (20871 + EURO SYMBOL); IBM EBCDIC (ICELANDIC-EURO)
    IBM037(37L, "IBM037"),        // IBM EBCDIC US-Canada
    IBM1026(1026L, "IBM1026"),        // IBM EBCDIC TURKISH (LATIN 5)
    IBM01047(1047L, "IBM01047"),        // IBM EBCDIC LATIN 1/OPEN SYSTEM
    IBM273(20273L, "IBM273"),        // IBM EBCDIC GERMANY
    IBM277(20277L, "IBM277"),        // IBM EBCDIC DENMARK-NORWAY
    IBM278(20278L, "IBM278"),        // IBM EBCDIC FINLAND-SWEDEN
    IBM280(20280L, "IBM280"),        // IBM EBCDIC ITALY
    IBM284(20284L, "IBM284"),        // IBM EBCDIC LATIN AMERICA-SPAIN
    IBM285(20285L, "IBM285"),        // IBM EBCDIC UNITED KINGDOM
    IBM290(20290L, "IBM290"),        // IBM EBCDIC JAPANESE KATAKANA EXTENDED
    IBM297(20297L, "IBM297"),        // IBM EBCDIC FRANCE
    IBM420(20420L, "IBM420"),        // IBM EBCDIC ARABIC
    IBM424(20424L, "IBM424"),        // IBM EBCDIC HEBREW
    IBM437(437L, "IBM437"),        // OEM United States
    IBM500(500L, "IBM500"),        // IBM EBCDIC International
    IBM775(775L, "IBM775"),        // OEM Baltic; Baltic (DOS)
    IBM850(850L, "IBM850"),        // OEM Multilingual Latin 1; Western European (DOS)
    IBM852(852L, "IBM852"),        // OEM Latin 2; Central European (DOS)
    IBM855(855L, "IBM855"),        // OEM Cyrillic (primarily Russian)
    IBM857(857L, "IBM857"),        // OEM Turkish; Turkish (DOS)
    IBM860(860L, "IBM860"),        // OEM Portuguese; Portuguese (DOS)
    IBM861(861L, "IBM861"),        // OEM Icelandic; Icelandic (DOS)
    DOS_862(862L, "DOS-862"),        // OEM Hebrew; Hebrew (DOS)
    IBM863(863L, "IBM863"),        // OEM French Canadian; French Canadian (DOS)
    IBM864(864L, "IBM864"),        // OEM Arabic; Arabic (864)
    IBM865(865L, "IBM865"),        // OEM Nordic; Nordic (DOS)
    CP866(866L, "cp866"),        // OEM Russian; Cyrillic (DOS)
    IBM869(869L, "IBM869"),        // OEM Modern Greek; Greek, Modern (DOS)
    IBM870(870L, "IBM870"),        // IBM EBCDIC Multilingual/ROECE (Latin 2); IBM EBCDIC Multilingual Latin 2
    WINDOWS_874(874L, "x-windows-874"),  // ?      // 	ANSI/OEM THAI (ISO 8859-11); THAI (WINDOWS)
    IBM871(20871L, "IBM871"),        // IBM EBCDIC ICELANDIC
    X_CP50227(50227L, "ISO-2022-CN"),        // ISO 2022 SIMPLIFIED CHINESE; CHINESE SIMPLIFIED (ISO 2022)
    ISO_2022_JP(50220L, "ISO-2022-JP"),        // ISO 2022 JAPANESE WITH NO HALFWIDTH KATAKANA; JAPANESE (JIS)
    ISO_2022_JP_0201(50222L, "ISO-2022-JP-2"),  // JIS_X0201 ?      // ISO 2022 JAPANESE JIS X 0201-1989; JAPANESE (JIS-ALLOW 1 BYTE KANA - SO/SI)
    ISO_2022_KR(50225L, "ISO-2022-KR"),        // ISO 2022 KOREAN
    ISO_8859_1(28591L, "ISO-8859-1"),        // ISO 8859-1 LATIN 1; WESTERN EUROPEAN (ISO)
    ISO_8859_2(28592L, "ISO-8859-2"),        // ISO 8859-2 CENTRAL EUROPEAN; CENTRAL EUROPEAN (ISO)
    ISO_8859_3(28593L, "ISO-8859-3"),        // ISO 8859-3 LATIN 3
    ISO_8859_4(28594L, "ISO-8859-4"),        // ISO 8859-4 BALTIC
    ISO_8859_5(28595L, "ISO-8859-5"),        // ISO 8859-5 CYRILLIC
    ISO_8859_6(28596L, "ISO-8859-6"),        // ISO 8859-6 ARABIC
    ISO_8859_7(28597L, "ISO-8859-7"),        // ISO 8859-7 GREEK
    ISO_8859_8(28598L, "ISO-8859-8"),        // ISO 8859-8 HEBREW; HEBREW (ISO-VISUAL)
    ISO_8859_8_I(38598L, "ISO-8859-8-I"),        // ISO 8859-8 HEBREW; HEBREW (ISO-LOGICAL)
    ISO_8859_9(28599L, "ISO-8859-9"),        // ISO 8859-9 TURKISH
    ISO_8859_13(28603L, "ISO-8859-13"),        // ISO 8859-13 ESTONIAN
    ISO_8859_15(28605L, "ISO-8859-15"),        // ISO 8859-15 LATIN 9
    EUC_JP_JIS(20932L, "JIS_X0212-1990"),        // JAPANESE (JIS 0208-1990 AND 0212-1990)
    KOI8R(20866L, "KOI8-R"),        // RUSSIAN (KOI8-R); CYRILLIC (KOI8-R)
    KOI8U(21866L, "KOI8-U"),        // UKRAINIAN (KOI8-U); CYRILLIC (KOI8-U)
    SHIFT_JIS(932L, "SHIFT_JIS"),        // ANSI/OEM JAPANESE; JAPANESE (SHIFT-JIS)
    WINDOWS_1250(1250L, "WINDOWS-1250"),        // ANSI CENTRAL EUROPEAN; CENTRAL EUROPEAN (WINDOWS)
    WINDOWS_1251(1251L, "WINDOWS-1251"),        // ANSI CYRILLIC; CYRILLIC (WINDOWS)
    WINDOWS_1252(1252L, "WINDOWS-1252"),        // ANSI LATIN 1; WESTERN EUROPEAN (WINDOWS)
    WINDOWS_1253(1253L, "WINDOWS-1253"),        // ANSI GREEK; GREEK (WINDOWS)
    WINDOWS_1254(1254L, "WINDOWS-1254"),        // ANSI TURKISH; TURKISH (WINDOWS)
    WINDOWS_1255(1255L, "WINDOWS-1255"),        // ANSI HEBREW; HEBREW (WINDOWS)
    WINDOWS_1256(1256L, "WINDOWS-1256"),        // ANSI ARABIC; ARABIC (WINDOWS)
    WINDOWS_1257(1257L, "WINDOWS-1257"),        // ANSI BALTIC; BALTIC (WINDOWS)
    WINDOWS_1258(1258L, "WINDOWS-1258"),        // ANSI/OEM VIETNAMESE; VIETNAMESE (WINDOWS)
    CP1025(21025L, "x-IBM1025"),        // IBM EBCDIC CYRILLIC SERBIAN-BULGARIAN
    IBM737(737L, "x-IBM737"),        // OEM Greek (formerly 437G); Greek (DOS)
    X_EBCDIC_KOREANEXTENDED(20833L, "x-IBM833"),   // ?     // IBM EBCDIC KOREAN EXTENDED
    CP875(875L, "x-IBM875"),        // IBM EBCDIC GREEK MODERN
    X_IBM930(50930L, "x-IBM930"),    //		EBCDIC Japanese (Katakana) Extended
//    X_IBM931(50931L, "x-IBM931"),        // EBCDIC US-Canada and Japanese
    X_IBM933(50933L, "x-IBM933"),        // EBCDIC Korean Extended and Korean
    X_IBM935(50935L, "x-IBM935"),        // EBCDIC Simplified Chinese Extended and Simplified Chinese
    X_IBM936(50936L, "x-mswin-936"),  // ?      // EBCDIC Simplified Chinese
    X_IBM937(50937L, "x-IBM937"),        // EBCDIC US-Canada and Traditional Chinese
    X_IBM939(50939L, "x-IBM939"),        // EBCDIC Japanese (Latin) Extended and Japanese
    KS_C_5601_1987(949L, "x-IBM949C"),        // ANSI/OEM KOREAN (UNIFIED HANGUL CODE)
    X_IBM950(51950L, "x-IBM950"),       // EUC Traditional Chinese
    ISO_2022_CN(50229L, "ISO-2022-CN"),     // ISO 2022 Traditional Chinese
    JOHAB(1361L, "x-Johab"),        // KOREAN (JOHAB)
    X_MAC_ARABIC(10004L, "x-MacArabic"),        // ARABIC (MAC)
    X_MAC_HEBREW(10005L, "x-MacHebrew"),        // HEBREW (MAC)
    X_MAC_GREEK(10006L, "x-MacGreek"),        // GREEK (MAC)
    X_MAC_CYRILLIC(10007L, "x-MacCyrillic"),        // CYRILLIC (MAC)
    X_MAC_CHINESESIMP(10008L, ""),        // MAC SIMPLIFIED CHINESE (GB 2312); CHINESE SIMPLIFIED (MAC)
    X_MAC_ROMANIAN(10010L, "x-MacRomania"),        // ROMANIAN (MAC)
    X_MAC_UKRAINIAN(10017L, "x-MacUkraine"),        // UKRAINIAN (MAC)
    X_MAC_THAI(10021L, "x-MacThai"),        // THAI (MAC)
    X_MAC_CE(10029L, "x-MacCentralEurope"),        // MAC LATIN 2; CENTRAL EUROPEAN (MAC)
    X_MAC_ICELANDIC(10079L, "x-MacIceland"),        // ICELANDIC (MAC)
    X_MAC_TURKISH(10081L, "x-MacTurkish"),        // TURKISH (MAC)
    X_MAC_CROATIAN(10082L, "x-MacCroatian"),        // CROATIAN (MAC)
    CSISO2022JP(50221L, "x-windows-50221"),        // ISO 2022 JAPANESE WITH HALFWIDTH KATAKANA; JAPANESE (JIS-ALLOW 1 BYTE KANA)
    X_CP20949(20949L, "x-windows-949"),        // KOREAN WANSUNG
    MACINTOSH(10000L, "x-MacRoman"),        // MAC ROMAN; WESTERN EUROPEAN (MAC)
    IBM00924(20924L, "IBM1047"),        // IBM EBCDIC LATIN 1/OPEN SYSTEM (1047 + EURO SYMBOL)
    EUC_CN(51936L, "EUC_CN"),        // EUC SIMPLIFIED CHINESE; CHINESE SIMPLIFIED (EUC)
    HZ_GB_2312(52936L, "HZ-GB-2312"),        // HZ-GB2312 SIMPLIFIED CHINESE; CHINESE SIMPLIFIED (HZ)
    ;

    private final long id;
    private final String javaName;

    CodePage(long id, String javaName) {
        this.id = id;
        this.javaName = javaName;
    }

    public static CodePage byId(Long codepage) {
        if (codepage != null) {
            for (CodePage value : values()) {
                if (value.id == codepage) {
                    return value;
                }
            }
        }
        return null;
    }

    public long getId() {
        return id;
    }

    public String getJavaName() {
        return javaName;
    }
}
