package com.bigmoji.emoji;

import java.util.Map;

public class EmojiNormalizer {
  private static final Map<String, String> DEFAULT_EMOJI_TO_CODE =
      Map.of(
          "😊", "smile",
          "❤️", "heart",
          "❤", "heart",
          "🎉", "party",
          "👍", "thumbsup",
          "🔥", "fire");

  public String normalize(String raw) {
    if (raw == null) {
      return "";
    }

    String trimmed = raw.trim();
    if (trimmed.isEmpty()) {
      return "";
    }

    if (trimmed.startsWith(":") && trimmed.endsWith(":") && trimmed.length() > 2) {
      return trimmed.substring(1, trimmed.length() - 1).toLowerCase();
    }

    String withoutVariationSelector = trimmed.replace("\uFE0F", "");
    String withoutSkinTone = withoutVariationSelector.replaceAll("[\uD83C\uDFFB-\uD83C\uDFFF]", "");

    return DEFAULT_EMOJI_TO_CODE.getOrDefault(withoutSkinTone, trimmed);
  }
}
