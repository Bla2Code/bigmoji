package com.bigmoji.config;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

class ApiKeyInterceptorTest {
  @Test
  void acceptsValidKey() throws Exception {
    ApiKeyInterceptor i = new ApiKeyInterceptor("k");
    MockHttpServletRequest req = new MockHttpServletRequest();
    req.addHeader("X-API-Key", "k");
    assertTrue(i.preHandle(req, new MockHttpServletResponse(), new Object()));
  }

  @Test
  void rejectsInvalidKey() throws Exception {
    ApiKeyInterceptor i = new ApiKeyInterceptor("k");
    MockHttpServletRequest req = new MockHttpServletRequest();
    req.addHeader("X-API-Key", "x");
    assertFalse(i.preHandle(req, new MockHttpServletResponse(), new Object()));
  }
}
