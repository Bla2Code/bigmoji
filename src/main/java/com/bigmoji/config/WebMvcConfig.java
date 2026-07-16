package com.bigmoji.config;

import com.bigmoji.auth.AuthSessionInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {
  private final AuthSessionInterceptor authSessionInterceptor;

  public WebMvcConfig(AuthSessionInterceptor authSessionInterceptor) {
    this.authSessionInterceptor = authSessionInterceptor;
  }

  @Override
  public void addInterceptors(InterceptorRegistry registry) {
    registry
        .addInterceptor(authSessionInterceptor)
        .addPathPatterns("/api/**")
        .excludePathPatterns(
            "/api/auth/discord/login",
            "/api/auth/discord/callback",
            "/api/auth/discord/install-url",
            "/api/auth/logout",
            "/actuator/**",
            "/swagger-ui/**",
            "/v3/api-docs/**");
  }
}
