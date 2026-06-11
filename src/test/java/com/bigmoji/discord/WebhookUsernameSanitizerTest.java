package com.bigmoji.discord;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class WebhookUsernameSanitizerTest {

  @Test
  void sanitizesNormalName() {
    assertEquals("Alice БОТ", WebhookUsernameSanitizer.sanitize("Alice"));
  }

  @Test
  void removesAtSign() {
    assertEquals("lice123 БОТ", WebhookUsernameSanitizer.sanitize("@lice123"));
  }

  @Test
  void removesHashSign() {
    assertEquals("alice123 БОТ", WebhookUsernameSanitizer.sanitize("alice#123"));
  }

  @Test
  void removesBothAtAndHash() {
    assertEquals("lice123 БОТ", WebhookUsernameSanitizer.sanitize("@lice#123"));
  }

  @Test
  void truncatesLongName() {
    String longName = "A".repeat(50);
    String result = WebhookUsernameSanitizer.sanitize(longName);
    assertEquals(32, result.length());
    assertEquals("A".repeat(28) + " БОТ", result);
  }

  @Test
  void handlesExactly28CharName() {
    String name = "A".repeat(28);
    assertEquals("A".repeat(28) + " БОТ", WebhookUsernameSanitizer.sanitize(name));
  }

  @Test
  void handlesNullName() {
    assertEquals("БОТ", WebhookUsernameSanitizer.sanitize(null));
  }

  @Test
  void handlesBlankName() {
    assertEquals("БОТ", WebhookUsernameSanitizer.sanitize(""));
    assertEquals("БОТ", WebhookUsernameSanitizer.sanitize("   "));
  }

  @Test
  void handlesNameThatBecomesEmptyAfterSanitization() {
    assertEquals("БОТ", WebhookUsernameSanitizer.sanitize("@@@"));
    assertEquals("БОТ", WebhookUsernameSanitizer.sanitize("###"));
  }

  @Test
  void trimsWhitespace() {
    assertEquals("Alice БОТ", WebhookUsernameSanitizer.sanitize("  Alice  "));
  }

  @Test
  void handlesNameWithSpaces() {
    assertEquals("Alice Bob БОТ", WebhookUsernameSanitizer.sanitize("Alice Bob"));
  }
}
