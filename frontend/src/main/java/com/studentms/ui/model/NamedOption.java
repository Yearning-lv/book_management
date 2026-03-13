package com.studentms.ui.model;

import java.util.Map;

public class NamedOption {
  private final String value;
  private final String label;
  private final Map<String, Object> raw;

  public NamedOption(String value, String label, Map<String, Object> raw) {
    this.value = value;
    this.label = label;
    this.raw = raw;
  }

  public String getValue() {
    return value;
  }

  public Integer getIntValue() {
    if (value == null || value.isBlank()) {
      return null;
    }
    try {
      return Integer.valueOf(value);
    } catch (NumberFormatException ex) {
      return null;
    }
  }

  public String getLabel() {
    return label;
  }

  public Map<String, Object> getRaw() {
    return raw;
  }

  @Override
  public String toString() {
    return label == null ? "" : label;
  }
}
