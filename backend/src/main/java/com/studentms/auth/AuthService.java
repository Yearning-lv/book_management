package com.studentms.auth;

import com.studentms.exception.ForbiddenException;
import com.studentms.exception.UnauthorizedException;
import com.studentms.security.AuthSession;
import com.studentms.security.Role;
import com.studentms.user.LibraryUser;
import com.studentms.user.LibraryUserRepository;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class AuthService {
  private static final Duration TOKEN_TTL = Duration.ofHours(24);

  private final LibraryUserRepository libraryUserRepository;
  private final Map<String, AuthSession> sessions = new ConcurrentHashMap<>();

  public AuthService(LibraryUserRepository libraryUserRepository) {
    this.libraryUserRepository = libraryUserRepository;
  }

  public LoginResponse login(LoginRequest request) {
    if (request == null) {
      throw new IllegalArgumentException("请求数据不能为空");
    }
    String username = request.getUsername();
    String password = request.getPassword();
    if (username == null || username.isBlank() || password == null || password.isBlank()) {
      throw new IllegalArgumentException("账号或密码不能为空");
    }

    LibraryUser user = libraryUserRepository.findByAccountAndIsDeleted(username, 0)
        .orElseThrow(() -> new UnauthorizedException("账号或密码错误"));

    if (!"ACTIVE".equalsIgnoreCase(user.getStatus())) {
      throw new ForbiddenException("账号已被冻结，请联系管理员");
    }

    if (user.getPassword() == null || !user.getPassword().equals(password)) {
      throw new UnauthorizedException("账号或密码错误");
    }

    Role role = Role.fromValue(user.getRoleCode());
    if (role == null) {
      throw new ForbiddenException("账号角色无效");
    }

    Role requestRole = Role.fromValue(request.getRole());
    if (requestRole != null && requestRole != role) {
      throw new ForbiddenException("角色不匹配");
    }

    String token = UUID.randomUUID().toString().replace("-", "");
    Instant expiresAt = Instant.now().plus(TOKEN_TTL);
    AuthSession session = new AuthSession(token, user.getUserId(), user.getAccount(), user.getName(), role, expiresAt);
    sessions.put(token, session);

    return new LoginResponse(token, user.getUserId(), user.getAccount(), user.getName(), role.name());
  }

  public AuthSession verify(String token) {
    if (token == null || token.isBlank()) {
      return null;
    }
    AuthSession session = sessions.get(token);
    if (session == null) {
      return null;
    }
    if (session.isExpired()) {
      sessions.remove(token);
      return null;
    }
    return session;
  }

  public void logout(String token) {
    if (token != null) {
      sessions.remove(token);
    }
  }

}
