package com.studentms.security;

import com.studentms.auth.AuthService;
import com.studentms.exception.ForbiddenException;
import com.studentms.exception.UnauthorizedException;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Arrays;

@Component
public class AuthInterceptor implements HandlerInterceptor {
  private final AuthService authService;

  public AuthInterceptor(AuthService authService) {
    this.authService = authService;
  }

  @Override
  public boolean preHandle(@NonNull HttpServletRequest request,
                           @NonNull HttpServletResponse response,
                           @NonNull Object handler) {
    String path = request.getRequestURI();
    if (path.startsWith("/api/auth/login") || path.startsWith("/api/health")) {
      return true;
    }

    String token = resolveToken(request);
    AuthSession session = authService.verify(token);
    if (session == null) {
      throw new UnauthorizedException("未登录或令牌无效");
    }
    AuthContext.set(session);

    if (handler instanceof HandlerMethod handlerMethod) {
      RequiresRoles requiresRoles = handlerMethod.getMethodAnnotation(RequiresRoles.class);
      if (requiresRoles == null) {
        requiresRoles = handlerMethod.getBeanType().getAnnotation(RequiresRoles.class);
      }
      if (requiresRoles != null && requiresRoles.value().length > 0) {
        boolean allowed = Arrays.stream(requiresRoles.value())
            .anyMatch(role -> role == session.getRole());
        if (!allowed) {
          throw new ForbiddenException("无权限访问");
        }
      }
    }

    return true;
  }

  @Override
  public void afterCompletion(@NonNull HttpServletRequest request,
                              @NonNull HttpServletResponse response,
                              @NonNull Object handler,
                              @Nullable Exception ex) {
    AuthContext.clear();
  }

  private String resolveToken(HttpServletRequest request) {
    String authHeader = request.getHeader("Authorization");
    if (authHeader != null && !authHeader.isBlank()) {
      if (authHeader.toLowerCase().startsWith("bearer ")) {
        return authHeader.substring(7).trim();
      }
      return authHeader.trim();
    }
    String token = request.getHeader("X-Auth-Token");
    if (token != null && !token.isBlank()) {
      return token.trim();
    }
    String param = request.getParameter("token");
    return param == null ? null : param.trim();
  }
}
