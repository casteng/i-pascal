package com.siberika.idea.pascal.editor.formatter;

import com.google.common.collect.Iterables;
import com.intellij.openapi.editor.Editor;
import com.siberika.idea.pascal.lang.psi.PasNamedIdent;
import com.siberika.idea.pascal.lang.psi.PasTypes;
import com.siberika.idea.pascal.lang.psi.PascalVariableDeclaration;
import com.siberika.idea.pascal.util.DocUtil;

import static com.siberika.idea.pascal.editor.formatter.PascalSmartEnterProcessor.getChildEndOffset;

/**
 * Author: George Bakhtadze
 * Date: 12/07/2016
 */
public class PascalCompleteIdent {

    public static void completeIdent(Editor editor, PascalVariableDeclaration el) {
        /**
         *      => _:;
         * name => name: _;
         */
        int colonPos = getChildEndOffset(PasTypes.COLON, el);
        String colonStr = colonPos >= 0 ? "" : ": ";
        String rparenStr = "";
        PasNamedIdent last = Iterables.getLast(el.getNamedIdentList(), null);
        if (last != null) {
            DocUtil.adjustDocument(editor, colonPos >= 0 ? colonPos + 1 : last.getTextRange().getEndOffset(), colonStr + DocUtil.PLACEHOLDER_CARET + rparenStr);
        }
    }

}
