package com.bigmoji.emoji;

import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Pattern;
import net.fellbaum.jemoji.Emoji;
import net.fellbaum.jemoji.EmojiManager;

public class EmojiNormalizer {
  private static final Pattern DISCORD_CUSTOM_EMOJI =
      Pattern.compile("^<a?:([a-zA-Z0-9_]+):\\d+>$");
  private static final Pattern SHORTCODE = Pattern.compile("^:([a-zA-Z0-9_+-]+):$");
  private static final Pattern PLAIN_EMOJI_NAME = Pattern.compile("^[a-zA-Z0-9_+-]+$");

  private static final Map<String, String> LEGACY_DEFAULT_EMOJI_TO_CODE =
      Map.of(
          "😊", "smile",
          "❤️", "heart",
          "❤", "heart",
          "🎉", "party",
          "👍", "thumbsup",
          "🔥", "fire",
          "😢", "cry",
          "😮", "open_mouth",
          "😔", "pensive",
          "🫩", "face_with_bags_under_eyes");

  private static final Set<String> APPLICATION_SHORTCODES =
      Set.of(
          "smile",
          "heart",
          "party",
          "thumbsup",
          "fire",
          "cry",
          "open_mouth",
          "pensive",
          "face_with_bags_under_eyes");

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
      return normalizeEmojiName(shortcode.group(1));
    }

    if (PLAIN_EMOJI_NAME.matcher(trimmed).matches()) {
      return normalizeEmojiName(trimmed);
    }

    String withoutSkinTone = removeSkinTone(trimmed);
    String withoutVariationSelector = withoutSkinTone.replace("\uFE0F", "");

    String defaultCode = LEGACY_DEFAULT_EMOJI_TO_CODE.get(withoutSkinTone);
    if (defaultCode == null) {
      defaultCode = LEGACY_DEFAULT_EMOJI_TO_CODE.get(withoutVariationSelector);
    }
    if (defaultCode != null) {
      return defaultCode;
    }

    return canonicalDiscordAlias(withoutSkinTone)
        .or(() -> canonicalDiscordAlias(withoutVariationSelector))
        .orElse(trimmed);
  }

  private String normalizeEmojiName(String rawName) {
    String name = rawName.toLowerCase(Locale.ROOT);
    if (APPLICATION_SHORTCODES.contains(name)) {
      return name;
    }

    return EmojiManager.getByAlias(name)
        .flatMap(
            emojis ->
                emojis.stream()
                    .filter(emoji -> hasDiscordAlias(emoji, name))
                    .findFirst()
                    .flatMap(this::firstDiscordAlias))
        .orElse(name);
  }

  private Optional<String> canonicalDiscordAlias(String unicodeEmoji) {
    return EmojiManager.getEmoji(unicodeEmoji).flatMap(this::firstDiscordAlias);
  }

  private Optional<String> firstDiscordAlias(Emoji emoji) {
    return emoji.getDiscordAliases().stream().findFirst().map(this::stripColons);
  }

  private boolean hasDiscordAlias(Emoji emoji, String name) {
    return emoji.getDiscordAliases().stream().map(this::stripColons).anyMatch(name::equals);
  }

  private String stripColons(String alias) {
    if (alias.length() >= 2 && alias.charAt(0) == ':' && alias.charAt(alias.length() - 1) == ':') {
      return alias.substring(1, alias.length() - 1).toLowerCase(Locale.ROOT);
    }
    return alias.toLowerCase(Locale.ROOT);
  }

  private String removeSkinTone(String value) {
    StringBuilder result = new StringBuilder(value.length());
    value
        .codePoints()
        .filter(codePoint -> codePoint < 0x1F3FB || codePoint > 0x1F3FF)
        .forEach(result::appendCodePoint);
    return result.toString();
  }
}
