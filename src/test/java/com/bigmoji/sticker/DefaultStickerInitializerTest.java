package com.bigmoji.sticker;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;

import com.bigmoji.domain.repository.StickerMappingRepository;
import org.junit.jupiter.api.Test;

class DefaultStickerInitializerTest {
  @Test
  void providesFiveDefaults() {
    DefaultStickerInitializer init =
        new DefaultStickerInitializer(
            mock(StickerMappingRepository.class),
            mock(com.bigmoji.storage.MinioStorageService.class),
            mock(org.springframework.core.io.ResourceLoader.class));
    assertEquals(5, init.defaults().size());
  }
}
