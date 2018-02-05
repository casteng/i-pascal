package com.siberika.idea.pascal.editor.refactoring;

import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * Author: George Bakhtadze
 * Date: 04/02/2018
 */
public class NameSuggestTest {
    @Test
    public void suggestVar() {
        Set<String> suggested = new HashSet<>();
        PascalNameSuggestionProvider.suggestNames(suggested, "MyBigName", false);
        printNames(suggested);
        Assert.assertTrue(suggested.containsAll(Arrays.asList("Name", "BigName", "MyBigName")));
    }

    @Test
    public void suggestConst() {
        Set<String> suggested = new HashSet<>();
        PascalNameSuggestionProvider.suggestNames(suggested, "MyBigName", true);
        printNames(suggested);
        Assert.assertTrue(suggested.containsAll(Arrays.asList("NAME", "BIG_NAME", "MY_BIG_NAME")));
    }

    private void printNames(Collection<String> names) {
        for (String name : names) {
            System.out.println(name);
        }
    }
}