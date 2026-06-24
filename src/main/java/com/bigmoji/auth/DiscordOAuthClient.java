package com.bigmoji.auth;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.math.BigInteger;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class DiscordOAuthClient {
  private static final String DISCORD_API_BASE = "https://discord.com/api";
  private static final BigInteger ADMINISTRATOR = BigInteger.ONE.shiftLeft(3);
  private static final BigInteger MANAGE_GUILD = BigInteger.ONE.shiftLeft(5);

  private final ObjectMapper objectMapper;
  private final HttpClient httpClient;
  private final String clientId;
  private final String clientSecret;
  private final String redirectUri;
  private final String botPermissions;

  public DiscordOAuthClient(
      ObjectMapper objectMapper,
      @Value("${bigmoji.discord.oauth.client-id:}") String clientId,
      @Value("${bigmoji.discord.oauth.client-secret:}") String clientSecret,
      @Value("${bigmoji.discord.oauth.redirect-uri:}") String redirectUri,
      @Value("${bigmoji.discord.oauth.bot-permissions:125952}") String botPermissions) {
    this.objectMapper = objectMapper;
    this.httpClient = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(5)).build();
    this.clientId = clientId;
    this.clientSecret = clientSecret;
    this.redirectUri = redirectUri;
    this.botPermissions = botPermissions;
  }

  public URI authorizationUri(String state) {
    requireClientId();
    Map<String, String> params = new LinkedHashMap<>();
    params.put("response_type", "code");
    params.put("client_id", clientId);
    params.put("redirect_uri", redirectUri);
    params.put("scope", "identify guilds");
    params.put("state", state);
    return URI.create(DISCORD_API_BASE + "/oauth2/authorize?" + form(params));
  }

  public URI botInstallUri(String guildId) {
    requireClientId();
    Map<String, String> params = new LinkedHashMap<>();
    params.put("client_id", clientId);
    params.put("permissions", botPermissions);
    params.put("scope", "bot");
    if (guildId != null && !guildId.isBlank()) {
      params.put("guild_id", guildId);
      params.put("disable_guild_select", "true");
    }
    return URI.create(DISCORD_API_BASE + "/oauth2/authorize?" + form(params));
  }

  public DiscordOAuthProfile authenticate(String code) {
    requireConfigured();
    TokenResponse token = exchangeCode(code);
    DiscordUser user = getCurrentUser(token.accessToken());
    List<AuthorizedGuild> guilds =
        getCurrentUserGuilds(token.accessToken()).stream()
            .filter(this::isManageable)
            .map(guild -> new AuthorizedGuild(guild.id(), guild.name()))
            .toList();
    return new DiscordOAuthProfile(user.id(), user.username(), user.globalName(), guilds);
  }

  private TokenResponse exchangeCode(String code) {
    Map<String, String> params = new LinkedHashMap<>();
    params.put("grant_type", "authorization_code");
    params.put("code", code);
    params.put("redirect_uri", redirectUri);
    params.put("client_id", clientId);
    params.put("client_secret", clientSecret);

    HttpRequest request =
        HttpRequest.newBuilder(URI.create(DISCORD_API_BASE + "/oauth2/token"))
            .timeout(Duration.ofSeconds(10))
            .header("Content-Type", "application/x-www-form-urlencoded")
            .POST(HttpRequest.BodyPublishers.ofString(form(params)))
            .build();
    return send(request, TokenResponse.class);
  }

  private DiscordUser getCurrentUser(String accessToken) {
    HttpRequest request =
        HttpRequest.newBuilder(URI.create(DISCORD_API_BASE + "/users/@me"))
            .timeout(Duration.ofSeconds(10))
            .header("Authorization", "Bearer " + accessToken)
            .GET()
            .build();
    return send(request, DiscordUser.class);
  }

  private List<DiscordGuild> getCurrentUserGuilds(String accessToken) {
    HttpRequest request =
        HttpRequest.newBuilder(URI.create(DISCORD_API_BASE + "/users/@me/guilds"))
            .timeout(Duration.ofSeconds(10))
            .header("Authorization", "Bearer " + accessToken)
            .GET()
            .build();
    DiscordGuild[] guilds = send(request, DiscordGuild[].class);
    return List.of(guilds);
  }

  private <T> T send(HttpRequest request, Class<T> type) {
    try {
      HttpResponse<String> response =
          httpClient.send(request, HttpResponse.BodyHandlers.ofString());
      if (response.statusCode() < 200 || response.statusCode() >= 300) {
        throw new AuthenticationRequiredException("Discord OAuth2 request failed");
      }
      return objectMapper.readValue(response.body(), type);
    } catch (IOException ex) {
      throw new AuthenticationRequiredException("Failed to read Discord OAuth2 response");
    } catch (InterruptedException ex) {
      Thread.currentThread().interrupt();
      throw new AuthenticationRequiredException("Discord OAuth2 request interrupted");
    }
  }

  private boolean isManageable(DiscordGuild guild) {
    if (guild.owner()) {
      return true;
    }
    if (guild.permissions() == null || guild.permissions().isBlank()) {
      return false;
    }
    BigInteger permissions = new BigInteger(guild.permissions());
    return permissions.and(ADMINISTRATOR).signum() != 0
        || permissions.and(MANAGE_GUILD).signum() != 0;
  }

  private void requireConfigured() {
    requireClientId();
    if (clientSecret.isBlank() || redirectUri.isBlank()) {
      throw new IllegalStateException("Discord OAuth2 is not configured");
    }
  }

  private void requireClientId() {
    if (clientId.isBlank()) {
      throw new IllegalStateException("Discord OAuth2 client id is not configured");
    }
  }

  private String form(Map<String, String> params) {
    return params.entrySet().stream()
        .map(entry -> encode(entry.getKey()) + "=" + encode(entry.getValue()))
        .reduce((left, right) -> left + "&" + right)
        .orElse("");
  }

  private String encode(String value) {
    return URLEncoder.encode(value, StandardCharsets.UTF_8);
  }

  public record DiscordOAuthProfile(
      String userId, String username, String globalName, List<AuthorizedGuild> manageableGuilds) {}

  @JsonIgnoreProperties(ignoreUnknown = true)
  private record TokenResponse(@JsonProperty("access_token") String accessToken) {}

  @JsonIgnoreProperties(ignoreUnknown = true)
  private record DiscordUser(
      String id, String username, @JsonProperty("global_name") String globalName) {}

  @JsonIgnoreProperties(ignoreUnknown = true)
  private record DiscordGuild(String id, String name, boolean owner, String permissions) {}
}
