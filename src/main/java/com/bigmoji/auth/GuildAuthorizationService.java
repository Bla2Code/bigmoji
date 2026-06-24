package com.bigmoji.auth;

import org.springframework.stereotype.Service;

@Service
public class GuildAuthorizationService {
  public void requireManageAccess(AuthSession session, String guildId) {
    if (!session.canManageGuild(guildId)) {
      throw new GuildAccessDeniedException(guildId);
    }
  }
}
