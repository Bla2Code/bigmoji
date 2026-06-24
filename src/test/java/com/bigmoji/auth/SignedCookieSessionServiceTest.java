package com.bigmoji.auth;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.Cookie;
import java.time.Duration;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;

class SignedCookieSessionServiceTest {
  @Test
  void readsSignedSessionCookie() {
    SignedCookieSessionService service = service();
    String cookieValue =
        service
            .createCookie("user-1", "alice", "Alice", List.of(new AuthorizedGuild("guild-1", "G")))
            .getValue();
    MockHttpServletRequest request = new MockHttpServletRequest();
    request.setCookies(new Cookie(SignedCookieSessionService.COOKIE_NAME, cookieValue));

    AuthSession session = service.read(request).orElseThrow();

    assertEquals("user-1", session.userId());
    assertTrue(session.canManageGuild("guild-1"));
  }

  @Test
  void rejectsTamperedSessionCookie() {
    SignedCookieSessionService service = service();
    String cookieValue =
        service
            .createCookie("user-1", "alice", "Alice", List.of(new AuthorizedGuild("guild-1", "G")))
            .getValue();
    MockHttpServletRequest request = new MockHttpServletRequest();
    request.setCookies(new Cookie(SignedCookieSessionService.COOKIE_NAME, cookieValue + "x"));

    assertTrue(service.read(request).isEmpty());
  }

  private SignedCookieSessionService service() {
    return new SignedCookieSessionService(
        new ObjectMapper(), "test-secret-at-least-32-bytes", Duration.ofHours(1), false);
  }
}
