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
 * <p>Discord renders the standard bot badge separately, so this utility must not add a textual
 * "БОТ" marker to the webhook username.
 */
public final class WebhookUsernameSanitizer {

  private static final String BOT_SUFFIX_TEXT = " БОТ";
  private static final String FALLBACK_USERNAME = "Bigmoji";
  private static final int MAX_USERNAME_LENGTH = 32;

  private WebhookUsernameSanitizer() {}

  /**
   * Sanitizes a raw author name for use as a Discord webhook username.
   *
   * <p>Steps:
   *
   * <ol>
   *   <li>Remove {@code @} and {@code #} characters
   *   <li>Trim leading/trailing whitespace
   *   <li>Remove an existing textual " БОТ" suffix, if present
   *   <li>Truncate to {@value #MAX_USERNAME_LENGTH} characters
   * </ol>
   *
   * @param rawName the original author guild display name
   * @return sanitized username, max 32 characters, without a textual bot suffix
   */
  public static String sanitize(String rawName) {
    if (rawName == null || rawName.isBlank()) {
      return FALLBACK_USERNAME;
    }

    String cleaned = rawName.replace("@", "").replace("#", "").trim();

    if (cleaned.isEmpty()) {
      return FALLBACK_USERNAME;
    }

    String baseName = cleaned;
    if (baseName.endsWith(BOT_SUFFIX_TEXT)) {
      baseName = baseName.substring(0, baseName.length() - BOT_SUFFIX_TEXT.length()).trim();
    }

    if (baseName.isEmpty()) {
      return FALLBACK_USERNAME;
    }

    if (baseName.length() > MAX_USERNAME_LENGTH) {
      baseName = baseName.substring(0, MAX_USERNAME_LENGTH);
    }

    return baseName;
  }
}
