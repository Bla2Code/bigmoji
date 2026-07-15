package com.bigmoji.sticker;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Set;
import javax.imageio.ImageIO;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.DefaultResourceLoader;

class DefaultStickerInitializerTest {
  private static final Set<String> DEFAULT_KEYS =
      Set.of(
          "smile",
          "heart",
          "party",
          "thumbsup",
          "fire",
          "cry",
          "open_mouth",
          "pensive",
          "face_with_bags_under_eyes");

  @Test
  void providesNineDefaults() {
    DefaultStickerInitializer init = new DefaultStickerInitializer(new DefaultResourceLoader());

    assertEquals(9, init.defaults().size());
    assertEquals(
        DEFAULT_KEYS,
        init.defaults().stream()
            .map(d -> d.shortcodeName())
            .collect(java.util.stream.Collectors.toSet()));
  }

  @Test
  void loadsFallbackAssetsFromResources() {
    DefaultStickerInitializer init = new DefaultStickerInitializer(new DefaultResourceLoader());

    for (String shortcodeName : DEFAULT_KEYS) {
      assertTrue(init.fallbackFor(shortcodeName).isPresent(), shortcodeName);
    }
  }

  @Test
  void findsCatalogEntriesByShortcodeName() {
    DefaultStickerInitializer init = new DefaultStickerInitializer(new DefaultResourceLoader());

    assertEquals("cry.png", init.findByShortcodeName("cry").orElseThrow().fileName());
    assertTrue(init.findByShortcodeName("../smile").isEmpty());
  }

  @Test
  void smileCatalogEntryUsesDiscordSmileUnicode() {
    DefaultStickerInitializer init = new DefaultStickerInitializer(new DefaultResourceLoader());

    assertEquals("😄", init.findByShortcodeName("smile").orElseThrow().emojiName());
  }

  @Test
  void bundledAssetsUseDiscordOrientedSquareCanvas() throws Exception {
    DefaultStickerInitializer init = new DefaultStickerInitializer(new DefaultResourceLoader());
    ClassLoader classLoader = Thread.currentThread().getContextClassLoader();

    for (var def : init.defaults()) {
      try (var inputStream =
          classLoader.getResourceAsStream("default-stickers/" + def.fileName())) {
        assertTrue(inputStream != null, def.fileName());
        var image = ImageIO.read(inputStream);
        assertEquals(320, image.getWidth(), def.fileName());
        assertEquals(320, image.getHeight(), def.fileName());
      }
    }
  }
}
