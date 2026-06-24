package com.bigmoji.api;

import com.bigmoji.api.dto.AuthMeResponse;
import com.bigmoji.api.dto.InstallUrlResponse;
import com.bigmoji.auth.AuthSession;
import com.bigmoji.auth.AuthSessionInterceptor;
import com.bigmoji.auth.AuthenticationRequiredException;
import com.bigmoji.auth.DiscordOAuthClient;
import com.bigmoji.auth.OAuthStateService;
import com.bigmoji.auth.SignedCookieSessionService;
import jakarta.servlet.http.HttpServletRequest;
import java.net.URI;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
  private final DiscordOAuthClient discordOAuthClient;
  private final OAuthStateService stateService;
  private final SignedCookieSessionService sessionService;
  private final String postLoginRedirectUri;

  public AuthController(
      DiscordOAuthClient discordOAuthClient,
      OAuthStateService stateService,
      SignedCookieSessionService sessionService,
      @Value("${bigmoji.discord.oauth.post-login-redirect-uri:/}") String postLoginRedirectUri) {
    this.discordOAuthClient = discordOAuthClient;
    this.stateService = stateService;
    this.sessionService = sessionService;
    this.postLoginRedirectUri = postLoginRedirectUri;
  }

  @GetMapping("/discord/login")
  public ResponseEntity<Void> discordLogin() {
    var stateCookie = stateService.create();
    return ResponseEntity.status(302)
        .location(discordOAuthClient.authorizationUri(stateCookie.state()))
        .header(HttpHeaders.SET_COOKIE, stateCookie.cookie().toString())
        .build();
  }

  @GetMapping("/discord/callback")
  public ResponseEntity<Void> discordCallback(
      @RequestParam String code, @RequestParam String state, HttpServletRequest request) {
    if (!stateService.isValid(request, state)) {
      throw new AuthenticationRequiredException("Invalid OAuth state");
    }

    var profile = discordOAuthClient.authenticate(code);
    var sessionCookie =
        sessionService.createCookie(
            profile.userId(), profile.username(), profile.globalName(), profile.manageableGuilds());

    return ResponseEntity.status(302)
        .location(URI.create(postLoginRedirectUri))
        .header(HttpHeaders.SET_COOKIE, sessionCookie.toString())
        .header(HttpHeaders.SET_COOKIE, stateService.clearCookie().toString())
        .build();
  }

  @GetMapping("/discord/install-url")
  public InstallUrlResponse installUrl(@RequestParam(required = false) String guildId) {
    return new InstallUrlResponse(discordOAuthClient.botInstallUri(guildId).toString());
  }

  @GetMapping("/me")
  public AuthMeResponse me(
      @RequestAttribute(AuthSessionInterceptor.REQUEST_ATTRIBUTE) AuthSession session) {
    return AuthMeResponse.from(session);
  }

  @PostMapping("/logout")
  public ResponseEntity<Void> logout() {
    return ResponseEntity.noContent()
        .header(HttpHeaders.SET_COOKIE, sessionService.clearCookie().toString())
        .build();
  }
}
