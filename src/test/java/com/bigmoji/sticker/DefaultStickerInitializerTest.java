package com.bigmoji.sticker;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.springframework.core.io.DefaultResourceLoader;

class DefaultStickerInitializerTest {
  @Test
  void providesFiveDefaults() {
    DefaultStickerInitializer init = new DefaultStickerInitializer(new DefaultResourceLoader());
    assertEquals(5, init.defaults().size());
  }

  @Test
  void loadsFallbackAssetFromResources() {
    DefaultStickerInitializer init = new DefaultStickerInitializer(new DefaultResourceLoader());

    assertTrue(init.fallbackFor("smile").isPresent());
  }
}
