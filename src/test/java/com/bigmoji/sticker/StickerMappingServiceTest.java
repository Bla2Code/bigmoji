package com.bigmoji.sticker;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.bigmoji.domain.entity.StickerMapping;
import com.bigmoji.domain.repository.StickerMappingRepository;
import com.bigmoji.storage.MinioStorageService;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class StickerMappingServiceTest {
  @Test
  void returnsEmptyWhenNoMapping() {
    StickerMappingRepository repo = mock(StickerMappingRepository.class);
    StickerMappingCache cache = mock(StickerMappingCache.class);
    MinioStorageService storage = mock(MinioStorageService.class);
    when(cache.find("1", "smile")).thenReturn(List.of());

    StickerMappingService service =
        new StickerMappingService(repo, cache, storage, Optional.empty());
    assertTrue(service.pickRandomMapping("1", "smile").isEmpty());
  }

  @Test
  void returnsAnyMappingWhenAvailable() {
    StickerMappingRepository repo = mock(StickerMappingRepository.class);
    StickerMappingCache cache = mock(StickerMappingCache.class);
    MinioStorageService storage = mock(MinioStorageService.class);
    StickerMapping m = new StickerMapping();
    when(cache.find("1", "smile")).thenReturn(List.of(m));

    StickerMappingService service =
        new StickerMappingService(repo, cache, storage, Optional.empty());
    assertTrue(service.pickRandomMapping("1", "smile").isPresent());
  }
}
