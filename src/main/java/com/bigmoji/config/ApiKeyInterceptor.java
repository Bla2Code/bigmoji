package com.bigmoji.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class ApiKeyInterceptor implements HandlerInterceptor {
  private final String expectedApiKey;

  public ApiKeyInterceptor(@Value("${bigmoji.api-key}") String expectedApiKey) {
    this.expectedApiKey = expectedApiKey;
  }

  @Override
  public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
      throws Exception {
    String apiKey = request.getHeader("X-API-Key");
    if (expectedApiKey.equals(apiKey)) {
      return true;
    }
    response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Missing or invalid API key");
    return false;
  }
}
