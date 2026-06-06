package com.bigmoji.sticker;

import com.bigmoji.domain.entity.StickerMapping;
import com.bigmoji.domain.repository.StickerMappingRepository;
import jakarta.annotation.PostConstruct;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Component;

@Component
public class StickerMappingCache {
  private final StickerMappingRepository repository;
  private final Map<String, List<StickerMapping>> cache = new ConcurrentHashMap<>();

  public StickerMappingCache(StickerMappingRepository repository) {
    this.repository = repository;
  }

  @PostConstruct
  public void bootstrap() {
    refreshAll();
  }

  public void refreshAll() {
    cache.clear();
    repository
        .findAll()
        .forEach(
            mapping ->
                cache
                    .computeIfAbsent(
                        key(mapping.getGuildId(), mapping.getEmojiName()),
                        k -> new java.util.ArrayList<>())
                    .add(mapping));
  }

  public void refreshGuildEmoji(String guildId, String emojiName) {
    cache.put(key(guildId, emojiName), repository.findByGuildIdAndEmojiName(guildId, emojiName));
  }

  public List<StickerMapping> find(String guildId, String emojiName) {
    return cache.getOrDefault(key(guildId, emojiName), List.of());
  }

  private String key(String guildId, String emojiName) {
    return guildId + ":" + emojiName;
  }
}
