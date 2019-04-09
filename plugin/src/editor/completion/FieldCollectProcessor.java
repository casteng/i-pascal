package com.siberika.idea.pascal.editor.completion;

import com.intellij.codeInsight.completion.CompletionResultSet;
import com.intellij.codeInsight.completion.PrioritizedLookupElement;
import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.siberika.idea.pascal.lang.psi.PasEntityScope;
import com.siberika.idea.pascal.lang.psi.impl.PasField;
import com.siberika.idea.pascal.lang.references.ResolveUtil;
import com.siberika.idea.pascal.lang.references.resolve.ResolveProcessor;

class FieldCollectProcessor implements ResolveProcessor {
    private final CompletionResultSet result;
    private final EntityCompletionContext completionContext;

    FieldCollectProcessor(CompletionResultSet result, EntityCompletionContext completionContext) {
        this.result = result;
        this.completionContext = completionContext;
    }

    @Override
    public boolean process(final PasEntityScope originalScope, final PasEntityScope scope, final PasField field, final PasField.FieldType type) {
        fieldToEntity(result, field, completionContext);
        return true;
    }

    static void fieldToEntity(CompletionResultSet result, PasField field, EntityCompletionContext completionContext) {
        if ((field.name != null) && !field.name.contains(ResolveUtil.STRUCT_SUFFIX)) {
            LookupElement lookupElement;
            LookupElementBuilder el = CompletionUtil.buildFromElement(field) ? CompletionUtil.createLookupElement(completionContext.completionParameters.getEditor(), field) : LookupElementBuilder.create(field.name);
            if (null == el) {
                return;
            }
            lookupElement = el.appendTailText(" : " + field.fieldType.toString().toLowerCase(), true).
                    withCaseSensitivity(true).withTypeText(field.owner != null ? field.owner.getName() : "-", false);
            int priority = completionContext.calcPriority(lookupElement.getLookupString(), field.name, field.fieldType, completionContext.isFromOtherFile(field));
            lookupElement = priority != 0 ? PrioritizedLookupElement.withPriority(lookupElement, priority) : lookupElement;
            result.caseInsensitive().addElement(lookupElement);
        }
    }
}
