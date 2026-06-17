package com.bigmoji.discord;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class WebhookUsernameSanitizerTest {

  @Test
  void sanitizesNormalName() {
    assertEquals("Alice", WebhookUsernameSanitizer.sanitize("Alice"));
  }

  @Test
  void removesAtSign() {
    assertEquals("lice123", WebhookUsernameSanitizer.sanitize("@lice123"));
  }

  @Test
  void removesHashSign() {
    assertEquals("alice123", WebhookUsernameSanitizer.sanitize("alice#123"));
  }

  @Test
  void removesBothAtAndHash() {
    assertEquals("lice123", WebhookUsernameSanitizer.sanitize("@lice#123"));
  }

  @Test
  void truncatesLongName() {
    String longName = "A".repeat(50);
    String result = WebhookUsernameSanitizer.sanitize(longName);
    assertEquals(32, result.length());
    assertEquals("A".repeat(32), result);
  }

  @Test
  void handlesExactly32CharName() {
    String name = "A".repeat(32);
    assertEquals("A".repeat(32), WebhookUsernameSanitizer.sanitize(name));
  }

  @Test
  void handlesNullName() {
    assertEquals("Bigmoji", WebhookUsernameSanitizer.sanitize(null));
  }

  @Test
  void handlesBlankName() {
    assertEquals("Bigmoji", WebhookUsernameSanitizer.sanitize(""));
    assertEquals("Bigmoji", WebhookUsernameSanitizer.sanitize("   "));
  }

  @Test
  void handlesNameThatBecomesEmptyAfterSanitization() {
    assertEquals("Bigmoji", WebhookUsernameSanitizer.sanitize("@@@"));
    assertEquals("Bigmoji", WebhookUsernameSanitizer.sanitize("###"));
  }

  @Test
  void trimsWhitespace() {
    assertEquals("Alice", WebhookUsernameSanitizer.sanitize("  Alice  "));
  }

  @Test
  void handlesNameWithSpaces() {
    assertEquals("Alice Bob", WebhookUsernameSanitizer.sanitize("Alice Bob"));
  }

  @Test
  void preservesGuildNicknameCasingAndUnderscore() {
    assertEquals("Ne_Tort", WebhookUsernameSanitizer.sanitize("Ne_Tort"));
  }

  @Test
  void removesTextualBotSuffix() {
    assertEquals("Ne_Tort", WebhookUsernameSanitizer.sanitize("Ne_Tort БОТ"));
  }
}
