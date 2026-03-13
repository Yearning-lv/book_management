package com.studentms.auth;

import com.studentms.common.DataRequest;
import com.studentms.common.DataResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
  private final AuthService authService;

  public AuthController(AuthService authService) {
    this.authService = authService;
  }

  @PostMapping("/login")
  public DataResponse<LoginResponse> login(@RequestBody DataRequest<LoginRequest> request) {
    LoginResponse response = authService.login(request == null ? null : request.getData());
    return DataResponse.success(response);
  }

  @PostMapping("/logout")
  public DataResponse<String> logout(@RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authorization) {
    authService.logout(resolveToken(authorization));
    return DataResponse.success("OK");
  }

  private String resolveToken(String authorization) {
    if (authorization == null || authorization.isBlank()) {
      return null;
    }
    if (authorization.regionMatches(true, 0, "Bearer ", 0, 7)) {
      return authorization.substring(7).trim();
    }
    return authorization.trim();
  }
}
