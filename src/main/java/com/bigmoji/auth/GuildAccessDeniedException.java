package com.bigmoji.auth;

public class GuildAccessDeniedException extends RuntimeException {
  public GuildAccessDeniedException(String guildId) {
    super("User is not authorized to manage guild " + guildId);
  }
}
