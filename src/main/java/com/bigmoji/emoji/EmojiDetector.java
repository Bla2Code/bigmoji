package com.bigmoji.emoji;

import java.util.regex.Pattern;
import net.fellbaum.jemoji.EmojiManager;
import org.springframework.stereotype.Component;

@Component
public class EmojiDetector {
  private static final Pattern SHORTCODE = Pattern.compile("^:[a-zA-Z0-9_+-]+:$");
  private static final Pattern DISCORD_CUSTOM_EMOJI = Pattern.compile("^<a?:[a-zA-Z0-9_]+:\\d+>$");

  private final EmojiNormalizer normalizer = new EmojiNormalizer();

  public boolean isSingleEmojiMessage(String content) {
    if (content == null) return false;
    var trimmed = content.trim();
    if (trimmed.isEmpty()) return false;
    if (DISCORD_CUSTOM_EMOJI.matcher(trimmed).matches()) return true;
    if (SHORTCODE.matcher(trimmed).matches()) return true;
    return EmojiManager.isEmoji(trimmed);
  }

  public String normalize(String content) {
    return normalizer.normalize(content);
  }
}
