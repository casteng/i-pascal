package com.siberika.idea.pascal.editor.completion;

import com.intellij.lang.Commenter;
import org.jetbrains.annotations.Nullable;

/**
 * Author: George Bakhtadze
 * Date: 01/10/2013
 */
public class PascalCommenter implements Commenter {
    @Nullable
    @Override
    public String getLineCommentPrefix() {
        return "//";
    }

    @Nullable
    @Override
    public String getBlockCommentPrefix() {
        return "{";
    }

    @Nullable
    @Override
    public String getBlockCommentSuffix() {
        return "}";
    }

    @Nullable
    @Override
    public String getCommentedBlockCommentPrefix() {
        return "(*";
    }

    @Nullable
    @Override
    public String getCommentedBlockCommentSuffix() {
        return "*)";
    }
}
