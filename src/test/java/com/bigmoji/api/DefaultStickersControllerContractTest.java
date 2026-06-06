package com.bigmoji.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;

import com.bigmoji.sticker.DefaultStickerInitializer;
import com.bigmoji.domain.repository.StickerMappingRepository;
import org.junit.jupiter.api.Test;

class DefaultStickersControllerContractTest {
  @Test
  void returnsDefaults() {
    DefaultStickerInitializer i = new DefaultStickerInitializer(mock(StickerMappingRepository.class));
    DefaultStickersController c = new DefaultStickersController(i);
    assertEquals(5, c.defaults().size());
  }
}
