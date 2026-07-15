package com.bigmoji.sticker;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.bigmoji.domain.entity.StickerMapping;
import com.bigmoji.domain.repository.StickerMappingRepository;
import java.util.List;
import org.junit.jupiter.api.Test;

class StickerMappingCacheTest {
  @Test
  void refreshesCustomEmojiMentionsUnderNormalizedName() {
    StickerMappingRepository repository = mock(StickerMappingRepository.class);
    StickerMapping mapping = new StickerMapping();
    mapping.setGuildId("guild-1");
    mapping.setEmojiName("<:aaa:933444648909832222>");

    when(repository.findByGuildIdAndEmojiName("guild-1", "aaa")).thenReturn(List.of());
    when(repository.findByGuildId("guild-1")).thenReturn(List.of(mapping));

    StickerMappingCache cache = new StickerMappingCache(repository);
    cache.refreshGuildEmoji("guild-1", "aaa");

    List<StickerMapping> found = cache.find("guild-1", "<:aaa:933444648909832222>");

    assertEquals(1, found.size());
    assertSame(mapping, found.getFirst());
    verify(repository).findByGuildIdAndEmojiName("guild-1", "aaa");
    verify(repository).findByGuildId("guild-1");
  }

  @Test
  void resolvesPersistedStandardShortcodeFromIncomingUnicode() {
    StickerMappingRepository repository = mock(StickerMappingRepository.class);
    StickerMapping mapping = new StickerMapping();
    mapping.setGuildId("guild-1");
    mapping.setEmojiName(":innocent:");

    when(repository.findByGuildIdAndEmojiName("guild-1", "innocent")).thenReturn(List.of());
    when(repository.findByGuildId("guild-1")).thenReturn(List.of(mapping));

    StickerMappingCache cache = new StickerMappingCache(repository);
    cache.refreshGuildEmoji("guild-1", "😇");

    List<StickerMapping> found = cache.find("guild-1", "😇");

    assertEquals(1, found.size());
    assertSame(mapping, found.getFirst());
    verify(repository).findByGuildIdAndEmojiName("guild-1", "innocent");
    verify(repository).findByGuildId("guild-1");
  }
}
