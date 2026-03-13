package com.studentms.ui.api;

public final class ApiConfig {
  private static final String DEFAULT_BASE_URL = "http://localhost:8081";

  private ApiConfig() {
  }

  public static String baseUrl() {
    String value = System.getProperty("api.baseUrl");
    if (value == null || value.isBlank()) {
      value = System.getenv("API_BASE_URL");
    }
    if (value == null || value.isBlank()) {
      value = DEFAULT_BASE_URL;
    }
    return value;
  }
}
