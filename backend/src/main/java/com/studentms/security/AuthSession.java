package com.studentms.security;

import java.time.Instant;

public class AuthSession {
  private final String token;
  private final Integer userId;
  private final String account;
  private final String name;
  private final Role role;
  private final Instant expiresAt;

  public AuthSession(String token, Integer userId, String account, String name, Role role, Instant expiresAt) {
    this.token = token;
    this.userId = userId;
    this.account = account;
    this.name = name;
    this.role = role;
    this.expiresAt = expiresAt;
  }

  public String getToken() {
    return token;
  }

  public Integer getUserId() {
    return userId;
  }

  public String getAccount() {
    return account;
  }

  public String getName() {
    return name;
  }

  public Role getRole() {
    return role;
  }

  public Instant getExpiresAt() {
    return expiresAt;
  }

  public boolean isExpired() {
    return expiresAt != null && Instant.now().isAfter(expiresAt);
  }
}
