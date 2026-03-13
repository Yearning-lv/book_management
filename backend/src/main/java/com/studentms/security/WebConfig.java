package com.studentms.security;

import org.springframework.context.annotation.Configuration;
import org.springframework.lang.NonNull;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.Objects;

@Configuration
public class WebConfig implements WebMvcConfigurer {
  @NonNull
  private final AuthInterceptor authInterceptor;

  public WebConfig(@NonNull AuthInterceptor authInterceptor) {
    this.authInterceptor = Objects.requireNonNull(authInterceptor);
  }

  @Override
  public void addInterceptors(@NonNull InterceptorRegistry registry) {
    registry.addInterceptor(authInterceptor)
        .addPathPatterns("/api/**");
  }
}
