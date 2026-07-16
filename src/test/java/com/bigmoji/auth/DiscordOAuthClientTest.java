package com.bigmoji.auth;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;

class DiscordOAuthClientTest {

  @Test
  void defaultBotInstallPermissionsIncludeManageWebhooks() {
    DiscordOAuthClient client =
        new DiscordOAuthClient(
            new ObjectMapper(),
            "client-id",
            "client-secret",
            "http://localhost/callback",
            DiscordOAuthClient.DEFAULT_BOT_PERMISSIONS);

    Map<String, String> query = parseQuery(client.botInstallUri("guild-id").getRawQuery());

    assertEquals("536996864", query.get("permissions"));
    long permissions = Long.parseLong(query.get("permissions"));
    assertTrue((permissions & (1L << 29)) != 0, "Manage Webhooks permission must be requested");
  }

  private Map<String, String> parseQuery(String rawQuery) {
    return Arrays.stream(rawQuery.split("&"))
        .map(parameter -> parameter.split("=", 2))
        .collect(
            Collectors.toMap(
                pair -> decode(pair[0]), pair -> pair.length > 1 ? decode(pair[1]) : ""));
  }

  private String decode(String value) {
    return URLDecoder.decode(value, StandardCharsets.UTF_8);
  }
}
