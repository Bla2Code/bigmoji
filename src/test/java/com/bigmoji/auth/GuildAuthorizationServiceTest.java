package com.bigmoji.auth;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.Test;

class GuildAuthorizationServiceTest {
  private final GuildAuthorizationService service = new GuildAuthorizationService();

  @Test
  void allowsManageableGuild() {
    AuthSession session =
        new AuthSession(
            "user-1",
            "alice",
            "Alice",
            List.of(new AuthorizedGuild("guild-1", "Guild")),
            Instant.now().plusSeconds(60));

    assertDoesNotThrow(() -> service.requireManageAccess(session, "guild-1"));
  }

  @Test
  void rejectsGuildOutsideSession() {
    AuthSession session =
        new AuthSession(
            "user-1",
            "alice",
            "Alice",
            List.of(new AuthorizedGuild("guild-1", "Guild")),
            Instant.now().plusSeconds(60));

    assertThrows(
        GuildAccessDeniedException.class, () -> service.requireManageAccess(session, "guild-2"));
  }
}
