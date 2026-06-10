package com.bigmoji.emoji;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class EmojiDetectorTest {
  private final EmojiDetector detector = new EmojiDetector();

  @Test
  void detectsSingleEmojiAndShortcode() {
    assertTrue(detector.isSingleEmojiMessage("😊"));
    assertTrue(detector.isSingleEmojiMessage(":smile:"));
  }

  @Test
  void rejectsMixedOrMultiple() {
    assertFalse(detector.isSingleEmojiMessage("😊 😊"));
    assertFalse(detector.isSingleEmojiMessage("hi 😊"));
    assertFalse(detector.isSingleEmojiMessage("   "));
  }

  @Test
  void normalizesDefaultEmojiToCanonicalCode() {
    assertEquals("smile", detector.normalize("😊"));
    assertEquals("heart", detector.normalize("❤️"));
    assertEquals("thumbsup", detector.normalize("👍🏻"));
    assertEquals("party", detector.normalize(":party:"));
  }
}
