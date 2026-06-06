package com.bigmoji.emoji;

public class EmojiNormalizer {
  public String normalize(String raw) {
    if (raw == null) {
      return "";
    }
    String trimmed = raw.trim();
    if (trimmed.startsWith(":") && trimmed.endsWith(":") && trimmed.length() > 2) {
      return trimmed.substring(1, trimmed.length() - 1).toLowerCase();
    }
    return trimmed;
  }
}
