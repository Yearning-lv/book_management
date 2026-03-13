package com.studentms.ui.util;

public final class TextCleaner {
  private static final String REPLACEMENT = "（乱码）";

  private TextCleaner() {
  }

  public static String clean(String value) {
    if (value == null) {
      return "";
    }
    String text = value.trim();
    if (text.isBlank()) {
      return "";
    }
    return looksCorrupted(text) ? REPLACEMENT : text;
  }

  public static String clean(Object value) {
    if (value == null) {
      return "";
    }
    return clean(String.valueOf(value));
  }

  public static boolean looksCorrupted(String value) {
    if (value == null || value.isBlank()) {
      return false;
    }

    int bad = 0;
    int mojibake = 0;
    for (char ch : value.toCharArray()) {
      if (ch == '?' || ch == '\uFFFD') {
        bad++;
      }
      if ("ÃÂÅåÆæÐð".indexOf(ch) >= 0) {
        mojibake++;
      }
    }

    if (bad > 0 && bad * 2 >= value.length()) {
      return true;
    }
    return mojibake >= 2 && mojibake * 2 >= value.length();
  }
}
