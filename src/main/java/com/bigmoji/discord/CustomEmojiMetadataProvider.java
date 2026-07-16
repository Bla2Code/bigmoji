package com.bigmoji.discord;

import jakarta.annotation.Nullable;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.regex.Pattern;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.emoji.RichCustomEmoji;
import net.fellbaum.jemoji.Emoji;
import net.fellbaum.jemoji.EmojiManager;
import org.springframework.stereotype.Component;

@Component
public class CustomEmojiMetadataProvider {
  private static final Pattern DISCORD_CUSTOM_EMOJI =
      Pattern.compile("^<(a?):([a-zA-Z0-9_]+):(\\d+)>$");
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

    RichCustomEmoji emoji = null;
    if (jda != null && guildId != null && !guildId.isBlank()) {
      Guild guild = jda.getGuildById(guildId);
      if (guild != null) {
        if (parsed.id() != null) {
          emoji = guild.getEmojiById(parsed.id());
        }
        if (emoji == null) {
          emoji = guild.getEmojisByName(parsed.name(), true).stream().findFirst().orElse(null);
        }
      }
    }

    if (emoji == null && jda != null && parsed.id() == null) {
      List<RichCustomEmoji> matches = jda.getEmojisByName(parsed.name(), true);
      if (matches.size() == 1) {
        emoji = matches.get(0);
      }
    }

    if (emoji != null) {
      String imageUrl = emoji.getImageUrl();
      return Optional.of(
          new CustomEmojiMetadata(
              emoji.getId(),
              emoji.getName(),
              shortcode(emoji.getName()),
              imageUrl,
              emoji.isAnimated(),
              imageUrl != null && !imageUrl.isBlank(),
              null));
    }

    if (parsed.id() == null) {
      Optional<Emoji> standardEmoji = standardEmojiFor(parsed.name());
      if (standardEmoji.isPresent()) {
        return Optional.of(
            new CustomEmojiMetadata(
                null,
                parsed.name(),
                shortcode(parsed.name()),
                null,
                false,
                true,
                standardEmoji.get().getEmoji()));
      }
    } else {
      return Optional.of(cdnFallback(parsed));
    }

    return Optional.of(unavailable(parsed));
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
              customEmoji.group(2), customEmoji.group(3), !customEmoji.group(1).isEmpty()));
    }

    var shortcode = SHORTCODE.matcher(trimmed);
    if (shortcode.matches()) {
      return Optional.of(new ParsedCustomEmoji(shortcode.group(1), null, false));
    }

    return Optional.empty();
  }

  private CustomEmojiMetadata unavailable(ParsedCustomEmoji parsed) {
    return new CustomEmojiMetadata(
        parsed.id(), parsed.name(), shortcode(parsed.name()), null, parsed.animated(), false, null);
  }

  private CustomEmojiMetadata cdnFallback(ParsedCustomEmoji parsed) {
    String extension = parsed.animated() ? ".gif" : ".png";
    String imageUrl =
        "https://cdn.discordapp.com/emojis/"
            + parsed.id()
            + extension
            + "?size=96&quality=lossless";
    return new CustomEmojiMetadata(
        parsed.id(),
        parsed.name(),
        shortcode(parsed.name()),
        imageUrl,
        parsed.animated(),
        true,
        null);
  }

  private Optional<Emoji> standardEmojiFor(String name) {
    String normalizedName = name.toLowerCase(Locale.ROOT);
    return EmojiManager.getByAlias(normalizedName)
        .flatMap(
            emojis ->
                emojis.stream()
                    .filter(
                        emoji ->
                            emoji.getDiscordAliases().stream()
                                .map(this::stripColons)
                                .anyMatch(normalizedName::equals))
                    .findFirst());
  }

  private String stripColons(String alias) {
    if (alias.length() >= 2 && alias.charAt(0) == ':' && alias.charAt(alias.length() - 1) == ':') {
      return alias.substring(1, alias.length() - 1).toLowerCase(Locale.ROOT);
    }
    return alias.toLowerCase(Locale.ROOT);
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
      boolean available,
      String unicodeEmoji) {}

  private record ParsedCustomEmoji(String name, String id, boolean animated) {}
}
