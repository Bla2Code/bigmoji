package com.bigmoji.api;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.bigmoji.sticker.DefaultStickerInitializer;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.DefaultResourceLoader;

class DefaultStickersControllerContractTest {
  @Test
  void returnsDefaults() {
    DefaultStickerInitializer i = new DefaultStickerInitializer(new DefaultResourceLoader());
    DefaultStickersController c = new DefaultStickersController(i);
    assertEquals(5, c.defaults().size());
  }
}
