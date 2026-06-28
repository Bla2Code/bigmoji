package com.bigmoji.sticker;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.bigmoji.domain.entity.StickerMapping;
import com.bigmoji.domain.repository.StickerMappingRepository;
import com.bigmoji.storage.MinioStorageService;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

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
    verify(cache).refreshGuildEmoji("1", "smile");
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
    verify(cache).refreshGuildEmoji("1", "smile");
  }

  @Test
  void resolvesFallbackWhenCustomMappingMissing() throws Exception {
    StickerMappingRepository repo = mock(StickerMappingRepository.class);
    StickerMappingCache cache = mock(StickerMappingCache.class);
    MinioStorageService storage = mock(MinioStorageService.class);
    DefaultStickerInitializer initializer = mock(DefaultStickerInitializer.class);
    StickerAsset fallback =
        new StickerAsset("heart.png", new byte[] {1}, true, "default/heart.png");
    when(cache.find("guild-1", "heart")).thenReturn(List.of());
    when(initializer.fallbackFor("heart")).thenReturn(Optional.of(fallback));

    StickerMappingService service =
        new StickerMappingService(repo, cache, storage, Optional.of(initializer));

    Optional<StickerAsset> resolved = service.resolveSticker("guild-1", "heart");

    assertTrue(resolved.isPresent());
    assertTrue(resolved.get().isDefault());
    verify(initializer).fallbackFor("heart");
    verify(cache).refreshGuildEmoji("guild-1", "heart");
  }

  @Test
  void keepsCustomEmojiDisplayValueAndRefreshesNormalizedName() throws Exception {
    StickerMappingRepository repo = mock(StickerMappingRepository.class);
    StickerMappingCache cache = mock(StickerMappingCache.class);
    MinioStorageService storage = mock(MinioStorageService.class);
    MockMultipartFile file =
        new MockMultipartFile("file", "sticker.png", "image/png", new byte[] {1, 2, 3});

    when(storage.upload(eq("guild-1"), eq(".png"), any(), eq(3L), eq("image/png")))
        .thenReturn("stickers/sticker.png");
    when(storage.bucketForGuild("guild-1")).thenReturn("bucket-guild-1");
    when(repo.save(any(StickerMapping.class)))
        .thenAnswer(invocation -> invocation.getArgument(0, StickerMapping.class));

    StickerMappingService service =
        new StickerMappingService(repo, cache, storage, Optional.empty());

    StickerMapping saved = service.create("guild-1", "<:aaa:933444648909832222>", file, false);

    assertEquals("<:aaa:933444648909832222>", saved.getEmojiName());
    verify(cache).refreshGuildEmoji("guild-1", "aaa");
  }

  @Test
  void downloadsPreviewBytesForSpecificMapping() throws Exception {
    StickerMappingRepository repo = mock(StickerMappingRepository.class);
    StickerMappingCache cache = mock(StickerMappingCache.class);
    MinioStorageService storage = mock(MinioStorageService.class);
    StickerMapping mapping = new StickerMapping();
    mapping.setMinioBucketName("bucket-guild-1");
    mapping.setMinioObjectKey("stickers/sticker.png");
    when(storage.downloadFile("bucket-guild-1", "stickers/sticker.png"))
        .thenReturn(new byte[] {1, 2, 3});

    StickerMappingService service =
        new StickerMappingService(repo, cache, storage, Optional.empty());

    assertArrayEquals(new byte[] {1, 2, 3}, service.previewBytesFor(mapping));
    verify(storage).downloadFile("bucket-guild-1", "stickers/sticker.png");
  }
}
