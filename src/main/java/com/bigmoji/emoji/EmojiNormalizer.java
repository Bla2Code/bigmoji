package com.bigmoji.emoji;

import java.util.Locale;
import java.util.Map;
import java.util.regex.Pattern;

public class EmojiNormalizer {
  private static final Pattern DISCORD_CUSTOM_EMOJI =
      Pattern.compile("^<a?:([a-zA-Z0-9_]+):\\d+>$");
  private static final Pattern SHORTCODE = Pattern.compile("^:([a-zA-Z0-9_+-]+):$");
  private static final Pattern PLAIN_EMOJI_NAME = Pattern.compile("^[a-zA-Z0-9_+-]+$");

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

    var customEmoji = DISCORD_CUSTOM_EMOJI.matcher(trimmed);
    if (customEmoji.matches()) {
      return customEmoji.group(1).toLowerCase(Locale.ROOT);
    }

    var shortcode = SHORTCODE.matcher(trimmed);
    if (shortcode.matches()) {
      return shortcode.group(1).toLowerCase(Locale.ROOT);
    }

    String withoutVariationSelector = trimmed.replace("\uFE0F", "");
    String withoutSkinTone = withoutVariationSelector.replaceAll("[\uD83C\uDFFB-\uD83C\uDFFF]", "");

    String defaultCode = DEFAULT_EMOJI_TO_CODE.get(withoutSkinTone);
    if (defaultCode != null) {
      return defaultCode;
    }

    if (PLAIN_EMOJI_NAME.matcher(trimmed).matches()) {
      return trimmed.toLowerCase(Locale.ROOT);
    }

    return trimmed;
  }
}
