package com.bigmoji.discord;

import jakarta.annotation.Nullable;
import java.util.Locale;
import java.util.Optional;
import java.util.regex.Pattern;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.emoji.RichCustomEmoji;
import org.springframework.stereotype.Component;

@Component
public class CustomEmojiMetadataProvider {
  private static final Pattern DISCORD_CUSTOM_EMOJI =
      Pattern.compile("^<a?:([a-zA-Z0-9_]+):(\\d+)>$");
  private static final Pattern SHORTCODE = Pattern.compile("^:([a-zA-Z0-9_+-]+):$");

  @Nullable private final JDA jda;

  public CustomEmojiMetadataProvider(@Nullable JDA jda) {
    this.jda = jda;
  }

  public Optional<CustomEmojiMetadata> findFor(String guildId, String emojiName) {
    ParsedCustomEmoji parsed = parse(emojiName).orElse(null);
    if (parsed == null) {
      return Optional.empty();
    }

    if (jda == null || guildId == null || guildId.isBlank()) {
      return Optional.of(unavailable(parsed));
    }

    Guild guild = jda.getGuildById(guildId);
    if (guild == null) {
      return Optional.of(unavailable(parsed));
    }

    RichCustomEmoji emoji = null;
    if (parsed.id() != null) {
      emoji = guild.getEmojiById(parsed.id());
    }
    if (emoji == null) {
      emoji = guild.getEmojisByName(parsed.name(), true).stream().findFirst().orElse(null);
    }
    if (emoji == null) {
      return Optional.of(unavailable(parsed));
    }

    boolean available = emoji.isAvailable() && emoji.getImageUrl() != null;
    return Optional.of(
        new CustomEmojiMetadata(
            emoji.getId(),
            emoji.getName(),
            shortcode(emoji.getName()),
            available ? emoji.getImageUrl() : null,
            emoji.isAnimated(),
            available));
  }

  private Optional<ParsedCustomEmoji> parse(String raw) {
    if (raw == null) {
      return Optional.empty();
    }

    String trimmed = raw.trim();
    var customEmoji = DISCORD_CUSTOM_EMOJI.matcher(trimmed);
    if (customEmoji.matches()) {
      return Optional.of(
          new ParsedCustomEmoji(
              customEmoji.group(1).toLowerCase(Locale.ROOT), customEmoji.group(2)));
    }

    var shortcode = SHORTCODE.matcher(trimmed);
    if (shortcode.matches()) {
      return Optional.of(new ParsedCustomEmoji(shortcode.group(1).toLowerCase(Locale.ROOT), null));
    }

    return Optional.empty();
  }

  private CustomEmojiMetadata unavailable(ParsedCustomEmoji parsed) {
    return new CustomEmojiMetadata(
        parsed.id(), parsed.name(), shortcode(parsed.name()), null, false, false);
  }

  private static String shortcode(String name) {
    return ":" + name + ":";
  }

  public record CustomEmojiMetadata(
      String id,
      String name,
      String shortcode,
      String imageUrl,
      boolean animated,
      boolean available) {}

  private record ParsedCustomEmoji(String name, String id) {}
}
