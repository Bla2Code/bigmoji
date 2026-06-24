package com.bigmoji.api.dto;

import com.bigmoji.auth.AuthSession;
import com.bigmoji.auth.AuthorizedGuild;
import java.time.Instant;
import java.util.List;

public record AuthMeResponse(
    String userId,
    String username,
    String globalName,
    List<AuthorizedGuild> manageableGuilds,
    Instant expiresAt) {
  public static AuthMeResponse from(AuthSession session) {
    return new AuthMeResponse(
        session.userId(),
        session.username(),
        session.globalName(),
        session.manageableGuilds(),
        session.expiresAt());
  }
}
