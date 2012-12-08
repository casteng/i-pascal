/*
 * Copyright 2011 Jon S Akhtar (Sylvanaar)
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

package com.siberika.idea.pascal.lang.lexer;

import com.intellij.lexer.Lexer;
import com.intellij.lexer.MergingLexerAdapterBase;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.tree.TokenSet;

/**
 * Created by IntelliJ IDEA.
 * User: Jon S Akhtar
 * Date: 4/3/11
 * Time: 12:27 AM
 */
public class PascalParsingLexerMergingAdapter extends MergingLexerAdapterBase implements PascalTokenTypes {
    static final TokenSet tokensToMerge = TokenSet.create(STRING);
    static final TokenSet tokensToMerge2 = TokenSet.create(LONGCOMMENT_BEGIN, LONGCOMMENT, LONGCOMMENT_END,
            NL_BEFORE_LONGSTRING);

    static final TokenSet allMergables = TokenSet.orSet(tokensToMerge, tokensToMerge2);

    public PascalParsingLexerMergingAdapter(Lexer original) {
        super(original, new MergeFunction() {

            public IElementType merge(IElementType type, Lexer originalLexer) {
                if (!allMergables.contains(type)) {
                    return type;
                }

                TokenSet merging = tokensToMerge.contains(type) ? tokensToMerge : tokensToMerge2;
                
                while (true) {
                    final IElementType tokenType = originalLexer.getTokenType();
                    if (!merging.contains(tokenType)) break;
                    originalLexer.advance();
                }
                
                return merging == tokensToMerge ? STRING : LONGCOMMENT;
            }
        });
    }


}
