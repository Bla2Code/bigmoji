package com.bigmoji.emoji;

import java.util.regex.Pattern;
import org.springframework.stereotype.Component;

@Component
public class EmojiDetector {
  private static final Pattern SHORTCODE = Pattern.compile("^:[a-zA-Z0-9_+-]+:$");

  private final EmojiNormalizer normalizer = new EmojiNormalizer();

  public boolean isSingleEmojiMessage(String content) {
    if (content == null) return false;
    String trimmed = content.trim();
    if (trimmed.isEmpty()) return false;
    if (SHORTCODE.matcher(trimmed).matches()) return true;
    int codePoints = trimmed.codePointCount(0, trimmed.length());
    return codePoints == 1 || (codePoints <= 4 && trimmed.matches("[\\p{So}\\p{Sk}\\uFE0F\\u200D]+"));
  }

  public String normalize(String content) {
    return normalizer.normalize(content);
  }
}
