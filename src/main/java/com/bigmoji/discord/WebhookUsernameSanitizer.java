package com.bigmoji.discord;

/**
 * Sanitizes Discord usernames for use in webhook impersonation.
 *
 * <p>Discord webhook usernames must:
 *
 * <ul>
 *   <li>Be at most 32 characters long
 *   <li>Not contain {@code @} or {@code #} characters
 * </ul>
 *
 * <p>This utility appends a " БОТ" suffix to indicate the message is bot-generated, leaving 28
 * characters for the original author name.
 */
public final class WebhookUsernameSanitizer {

  private static final String BOT_SUFFIX = " БОТ";
  private static final int MAX_USERNAME_LENGTH = 32;
  private static final int MAX_NAME_LENGTH = MAX_USERNAME_LENGTH - BOT_SUFFIX.length(); // 28

  private WebhookUsernameSanitizer() {}

  /**
   * Sanitizes a raw author name for use as a Discord webhook username.
   *
   * <p>Steps:
   *
   * <ol>
   *   <li>Remove {@code @} and {@code #} characters
   *   <li>Trim leading/trailing whitespace
   *   <li>Truncate to {@value #MAX_NAME_LENGTH} characters
   *   <li>Append {@value #BOT_SUFFIX}
   * </ol>
   *
   * @param rawName the original author display name
   * @return sanitized username with " БОТ" suffix, max 32 characters
   */
  public static String sanitize(String rawName) {
    if (rawName == null || rawName.isBlank()) {
      return BOT_SUFFIX.trim();
    }

    String cleaned = rawName.replace("@", "").replace("#", "").trim();

    if (cleaned.isEmpty()) {
      return BOT_SUFFIX.trim();
    }

    if (cleaned.length() > MAX_NAME_LENGTH) {
      cleaned = cleaned.substring(0, MAX_NAME_LENGTH);
    }

    return cleaned + BOT_SUFFIX;
  }
}
