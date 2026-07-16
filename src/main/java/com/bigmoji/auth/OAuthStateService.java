package com.bigmoji.auth;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.Base64;
import java.util.Optional;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Service;

@Service
public class OAuthStateService {
  public static final String COOKIE_NAME = "bigmoji_oauth_state";

  private final SecureRandom secureRandom = new SecureRandom();
  private final byte[] secret;
  private final boolean secureCookies;

  public OAuthStateService(
      @Value("${bigmoji.auth.session-secret}") String sessionSecret,
      @Value("${bigmoji.auth.secure-cookies:false}") boolean secureCookies) {
    this.secret = (sessionSecret + ":oauth-state").getBytes(StandardCharsets.UTF_8);
    this.secureCookies = secureCookies;
  }

  public StateCookie create() {
    byte[] nonce = new byte[24];
    secureRandom.nextBytes(nonce);
    String payload =
        encode(nonce) + ":" + Instant.now().plus(Duration.ofMinutes(10)).getEpochSecond();
    String state = payload + "." + signature(payload);
    return new StateCookie(state, cookie(COOKIE_NAME, state, Duration.ofMinutes(10)));
  }

  public ResponseCookie clearCookie() {
    return cookie(COOKIE_NAME, "", Duration.ZERO);
  }

  public boolean isValid(HttpServletRequest request, String state) {
    if (state == null || state.isBlank()) {
      return false;
    }
    Optional<String> cookieValue = findCookie(request, COOKIE_NAME);
    if (cookieValue.isEmpty() || !constantTimeEquals(cookieValue.get(), state)) {
      return false;
    }
    String[] signedParts = state.split("\\.", 2);
    if (signedParts.length != 2 || !constantTimeEquals(signedParts[1], signature(signedParts[0]))) {
      return false;
    }
    String[] payloadParts = signedParts[0].split(":", 2);
    if (payloadParts.length != 2) {
      return false;
    }
    try {
      long expiresAt = Long.parseLong(payloadParts[1]);
      return expiresAt >= Instant.now().getEpochSecond();
    } catch (NumberFormatException ex) {
      return false;
    }
  }

  private String signature(String payload) {
    try {
      Mac mac = Mac.getInstance("HmacSHA256");
      mac.init(new SecretKeySpec(secret, "HmacSHA256"));
      return encode(mac.doFinal(payload.getBytes(StandardCharsets.UTF_8)));
    } catch (Exception ex) {
      throw new IllegalStateException("Failed to sign OAuth state", ex);
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

  public record StateCookie(String state, ResponseCookie cookie) {}
}
