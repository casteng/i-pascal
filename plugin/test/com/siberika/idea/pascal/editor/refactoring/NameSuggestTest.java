package com.siberika.idea.pascal.editor.refactoring;

import com.siberika.idea.pascal.util.StrUtil;
import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Author: George Bakhtadze
 * Date: 04/02/2018
 */
public class NameSuggestTest {
    @Test
    public void suggestVar() {
        Set<String> suggested = new LinkedHashSet<>();
        PascalNameSuggestionProvider.suggestNames("MyBigName", Collections.emptyList(), StrUtil.ElementType.VAR, suggested);
        printNames(suggested);
        Assert.assertEquals(new LinkedHashSet<>(Arrays.asList("MyBigName", "BigName", "Name")), suggested);
    }

    @Test
    public void suggestConst() {
        Set<String> suggested = new LinkedHashSet<>();
        PascalNameSuggestionProvider.suggestNames("MyBigName", Collections.emptyList(), StrUtil.ElementType.CONST, suggested);
        printNames(suggested);
        Assert.assertEquals(new LinkedHashSet<>(Arrays.asList("MY_BIG_NAME", "BIG_NAME", "NAME")), suggested);
    }

    @Test
    public void suggestType() {
        Set<String> suggested = new LinkedHashSet<>();
        PascalNameSuggestionProvider.suggestNames("TMyBigName", Collections.emptyList(), StrUtil.ElementType.TYPE, suggested);
        printNames(suggested);
        Assert.assertEquals(new LinkedHashSet<>(Arrays.asList("TMyBigName", "TBigName", "TName")), suggested);
    }

    private void printNames(Collection<String> names) {
        for (String name : names) {
            System.out.println(name);
        }
    }
}