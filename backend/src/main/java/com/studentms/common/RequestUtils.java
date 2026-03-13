package com.studentms.common;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Map;

public final class RequestUtils {
  private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE;

  private RequestUtils() {
  }

  public static String stringValue(Map<String, Object> data, String key) {
    if (data == null || key == null || !data.containsKey(key) || data.get(key) == null) {
      return null;
    }
    String value = String.valueOf(data.get(key)).trim();
    return value.isEmpty() ? null : value;
  }

  public static Integer intValue(Map<String, Object> data, String key) {
    String value = stringValue(data, key);
    if (value == null) {
      return null;
    }
    return Integer.valueOf(value);
  }

  public static Long longValue(Map<String, Object> data, String key) {
    String value = stringValue(data, key);
    if (value == null) {
      return null;
    }
    return Long.valueOf(value);
  }

  public static BigDecimal decimalValue(Map<String, Object> data, String key) {
    String value = stringValue(data, key);
    if (value == null) {
      return null;
    }
    return new BigDecimal(value);
  }

  public static LocalDate dateValue(Map<String, Object> data, String key) {
    String value = stringValue(data, key);
    if (value == null) {
      return null;
    }
    return LocalDate.parse(value, DATE_FORMATTER);
  }
}
