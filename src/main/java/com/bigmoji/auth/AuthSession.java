package com.bigmoji.auth;

import java.time.Instant;
import java.util.List;

public record AuthSession(
    String userId,
    String username,
    String globalName,
    List<AuthorizedGuild> manageableGuilds,
    Instant expiresAt) {

  public boolean canManageGuild(String guildId) {
    return manageableGuilds.stream().anyMatch(guild -> guild.id().equals(guildId));
  }
}
