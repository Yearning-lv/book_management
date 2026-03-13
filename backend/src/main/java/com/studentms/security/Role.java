package com.studentms.security;

import java.util.Locale;

public enum Role {
  ADMIN("0", "管理员"),
  STAFF("1", "工作人员"),
  READER("2", "读者");

  private final String code;
  private final String label;

  Role(String code, String label) {
    this.code = code;
    this.label = label;
  }

  public String getCode() {
    return code;
  }

  public String getLabel() {
    return label;
  }

  public static Role fromValue(String value) {
    if (value == null || value.isBlank()) {
      return null;
    }
    String normalized = value.trim();
    for (Role role : values()) {
      if (role.code.equals(normalized) || role.name().equalsIgnoreCase(normalized)
          || role.label.equals(normalized)) {
        return role;
      }
    }
    String upper = normalized.toUpperCase(Locale.ROOT);
    for (Role role : values()) {
      if (role.name().equals(upper)) {
        return role;
      }
    }
    return null;
  }
}
