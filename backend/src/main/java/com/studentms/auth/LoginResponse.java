package com.studentms.auth;

public class LoginResponse {
  private String token;
  private Integer userId;
  private String account;
  private String name;
  private String role;

  public LoginResponse() {
  }

  public LoginResponse(String token, Integer userId, String account, String name, String role) {
    this.token = token;
    this.userId = userId;
    this.account = account;
    this.name = name;
    this.role = role;
  }

  public String getToken() {
    return token;
  }

  public void setToken(String token) {
    this.token = token;
  }

  public Integer getUserId() {
    return userId;
  }

  public void setUserId(Integer userId) {
    this.userId = userId;
  }

  public String getAccount() {
    return account;
  }

  public void setAccount(String account) {
    this.account = account;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getRole() {
    return role;
  }

  public void setRole(String role) {
    this.role = role;
  }
}
