package com.bigmoji.auth;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import java.util.Optional;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Service;

@Service
public class SignedCookieSessionService {
  public static final String COOKIE_NAME = "bigmoji_session";

  private final ObjectMapper objectMapper;
  private final byte[] secret;
  private final Duration ttl;
  private final boolean secureCookies;

  public SignedCookieSessionService(
      ObjectMapper objectMapper,
      @Value("${bigmoji.auth.session-secret}") String sessionSecret,
      @Value("${bigmoji.auth.session-ttl}") Duration ttl,
      @Value("${bigmoji.auth.secure-cookies:false}") boolean secureCookies) {
    this.objectMapper = objectMapper;
    this.secret = sessionSecret.getBytes(StandardCharsets.UTF_8);
    this.ttl = ttl;
    this.secureCookies = secureCookies;
  }

  public ResponseCookie createCookie(
      String userId, String username, String globalName, List<AuthorizedGuild> guilds) {
    Instant expiresAt = Instant.now().plus(ttl);
    SessionPayload payload =
        new SessionPayload(
            userId, username, globalName, List.copyOf(guilds), expiresAt.getEpochSecond());
    return cookie(COOKIE_NAME, sign(payload), ttl);
  }

  public ResponseCookie clearCookie() {
    return cookie(COOKIE_NAME, "", Duration.ZERO);
  }

  public Optional<AuthSession> read(HttpServletRequest request) {
    String cookieValue = findCookie(request, COOKIE_NAME).orElse(null);
    if (cookieValue == null || cookieValue.isBlank()) {
      return Optional.empty();
    }

    String[] parts = cookieValue.split("\\.", 2);
    if (parts.length != 2 || !constantTimeEquals(parts[1], signature(parts[0]))) {
      return Optional.empty();
    }

    try {
      SessionPayload payload = objectMapper.readValue(decode(parts[0]), SessionPayload.class);
      Instant expiresAt = Instant.ofEpochSecond(payload.expiresAtEpochSecond());
      if (expiresAt.isBefore(Instant.now())) {
        return Optional.empty();
      }
      return Optional.of(
          new AuthSession(
              payload.userId(),
              payload.username(),
              payload.globalName(),
              List.copyOf(payload.manageableGuilds()),
              expiresAt));
    } catch (IOException | IllegalArgumentException ex) {
      return Optional.empty();
    }
  }

  private String sign(SessionPayload payload) {
    try {
      String encodedPayload = encode(objectMapper.writeValueAsBytes(payload));
      return encodedPayload + "." + signature(encodedPayload);
    } catch (JsonProcessingException ex) {
      throw new IllegalStateException("Failed to create auth session", ex);
    }
  }

  private String signature(String encodedPayload) {
    try {
      Mac mac = Mac.getInstance("HmacSHA256");
      mac.init(new SecretKeySpec(secret, "HmacSHA256"));
      return encode(mac.doFinal(encodedPayload.getBytes(StandardCharsets.UTF_8)));
    } catch (Exception ex) {
      throw new IllegalStateException("Failed to sign auth session", ex);
    }
  }

  private boolean constantTimeEquals(String left, String right) {
    return MessageDigest.isEqual(
        left.getBytes(StandardCharsets.UTF_8), right.getBytes(StandardCharsets.UTF_8));
  }

  private ResponseCookie cookie(String name, String value, Duration maxAge) {
    return ResponseCookie.from(name, value)
        .path("/")
        .httpOnly(true)
        .secure(secureCookies)
        .sameSite("Lax")
        .maxAge(maxAge)
        .build();
  }

  private Optional<String> findCookie(HttpServletRequest request, String name) {
    Cookie[] cookies = request.getCookies();
    if (cookies == null) {
      return Optional.empty();
    }
    return Arrays.stream(cookies)
        .filter(cookie -> name.equals(cookie.getName()))
        .map(Cookie::getValue)
        .findFirst();
  }

  private String encode(byte[] value) {
    return Base64.getUrlEncoder().withoutPadding().encodeToString(value);
  }

  private byte[] decode(String value) {
    return Base64.getUrlDecoder().decode(value);
  }

  private record SessionPayload(
      String userId,
      String username,
      String globalName,
      List<AuthorizedGuild> manageableGuilds,
      long expiresAtEpochSecond) {}
}
