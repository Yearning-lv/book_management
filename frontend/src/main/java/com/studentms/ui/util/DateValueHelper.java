package com.studentms.ui.util;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public final class DateValueHelper {
  private static final DateTimeFormatter DATE_TIME = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
  private static final DateTimeFormatter SPACE_DATE_TIME = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

  private DateValueHelper() {
  }

  public static LocalDate parseDate(String value) {
    if (value == null || value.isBlank()) {
      return null;
    }
    String text = value.trim();
    try {
      return LocalDate.parse(text);
    } catch (DateTimeParseException ignored) {
      // try next format
    }
    try {
      return LocalDateTime.parse(text).toLocalDate();
    } catch (DateTimeParseException ignored) {
      // try next format
    }
    try {
      return LocalDateTime.parse(text, SPACE_DATE_TIME).toLocalDate();
    } catch (DateTimeParseException ignored) {
      // try next format
    }
    try {
      return YearMonth.parse(text).atDay(1);
    } catch (DateTimeParseException ignored) {
      return null;
    }
  }

  public static String formatDate(LocalDate value) {
    return value == null ? "" : value.toString();
  }

  public static String formatDateTime(LocalDate date, String original, String defaultTime) {
    if (date == null) {
      return "";
    }
    LocalTime time = resolveTime(original, defaultTime);
    return date.atTime(time).format(DATE_TIME);
  }

  private static LocalTime resolveTime(String original, String defaultTime) {
    if (original != null && !original.isBlank()) {
      try {
        return LocalDateTime.parse(original.trim()).toLocalTime();
      } catch (DateTimeParseException ignored) {
        // try next format
      }
      try {
        return LocalDateTime.parse(original.trim(), SPACE_DATE_TIME).toLocalTime();
      } catch (DateTimeParseException ignored) {
        // try default
      }
    }
    try {
      return LocalTime.parse(defaultTime);
    } catch (DateTimeParseException ignored) {
      return LocalTime.NOON;
    }
  }
}
