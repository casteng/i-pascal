package com.siberika.idea.pascal;

import com.intellij.CommonBundle;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.PropertyKey;

import java.lang.ref.Reference;
import java.lang.ref.SoftReference;
import java.util.ResourceBundle;

/**
 * Author: George Bakhtadze
 * Date: 1/5/13
 */
public class PascalBundle {

  private static Reference<ResourceBundle> ourBundle;

  @NonNls
  public static final String BUNDLE = "com.siberika.idea.pascal.PascalBundle";

  public static String message(@PropertyKey(resourceBundle = BUNDLE)String key, Object... params) {
    return CommonBundle.message(getBundle(), key, params);
  }

  private static ResourceBundle getBundle() {
    ResourceBundle bundle = null;

    if (ourBundle != null) bundle = ourBundle.get();

    if (bundle == null) {
      bundle = ResourceBundle.getBundle(BUNDLE);
      ourBundle = new SoftReference<ResourceBundle>(bundle);
    }
    return bundle;
  }
}
