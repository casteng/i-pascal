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

package com.siberika.idea.pascal.lang.lexer;

import com.intellij.lexer.FlexAdapter;
import com.intellij.lexer.MergingLexerAdapter;
import com.intellij.psi.tree.TokenSet;

import java.io.Reader;

/**
 * Created by IntelliJ IDEA.
 * User: jon
 * Date: Mar 20, 2010
 * Time: 7:30:59 PM
 */
public class PascalLexer extends MergingLexerAdapter implements PascalTokenTypes {
    public PascalLexer() {

        super(new FlexAdapter(new _PascalLexer((Reader) null)),
                TokenSet.create(LONGCOMMENT, STRING, SHORTCOMMENT));
    }
}