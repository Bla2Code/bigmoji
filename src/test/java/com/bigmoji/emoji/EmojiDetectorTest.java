package com.bigmoji.emoji;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class EmojiDetectorTest {
  private final EmojiDetector detector = new EmojiDetector();

  @Test
  void detectsSingleEmojiAndShortcode() {
    assertTrue(detector.isSingleEmojiMessage("😊"));
    assertTrue(detector.isSingleEmojiMessage("👨‍👩‍👧‍👦"));
    assertTrue(detector.isSingleEmojiMessage("😢"));
    assertTrue(detector.isSingleEmojiMessage("🫩"));
    assertTrue(detector.isSingleEmojiMessage(":smile:"));
    assertTrue(detector.isSingleEmojiMessage(":face_with_bags_under_eyes:"));
    assertTrue(detector.isSingleEmojiMessage("<:aaa:933444648909832222>"));
    assertTrue(detector.isSingleEmojiMessage("<a:partyblob:933444648909832222>"));
  }

  @Test
  void rejectsMixedOrMultiple() {
    assertFalse(detector.isSingleEmojiMessage("😊 😊"));
    assertFalse(
        detector.isSingleEmojiMessage("<:aaa:933444648909832222> <:bbb:933444648909832223>"));
    assertFalse(detector.isSingleEmojiMessage("hi 😊"));
    assertFalse(detector.isSingleEmojiMessage("a"));
    assertFalse(detector.isSingleEmojiMessage("   "));
  }

  @Test
  void normalizesDefaultEmojiToCanonicalCode() {
    assertEquals("smile", detector.normalize("😊"));
    assertEquals("smile", detector.normalize("😄"));
    assertEquals("heart", detector.normalize("❤️"));
    assertEquals("thumbsup", detector.normalize("👍🏻"));
    assertEquals("fire", detector.normalize("🔥"));
    assertEquals("party", detector.normalize(":party:"));
    assertEquals("aaa", detector.normalize("<:aaa:933444648909832222>"));
    assertEquals("partyblob", detector.normalize("<a:PartyBlob:933444648909832222>"));
  }

  @Test
  void normalizesStandardUnicodeAndDiscordShortcodesFromEmojiMetadata() {
    assertEquals("innocent", detector.normalize("😇"));
    assertEquals("innocent", detector.normalize(":innocent:"));
    assertEquals("grimacing", detector.normalize("😬"));
    assertEquals("grimacing", detector.normalize(":grimacing:"));
    assertEquals(detector.normalize(":family_mwgb:"), detector.normalize("👨‍👩‍👧‍👦"));
  }

  @Test
  void normalizesNewDefaultEmojiToCanonicalCode() {
    assertEquals("cry", detector.normalize("😢"));
    assertEquals("open_mouth", detector.normalize("😮"));
    assertEquals("pensive", detector.normalize("😔"));
    assertEquals("face_with_bags_under_eyes", detector.normalize("🫩"));
    assertEquals("cry", detector.normalize(":cry:"));
    assertEquals("open_mouth", detector.normalize(":open_mouth:"));
    assertEquals("pensive", detector.normalize(":pensive:"));
    assertEquals("face_with_bags_under_eyes", detector.normalize(":face_with_bags_under_eyes:"));
  }
}
