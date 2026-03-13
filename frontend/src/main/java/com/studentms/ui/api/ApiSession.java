package com.studentms.ui.api;

public final class ApiSession {
  private static String token;
  private static Integer userId;
  private static String account;
  private static String name;
  private static String role;

  private ApiSession() {
  }

  public static String getToken() {
    return token;
  }

  public static void setToken(String token) {
    ApiSession.token = token;
  }

  public static Integer getUserId() {
    return userId;
  }

  public static void setUserId(Integer userId) {
    ApiSession.userId = userId;
  }

  public static String getAccount() {
    return account;
  }

  public static void setAccount(String account) {
    ApiSession.account = account;
  }

  public static String getName() {
    return name;
  }

  public static void setName(String name) {
    ApiSession.name = name;
  }

  public static String getRole() {
    return role;
  }

  public static void setRole(String role) {
    ApiSession.role = role;
  }

  public static void clear() {
    token = null;
    userId = null;
    account = null;
    name = null;
    role = null;
  }
}
