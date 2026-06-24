package com.bigmoji.auth;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.Cookie;
import java.time.Duration;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

class AuthSessionInterceptorTest {
  @Test
  void attachesSessionToRequest() throws Exception {
    SignedCookieSessionService sessionService = sessionService();
    AuthSessionInterceptor interceptor = new AuthSessionInterceptor(sessionService);
    String cookieValue =
        sessionService
            .createCookie("user-1", "alice", "Alice", List.of(new AuthorizedGuild("guild-1", "G")))
            .getValue();
    MockHttpServletRequest request = new MockHttpServletRequest();
    request.setCookies(new Cookie(SignedCookieSessionService.COOKIE_NAME, cookieValue));

    interceptor.preHandle(request, new MockHttpServletResponse(), new Object());

    AuthSession session =
        (AuthSession) request.getAttribute(AuthSessionInterceptor.REQUEST_ATTRIBUTE);
    assertEquals("user-1", session.userId());
  }

  @Test
  void rejectsMissingSession() {
    AuthSessionInterceptor interceptor = new AuthSessionInterceptor(sessionService());

    assertThrows(
        AuthenticationRequiredException.class,
        () ->
            interceptor.preHandle(
                new MockHttpServletRequest(), new MockHttpServletResponse(), new Object()));
  }

  private SignedCookieSessionService sessionService() {
    return new SignedCookieSessionService(
        new ObjectMapper(), "test-secret-at-least-32-bytes", Duration.ofHours(1), false);
  }
}
